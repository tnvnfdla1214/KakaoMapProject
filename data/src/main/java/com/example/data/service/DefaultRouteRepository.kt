package com.example.data.service

import android.util.Log
import com.example.data.RouteRepository
import com.example.data.response.ApiErrorMessage
import com.example.data.response.DistanceTime
import com.example.data.response.LocationsResponse
import com.example.data.response.ApiException
import com.example.data.response.RouteResponse
import retrofit2.HttpException
import javax.inject.Inject

class DefaultRouteRepository @Inject constructor(
    private val routeService: RouteService,
) : RouteRepository {
    override suspend fun getLocations(): Result<LocationsResponse> {
        return try {
            val response = routeService.getLocations()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception(ApiErrorMessage.EMPTY_BODY))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRoute(
        origin: String,
        destination: String
    ): Result<List<RouteResponse>> {
        return try {
            val response = routeService.getRoute(origin, destination)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception(ApiErrorMessage.EMPTY_BODY))
            } else {
                val errorCode = response.code()

                val errorMessage = response.errorBody()?.string()
                    ?: ApiException.getPathErrorMessage(response.raw().request.url.toString())
                val apiException = ApiException.fromJson(errorMessage, errorCode)

                Result.failure(apiException)
            }
        } catch (e: Exception) {
            Result.failure(ApiException(-1, e.message ?: ApiErrorMessage.UNKNOWN_ERROR))
        }
    }

    override suspend fun getDistanceTime(
        origin: String,
        destination: String
    ): Result<DistanceTime> {
        return try {
            val response = routeService.getDistanceTime(origin, destination)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception(ApiErrorMessage.EMPTY_BODY))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}