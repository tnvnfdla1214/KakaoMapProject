package com.example.data.service

import com.example.data.response.DistanceTime
import com.example.data.response.LocationsResponse
import com.example.data.response.Route
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface RouteService {
    @GET("locations")
    suspend fun getLocations(): Response<LocationsResponse>

    @Headers("Content-Type: application/json")
    @GET("routes")
    suspend fun getRoute(@Query("origin") origin: String,@Query("destination")  destination: String): Response<List<Route>>

    @Headers("Content-Type: application/json")
    @GET("distance-time")
    suspend fun getDistanceTime(@Query("origin") origin: String,@Query("destination")  destination: String): Response<DistanceTime>
}