package com.example.antwinner_kotlin.data.model

import com.google.gson.annotations.SerializedName

// API 응답의 최상위 구조 (List of ThemeData)
typealias ThemeDetailResponse = List<ThemeData>

data class ThemeData(
    @SerializedName("average_rate")
    val averageRate: String,

    @SerializedName("companies")
    val companies: List<CompanyData>,

    @SerializedName("rising_ratio")
    val risingRatio: String,

    @SerializedName("thema")
    val thema: String
)

data class CompanyData(
    @SerializedName("fluctuation")
    val fluctuation: String,

    @SerializedName("stockname")
    val stockName: String,

    @SerializedName("volume")
    val volume: String
) 