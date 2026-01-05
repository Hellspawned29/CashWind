package com.cashwind.app.model

data class User(
    val id: Int,
    val email: String,
    val name: String
)

data class AuthRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

data class AuthResponse(
    val token: String,
    val user: User
)
