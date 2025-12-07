package com.example.antwinner_kotlin.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.ui.home.model.MarketTicker

class MarketTickerAdapter(private val tickers: List<MarketTicker>) : 
    RecyclerView.Adapter<MarketTickerAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.tv_market_name)
        val valueTextView: TextView = view.findViewById(R.id.tv_market_value)
        val changeTextView: TextView = view.findViewById(R.id.tv_market_change)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_market_ticker, parent, false)
        return ViewHolder(view)
    }
    
    override fun getItemCount() = tickers.size
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ticker = tickers[position]
        
        holder.nameTextView.text = ticker.name
        holder.valueTextView.text = ticker.value
        holder.changeTextView.text = ticker.change
        
        // 상승/하락에 따른 색상 설정
        val color = if (ticker.isUp) {
            ContextCompat.getColor(holder.itemView.context, R.color.market_up)
        } else {
            ContextCompat.getColor(holder.itemView.context, R.color.market_down)
        }
        
        holder.changeTextView.setTextColor(color)
    }
} 