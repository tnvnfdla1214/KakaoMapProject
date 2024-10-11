package com.example.data.service

import com.example.data.RouteRepository
import com.example.data.service.response.DistanceTime
import com.example.data.service.response.OriginDestination
import com.example.data.service.response.Route
import javax.inject.Inject

class DefaultRouteRepository @Inject constructor(
    private val routeService: RouteService,
) : RouteRepository {
    override suspend fun getLocations(): List<OriginDestination> = routeService.getLocations()

    override suspend fun getRoute(origin: String, destination: String): Route =
        routeService.getRoute(origin, destination)

    override suspend fun getDistanceTime(origin: String, destination: String): DistanceTime =
        routeService.getDistanceTime(origin, destination)
}