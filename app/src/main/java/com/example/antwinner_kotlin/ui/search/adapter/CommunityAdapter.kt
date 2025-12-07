package com.example.antwinner_kotlin.ui.search.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.antwinner_kotlin.R

class CommunityAdapter(
    private var items: List<CommunityItem> = emptyList()
) : RecyclerView.Adapter<CommunityAdapter.ViewHolder>() {
    
    // 클릭 리스너 콜백
    private var onItemClickListener: ((CommunityItem) -> Unit)? = null
    
    // 클릭 리스너 설정 메서드
    fun setOnItemClickListener(listener: (CommunityItem) -> Unit) {
        onItemClickListener = listener
    }
    
    // 데이터 업데이트 메서드
    fun updateItems(newItems: List<CommunityItem>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount(): Int = items.size
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rankTextView: TextView = itemView.findViewById(R.id.tv_rank)
        private val iconImageView: ImageView = itemView.findViewById(R.id.iv_community_icon)
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_community_name)
        private val tradeCountTextView: TextView = itemView.findViewById(R.id.tv_trade_count)
        
        fun bind(item: CommunityItem) {
            rankTextView.text = "${item.rank}"
            nameTextView.text = item.name
            tradeCountTextView.text = "매매인증 ${item.tradeCount}건"
            
            // Glide로 이미지 로딩
            Glide.with(itemView.context)
                .load(item.iconUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(iconImageView)
            
            // 클릭 이벤트 설정
            itemView.setOnClickListener {
                onItemClickListener?.invoke(item)
            }
        }
    }
    
    // 커뮤니티 아이템 모델
    data class CommunityItem(
        val rank: Int,
        val name: String,
        val iconUrl: String,
        val tradeCount: Int
    )
} 