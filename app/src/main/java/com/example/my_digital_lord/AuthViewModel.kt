package com.example.my_digital_lord

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import android.webkit.CookieManager
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_digital_lord.data.remote.VkAuthRequest
import com.example.my_digital_lord.data.remote.AuthResponse
import com.example.my_digital_lord.data.remote.UserProfile
import com.example.my_digital_lord.di.ServiceLocator
import com.example.my_digital_lord.utils.PkceUtil
import com.example.my_digital_lord.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.vk.id.auth.VKIDAuthUiParams
import androidx.core.content.edit
import com.example.my_digital_lord.data.remote.LogoutRequest
import com.example.my_digital_lord.utils.LogTags

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_CODE_VERIFIER = "pkce_code_verifier"
    }

    private val apiService = ServiceLocator.apiService
    private val tokenManager = TokenManager(application)
    private val prefs = application.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _authParams = MutableStateFlow<VKIDAuthUiParams?>(null)
    val authParams: StateFlow<VKIDAuthUiParams?> = _authParams.asStateFlow()

    private var currentState: String? = null

    init {
        restoreSession()
    }

    private fun restoreSession() {
        val token = tokenManager.getAccessToken()
        if (token != null) {
            val user = tokenManager.getUserProfile()
            _userProfile.value = user
            _isLoggedIn.value = true
        }
    }

    private fun saveSession(accessToken: String, refreshToken: String, user: UserProfile?) {
        tokenManager.saveTokens(accessToken, refreshToken, user)
        _userProfile.value = user
        _isLoggedIn.value = true
    }

    private fun clearSession() {
        tokenManager.clear()
        _isLoggedIn.value = false
        _userProfile.value = null
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }

    fun prepareAuth() {
        val codeVerifier = PkceUtil.generateCodeVerifier()
        val codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier)
        val state = PkceUtil.generateState()

        prefs.edit { putString(KEY_CODE_VERIFIER, codeVerifier) }
        currentState = state

        _authParams.value = VKIDAuthUiParams {
            this.state = state
            this.codeChallenge = codeChallenge
            scopes = setOf("phone", "email")
        }
    }

    @SuppressLint("UseKtx")
    fun exchangeCode(code: String, deviceId: String) {
        _isLoading.value = true
        _authError.value = null
        viewModelScope.launch {
            try {
                val codeVerifier = prefs.getString(KEY_CODE_VERIFIER, null)
                    ?: throw IllegalStateException("PKCE code verifier not found")
                val state = currentState ?: throw IllegalStateException("State not generated")

                val request = VkAuthRequest(code, codeVerifier, deviceId, state)
                val response: AuthResponse = apiService.exchangeCode(request)
                Log.d(LogTags.AUTH, "exchangeCode response: accessToken=${response.accessToken.take(20)}..., refreshToken=${response.refreshToken.take(20)}..., user=${response.userProfile.firstName} ${response.userProfile.lastName}")

                saveSession(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    user = response.userProfile
                )
            } catch (e: Exception) {
                _authError.value = "Ошибка входа: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onAuthFail(message: String) {
        _authError.value = message
        _isLoading.value = false
    }

    fun logout() {
        viewModelScope.launch {
            val refreshToken = tokenManager.getRefreshToken()
            Log.d(LogTags.AUTH, "logout: sending refreshToken=${refreshToken?.take(20)}...")
            if (refreshToken != null) {
                try {
                    apiService.logout(LogoutRequest(refreshToken))
                    Log.d(LogTags.AUTH, "logout: server success")
                } catch (e: Exception) {
                    Log.e(LogTags.AUTH, "logout server error", e)
                }
            }
            clearSession()
            prefs.edit { remove(KEY_CODE_VERIFIER) }
            _authParams.value = null
        }
    }
}