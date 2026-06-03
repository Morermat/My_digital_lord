package com.example.my_digital_lord.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.my_digital_lord.TaskCategory
import com.example.my_digital_lord.data.TaskUiModel
import com.example.my_digital_lord.ui.theme.AppDimens
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TaskCard(
    task: TaskUiModel,
    onCompleteToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (String) -> Unit,
    onToggleSubtask: (String, String) -> Unit,   // (taskId, subtaskId)
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onEdit(task.id) },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = RoundedCornerShape(AppDimens.CardRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.CardElevation),
        border = BorderStroke(
            width = AppDimens.BorderWidth,
            color = if (task.completed)
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.secondary
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (task.completed)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.completed,
                    onCheckedChange = { onCompleteToggle(task.id) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.secondary,
                        uncheckedColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        checkmarkColor = MaterialTheme.colorScheme.background
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (task.completed)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        PriorityIndicator(task.priority)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatHumanDeadline(task.deadline),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (task.deadline?.isBefore(LocalDateTime.now()) == true)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (task.hasSubtasks) {
                            Text(
                                text = "${task.completedSubtasks}/${task.subtaskCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = categoryName(task.category),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = { onEdit(task.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { onDelete(task.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Список подзадач (отображается под основной карточкой)
            if (task.hasSubtasks) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 60.dp, end = 16.dp, bottom = 16.dp) // отступ под чекбоксом
                ) {
                    task.subtasks.forEach { subtask ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = subtask.isCompleted,
                                onCheckedChange = { onToggleSubtask(task.id, subtask.id) },
                                enabled = !task.completed,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = subtask.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (subtask.isCompleted || task.completed)
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun categoryName(category: TaskCategory): String = when (category) {
    TaskCategory.ALL -> "Все"
    TaskCategory.WORK -> "Работа"
    TaskCategory.STUDY -> "Учёба"
    TaskCategory.PERSONAL -> "Личное"
    TaskCategory.HEALTH -> "Здоровье"
}

private fun formatHumanDeadline(dateTime: LocalDateTime?): String {
    if (dateTime == null) return "Без дедлайна"
    val now = LocalDateTime.now()
    return when {
        dateTime.toLocalDate() == now.toLocalDate() -> "сегодня ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        dateTime.toLocalDate() == now.plusDays(1).toLocalDate() -> "завтра ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        else -> dateTime.format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))
    }
}