package com.example.kakaomapproject

import com.example.data.response.OriginDestination
import com.example.data.response.Route

sealed class MainViewState {
    data object Init : MainViewState()
    class MapView(val route: Route) : MainViewState()
    class ListView(val locations: List<OriginDestination>) : MainViewState()
}