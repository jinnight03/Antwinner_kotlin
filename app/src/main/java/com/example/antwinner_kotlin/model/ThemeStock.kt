package com.example.antwinner_kotlin.model

data class ThemeStock(
    val id: String,
    val name: String,
    val price: Int,
    val changeRate: Double,
    val tradingAmount: Long = 0,
    val logoUrl: String,
    val stockCode: String = ""
) 