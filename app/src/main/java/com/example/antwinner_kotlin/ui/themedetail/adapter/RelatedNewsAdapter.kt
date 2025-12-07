package com.example.antwinner_kotlin.ui.themedetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.ui.news.model.NewsResponse
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.*

class RelatedNewsAdapter(
    private var newsList: List<NewsResponse>
) : RecyclerView.Adapter<RelatedNewsAdapter.NewsViewHolder>() {

    private var onNewsClickListener: ((NewsResponse) -> Unit)? = null
    private var onStockClickListener: ((String) -> Unit)? = null

    fun setOnNewsClickListener(listener: (NewsResponse) -> Unit) {
        onNewsClickListener = listener
    }

    fun setOnStockClickListener(listener: (String) -> Unit) {
        onStockClickListener = listener
    }

    fun updateNews(newNewsList: List<NewsResponse>) {
        newsList = newNewsList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news_card, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(newsList[position])
    }

    override fun getItemCount(): Int = newsList.size

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNewsDate: TextView = itemView.findViewById(R.id.tv_news_date)
        private val tvNewsTitle: TextView = itemView.findViewById(R.id.tv_news_title)
        private val layoutStockTags: LinearLayout = itemView.findViewById(R.id.layout_stock_tags)
        private val ivNewsThumbnail: ShapeableImageView = itemView.findViewById(R.id.iv_news_thumbnail)

        fun bind(news: NewsResponse) {
            // 날짜 포맷팅
            tvNewsDate.text = formatDate(news.date)
            
            // 제목 설정
            tvNewsTitle.text = news.title
            
            // 썸네일 이미지 설정 (항상 표시하되, 이미지가 없으면 기본 이미지)
            if (news.imageUrl.isNotBlank() && !news.imageUrl.contains("logos_sns.png")) {
                // 유효한 이미지 URL이 있는 경우
                Glide.with(itemView.context)
                    .load(news.imageUrl)
                    .apply(RequestOptions()
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .centerCrop()
                    )
                    .into(ivNewsThumbnail)
            } else {
                // 이미지가 없거나 기본 로고인 경우 기본 이미지 표시
                ivNewsThumbnail.setImageResource(R.drawable.ic_launcher_foreground)
            }
            
            // 종목 태그 설정 (맨 아래로 이동)
            setupStockTags(news.stockName)
            
            // 전체 영역 클릭 리스너 설정
            itemView.setOnClickListener {
                onNewsClickListener?.invoke(news)
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy. M. d", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                dateString
            }
        }

        private fun setupStockTags(stockName: String?) {
            layoutStockTags.removeAllViews()
            
            if (!stockName.isNullOrBlank()) {
                val chip = Chip(itemView.context)
                chip.text = stockName
                
                // 동그란 원형의 옅은 회색 배경 스타일
                chip.setChipBackgroundColorResource(android.R.color.transparent)
                chip.setBackgroundResource(R.drawable.bg_stock_tag_round)
                chip.setTextColor(itemView.context.getColor(R.color.text_secondary))
                chip.textSize = 12f
                chip.chipCornerRadius = 0f // drawable에서 모서리 처리
                chip.chipMinHeight = 0f
                chip.setPadding(32, 16, 32, 16) // 원형에 맞는 패딩
                chip.chipStrokeWidth = 0f
                
                // 종목 클릭 이벤트 설정
                chip.isClickable = true
                chip.isFocusable = true
                chip.setOnClickListener {
                    onStockClickListener?.invoke(stockName)
                }
                
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(0, 8, 16, 0) // 위쪽 8dp, 오른쪽 16dp 마진
                chip.layoutParams = layoutParams
                
                layoutStockTags.addView(chip)
            }
        }

    }
}
