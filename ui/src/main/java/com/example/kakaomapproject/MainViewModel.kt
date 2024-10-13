package com.example.kakaomapproject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RouteRepository
import com.example.data.response.OriginDestination
import com.example.data.response.ApiException
import com.example.data.response.DistanceTime
import com.example.kakaomapproject.model.Route
import com.example.kakaomapproject.model.RouteError
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.LatLngBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
) : ViewModel() {

    private val _mainViewState = MutableStateFlow<MainViewState>(MainViewState.Init)
    val mainViewState: StateFlow<MainViewState> = _mainViewState

    private val _errorViewState = MutableStateFlow<RouteError?>(null)
    val errorViewState: StateFlow<RouteError?> = _errorViewState

    fun restErrorViewState() {
        _errorViewState.value = null
    }

    fun fetchLocations() {
        viewModelScope.launch {
            routeRepository.getLocations().onSuccess { response ->
                _mainViewState.value = MainViewState.ListView(response.locations)
            }.onFailure { throwable -> }
        }
    }

    fun fetchRoute(location: OriginDestination) {
        viewModelScope.launch {
            routeRepository.getRoute(location.origin, location.destination).onSuccess { response ->
                val routes =
                    response.map { routeResponse -> Route.fromRouteResponse(routeResponse) }
                fetchDistanceTime(location, routes)
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
                }.onFailure { throwable -> }
        }
    }

    fun parseRoutePoints(route: Route, boundsBuilder: LatLngBounds.Builder): List<LatLng> {
        return route.points.split(" ").map {
            val latLng = it.split(",")
            LatLng.from(latLng[1].toDouble(), latLng[0].toDouble()).apply {
                boundsBuilder.include(this)
            }
        }
    }

    fun getFormattedTime(distanceTime: DistanceTime): String {
        val minutes = distanceTime.time / 60
        val seconds = distanceTime.time % 60
        return FORMAT_TIME.format(minutes, seconds)
    }

    fun getFormattedDistance(distanceTime: DistanceTime): String {
        return FORMAT_DISTANCE.format(distanceTime.distance)
    }

    companion object {
        const val FORMAT_TIME = "%d분 %d초"
        const val FORMAT_DISTANCE = "%,dm"
    }

}