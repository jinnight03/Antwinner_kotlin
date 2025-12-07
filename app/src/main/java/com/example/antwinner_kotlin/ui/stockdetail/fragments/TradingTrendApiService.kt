package com.example.antwinner_kotlin.ui.stockdetail.fragments

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TradingTrendApiService {
    @GET("trading_data/{stockName}")
    suspend fun getTradingData(@Path("stockName") stockName: String): Response<TradingTrendApiResponse>
} 