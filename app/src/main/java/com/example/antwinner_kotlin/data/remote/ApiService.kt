package com.example.antwinner_kotlin.data.remote

import com.example.antwinner_kotlin.data.model.ThemeDetailResponse
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    // 테마 상세 데이터 (평균 등락률)
    @GET("/api/average-fluctuation") // 실제 엔드포인트 경로로 수정해야 합니다.
    suspend fun getThemeAverageFluctuation(): Response<ThemeDetailResponse>

    // 다른 API 호출 메소드들을 여기에 추가할 수 있습니다.

} 