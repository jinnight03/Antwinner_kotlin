package com.example.antwinner_kotlin.ui.home.model

import com.google.gson.annotations.SerializedName
import kotlin.math.abs

data class ThemeFluctuation(
    @SerializedName("average_rate") val averageRate: String = "0%",
    @SerializedName("companies") val companies: List<CompanyFluctuation> = emptyList(),
    @SerializedName("thema") val thema: String = "",
    @SerializedName("rising_ratio") val risingRatioString: String = "0%",
    @SerializedName("transaction_amount") val transactionAmount: String = "0",
    @SerializedName("uniformity") val uniformity: String = "0.0"
) {
    // 평균 등락률 문자열에서 숫자만 추출
    val averageRateValue: Double
        get() {
            // '%' 제거 후 Double로 변환 시도
            val cleaned = averageRate.replace("%", "").trim()
            return try {
                cleaned.toDouble()
            } catch (e: Exception) {
                0.0
            }
        }
    
    // 상승비율 문자열에서 숫자만 추출
    val risingRatioValue: Double
        get() {
            return try {
                // API에서 제공하는 risingRatio 사용
                val cleaned = risingRatioString.replace("%", "").trim()
                cleaned.toDouble()
            } catch (e: Exception) {
                // 실패시 직접 계산
                companies.count { it.isRising } * 100.0 / (companies.size.takeIf { it > 0 } ?: 1)
            }
        }
    
    // 테마의 상승/하락 여부 판단
    val isRising: Boolean
        get() = averageRateValue >= 0
    
    // 테마 크기 결정 (UI에 표시할 크기: 1-작음, 2-중간, 3-큼)
    val size: Int
        get() = when {
            Math.abs(averageRateValue) > 20 -> 3 // 20% 이상은 큰 박스
            Math.abs(averageRateValue) > 10 -> 2 // 10% 이상은 중간 박스
            else -> 1 // 나머지는 작은 박스
        }
}

data class CompanyFluctuation(
    @SerializedName("fluctuation") val fluctuation: String = "0%",
    @SerializedName("stockname") val stockName: String = "",
    @SerializedName("volume") val volume: String = "0"
) {
    // 등락률 문자열에서 숫자만 추출
    val fluctuationValue: Double
        get() {
            // '%' 제거 후 Double로 변환 시도
            val cleaned = fluctuation.replace("%", "").trim()
            return try {
                cleaned.toDouble()
            } catch (e: Exception) {
                0.0
            }
        }
    
    // 상승/하락 여부 판단
    val isRising: Boolean
        get() = fluctuationValue >= 0
} 