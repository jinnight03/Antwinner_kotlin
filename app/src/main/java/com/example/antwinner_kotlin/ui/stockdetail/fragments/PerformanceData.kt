package com.example.antwinner_kotlin.ui.stockdetail.fragments

data class PerformanceData(
    val year: String,
    val revenue: Long,        // 매출 (단위: 원)
    val operatingProfit: Long, // 영업이익 (단위: 원) 
    val netIncome: Long,      // 순이익 (단위: 원)
    val isEstimate: Boolean = false // 예상치 여부
)

data class QuarterlyPerformanceData(
    val quarter: String,      // 예: "2024 Q1"
    val revenue: Long,
    val operatingProfit: Long,
    val netIncome: Long,
    val isEstimate: Boolean = false
)

object PerformanceSampleData {
    
    // 연간 실적 샘플 데이터 (2025년 제외 - 연간 데이터 미완성)
    fun getAnnualSampleData(): List<PerformanceData> {
        return listOf(
            PerformanceData(
                year = "2022",
                revenue = 8_500_000_000_000L,  // 8조 5천억
                operatingProfit = 1_200_000_000_000L,  // 1조 2천억
                netIncome = 950_000_000_000L,  // 9천 5백억
                isEstimate = false
            ),
            PerformanceData(
                year = "2023", 
                revenue = 9_800_000_000_000L,  // 9조 8천억
                operatingProfit = 1_800_000_000_000L,  // 1조 8천억
                netIncome = 1_450_000_000_000L, // 1조 4천 5백억
                isEstimate = false
            ),
            PerformanceData(
                year = "2024",
                revenue = 11_200_000_000_000L, // 11조 2천억
                operatingProfit = 3_100_000_000_000L,  // 3조 1천억
                netIncome = 2_800_000_000_000L, // 2조 8천억
                isEstimate = false
            )
            // 2025년 제외: 연간 데이터가 아직 완성되지 않음
        )
    }
    
    // 분기별 실적 샘플 데이터 (6개 분기, 음수 값 포함)
    fun getQuarterlySampleData(): List<QuarterlyPerformanceData> {
        return listOf(
            QuarterlyPerformanceData(
                quarter = "23.9",  // 2023년 9월 (Q3)
                revenue = 2_400_000_000_000L,
                operatingProfit = 350_000_000_000L,
                netIncome = 280_000_000_000L,
                isEstimate = false
            ),
            QuarterlyPerformanceData(
                quarter = "23.12", // 2023년 12월 (Q4)
                revenue = 2_600_000_000_000L,
                operatingProfit = 420_000_000_000L,
                netIncome = 350_000_000_000L,
                isEstimate = false
            ),
            QuarterlyPerformanceData(
                quarter = "24.3",  // 2024년 3월 (Q1)
                revenue = 2_800_000_000_000L,
                operatingProfit = -165_000_000_000L,  // 음수 (실제 API 데이터와 동일)
                netIncome = -220_000_000_000L,       // 음수 (실제 API 데이터와 동일)
                isEstimate = false
            ),
            QuarterlyPerformanceData(
                quarter = "24.6",  // 2024년 6월 (Q2)
                revenue = 2_900_000_000_000L,
                operatingProfit = -180_000_000_000L,
                netIncome = -250_000_000_000L,
                isEstimate = false
            ),
            QuarterlyPerformanceData(
                quarter = "24.9",  // 2024년 9월 (Q3)
                revenue = 2_750_000_000_000L,
                operatingProfit = -120_000_000_000L,
                netIncome = -190_000_000_000L,
                isEstimate = false
            ),
            QuarterlyPerformanceData(
                quarter = "24.12", // 2024년 12월 (Q4)
                revenue = 3_200_000_000_000L,  // 32억 (실제 API 데이터와 유사)
                operatingProfit = -165_000_000_000L,
                netIncome = -220_000_000_000L,
                isEstimate = false
            )
        )
    }
}

// 실적 API 서비스 인터페이스
interface PerformanceApiService {
    @retrofit2.http.GET("api/performance/{stockName}")
    suspend fun getPerformanceData(@retrofit2.http.Path("stockName") stockName: String): retrofit2.Response<PerformanceApiResponse>
}

// API 응답 데이터 클래스
data class PerformanceApiResponse(
    val annual: List<PerformanceData>?,
    val quarterly: List<QuarterlyPerformanceData>?
)

// 단위 변환 유틸리티
object PerformanceUtils {
    
    // 원 단위를 조/억 단위로 변환 (하단 요약용)
    fun formatToTrillionWon(amount: Long): String {
        val trillion = amount / 1_000_000_000_000.0
        return when {
            trillion >= 1.0 -> String.format("%.1f조", trillion)
            else -> {
                val billion = amount / 100_000_000.0
                String.format("%.0f억", billion)
            }
        }
    }
    
    // 하단 요약 정보에 연도 추가 (가독성 개선)
    fun formatSummaryWithYear(amount: Long, year: String): String {
        val formatted = formatToTrillionWon(amount)
        return formatted  // 메인 텍스트만 반환
    }
    
    // 연도 정보 (부제목용)
    fun formatYearSubtitle(year: String): String {
        return "($year)"
    }
    
    // 차트용 값 변환 (조 단위)
    fun toTrillionUnit(amount: Long): Float {
        return (amount / 1_000_000_000_000.0).toFloat()
    }
    
    // 억 단위로 변환
    fun toBillionUnit(amount: Long): Float {
        return (amount / 100_000_000.0).toFloat()
    }
    
    // 차트 클릭 시 표시할 값 포맷팅
    fun formatChartValue(amount: Long): String {
        val trillion = amount / 1_000_000_000_000.0
        return when {
            trillion >= 1.0 -> String.format("%.2f조원", trillion)
            else -> {
                val billion = amount / 100_000_000.0
                String.format("%.0f억원", billion)
            }
        }
    }
} 