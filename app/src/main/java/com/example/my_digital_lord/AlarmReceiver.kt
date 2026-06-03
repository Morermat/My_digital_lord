package com.example.my_digital_lord

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.provider.Settings
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "✅ Будильник сработал!")  // <-- добавить эту строку

        createNotificationChannel(context)

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true

        if (hasPermission) {
            showFullScreenNotification(context)
        } else {
            Log.d("AlarmReceiver", "Нет разрешения на уведомления, пытаюсь запустить активность напрямую")
            // fallback: пробуем запустить активность напрямую (может не работать на Android 10+)
            try {
                val intent = Intent(context, LordWarningActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Не удалось запустить активность", e)
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Контроль бездействия",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Возвращает Господина, если вы бездействуете"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            Log.d("AlarmReceiver", "Канал уведомлений создан")
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @SuppressLint("FullScreenIntentPolicy")
    private fun showFullScreenNotification(context: Context) {
        try {
            // 1. Показываем уведомление (для информации)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_dialog_alert)
                .setContentTitle("Господин недоволен!")
                .setContentText("Бездействие недопустимо. Вернитесь к работе.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(2, notification)

            if (Settings.canDrawOverlays(context)) {
                val intent = Intent(context, LordWarningActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(intent)
            } else {
                Log.d("AlarmReceiver", "Нет разрешения на рисование поверх окон")
            }
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Ошибка", e)
        }
    }

    companion object {
        private const val CHANNEL_ID = "inactivity_channel"
    }
}