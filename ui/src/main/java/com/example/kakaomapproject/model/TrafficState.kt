package com.example.kakaomapproject.model

import androidx.annotation.ColorRes
import com.example.kakaomapproject.R

enum class TrafficState(@ColorRes val color: Int) {
    UNKNOWN(R.style.UnknownRouteLineStyle),
    JAM(R.style.JamRouteLineStyle),
    DELAY(R.style.DelayRouteLineStyle),
    SLOW(R.style.SlowRouteLineStyle),
    NORMAL(R.style.NormalRouteLineStyle),
    BLOCK(R.style.BlockRouteLineStyle);

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