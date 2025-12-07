package com.example.antwinner_kotlin.ui.stockdetail.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R

class TradingTrendSimpleAdapter(
    private val data: List<TradingTrendItem>
) : RecyclerView.Adapter<TradingTrendSimpleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val tvIndividual: TextView = view.findViewById(R.id.tv_individual)
        val tvForeign: TextView = view.findViewById(R.id.tv_foreign)
        val tvInstitutional: TextView = view.findViewById(R.id.tv_institutional)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trading_trend_simple, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val context = holder.itemView.context
        
        holder.tvDate.text = item.date
        
        // 개인, 외국인, 기관 데이터만 표시
        setupValueView(holder.tvIndividual, item.investorData["개인"] ?: 0L, context)
        setupValueView(holder.tvForeign, item.investorData["외국인"] ?: 0L, context)
        setupValueView(holder.tvInstitutional, item.investorData["기관"] ?: 0L, context)
    }

    private fun setupValueView(textView: TextView, value: Long, context: android.content.Context) {
        // 값 포맷팅
        textView.text = when {
            value == 0L -> "0"
            value > 0 -> "+${value}억"
            else -> "${value}억"
        }
        
        // 배경과 텍스트 색상 설정
        when {
            value > 0 -> {
                textView.setBackgroundResource(R.drawable.bg_positive_value)
                textView.setTextColor(context.getColor(R.color.red))
            }
            value < 0 -> {
                textView.setBackgroundResource(R.drawable.bg_negative_value)
                textView.setTextColor(context.getColor(R.color.primary_blue))
            }
            else -> {
                textView.setBackgroundResource(R.drawable.bg_neutral_value)
                textView.setTextColor(context.getColor(R.color.text_secondary))
            }
        }
    }

    override fun getItemCount() = data.size
} 