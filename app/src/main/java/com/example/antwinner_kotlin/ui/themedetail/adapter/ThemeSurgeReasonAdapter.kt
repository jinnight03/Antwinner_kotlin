package com.example.antwinner_kotlin.ui.themedetail.adapter // 패키지 경로 확인 필요

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.model.ThemeSurgeReason

class ThemeSurgeReasonAdapter(private var reasons: List<ThemeSurgeReason>) :
    RecyclerView.Adapter<ThemeSurgeReasonAdapter.ReasonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReasonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme_surge_reason, parent, false)
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
        private val relatedStockBadgeCard: CardView = itemView.findViewById(R.id.card_related_stock)
        private val relatedStockBadgeText: TextView = itemView.findViewById(R.id.tv_related_stock_badge)
        private val themeBadgeIcon: ImageView = itemView.findViewById(R.id.iv_theme_badge_icon)
        private val themeBadgeName: TextView = itemView.findViewById(R.id.tv_theme_badge_name)
        private val reasonTitle: TextView = itemView.findViewById(R.id.tv_reason_title)
        private val reasonDate: TextView = itemView.findViewById(R.id.tv_reason_date)
        private val reasonTradingInfo: TextView = itemView.findViewById(R.id.tv_reason_trading_info)

        fun bind(reason: ThemeSurgeReason) {
            // 관련 종목 배지 설정
            if (reason.relatedStockName != null && reason.relatedStockRate != null) {
                relatedStockBadgeCard.visibility = View.VISIBLE
                val rateText = String.format("%+.2f%%", reason.relatedStockRate)
                relatedStockBadgeText.text = "${reason.relatedStockName} $rateText"
                // 등락률에 따른 색상 변경 (옵션)
                val colorRes = if (reason.relatedStockRate > 0) R.color.rising_color else if (reason.relatedStockRate < 0) R.color.falling_color else R.color.text_secondary
                relatedStockBadgeText.setTextColor(ContextCompat.getColor(itemView.context, colorRes))
                val bgColorRes = if (reason.relatedStockRate > 0) R.color.bg_rising_badge else if (reason.relatedStockRate < 0) R.color.bg_falling_badge else R.color.bg_neutral_badge
                relatedStockBadgeCard.setCardBackgroundColor(ContextCompat.getColor(itemView.context, bgColorRes))
            } else {
                relatedStockBadgeCard.visibility = View.GONE
            }

            // 테마 배지 설정
            themeBadgeName.text = reason.themeName
            // 테마 아이콘 로드 (ThemeDetailActivity의 loadThemeIcon과 유사하게 구현 필요)
            loadThemeBadgeIcon(reason.themeName)

            reasonTitle.text = reason.reasonTitle
            reasonDate.text = reason.date
            reasonTradingInfo.text = "거래량 ${reason.tradingVolume} · 거래대금 ${reason.tradingValue}"
        }

        private fun loadThemeBadgeIcon(themeName: String) {
            try {
                val encodedName = java.net.URLEncoder.encode(themeName, "UTF-8")
                val iconUrl = "https://antwinner.com/api/image/${encodedName}.png"
                Glide.with(itemView.context)
                    .load(iconUrl)
                    .apply(RequestOptions()
                        .placeholder(R.drawable.placeholder_circle)
                        .error(R.drawable.placeholder_circle)
                        .circleCrop() // 아이콘을 원형으로 표시
                    )
                    .into(themeBadgeIcon)
            } catch (e: Exception) {
                themeBadgeIcon.setImageResource(R.drawable.placeholder_circle) // 에러 시 기본 원형 이미지
            }
        }
    }
} 