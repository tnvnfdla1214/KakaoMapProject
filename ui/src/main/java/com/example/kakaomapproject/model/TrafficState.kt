package com.example.kakaomapproject.model

enum class TrafficState(val description: String) {
    UNKNOWN("교통정보 없음"),
    JAM("정체"),
    DELAY("지체"),
    SLOW("서행"),
    NORMAL("원활"),
    BLOCK("교통사고");

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