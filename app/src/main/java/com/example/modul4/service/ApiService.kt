package com.example.modul4.service

import com.example.modul4.model.LoginRequest
import com.example.modul4.model.LoginResponse
import com.example.modul4.model.UserListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/users")
    suspend fun getUsers(@Query("page") page: Int = 1): Response<UserListResponse>
}