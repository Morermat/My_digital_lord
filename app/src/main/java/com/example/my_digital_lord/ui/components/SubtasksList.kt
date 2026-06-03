package com.example.my_digital_lord.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.my_digital_lord.Subtask

@Composable
fun SubtasksList(
    subtasks: List<Subtask>,
    onToggleSubtask: (String) -> Unit,
    onAddSubtask: (String) -> Unit,
    onRemoveSubtask: (String) -> Unit,
    modifier: Modifier
) {
    var newSubtaskText by remember { mutableStateOf("") }

    Column {
        Text("Подзадачи", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))

        subtasks.forEach { subtask ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = subtask.isCompleted,
                    onCheckedChange = { onToggleSubtask(subtask.id) }
                )
                Text(
                    text = subtask.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (subtask.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { onRemoveSubtask(subtask.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить подзадачу")
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newSubtaskText,
                onValueChange = { newSubtaskText = it },
                label = { Text("Новая подзадача") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(onClick = {
                if (newSubtaskText.isNotBlank()) {
                    onAddSubtask(newSubtaskText)
                    newSubtaskText = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    }
}