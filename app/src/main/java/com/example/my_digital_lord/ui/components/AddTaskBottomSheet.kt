package com.example.my_digital_lord.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.my_digital_lord.Priority
import com.example.my_digital_lord.TaskCategory
import com.example.my_digital_lord.utils.TaskParser
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    onDismiss: () -> Unit,
    onAddTask: (title: String, description: String, deadline: LocalDateTime?, priority: Priority, category: TaskCategory) -> Unit,
    onQuickAdd: (rawText: String) -> Unit   // для быстрого AI-парсинга
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Быстрый ввод", "Ручной ввод")

    // --- состояние для быстрого ввода ---
    var quickText by remember { mutableStateOf("") }
    var isParsing by remember { mutableStateOf(false) }
    var parseError by remember { mutableStateOf<String?>(null) }

    // --- состояние для ручного ввода ---
    var manualTitle by remember { mutableStateOf("") }
    var manualDescription by remember { mutableStateOf("") }
    var manualDeadlineText by remember { mutableStateOf("") }
    var manualDeadlineError by remember { mutableStateOf<String?>(null) }
    var manualPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var manualCategory by remember { mutableStateOf(TaskCategory.PERSONAL) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    // --- голосовой ввод ---
    val context = LocalContext.current
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                quickText = matches[0]
            }
        }
    }

    fun startVoiceInput() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Запросить разрешение (можно через другой лаунчер, для простоты пропустим)
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажите задачу...")
        }
        voiceLauncher.launch(intent)
    }

    // --- ручное создание задачи ---
    fun createManualTask() {
        if (manualTitle.isBlank()) return
        val deadline = try {
            if (manualDeadlineText.isNotBlank())
                LocalDateTime.parse(manualDeadlineText, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            else null
        } catch (e: DateTimeParseException) {
            manualDeadlineError = "Неверный формат (дд.мм.гггг чч:мм)"
            return
        }
        onAddTask(manualTitle, manualDescription, deadline, manualPriority, manualCategory)
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Заголовок
            Text(
                text = "Новая задача",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Вкладки
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Medium) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Контент вкладок
            when (selectedTab) {
                0 -> {
                    // Быстрый ввод
                    Column {
                        OutlinedTextField(
                            value = quickText,
                            onValueChange = { quickText = it; parseError = null },
                            label = { Text("Опишите задачу") },
                            placeholder = { Text("Например: Купить молоко завтра в 18:00, срочно") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = ::startVoiceInput) {
                                    Icon(Icons.Default.Mic, contentDescription = "Голосовой ввод")
                                }
                            }
                        )
                        if (parseError != null) {
                            Text(parseError!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                if (quickText.isBlank()) return@Button
                                isParsing = true
                                try {
                                    // Локальный AI-парсинг (можно заменить на вызов сервера)
                                    val parsed = TaskParser.parse(quickText)
                                    // После парсинга создаём задачу через быстрый колбэк (можно передать распаршенные данные)
                                    onQuickAdd(quickText)   // пока просто текст, но можно расширить
                                    onDismiss()
                                } catch (e: Exception) {
                                    parseError = "Ошибка: ${e.message}"
                                } finally {
                                    isParsing = false
                                }
                            },
                            enabled = quickText.isNotBlank() && !isParsing,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isParsing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Анализирую...")
                            } else {
                                Text("Создать (AI)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                1 -> {
                    // Ручной ввод
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = manualTitle,
                            onValueChange = { manualTitle = it },
                            label = { Text("Название *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = manualDescription,
                            onValueChange = { manualDescription = it },
                            label = { Text("Описание (необязательно)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = manualDeadlineText,
                            onValueChange = { manualDeadlineText = it; manualDeadlineError = null },
                            label = { Text("Дедлайн (дд.мм.гггг чч:мм)") },
                            isError = manualDeadlineError != null,
                            supportingText = { if (manualDeadlineError != null) Text(manualDeadlineError!!) },
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
                                        text = when (manualCategory) {
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
                                                    manualCategory = cat
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
                            Text("Приоритет", style = MaterialTheme.typography.labelMedium)
                            Slider(
                                value = when (manualPriority) {
                                    Priority.LOW -> 0f
                                    Priority.MEDIUM -> 1f
                                    Priority.HIGH -> 2f
                                    Priority.URGENT -> 3f
                                },
                                onValueChange = {
                                    manualPriority = when (it.toInt()) {
                                        0 -> Priority.LOW
                                        1 -> Priority.MEDIUM
                                        2 -> Priority.HIGH
                                        else -> Priority.URGENT
                                    }
                                },
                                valueRange = 0f..3f,
                                steps = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = when (manualPriority) {
                                    Priority.LOW -> "Низкий"
                                    Priority.MEDIUM -> "Средний"
                                    Priority.HIGH -> "Высокий"
                                    Priority.URGENT -> "Срочный"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = ::createManualTask,
                            enabled = manualTitle.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Добавить задачу", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}