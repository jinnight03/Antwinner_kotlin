package com.example.antwinner_kotlin.ui.stockdetail.fragments

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R

class WhyRiseAdapter(
    private var items: List<WhyRiseItem>
) : RecyclerView.Adapter<WhyRiseAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvRiseReason: TextView = itemView.findViewById(R.id.tv_rise_reason)
        val tvRiseRate: TextView = itemView.findViewById(R.id.tv_rise_rate)
        val tvTradeInfo: TextView = itemView.findViewById(R.id.tv_trade_info)
        val tvTheme: TextView = itemView.findViewById(R.id.tv_theme)
        val layoutTheme: LinearLayout = itemView.findViewById(R.id.layout_theme)

        fun bind(item: WhyRiseItem) {
            tvDate.text = item.date
            tvRiseReason.text = item.riseReason
            tvRiseRate.text = item.riseRate
            tvTradeInfo.text = item.tradeInfo
            
            // 테마 텍스트가 비어있거나 null이면 연관 테마 전체 영역 숨기기
            if (item.theme.isNullOrBlank()) {
                layoutTheme.visibility = View.GONE
            } else {
                layoutTheme.visibility = View.VISIBLE
                tvTheme.text = item.theme
                // 밑줄 추가
                tvTheme.paintFlags = tvTheme.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_why_rise, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<WhyRiseItem>) {
        items = newItems
        notifyDataSetChanged()
    }
} 