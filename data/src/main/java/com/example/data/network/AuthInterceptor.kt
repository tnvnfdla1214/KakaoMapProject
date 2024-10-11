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

        val ignoreAuthorizationHeader = url.toString().contains(PATH_V2_LOGIN)

        val newRequest = if (ignoreAuthorizationHeader) {
            request.newBuilder().build()
        } else {
            request.newBuilder()
                .header(AUTHORIZATION, BuildConfig)
                .build()
        }

        return chain.proceed(newRequest)
    }
}