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
import com.kakao.vectormap.MapLifeCycleCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var kakaoMap: KakaoMap

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
        viewModel.removeExistingRoute()
        viewModel.createMultiStyleRoute(viewState.routes, kakaoMap)
        setTimeDistanceView(viewState.distanceTime)
        hideLocationListWithAnimation()
    }

    private fun showErrorBottomSheet(routeError: RouteError) {
        val bottomSheet = ErrorBottomSheetFragment.newInstance(
            routeError.code,
            routeError.message,
            routeError.path
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
                viewModel.initRouteLineLayer(kakaoMap)
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

    override fun onResume() {
        super.onResume()
        binding.mapView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.pause()
    }
}