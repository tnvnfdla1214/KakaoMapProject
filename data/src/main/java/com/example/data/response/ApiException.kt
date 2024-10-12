package com.example.data.response

import com.google.gson.Gson

data class ApiException(
    val code: Int,
    val errorMessage: String,
) : Throwable(errorMessage) {
    companion object {
        fun fromJson(json: String, defaultCode: Int = -1): ApiException {
            return try {
                val gson = Gson()
                val errorResponse = gson.fromJson(json, ApiErrorResponse::class.java)
                ApiException(errorResponse.code, errorResponse.message)
            } catch (e: Exception) {
                ApiException(defaultCode, json)
            }
        }
    }
}

data class ApiErrorResponse(
    val code: Int,
    val message: String,
)