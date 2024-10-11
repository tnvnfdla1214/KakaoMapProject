package com.example.data

import com.example.data.service.response.DistanceTime
import com.example.data.service.response.LocationsResponse
import com.example.data.service.response.Route

interface RouteRepository {
    suspend fun getLocations(): Result<LocationsResponse>
    suspend fun getRoute(origin: String, destination: String): Result<List<Route>>
    suspend fun getDistanceTime(origin: String, destination: String): Result<DistanceTime>
}