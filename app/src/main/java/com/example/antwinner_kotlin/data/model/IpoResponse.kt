package com.example.antwinner_kotlin.data.model

import com.google.gson.annotations.SerializedName

/**
 * IPO 스케줄 API 응답 모델
 */
data class IpoResponse(
    @SerializedName("ipos") val ipos: List<Ipo>
)

data class Ipo(
    @SerializedName("band") val band: String,
    @SerializedName("company") val company: String,
    @SerializedName("competition_rate") val competitionRate: String,
    @SerializedName("price") val price: String,
    @SerializedName("public_date") val publicDate: String,
    @SerializedName("underwriter") val underwriter: String
) 