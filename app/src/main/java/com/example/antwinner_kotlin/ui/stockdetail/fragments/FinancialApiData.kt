package com.example.antwinner_kotlin.ui.stockdetail.fragments

import com.google.gson.annotations.SerializedName

/**
 * 실적 API 응답 모델 (financial_data 엔드포인트용)
 */
data class FinancialApiResponse(
    @SerializedName("company_name") val companyName: String,
    @SerializedName("data") val data: FinancialData
)

data class FinancialData(
    @SerializedName("annual") val annual: List<FinancialRecord>,
    @SerializedName("quarterly") val quarterly: List<FinancialRecord>
)

data class FinancialRecord(
    @SerializedName("company_name") val companyName: String?,
    @SerializedName("symbol") val symbol: String?,
    @SerializedName("날짜") val date: String,
    @SerializedName("매출액") val revenue: Double?,  // Long → Double로 변경
    @SerializedName("영업이익") val operatingProfit: Double?,  // Long → Double로 변경
    @SerializedName("당기순이익") val netIncome: Double?,  // Long → Double로 변경
    @SerializedName("영업이익_발표기준") val operatingProfitAnnounced: Double?,
    @SerializedName("순이익률") val netProfitMargin: Double?,
    @SerializedName("영업이익률") val operatingProfitMargin: Double?,
    @SerializedName("매출액증가율") val revenueGrowthRate: Double?,
    @SerializedName("영업이익증가율") val operatingProfitGrowthRate: Double?,
    // 기타 필드들 (필요에 따라 추가)
    @SerializedName("PER_배") val per: Double?,
    @SerializedName("PBR_배") val pbr: Double?,
    @SerializedName("ROE_퍼센트") val roe: Double?,
    @SerializedName("ROA_퍼센트") val roa: Double?
)

/**
 * 새로운 실적 API 서비스 인터페이스
 */
interface FinancialApiService {
    @retrofit2.http.GET("financial_data/{stockName}")
    suspend fun getFinancialData(@retrofit2.http.Path("stockName") stockName: String): retrofit2.Response<FinancialApiResponse>
} 