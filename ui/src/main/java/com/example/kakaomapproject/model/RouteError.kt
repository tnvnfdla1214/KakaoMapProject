package com.example.kakaomapproject.model

data class RouteError(
    val title: String,
    val path: String,
    val code: Int,
    val message: String
)