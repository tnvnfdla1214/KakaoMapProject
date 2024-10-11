package com.example.data.service.response

data class OriginDestination(
    val origin: String,
    val destination: String,
)

data class LocationsResponse(
    val locations: List<OriginDestination>
)