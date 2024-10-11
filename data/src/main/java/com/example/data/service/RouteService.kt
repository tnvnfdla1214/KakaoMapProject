package com.example.data.service

import com.example.data.service.response.DistanceTime
import com.example.data.service.response.OriginDestination
import com.example.data.service.response.Route
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface RouteService {
    @GET("v2/user/word/{bid}")
    suspend fun getLocations(): List<OriginDestination>

    @Headers("Content-Type: application/json")
    @GET("v2/user/word/{bid}")
    suspend fun getRoute(@Query("origin") origin: String,@Query("destination")  destination: String): Route

    @Headers("Content-Type: application/json")
    @GET("v2/user/word/{bid}")
    suspend fun getDistanceTime(@Query("origin") origin: String,@Query("destination")  destination: String): DistanceTime
}