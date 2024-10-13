package com.example.kakaomapproject.model

import android.content.Context
import com.example.kakaomapproject.R
import com.kakao.vectormap.route.RouteLineStyle

enum class TrafficState(val description: String) {
    UNKNOWN("교통정보 없음"),
    JAM("정체"),
    DELAY("지체"),
    SLOW("서행"),
    NORMAL("원활"),
    BLOCK("교통사고");

    fun getRouteLineStyle(context: Context): RouteLineStyle {
        return when (this) {
            UNKNOWN -> RouteLineStyle.from(context, R.style.UnknownRouteLineStyle)
            BLOCK -> RouteLineStyle.from(context, R.style.BlockRouteLineStyle)
            JAM -> RouteLineStyle.from(context, R.style.JamRouteLineStyle)
            DELAY -> RouteLineStyle.from(context, R.style.DelayRouteLineStyle)
            SLOW -> RouteLineStyle.from(context, R.style.SlowRouteLineStyle)
            NORMAL -> RouteLineStyle.from(context, R.style.NormalRouteLineStyle)
        }
    }

    companion object {
        fun fromString(value: String): TrafficState {
            return when (value) {
                "UNKNOWN" -> UNKNOWN
                "JAM" -> JAM
                "DELAY" -> DELAY
                "SLOW" -> SLOW
                "NORMAL" -> NORMAL
                "BLOCK" -> BLOCK
                else -> UNKNOWN
            }
        }
    }
}