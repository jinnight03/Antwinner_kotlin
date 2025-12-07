package com.example.antwinner_kotlin.ui.search.model

import com.google.gson.annotations.SerializedName

/**
 * 추천 검색어 API 응답 데이터 모델 (/api/latest_keywords)
 */
data class LatestKeywordResponse(
    val count: Int,
    val keyword: String
) 