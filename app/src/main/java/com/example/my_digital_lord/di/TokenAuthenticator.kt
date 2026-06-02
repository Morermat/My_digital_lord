package com.example.my_digital_lord.di

import android.util.Log
import com.example.my_digital_lord.data.remote.RefreshRequest
import com.example.my_digital_lord.utils.LogTags
import com.example.my_digital_lord.utils.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val onSessionExpired: () -> Unit
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val url = response.request.url.encodedPath
        if (url.contains("/api/auth/logout") || url.contains("/api/auth/refresh")) return null

        val refreshToken = tokenManager.getRefreshToken() ?: return null

        return runBlocking {
            try {
                // Отправляем refresh-токен в теле (без заголовка)
                val refreshResponse = ServiceLocator.apiService.refreshToken(RefreshRequest(refreshToken))
                val newAccess = refreshResponse.accessToken
                val newRefresh = refreshResponse.refreshToken

                tokenManager.updateAccessToken(newAccess)
                if (newRefresh.isNotBlank()) {
                    val user = tokenManager.getUserProfile()
                    tokenManager.saveTokens(newAccess, newRefresh, user)
                }

                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccess")
                    .build()
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    Log.e(LogTags.AUTH, "Refresh token expired, logging out")
                    tokenManager.clear()
                    onSessionExpired()
                }
                null
            } catch (e: Exception) {
                Log.e(LogTags.AUTH, "Refresh failed", e)
                null
            }
        }
    }
}