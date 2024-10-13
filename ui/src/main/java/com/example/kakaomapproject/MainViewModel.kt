package com.example.kakaomapproject

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RouteRepository
import com.example.data.response.OriginDestination
import com.example.data.response.ApiException
import com.example.data.response.Route
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

    init {
        fetchLocations()
    }

    private fun fetchLocations() {
        viewModelScope.launch {
            routeRepository.getLocations().onSuccess { response ->
                _mainViewState.value = MainViewState.ListView(response.locations)
            }.onFailure { throwable ->
                Log.d("qweqwe", "throwable : " + throwable)
            }
        }
    }

    fun fetchRoute(location: OriginDestination) {
        viewModelScope.launch {
            routeRepository.getRoute(location.origin, location.destination).onSuccess { response ->
                fetchDistanceTime(location, response)
            }.onFailure { throwable ->
                if (throwable is ApiException) {
                    _errorViewState.value = RouteError(
                        RouteError.getRouteErrorPath(location),
                        throwable.code,
                        throwable.errorMessage
                    )
                }
            }
        }
    }

    private fun fetchDistanceTime(location: OriginDestination, routes: List<Route>) {
        viewModelScope.launch {
            routeRepository.getDistanceTime(location.origin, location.destination)
                .onSuccess { distanceTime ->
                    _mainViewState.value = MainViewState.MapView(routes, distanceTime)
                }.onFailure { throwable ->
                Log.d("qweqwe", "throwable : " + throwable)
            }
        }
    }

}