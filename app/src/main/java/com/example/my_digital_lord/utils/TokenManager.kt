package com.example.my_digital_lord.utils

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.my_digital_lord.data.remote.UserProfile
import com.google.gson.Gson

class TokenManager(private val context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val sharedPrefs = EncryptedSharedPreferences.create(
        "secure_tokens",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val gson = Gson()

    fun saveTokens(accessToken: String, refreshToken: String, userProfile: UserProfile?) {
        Log.d(LogTags.TOKEN, "saveTokens: accessToken=${accessToken.take(20)}..., refreshToken=${refreshToken.take(20)}...")
        sharedPrefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
        userProfile?.let { saveUserProfile(it) }
    }

    fun getAccessToken(): String? {
        val token = sharedPrefs.getString("access_token", null)
        Log.d(LogTags.TOKEN, "getAccessToken: ${token?.take(20)}...")
        return token
    }

    fun getRefreshToken(): String? {
        val token = sharedPrefs.getString("refresh_token", null)
        Log.d(LogTags.TOKEN, "getRefreshToken: ${token?.take(20)}...")
        return token
    }

    fun getUserProfile(): UserProfile? {
        val json = sharedPrefs.getString("user_profile", null) ?: return null
        return try {
            gson.fromJson(json, UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun saveUserProfile(profile: UserProfile) {
        val json = gson.toJson(profile)
        Log.d(LogTags.TOKEN, "saveUserProfile: ${profile.firstName} ${profile.lastName}")
        sharedPrefs.edit().putString("user_profile", json).apply()
    }

    fun updateAccessToken(newAccessToken: String) {
        Log.d(LogTags.TOKEN, "updateAccessToken: ${newAccessToken.take(20)}...")
        sharedPrefs.edit().putString("access_token", newAccessToken).apply()
    }

    fun clear() {
        Log.d(LogTags.TOKEN, "clear: removing all tokens")
        sharedPrefs.edit().clear().apply()
    }
}