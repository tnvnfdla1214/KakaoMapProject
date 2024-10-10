package com.example.kakaomapproject

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView


class MainActivity : AppCompatActivity() {
    lateinit var mapView: MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KakaoMapSdk.INSTANCE.toString()
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.map_view)
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                Log.d("qweqwe", "onMapDestroy")
                // 지도 API 가 정상적으로 종료될 때 호출됨
            }

            override fun onMapError(error: Exception) {
                Log.d("qweqwe", "error : " + error)
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                Log.d("qweqwe", "kakaoMap : " + kakaoMap.isMapClickable)
                // 인증 후 API 가 정상적으로 실행될 때 호출됨
            }
        })
    }

    public override fun onResume() {
        super.onResume()
        mapView.resume() // MapView 의 resume 호출
    }

    public override fun onPause() {
        super.onPause()
        mapView.pause() // MapView 의 pause 호출
    }


}