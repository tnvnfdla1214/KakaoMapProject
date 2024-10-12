package com.example.kakaomapproject

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.data.response.DistanceTime
import com.example.data.response.OriginDestination
import com.example.data.response.Route
import com.example.kakaomapproject.databinding.ActivityMainBinding
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
                    is MainViewState.MapView -> {
                        if (multiStyleLine != null) {
                            routeLineLayer.remove(multiStyleLine)
                        }
                        createMultiStyleRoute(viewState.routes)
                        setTimeDistanceView(viewState.distanceTime)
                        hideLocationListWithAnimation()
                    }

                    is MainViewState.ListView -> {
                        setLocationListView(viewState.locations)
                    }

                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            viewModel.errorViewState.collect {
                it?.let {
                    val bottomSheet =
                        ErrorBottomSheetFragment.newInstance(it.code, it.message, it.path)
                    bottomSheet.show(supportFragmentManager, "ErrorBottomSheet")
                }
            }
        }
    }

    private fun initMap() {
        binding.mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {}

            override fun onMapError(error: Exception) {}
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                kakaoMap = map
                kakaoMap.routeLineManager?.let { routeLineManager ->
                    routeLineLayer = routeLineManager.getLayer()
                }
                //routeLineLayer = kakaoMap.routeLineManager.getLayer()
            }
        })
    }

    private fun setTimeDistanceView(distanceTime: DistanceTime) {
        binding.time.text = distanceTime.time.toString()
        binding.distance.text = distanceTime.distance.toString()
        binding.timeDistanceBox.visibility = View.VISIBLE
    }

    private fun setLocationListView(locations: List<OriginDestination>) {
        val locationListAdapter = LocationListAdapter(locations) { location ->
            viewModel.fetchRoute(location)
        }
        binding.locationListView.layoutManager = LinearLayoutManager(this)
        binding.locationListView.adapter = locationListAdapter

    }

    override fun onResume() {
        super.onResume()
        binding.mapView.resume() // MapView 의 resume 호출
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.pause() // MapView 의 pause 호출
    }

    private fun hideLocationListWithAnimation() {
        val translationY = ObjectAnimator.ofFloat(
            binding.locationListView,
            "translationY",
            0f,
            binding.locationListView.height.toFloat()
        )

        val fadeOut = ObjectAnimator.ofFloat(binding.locationListView, "alpha", 1f, 0f)

        val animatorSet = AnimatorSet().apply {
            playTogether(translationY, fadeOut)
            duration = 300
        }

        animatorSet.start()
        animatorSet.doOnEnd {
            binding.locationListView.visibility = View.GONE
            binding.locationListView.translationY = 0f // 위치 원복
            binding.locationListView.alpha = 1f // 알파값 원복
        }
    }

    private fun createMultiStyleRoute(routes: List<Route>) {
        val segments = mutableListOf<RouteLineSegment>()
        val boundsBuilder = LatLngBounds.Builder()

        routes.forEachIndexed { index, route ->
            val style = when (index % 3) {
                0 -> RouteLineStyle.from(this, R.style.RedRouteLineStyle)
                1 -> RouteLineStyle.from(this, R.style.YellowRouteLineStyle)
                2 -> RouteLineStyle.from(this, R.style.GreenRouteLineStyle)
                else -> RouteLineStyle.from(this, R.style.BlueRouteLineStyle)
            }

            // Convert route points to LatLng and create RouteLineSegment
            val points = route.points.split(" ").map {
                val latLng = it.split(",")
                val latLngPoint = LatLng.from(latLng[1].toDouble(), latLng[0].toDouble())
                boundsBuilder.include(latLngPoint) // Add each LatLng point to bounds builder
                latLngPoint
            }
            segments.add(RouteLineSegment.from(points, style))

            if (index == 0) {
                addIconTextLabel("startLabel_$index", points.first(), "Start")
            }
            if (index == routes.lastIndex) {
                addIconTextLabel("endLabel_$index", points.last(), "End")
            }
        }

        val options = RouteLineOptions.from(segments)

        multiStyleLine = routeLineLayer.addRouteLine(options)

        // Move the camera to fit the route bounds
        val bounds = boundsBuilder.build() // Build the bounds from all the route points
        kakaoMap.moveCamera(
            CameraUpdateFactory.fitMapPoints(
                bounds,
                100
            ), // Adjust the camera to fit the bounds with padding
            CameraAnimation.from(500) // Optional smooth animation
        )
    }

    private fun addIconTextLabel(labelId: String, position: LatLng, text: String) {
        val labelLayer = kakaoMap.labelManager?.layer

        // Define the styles for the marker with text
        val styles = kakaoMap.labelManager?.addLabelStyles(
            LabelStyles.from(
                LabelStyle.from(R.drawable.ic_launcher_foreground).setTextStyles(
                    LabelTextStyle.from(this, R.style.labelTextStyle_1),
                    LabelTextStyle.from(this, R.style.labelTextStyle_2)
                ).setIconTransition(LabelTransition.from(Transition.None, Transition.None))
            )
        )

        // Create and add the label to the map
        labelLayer?.addLabel(
            LabelOptions.from(labelId, position).setStyles(styles)
                .setTexts(LabelTextBuilder().setTexts(text))
        )
    }
}