package com.example.antwinner_kotlin.ui.stockdetail.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import java.text.DecimalFormat

class TradingTrendFullAdapter(
    private val data: List<TradingTrendResponse>
) : RecyclerView.Adapter<TradingTrendFullAdapter.ViewHolder>() {

    private val numberFormat = DecimalFormat("#,###")

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val tvIndividual: TextView = view.findViewById(R.id.tv_individual)
        val tvForeign: TextView = view.findViewById(R.id.tv_foreign)
        val tvSecurities: TextView = view.findViewById(R.id.tv_securities)
        val tvOtherCorporation: TextView = view.findViewById(R.id.tv_other_corporation)
        val tvOtherForeign: TextView = view.findViewById(R.id.tv_other_foreign)
        val tvInsurance: TextView = view.findViewById(R.id.tv_insurance)
        val tvPrivateEquity: TextView = view.findViewById(R.id.tv_private_equity)
        val tvPension: TextView = view.findViewById(R.id.tv_pension)
        val tvBank: TextView = view.findViewById(R.id.tv_bank)
        val tvInvestment: TextView = view.findViewById(R.id.tv_investment)
        val tvOtherFinance: TextView = view.findViewById(R.id.tv_other_finance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trading_trend_full, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val context = holder.itemView.context
        
        // 날짜 포맷팅 (2025/06/13 -> 6월 13일)
        holder.tvDate.text = formatDate(item.date)
        
        // 각 투자자 유형별 데이터 설정
        setupValueView(holder.tvIndividual, item.individual, context)
        setupValueView(holder.tvForeign, item.foreign, context)
        setupValueView(holder.tvSecurities, item.securities, context)
        setupValueView(holder.tvOtherCorporation, item.otherCorporation, context)
        setupValueView(holder.tvOtherForeign, item.otherForeign, context)
        setupValueView(holder.tvInsurance, item.insurance, context)
        setupValueView(holder.tvPrivateEquity, item.privateEquity, context)
        setupValueView(holder.tvPension, item.pension, context)
        setupValueView(holder.tvBank, item.bank, context)
        setupValueView(holder.tvInvestment, item.investment, context)
        setupValueView(holder.tvOtherFinance, item.otherFinance, context)
    }

    private fun setupValueView(textView: TextView, value: Long, context: android.content.Context) {
        // 원 단위를 억 단위로 변환 (1억 = 100,000,000원)
        val valueInEok = value / 100000000.0
        
        // 값 포맷팅
        textView.text = when {
            valueInEok == 0.0 -> "0"
            valueInEok >= 1.0 || valueInEok <= -1.0 -> {
                // 1억 이상은 정수로 표시
                val intValue = valueInEok.toInt()
                if (intValue > 0) "+${numberFormat.format(intValue)}억" else "${numberFormat.format(intValue)}억"
            }
            valueInEok > 0 -> {
                // 1억 미만은 소수점 첫째자리까지 표시
                "+${String.format("%.1f", valueInEok)}억"
            }
            else -> {
                // 음수이면서 1억 미만
                "${String.format("%.1f", valueInEok)}억"
            }
        }
        
        // 배경과 텍스트 색상 설정
        when {
            valueInEok > 0 -> {
                textView.setBackgroundResource(R.drawable.bg_positive_value)
                textView.setTextColor(context.getColor(R.color.red))
            }
            valueInEok < 0 -> {
                textView.setBackgroundResource(R.drawable.bg_negative_value)
                textView.setTextColor(context.getColor(R.color.primary_blue))
            }
            else -> {
                textView.setBackgroundResource(R.drawable.bg_neutral_value)
                textView.setTextColor(context.getColor(R.color.text_secondary))
            }
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val parts = dateString.split("/")
            if (parts.size >= 3) {
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                "${month}월 ${day}일"
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }

    override fun getItemCount() = data.size
} 