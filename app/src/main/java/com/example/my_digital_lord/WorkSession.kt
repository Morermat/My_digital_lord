package com.example.my_digital_lord

import java.util.UUID

data class WorkSession(
    val id: String = UUID.randomUUID().toString(),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val durationSeconds: Int = 0,
    val isValid: Boolean = true
)