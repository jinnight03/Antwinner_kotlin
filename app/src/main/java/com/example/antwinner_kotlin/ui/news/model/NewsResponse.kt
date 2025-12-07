package com.example.antwinner_kotlin.ui.news.model

import com.google.gson.annotations.SerializedName

data class NewsResponse(
    val id: Int,
    @SerializedName("OG_제목") val title: String,
    @SerializedName("OG_설명") val description: String,
    @SerializedName("OG_이미지") val imageUrl: String,
    @SerializedName("날자") val date: String,
    @SerializedName("링크") val link: String,
    @SerializedName("종목명") val stockName: String?
) 