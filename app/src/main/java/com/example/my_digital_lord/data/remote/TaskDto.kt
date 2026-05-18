package com.example.my_digital_lord.data.remote

import com.google.gson.annotations.SerializedName

data class TaskDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("deadline")
    val deadline: String? = null,

    @SerializedName("priority")
    val priority: String,

    @SerializedName("isCompleted")
    val isCompleted: Boolean,

    @SerializedName("createdAt")
    val createdAt: String
)