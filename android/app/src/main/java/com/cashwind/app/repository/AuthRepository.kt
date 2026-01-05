package com.cashwind.app.repository

import com.cashwind.app.model.AuthRequest
import com.cashwind.app.model.AuthResponse
import com.cashwind.app.model.RegisterRequest
import com.cashwind.app.network.RetrofitProvider
import com.cashwind.app.util.TokenManager

class AuthRepository(private val tokenManager: TokenManager) {
    
    suspend fun register(email: String, password: String, name: String): AuthResponse {
        val request = RegisterRequest(email, password, name)
        val response = RetrofitProvider.authService.register(request)
        tokenManager.saveToken(response.token)
        return response
    }

    suspend fun login(email: String, password: String): AuthResponse {
        val request = AuthRequest(email, password)
        val response = RetrofitProvider.authService.login(request)
        tokenManager.saveToken(response.token)
        return response
    }

    fun logout() {
        tokenManager.clearToken()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.isTokenValid()
    }
}
