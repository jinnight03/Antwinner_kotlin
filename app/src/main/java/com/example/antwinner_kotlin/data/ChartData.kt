package com.example.antwinner_kotlin.data

import com.google.gson.annotations.SerializedName

data class ChartData(
    @SerializedName("Change") val change: Double,
    @SerializedName("Close") val close: Float,
    @SerializedName("High") val high: Float,
    @SerializedName("Low") val low: Float,
    @SerializedName("Open") val open: Float,
    @SerializedName("Volume") val volume: Long,
    @SerializedName("date") val date: String
) 