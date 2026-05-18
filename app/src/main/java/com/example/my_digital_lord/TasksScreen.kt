package com.example.my_digital_lord

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    timerViewModel: TimerViewModel
) {
    val tasks by viewModel.tasks.collectAsState()
    val isSheetOpen by viewModel.isAddTaskSheetOpen.collectAsState()
    val activeSession by timerViewModel.activeSession.collectAsState()
    var showTimerSheet by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = { showTimerSheet = true },
                    modifier = Modifier.padding(bottom = 16.dp),
                    containerColor = if (activeSession != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Таймер"
                    )
                }
                FloatingActionButton(
                    onClick = { viewModel.openAddTaskSheet() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить задачу")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (tasks.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Господин недоволен",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Список задач пуст.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = tasks,
                        key = { it.id }
                    ) { task ->
                        TaskCard(
                            task = task,
                            onCompleteToggle = { viewModel.toggleTaskCompletion(task.id) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }

    if (isSheetOpen) {
        AddTaskBottomSheet(
            onDismiss = { viewModel.closeAddTaskSheet() },
            onAddTask = { rawText ->
                viewModel.addTaskWithAiParse(rawText)
            },
            isParsing = viewModel.isParsing.value,
            parseError = viewModel.parseError.value,
            onClearError = { viewModel.clearParseError() }
        )
    }

    if (showTimerSheet) {
        TimerBottomSheet(
            viewModel = timerViewModel,
            onDismiss = { showTimerSheet = false }
        )
    }
}