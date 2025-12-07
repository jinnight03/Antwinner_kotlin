package com.example.antwinner_kotlin.model

import com.google.gson.annotations.SerializedName

data class WeeklyRankingResponse(
    @SerializedName("thema_ranking")
    val themaRanking: List<WeeklyThemeRanking>
)

data class WeeklyThemeRanking(
    @SerializedName("이슈횟수")
    val issueCount: Int,
    
    @SerializedName("종목수")
    val stockCount: Int,
    
    @SerializedName("테마명")
    val themeName: String
) 