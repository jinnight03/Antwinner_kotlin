package com.example.antwinner_kotlin.ui.home.model

/**
 * 가장 많이 오른 종목 데이터 모델
 */
data class TopRisingStock(
    val rank: Int,
    val name: String,
    val logoUrl: String,
    val percentChange: Double,
    val newsDate: String?,
    val dailyChange: Double?,
    val newsContent: String?
) 