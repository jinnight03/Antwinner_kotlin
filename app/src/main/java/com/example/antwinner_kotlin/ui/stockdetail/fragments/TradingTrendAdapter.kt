package com.example.antwinner_kotlin.ui.stockdetail.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.antwinner_kotlin.R

// ViewPager2용 메인 어댑터
class TradingTrendPagerAdapter(
    private val data: List<TradingTrendItem>
) : RecyclerView.Adapter<TradingTrendPagerAdapter.PageViewHolder>() {

    // 투자자 유형들을 3개씩 그룹으로 나눔
    private val investorGroups = listOf(
        listOf("개인", "외국인", "금융투자"),
        listOf("기타법인", "보험", "사모"),
        listOf("투신", "은행", "기타금융")
    )

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHeader1: TextView = view.findViewById(R.id.tvHeader1)
        val tvHeader2: TextView = view.findViewById(R.id.tvHeader2)
        val tvHeader3: TextView = view.findViewById(R.id.tvHeader3)
        val rvTradingData: RecyclerView = view.findViewById(R.id.rvTradingData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trading_trend, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val investorGroup = investorGroups[position]
        
        // 헤더 설정
        holder.tvHeader1.text = investorGroup[0]
        holder.tvHeader2.text = investorGroup[1]
        holder.tvHeader3.text = investorGroup[2]
        
        // RecyclerView 설정
        val pageAdapter = TradingTrendPageAdapter(data, investorGroup)
        holder.rvTradingData.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pageAdapter
        }
    }

    override fun getItemCount(): Int = investorGroups.size
}

// 각 페이지 내부의 데이터 어댑터
class TradingTrendPageAdapter(
    private val data: List<TradingTrendItem>,
    private val investorTypes: List<String>
) : RecyclerView.Adapter<TradingTrendPageAdapter.RowViewHolder>() {

    class RowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvInvestor1: TextView = view.findViewById(R.id.tvInvestor1)
        val tvInvestor2: TextView = view.findViewById(R.id.tvInvestor2)
        val tvInvestor3: TextView = view.findViewById(R.id.tvInvestor3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trading_trend_row, parent, false)
        return RowViewHolder(view)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        val item = data[position]
        val context = holder.itemView.context
        
        // 날짜 설정
        holder.tvDate.text = item.date
        
        // 투자자 데이터 설정
        setupValueView(holder.tvInvestor1, item.investorData[investorTypes[0]] ?: 0L, context)
        setupValueView(holder.tvInvestor2, item.investorData[investorTypes[1]] ?: 0L, context)
        setupValueView(holder.tvInvestor3, item.investorData[investorTypes[2]] ?: 0L, context)
    }

    private fun setupValueView(textView: TextView, value: Long, context: android.content.Context) {
        // 값 포맷팅
        textView.text = when {
            value == 0L -> "0"
            value > 0 -> "+${value}억"
            else -> "${value}억"
        }
        
        // 배경과 텍스트 색상 설정
        when {
            value > 0 -> {
                textView.setBackgroundResource(R.drawable.bg_positive_value)
                textView.setTextColor(context.getColor(R.color.red))
            }
            value < 0 -> {
                textView.setBackgroundResource(R.drawable.bg_negative_value)
                textView.setTextColor(context.getColor(R.color.primary_blue))
            }
            else -> {
                textView.setBackgroundResource(R.drawable.bg_neutral_value)
                textView.setTextColor(context.getColor(R.color.text_secondary))
            }
        }
    }

    override fun getItemCount() = data.size
} 