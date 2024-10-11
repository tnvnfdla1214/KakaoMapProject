package com.example.data

import com.example.data.service.response.DistanceTime
import com.example.data.service.response.OriginDestination
import com.example.data.service.response.Route

interface RouteRepository {
    suspend fun getLocations(): List<OriginDestination>
    suspend fun getRoute(origin: String, destination: String): Route
    suspend fun getDistanceTime(origin: String, destination: String): DistanceTime
}