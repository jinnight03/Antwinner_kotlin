package com.example.antwinner_kotlin.ui.stockdetail.fragments

import com.google.gson.annotations.SerializedName

/**
 * 투자 지표 API 응답 모델 (realprice 엔드포인트용)
 */
data class InvestmentIndicatorResponse(
    @SerializedName("PBR") val pbr: String?,
    @SerializedName("PER") val per: String?,
    @SerializedName("ROA") val roa: String?,
    @SerializedName("ROE") val roe: String?,
    @SerializedName("거래대금") val tradingValue: String?,
    @SerializedName("거래량") val tradingVolume: String?,
    @SerializedName("고가") val high: String?,
    @SerializedName("당기순이익") val netIncome: String?,
    @SerializedName("데이터_유효_회사수") val validCompanyCount: Int?,
    @SerializedName("등락률") val changeRate: String?,
    @SerializedName("매출액") val revenue: String?,
    @SerializedName("매출액증가율") val revenueGrowthRate: String?,
    @SerializedName("보통주배당금") val dividend: String?,
    @SerializedName("시가총액") val marketCap: String?,
    @SerializedName("업종내_회사수") val industryCompanyCount: Int?,
    @SerializedName("업종명") val industryName: String?,
    @SerializedName("업종평균_PBR") val industryAvgPbr: Double?,
    @SerializedName("업종평균_PER") val industryAvgPer: Double?,
    @SerializedName("업종평균_ROE") val industryAvgRoe: Double?,
    @SerializedName("업종평균_매출성장률") val industryAvgRevenueGrowth: Double?,
    @SerializedName("업종평균_영업이익률") val industryAvgOperatingMargin: Double?,
    @SerializedName("업종평균_주당순이익") val industryAvgEps: Double?,
    @SerializedName("영업이익") val operatingIncome: String?,
    @SerializedName("영업이익증가율") val operatingIncomeGrowthRate: String?,
    @SerializedName("종목명") val stockName: String?,
    @SerializedName("주당순이익") val eps: String?,
    @SerializedName("현재가") val currentPrice: String?
)

/**
 * 투자 지표 API 서비스 인터페이스
 */
interface InvestmentIndicatorApiService {
    @retrofit2.http.GET("realprice")
    suspend fun getInvestmentIndicators(
        @retrofit2.http.Query("company_names") companyNames: String
    ): retrofit2.Response<List<InvestmentIndicatorResponse>>
}

/**
 * 투자 지표 분석 결과
 */
data class IndicatorAnalysis(
    val value: String,           // 실제 값 (예: "17.4배", "30.69%")
    val industryAvg: String,     // 업종 평균 (예: "업종 평균 19.6배") 
    val progressPercent: Int,    // 프로그레스 바 퍼센트 (0-100)
    val comment: String,         // 분석 코멘트
    val rating: IndicatorRating  // 평가 등급
)

enum class IndicatorRating {
    EXCELLENT,  // 우수
    GOOD,       // 양호
    AVERAGE,    // 보통
    POOR,       // 저조
    BAD         // 불량
}

/**
 * 종합 분석 결과
 */
data class OverallAnalysis(
    val comment: String,        // 종합 분석 코멘트
    val attractionScore: Int,   // 투자 매력도 점수 (1-4)
    val rating: IndicatorRating // 전체 평가
) 