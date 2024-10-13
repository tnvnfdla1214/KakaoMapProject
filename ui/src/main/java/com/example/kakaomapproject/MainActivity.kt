package com.example.kakaomapproject

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.data.response.DistanceTime
import com.example.data.response.OriginDestination
import com.example.kakaomapproject.databinding.ActivityMainBinding
import com.example.kakaomapproject.model.Route
import com.example.kakaomapproject.model.RouteError
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.LatLngBounds
import com.kakao.vectormap.MapLifeCycleCallback
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
import com.kakao.vectormap.route.RouteLineStyle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var kakaoMap: KakaoMap

    private lateinit var routeLineLayer: RouteLineLayer
    private var multiStyleLine: RouteLine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initMap()
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.mainViewState.collect { viewState ->
                when (viewState) {
                    is MainViewState.MapView -> handleMapViewState(viewState)
                    is MainViewState.ListView -> setLocationListView(viewState.locations)
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            viewModel.errorViewState.collect { errorState ->
                errorState?.let { showErrorBottomSheet(errorState) }
            }
        }
    }

    private fun handleMapViewState(viewState: MainViewState.MapView) {
        removeExistingRoute()
        createMultiStyleRoute(viewState.routes)
        setTimeDistanceView(viewState.distanceTime)
        hideLocationListWithAnimation()
    }

    private fun showErrorBottomSheet(routeError: RouteError) {
        val bottomSheet = ErrorBottomSheetFragment.newInstance(
            routeError.code,
            routeError.message,
            routeError.path,
            onDismissCallback = { viewModel.restErrorViewState() }
        )
        bottomSheet.show(supportFragmentManager, "ErrorBottomSheet")
    }

    private fun initMap() {
        binding.mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {}

            override fun onMapError(error: Exception) {}
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                kakaoMap = map
                initRouteLineLayer()
            }
        })
    }

    private fun setTimeDistanceView(distanceTime: DistanceTime) {
        binding.time.text = distanceTime.getFormattedTime()
        binding.distance.text = distanceTime.getFormattedDistance()
        binding.timeDistanceBox.visibility = View.VISIBLE
    }

    private fun setLocationListView(locations: List<OriginDestination>) {
        val locationListAdapter = LocationListAdapter(locations) { location ->
            viewModel.fetchRoute(location)
        }
        binding.locationListView.layoutManager = LinearLayoutManager(this)
        binding.locationListView.adapter = locationListAdapter

    }

    private fun hideLocationListWithAnimation() {
        val translationY = ObjectAnimator.ofFloat(
            binding.locationListView,
            "translationY",
            0f,
            binding.locationListView.height.toFloat()
        )
        val fadeOut = ObjectAnimator.ofFloat(binding.locationListView, "alpha", 1f, 0f)

        AnimatorSet().apply {
            playTogether(translationY, fadeOut)
            duration = 300
            start()
            doOnEnd {
                binding.locationListView.visibility = View.INVISIBLE
                resetLocationListView()
            }
        }
    }

    private fun resetLocationListView() {
        binding.locationListView.translationY = 0f
        binding.locationListView.alpha = 1f
    }

    fun initRouteLineLayer() {
        routeLineLayer = kakaoMap.routeLineManager?.layer ?: return
    }

    fun removeExistingRoute() {
        multiStyleLine?.let { routeLine ->
            routeLineLayer.remove(routeLine)
            multiStyleLine = null
        }
    }

    fun createMultiStyleRoute(routes: List<Route>) {
        val segments = mutableListOf<RouteLineSegment>()
        val boundsBuilder = LatLngBounds.Builder()

        routes.forEachIndexed { index, route ->
            val points = parseRoutePoints(route, boundsBuilder)
            segments.add(
                RouteLineSegment.from(
                    points,
                    RouteLineStyle.from(application.baseContext, route.trafficState.color)
                )
            )

            if (index == 0) addIconTextLabel("startLabel_$index", points.first(), "Start")
            if (index == routes.lastIndex) addIconTextLabel(
                "endLabel_$index",
                points.last(),
                "End",
            )
        }
        drawRouteLine(segments)
        moveCameraToRouteBounds(boundsBuilder, kakaoMap)
    }

    private fun addIconTextLabel(
        labelId: String,
        position: LatLng,
        text: String,
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

    private fun parseRoutePoints(route: Route, boundsBuilder: LatLngBounds.Builder): List<LatLng> {
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

    override fun onResume() {
        super.onResume()
        binding.mapView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.pause()
    }
}