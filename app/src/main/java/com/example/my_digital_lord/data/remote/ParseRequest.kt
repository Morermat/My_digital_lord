package com.example.my_digital_lord.data.remote

import com.google.gson.annotations.SerializedName

data class ParseRequest(
    @SerializedName("text")
    val text: String
)