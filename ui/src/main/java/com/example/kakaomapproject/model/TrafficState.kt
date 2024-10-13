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

        private val trafficStateMap by lazy { entries.associateBy(TrafficState::name) }

        fun fromString(value: String): TrafficState {
            return trafficStateMap[value] ?: UNKNOWN
        }
    }
}