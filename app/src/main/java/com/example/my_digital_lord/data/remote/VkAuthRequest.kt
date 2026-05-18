package com.example.my_digital_lord.data.remote

import com.google.gson.annotations.SerializedName

data class VkAuthRequest(
    @SerializedName("code")
    val code: String,

    @SerializedName("codeVerifier")
    val codeVerifier: String,

    @SerializedName("deviceId")
    val deviceId: String,

    @SerializedName("state")
    val state: String
)