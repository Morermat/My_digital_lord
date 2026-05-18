package com.example.my_digital_lord.data.remote

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("userProfile")
    val userProfile: UserProfile,
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String
)

data class UserProfile(
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("sex")
    val sex: String?
)