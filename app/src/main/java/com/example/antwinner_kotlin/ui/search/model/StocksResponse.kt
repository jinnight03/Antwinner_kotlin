package com.example.antwinner_kotlin.ui.search.model

import com.google.gson.annotations.SerializedName

/**
 * 주식 API 응답 래퍼 데이터 모델 (antwinner.com/api/stocks)
 */
data class StocksResponse(
    @SerializedName("stocks") val stocks: List<IssueStockResponse>
) 