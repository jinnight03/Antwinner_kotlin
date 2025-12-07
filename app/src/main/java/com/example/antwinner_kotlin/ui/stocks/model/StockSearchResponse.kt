package com.example.antwinner_kotlin.ui.stocks.model

import com.google.gson.annotations.SerializedName

/**
 * 종목 검색 API 응답 데이터 모델
 */
data class StockSearchResponse(
    @SerializedName("Stock_Code") val code: String,
    @SerializedName("Stock_Name") val name: String,
    @SerializedName("현재가") val price: String? = null,
    @SerializedName("등락률") val fluctuation: String? = "0.00%"
) 