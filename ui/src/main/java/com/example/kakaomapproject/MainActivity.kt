package com.example.kakaomapproject

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.data.response.OriginDestination
import com.example.kakaomapproject.databinding.ActivityMainBinding
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initMap()
        observeViewModel()

        viewModel.fetchLocations()
        //viewModel.fetchRoute("서울역", "판교역")
        //viewModel.fetchDistanceTime("서울역", "판교역")
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.mainViewState.collect {
                when (it) {
                    is MainViewState.MapView -> {}
                    is MainViewState.ListView -> {
                        setLocationListView(it.locations)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun initMap() {
        binding.mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API 가 정상적으로 종료될 때 호출됨
            }

            override fun onMapError(error: Exception) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                // 인증 후 API 가 정상적으로 실행될 때 호출됨
            }
        })
    }

    private fun setLocationListView(locations: List<OriginDestination>) {
        val locationListAdapter = LocationListAdapter(locations)
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


}