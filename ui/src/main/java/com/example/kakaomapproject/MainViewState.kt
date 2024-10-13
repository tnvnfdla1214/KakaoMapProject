package com.example.kakaomapproject

import com.example.data.response.DistanceTime
import com.example.data.response.OriginDestination
import com.example.kakaomapproject.model.Route

sealed class MainViewState {
    data object Init : MainViewState()
    class MapView(val routes: List<Route>, val distanceTime: DistanceTime) : MainViewState()
    class ListView(val locations: List<OriginDestination>) : MainViewState()
}