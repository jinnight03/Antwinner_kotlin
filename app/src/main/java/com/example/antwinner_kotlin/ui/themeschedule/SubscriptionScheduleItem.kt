package com.example.antwinner_kotlin.ui.themeschedule

data class SubscriptionScheduleItem(
    val id: Int,
    val date: String,
    val title: String,
    val subscriptionPrice: String, // 공모가
    val expectedPrice: String, // 희망가
    val competitionRate: String, // 경쟁률
    val underwriter: String, // 주간사
    val showRedDot: Boolean, // 공모가 확정 전 빨간 점 표시 여부
    val companyName: String // 회사명 (API 호출용)
) 