package com.example.kakaomapproject

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

    fun getLocations() {
        viewModelScope.launch {
            val locations = routeRepository.getLocations()
        }
    }

    fun getRoute(origin: String, destination: String) {
        viewModelScope.launch {
            val route = routeRepository.getRoute(origin, destination)
        }
    }

    fun getDistanceTime(origin: String, destination: String) {
        viewModelScope.launch {
            val distanceTime = routeRepository.getDistanceTime(origin, destination)
        }
    }

}