package com.example.my_digital_lord.data.remote

import com.google.gson.annotations.SerializedName

data class ParseResponse(
    @SerializedName("title")
    val title: String,

    @SerializedName("deadline")
    val deadline: String? = null,

    @SerializedName("priority")
    val priority: String
)