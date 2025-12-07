package com.example.antwinner_kotlin.ui.home.model

/**
 * 노려볼만한 테마 데이터 모델
 */
data class PromisingTheme(
    val id: String,
    val name: String,        // 테마명
    val logoUrl: String,     // 테마 로고 URL
    val stockNames: List<String>,  // 관련 종목명 목록
    val isHot: Boolean = false     // 인기 테마 여부
) 