package com.example.my_digital_lord.di

import android.util.Log
import com.example.my_digital_lord.utils.LogTags
import com.example.my_digital_lord.utils.TokenManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val onSessionExpired: () -> Unit = {}
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Authorization")?.startsWith("Bearer") == true) {
            Log.w(LogTags.AUTH, "401 after Bearer token, session expired, clearing tokens")
            tokenManager.clear()
            onSessionExpired()
            return null
        }
        return null
    }
}