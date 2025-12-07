package com.example.antwinner_kotlin.ui.theme.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.ui.theme.model.ThemeStock

class ThemeStocksAdapter(private val stocks: List<ThemeStock>) : 
    RecyclerView.Adapter<ThemeStocksAdapter.StockViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme_stock, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(stocks[position])
    }

    override fun getItemCount(): Int = stocks.size

    inner class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_stock_name)
        private val rateTextView: TextView = itemView.findViewById(R.id.tv_stock_rate)
        private val volumeTextView: TextView = itemView.findViewById(R.id.tv_stock_volume)

        fun bind(stock: ThemeStock) {
            // 종목명 설정 - 확실히 표시되도록 함
            nameTextView.text = stock.name
            nameTextView.maxLines = 1
            nameTextView.ellipsize = android.text.TextUtils.TruncateAt.END
            nameTextView.setTextColor(Color.BLACK)
            
            // Set percent change with + sign for positive values
            val percentText = if (stock.percentChange > 0) 
                "+${stock.percentChange}%" else "${stock.percentChange}%"
            rateTextView.text = percentText
            
            // Set color based on percent change
            rateTextView.setTextColor(
                itemView.context.getColor(
                    if (stock.percentChange > 0) R.color.rising_color else R.color.falling_color
                )
            )
            
            // 거래량 설정
            volumeTextView.text = stock.price
            volumeTextView.setTextColor(Color.BLACK)
        }
    }
} 