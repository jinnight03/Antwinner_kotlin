package com.example.antwinner_kotlin.ui.home.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.ui.home.model.TopRisingStock

/**
 * 상승 종목 ViewPager2 어댑터
 */
class TopRisingStocksPagerAdapter(
    private val onItemClick: (TopRisingStock) -> Unit
) : RecyclerView.Adapter<TopRisingStocksPagerAdapter.ViewHolder>() {

    // 페이지별 데이터를 담는 맵
    private val pageDataMap = mutableMapOf<Int, List<TopRisingStock>>()
    
    // 페이지 타입
    companion object {
        const val PAGE_WEEKLY = 0
        const val PAGE_MONTHLY = 1
        const val PAGE_THREE_MONTHS = 2
        const val PAGE_SIX_MONTHS = 3
        
        const val PAGE_COUNT = 4
        
        // 페이지 인덱스를 기간 코드로 변환
        fun getPeriodByPageIndex(pageIndex: Int): String {
            return when (pageIndex) {
                PAGE_WEEKLY -> "1W"
                PAGE_MONTHLY -> "1M"
                PAGE_THREE_MONTHS -> "3M"
                PAGE_SIX_MONTHS -> "6M"
                else -> "1W"
            }
        }
        
        // 기간 코드를 페이지 인덱스로 변환
        fun getPageIndexByPeriod(period: String): Int {
            return when (period) {
                "1W" -> PAGE_WEEKLY
                "1M" -> PAGE_MONTHLY
                "3M" -> PAGE_THREE_MONTHS
                "6M" -> PAGE_SIX_MONTHS
                else -> PAGE_WEEKLY
            }
        }
    }

    /**
     * 특정 페이지의 데이터 업데이트
     */
    fun updatePageData(pageIndex: Int, data: List<TopRisingStock>) {
        pageDataMap[pageIndex] = data
        notifyItemChanged(pageIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.page_top_rising_stocks, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = pageDataMap[position] ?: emptyList()
        holder.bind(data)
    }

    override fun getItemCount(): Int = PAGE_COUNT

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.rv_top_rising_stocks)
        private val adapter = TopRisingStockAdapter(emptyList(), onItemClick)

        init {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = this@ViewHolder.adapter
                setHasFixedSize(true)
            }
        }

        fun bind(stocks: List<TopRisingStock>) {
            adapter.updateData(stocks)
        }
    }
} 