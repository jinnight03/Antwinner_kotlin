package com.example.antwinner_kotlin.ui.home.model

data class TrendDay(
    val day: Int,
    val themes: List<TrendTheme>,
    val themeName: String = ""
)

data class TrendTheme(
    val name: String,
    val stockCount: Int,
    val isPositive: Boolean = true,
    val iconResId: Int = 0
) 