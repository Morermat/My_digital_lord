package com.example.my_digital_lord.utils

import android.annotation.SuppressLint
import android.content.Context
import com.example.my_digital_lord.ui.theme.AppTheme
import androidx.core.content.edit

object ThemeManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_THEME = "selected_theme"

    @SuppressLint("UseKtx")
    fun saveTheme(context: Context, theme: AppTheme) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_THEME, theme.name)
            }
    }

    fun loadTheme(context: Context): AppTheme {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val themeName = prefs.getString(KEY_THEME, AppTheme.SUNSET_NEON.name)
        return try {
            AppTheme.valueOf(themeName!!)
        } catch (e: Exception) {
            AppTheme.SUNSET_NEON
        }
    }

    fun saveDarkTheme(context: Context, isDark: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putBoolean("dark_theme", isDark)
            }
    }

    fun loadDarkTheme(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("dark_theme", true)
    }
}