package com.example.antwinner_kotlin.repository

import android.util.Log
import com.example.antwinner_kotlin.network.RetrofitClient
import com.example.antwinner_kotlin.ui.stocks.model.FluctuationStock
import com.example.antwinner_kotlin.ui.stocks.model.ForeignerStock
import com.example.antwinner_kotlin.ui.stocks.model.IStockItem
import com.example.antwinner_kotlin.ui.stocks.model.StockFluctuationItem
import com.example.antwinner_kotlin.ui.stocks.model.StockForeignerItem
import com.example.antwinner_kotlin.ui.stocks.model.StockTradeAmountItem
import com.example.antwinner_kotlin.ui.stocks.model.StockVolumeItem
import com.example.antwinner_kotlin.ui.stocks.model.TradeAmountStock
import com.example.antwinner_kotlin.ui.stocks.model.VolumeStock

class StockRepository {
    private val apiService = RetrofitClient.apiService
    
    // 등락률 상위 종목 조회
    suspend fun getTopFluctuations(): List<IStockItem> {
        return try {
            val response = apiService.getTopFluctuations()
            Log.d("StockRepository", "Top fluctuations response size: ${response.size}")
            response.take(10).map { StockFluctuationItem(it) }
        } catch (e: Exception) {
            Log.e("StockRepository", "Error fetching top fluctuations", e)
            emptyList()
        }
    }
    
    // 거래량 상위 종목 조회
    suspend fun getTopVolume(): List<IStockItem> {
        return try {
            val response = apiService.getTopVolume()
            Log.d("StockRepository", "Top volume response size: ${response.size}")
            response.take(10).map { StockVolumeItem(it) }
        } catch (e: Exception) {
            Log.e("StockRepository", "Error fetching top volume", e)
            emptyList()
        }
    }
    
    // 거래대금 상위 종목 조회
    suspend fun getTopTradeAmount(): List<IStockItem> {
        return try {
            val response = apiService.getTopTradeAmount()
            Log.d("StockRepository", "Top trade amount response size: ${response.size}")
            response.take(10).map { StockTradeAmountItem(it) }
        } catch (e: Exception) {
            Log.e("StockRepository", "Error fetching top trade amount", e)
            emptyList()
        }
    }
    
    // 외국인비율 상위 종목 조회
    suspend fun getTopForeigners(): List<IStockItem> {
        return try {
            val response = apiService.getTopForeigners()
            Log.d("StockRepository", "Top foreigners response size: ${response.size}")
            response.take(10).map { StockForeignerItem(it) }
        } catch (e: Exception) {
            Log.e("StockRepository", "Error fetching top foreigners", e)
            emptyList()
        }
    }
    
    // 탭 인덱스에 따라 적절한 데이터 가져오기
    suspend fun getStocksByTabIndex(tabIndex: Int): List<IStockItem> {
        return when (tabIndex) {
            0 -> getTopFluctuations()
            1 -> getTopVolume()
            2 -> getTopTradeAmount()
            3 -> getTopForeigners()
            else -> emptyList()
        }
    }
    
    // 더미 데이터 생성 (API 오류 시 사용)
    fun getDummyStockData(tabIndex: Int): List<IStockItem> {
        return when (tabIndex) {
            0 -> createDummyFluctuationItems()
            1 -> createDummyVolumeItems()
            2 -> createDummyTradeAmountItems()
            3 -> createDummyForeignerItems()
            else -> emptyList()
        }
    }
    
    private fun createDummyFluctuationItems(): List<IStockItem> {
        return listOf(
            StockFluctuationItem(FluctuationStock("+30.00%", "경남스틸", "039240")),
            StockFluctuationItem(FluctuationStock("+29.98%", "아이티아이즈", "372800")),
            StockFluctuationItem(FluctuationStock("+29.96%", "포바이포", "291810"))
        )
    }
    
    private fun createDummyVolumeItems(): List<IStockItem> {
        return listOf(
            StockVolumeItem(VolumeStock("111,713,002", "+26.89%", "나우IB", "293580")),
            StockVolumeItem(VolumeStock("38,428,363", "+21.81%", "대한방직", "001070")),
            StockVolumeItem(VolumeStock("35,751,055", "+29.98%", "아이티아이즈", "372800"))
        )
    }
    
    private fun createDummyTradeAmountItems(): List<IStockItem> {
        return listOf(
            StockTradeAmountItem(TradeAmountStock("111,713,002", "+26.89%", "나우IB", "293580")),
            StockTradeAmountItem(TradeAmountStock("38,428,363", "+21.81%", "대한방직", "001070")),
            StockTradeAmountItem(TradeAmountStock("35,751,055", "+29.98%", "아이티아이즈", "372800"))
        )
    }
    
    private fun createDummyForeignerItems(): List<IStockItem> {
        return listOf(
            StockForeignerItem(ForeignerStock("+0.46%", "100.00", "LB세미콘", "061970")),
            StockForeignerItem(ForeignerStock("+2.33%", "72.97", "금강고려화학", "014130")),
            StockForeignerItem(ForeignerStock("+1.83%", "65.65", "경동인베스트", "012320"))
        )
    }
} 