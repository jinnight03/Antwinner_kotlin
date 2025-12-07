package com.example.antwinner_kotlin.ui.search.model

import com.google.gson.annotations.SerializedName

/**
 * 오늘의 이슈종목 API 응답 데이터 모델 (antwinner.com/api/stocks)
 */
data class IssueStockResponse(
    val id: Int,
    @SerializedName("거래대금") val tradingAmount: String,
    @SerializedName("거래량") val volume: String,
    @SerializedName("날자") val date: String,
    @SerializedName("상승률") val riseRate: Double,
    @SerializedName("상승이유") val riseReason: String,
    @SerializedName("종목명") val stockName: String,
    @SerializedName("종목코드") val stockCode: String,
    @SerializedName("테마") val theme: String
) 