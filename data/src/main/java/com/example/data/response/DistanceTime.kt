package com.example.data.response

data class DistanceTime(
    val distance: Int,
    val time: Int,
) {

    fun getFormattedTime(): String {
        val minutes = time / 60
        val seconds = time % 60
        return "%d분 %d초".format(minutes, seconds)
    }

    fun getFormattedDistance(): String {
        return "%,dm".format(distance)
    }
}