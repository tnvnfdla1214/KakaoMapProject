package com.example.data.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val authToken: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newRequest = request.newBuilder()
            .header(AUTHORIZATION_HEADER, authToken)
            .build()

        return chain.proceed(newRequest)
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
    }
}