package com.example.data.response

import com.google.gson.annotations.SerializedName

data class RouteResponse(
    val points: String,
    @SerializedName("traffic_state") val trafficState: String,
)