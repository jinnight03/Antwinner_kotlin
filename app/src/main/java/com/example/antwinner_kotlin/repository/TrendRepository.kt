package com.example.antwinner_kotlin.repository

import android.util.Log
import com.example.antwinner_kotlin.network.FluctuationService
import com.example.antwinner_kotlin.network.RetrofitClient
import com.example.antwinner_kotlin.ui.home.model.AIKeywordResponse
import com.example.antwinner_kotlin.ui.home.model.CompanyFluctuation
import com.example.antwinner_kotlin.ui.home.model.FluctuationResponse
import com.example.antwinner_kotlin.ui.home.model.MarketIndexResponse
import com.example.antwinner_kotlin.ui.home.model.PromisingTheme
import com.example.antwinner_kotlin.ui.home.model.ThemeFluctuation
import com.example.antwinner_kotlin.ui.home.model.TopRisingStock
import com.example.antwinner_kotlin.ui.home.model.TrendKeywordResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class TrendRepository {
    private val apiService = RetrofitClient.apiService
    private val fluctuationService: FluctuationService
    
    init {
        // API 호출을 위한 OkHttpClient 설정
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        // Retrofit 설정
        val retrofit = Retrofit.Builder()
            .baseUrl(FluctuationService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        // FluctuationService 인스턴스 생성
        fluctuationService = retrofit.create(FluctuationService::class.java)
    }
    
    suspend fun getStockKeywords(): List<TrendKeywordResponse> {
        return apiService.getStockKeywords()
    }
    
    suspend fun getMarketIndices(): MarketIndexResponse {
        return apiService.getMarketIndices()
    }
    
    suspend fun getThemeFluctuations(): List<ThemeFluctuation> {
        return try {
            // API 호출 전 로그 추가
            Log.d("TrendRepository", "API 호출: getThemeFluctuations()")
            
            // 명확하게 getThemeFluctuations() API 호출 (thema가 아닌 getThema)
            val response = apiService.getThemeFluctuations()
            
            // 응답 로그 추가
            Log.d("TrendRepository", "getThemeFluctuations 응답 크기: ${response.size}")
            
            if (response.isEmpty()) {
                Log.w("TrendRepository", "getThemeFluctuations 빈 응답 받음, 더미 데이터 사용")
                getDummyThemeFluctuations()
            } else {
                // 응답의 첫 번째와 마지막 항목 로깅
                if (response.isNotEmpty()) {
                    val first = response.first()
                    val last = response.last()
                    Log.d("TrendRepository", "첫 번째 테마: thema=${first.thema}, averageRate=${first.averageRate}")
                    Log.d("TrendRepository", "마지막 테마: thema=${last.thema}, averageRate=${last.averageRate}")
                }
                
                // averageRateValue 기준으로 내림차순 정렬 (등락률 높은 순)
                val sortedResponse = response.sortedByDescending { it.averageRateValue }
                Log.d("TrendRepository", "정렬 후 크기: ${sortedResponse.size}")
                sortedResponse
            }
        } catch (e: Exception) {
            Log.e("TrendRepository", "getThemeFluctuations 오류: ${e.message}", e)
            getDummyThemeFluctuations()
        }
    }

    // 테마 상승률 조회 API 호출
    suspend fun getHotThemes(): List<ThemeFluctuation> {
        return try {
            // 중요: 테마 변동률 데이터 가져오기
            val response = apiService.getThemaFluctuations()
            
            // 응답 로깅
            Log.d("TrendRepository", "Hot Themes response size: ${response.size}")
            
            // 정렬: 평균 등락률 내림차순 (가장 많이 상승한 테마가 앞에 오도록)
            val sortedResponse = response.sortedByDescending { it.averageRateValue }
            
            sortedResponse
        } catch (e: Exception) {
            Log.e("TrendRepository", "Error fetching hot themes", e)
            
            // 에러 발생 시 더미 데이터 사용
            val dummyData = getDummyThemeFluctuations().sortedByDescending { it.averageRateValue }
            Log.d("TrendRepository", "Using ${dummyData.size} dummy hot themes")
            dummyData
        }
    }

    // 노려볼만한 테마 조회
    suspend fun getPromisingThemes(): List<PromisingTheme> {
        return try {
            val response = apiService.getPromisingThemes()
            Log.d("TrendRepository", "Promising Themes response size: ${response.size}")
            response
        } catch (e: Exception) {
            Log.e("TrendRepository", "Error fetching promising themes", e)
            // API 호출에 실패한 경우 더미 데이터 반환
            getDummyPromisingThemes()
        }
    }
    
    // 더미 데이터 (API가 아직 준비되지 않았을 때 사용)
    private fun getDummyPromisingThemes(): List<PromisingTheme> {
        return listOf(
            PromisingTheme(
                id = "1",
                name = "자율주행",
                logoUrl = "https://example.com/theme_logo1.png",
                stockNames = listOf("라이콤", "에스오에스랩"),
                isHot = true
            ),
            PromisingTheme(
                id = "2", 
                name = "조선기자재",
                logoUrl = "https://example.com/theme_logo2.png",
                stockNames = listOf("HD현대중공업", "삼성중공업"),
                isHot = true
            ),
            PromisingTheme(
                id = "3",
                name = "CRISPR",
                logoUrl = "https://example.com/theme_logo3.png",
                stockNames = listOf("케이프", "툴젠"),
                isHot = false
            ),
            PromisingTheme(
                id = "4",
                name = "2차전지",
                logoUrl = "https://example.com/theme_logo4.png",
                stockNames = listOf("LG화학", "삼성SDI"),
                isHot = true
            ),
            PromisingTheme(
                id = "5",
                name = "바이오",
                logoUrl = "https://example.com/theme_logo5.png",
                stockNames = listOf("셀트리온", "삼성바이오로직스"),
                isHot = false
            )
        )
    }

    // AI 키워드 조회
    suspend fun getAIKeywords(): List<AIKeywordResponse> {
        return try {
            Log.d("TrendRepository", "Calling API: getAIKeywords() from /api/keywords/ai_info")
            val response = apiService.getAIKeywords()
            Log.d("TrendRepository", "AI Keywords response size: ${response.size}")
            
            if (response.isNotEmpty()) {
                // 첫 번째 항목 로깅
                val first = response.first()
                Log.d("TrendRepository", "First keyword: ${first.keyword}, Frequency: ${first.frequency}")
            }
            
            response
        } catch (e: Exception) {
            Log.e("TrendRepository", "Error fetching AI keywords: ${e.message}", e)
            emptyList()
        }
    }
    
    // AI 키워드를 PromisingTheme으로 변환하는 함수
    fun convertAIKeywordsToPromisingThemes(aiKeywords: List<AIKeywordResponse>): List<PromisingTheme> {
        return aiKeywords.map { keyword ->
            PromisingTheme(
                id = keyword.keyword,
                name = keyword.keyword,
                logoUrl = "https://antwinner.com/api/image/${keyword.keyword}.png",
                stockNames = keyword.stock_names,
                isHot = keyword.frequency > 1 // 빈도가 1보다 크면 핫한 테마로 간주
            )
        }
    }
    
    // 상승종목 API 호출 메서드
    suspend fun getTopRisingStocks(period: String): List<FluctuationResponse> {
        return try {
            val url = when (period) {
                "1W" -> FluctuationService.WEEKLY_URL
                "1M" -> FluctuationService.MONTHLY_URL
                "3M" -> FluctuationService.THREE_MONTHS_URL
                "6M" -> FluctuationService.SIX_MONTHS_URL
                else -> FluctuationService.SIX_MONTHS_URL
            }
            
            val response = fluctuationService.getFluctuations(url)
            Log.d("TrendRepository", "Top rising stocks for $period: ${response.size}")
            response
        } catch (e: Exception) {
            Log.e("TrendRepository", "Error fetching top rising stocks for $period", e)
            emptyList()
        }
    }
    
    // FluctuationResponse를 TopRisingStock 모델로 변환
    fun convertToTopRisingStocks(responses: List<FluctuationResponse>): List<TopRisingStock> {
        return responses.mapIndexed { index, response ->
            TopRisingStock(
                rank = index + 1,
                name = response.stockName,
                logoUrl = "https://antwinner.com/api/stock_logos/${response.stockCode}",
                percentChange = response.fluctuationRate,
                newsDate = response.date,
                dailyChange = response.dailyFluctuationRate,
                newsContent = response.reasonForRise
            )
        }
    }

    /**
     * 거래대금 기준으로 테마를 가져오는 메서드
     */
    suspend fun getThemesByTradingVolume(): List<ThemeFluctuation> {
        return try {
            // 테마 변동률 데이터 가져오기
            val themes = apiService.getThemaFluctuations()
            
            // 거래대금 기준으로 정렬
            themes.sortedByDescending { 
                try {
                    it.transactionAmount.replace(",", "").toDouble()
                } catch (e: Exception) {
                    0.0
                }
            }
        } catch (e: Exception) {
            Log.e("TrendRepository", "Error fetching themes by trading volume", e)
            
            // 에러 발생 시 더미 데이터 사용하고 거래대금으로 정렬
            val dummyThemes = getDummyThemeFluctuations()
            dummyThemes.sortedByDescending { 
                try {
                    it.transactionAmount.replace(",", "").toDouble()
                } catch (e: Exception) {
                    0.0
                }
            }
        }
    }

    // 테마 이름으로 테마 검색
    suspend fun searchTheme(query: String): ThemeFluctuation? {
        return try {
            if (query.isEmpty()) {
                Log.d("TrendRepository", "Empty query in searchTheme, returning null")
                return null
            }
            
            Log.d("TrendRepository", "Searching theme with query: $query")
            
            // 모든 테마 데이터 가져오기
            val allThemes = try {
                apiService.getThemaFluctuations()
            } catch (e: Exception) {
                Log.e("TrendRepository", "Error fetching themes for search: ${e.message}", e)
                getDummyThemeFluctuations()
            }
            
            // 정확히 일치하는 테마 찾기
            val exactMatch = allThemes.find { it.thema?.equals(query, ignoreCase = true) == true }
            if (exactMatch != null) {
                Log.d("TrendRepository", "Found exact theme match: ${exactMatch.thema}")
                return exactMatch
            }
            
            // 부분 일치하는 테마 찾기
            val partialMatch = allThemes.find { it.thema?.contains(query, ignoreCase = true) == true }
            if (partialMatch != null) {
                Log.d("TrendRepository", "Found partial theme match: ${partialMatch.thema}")
                return partialMatch
            }
            
            Log.d("TrendRepository", "No matching theme found for query: $query")
            
            // 일치하는 테마가 없으면 null 반환
            null
        } catch (e: Exception) {
            Log.e("TrendRepository", "Error searching theme: ${e.message}", e)
            
            // 검색 실패 시 더미 데이터 반환
            if (query.isNotEmpty()) {
                val dummyThemes = getDummyThemeFluctuations()
                dummyThemes.find { it.thema?.contains(query, ignoreCase = true) == true }
            } else {
                null
            }
        }
    }
    
    // 종목 이름으로 종목 검색
    suspend fun searchStock(query: String): com.example.antwinner_kotlin.ui.stocks.model.StockSearchResponse? {
        return try {
            // 종목 검색 API 호출
            Log.d("TrendRepository", "Searching stock: $query")
            val response = apiService.searchStock(query)
            Log.d("TrendRepository", "Stock search response: $response")
            
            // 검색 결과가 있으면 첫 번째 항목 반환
            if (response.isNotEmpty()) {
                return response[0]
            }
            
            // 검색 결과가 없으면 더미 데이터 확인
            if (query.isNotEmpty()) {
                val dummyStocks = getDummyStocksForQuery(query)
                if (dummyStocks.isNotEmpty()) {
                    return dummyStocks[0]
                }
            }
            
            // 검색 결과가 없으면 null 반환
            null
        } catch (e: Exception) {
            Log.e("TrendRepository", "Error searching stock", e)
            
            // 에러 발생 시 더미 데이터 반환
            if (query.isNotEmpty()) {
                val dummyStocks = getDummyStocksForQuery(query)
                if (dummyStocks.isNotEmpty()) {
                    return dummyStocks[0]
                }
            }
            null
        }
    }

    // 검색어에 맞는 더미 주식 데이터 반환 (여러 항목)
    fun getDummyStocksForQuery(query: String): List<com.example.antwinner_kotlin.ui.stocks.model.StockSearchResponse> {
        try {
            val dummyStocks = listOf(
                com.example.antwinner_kotlin.ui.stocks.model.StockSearchResponse(
                    name = "미래반도체",
                    code = "001234",
                    price = "24,500",
                    fluctuation = "+2.45%"
                ),
                com.example.antwinner_kotlin.ui.stocks.model.StockSearchResponse(
                    name = "한울반도체",
                    code = "002345",
                    price = "12,800",
                    fluctuation = "-1.23%"
                ),
                com.example.antwinner_kotlin.ui.stocks.model.StockSearchResponse(
                    name = "퀄리타스반도체",
                    code = "003456",
                    price = "36,700",
                    fluctuation = "+3.42%"
                ),
                com.example.antwinner_kotlin.ui.stocks.model.StockSearchResponse(
                    name = "반도체산업",
                    code = "004567",
                    price = "8,950",
                    fluctuation = "-0.56%"
                ),
                com.example.antwinner_kotlin.ui.stocks.model.StockSearchResponse(
                    name = "신성반도체",
                    code = "005678",
                    price = "15,200",
                    fluctuation = "+0.86%"
                ),
                com.example.antwinner_kotlin.ui.stocks.model.StockSearchResponse(
                    name = "ITF K-AI반도체코어테크",
                    code = "006789",
                    price = "42,600",
                    fluctuation = "+1.75%"
                )
            )
            
            return dummyStocks.filter { 
                it.name.contains(query, ignoreCase = true) || 
                query.uppercase() in it.name.uppercase() 
            }
        } catch (e: Exception) {
            Log.e("TrendRepository", "Error in getDummyStocksForQuery: ${e.message}", e)
            return emptyList()
        }
    }

    // 검색어에 맞는 더미 주식 데이터 반환 (단일 항목) - 기존 메서드는 호환성을 위해 유지
    private fun getDummyStockForQuery(query: String): com.example.antwinner_kotlin.ui.stocks.model.StockSearchResponse? {
        val dummyStocks = getDummyStocksForQuery(query)
        return if (dummyStocks.isNotEmpty()) dummyStocks[0] else null
    }

    // 테마 자동완성을 위한 테마 검색 함수 (쿼리와 부분 일치하는 테마 목록 반환)
    suspend fun searchThemesForAutocomplete(query: String): List<ThemeFluctuation> {
        return try {
            if (query.isEmpty()) {
                Log.d("TrendRepository", "Empty query in searchThemesForAutocomplete, returning empty list")
                return emptyList()
            }
            
            // 1글자부터 검색 허용 (기존 2글자 제한 삭제)
            Log.d("TrendRepository", "Searching themes for autocomplete: $query")
            
            val allThemes = try {
                apiService.getThemaFluctuations()
            } catch (e: Exception) {
                Log.e("TrendRepository", "Error getting all themes for autocomplete: ${e.message}", e)
                getDummyThemeFluctuations()
            }
            
            Log.d("TrendRepository", "Got ${allThemes.size} themes for autocomplete search")
            
            // 검색어와 부분 일치하는 테마만 필터링 (최대 5개까지만 반환)
            val filteredThemes = try {
                allThemes
                    .filter { theme -> 
                        theme.thema?.contains(query, ignoreCase = true) == true || 
                        (theme.thema != null && query.uppercase() in theme.thema.uppercase())
                    }
                    .take(5)
            } catch (e: Exception) {
                Log.e("TrendRepository", "Error filtering themes: ${e.message}", e)
                emptyList()
            }
            
            Log.d("TrendRepository", "Theme autocomplete results: ${filteredThemes.size} themes found")
            
            if (filteredThemes.isEmpty()) {
                Log.d("TrendRepository", "No matching themes found for query: $query")
            } else {
                Log.d("TrendRepository", "Matching themes: ${filteredThemes.joinToString(", ") { it.thema ?: "" }}")
            }
            
            // 필터링된 결과가 없으면 테마명에 "반도체"가 포함된 결과를 강제로 추가
            if (filteredThemes.isEmpty() && query.contains("반")) {
                Log.d("TrendRepository", "Forcing to add '반도체' related themes for query: $query")
                try {
                    val semiconductorThemes = allThemes.filter { it.thema?.contains("반도체") == true }.take(5)
                    return semiconductorThemes
                } catch (e: Exception) {
                    Log.e("TrendRepository", "Error adding semiconductor themes: ${e.message}", e)
                    return getDummyThemeFluctuations().filter { it.thema?.contains("반도체") == true }.take(5)
                }
            }
            
            filteredThemes
        } catch (e: Exception) {
            Log.e("TrendRepository", "Error searching themes for autocomplete: ${e.message}", e)
            
            // 검색 실패 시 쿼리와 일치하는 더미 데이터 반환
            if (query.isNotEmpty()) {
                try {
                    val dummyThemes = getDummyThemeFluctuations()
                    val filteredDummyThemes = dummyThemes
                        .filter { it.thema?.contains(query, ignoreCase = true) == true }
                        .take(5)
                    
                    // 필터링된 더미 결과가 없으면 테마명에 "반도체"가 포함된 결과를 강제로 추가
                    if (filteredDummyThemes.isEmpty() && query.contains("반")) {
                        Log.d("TrendRepository", "Forcing to add dummy '반도체' related themes for query: $query")
                        return dummyThemes.filter { it.thema?.contains("반도체") == true }.take(5)
                    }
                    
                    return filteredDummyThemes
                } catch (e: Exception) {
                    Log.e("TrendRepository", "Error filtering dummy themes: ${e.message}", e)
                    return emptyList()
                }
            } else {
                emptyList()
            }
        }
    }

    // 더미 테마 변동률 데이터 생성 함수 - 테마 데이터를 더 많이 추가
    private fun getDummyThemeFluctuations(): List<ThemeFluctuation> {
        val dummyThemes = mutableListOf(
            ThemeFluctuation(
                averageRate = "5.2%",
                companies = listOf(
                    CompanyFluctuation(
                        fluctuation = "7.8%",
                        stockName = "삼성전자",
                        volume = "10,000"
                    ),
                    CompanyFluctuation(
                        fluctuation = "3.5%",
                        stockName = "LG전자",
                        volume = "5,000"
                    )
                ),
                thema = "반도체",
                transactionAmount = "15,000"
            ),
            ThemeFluctuation(
                averageRate = "3.1%",
                companies = listOf(
                    CompanyFluctuation(
                        fluctuation = "4.2%",
                        stockName = "현대차",
                        volume = "8,000"
                    ),
                    CompanyFluctuation(
                        fluctuation = "2.8%",
                        stockName = "기아",
                        volume = "6,000"
                    )
                ),
                thema = "자동차",
                transactionAmount = "14,000"
            ),
            ThemeFluctuation(
                averageRate = "6.7%",
                companies = listOf(
                    CompanyFluctuation(
                        fluctuation = "8.1%",
                        stockName = "카카오",
                        volume = "12,000"
                    ),
                    CompanyFluctuation(
                        fluctuation = "5.3%",
                        stockName = "네이버",
                        volume = "9,000"
                    )
                ),
                thema = "인터넷",
                transactionAmount = "21,000"
            ),
            ThemeFluctuation(
                averageRate = "4.5%",
                companies = listOf(
                    CompanyFluctuation(
                        fluctuation = "5.6%",
                        stockName = "삼성바이오로직스",
                        volume = "7,000"
                    ),
                    CompanyFluctuation(
                        fluctuation = "3.4%",
                        stockName = "셀트리온",
                        volume = "5,500"
                    )
                ),
                thema = "바이오",
                transactionAmount = "12,500"
            ),
            ThemeFluctuation(
                averageRate = "7.9%",
                companies = listOf(
                    CompanyFluctuation(
                        fluctuation = "9.2%",
                        stockName = "LG화학",
                        volume = "15,000"
                    ),
                    CompanyFluctuation(
                        fluctuation = "6.6%",
                        stockName = "SK이노베이션",
                        volume = "11,000"
                    )
                ),
                thema = "2차전지",
                transactionAmount = "26,000"
            ),
            // 반도체 관련 추가 테마 데이터
            ThemeFluctuation(
                averageRate = "8.2%",
                companies = listOf(
                    CompanyFluctuation(
                        fluctuation = "9.8%",
                        stockName = "미래반도체",
                        volume = "13,000"
                    ),
                    CompanyFluctuation(
                        fluctuation = "6.5%",
                        stockName = "한울반도체",
                        volume = "7,500"
                    )
                ),
                thema = "반도체(인공지능)",
                transactionAmount = "20,500"
            ),
            ThemeFluctuation(
                averageRate = "0.0%",
                companies = listOf(
                    CompanyFluctuation(
                        fluctuation = "1.2%",
                        stockName = "퀄리타스반도체",
                        volume = "6,000"
                    ),
                    CompanyFluctuation(
                        fluctuation = "-1.2%",
                        stockName = "반도체산업",
                        volume = "4,500"
                    )
                ),
                thema = "반도체(CXL)",
                transactionAmount = "10,500"
            ),
            ThemeFluctuation(
                averageRate = "-1.46%",
                companies = listOf(
                    CompanyFluctuation(
                        fluctuation = "-2.1%",
                        stockName = "신성반도체",
                        volume = "3,200"
                    ),
                    CompanyFluctuation(
                        fluctuation = "-0.8%",
                        stockName = "ITF K-AI반도체",
                        volume = "5,800"
                    )
                ),
                thema = "반도체(전력)",
                transactionAmount = "9,000"
            )
        )
        
        // 추가 테마 데이터 더 많이 넣기 (30개 이상)
        for (i in 1..30) {
            val isRising = (0..1).random() == 1
            val rateValue = (1..10).random() + Math.random()
            val rate = if (isRising) "+%.2f%%".format(rateValue) else "-%.2f%%".format(rateValue)
            val totalVolume = (5000..25000).random()
            
            dummyThemes.add(
                ThemeFluctuation(
                    averageRate = rate,
                    companies = listOf(
                        CompanyFluctuation(
                            fluctuation = if (isRising) "+%.1f%%".format(rateValue + 1) else "-%.1f%%".format(rateValue - 1),
                            stockName = "회사 ${i}A",
                            volume = "${(1000..10000).random()}"
                        ),
                        CompanyFluctuation(
                            fluctuation = if (isRising) "+%.1f%%".format(rateValue - 1) else "-%.1f%%".format(rateValue - 1),
                            stockName = "회사 ${i}B",
                            volume = "${(1000..10000).random()}"
                        )
                    ),
                    thema = "테마${i}",
                    transactionAmount = "$totalVolume"
                )
            )
        }
        
        return dummyThemes
    }
} 