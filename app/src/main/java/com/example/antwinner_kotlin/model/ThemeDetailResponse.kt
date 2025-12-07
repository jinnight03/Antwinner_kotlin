package com.example.antwinner_kotlin.model

import com.google.gson.annotations.SerializedName

data class ThemeDetailResponse(
    @SerializedName("execution_time_ms") val executionTimeMs: Double?,
    @SerializedName("issue_count") val issueCount: Int?,
    @SerializedName("period") val period: String?,
    @SerializedName("rank_info") val rankInfo: ThemeRankInfo?,
    @SerializedName("stock_count") val stockCount: Int?,
    @SerializedName("stock_details") val stockDetails: List<ThemeStockDetail>?
)

data class ThemeRankInfo(
    @SerializedName("percentile") val percentile: Double?,
    @SerializedName("rank") val rank: Int?,
    @SerializedName("total_themas") val totalThemas: Int?
)

data class ThemeStockDetail(
    @SerializedName("등락률") val changeRate: String?,
    @SerializedName("이슈목록") val issueList: List<ThemeStockIssue>?,
    @SerializedName("이슈횟수") val issueCount: Int?,
    @SerializedName("전일비") val dayChange: String?,
    @SerializedName("종목명") val stockName: String?,
    @SerializedName("현재가") val currentPrice: String?
)

data class ThemeStockIssue(
    @SerializedName("날짜") val date: String?,
    @SerializedName("상승률") val riseRate: Double?,
    @SerializedName("상승이유") val riseReason: String?
) 