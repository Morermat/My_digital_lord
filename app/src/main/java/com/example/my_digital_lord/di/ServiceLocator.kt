package com.example.my_digital_lord.di

import com.example.my_digital_lord.App
import com.example.my_digital_lord.data.TaskRepository
import com.example.my_digital_lord.data.remote.ApiService
import com.example.my_digital_lord.utils.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceLocator {
    private const val BASE_URL = "https://mydigitalmaster.ru"

    private val tokenManager by lazy { TokenManager(App.getInstance()) }

    private val authInterceptor by lazy { AuthInterceptor(tokenManager) }

    private var sessionExpiredCallback: (() -> Unit)? = null

    fun setSessionExpiredCallback(callback: () -> Unit) {
        sessionExpiredCallback = callback
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .authenticator(TokenAuthenticator(tokenManager) { sessionExpiredCallback?.invoke() })
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepository(apiService)
    }
}