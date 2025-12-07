package com.example.antwinner_kotlin.ui.home.model

/**
 * AI 키워드 API 응답 모델
 */
data class AIKeywordResponse(
    val frequency: Int,
    val keyword: String,
    val stock_names: List<String>
) 