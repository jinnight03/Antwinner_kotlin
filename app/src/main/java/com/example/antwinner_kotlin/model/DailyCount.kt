package com.example.antwinner_kotlin.model

import com.google.gson.annotations.SerializedName

data class DailyCount(
    @SerializedName("date")
    val date: String, // "yyyy-MM-dd" 형식
    
    @SerializedName("count") 
    val count: Int
) 