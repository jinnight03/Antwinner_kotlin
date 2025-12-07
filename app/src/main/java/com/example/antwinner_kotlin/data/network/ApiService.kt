package com.example.antwinner_kotlin.data.network // 네트워크 관련 클래스는 data/network 패키지로 이동하는 것을 권장

import com.example.antwinner_kotlin.data.model.ScheduleResponse
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("/api/schedules") // 엔드포인트 경로
    suspend fun getSchedules(): Response<ScheduleResponse>
} 