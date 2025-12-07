package com.example.antwinner_kotlin.ui.home

import android.util.Log
import com.example.antwinner_kotlin.ui.home.model.TrendKeywordResponse

/**
 * API 응답 디버깅을 위한 유틸리티 클래스
 */
object TrendLogTest {
    private const val TAG = "TrendApiResponse"
    
    /**
     * API 응답 로깅
     */
    fun logResponse(responses: List<TrendKeywordResponse>) {
        Log.d(TAG, "API 응답 수: ${responses.size}")
        
        responses.forEachIndexed { index, response ->
            Log.d(TAG, "[$index] 날짜: ${response.date}, 키워드 수: ${response.keywords.size}")
            response.keywords.forEachIndexed { kidx, keyword ->
                Log.d(TAG, "  [$kidx] ${keyword.keyword}: ${keyword.count}종목")
            }
        }
    }
} 