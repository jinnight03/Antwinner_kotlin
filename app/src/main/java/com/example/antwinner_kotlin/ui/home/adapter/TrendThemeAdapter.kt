package com.example.antwinner_kotlin.ui.home.adapter

import android.content.Intent
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
import com.example.antwinner_kotlin.ui.home.model.TrendTheme
import com.example.antwinner_kotlin.ui.themedetail.ThemeDetailActivity
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class TrendThemeAdapter(private val themes: List<TrendTheme>) : 
    RecyclerView.Adapter<TrendThemeAdapter.ViewHolder>() {

    companion object {
        private const val BASE_IMAGE_URL = "https://antwinner.com/api/image/"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trend_theme, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(themes[position])
    }

    override fun getItemCount() = themes.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.iv_theme_icon)
        private val name: TextView = itemView.findViewById(R.id.tv_theme_name)
        private val stockCount: TextView = itemView.findViewById(R.id.tv_stock_count)

        fun bind(theme: TrendTheme) {
            // 테마 이름 URL 인코딩
            val encodedThemeName = try {
                URLEncoder.encode(theme.name, StandardCharsets.UTF_8.toString())
            } catch (e: Exception) {
                theme.name // 인코딩 실패시 원래 이름 사용
            }
            
            // API에서 테마 이미지 로드
            val imageUrl = BASE_IMAGE_URL + "$encodedThemeName.png"
            
            // Glide로 이미지 로드
            Glide.with(itemView.context)
                .load(imageUrl)
                .apply(RequestOptions()
                    .placeholder(R.drawable.ic_theme_default)
                    .error(R.drawable.ic_theme_default)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .into(icon)
            
            name.text = theme.name
            
            val countText = if (theme.isPositive) "+${theme.stockCount}종목" else "${theme.stockCount}종목"
            stockCount.text = countText
            stockCount.setTextColor(
                itemView.context.getColor(
                    if (theme.isPositive) R.color.market_up else R.color.market_down
                )
            )

            // 클릭 리스너 추가
            itemView.setOnClickListener {
                val intent = ThemeDetailActivity.newIntent(itemView.context, "", theme.name)
                itemView.context.startActivity(intent)
            }
        }
    }
} 