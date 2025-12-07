package com.example.antwinner_kotlin.model

/**
 * 테마 아이템을 위한 데이터 클래스
 *
 * @param id 테마 ID
 * @param name 테마 이름
 * @param rate 테마의 등락률 (문자열로 표시)
 */
data class ThemeItem(
    val id: String = "",
    val name: String,
    val rate: String
) 