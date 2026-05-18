package com.example.my_digital_lord

import android.app.Application
import com.vk.id.VKID
import java.util.Locale

class App : Application() {

    companion object {
        private lateinit var instance: App
        fun getInstance(): App = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        VKID.init(this)
        VKID.logsEnabled = true
        VKID.instance.setLocale(Locale("ru"))
    }
}