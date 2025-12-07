package com.example.antwinner_kotlin.data

import com.google.gson.annotations.SerializedName

/**
 * 실시간 시세 정보 API 응답 데이터 클래스
 */
data class RealPriceResponse(
    @SerializedName("PBR") val pbr: String,
    @SerializedName("PER") val per: String,
    @SerializedName("ROA") val roa: String,
    @SerializedName("ROE") val roe: String,
    @SerializedName("거래대금") val tradingValue: String,
    @SerializedName("거래량") val volume: String,
    @SerializedName("고가") val highPrice: String,
    @SerializedName("당기순이익") val netIncome: String,
    @SerializedName("등락률") val changeRate: String,
    @SerializedName("매도총잔량") val totalSellVolume: String,
    @SerializedName("매도호가") val sellPrice: String,
    @SerializedName("매수총잔량") val totalBuyVolume: String,
    @SerializedName("매수호가") val buyPrice: String,
    @SerializedName("매출액") val sales: String,
    @SerializedName("매출액증가율") val salesGrowthRate: String,
    @SerializedName("보통주배당금") val dividend: String,
    @SerializedName("부채총계") val totalDebt: String,
    @SerializedName("상장주식수") val listedShares: String,
    @SerializedName("시가") val openPrice: String,
    @SerializedName("시가총액") val marketCap: String,
    @SerializedName("액면가") val parValue: String,
    @SerializedName("영업이익") val operatingProfit: String,
    @SerializedName("영업이익증가율") val operatingProfitGrowthRate: String,
    @SerializedName("외국인비율") val foreignRatio: String,
    @SerializedName("유보율") val reserveRatio: String,
    @SerializedName("자산총계") val totalAssets: String,
    @SerializedName("저가") val lowPrice: String,
    @SerializedName("전일거래량") val prevDayVolume: String,
    @SerializedName("전일비") val previousDayDiff: String,
    @SerializedName("종목명") val companyName: String,
    @SerializedName("주당순이익") val earningsPerShare: String,
    @SerializedName("현재가") val currentPrice: String
) 