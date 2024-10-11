package com.example.data.service.response

import com.google.gson.annotations.SerializedName

data class Route(
    val points: String,
    @SerializedName("traffic_state") val trafficState: String,
)