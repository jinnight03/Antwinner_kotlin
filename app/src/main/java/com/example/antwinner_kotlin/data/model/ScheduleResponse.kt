package com.example.antwinner_kotlin.data.model // 데이터 관련 클래스는 data/model 패키지로 이동하는 것을 권장

import com.google.gson.annotations.SerializedName

data class ScheduleResponse(
    @SerializedName("schedules") val schedules: List<Schedule>
)

data class Schedule(
    @SerializedName("id") val id: Int,
    @SerializedName("날자") val date: String,
    @SerializedName("내용") val content: String,
    @SerializedName("링크") val link: String,
    @SerializedName("종목명") val stockName: String,
    @SerializedName("이슈임팩트") val issueImpact: String,
    @SerializedName("투자신호") val investmentSignal: String?
) 