package com.example.my_digital_lord

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.core.content.edit

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        var isAppInBackground = false
        val Factory: androidx.lifecycle.ViewModelProvider.Factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return TimerViewModel(App.getInstance()) as T
            }
        }
    }

    private val prefs = application.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _activeSession = MutableStateFlow<WorkSession?>(null)
    val activeSession: StateFlow<WorkSession?> = _activeSession.asStateFlow()

    private val _sessions = MutableStateFlow<List<WorkSession>>(emptyList())

    private val _inactivityLimitSeconds = MutableStateFlow(600)
    val inactivityLimitSeconds: StateFlow<Int> = _inactivityLimitSeconds.asStateFlow()

    private var backgroundTime: Long = 0L
    private var isInBackground = false

    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private var inactivityAlarmIntent: PendingIntent? = null

    init {
        loadSessions()
        loadInactivityLimit()
    }

    private fun loadSessions() {
        val json = prefs.getString("work_sessions", null)
        if (json != null) {
            val type = object : TypeToken<List<WorkSession>>() {}.type
            _sessions.value = gson.fromJson(json, type) ?: emptyList()
        }
    }

    private fun saveSessions() {
        val json = gson.toJson(_sessions.value)
        prefs.edit { putString("work_sessions", json) }
    }

    private fun loadInactivityLimit() {
        _inactivityLimitSeconds.value = prefs.getInt("inactivity_limit_seconds", 600)
    }

    fun saveInactivityLimit(seconds: Int) {
        _inactivityLimitSeconds.value = seconds
        prefs.edit { putInt("inactivity_limit_seconds", seconds) }
    }

    fun startSession() {
        if (_activeSession.value != null) return
        val newSession = WorkSession()
        _activeSession.value = newSession
        InactivityService.start(getApplication())
    }

    fun stopSession() {
        val current = _activeSession.value ?: return
        val endTime = System.currentTimeMillis()
        val duration = ((endTime - current.startTime) / 1000).toInt()
        val updatedSession = current.copy(
            endTime = endTime,
            durationSeconds = duration,
            isValid = true
        )
        _sessions.update { it + updatedSession }
        saveSessions()
        _activeSession.value = null
        cancelInactivityAlarm()
        InactivityService.stop(getApplication())
    }

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun onAppBackgrounded() {
        isAppInBackground = true
        if (_activeSession.value != null) {
            isInBackground = true
            backgroundTime = System.currentTimeMillis()
            scheduleInactivityAlarm(_inactivityLimitSeconds.value)
            Log.d("TimerViewModel", "Будильник установлен на ${_inactivityLimitSeconds.value} секунд")
        }
    }

    fun onAppForegrounded() {
        cancelInactivityAlarm()
        isAppInBackground = false
        isInBackground = false
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private fun scheduleInactivityAlarm(limitSeconds: Int) {
        try {
            val intent = Intent(getApplication(), AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                getApplication(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            inactivityAlarmIntent = pendingIntent
            val triggerTime = System.currentTimeMillis() + limitSeconds * 1000L

            // Основной метод – AlarmClock (показывает иконку будильника)
            alarmManager.setAlarmClock(
                AlarmClockInfo(triggerTime, pendingIntent),
                pendingIntent
            )
            // Дополнительный – точный будильник, работающий в режиме ожидания
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
            Log.e("TimerViewModel", "Не удалось установить будильник", e)
        }
    }

    private fun cancelInactivityAlarm() {
        inactivityAlarmIntent?.let {
            alarmManager.cancel(it)
            inactivityAlarmIntent = null
        }
    }

    fun getProductiveTimeLast7Days(): Long {
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        return _sessions.value.filter {
            it.endTime != null && it.endTime > sevenDaysAgo && it.isValid
        }.sumOf { it.durationSeconds.toLong() }
    }

    fun getTopSessionsLast7Days(limit: Int = 5): List<WorkSession> {
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        return _sessions.value.filter {
            it.endTime != null && it.endTime > sevenDaysAgo && it.isValid
        }.sortedByDescending { it.durationSeconds }.take(limit)
    }
}