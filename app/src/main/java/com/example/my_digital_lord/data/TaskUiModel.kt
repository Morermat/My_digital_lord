package com.example.my_digital_lord.data

import com.example.my_digital_lord.Priority
import com.example.my_digital_lord.Subtask
import com.example.my_digital_lord.TaskCategory
import java.time.LocalDateTime

data class TaskUiModel(
    val id: String,
    val title: String,
    val description: String = "",
    val deadline: LocalDateTime? = null,
    val completed: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val category: TaskCategory = TaskCategory.PERSONAL,
    val hasSubtasks: Boolean = false,
    val subtaskCount: Int = 0,
    val completedSubtasks: Int = 0,
    val subtasks: List<Subtask> = emptyList()  // добавили
)