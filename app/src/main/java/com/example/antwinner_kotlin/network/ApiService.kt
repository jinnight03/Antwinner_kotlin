package com.example.antwinner_kotlin.network

import com.example.antwinner_kotlin.network.response.ThemeFluctuationResponse
import com.example.antwinner_kotlin.ui.home.model.AIKeywordResponse
import com.example.antwinner_kotlin.ui.home.model.MarketIndexResponse
import com.example.antwinner_kotlin.ui.home.model.PromisingTheme
import com.example.antwinner_kotlin.ui.home.model.ThemeFluctuation
import com.example.antwinner_kotlin.ui.home.model.TrendKeywordResponse
import com.example.antwinner_kotlin.ui.stocks.model.FluctuationStock
import com.example.antwinner_kotlin.ui.stocks.model.ForeignerStock
import com.example.antwinner_kotlin.ui.stocks.model.TradeAmountStock
import com.example.antwinner_kotlin.ui.stocks.model.VolumeStock
import com.example.antwinner_kotlin.model.ThemeResponse
import com.example.antwinner_kotlin.model.KeywordCountResponse
import com.example.antwinner_kotlin.model.BracketKeywordResponse
import com.example.antwinner_kotlin.data.model.IpoResponse
import com.example.antwinner_kotlin.ui.stocks.model.StockSearchResponse
import com.example.antwinner_kotlin.model.ThemeDetailResponse
import com.example.antwinner_kotlin.model.WeeklyRankingResponse
import com.example.antwinner_kotlin.ui.news.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BaseApiService {
    @GET("api/stock_keywords")
    suspend fun getStockKeywords(): List<TrendKeywordResponse>
    
    @GET("api/jisu")
    suspend fun getMarketIndices(): MarketIndexResponse
    
    @GET("api/average-fluctuation")
    suspend fun getThemeFluctuations(): List<ThemeFluctuation>

    @GET("api/average-fluctuation/{timeFrame}")
    suspend fun getThemeFluctuations(@Path("timeFrame") timeFrame: String): retrofit2.Response<ThemeFluctuationResponse>
    
    @GET("api/average-fluctuation")
    suspend fun getAverageFluctuation(): List<ThemeFluctuation>
    
    @GET("api/promising-themes")
    suspend fun getPromisingThemes(): List<PromisingTheme>

    @GET("api/keywords/ai_info")
    suspend fun getAIKeywords(): List<AIKeywordResponse>
    
    @GET("api/stocks/top_fluctuations")
    suspend fun getTopFluctuations(): List<FluctuationStock>
    
    @GET("api/stocks/top_volume")
    suspend fun getTopVolume(): List<VolumeStock>
    
    @GET("api/stocks/top_trade_amount")
    suspend fun getTopTradeAmount(): List<TradeAmountStock>
    
    @GET("api/stocks/top_foreigners")
    suspend fun getTopForeigners(): List<ForeignerStock>

    @GET("api/all-themas")
    suspend fun getAllThemes(): List<ThemeResponse>
    
    @GET("api/thema/{themeId}")
    suspend fun getThemeDetail(@Path("themeId") themeId: String): ThemeResponse
    
    @GET("api/stocks/daily-keyword-count/{keyword}")
    suspend fun getKeywordCount(@Path("keyword") keyword: String): KeywordCountResponse
    
    @GET("api/all-themas/{themeName}")
    suspend fun getThemaRate(@Path("themeName") themeName: String): ThemeResponse

    @GET("/api/stocks/bracket-keyword/{themeName}")
    suspend fun getBracketKeywords(@Path("themeName") themeName: String): List<BracketKeywordResponse>

    @GET("api/ipos")
    suspend fun getIpos(): retrofit2.Response<IpoResponse>

    // 테마 검색 API
    @GET("api/top_stocks/{themeName}")
    suspend fun searchTheme(@Path("themeName") themeName: String): ThemeResponse

    // 종목 검색 API (자동완성 기능)
    @GET("api/autocomplete/{stockName}")
    suspend fun searchStock(@Path("stockName") stockName: String): List<StockSearchResponse>

    @GET("/api/thema_issue_detail/{themeName}")
    suspend fun getThemeIssueDetail(@Path("themeName") themeName: String): ThemeDetailResponse

    @GET("/api/thema_issue_ranking")
    suspend fun getWeeklyThemeRanking(@Query("days") days: Int): WeeklyRankingResponse

    // 관련 뉴스 API
    @GET("api/news_og/title/{keyword}")
    suspend fun getRelatedNews(@Path("keyword") keyword: String): List<NewsResponse>
} 