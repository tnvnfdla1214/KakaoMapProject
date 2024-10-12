package com.example.kakaomapproject

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RouteRepository
import com.example.kakaomapproject.model.RouteError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _mainViewState = MutableStateFlow<MainViewState>(MainViewState.Init)
    val mainViewState: StateFlow<MainViewState> = _mainViewState

    private val _errorViewState = MutableStateFlow<RouteError?>(null)
    val errorViewState: StateFlow<RouteError?> = _errorViewState

    // Locations 데이터 요청
    fun fetchLocations() {
        Log.d("qweqwe","fetchLocations")
        viewModelScope.launch {
            routeRepository.getLocations().onSuccess { response ->
                _mainViewState.value = MainViewState.ListView(response.locations)
                Log.d("qweqwe", "locations : " + response.locations.size)
            }.onFailure { throwable ->
                Log.d("qweqwe", "throwable : " + throwable)
            }
        }
    }

    // Route 데이터 요청
    fun fetchRoute(origin: String, destination: String) {
        viewModelScope.launch {
            routeRepository.getRoute(origin, destination).onSuccess { route ->
                Log.d("qweqwe", "route : " + route)
            }.onFailure { throwable ->
                Log.d("qweqwe", "throwable : " + throwable)
            }
        }
    }

    // DistanceTime 데이터 요청
    fun fetchDistanceTime(origin: String, destination: String) {
        viewModelScope.launch {
            routeRepository.getDistanceTime(origin, destination).onSuccess { distanceTime ->
                Log.d("qweqwe", "distanceTime : " + distanceTime)
            }.onFailure { throwable ->
                Log.d("qweqwe", "throwable : " + throwable)
            }
        }
    }

}