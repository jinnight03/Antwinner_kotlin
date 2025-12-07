package com.example.antwinner_kotlin.ui.stockdetail.fragments

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

/**
 * API 응답용 차트 데이터 모델
 */
data class ChartApiResponse(
    @SerializedName("Change")
    val change: Double,
    
    @SerializedName("Close")
    val close: Int,
    
    @SerializedName("High")
    val high: Int,
    
    @SerializedName("Low")
    val low: Int,
    
    @SerializedName("Open")
    val open: Int,
    
    @SerializedName("Volume")
    val volume: Int,
    
    @SerializedName("date")
    val date: String
) {
    /**
     * API 응답을 CandleData로 변환
     */
    fun toCandleData(): CandleData {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = try {
            dateFormat.parse(date) ?: Date()
        } catch (e: Exception) {
            Date()
        }
        
        return CandleData(
            date = parsedDate,
            open = open.toFloat(),
            high = high.toFloat(),
            low = low.toFloat(),
            close = close.toFloat(),
            volume = volume.toLong()
        )
    }
}

/**
 * 캔들스틱 차트용 데이터 모델
 */
data class CandleData(
    val date: Date,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Long
)

/**
 * 이동평균선 데이터
 */
data class MovingAverageData(
    val date: Date,
    val value: Float,
    val period: Int
)

/**
 * 거래량 데이터
 */
data class VolumeData(
    val date: Date,
    val volume: Long,
    val isRising: Boolean
)

/**
 * 차트 지표 계산 유틸리티
 */
object ChartIndicators {
    
    /**
     * 단순 이동평균 계산
     */
    fun calculateSMA(data: List<CandleData>, period: Int): List<MovingAverageData> {
        val result = mutableListOf<MovingAverageData>()
        
        for (i in period - 1 until data.size) {
            val sum = data.subList(i - period + 1, i + 1).sumOf { it.close.toDouble() }
            val average = (sum / period).toFloat()
            result.add(MovingAverageData(data[i].date, average, period))
        }
        
        return result
    }
    
    /**
     * 지수 이동평균 계산
     */
    fun calculateEMA(data: List<CandleData>, period: Int): List<MovingAverageData> {
        if (data.size < period) return emptyList()
        
        val result = mutableListOf<MovingAverageData>()
        val multiplier = 2.0 / (period + 1)
        
        // 첫 번째 EMA는 SMA로 시작
        val firstSMA = data.take(period).map { it.close }.average().toFloat()
        result.add(MovingAverageData(data[period - 1].date, firstSMA, period))
        
        var previousEMA = firstSMA
        
        // 나머지 EMA 계산
        for (i in period until data.size) {
            val ema = (data[i].close * multiplier + previousEMA * (1 - multiplier)).toFloat()
            result.add(MovingAverageData(data[i].date, ema, period))
            previousEMA = ema
        }
        
        return result
    }
    
    /**
     * 볼린저 밴드 계산
     */
    data class BollingerBand(
        val date: Date,
        val upper: Float,
        val middle: Float,
        val lower: Float
    )
    
    fun calculateBollingerBands(data: List<CandleData>, period: Int = 20, stdDevMultiplier: Double = 2.0): List<BollingerBand> {
        val result = mutableListOf<BollingerBand>()
        
        for (i in period - 1 until data.size) {
            val subset = data.subList(i - period + 1, i + 1)
            val mean = subset.map { it.close }.average()
            val variance = subset.map { (it.close - mean) * (it.close - mean) }.average()
            val stdDev = kotlin.math.sqrt(variance)
            
            val upper = (mean + stdDev * stdDevMultiplier).toFloat()
            val lower = (mean - stdDev * stdDevMultiplier).toFloat()
            
            result.add(BollingerBand(data[i].date, upper, mean.toFloat(), lower))
        }
        
        return result
    }
    
    /**
     * RSI 계산
     */
    fun calculateRSI(data: List<CandleData>, period: Int = 14): List<Pair<Date, Float>> {
        if (data.size < period + 1) return emptyList()
        
        val result = mutableListOf<Pair<Date, Float>>()
        
        // 첫 번째 RSI 계산을 위한 평균 gain/loss
        var avgGain = 0.0
        var avgLoss = 0.0
        
        for (i in 1..period) {
            val change = data[i].close - data[i - 1].close
            if (change > 0) {
                avgGain += change
            } else {
                avgLoss += kotlin.math.abs(change)
            }
        }
        
        avgGain /= period
        avgLoss /= period
        
        var rsi = if (avgLoss == 0.0) 100.0 else 100.0 - (100.0 / (1.0 + avgGain / avgLoss))
        result.add(Pair(data[period].date, rsi.toFloat()))
        
        // 나머지 RSI 계산
        for (i in period + 1 until data.size) {
            val change = data[i].close - data[i - 1].close
            val gain = if (change > 0) change.toDouble() else 0.0
            val loss = if (change < 0) kotlin.math.abs(change.toDouble()) else 0.0
            
            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period
            
            rsi = if (avgLoss == 0.0) 100.0 else 100.0 - (100.0 / (1.0 + avgGain / avgLoss))
            result.add(Pair(data[i].date, rsi.toFloat()))
        }
        
        return result
    }
} 