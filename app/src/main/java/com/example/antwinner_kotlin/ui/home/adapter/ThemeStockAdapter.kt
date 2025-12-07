package com.example.antwinner_kotlin.ui.home.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.ui.home.model.ThemeStock
import java.text.DecimalFormat

/**
 * 테마 카드 뒷면에 표시되는 종목 리스트 어댑터
 */
class ThemeStockAdapter(private var stocks: List<ThemeStock>) : 
    RecyclerView.Adapter<ThemeStockAdapter.StockViewHolder>() {
    
    private val decimalFormat = DecimalFormat("#,##0.00")
    
    // 종목 리스트 업데이트
    fun updateStocks(newStocks: List<ThemeStock>) {
        this.stocks = newStocks
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme_stock, parent, false)
        
        // 첫 번째 아이템에 추가 상단 여백 주기
        if (stocks.isNotEmpty() && viewType == 0) {
            val params = view.layoutParams as RecyclerView.LayoutParams
            params.topMargin = 8
            view.layoutParams = params
        }
        
        return StockViewHolder(view)
    }
    
    override fun getItemViewType(position: Int): Int {
        return position // 각 위치별로 다른 뷰 타입 반환
    }
    
    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(stocks[position])
    }
    
    override fun getItemCount(): Int = stocks.size
    
    inner class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stockName: TextView = itemView.findViewById(R.id.tv_stock_name)
        private val stockRate: TextView = itemView.findViewById(R.id.tv_stock_rate)
        private val stockVolume: TextView = itemView.findViewById(R.id.tv_stock_volume)
        
        fun bind(stock: ThemeStock) {
            // 종목명 설정 - 확실히 표시되도록 함
            stockName.text = stock.name
            stockName.maxLines = 1
            stockName.ellipsize = android.text.TextUtils.TruncateAt.END
            
            // "테마를 한눈에" 메뉴에서는 종목명을 흰색으로 설정
            stockName.setTextColor(Color.WHITE)
            
            // 등락률 포맷팅
            val rateText = "${if (stock.rate > 0) "+" else ""}${decimalFormat.format(stock.rate)}%"
            stockRate.text = rateText
            
            // 등락률에 따른 배경색 설정 (텍스트는 항상 흰색)
            stockRate.setTextColor(Color.WHITE)
            
            // 투명도가 있는 배경색 설정
            val backgroundColor = if (stock.rate > 0) {
                Color.parseColor("#55FF3B30") // 상승: 빨간색 배경 (반투명)
            } else if (stock.rate < 0) {
                Color.parseColor("#550000FF") // 하락: 파란색 배경 (반투명)
            } else {
                Color.parseColor("#33FFFFFF") // 변동 없음: 회색 배경 (반투명)
            }
            stockRate.setBackgroundColor(backgroundColor)
            
            // 둥근 모서리 효과를 주기 위한 drawable 설정
            val drawable = itemView.context.getDrawable(R.drawable.bg_rounded_percent)?.mutate()
            drawable?.setTint(backgroundColor)
            stockRate.background = drawable
            
            // 거래량 - "테마를 한눈에" 메뉴에서는 숨김
            stockVolume.visibility = View.GONE
        }
    }
} 