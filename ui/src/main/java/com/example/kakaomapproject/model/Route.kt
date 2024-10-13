package com.example.kakaomapproject.model

import com.example.data.response.RouteResponse

data class Route(
    val points: String,
    val trafficState: TrafficState,
) {
    companion object {
        fun fromRouteResponse(routeResponse: RouteResponse): Route {
            return Route(
                points = routeResponse.points,
                trafficState = TrafficState.fromString(routeResponse.trafficState)
            )
        }
    }
}