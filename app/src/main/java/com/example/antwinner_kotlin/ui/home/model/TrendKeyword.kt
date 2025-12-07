package com.example.antwinner_kotlin.ui.home.model

data class TrendKeywordResponse(
    val date: String,
    val keywords: List<KeywordItem>
)

data class KeywordItem(
    val count: Int,
    val keyword: String
) 