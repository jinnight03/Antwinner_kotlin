package com.example.antwinner_kotlin.ui.theme.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.model.ThemeItem
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout

class AllThemesAdapter(private var themes: List<ThemeItem>) : 
    RecyclerView.Adapter<AllThemesAdapter.ThemeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme, parent, false)
        return ThemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        holder.bind(themes[position])
    }

    override fun getItemCount(): Int = themes.size

    fun updateData(newThemes: List<ThemeItem>) {
        themes = newThemes
        notifyDataSetChanged()
    }

    inner class ThemeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val themeBackground: LinearLayout = itemView.findViewById(R.id.theme_background)
        private val themeName: TextView = itemView.findViewById(R.id.tv_theme_name)
        private val themePercent: TextView = itemView.findViewById(R.id.tv_theme_percent)
        private val themeVolume: TextView = itemView.findViewById(R.id.tv_theme_volume)

        fun bind(theme: ThemeItem) {
            // 테마 이름 설정
            themeName.text = theme.name
            
            // 등락률 포맷팅 및 표시
            val rateValue = theme.rate.toDoubleOrNull() ?: 0.0
            val isRising = rateValue > 0
            val rateText = if (isRising) "+${theme.rate}%" else "${theme.rate}%"
            themePercent.text = rateText
            
            // 상승/하락에 따른 배경색 설정
            val backgroundColor = if (isRising) {
                Color.parseColor("#FF3B30") // 빨간색 (상승)
            } else {
                Color.parseColor("#007AFF") // 파란색 (하락)
            }
            
            themeBackground.setBackgroundColor(backgroundColor)
            
            // 상승비율 더미 텍스트 설정
            themeVolume.text = "상승비율 ${Math.abs(rateValue) / 2}%"
        }
    }
} 