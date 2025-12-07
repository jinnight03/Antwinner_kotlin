package com.example.antwinner_kotlin.ui.stocks.model

import com.google.gson.annotations.SerializedName

/**
 * 등락률 기준 상위 종목 데이터 모델
 */
data class FluctuationStock(
    @SerializedName("등락률") val fluctuation: String,
    @SerializedName("종목명") val name: String,
    @SerializedName("종목코드") val code: String
)

/**
 * 거래량 기준 상위 종목 데이터 모델
 */
data class VolumeStock(
    @SerializedName("거래량") val volume: String,
    @SerializedName("등락률") val fluctuation: String,
    @SerializedName("종목명") val name: String,
    @SerializedName("종목코드") val code: String
)

/**
 * 거래대금 기준 상위 종목 데이터 모델
 */
data class TradeAmountStock(
    @SerializedName("거래대금") val tradeAmount: String,
    @SerializedName("등락률") val fluctuation: String,
    @SerializedName("종목명") val name: String,
    @SerializedName("종목코드") val code: String
)

/**
 * 외국인비율 기준 상위 종목 데이터 모델
 */
data class ForeignerStock(
    @SerializedName("등락률") val fluctuation: String,
    @SerializedName("외국인비율") val foreignerRatio: String,
    @SerializedName("종목명") val name: String,
    @SerializedName("종목코드") val code: String
)

/**
 * 주식 항목 공통 인터페이스 (모든 타입의 주식 데이터를 표현할 수 있도록)
 */
interface IStockItem {
    val name: String
    val code: String
    val fluctuation: String
    fun getLogoUrl(): String = "https://antwinner.com/api/stock_logos/$code"
    fun getMainValue(): String
    fun getMainValueLabel(): String
    fun hasAdditionalInfo(): Boolean // 부가 정보가 있는지 여부
    fun getAdditionalInfo(): String // 부가 정보 (거래량, 거래대금, 외국인비율 등)
}

/**
 * 등락률 기준 주식 항목 구현
 */
data class StockFluctuationItem(
    val stockData: FluctuationStock
) : IStockItem {
    override val name: String get() = stockData.name
    override val code: String get() = stockData.code
    override val fluctuation: String get() = stockData.fluctuation
    override fun getMainValue(): String = fluctuation
    override fun getMainValueLabel(): String = "등락률"
    override fun hasAdditionalInfo(): Boolean = false
    override fun getAdditionalInfo(): String = ""
}

/**
 * 거래량 기준 주식 항목 구현
 */
data class StockVolumeItem(
    val stockData: VolumeStock
) : IStockItem {
    override val name: String get() = stockData.name
    override val code: String get() = stockData.code
    override val fluctuation: String get() = stockData.fluctuation
    override fun getMainValue(): String = fluctuation
    override fun getMainValueLabel(): String = "등락률"
    override fun hasAdditionalInfo(): Boolean = true
    override fun getAdditionalInfo(): String = "거래량: ${stockData.volume}"
}

/**
 * 거래대금 기준 주식 항목 구현
 */
data class StockTradeAmountItem(
    val stockData: TradeAmountStock
) : IStockItem {
    override val name: String get() = stockData.name
    override val code: String get() = stockData.code
    override val fluctuation: String get() = stockData.fluctuation
    override fun getMainValue(): String = fluctuation
    override fun getMainValueLabel(): String = "등락률"
    override fun hasAdditionalInfo(): Boolean = true
    override fun getAdditionalInfo(): String = "거래대금: ${stockData.tradeAmount}"
}

/**
 * 외국인비율 기준 주식 항목 구현
 */
data class StockForeignerItem(
    val stockData: ForeignerStock
) : IStockItem {
    override val name: String get() = stockData.name
    override val code: String get() = stockData.code
    override val fluctuation: String get() = stockData.fluctuation
    override fun getMainValue(): String = fluctuation
    override fun getMainValueLabel(): String = "등락률"
    override fun hasAdditionalInfo(): Boolean = true
    override fun getAdditionalInfo(): String = "외국인비율: ${stockData.foreignerRatio}%"
} 