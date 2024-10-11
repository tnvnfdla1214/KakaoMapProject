package com.example.data.network

import android.content.Context
import com.example.data.AUTHORIZATION
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val context: Context,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        val newRequest = request.newBuilder().header(AUTHORIZATION, "5c3bda0d-001e-4db0-b1f0-01ceb2cce8f5").build()

        return chain.proceed(newRequest)
    }
}