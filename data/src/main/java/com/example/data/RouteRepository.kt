package com.example.data

import com.example.data.response.DistanceTime
import com.example.data.response.LocationsResponse
import com.example.data.response.RouteResponse

interface RouteRepository {
    suspend fun getLocations(): Result<LocationsResponse>
    suspend fun getRoute(origin: String, destination: String): Result<List<RouteResponse>>
    suspend fun getDistanceTime(origin: String, destination: String): Result<DistanceTime>
}