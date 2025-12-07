package com.example.antwinner_kotlin.api

import com.example.antwinner_kotlin.data.ChartData
import com.example.antwinner_kotlin.data.RealPriceResponse
import com.example.antwinner_kotlin.data.StockDetailResponse
import com.example.antwinner_kotlin.data.StockRiseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StockApiService {
    @GET("api/stock_people/{stockName}")
    fun getStockDetail(@Path("stockName") stockName: String): Call<List<StockDetailResponse>>
    
    @GET("api/stocks/{stockName}")
    fun getStockRiseDays(@Path("stockName") stockName: String): Call<List<StockRiseResponse>>
    
    @GET("api/chart/{stockName}")
    fun getChartData(@Path("stockName") stockName: String): Call<List<ChartData>>
    
    @GET("api/realprice")
    fun getRealPrice(@Query("company_names") companyName: String): Call<List<RealPriceResponse>>
} 