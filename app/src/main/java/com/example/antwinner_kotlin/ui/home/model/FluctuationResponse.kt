package com.example.antwinner_kotlin.ui.home.model

import com.google.gson.annotations.SerializedName

/**
 * 상승 종목 API 응답 데이터 모델
 */
data class FluctuationResponse(
    @SerializedName("FLUC_RT")
    val fluctuationRate: Double, // 전체 기간 등락률
    
    @SerializedName("ISU_ABBRV")
    val stockName: String, // 종목명
    
    @SerializedName("ISU_CD")
    val stockCode: String, // 종목 코드
    
    @SerializedName("Stock_Code")
    val stockImageCode: String, // 이미지 코드
    
    @SerializedName("date")
    val date: String, // 날짜
    
    @SerializedName("fluctuation_rate")
    val dailyFluctuationRate: Double, // 일간 등락률
    
    @SerializedName("period")
    val period: String, // 기간 (week, month, three_months, six_months)
    
    @SerializedName("reason_for_rise")
    val reasonForRise: String, // 상승 이유
    
    @SerializedName("recorded_date")
    val recordedDate: String, // 기록된 날짜
    
    @SerializedName("start_date")
    val startDate: String // 시작 날짜
) 