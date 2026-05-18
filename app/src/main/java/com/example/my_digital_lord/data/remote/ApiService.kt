package com.example.my_digital_lord.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/api/auth/vk")
    suspend fun exchangeCode(@Body request: VkAuthRequest): AuthResponse
    @POST("/api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<Unit>
}