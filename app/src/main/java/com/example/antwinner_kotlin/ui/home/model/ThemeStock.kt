package com.example.antwinner_kotlin.ui.home.model

data class ThemeStock(
    val name: String,    // 종목명
    val rate: Double,    // 등락률
    val volume: String = ""   // 거래량 (옵션)
) 