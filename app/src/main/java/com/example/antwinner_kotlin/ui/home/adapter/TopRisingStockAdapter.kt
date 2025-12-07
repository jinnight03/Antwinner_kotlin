package com.example.antwinner_kotlin.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import android.graphics.drawable.Drawable
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.ui.home.model.TopRisingStock
import de.hdodenhof.circleimageview.CircleImageView
import java.text.DecimalFormat

/**
 * 상승 종목 리스트 어댑터
 */
class TopRisingStockAdapter(
    private var stocks: List<TopRisingStock> = emptyList(),
    private val onItemClick: (TopRisingStock) -> Unit
) : RecyclerView.Adapter<TopRisingStockAdapter.ViewHolder>() {

    /**
     * 데이터 업데이트 함수
     */
    fun updateData(newStocks: List<TopRisingStock>) {
        this.stocks = newStocks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_rising_stock, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stocks[position])
    }

    override fun getItemCount(): Int = stocks.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRank: TextView = itemView.findViewById(R.id.tv_rank)
        private val ivStockLogo: CircleImageView = itemView.findViewById(R.id.iv_stock_logo)
        private val tvStockName: TextView = itemView.findViewById(R.id.tv_stock_name)
        private val tvStockPercent: TextView = itemView.findViewById(R.id.tv_stock_percent)
        private val tvNewsDate: TextView = itemView.findViewById(R.id.tv_news_date)
        private val tvDailyChange: TextView = itemView.findViewById(R.id.tv_daily_change)
        private val tvNewsContent: TextView = itemView.findViewById(R.id.tv_news_content)
        
        private val percentFormat = DecimalFormat("#,##0.00%")
        private val decimalFormat = DecimalFormat("#,##0.00")

        fun bind(stock: TopRisingStock) {
            // 순위 설정
            tvRank.text = "${stock.rank}."
            
            // 종목 로고 설정
            Glide.with(itemView.context)
                .load(stock.logoUrl)
                .placeholder(R.drawable.placeholder_circle)
                .error(R.drawable.error_circle)
                .centerCrop()
                .into(ivStockLogo)
            
            // 종목명 설정
            tvStockName.text = stock.name
            
            // 종목명 클릭 이벤트 - XML에 이미 selectableItemBackground 효과가 설정되어 있음
            tvStockName.setOnClickListener {
                val intent = com.example.antwinner_kotlin.ui.stockdetail.StockDetailActivity.newIntent(
                    itemView.context,
                    stock.name
                )
                itemView.context.startActivity(intent)
            }
            
            // 상승률 설정 (소수점 둘째자리까지)
            tvStockPercent.text = "${decimalFormat.format(stock.percentChange)}%"
            
            // 날짜 설정
            tvNewsDate.text = stock.newsDate ?: "-"
            
            // 일간 변동률 설정
            val dailyChange = stock.dailyChange ?: 0.0
            val changeSymbol = if (dailyChange >= 0) "▲" else "▼"
            val textColor = if (dailyChange >= 0) {
                itemView.context.getColor(R.color.rising_color) // 빨간색 (상승)
            } else {
                itemView.context.getColor(R.color.falling_color) // 파란색 (하락)
            }
            
            tvDailyChange.text = "$changeSymbol ${decimalFormat.format(Math.abs(dailyChange))}%"
            tvDailyChange.setTextColor(textColor)
            
            // 뉴스 내용 설정
            tvNewsContent.text = stock.newsContent ?: "내용 없음"
            
            // 아이템 전체 클릭 리스너 설정
            itemView.setOnClickListener {
                onItemClick(stock)
            }
        }
    }
} 