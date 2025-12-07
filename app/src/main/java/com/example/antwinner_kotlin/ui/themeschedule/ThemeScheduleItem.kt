package com.example.antwinner_kotlin.ui.themeschedule

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class ThemeScheduleItem(
    val id: Int,
    val category: String, // 내용에서 파싱
    @DrawableRes val categoryIcon: Int?, // 카테고리에 따른 아이콘 (Drawable 리소스 ID)
    // val categoryImageUrl: String?, // 테마 이미지 URL 제거
    val date: String, // API의 '날자'
    val title: String, // API의 '내용' (카테고리 태그 제외)
    val relatedStocks: String, // API의 '종목명'
    val link: String, // API의 '링크'
    // 아래 필드는 API에 없으므로, 기본값 또는 로직으로 결정
    val impact: String = "정보없음",
    val opinion: String = "-",
    @ColorRes val opinionColor: Int
) 