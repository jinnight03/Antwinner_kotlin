package com.example.antwinner_kotlin.ui.stockdetail.fragments

import com.google.gson.annotations.SerializedName

/**
 * 뉴스 API 응답 모델
 */
data class NewsApiResponse(
    @SerializedName("data") val data: List<NewsItem>
)

/**
 * 뉴스 아이템 모델
 */
data class NewsItem(
    @SerializedName("Stock_Name") val stockName: String?,
    @SerializedName("code") val code: String?,
    @SerializedName("link") val link: String?,
    @SerializedName("og_image") val ogImage: String?,
    @SerializedName("pDate") val pDate: String?,
    @SerializedName("title") val title: String?
)

/**
 * 뉴스 API 서비스 인터페이스
 */
interface NewsApiService {
    @retrofit2.http.GET("news_naver_real/{stockName}")
    suspend fun getStockNews(
        @retrofit2.http.Path("stockName") stockName: String
    ): retrofit2.Response<NewsApiResponse>
} 