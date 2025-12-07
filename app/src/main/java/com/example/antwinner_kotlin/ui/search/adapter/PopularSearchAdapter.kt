package com.example.antwinner_kotlin.ui.search.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R

class PopularSearchAdapter(
    private var items: List<PopularSearchItem> = emptyList()
) : RecyclerView.Adapter<PopularSearchAdapter.ViewHolder>() {
    
    // 클릭 리스너 콜백
    private var onItemClickListener: ((PopularSearchItem) -> Unit)? = null
    
    // 클릭 리스너 설정 메서드
    fun setOnItemClickListener(listener: (PopularSearchItem) -> Unit) {
        onItemClickListener = listener
    }
    
    // 데이터 업데이트 메서드
    fun updateItems(newItems: List<PopularSearchItem>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_popular_search, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount(): Int = items.size
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rankTextView: TextView = itemView.findViewById(R.id.tv_rank)
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_name)
        private val changeRateTextView: TextView = itemView.findViewById(R.id.tv_change_rate)
        
        fun bind(item: PopularSearchItem) {
            rankTextView.text = "${item.rank}"
            nameTextView.text = item.name
            
            // 등락률 설정 (상승/하락에 따라 색상 변경)
            val rateText = if (item.changeRate >= 0) "▲ ${item.changeRate}%" else "▼ ${Math.abs(item.changeRate)}%"
            changeRateTextView.text = rateText
            
            // 상승/하락에 따른 텍스트 색상 설정
            val textColor = if (item.changeRate >= 0) 
                itemView.context.getColor(R.color.price_up) 
            else 
                itemView.context.getColor(R.color.price_down)
            changeRateTextView.setTextColor(textColor)
            
            // 클릭 이벤트 설정
            itemView.setOnClickListener {
                onItemClickListener?.invoke(item)
            }
        }
    }
    
    // 인기 검색어 아이템 모델
    data class PopularSearchItem(
        val rank: Int,
        val name: String,
        val changeRate: Float
    )
} 