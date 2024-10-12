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
import com.kakao.vectormap.route.RouteLine
import com.kakao.vectormap.route.RouteLineLayer
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import com.kakao.vectormap.shape.MapPoints
import com.kakao.vectormap.shape.Polyline
import com.kakao.vectormap.shape.PolylineCap
import com.kakao.vectormap.shape.PolylineOptions
import com.kakao.vectormap.shape.PolylineStyles
import com.kakao.vectormap.shape.PolylineStylesSet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Collections.addAll

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
        // Y축 이동 애니메이션 (아래로 이동)
        val translationY = ObjectAnimator.ofFloat(
            binding.locationListView,
            "translationY",
            0f,
            binding.locationListView.height.toFloat()
        )

        // 알파값을 1에서 0으로 (투명해지기)
        val fadeOut = ObjectAnimator.ofFloat(binding.locationListView, "alpha", 1f, 0f)

        // 애니메이션 세트를 통해 동시에 실행
        val animatorSet = AnimatorSet().apply {
            playTogether(translationY, fadeOut)
            duration = 300 // 애니메이션 시간 설정 (300ms)
        }

        animatorSet.start()
        // 애니메이션 끝난 후 View.GONE 처리
        animatorSet.doOnEnd {
            binding.locationListView.visibility = View.GONE
            binding.locationListView.translationY = 0f // 위치 원복
            binding.locationListView.alpha = 1f // 알파값 원복
        }
    }

    private fun createMultiStyleRoute(routes: List<Route>) {
        val segments = mutableListOf<RouteLineSegment>()

        // Loop through each route and create segments with different styles
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
                LatLng.from(latLng[1].toDouble(), latLng[0].toDouble())
            }
            segments.add(RouteLineSegment.from(points, style))
        }

        // Create RouteLineOptions with segments
        val options = RouteLineOptions.from(segments)

        // Add route line to the map
        multiStyleLine = routeLineLayer.addRouteLine(options)

        // Move camera to the route
        if (routes.isNotEmpty()) {
            kakaoMap.moveCamera(
                CameraUpdateFactory.newCenterPosition(LatLng.from(37.394882, 127.110457), 15),
                CameraAnimation.from(500)
            )
        }
    }
}