package com.example.data.response

data class OriginDestination(
    val origin: String,
    val destination: String,
)

data class LocationsResponse(
    val locations: List<OriginDestination>
)