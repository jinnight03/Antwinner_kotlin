package com.example.antwinner_kotlin.ui.search.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.network.RetrofitClient
import com.example.antwinner_kotlin.ui.search.model.LatestKeywordResponse

class RecommendedKeywordAdapter : RecyclerView.Adapter<RecommendedKeywordAdapter.KeywordViewHolder>() {
    
    private val keywords = mutableListOf<LatestKeywordResponse>()
    private var onKeywordClickListener: ((String) -> Unit)? = null
    
    fun updateKeywords(newKeywords: List<LatestKeywordResponse>) {
        keywords.clear()
        keywords.addAll(newKeywords)
        notifyDataSetChanged()
    }
    
    fun setOnKeywordClickListener(listener: (String) -> Unit) {
        onKeywordClickListener = listener
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommended_keyword, parent, false)
        return KeywordViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: KeywordViewHolder, position: Int) {
        val keyword = keywords[position]
        holder.bind(keyword)
    }
    
    override fun getItemCount() = keywords.size
    
    inner class KeywordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val keywordIcon: ImageView = itemView.findViewById(R.id.iv_keyword_icon)
        private val keywordTextView: TextView = itemView.findViewById(R.id.tv_keyword_name)
        
        fun bind(keyword: LatestKeywordResponse) {
            keywordTextView.text = keyword.keyword
            
            // API에서 테마 아이콘 로드
            val iconUrl = "${RetrofitClient.BASE_URL}api/image/${keyword.keyword}.png"
            
            // Glide를 사용하여 이미지 로드 - 원형으로 표시
            Glide.with(itemView.context)
                .load(iconUrl)
                .apply(RequestOptions.circleCropTransform()) // 원형 이미지로 변환
                .placeholder(R.drawable.ic_theme_default) // 로드 중 표시할 기본 이미지
                .error(R.drawable.ic_theme_default) // 오류 시 표시할 기본 이미지
                .into(keywordIcon)
            
            itemView.setOnClickListener {
                onKeywordClickListener?.invoke(keyword.keyword)
            }
        }
    }
} 