package com.example.my_digital_lord.utils

import com.example.my_digital_lord.Priority
import com.example.my_digital_lord.TaskCategory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.roundToInt

data class ParsedTask(
    val title: String,
    val deadline: LocalDateTime?,
    val priority: Priority,
    val category: String? = null
)

object TaskParser {
    fun parse(raw: String): ParsedTask {
        var title = raw
        var deadline: LocalDateTime? = null
        var priority = Priority.MEDIUM
        var category: String? = null

        // Приоритет
        val urgentWords = listOf("срочно", "немедленно", "жизненно")
        val highWords = listOf("важно", "критично", "срочное")
        val lowWords = listOf("необязательно", "можно не", "по желанию")
        when {
            urgentWords.any { raw.contains(it, ignoreCase = true) } -> priority = Priority.URGENT
            highWords.any { raw.contains(it, ignoreCase = true) } -> priority = Priority.HIGH
            lowWords.any { raw.contains(it, ignoreCase = true) } -> priority = Priority.LOW
        }

        // Категории (пример)
        when {
            raw.contains("учёба", ignoreCase = true) || raw.contains("курс", ignoreCase = true) -> category = "Учёба"
            raw.contains("работа", ignoreCase = true) || raw.contains("проект", ignoreCase = true) -> category = "Работа"
            raw.contains("здоровье", ignoreCase = true) || raw.contains("спорт", ignoreCase = true) -> category = "Здоровье"
            raw.contains("личное", ignoreCase = true) || raw.contains("дом", ignoreCase = true) -> category = "Личное"
        }

        // Дедлайн
        val today = LocalDate.now()
        val timePattern = Regex("(\\d{1,2}):(\\d{2})")
        val timeMatch = timePattern.find(raw)
        val hour = timeMatch?.groupValues?.get(1)?.toIntOrNull() ?: 18
        val minute = timeMatch?.groupValues?.get(2)?.toIntOrNull() ?: 0

        val datePattern = Regex("(\\d{1,2})\\.(\\d{1,2})(?:\\.(\\d{4}))?")
        val dateMatch = datePattern.find(raw)
        if (dateMatch != null) {
            val day = dateMatch.groupValues[1].toIntOrNull()
            val month = dateMatch.groupValues[2].toIntOrNull()
            val year = dateMatch.groupValues.getOrNull(3)?.toIntOrNull() ?: today.year
            if (day != null && month != null) {
                deadline = LocalDateTime.of(year, month, day, hour, minute)
            }
        } else {
            when {
                raw.contains("завтра", ignoreCase = true) -> deadline = today.plusDays(1).atTime(hour, minute)
                raw.contains("сегодня", ignoreCase = true) -> deadline = today.atTime(hour, minute)
                else -> {
                    // Если не нашли дату, но есть время, ставим на сегодня
                    if (timeMatch != null) {
                        deadline = today.atTime(hour, minute)
                    }
                }
            }
        }

        // Очистка заголовка от слов-маркеров дат/приоритетов (опционально)
        title = title
            .replace(Regex("\\b(завтра|сегодня|срочно|важно|необязательно)\\b", RegexOption.IGNORE_CASE), "")
            .replace(timePattern, "")
            .replace(datePattern, "")
            .trim()
            .replace(Regex("\\s+"), " ")
        if (title.isBlank()) title = raw

        return ParsedTask(title, deadline, priority, category)
    }

    fun parseCategory(raw: String): TaskCategory = when {
        raw.contains("работа", ignoreCase = true) -> TaskCategory.WORK
        raw.contains("учёба", ignoreCase = true) -> TaskCategory.STUDY
        raw.contains("здоровье", ignoreCase = true) -> TaskCategory.HEALTH
        else -> TaskCategory.PERSONAL
    }
}