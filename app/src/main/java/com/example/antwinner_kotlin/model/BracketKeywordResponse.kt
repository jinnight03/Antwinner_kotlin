package com.example.antwinner_kotlin.model

import com.google.gson.annotations.SerializedName

/**
 * /api/stocks/bracket-keyword/{테마명} API 응답 모델
 */
data class BracketKeywordResponse(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("거래대금")
    val tradingValue: String?,

    @SerializedName("거래량")
    val tradingVolume: String?,

    @SerializedName("날자") // 오타 주의: API 응답이 "날자"일 경우
    val date: String?,

    @SerializedName("상승률")
    val fluctuationRate: Double?,

    @SerializedName("상승이유")
    val reason: String?,

    @SerializedName("종목명")
    val stockName: String?,

    @SerializedName("종목코드")
    val stockCode: String?,

    @SerializedName("테마")
    val theme: String?
) 