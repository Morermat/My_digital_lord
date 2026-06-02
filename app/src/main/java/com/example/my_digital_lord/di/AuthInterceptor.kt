package com.example.my_digital_lord.di

import android.util.Log
import com.example.my_digital_lord.utils.LogTags
import com.example.my_digital_lord.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.encodedPath

        // Для logout и refresh не нужен заголовок
        if (url.contains("/api/auth/logout") || url.contains("/api/auth/refresh")) {
            Log.d(LogTags.NETWORK, "Skipping Authorization header for $url")
            return chain.proceed(original)
        }

        val token = tokenManager.getAccessToken()
        val request = if (token != null) {
            Log.d(LogTags.NETWORK, "Adding Authorization header: Bearer ${token.take(20)}...")
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            Log.d(LogTags.NETWORK, "No access token, request without Authorization")
            original
        }
        return chain.proceed(request)
    }
}