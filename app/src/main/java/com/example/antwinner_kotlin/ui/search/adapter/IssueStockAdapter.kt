package com.example.antwinner_kotlin.ui.search.adapter

import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.ui.search.model.IssueStockResponse
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class IssueStockAdapter : RecyclerView.Adapter<IssueStockAdapter.StockViewHolder>() {
    
    private val stocks = mutableListOf<IssueStockResponse>()
    private var onStockClickListener: ((IssueStockResponse) -> Unit)? = null
    private val percentFormat = DecimalFormat("0")
    
    fun updateStocks(newStocks: List<IssueStockResponse>) {
        Log.d("IssueStockAdapter", "Updating stocks: received ${newStocks.size} items")
        stocks.clear()
        stocks.addAll(newStocks)
        Log.d("IssueStockAdapter", "After update: adapter has ${stocks.size} items")
        notifyDataSetChanged()
    }
    
    fun setOnStockClickListener(listener: (IssueStockResponse) -> Unit) {
        onStockClickListener = listener
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_issue_stock, parent, false)
        return StockViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock = stocks[position]
        Log.d("IssueStockAdapter", "Binding item at position $position: ${stock.stockName}")
        holder.bind(stock)
    }
    
    override fun getItemCount(): Int {
        val count = stocks.size
        Log.d("IssueStockAdapter", "getItemCount() returning $count")
        return count
    }
    
    inner class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_stock_name)
        private val reasonTextView: TextView = itemView.findViewById(R.id.tv_stock_reason)
        private val rateTextView: TextView = itemView.findViewById(R.id.tv_stock_rate)
        private val rateBoxCardView: androidx.cardview.widget.CardView = itemView.findViewById(R.id.cv_rate_box)
        private val tradingInfoTextView: TextView = itemView.findViewById(R.id.tv_trading_info)
        private val themesTextView: TextView = itemView.findViewById(R.id.tv_stock_themes)
        private val dateYearTextView: TextView = itemView.findViewById(R.id.tv_date_year)
        private val dateMonthDayTextView: TextView = itemView.findViewById(R.id.tv_date_month_day)
        
        fun bind(stock: IssueStockResponse) {
            // 종목명
            nameTextView.text = stock.stockName
            
            // 설명/헤드라인
            reasonTextView.text = stock.riseReason
            
            // 상승률 설정 (정수로 표시)
            val rateValue = (stock.riseRate * 100).toInt()
            val rateStr = if (rateValue > 0) "+${rateValue}%" else "${rateValue}%"
            rateTextView.text = rateStr
            
            // 등락률에 따라 배경색 설정
            if (rateValue >= 0) {
                // 상승: 옅은 빨간색
                rateBoxCardView.setCardBackgroundColor(itemView.context.getColor(R.color.rising_color_light))
            } else {
                // 하락: 파란색
                rateBoxCardView.setCardBackgroundColor(itemView.context.getColor(R.color.falling_color))
            }
            
            // 거래량/거래대금 설정
            val tradingInfo = "거래량 ${stock.volume} · 거래대금 ${stock.tradingAmount}"
            tradingInfoTextView.text = tradingInfo
            
            // 날짜 설정 (월과 일 부분 강조)
            try {
                val date = parseDate(stock.date)
                dateYearTextView.text = "${date.year}년"
                
                // 월과 일 부분을 굵게 강조
                val monthDayText = "${date.monthValue}월 ${date.dayOfMonth}일"
                val spannable = SpannableString(monthDayText)
                // "월" 앞의 숫자와 "일" 앞의 숫자를 굵게 처리
                val monthStart = monthDayText.indexOf("월")
                val dayStart = monthDayText.indexOf("일")
                if (monthStart > 0) {
                    spannable.setSpan(StyleSpan(Typeface.BOLD), 0, monthStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (dayStart > monthStart) {
                    val dayNumberStart = monthStart + 1
                    spannable.setSpan(StyleSpan(Typeface.BOLD), dayNumberStart, dayStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                dateMonthDayTextView.text = spannable
            } catch (e: Exception) {
                Log.e("IssueStockAdapter", "Error parsing date: ${stock.date}", e)
                // 오류 발생 시 현재 날짜 사용
                val now = LocalDate.now()
                dateYearTextView.text = "${now.year}년"
                val monthDayText = "${now.monthValue}월 ${now.dayOfMonth}일"
                val spannable = SpannableString(monthDayText)
                val monthStart = monthDayText.indexOf("월")
                val dayStart = monthDayText.indexOf("일")
                if (monthStart > 0) {
                    spannable.setSpan(StyleSpan(Typeface.BOLD), 0, monthStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (dayStart > monthStart) {
                    val dayNumberStart = monthStart + 1
                    spannable.setSpan(StyleSpan(Typeface.BOLD), dayNumberStart, dayStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                dateMonthDayTextView.text = spannable
            }
            
            // 연관 테마 설정 (밑줄 처리)
            val themeText = if (stock.theme.isNotEmpty()) {
                val themes = stock.theme.split(",", "·").map { it.trim() }.filter { it.isNotEmpty() }
                if (themes.isNotEmpty()) {
                    val themeString = themes.joinToString(" · ")
                    val spannable = SpannableString("연관 테마 $themeString")
                    // "연관 테마 " 이후의 텍스트에 밑줄 적용
                    val startIndex = "연관 테마 ".length
                    spannable.setSpan(UnderlineSpan(), startIndex, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable
                } else {
                    ""
                }
            } else {
                ""
            }
            
            if (themeText.isNotEmpty()) {
                themesTextView.text = themeText
                themesTextView.visibility = View.VISIBLE
            } else {
                themesTextView.visibility = View.GONE
            }
            
            // 아이템 클릭 이벤트
            itemView.setOnClickListener {
                onStockClickListener?.invoke(stock)
            }
        }
        
        private fun parseDate(dateString: String): LocalDate {
            return try {
                // "yyyy-MM-dd" 형식 파싱 시도
                LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (e: DateTimeParseException) {
                try {
                    // "yyyyMMdd" 형식 파싱 시도
                    LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"))
                } catch (e2: DateTimeParseException) {
                    // 기본 ISO 형식 시도
                    LocalDate.parse(dateString)
                }
            }
        }
    }
} 