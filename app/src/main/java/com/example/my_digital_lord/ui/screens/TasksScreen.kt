package com.example.my_digital_lord.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.my_digital_lord.*
import com.example.my_digital_lord.data.TaskUiModel
import com.example.my_digital_lord.ui.components.*

@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    timerViewModel: TimerViewModel,
    onTimer: () -> Unit,
    onVoiceInput: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()

    val uiTasks = tasks.map { task ->
        val completedSubtasks = task.subtasks.count { it.isCompleted }
        val subtaskCount = task.subtasks.size
        TaskUiModel(
            id = task.id,
            title = task.title,
            description = task.description,
            deadline = task.deadline,
            completed = task.isCompleted,
            priority = task.priority,
            category = task.category ?: TaskCategory.PERSONAL,
            hasSubtasks = subtaskCount > 0,
            subtaskCount = subtaskCount,
            completedSubtasks = completedSubtasks,
            subtasks = task.subtasks   // передаём список подзадач
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.ALL) }
    var selectedSort by remember { mutableStateOf(SortOption.BY_DEADLINE) }
    var showSearch by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }

    val filteredTasks = uiTasks
        .filter { task -> if (searchQuery.isNotBlank()) task.title.contains(searchQuery, ignoreCase = true) else true }
        .filter { task -> if (selectedCategory != TaskCategory.ALL) task.category == selectedCategory else true }

    Scaffold(
        floatingActionButton = {
            ExpandableTaskFab(onAddTask = { showAddSheet = true })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                TasksTopBar(onSearchClick = { showSearch = !showSearch })
                if (showSearch) {
                    SearchSection(query = searchQuery, onQueryChange = { searchQuery = it })
                    Spacer(modifier = Modifier.height(8.dp))
                }
                CategoryChipsRow(selectedCategory = selectedCategory, onCategorySelected = { selectedCategory = it })
                SortSection(selectedSort = selectedSort, onSortSelected = { selectedSort = it })

                if (filteredTasks.isEmpty()) {
                    EmptyTasksState(modifier = Modifier.fillMaxSize().padding(top = 32.dp))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onCompleteToggle = { viewModel.toggleTaskCompletion(it) },
                                onDelete = { viewModel.deleteTask(it) },
                                onEdit = { taskId ->
                                    val originalTask = viewModel.tasks.value.find { it.id == taskId }
                                    editingTask = originalTask
                                },
                                onToggleSubtask = { taskId, subtaskId ->
                                    viewModel.toggleSubtask(taskId, subtaskId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    editingTask?.let { task ->
        EditTaskDialog(
            task = task,
            onDismiss = { editingTask = null },
            onSave = { updatedTask ->
                viewModel.updateTask(updatedTask)
                editingTask = null
            }
        )
    }

    if (showAddSheet) {
        AddTaskBottomSheet(
            onDismiss = { showAddSheet = false },
            onAddTask = { title, description, deadline, priority, category ->
                viewModel.addManualTask(title, description, deadline, priority, category)
                showAddSheet = false
            },
            onQuickAdd = { rawText ->
                viewModel.addTaskWithAiParse(rawText)
                showAddSheet = false
            }
        )
    }
}