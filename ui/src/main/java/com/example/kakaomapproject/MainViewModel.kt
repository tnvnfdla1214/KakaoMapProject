package com.example.kakaomapproject

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val routeRepository: RouteRepository
) : ViewModel() {

    // Locations 데이터 요청
    fun fetchLocations() {
        Log.d("qweqwe","fetchLocations")
        viewModelScope.launch {
            val result = routeRepository.getLocations()
            result.onSuccess { locations ->
                Log.d("qweqwe", "locations : " + locations)
            }.onFailure { throwable ->
                Log.d("qweqwe", "throwable : " + throwable)
            }
        }
    }

    // Route 데이터 요청
    fun fetchRoute(origin: String, destination: String) {
        viewModelScope.launch {
            val result = routeRepository.getRoute(origin, destination)
            result.onSuccess { route ->
                Log.d("qweqwe", "route : " + route)
            }.onFailure { throwable ->
                Log.d("qweqwe", "throwable : " + throwable)
            }
        }
    }

    // DistanceTime 데이터 요청
    fun fetchDistanceTime(origin: String, destination: String) {
        viewModelScope.launch {
            val result = routeRepository.getDistanceTime(origin, destination)
            result.onSuccess { distanceTime ->
                Log.d("qweqwe", "distanceTime : " + distanceTime)
            }.onFailure { throwable ->
                Log.d("qweqwe", "throwable : " + throwable)
            }
        }
    }

}