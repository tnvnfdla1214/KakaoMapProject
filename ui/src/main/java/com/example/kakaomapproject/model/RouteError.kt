package com.example.kakaomapproject.model

import com.example.data.response.OriginDestination

data class RouteError(
    val path: String,
    val code: Int,
    val message: String
) {
    companion object {
        fun getRouteErrorPath(location: OriginDestination): String {
            return "${location.origin}-${location.destination}"
        }
    }
}