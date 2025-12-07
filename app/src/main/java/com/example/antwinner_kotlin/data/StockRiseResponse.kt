package com.example.antwinner_kotlin.data

import com.google.gson.annotations.SerializedName

data class StockRiseResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("거래대금") val tradingValue: String,
    @SerializedName("거래량") val tradingVolume: String,
    @SerializedName("날자") val date: String,
    @SerializedName("상승률") val riseRate: Double,
    @SerializedName("상승이유") val riseReason: String,
    @SerializedName("종목명") val stockName: String,
    @SerializedName("종목코드") val stockCode: String,
    @SerializedName("테마") val theme: String
) 