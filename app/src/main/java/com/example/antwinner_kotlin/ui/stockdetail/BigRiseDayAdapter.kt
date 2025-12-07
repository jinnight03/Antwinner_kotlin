package com.example.antwinner_kotlin.ui.stockdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import timber.log.Timber
import java.net.URLEncoder

/**
 * 크게 상승한 날 목록을 위한 RecyclerView 어댑터
 */
class BigRiseDayAdapter(
    private val items: List<BigRiseDayItem>
) : RecyclerView.Adapter<BigRiseDayAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_big_rise_day, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 날짜 카드 구성요소
        private val tvMonth: TextView = itemView.findViewById(R.id.tv_month)
        private val tvDay: TextView = itemView.findViewById(R.id.tv_day)
        private val tvYear: TextView = itemView.findViewById(R.id.tv_year)
        
        // 상승률 및 테마
        private val tvChangeRate: TextView = itemView.findViewById(R.id.tv_item_change_rate)
        private val progressRiseRate: ProgressBar = itemView.findViewById(R.id.progress_rise_rate)
        private val chipTheme: Chip = itemView.findViewById(R.id.chip_theme)
        
        // 이벤트 설명 및 거래 정보
        private val tvEventDescription: TextView = itemView.findViewById(R.id.tv_event_description)
        private val tvTradeAmount: TextView = itemView.findViewById(R.id.tv_trade_amount)
        private val tvTradeVolume: TextView = itemView.findViewById(R.id.tv_trade_volume)

        fun bind(item: BigRiseDayItem) {
            // 날짜 파싱 및 표시
            try {
                val dateParts = item.date.split("-")
                if (dateParts.size == 3) {
                    tvYear.text = dateParts[0]
                    tvMonth.text = "${dateParts[1]}월"
                    tvDay.text = dateParts[2].replace("^0+".toRegex(), "") // 앞의 0 제거
                }
            } catch (e: Exception) {
                Timber.e(e, "날짜 파싱 실패: ${item.date}")
                tvMonth.text = "--월"
                tvDay.text = "--"
                tvYear.text = "----"
            }
            
            // 상승률 표시
            tvChangeRate.text = item.changeRate
            
            // 상승률에 따른 색상 설정 및 프로그레스 바 진행도 설정
            val isPositive = item.changeRate.startsWith("+")
            if (isPositive) {
                tvChangeRate.setTextColor(itemView.context.getColor(R.color.price_up))
                
                // 상승률 수치를 추출하여 프로그레스 바에 설정
                try {
                    val rateString = item.changeRate.replace("[+%]".toRegex(), "")
                    val rateValue = rateString.toFloat()
                    progressRiseRate.progress = rateValue.toInt()
                } catch (e: Exception) {
                    Timber.e(e, "상승률 파싱 실패: ${item.changeRate}")
                    progressRiseRate.progress = 0
                }
            } else {
                tvChangeRate.setTextColor(itemView.context.getColor(R.color.price_down))
                progressRiseRate.progress = 0
            }
            
            // 테마 표시
            if (item.themeText.isNotEmpty()) {
                chipTheme.text = item.themeText
                chipTheme.visibility = View.VISIBLE
            } else {
                chipTheme.visibility = View.GONE
            }
            
            // 이벤트 설명
            tvEventDescription.text = item.eventDescription
            
            // 거래 정보
            tvTradeAmount.text = "거래대금: ${item.tradeAmount}"
            tvTradeVolume.text = "거래량: ${item.tradeVolume}"
        }
    }
} 