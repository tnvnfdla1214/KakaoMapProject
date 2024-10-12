package com.example.data.response

data class ResponseError(
    val code: Int,
    val errorMessage: String,
) : Throwable(errorMessage)