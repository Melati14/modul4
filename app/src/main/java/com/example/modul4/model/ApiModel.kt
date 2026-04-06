package com.example.modul4.model

import com.google.gson.annotations.SerializedName
// Model Login
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String)

// Model Daftar Pengguna
data class User(
    val id: Int,
    val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val avatar: String
)

data class UserListResponse(
    val page: Int,
    val data: List<User>
)

data class UserRequest(
    val name: String,
    val job: String
)

data class UserResponse(
    val id: String,
    val name: String,
    val job: String,
    val createdAt: String?,
    val updatedAt: String?
)