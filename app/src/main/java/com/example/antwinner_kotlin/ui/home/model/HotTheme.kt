package com.example.antwinner_kotlin.ui.home.model

data class HotTheme(
    val name: String,
    val percent: Double,
    val logoUrl: String,
    val companies: List<ThemeCompany>
)

data class ThemeCompany(
    val name: String,
    val percent: Double,
    val marketCap: String
) 