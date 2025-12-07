package com.example.antwinner_kotlin.ui.search.model

import com.example.antwinner_kotlin.ui.stocks.model.StockSearchResponse

/**
 * 테스트용 더미 데이터 모델 제공 클래스
 */
object DummyModels {
    
    /**
     * 반도체 관련 더미 주식 데이터 반환
     */
    fun getDummySemiconductorStocks(): List<StockSearchResponse> {
        return listOf(
            StockSearchResponse(
                code = "005930",
                name = "삼성전자",
                price = "73,400",
                fluctuation = "+2.52%"
            ),
            StockSearchResponse(
                code = "000660",
                name = "SK하이닉스",
                price = "165,000",
                fluctuation = "+3.45%"
            ),
            StockSearchResponse(
                code = "042700",
                name = "한미반도체",
                price = "36,750",
                fluctuation = "-1.87%"
            ),
            StockSearchResponse(
                code = "039030",
                name = "이오테크닉스",
                price = "149,500",
                fluctuation = "+1.35%"
            )
        )
    }
    
    /**
     * 일반적인 더미 주식 데이터 반환 (검색어에 맞게)
     */
    fun getDummyGenericStocks(query: String): List<StockSearchResponse> {
        val stocks = listOf(
            StockSearchResponse(
                code = "005930",
                name = "삼성전자",
                price = "73,400",
                fluctuation = "+2.52%"
            ),
            StockSearchResponse(
                code = "000660",
                name = "SK하이닉스",
                price = "165,000",
                fluctuation = "+3.45%"
            ),
            StockSearchResponse(
                code = "035720",
                name = "카카오",
                price = "47,600",
                fluctuation = "-0.83%"
            ),
            StockSearchResponse(
                code = "035420",
                name = "NAVER",
                price = "203,500",
                fluctuation = "+0.74%"
            ),
            StockSearchResponse(
                code = "051910",
                name = "LG화학",
                price = "475,000",
                fluctuation = "+2.15%"
            ),
            StockSearchResponse(
                code = "105560",
                name = "KB금융",
                price = "65,700",
                fluctuation = "-0.46%"
            )
        )
        
        // 검색어와 일치하는 주식만 필터링
        return stocks.filter {
            it.name.contains(query, ignoreCase = true) || 
            it.code.contains(query, ignoreCase = true)
        }
    }
} 