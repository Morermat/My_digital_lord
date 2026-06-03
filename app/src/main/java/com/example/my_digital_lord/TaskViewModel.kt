package com.example.my_digital_lord

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_digital_lord.di.ServiceLocator
import com.example.my_digital_lord.data.remote.ParseRequest
import com.example.my_digital_lord.utils.TaskParser
import com.example.my_digital_lord.utils.ParsedTask
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
import java.util.UUID

class TasksViewModel : ViewModel() {

    private val prefs = App.getInstance().getSharedPreferences("tasks_prefs", Context.MODE_PRIVATE)

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java,
            JsonSerializer<LocalDateTime> { src, _, _ -> JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) }
        )
        .registerTypeAdapter(LocalDateTime::class.java,
            JsonDeserializer<LocalDateTime> { json, _, _ -> LocalDateTime.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
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
            _tasks.value = listOf(Task(
                title = "Добавь свою первую задачу",
                deadline = LocalDateTime.now().plusDays(2),
                priority = Priority.HIGH
            ))
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

    fun updateTask(updatedTask: Task) {
        _tasks.update { tasks -> tasks.map { if (it.id == updatedTask.id) updatedTask else it } }
        saveTasks()
    }

    fun addTaskWithAiParse(rawText: String, category: TaskCategory, priority: Priority) {
        viewModelScope.launch {
            _isParsing.value = true
            _parseError.value = null
            try {
                val parsed = TaskParser.parse(rawText)
                val newTask = Task(
                    title = parsed.title,
                    description = rawText,
                    deadline = parsed.deadline,
                    priority = priority,
                    category = category,
                    isCompleted = false
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

    // ===== Подзадачи =====
    fun toggleSubtask(taskId: String, subtaskId: String) {
        _tasks.update { tasks ->
            tasks.map { task ->
                if (task.id == taskId) {
                    val newSubtasks = task.subtasks.map { subtask ->
                        if (subtask.id == subtaskId) subtask.copy(isCompleted = !subtask.isCompleted)
                        else subtask
                    }
                    task.copy(subtasks = newSubtasks)
                } else task
            }
        }
        saveTasks()
    }

    fun addSubtask(taskId: String, subtaskTitle: String) {
        if (subtaskTitle.isBlank()) return
        _tasks.update { tasks ->
            tasks.map { task ->
                if (task.id == taskId) {
                    val newSubtask = Subtask(title = subtaskTitle)
                    task.copy(subtasks = task.subtasks + newSubtask)
                } else task
            }
        }
        saveTasks()
    }

    fun removeSubtask(taskId: String, subtaskId: String) {
        _tasks.update { tasks ->
            tasks.map { task ->
                if (task.id == taskId) {
                    task.copy(subtasks = task.subtasks.filter { it.id != subtaskId })
                } else task
            }
        }
        saveTasks()
    }

    // ===== Повторение и переключение выполнения =====
    private var pendingRecurringTask: Task? = null

    fun toggleTaskCompletion(taskId: String) {
        _tasks.update { tasks ->
            tasks.map { task ->
                if (task.id == taskId) {
                    val newCompleted = !task.isCompleted
                    val updatedSubtasks = if (newCompleted) {
                        // отмечаем все подзадачи выполненными
                        task.subtasks.map { it.copy(isCompleted = true) }
                    } else {
                        task.subtasks // не меняем
                    }
                    val updatedTask = task.copy(
                        isCompleted = newCompleted,
                        subtasks = updatedSubtasks
                    )
                    if (newCompleted && task.recurrence.type != RecurrenceType.NONE) {
                        val newTask = createRecurringTask(task)
                        pendingRecurringTask = newTask
                        updatedTask
                    } else {
                        updatedTask
                    }
                } else task
            }
        }
        if (pendingRecurringTask != null) {
            _tasks.update { it + pendingRecurringTask!! }
            pendingRecurringTask = null
        }
        saveTasks()
    }

    private fun createRecurringTask(original: Task): Task {
        val newDeadline = calculateNextDeadline(original.deadline, original.recurrence)
        return original.copy(
            id = UUID.randomUUID().toString(),
            isCompleted = false,
            subtasks = original.subtasks.map { it.copy(isCompleted = false, id = UUID.randomUUID().toString()) },
            deadline = newDeadline,
            createdAt = LocalDateTime.now()
        )
    }

    private fun calculateNextDeadline(current: LocalDateTime?, rule: RecurrenceRule): LocalDateTime? {
        if (current == null) return null
        return when (rule.type) {
            RecurrenceType.DAILY -> current.plusDays(rule.interval.toLong())
            RecurrenceType.WEEKLY -> current.plusWeeks(rule.interval.toLong())
            RecurrenceType.MONTHLY -> current.plusMonths(rule.interval.toLong())
            else -> null
        }
    }

    fun addTaskWithAiParse(rawText: String) {
        viewModelScope.launch {
            _isParsing.value = true
            _parseError.value = null
            try {
                val parsed = TaskParser.parse(rawText)
                val newTask = Task(
                    title = parsed.title,
                    description = rawText,
                    deadline = parsed.deadline,
                    priority = parsed.priority
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

    fun addManualTask(title: String, description: String, deadline: LocalDateTime?, priority: Priority, category: TaskCategory) {
        val newTask = Task(
            title = title,
            description = description,
            deadline = deadline,
            priority = priority,
            category = category,
            isCompleted = false
        )
        _tasks.update { it + newTask }
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