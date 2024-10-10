package com.example.kakaomapproject

import android.app.Application
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KakaoMapProject: Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoMapSdk.init(this, "b0544af71148d1e6576d87d8f6b18d39")
    }
}