package com.example.my_digital_lord.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.my_digital_lord.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    // Основные поля
    var title by remember(task) { mutableStateOf(task.title) }
    var description by remember(task) { mutableStateOf(task.description) }
    var deadlineText by remember(task) {
        mutableStateOf(task.deadline?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) ?: "")
    }
    var priority by remember(task) { mutableStateOf(task.priority) }
    var category by remember(task) { mutableStateOf(task.category) }
    var deadlineError by remember { mutableStateOf<String?>(null) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    // Повторение
    var recurrenceType by remember(task) { mutableStateOf(task.recurrence.type) }
    var recurrenceInterval by remember(task) { mutableStateOf(task.recurrence.interval) }

    // Подзадачи – работаем с копией
    var subtasks by remember(task) { mutableStateOf(task.subtasks.toMutableList()) }
    var newSubtaskText by remember { mutableStateOf("") }
    var isAddingSubtask by remember { mutableStateOf(false) }

    // Ползунок приоритета
    val sliderPosition = remember(priority) {
        mutableStateOf(
            when (priority) {
                Priority.LOW -> 0f
                Priority.MEDIUM -> 1f
                Priority.HIGH -> 2f
                Priority.URGENT -> 3f
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 700.dp),
        title = {
            Text(
                text = "Редактирование задачи",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Название
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Описание
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание (необязательно)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                // Дедлайн
                OutlinedTextField(
                    value = deadlineText,
                    onValueChange = { newValue ->
                        deadlineText = newValue
                        deadlineError = null
                    },
                    label = { Text("Дедлайн (дд.мм.гггг чч:мм)") },
                    isError = deadlineError != null,
                    supportingText = {
                        if (deadlineError != null) Text(deadlineError!!, color = MaterialTheme.colorScheme.error)
                    },
                    placeholder = { Text("Пример: 31.12.2025 18:00") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Категория (выпадающий список)
                Column {
                    Text("Категория", style = MaterialTheme.typography.labelMedium)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { categoryMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = when (category) {
                                    TaskCategory.WORK -> "Работа"
                                    TaskCategory.STUDY -> "Учёба"
                                    TaskCategory.PERSONAL -> "Личное"
                                    TaskCategory.HEALTH -> "Здоровье"
                                    TaskCategory.ALL -> "Все"
                                },
                                modifier = Modifier.fillMaxWidth(),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        DropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            TaskCategory.entries.forEach { cat ->
                                if (cat != TaskCategory.ALL) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                when (cat) {
                                                    TaskCategory.WORK -> "Работа"
                                                    TaskCategory.STUDY -> "Учёба"
                                                    TaskCategory.PERSONAL -> "Личное"
                                                    TaskCategory.HEALTH -> "Здоровье"
                                                    TaskCategory.ALL -> "Все"
                                                }
                                            )
                                        },
                                        onClick = {
                                            category = cat
                                            categoryMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Приоритет (ползунок)
                Column {
                    Text("Приоритет", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = sliderPosition.value,
                        onValueChange = { sliderPosition.value = it },
                        valueRange = 0f..3f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = when (sliderPosition.value.toInt()) {
                                0 -> MaterialTheme.colorScheme.secondary
                                1 -> MaterialTheme.colorScheme.tertiary
                                2 -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.error
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = when (sliderPosition.value.toInt()) {
                            0 -> "Низкий"
                            1 -> "Средний"
                            2 -> "Высокий"
                            else -> "Срочный"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                // ========== ПОДЗАДАЧИ ==========
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Подзадачи", fontWeight = FontWeight.Medium)
                            if (!isAddingSubtask) {
                                TextButton(onClick = { isAddingSubtask = true }) {
                                    Icon(Icons.Default.Add, contentDescription = "Добавить", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Добавить")
                                }
                            }
                        }

                        // Список подзадач
                        subtasks.forEach { subtask ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = subtask.isCompleted,
                                    onCheckedChange = {
                                        val index = subtasks.indexOfFirst { it.id == subtask.id }
                                        if (index != -1) {
                                            subtasks[index] = subtask.copy(isCompleted = !subtask.isCompleted)
                                            subtasks = subtasks.toMutableList()
                                        }
                                    }
                                )
                                Text(
                                    text = subtask.title,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (subtask.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(onClick = {
                                    subtasks.removeAll { it.id == subtask.id }
                                    subtasks = subtasks.toMutableList()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Удалить", modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        // Поле добавления новой подзадачи
                        if (isAddingSubtask) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                                OutlinedTextField(
                                    value = newSubtaskText,
                                    onValueChange = { newSubtaskText = it },
                                    label = { Text("Название подзадачи") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                IconButton(onClick = {
                                    if (newSubtaskText.isNotBlank()) {
                                        subtasks.add(Subtask(title = newSubtaskText))
                                        subtasks = subtasks.toMutableList()
                                        newSubtaskText = ""
                                        isAddingSubtask = false
                                    }
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Сохранить")
                                }
                                IconButton(onClick = {
                                    newSubtaskText = ""
                                    isAddingSubtask = false
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Отмена")
                                }
                            }
                        }
                    }
                }

                // ========== ПОВТОРЕНИЕ ==========
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Повторение", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Выбор типа повторения (радиокнопки)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = recurrenceType == RecurrenceType.NONE,
                                    onClick = { recurrenceType = RecurrenceType.NONE }
                                )
                                Text("Нет", modifier = Modifier.padding(start = 8.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = recurrenceType == RecurrenceType.DAILY,
                                    onClick = { recurrenceType = RecurrenceType.DAILY }
                                )
                                Text("Каждый день", modifier = Modifier.padding(start = 8.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = recurrenceType == RecurrenceType.WEEKLY,
                                    onClick = { recurrenceType = RecurrenceType.WEEKLY }
                                )
                                Text("Каждую неделю", modifier = Modifier.padding(start = 8.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = recurrenceType == RecurrenceType.MONTHLY,
                                    onClick = { recurrenceType = RecurrenceType.MONTHLY }
                                )
                                Text("Каждый месяц", modifier = Modifier.padding(start = 8.dp))
                            }
                        }

                        // Интервал (если выбран не NONE)
                        if (recurrenceType != RecurrenceType.NONE) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Повторять каждые", modifier = Modifier.weight(1f))
                                OutlinedTextField(
                                    value = recurrenceInterval.toString(),
                                    onValueChange = { recurrenceInterval = it.toIntOrNull()?.coerceAtLeast(1) ?: 1 },
                                    modifier = Modifier.width(80.dp),
                                    singleLine = true
                                )
                                Text(
                                    when (recurrenceType) {
                                        RecurrenceType.DAILY -> "дней"
                                        RecurrenceType.WEEKLY -> "недель"
                                        RecurrenceType.MONTHLY -> "месяцев"
                                        else -> ""
                                    },
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val deadline = try {
                        if (deadlineText.isNotBlank())
                            LocalDateTime.parse(deadlineText, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                        else null
                    } catch (e: DateTimeParseException) {
                        deadlineError = "Неверный формат. Пример: 31.12.2025 18:00"
                        return@Button
                    }
                    val newPriority = when (sliderPosition.value.toInt()) {
                        0 -> Priority.LOW
                        1 -> Priority.MEDIUM
                        2 -> Priority.HIGH
                        else -> Priority.URGENT
                    }
                    val recurrenceRule = RecurrenceRule(
                        type = recurrenceType,
                        interval = recurrenceInterval
                    )
                    val updated = task.copy(
                        title = title,
                        description = description,
                        deadline = deadline,
                        priority = newPriority,
                        category = category,
                        subtasks = subtasks,
                        recurrence = recurrenceRule,
                        isCompleted = task.isCompleted
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отмена")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}