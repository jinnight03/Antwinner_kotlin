package com.example.antwinner_kotlin.model

/**
 * 테마 상세 화면의 '급등 이유' 섹션에 사용될 데이터 모델
 */
data class ThemeSurgeReason(
    val id: String, // 각 이유 항목의 고유 ID (API 응답 기반 또는 임의 생성)
    val relatedStockName: String?, // 관련된 특정 종목명 (없을 수도 있음)
    val relatedStockRate: Double?, // 관련 종목 등락률 (없을 수도 있음)
    val themeName: String,         // 테마명 (항상 존재)
    val reasonTitle: String,       // 급등 사유 (뉴스 제목 등)
    val date: String,              // 날짜 (YYYY. MM. DD 형식)
    val tradingVolume: String,     // 거래량 문자열 (예: "170만")
    val tradingValue: String       // 거래대금 문자열 (예: "43억")
) 