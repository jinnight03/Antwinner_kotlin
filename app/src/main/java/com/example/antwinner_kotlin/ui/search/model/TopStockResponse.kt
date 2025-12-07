package com.example.antwinner_kotlin.ui.search.model

import com.google.gson.annotations.SerializedName

data class TopStockResponse(
    val keyword: String,
    val stocks: List<TopStock>
)

data class TopStock(
    @SerializedName("등락률") val fluctuationRate: String,
    @SerializedName("이슈횟수") val issueCount: Int,
    @SerializedName("종목명") val stockName: String,
    @SerializedName("종목코드") val stockCode: String
) 