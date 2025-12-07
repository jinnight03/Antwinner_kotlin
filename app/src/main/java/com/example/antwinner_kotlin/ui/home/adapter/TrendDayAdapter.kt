package com.example.antwinner_kotlin.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.ui.home.model.TrendDay

class TrendDayAdapter(
    private var trendDays: List<TrendDay>
) : RecyclerView.Adapter<TrendDayAdapter.DayViewHolder>() {

    private var onItemClickListener: ((TrendDay) -> Unit)? = null

    fun setOnItemClickListener(listener: (TrendDay) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trend_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val trend = trendDays[position]
        holder.bind(trend)
        
        // 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(trend)
        }
    }

    override fun getItemCount() = trendDays.size
    
    fun updateData(newTrendDays: List<TrendDay>) {
        this.trendDays = newTrendDays
        notifyDataSetChanged()
    }

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayLabel: TextView = itemView.findViewById(R.id.tv_day_label)
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.rv_themes)

        fun bind(trendDay: TrendDay) {
            // day가 0이면 "오늘", 그 외에는 "X일전"으로 표시
            dayLabel.text = if (trendDay.day == 0) "오늘" else "${trendDay.day}일전"
            
            // 가로 스크롤을 위한 LinearLayoutManager 설정
            recyclerView.layoutManager = LinearLayoutManager(
                itemView.context, 
                LinearLayoutManager.HORIZONTAL, 
                false
            )
            
            // 테마 어댑터 설정 - themes 리스트 전달
            recyclerView.adapter = TrendThemeAdapter(trendDay.themes)
        }
    }
} 