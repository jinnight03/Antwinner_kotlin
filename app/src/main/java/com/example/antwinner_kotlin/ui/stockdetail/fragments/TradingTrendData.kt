package com.example.antwinner_kotlin.ui.stockdetail.fragments

import com.google.gson.annotations.SerializedName

data class TradingTrendResponse(
    @SerializedName("개인") val individual: Long = 0,
    @SerializedName("외국인") val foreign: Long = 0,
    @SerializedName("금융투자") val securities: Long = 0,
    @SerializedName("기타금융") val otherFinance: Long = 0,
    @SerializedName("기타법인") val otherCorporation: Long = 0,
    @SerializedName("기타외국인") val otherForeign: Long = 0,
    @SerializedName("보험") val insurance: Long = 0,
    @SerializedName("사모") val privateEquity: Long = 0,
    @SerializedName("연기금") val pension: Long = 0,
    @SerializedName("은행") val bank: Long = 0,
    @SerializedName("투신") val investment: Long = 0,
    @SerializedName("날짜") val date: String = "",
    @SerializedName("한글종목약명") val stockName: String = "",
    @SerializedName("전체") val total: Long = 0
) {
    // 투자자 유형별 데이터를 Map으로 변환 (0이 아닌 값들만)
    fun toInvestorMap(): Map<String, Long> {
        val map = mutableMapOf<String, Long>()
        
        if (individual != 0L) map["개인"] = individual
        if (foreign != 0L) map["외국인"] = foreign
        if (securities != 0L) map["금융투자"] = securities
        if (otherFinance != 0L) map["기타금융"] = otherFinance
        if (otherCorporation != 0L) map["기타법인"] = otherCorporation
        if (otherForeign != 0L) map["기타외국인"] = otherForeign
        if (insurance != 0L) map["보험"] = insurance
        if (privateEquity != 0L) map["사모"] = privateEquity
        if (pension != 0L) map["연기금"] = pension
        if (bank != 0L) map["은행"] = bank
        if (investment != 0L) map["투신"] = investment
        
        return map
    }
    
    // 모든 투자자 유형 (0 포함)
    fun getAllInvestorMap(): Map<String, Long> {
        return mapOf(
            "개인" to individual,
            "외국인" to foreign,
            "금융투자" to securities,
            "기타금융" to otherFinance,
            "기타법인" to otherCorporation,
            "기타외국인" to otherForeign,
            "보험" to insurance,
            "사모" to privateEquity,
            "연기금" to pension,
            "은행" to bank,
            "투신" to investment
        )
    }
}

data class TradingTrendItem(
    val date: String,
    val investorData: Map<String, Long>
)

// API 응답 wrapper 모델 추가
data class TradingTrendApiResponse(
    @SerializedName("data") val data: List<TradingTrendResponse>,
    @SerializedName("message") val message: String,
    @SerializedName("stock_name") val stockName: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("total") val total: Int
) 