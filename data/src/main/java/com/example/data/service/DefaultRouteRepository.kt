package com.example.data.service

import com.example.data.RouteRepository
import com.example.data.service.response.DistanceTime
import com.example.data.service.response.LocationsResponse
import com.example.data.service.response.OriginDestination
import com.example.data.service.response.Route
import retrofit2.HttpException
import javax.inject.Inject

class DefaultRouteRepository @Inject constructor(
    private val routeService: RouteService,
) : RouteRepository {
    override suspend fun getLocations(): Result<LocationsResponse> {
        return try {
            val response = routeService.getLocations() // Retrofit 서비스 호출
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response)) // HTTP 에러 처리
            }
        } catch (e: Exception) {
            Result.failure(e) // 일반 예외 처리
        }
    }

    override suspend fun getRoute(origin: String, destination: String): Result<List<Route>> {
        return try {
            val response = routeService.getRoute(origin, destination)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDistanceTime(origin: String, destination: String): Result<DistanceTime> {
        return try {
            val response = routeService.getDistanceTime(origin, destination)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}