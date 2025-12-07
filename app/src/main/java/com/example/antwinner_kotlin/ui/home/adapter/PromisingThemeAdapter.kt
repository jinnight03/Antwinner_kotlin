package com.example.antwinner_kotlin.ui.home.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.ui.home.model.PromisingTheme
import com.google.android.material.chip.Chip

class PromisingThemeAdapter(
    private var themes: List<PromisingTheme>,
    private val onItemClick: (PromisingTheme) -> Unit
) : RecyclerView.Adapter<PromisingThemeAdapter.ViewHolder>() {
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logoImageView: ImageView = itemView.findViewById(R.id.iv_theme_logo)
        val themeNameTextView: TextView = itemView.findViewById(R.id.tv_theme_name)
        val logoText: TextView = itemView.findViewById(R.id.tv_logo_text)
        val chipStock1: Chip = itemView.findViewById(R.id.chip_stock1)
        val chipStock2: Chip = itemView.findViewById(R.id.chip_stock2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promising_theme, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val theme = themes[position]
        
        try {
            // 테마명 설정
            holder.themeNameTextView.text = theme.name
            
            // 로고 텍스트 설정 (테마명의 첫 글자, 이미지 로드 실패 시 표시)
            val firstLetter = theme.name.firstOrNull()?.uppercase() ?: "T"
            holder.logoText.text = firstLetter
            
            // 기본적으로 텍스트는 숨김
            holder.logoText.visibility = View.GONE
            
            // 테마 이미지 로드
            val imageUrl = "https://antwinner.com/api/image/${theme.name}.png"
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .apply(RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.drawable.ic_theme_default)
                    .error(R.drawable.ic_theme_default))
                .into(holder.logoImageView)
            
            // 종목명 Chip 설정
            setStockChips(holder, theme.stockNames)
            
            // 아이템 클릭 리스너 설정
            holder.itemView.setOnClickListener {
                onItemClick(theme)
            }
        } catch (e: Exception) {
            Log.e("PromisingThemeAdapter", "Error binding view holder", e)
        }
    }
    
    private fun setStockChips(holder: ViewHolder, stockNames: List<String>) {
        // 첫 번째 칩 설정
        if (stockNames.isNotEmpty()) {
            holder.chipStock1.text = stockNames[0]
            holder.chipStock1.visibility = View.VISIBLE
            
            // 종목명 클릭 시 종목 상세 화면으로 이동
            holder.chipStock1.setOnClickListener {
                val stockName = stockNames[0]
                val intent = com.example.antwinner_kotlin.ui.stockdetail.StockDetailActivity.newIntent(
                    holder.itemView.context,
                    stockName
                )
                holder.itemView.context.startActivity(intent)
            }
        } else {
            holder.chipStock1.visibility = View.GONE
        }
        
        // 두 번째 칩 설정
        if (stockNames.size > 1) {
            holder.chipStock2.text = stockNames[1]
            holder.chipStock2.visibility = View.VISIBLE
            
            // 종목명 클릭 시 종목 상세 화면으로 이동
            holder.chipStock2.setOnClickListener {
                val stockName = stockNames[1]
                val intent = com.example.antwinner_kotlin.ui.stockdetail.StockDetailActivity.newIntent(
                    holder.itemView.context,
                    stockName
                )
                holder.itemView.context.startActivity(intent)
            }
        } else {
            holder.chipStock2.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = themes.size

    // 데이터 업데이트 메서드
    fun updateData(newThemes: List<PromisingTheme>) {
        themes = newThemes
        notifyDataSetChanged()
    }
} 