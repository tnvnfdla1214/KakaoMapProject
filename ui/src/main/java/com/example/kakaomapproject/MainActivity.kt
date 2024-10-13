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
        viewModel.fetchLocations()
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
        bottomSheet.show(supportFragmentManager, ErrorBottomSheetFragment.TAG)
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
        binding.time.text = viewModel.getFormattedTime(distanceTime)
        binding.distance.text = viewModel.getFormattedDistance(distanceTime)
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
            TRANSLATION_Y,
            0f,
            binding.locationListView.height.toFloat()
        )
        val fadeOut = ObjectAnimator.ofFloat(binding.locationListView, ALPHA, FULL_OPACITY, NO_OPACITY)

        AnimatorSet().apply {
            playTogether(translationY, fadeOut)
            duration = ANIMATION_DURATION
            start()
            doOnEnd {
                binding.locationListView.visibility = View.INVISIBLE
                resetLocationListView()
            }
        }
    }

    private fun resetLocationListView() {
        binding.locationListView.translationY = INITIAL_POSITION
        binding.locationListView.alpha = FULL_OPACITY
    }

    fun initRouteLineLayer() {
        routeLineLayer = kakaoMap.routeLineManager?.layer ?: return
    }

    private fun removeExistingRoute() {
        multiStyleLine?.let { routeLine ->
            routeLineLayer.remove(routeLine)
            multiStyleLine = null
        }
    }

    private fun createMultiStyleRoute(routes: List<Route>) {
        val segments = mutableListOf<RouteLineSegment>()
        val boundsBuilder = LatLngBounds.Builder()

        routes.forEachIndexed { index, route ->
            val points = viewModel.parseRoutePoints(route, boundsBuilder)
            segments.add(
                RouteLineSegment.from(
                    points,
                    RouteLineStyle.from(application.baseContext, route.trafficState.color)
                )
            )

            if (index == 0) addIconTextLabel(points.first(), START_LABEL)
            if (index == routes.lastIndex) addIconTextLabel(
                points.last(),
                END_LABEL,
            )
        }
        drawRouteLine(segments)
        moveCameraToRouteBounds(boundsBuilder, kakaoMap)
    }

    private fun addIconTextLabel(
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

        labelLayer?.addLabel(
            LabelOptions.from(text, position).setStyles(styles)
                .setTexts(LabelTextBuilder().setTexts(text))
        )
    }

    private fun drawRouteLine(segments: List<RouteLineSegment>) {
        val options = RouteLineOptions.from(segments)
        routeLineLayer.addRouteLine(options)
    }

    private fun moveCameraToRouteBounds(boundsBuilder: LatLngBounds.Builder, kakaoMap: KakaoMap) {
        val bounds = boundsBuilder.build()
        kakaoMap.moveCamera(
            CameraUpdateFactory.fitMapPoints(bounds, CAMERA_PADDING),
            CameraAnimation.from(CAMERA_ANIMATION_DURATION)
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

    companion object {
        private const val TRANSLATION_Y = "translationY"
        private const val ALPHA = "alpha"
        private const val INITIAL_POSITION = 0f
        private const val NO_OPACITY = 0f
        private const val FULL_OPACITY = 1f
        private const val ANIMATION_DURATION = 300L
        private const val CAMERA_ANIMATION_DURATION = 500
        private const val START_LABEL = "Start"
        private const val END_LABEL = "End"
        private const val CAMERA_PADDING = 100
    }
}