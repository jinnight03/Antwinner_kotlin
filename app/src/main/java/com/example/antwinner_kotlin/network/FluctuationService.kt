package com.example.antwinner_kotlin.network

import com.example.antwinner_kotlin.ui.home.model.FluctuationResponse
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * 상승종목 API 서비스 인터페이스
 */
interface FluctuationService {
    @GET
    suspend fun getFluctuations(@Url url: String): List<FluctuationResponse>
    
    companion object {
        const val BASE_URL = "https://antwinner.com/api/"
        const val WEEKLY_URL = "fluctuations/week"
        const val MONTHLY_URL = "fluctuations/month"
        const val THREE_MONTHS_URL = "fluctuations/three_months"
        const val SIX_MONTHS_URL = "fluctuations/six_months"
    }
} 