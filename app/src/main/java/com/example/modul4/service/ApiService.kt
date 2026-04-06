package com.example.modul4.service

import com.example.modul4.model.LoginRequest
import com.example.modul4.model.LoginResponse
import com.example.modul4.model.UserListResponse
import com.example.modul4.model.UserRequest
import com.example.modul4.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/users")
    suspend fun getUsers(@Query("page") page: Int = 1): Response<UserListResponse>



// CREATE: Menambah pengguna baru
@POST("api/users")
suspend fun createUser (@Body request: UserRequest): Response<UserResponse>

// UPDATE: Mengubah data pengguna berdasarkan ID
@PUT("api/users/{id}")
suspend fun updateUser(@Path("id") userId: Int, @Body request: UserRequest): Response<UserResponse>

// DELETE: Menghapus data pengguna berdasarkan ID
@DELETE("api/users/{id}")
suspend fun deleteUser(@Path("id") userId: Int): Response<Unit>

}