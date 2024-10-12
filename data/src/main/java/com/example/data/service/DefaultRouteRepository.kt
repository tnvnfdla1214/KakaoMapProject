package com.example.data.service

import android.util.Log
import com.example.data.RouteRepository
import com.example.data.response.DistanceTime
import com.example.data.response.LocationsResponse
import com.example.data.response.ApiException
import com.example.data.response.Route
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
                val errorCode = response.code()
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"

                // fromJson 함수를 사용해 JSON 파싱 및 ApiException 생성
                val apiException = ApiException.fromJson(errorMessage, errorCode)

                Result.failure(apiException)
            }
        } catch (e: Exception) {
            Result.failure(ApiException(-1, e.message ?: "Unknown exception"))
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