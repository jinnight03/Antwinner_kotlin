package com.example.antwinner_kotlin.ui.stockdetail

data class SignificantRiseDay(
    val stockName: String,
    val changePercent: Double,
    val themeName: String,
    val newsTitle: String,
    val date: String,
    val tradingValue: String, // 예: "3018억"
    val tradingVolume: String // 예: "5315만"
    // 필요 시 아이콘 종류 등 추가 정보 포함 가능
) 