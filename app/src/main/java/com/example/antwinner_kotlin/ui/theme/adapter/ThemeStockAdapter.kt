package com.example.antwinner_kotlin.ui.theme.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.model.ThemeStock
import com.example.antwinner_kotlin.ui.theme.AllThemesActivity
import java.text.DecimalFormat

class ThemeStockAdapter(private var stocks: List<ThemeStock>) :
    RecyclerView.Adapter<ThemeStockAdapter.StockViewHolder>() {

    private var onItemClickListener: ((ThemeStock, Int) -> Unit)? = null
    private var isCurrentPriceFilter = true  // true면 현재가 표시, false면 거래대금 표시

    fun setOnItemClickListener(listener: (ThemeStock, Int) -> Unit) {
        onItemClickListener = listener
    }
    
    fun setFilter(isCurrentPrice: Boolean) {
        if (this.isCurrentPriceFilter != isCurrentPrice) {
            this.isCurrentPriceFilter = isCurrentPrice
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme_stock, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(stocks[position], position)
    }

    override fun getItemCount(): Int = stocks.size

    inner class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stockName: TextView = itemView.findViewById(R.id.tv_stock_name)
        private val stockRate: TextView = itemView.findViewById(R.id.tv_stock_rate)
        private val tradingAmount: TextView = itemView.findViewById(R.id.tv_stock_volume)
        private val priceFormat = DecimalFormat("#,###")
        private val volumeFormat = DecimalFormat("#,###")

        fun bind(stock: ThemeStock, position: Int) {
            // 종목명 설정 - 확실히 표시되도록 함
            stockName.text = stock.name
            stockName.maxLines = 1
            stockName.ellipsize = android.text.TextUtils.TruncateAt.END
            stockName.setTextColor(Color.BLACK)
            
            // 상승률 포맷팅 및 색상 설정
            stockRate.text = String.format("%.1f%%", stock.changeRate)
            if (stock.changeRate > 0) {
                stockRate.setTextColor(itemView.context.getColor(android.R.color.holo_red_light))
                stockRate.text = "+${stockRate.text}"
            } else if (stock.changeRate < 0) {
                stockRate.setTextColor(itemView.context.getColor(android.R.color.holo_blue_light))
            } else {
                stockRate.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
            }
            
            // 필터에 따라 현재가 또는 거래대금 표시
            if (isCurrentPriceFilter) {
                // 현재가 표시 (천 단위 콤마 포맷팅)
                tradingAmount.text = "${priceFormat.format(stock.price)}원"
            } else {
                // 거래대금 표시 (API 원본 형식에 맞춤)
                tradingAmount.text = formatTrading(stock.tradingAmount)
            }
            tradingAmount.setTextColor(Color.BLACK)
            
            // 클릭 리스너 설정
            itemView.setOnClickListener {
                onItemClickListener?.invoke(stock, position)
            }
        }
        
        // 거래대금 숫자를 원본 API 형식에 맞게 표시
        private fun formatTrading(amount: Long): String {
            return when {
                amount >= 1_000_000_000_000L -> {
                    // 조 단위
                    val value = amount / 1_000_000_000_000.0
                    String.format("%.1f조", value)
                }
                amount >= 100_000_000L -> {
                    // 억 단위 
                    val value = amount / 100_000_000.0
                    String.format("%.0f억", value)
                }
                amount >= 10000L -> {
                    // 만 단위
                    val value = amount / 10000.0
                    String.format("%.0f만", value)
                }
                else -> {
                    // 원 단위
                    String.format("%,d원", amount)
                }
            }
        }
    }
} 