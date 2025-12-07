package com.example.antwinner_kotlin.ui.themedetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.model.ThemeSurgeReason
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class ThemeSurgeReasonNewAdapter(private var reasons: List<ThemeSurgeReason>) :
    RecyclerView.Adapter<ThemeSurgeReasonNewAdapter.ReasonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReasonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme_surge_reason_new, parent, false)
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
        // 날짜 카드 구성요소
        private val tvMonth: TextView = itemView.findViewById(R.id.tv_month)
        private val tvDay: TextView = itemView.findViewById(R.id.tv_day)
        private val tvYear: TextView = itemView.findViewById(R.id.tv_year)
        
        // 배지 및 관련 뷰
        private val cardRelatedStock: CardView = itemView.findViewById(R.id.card_related_stock)
        private val tvRelatedStockName: TextView = itemView.findViewById(R.id.tv_related_stock_name)
        private val tvRelatedStockRate: TextView = itemView.findViewById(R.id.tv_related_stock_rate)
        
        // 내용
        private val tvReasonTitle: TextView = itemView.findViewById(R.id.tv_reason_title)
        private val tvTradeVolume: TextView = itemView.findViewById(R.id.tv_trade_volume)
        private val tvTradeAmount: TextView = itemView.findViewById(R.id.tv_trade_amount)
        
        // 시각화 요소
        private val progressRiseRate: ProgressBar = itemView.findViewById(R.id.progress_rise_rate)
        private val tvRateValue: TextView = itemView.findViewById(R.id.tv_rate_value)

        fun bind(reason: ThemeSurgeReason) {
            // 날짜 파싱 및 표시
            parseDateComponents(reason.date)
            
            // 관련 종목 배지 설정
            if (reason.relatedStockName != null && reason.relatedStockRate != null) {
                cardRelatedStock.visibility = View.VISIBLE
                tvRelatedStockName.text = reason.relatedStockName
                
                // 상승률에 100을 곱해서 퍼센트로 표시
                val adjustedRate = reason.relatedStockRate * 100
                val rateText = String.format("%+.2f%%", adjustedRate)
                tvRelatedStockRate.text = rateText
                
                // 등락률에 따른 색상 변경
                val rateColor = if (adjustedRate > 0) {
                    ContextCompat.getColor(itemView.context, R.color.rising_color)
                } else if (adjustedRate < 0) {
                    ContextCompat.getColor(itemView.context, R.color.falling_color)
                } else {
                    ContextCompat.getColor(itemView.context, R.color.text_secondary)
                }
                tvRelatedStockRate.setTextColor(rateColor)
                
                // 배경색 설정
                val bgColor = if (adjustedRate > 0) {
                    ContextCompat.getColor(itemView.context, R.color.bg_rising_badge)
                } else if (adjustedRate < 0) {
                    ContextCompat.getColor(itemView.context, R.color.bg_falling_badge)
                } else {
                    ContextCompat.getColor(itemView.context, R.color.bg_neutral_badge)
                }
                cardRelatedStock.setCardBackgroundColor(bgColor)
                
                // 상승률을 시각화하기 위한 프로그레스 바 설정
                try {
                    // 상승률에 100을 곱하여 적용
                    val absRate = Math.abs(adjustedRate)
                    progressRiseRate.progress = Math.min(absRate.toInt(), progressRiseRate.max)
                    tvRateValue.text = rateText
                    tvRateValue.setTextColor(rateColor)
                } catch (e: Exception) {
                    Timber.e(e, "상승률 시각화 설정 중 오류 발생")
                    progressRiseRate.progress = 0
                }
            } else {
                cardRelatedStock.visibility = View.GONE
                progressRiseRate.progress = 0
                tvRateValue.text = "0.00%"
            }
            
            // 이유 및 거래 정보 설정
            tvReasonTitle.text = reason.reasonTitle
            tvTradeVolume.text = "거래량: ${reason.tradingVolume}"
            tvTradeAmount.text = "거래대금: ${reason.tradingValue}"
        }
        
        private fun parseDateComponents(dateStr: String?) {
            if (dateStr.isNullOrEmpty()) {
                tvYear.text = "----"
                tvMonth.text = "--월"
                tvDay.text = "--"
                return
            }
            
            try {
                // 날짜 형식: "yyyy. MM. dd" 또는 "yyyy-MM-dd"
                val date = if (dateStr.contains(".")) {
                    SimpleDateFormat("yyyy. MM. dd", Locale.KOREA).parse(dateStr)
                } else {
                    SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).parse(dateStr)
                }
                
                if (date != null) {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.time = date
                    
                    val year = calendar.get(java.util.Calendar.YEAR)
                    val month = calendar.get(java.util.Calendar.MONTH) + 1
                    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                    
                    tvYear.text = year.toString()
                    tvMonth.text = "${month}월"
                    tvDay.text = day.toString()
                } else {
                    // 날짜 파싱 실패 시 기본값 표시
                    setDefaultDateDisplay()
                }
            } catch (e: Exception) {
                Timber.e(e, "날짜 파싱 실패: $dateStr")
                setDefaultDateDisplay()
            }
        }
        
        private fun setDefaultDateDisplay() {
            tvYear.text = "----"
            tvMonth.text = "--월"
            tvDay.text = "--"
        }
    }
} 