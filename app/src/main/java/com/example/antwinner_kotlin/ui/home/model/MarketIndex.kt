package com.example.antwinner_kotlin.ui.home.model

import com.google.gson.annotations.SerializedName

data class MarketIndexResponse(
    @SerializedName("global") val global: List<MarketIndex>
)

data class MarketIndex(
    @SerializedName("change") val change: String,
    @SerializedName("change_price") val changePrice: String,
    @SerializedName("change_rate") val changeRate: String,
    @SerializedName("country") val country: String,
    @SerializedName("date") val date: String,
    @SerializedName("exchange_country") val exchangeCountry: String,
    @SerializedName("is_closing") val isClosing: String,
    @SerializedName("name") val name: String,
    @SerializedName("region") val region: String,
    @SerializedName("symbol_code") val symbolCode: String,
    @SerializedName("trade_price") val tradePrice: String
) {
    // "RISE" 또는 "FALL"의 상승/하락 여부를 확인
    val isRising: Boolean
        get() = change == "RISE"
} 