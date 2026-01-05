package com.cashwind.app.network

import okhttp3.Interceptor
import okhttp3.Response
import com.cashwind.app.util.TokenManager

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Get token from TokenManager
        val token = tokenManager.getToken()

        // If token exists, add it to the Authorization header
        val requestBuilder = originalRequest.newBuilder()
        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
