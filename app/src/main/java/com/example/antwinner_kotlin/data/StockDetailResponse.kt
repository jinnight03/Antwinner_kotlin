package com.example.antwinner_kotlin.data

import com.google.gson.annotations.SerializedName

data class StockDetailResponse(
    @SerializedName("industry") val industry: String,
    @SerializedName("main_product") val mainProduct: String,
    @SerializedName("날짜") val date: String,
    @SerializedName("등락률") val changeRate: String,
    @SerializedName("전일비") val previousDayDiff: String,
    @SerializedName("종목코드") val stockCode: String,
    @SerializedName("총투자자") val totalInvestors: String,
    @SerializedName("테마목록") val themeList: List<String>,
    @SerializedName("평균단가") val averagePrice: String,
    @SerializedName("평균수익률") val averageReturnRate: String,
    @SerializedName("현재가") val currentPrice: String,
    @SerializedName("회사명") val companyName: String
) 