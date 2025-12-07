package com.example.antwinner_kotlin.ui.themedetail.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.model.ThemeSurgeReason
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class ThemeSurgeReasonWhyRiseAdapter(private var reasons: List<ThemeSurgeReason>) :
    RecyclerView.Adapter<ThemeSurgeReasonWhyRiseAdapter.ReasonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReasonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme_surge_reason_why_rise_style, parent, false)
        return ReasonViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReasonViewHolder, position: Int) {
        holder.bind(reasons[position])
    }

    override fun getItemCount(): Int = reasons.size

    fun updateData(newReasons: List<ThemeSurgeReason>) {
        reasons = newReasons
        notifyDataSetChanged()
    }

    inner class ReasonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvRiseRate: TextView = itemView.findViewById(R.id.tv_rise_rate)
        private val tvRiseReason: TextView = itemView.findViewById(R.id.tv_rise_reason)
        private val tvTradeInfo: TextView = itemView.findViewById(R.id.tv_trade_info)
        private val tvTheme: TextView = itemView.findViewById(R.id.tv_theme)
        private val layoutTheme: LinearLayout = itemView.findViewById(R.id.layout_theme)

        fun bind(reason: ThemeSurgeReason) {
            // 날짜 파싱 및 표시
            formatDate(reason.date)
            
            // 상승률 표시
            if (reason.relatedStockRate != null) {
                // 상승률에 100을 곱해서 퍼센트로 표시
                val adjustedRate = reason.relatedStockRate * 100
                val rateText = String.format("%+.2f%%", adjustedRate)
                tvRiseRate.text = rateText
                
                // 등락률에 따른 색상 변경
                val rateColor = if (adjustedRate > 0) {
                    itemView.context.getColor(R.color.rising_color)
                } else if (adjustedRate < 0) {
                    itemView.context.getColor(R.color.falling_color)
                } else {
                    itemView.context.getColor(android.R.color.black)
                }
                tvRiseRate.setTextColor(rateColor)
                tvRiseRate.visibility = View.VISIBLE
            } else {
                tvRiseRate.visibility = View.GONE
            }
            
            // 급등 이유 텍스트 설정
            tvRiseReason.text = reason.reasonTitle ?: ""
            
            // 거래 정보 설정
            val tradeInfo = buildString {
                if (!reason.tradingVolume.isNullOrBlank() && reason.tradingVolume != "-") {
                    append("거래량 ${reason.tradingVolume}")
                }
                if (!reason.tradingValue.isNullOrBlank() && reason.tradingValue != "-") {
                    if (isNotEmpty()) append(" · ")
                    append("거래대금 ${reason.tradingValue}")
                }
            }
            
            if (tradeInfo.isNotEmpty()) {
                tvTradeInfo.text = tradeInfo
                tvTradeInfo.visibility = View.VISIBLE
            } else {
                tvTradeInfo.visibility = View.GONE
            }
            
            // 연관 테마 설정
            if (!reason.themeName.isNullOrBlank()) {
                tvTheme.text = reason.themeName
                // 밑줄 추가
                tvTheme.paintFlags = tvTheme.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                layoutTheme.visibility = View.VISIBLE
            } else {
                layoutTheme.visibility = View.GONE
            }
        }
        
        private fun formatDate(dateStr: String?) {
            if (dateStr.isNullOrEmpty()) {
                tvDate.text = ""
                return
            }
            
            try {
                // API에서 받은 날짜 형식을 파싱 (예: "2024. 12. 20" 또는 "2024-12-20")
                val inputFormats = listOf(
                    SimpleDateFormat("yyyy. MM. dd", Locale.KOREA),
                    SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                )
                
                var parsedDate: java.util.Date? = null
                for (format in inputFormats) {
                    try {
                        parsedDate = format.parse(dateStr)
                        break
                    } catch (e: Exception) {
                        // 다음 형식 시도
                    }
                }
                
                if (parsedDate != null) {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.time = parsedDate
                    
                    val year = calendar.get(java.util.Calendar.YEAR)
                    val month = calendar.get(java.util.Calendar.MONTH) + 1
                    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                    
                    tvDate.text = "${year}년\n${month}월 ${day}일"
                } else {
                    tvDate.text = dateStr
                }
            } catch (e: Exception) {
                Timber.e(e, "날짜 파싱 중 오류: $dateStr")
                tvDate.text = dateStr
            }
        }
    }
} 