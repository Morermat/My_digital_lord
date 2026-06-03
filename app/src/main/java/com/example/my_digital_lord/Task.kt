package com.example.my_digital_lord

import java.time.LocalDateTime
import java.util.UUID

enum class Priority { LOW, MEDIUM, HIGH, URGENT }

enum class TaskCategory {
    ALL, WORK, STUDY, PERSONAL, HEALTH      // ALL – для фильтрации
}

data class Subtask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
)

enum class RecurrenceType {
    NONE, DAILY, WEEKLY, MONTHLY
}

data class RecurrenceRule(
    val type: RecurrenceType = RecurrenceType.NONE,
    val interval: Int = 1,           // каждые N дней/недель/месяцев
    val daysOfWeek: Set<Int>? = null // для еженедельной: дни недели (1=пн...7=вс)
)

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val deadline: LocalDateTime? = null,
    val priority: Priority = Priority.MEDIUM,
    val category: TaskCategory = TaskCategory.PERSONAL,
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val subtasks: List<Subtask> = emptyList(),          // новое
    val recurrence: RecurrenceRule = RecurrenceRule()  // новое
)