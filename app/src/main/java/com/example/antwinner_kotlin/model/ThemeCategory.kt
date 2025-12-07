package com.example.antwinner_kotlin.model

data class ThemeCategory(
    val id: String,
    val name: String,
    val fluctuationRate: Double,
    val stocks: List<ThemeStock>
) 