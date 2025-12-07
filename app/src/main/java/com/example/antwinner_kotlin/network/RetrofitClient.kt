package com.example.antwinner_kotlin.network

import com.example.antwinner_kotlin.ui.home.model.AIKeywordResponse
import com.example.antwinner_kotlin.ui.home.model.MarketIndexResponse
import com.example.antwinner_kotlin.ui.home.model.PromisingTheme
import com.example.antwinner_kotlin.ui.home.model.ThemeFluctuation
import com.example.antwinner_kotlin.ui.home.model.TrendKeywordResponse
import com.example.antwinner_kotlin.ui.news.model.NewsResponse
import com.example.antwinner_kotlin.ui.search.model.TopStockResponse
import com.example.antwinner_kotlin.ui.stocks.model.StockSearchResponse
import com.example.antwinner_kotlin.ui.stocks.model.FluctuationStock
import com.example.antwinner_kotlin.ui.stocks.model.VolumeStock
import com.example.antwinner_kotlin.ui.stocks.model.TradeAmountStock
import com.example.antwinner_kotlin.ui.stocks.model.ForeignerStock
import com.example.antwinner_kotlin.model.ThemeResponse
import com.example.antwinner_kotlin.model.KeywordCountResponse
import com.example.antwinner_kotlin.model.BracketKeywordResponse
import com.example.antwinner_kotlin.model.ThemeDetailResponse
import com.example.antwinner_kotlin.model.WeeklyRankingResponse
import com.example.antwinner_kotlin.data.model.IpoResponse
import com.example.antwinner_kotlin.network.response.ThemeFluctuationResponse
import com.example.antwinner_kotlin.network.response.ThemeAutocompleteResponse
import com.example.antwinner_kotlin.ui.search.model.LatestKeywordResponse
import com.example.antwinner_kotlin.ui.search.model.StocksResponse
import com.example.antwinner_kotlin.ui.themeschedule.IpoDetailInfo
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiService {
    @GET("api/stock_keywords")
    suspend fun getStockKeywords(): List<TrendKeywordResponse>
    
    @GET("api/market_indices")
    suspend fun getMarketIndices(): MarketIndexResponse
    
    @GET("api/average-fluctuation")
    suspend fun getThemeFluctuations(): List<ThemeFluctuation>
    
    @GET("api/average-fluctuation")
    suspend fun getThemaFluctuations(): List<ThemeFluctuation>
    
    @GET("api/average-fluctuation/{timeFrame}")
    suspend fun getThemeFluctuations(@Path("timeFrame") timeFrame: String): retrofit2.Response<ThemeFluctuationResponse>
    
    @GET("api/average_fluctuation")
    suspend fun getAverageFluctuation(): List<ThemeFluctuation>
    
    @GET("api/promising_themes")
    suspend fun getPromisingThemes(): List<PromisingTheme>
    
    @GET("api/keywords/ai_info")
    suspend fun getAIKeywords(): List<AIKeywordResponse>
    
    @GET("api/search_stock/{query}")
    suspend fun searchStock(@Path("query") query: String): List<StockSearchResponse>
    
    // 새로운 API 엔드포인트 추가
    @GET("api/top_stocks/{keyword}")
    suspend fun getTopStocks(@Path("keyword") keyword: String): TopStockResponse
    
    @GET("api/autocomplete/{query}")
    suspend fun getAutocomplete(@Path("query") query: String): List<StockSearchResponse>
    
    @GET("api/news_og/title/{keyword}")
    suspend fun getNewsForKeyword(@Path("keyword") keyword: String): List<NewsResponse>
    
    // Stock Repository에서 사용하는 메서드들 추가
    @GET("api/stocks/top_fluctuations")
    suspend fun getTopFluctuations(): List<FluctuationStock>
    
    @GET("api/stocks/top_volume")
    suspend fun getTopVolume(): List<VolumeStock>
    
    @GET("api/stocks/top_trade_amount")
    suspend fun getTopTradeAmount(): List<TradeAmountStock>
    
    @GET("api/stocks/top_foreigners")
    suspend fun getTopForeigners(): List<ForeignerStock>
    
    // 테마 관련 메서드 추가
    @GET("api/all-themas")
    suspend fun getAllThemes(): List<ThemeResponse>
    
    @GET("api/thema/{themeId}")
    suspend fun getThemeDetail(@Path("themeId") themeId: String): ThemeResponse
    
    // ThemeDetailActivity에서 사용하는 메서드들 추가
    @GET("api/all-themas/{themeName}")
    suspend fun getThemaRate(@Path("themeName") themeName: String): ThemeResponse
    
    @GET("api/stocks/daily-keyword-count/{keyword}")
    suspend fun getKeywordCount(@Path("keyword") keyword: String): KeywordCountResponse
    
    @GET("/api/stocks/bracket-keyword/{themeName}")
    suspend fun getBracketKeywords(@Path("themeName") themeName: String): List<BracketKeywordResponse>
    
    @GET("/api/thema_issue_detail/{themeName}")
    suspend fun getThemeIssueDetail(@Path("themeName") themeName: String): ThemeDetailResponse

    @GET("/api/thema_issue_ranking")
    suspend fun getWeeklyThemeRanking(@Query("days") days: Int): WeeklyRankingResponse
    
    // IPO 조회 메서드 추가
    @GET("api/ipos")
    suspend fun getIpos(): retrofit2.Response<IpoResponse>
    
    // IPO 상세 정보 조회 메서드 추가
    @GET("api/ipo_detailed_info/{companyName}")
    suspend fun getIpoDetailedInfo(@Path("companyName") companyName: String): IpoDetailInfo
    
    // Add new API endpoint for theme autocomplete
    @GET("api/thema_autocomplete/{query}")
    suspend fun getThemeAutocomplete(@Path("query") query: String): ThemeAutocompleteResponse
    
    // 추천 검색어 API
    @GET("api/latest_keywords")
    suspend fun getLatestKeywords(): List<LatestKeywordResponse>
    
    // 오늘의 이슈 종목 API
    @GET("api/stocks")
    suspend fun getIssueStocks(): StocksResponse
}

object RetrofitClient {
    const val BASE_URL = "https://antwinner.com/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
} 