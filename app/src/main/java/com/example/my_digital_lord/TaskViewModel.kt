package com.example.my_digital_lord

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_digital_lord.di.ServiceLocator
import com.example.my_digital_lord.data.remote.ParseRequest
import com.example.my_digital_lord.utils.TaskParser
import com.example.my_digital_lord.utils.ParsedTask   // импорт из TaskParser
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TasksViewModel : ViewModel() {

    private val prefs = App.getInstance().getSharedPreferences("tasks_prefs", Context.MODE_PRIVATE)

    // Правильный Gson с явными типами для сериализатора/десериализатора
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(
            LocalDateTime::class.java,
            JsonSerializer<LocalDateTime> { src, _, _ ->
                JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            }
        )
        .registerTypeAdapter(
            LocalDateTime::class.java,
            JsonDeserializer<LocalDateTime> { json, _, _ ->
                LocalDateTime.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        )
        .create()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _isAddTaskSheetOpen = MutableStateFlow(false)
    val isAddTaskSheetOpen: StateFlow<Boolean> = _isAddTaskSheetOpen.asStateFlow()

    private val _isParsing = MutableStateFlow(false)
    val isParsing: StateFlow<Boolean> = _isParsing.asStateFlow()

    private val _parseError = MutableStateFlow<String?>(null)
    val parseError: StateFlow<String?> = _parseError.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        val json = prefs.getString("tasks", null)
        if (json != null) {
            val type = object : TypeToken<List<Task>>() {}.type
            _tasks.value = gson.fromJson(json, type) ?: emptyList()
        }
        if (_tasks.value.isEmpty()) {
            _tasks.value = listOf(
                Task(
                    title = "Добавь свою первую задачу",
                    deadline = LocalDateTime.now().plusDays(2),
                    priority = Priority.HIGH
                )
            )
            saveTasks()
        }
    }

    private fun saveTasks() {
        val json = gson.toJson(_tasks.value)
        prefs.edit().putString("tasks", json).apply()
    }

    fun openAddTaskSheet() {
        _isAddTaskSheetOpen.value = true
        _parseError.value = null
    }

    fun closeAddTaskSheet() {
        _isAddTaskSheetOpen.value = false
        _isParsing.value = false
        _parseError.value = null
    }

    fun addTaskWithAiParse(rawText: String) {
        viewModelScope.launch {
            _isParsing.value = true
            _parseError.value = null

            try {
                // Сначала пробуем серверный AI
                val serverResult = callServerAi(rawText)
                val (title, deadline, priority) = if (serverResult != null) {
                    Triple(serverResult.title, serverResult.deadline, serverResult.priority)
                } else {
                    val parsed = TaskParser.parse(rawText)
                    Triple(parsed.title, parsed.deadline, parsed.priority)
                }

                val newTask = Task(
                    title = title,
                    description = rawText,
                    deadline = deadline,
                    priority = priority
                )
                _tasks.update { it + newTask }
                saveTasks()
            } catch (e: Exception) {
                _parseError.value = "Ошибка парсинга: ${e.localizedMessage}"
            } finally {
                _isParsing.value = false
                closeAddTaskSheet()
            }
        }
    }

    private suspend fun callServerAi(text: String): ParsedTask? {
        return try {
            val response = ServiceLocator.apiService.parseTask(ParseRequest(text))
            ParsedTask(
                title = response.title,
                deadline = response.deadline?.let { parseIsoDateTime(it) },
                priority = mapPriority(response.priority),
                category = null
            )
        } catch (e: Exception) {
            null
        }
    }

    fun addTaskManually(title: String) {
        val newTask = Task(
            title = title,
            deadline = LocalDateTime.now().plusDays(1),
            priority = Priority.MEDIUM
        )
        _tasks.update { currentTasks -> currentTasks + newTask }
        saveTasks()
    }

    private fun mapPriority(priorityStr: String): Priority {
        return when (priorityStr.uppercase()) {
            "URGENT" -> Priority.URGENT
            "HIGH" -> Priority.HIGH
            "LOW" -> Priority.LOW
            else -> Priority.MEDIUM
        }
    }

    private fun parseIsoDateTime(isoString: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(isoString, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            null
        }
    }

    fun toggleTaskCompletion(taskId: String) {
        _tasks.update { tasks ->
            tasks.map { task ->
                if (task.id == taskId) task.copy(isCompleted = !task.isCompleted) else task
            }
        }
        saveTasks()
    }

    fun deleteTask(taskId: String) {
        _tasks.update { tasks -> tasks.filter { it.id != taskId } }
        saveTasks()
    }

    fun clearParseError() {
        _parseError.value = null
    }
}