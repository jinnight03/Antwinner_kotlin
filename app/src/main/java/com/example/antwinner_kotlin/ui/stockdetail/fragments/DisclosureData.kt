package com.example.antwinner_kotlin.ui.stockdetail.fragments

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 공시 API 응답
 */
data class DisclosureResponse(
    @SerializedName("company_name") val companyName: String,
    @SerializedName("data") val data: List<DisclosureApiItem>
)

/**
 * 공시 API 아이템 데이터 모델
 */
data class DisclosureApiItem(
    @SerializedName("corp_cls") val corpCls: String,
    @SerializedName("corp_code") val corpCode: String,
    @SerializedName("corp_name") val corpName: String,
    @SerializedName("flr_nm") val flrNm: String,
    @SerializedName("rcept_dt") val rceptDt: String,
    @SerializedName("rcept_no") val rceptNo: String,
    @SerializedName("report_nm") val reportNm: String,
    @SerializedName("rm") val rm: String,
    @SerializedName("stock_code") val stockCode: String
)

/**
 * 공시 항목
 */
data class DisclosureItem(
    @SerializedName("title")
    val title: String,           // 공시 제목
    @SerializedName("date")
    val date: String,           // 공시 날짜 (예: "2024-05-23")
    @SerializedName("category")
    val category: String,       // 공시 유형 (예: "실적발표", "자기주식취득", "인사발령")
    @SerializedName("url")
    val url: String?,           // 공시 상세 URL (옵션)
    @SerializedName("importance")
    val importance: String      // 중요도 (높음/보통/낮음 - 색상 결정용)
)

/**
 * 공시 API 서비스
 */
interface DisclosureApiService {
    @GET("gongsi/company/{stockName}")
    fun getDisclosureData(@Path("stockName") stockName: String): Call<DisclosureResponse>
} 