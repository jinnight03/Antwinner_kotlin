package com.example.antwinner_kotlin.model

import com.google.gson.annotations.SerializedName

data class KeywordCountResponse(
    @SerializedName("status")
    val status: String? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("total_count")
    val totalCount: Int? = null,
    
    @SerializedName("daily_counts")
    val dailyCounts: List<DailyCount>? = null,
    
    @SerializedName("keyword")
    val keyword: String? = null
) 