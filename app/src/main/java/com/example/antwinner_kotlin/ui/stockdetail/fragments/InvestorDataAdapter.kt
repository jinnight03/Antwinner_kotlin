package com.example.antwinner_kotlin.ui.stockdetail.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R

class InvestorDataAdapter(
    private val data: List<Map<String, Long>>,
    private val investorTypes: List<String>
) : RecyclerView.Adapter<InvestorDataAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llInvestorData: LinearLayout = view.findViewById(R.id.llInvestorData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_investor_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val investorData = data[position]
        
        // 기존 뷰들 제거
        holder.llInvestorData.removeAllViews()
        
        // 각 투자자 유형별로 TextView 생성
        investorTypes.forEach { investorType ->
            val value = investorData[investorType] ?: 0L
            val textView = createInvestorValueView(holder.itemView.context, value)
            holder.llInvestorData.addView(textView)
        }
    }

    private fun createInvestorValueView(context: android.content.Context, value: Long): TextView {
        val textView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                100, // 고정 너비 (더 작게)
                56 // 고정 높이
            ).apply {
                setMargins(2, 0, 2, 0) // 여백 줄임
            }
            
            textSize = 12f // 폰트 크기 줄임
            gravity = android.view.Gravity.CENTER
            setPadding(4, 8, 4, 8)
            
            // 값 포맷팅 (더 간결하게)
            text = if (value == 0L) {
                "0"
            } else if (value >= 0) {
                "+${value}억"
            } else {
                "${value}억"
            }
            
            // 한 줄로 표시
            setSingleLine(true)
            
            // 배경색과 텍스트 색상 설정
            if (value > 0) {
                setBackgroundResource(R.drawable.bg_positive_value)
                setTextColor(context.getColor(R.color.red))
            } else if (value < 0) {
                setBackgroundResource(R.drawable.bg_negative_value)
                setTextColor(context.getColor(R.color.primary_blue))
            } else {
                setBackgroundResource(R.drawable.bg_neutral_value)
                setTextColor(context.getColor(R.color.text_secondary))
            }
        }
        
        return textView
    }

    override fun getItemCount() = data.size
} 