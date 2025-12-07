package com.example.antwinner_kotlin.ui.stockdetail.fragments

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 종합 분석 API 응답 모델
 */
data class ComprehensiveAnalysisResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("stock_name") val stockName: String?,
    @SerializedName("stock_symbol") val stockSymbol: String?,
    @SerializedName("industry") val industry: String?,
    @SerializedName("analysis_date") val analysisDate: String?,
    @SerializedName("comprehensive_analysis") val comprehensiveAnalysis: ComprehensiveAnalysisData?,
    @SerializedName("indicators") val indicators: IndicatorsData?
)

/**
 * 종합 분석 데이터
 */
data class ComprehensiveAnalysisData(
    @SerializedName("total_score") val totalScore: Double,
    @SerializedName("overall_rating") val overallRating: String,
    @SerializedName("attraction_score") val attractionScore: Int,
    @SerializedName("overall_comment") val overallComment: String
)

/**
 * 개별 지표 데이터
 */
data class IndicatorsData(
    @SerializedName("per") val per: IndicatorDetail?,
    @SerializedName("pbr") val pbr: IndicatorDetail?,
    @SerializedName("roe") val roe: IndicatorDetail?,
    @SerializedName("eps") val eps: IndicatorDetail?,
    @SerializedName("revenue_growth") val revenueGrowth: IndicatorDetail?,
    @SerializedName("operating_margin") val operatingMargin: IndicatorDetail?
)

/**
 * 개별 지표 상세 정보
 */
data class IndicatorDetail(
    @SerializedName("value") val value: String,
    @SerializedName("raw_value") val rawValue: Double?,
    @SerializedName("industry_avg") val industryAvg: String,
    @SerializedName("industry_avg_raw") val industryAvgRaw: Double?,
    @SerializedName("progress_percent") val progressPercent: Int,
    @SerializedName("comment") val comment: String,
    @SerializedName("rating") val rating: String,
    @SerializedName("score") val score: Double
)

/**
 * 종합 분석 API 서비스 인터페이스
 */
interface ComprehensiveAnalysisApiService {
    @GET("stock_comprehensive_analysis/{stockName}")
    suspend fun getComprehensiveAnalysis(
        @Path("stockName") stockName: String
    ): Response<ComprehensiveAnalysisResponse>
}

/**
 * API 응답을 기존 데이터 모델로 변환하는 유틸리티
 */
object ComprehensiveAnalysisConverter {
    
    /**
     * API 응답을 OverallAnalysis로 변환
     */
    fun toOverallAnalysis(response: ComprehensiveAnalysisResponse): OverallAnalysis? {
        val comprehensive = response.comprehensiveAnalysis ?: return null
        
        val rating = when (comprehensive.overallRating.uppercase()) {
            "EXCELLENT" -> IndicatorRating.EXCELLENT
            "GOOD" -> IndicatorRating.GOOD
            "AVERAGE" -> IndicatorRating.AVERAGE
            "POOR" -> IndicatorRating.POOR
            "BAD" -> IndicatorRating.BAD
            else -> IndicatorRating.AVERAGE
        }
        
        return OverallAnalysis(
            comment = comprehensive.overallComment,
            attractionScore = comprehensive.attractionScore,
            rating = rating
        )
    }
    
    /**
     * API 응답을 IndicatorAnalysis로 변환
     */
    fun toIndicatorAnalysis(indicator: IndicatorDetail?): IndicatorAnalysis {
        if (indicator == null) {
            return IndicatorAnalysis(
                value = "정보없음",
                industryAvg = "정보없음",
                progressPercent = 0,
                comment = "데이터를 불러올 수 없습니다.",
                rating = IndicatorRating.AVERAGE
            )
        }
        
        val rating = when (indicator.rating.uppercase()) {
            "EXCELLENT" -> IndicatorRating.EXCELLENT
            "GOOD" -> IndicatorRating.GOOD
            "AVERAGE" -> IndicatorRating.AVERAGE
            "POOR" -> IndicatorRating.POOR
            "BAD" -> IndicatorRating.BAD
            else -> IndicatorRating.AVERAGE
        }
        
        return IndicatorAnalysis(
            value = indicator.value,
            industryAvg = indicator.industryAvg,
            progressPercent = indicator.progressPercent,
            comment = indicator.comment,
            rating = rating
        )
    }
    
    /**
     * 전체 API 응답을 분석 결과 객체들로 변환
     */
    fun convertToAnalysisResults(response: ComprehensiveAnalysisResponse): AnalysisResults? {
        val overallAnalysis = toOverallAnalysis(response) ?: return null
        val indicators = response.indicators ?: return null
        
        return AnalysisResults(
            perAnalysis = toIndicatorAnalysis(indicators.per),
            pbrAnalysis = toIndicatorAnalysis(indicators.pbr),
            roeAnalysis = toIndicatorAnalysis(indicators.roe),
            epsAnalysis = toIndicatorAnalysis(indicators.eps),
            revenueGrowthAnalysis = toIndicatorAnalysis(indicators.revenueGrowth),
            operatingMarginAnalysis = toIndicatorAnalysis(indicators.operatingMargin),
            overallAnalysis = overallAnalysis
        )
    }
}

/**
 * 분석 결과를 담는 데이터 클래스
 */
data class AnalysisResults(
    val perAnalysis: IndicatorAnalysis,
    val pbrAnalysis: IndicatorAnalysis,
    val roeAnalysis: IndicatorAnalysis,
    val epsAnalysis: IndicatorAnalysis,
    val revenueGrowthAnalysis: IndicatorAnalysis,
    val operatingMarginAnalysis: IndicatorAnalysis,
    val overallAnalysis: OverallAnalysis
)
