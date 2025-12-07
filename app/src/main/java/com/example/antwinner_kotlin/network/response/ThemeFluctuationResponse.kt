package com.example.antwinner_kotlin.network.response

import com.example.antwinner_kotlin.model.ThemeItem
import com.google.gson.annotations.SerializedName

/**
 * 테마 변동률 API 응답 모델
 */
data class ThemeFluctuationResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: List<ThemeItem>
) 