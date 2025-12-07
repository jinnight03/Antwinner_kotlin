package com.example.antwinner_kotlin.network.response

import com.google.gson.annotations.SerializedName

data class ThemeAutocompleteResponse(
    val keyword: String,
    @SerializedName("execution_time_ms") val executionTimeMs: Double,
    val suggestions: List<ThemeSuggestion>,
    @SerializedName("suggestions_count") val suggestionsCount: Int
)

data class ThemeSuggestion(
    @SerializedName("테마명") val themeName: String,
    @SerializedName("종목수") val stockCount: Int,
    @SerializedName("average_rate") val averageRate: String? = "0.0%"
) 