package com.example.my_digital_lord

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_digital_lord.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TasksViewModel : ViewModel() {

    private val repository = ServiceLocator.taskRepository

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _isAddTaskSheetOpen = MutableStateFlow(false)
    val isAddTaskSheetOpen: StateFlow<Boolean> = _isAddTaskSheetOpen.asStateFlow()

    private val _isParsing = MutableStateFlow(false)
    val isParsing: StateFlow<Boolean> = _isParsing.asStateFlow()

    private val _parseError = MutableStateFlow<String?>(null)
    val parseError: StateFlow<String?> = _parseError.asStateFlow()

    init {
        _tasks.value = listOf(
            Task(
                title = "Добавь свою первую задачу",
                deadline = LocalDateTime.now().plusDays(2),
                priority = Priority.HIGH
            )
        )
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
        addTaskManually(rawText)
        _parseError.value = "Сервер AI-парсинга временно недоступен. Задача создана локально."
        closeAddTaskSheet()
    }

    fun addTaskManually(title: String) {
        val newTask = Task(
            title = title,
            deadline = LocalDateTime.now().plusDays(1),
            priority = Priority.MEDIUM
        )
        _tasks.update { currentTasks -> currentTasks + newTask }
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
    }

    fun deleteTask(taskId: String) {
        _tasks.update { tasks -> tasks.filter { it.id != taskId } }
    }

    fun clearParseError() {
        _parseError.value = null
    }
}