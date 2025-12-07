package com.example.antwinner_kotlin.ui.home.model

data class Theme(
    val name: String,
    val rate: Double,
    val risingRatio: Double,
    val isRising: Boolean,
    val size: Int = 1,
    val relativeImportance: Double = 0.0 // 상대적 중요도 (하락 테마 농도 계산에 사용)
) 