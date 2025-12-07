package com.example.antwinner_kotlin.ui.theme.model

data class ThemeItem(
    val id: String,
    val name: String,
    val iconUrl: String,
    val stockCount: Int,
    val rateChange: Float,
    val isRising: Boolean,
    val stocks: List<ThemeStock>
) 