package com.example.antwinner_kotlin.ui.theme.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.model.ThemeItem
import java.text.NumberFormat
import java.util.*

class AllThemesGridAdapter(private var themes: List<ThemeItem>) :
    RecyclerView.Adapter<AllThemesGridAdapter.ThemeViewHolder>() {
    
    private var onItemClickListener: ((ThemeItem) -> Unit)? = null
    
    fun setOnItemClickListener(listener: (ThemeItem) -> Unit) {
        onItemClickListener = listener
    }
    
    fun updateData(newThemes: List<ThemeItem>) {
        themes = newThemes
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme_grid, parent, false)
        return ThemeViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        holder.bind(themes[position])
    }
    
    override fun getItemCount(): Int = themes.size
    
    inner class ThemeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.card_view)
        private val themeName: TextView = itemView.findViewById(R.id.tv_theme_name)
        private val changeRate: TextView = itemView.findViewById(R.id.tv_change_rate)
        private val expandButton: ImageView = itemView.findViewById(R.id.iv_expand)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(themes[position])
                }
            }
            
            expandButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(themes[position])
                }
            }
        }
        
        fun bind(theme: ThemeItem) {
            themeName.text = theme.name
            
            // 변화율 처리
            val rate = theme.rate.toDoubleOrNull() ?: 0.0
            val formattedRate = if (rate > 0) {
                "+${formatChangeRate(rate)}"
            } else {
                formatChangeRate(rate)
            }
            
            changeRate.text = "$formattedRate%"
            
            // 색상 설정
            val textColor = when {
                rate > 0 -> Color.parseColor("#E51937") // 빨간색
                rate < 0 -> Color.parseColor("#1E88E5") // 파란색
                else -> Color.parseColor("#888888") // 회색
            }
            
            changeRate.setTextColor(textColor)
            
            // 배경색 설정 - 매우 연한 핑크색 (이미지 스타일 맞추기)
            cardView.setCardBackgroundColor(Color.parseColor("#FFF5F5"))
        }
        
        private fun formatChangeRate(rate: Double): String {
            val format = NumberFormat.getInstance(Locale.getDefault())
            format.minimumFractionDigits = 2
            format.maximumFractionDigits = 2
            return format.format(rate)
        }
    }
} 