package com.example.kakaomapproject

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RouteRepository
import com.example.data.response.OriginDestination
import com.example.data.response.ApiException
import com.example.kakaomapproject.model.Route
import com.example.kakaomapproject.model.RouteError
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.LatLngBounds
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import com.kakao.vectormap.label.LabelTextStyle
import com.kakao.vectormap.label.LabelTransition
import com.kakao.vectormap.label.Transition
import com.kakao.vectormap.route.RouteLine
import com.kakao.vectormap.route.RouteLineLayer
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    private val application: Application
) : ViewModel() {

    private val _mainViewState = MutableStateFlow<MainViewState>(MainViewState.Init)
    val mainViewState: StateFlow<MainViewState> = _mainViewState

    private val _errorViewState = MutableStateFlow<RouteError?>(null)
    val errorViewState: StateFlow<RouteError?> = _errorViewState

    private lateinit var routeLineLayer: RouteLineLayer
    private var multiStyleLine: RouteLine? = null

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
                }.onFailure { throwable ->
                    Log.d("qweqwe", "throwable : " + throwable)
                }
        }
    }

    fun initRouteLineLayer(kakaoMap: KakaoMap) {
        routeLineLayer = kakaoMap.routeLineManager?.layer ?: return
    }

    fun removeExistingRoute() {
        multiStyleLine?.let { routeLine ->
            routeLineLayer.remove(routeLine)
            multiStyleLine = null
        }
    }

    fun createMultiStyleRoute(routes: List<Route>, kakaoMap: KakaoMap) {
        val segments = mutableListOf<RouteLineSegment>()
        val boundsBuilder = LatLngBounds.Builder()

        routes.forEachIndexed { index, route ->
            val points = parseRoutePoints(route, boundsBuilder)
            val style = route.trafficState.getRouteLineStyle(application.baseContext)
            segments.add(RouteLineSegment.from(points, style))

            if (index == 0) addIconTextLabel("startLabel_$index", points.first(), "Start", kakaoMap)
            if (index == routes.lastIndex) addIconTextLabel(
                "endLabel_$index",
                points.last(),
                "End",
                kakaoMap
            )
        }
        drawRouteLine(segments)
        moveCameraToRouteBounds(boundsBuilder, kakaoMap)
    }

    private fun addIconTextLabel(
        labelId: String,
        position: LatLng,
        text: String,
        kakaoMap: KakaoMap
    ) {
        val labelLayer = kakaoMap.labelManager?.layer
        val styles = kakaoMap.labelManager?.addLabelStyles(
            LabelStyles.from(
                LabelStyle.from().setTextStyles(
                    LabelTextStyle.from(application.baseContext, R.style.labelTextStyle_1),
                    LabelTextStyle.from(application.baseContext, R.style.labelTextStyle_2)
                ).setIconTransition(LabelTransition.from(Transition.None, Transition.None))
            )
        )

        // Create and add the label to the map
        labelLayer?.addLabel(
            LabelOptions.from(labelId, position).setStyles(styles)
                .setTexts(LabelTextBuilder().setTexts(text))
        )
    }

    fun parseRoutePoints(route: Route, boundsBuilder: LatLngBounds.Builder): List<LatLng> {
        return route.points.split(" ").map {
            val latLng = it.split(",")
            LatLng.from(latLng[1].toDouble(), latLng[0].toDouble()).apply {
                boundsBuilder.include(this)
            }
        }
    }

    private fun drawRouteLine(segments: List<RouteLineSegment>) {
        val options = RouteLineOptions.from(segments)
        routeLineLayer.addRouteLine(options)
    }

    private fun moveCameraToRouteBounds(boundsBuilder: LatLngBounds.Builder, kakaoMap: KakaoMap) {
        val bounds = boundsBuilder.build()
        kakaoMap.moveCamera(
            CameraUpdateFactory.fitMapPoints(bounds, 100),
            CameraAnimation.from(500)
        )
    }

}