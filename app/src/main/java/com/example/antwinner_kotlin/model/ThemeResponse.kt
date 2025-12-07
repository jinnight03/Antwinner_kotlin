package com.example.antwinner_kotlin.model

import com.google.gson.annotations.SerializedName

data class ThemeResponse(
    @SerializedName("thema")
    val name: String,
    
    @SerializedName("average_rate")
    val averageRate: String,
    
    @SerializedName("rising_ratio")
    val risingRatio: String,
    
    @SerializedName("companies")
    val companies: List<Company>
)

data class Company(
    @SerializedName("stockname")
    val name: String,
    
    @SerializedName("current_price")
    val currentPrice: String,
    
    @SerializedName("fluctuation")
    val fluctuation: String,
    
    @SerializedName("volume")
    val volume: String,
    
    @SerializedName("stock_code")
    val stockCode: String?
) 