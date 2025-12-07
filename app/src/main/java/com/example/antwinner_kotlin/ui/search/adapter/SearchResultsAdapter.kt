package com.example.antwinner_kotlin.ui.search.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.ui.home.model.ThemeFluctuation
import com.example.antwinner_kotlin.ui.stocks.model.StockSearchResponse
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.util.Locale

class SearchResultsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<SearchItem>()
    private var onThemeClickListener: ((ThemeFluctuation) -> Unit)? = null
    private var onStockClickListener: ((StockSearchResponse) -> Unit)? = null
    private var onNewsClickListener: ((NewsData) -> Unit)? = null

    companion object {
        private const val TYPE_THEME_HEADER = 0
        private const val TYPE_THEME_ITEM = 1
        private const val TYPE_STOCK_HEADER = 2
        private const val TYPE_STOCK_ITEM = 3
        private const val TYPE_NEWS_HEADER = 4
        private const val TYPE_NEWS_ITEM = 5
    }

    sealed class SearchItem {
        data class ThemeHeader(val title: String = "테마") : SearchItem()
        data class ThemeItem(val theme: ThemeFluctuation) : SearchItem()
        data class StockHeader(val title: String = "주식") : SearchItem()
        data class StockItem(val stock: StockSearchResponse, val price: String = "N/A", val percentChange: Float = 0f) : SearchItem()
        data class NewsHeader(val title: String = "뉴스") : SearchItem()
        data class NewsItem(val news: NewsData) : SearchItem()
    }

    data class NewsData(
        val id: String,
        val title: String,
        val source: String,
        val date: String,
        val imageUrl: String
    )

    fun setOnThemeClickListener(listener: (ThemeFluctuation) -> Unit) {
        onThemeClickListener = listener
    }

    fun setOnStockClickListener(listener: (StockSearchResponse) -> Unit) {
        onStockClickListener = listener
    }

    fun setOnNewsClickListener(listener: (NewsData) -> Unit) {
        onNewsClickListener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(themes: List<ThemeFluctuation>, stocks: List<StockSearchResponse>, news: List<NewsData> = emptyList()) {
        try {
            Log.d("SearchResultsAdapter", "Setting data - themes: ${themes.size}, stocks: ${stocks.size}, news: ${news.size}")
            
            // 기존 아이템 저장
            val oldItems = ArrayList(items)
            
            // 모든 아이템 초기화
            items.clear()

            // 테마 섹션이 있는 경우
            if (themes.isNotEmpty()) {
                Log.d("SearchResultsAdapter", "Adding theme header and ${themes.size} theme items")
                items.add(SearchItem.ThemeHeader())
                items.addAll(themes.map { SearchItem.ThemeItem(it) })
            }

            // 주식 섹션이 있는 경우
            if (stocks.isNotEmpty()) {
                Log.d("SearchResultsAdapter", "Adding stock header and ${stocks.size} stock items")
                items.add(SearchItem.StockHeader())
                items.addAll(stocks.map { SearchItem.StockItem(it) })
            }

            // 뉴스 섹션이 있는 경우
            if (news.isNotEmpty()) {
                Log.d("SearchResultsAdapter", "Adding news header and ${news.size} news items")
                items.add(SearchItem.NewsHeader())
                items.addAll(news.map { SearchItem.NewsItem(it) })
            }

            // 데이터 변경을 통지하기 전에 기존 데이터와 새 데이터를 로그로 출력
            Log.d("SearchResultsAdapter", "Old item count: ${oldItems.size}, New item count: ${items.size}")
            
            // UI 업데이트
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("SearchResultsAdapter", "Error in setData: ${e.message}", e)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearData() {
        try {
            Log.d("SearchResultsAdapter", "Clearing data, current item count: ${items.size}")
            items.clear()
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("SearchResultsAdapter", "Error in clearData: ${e.message}", e)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SearchItem.ThemeHeader -> TYPE_THEME_HEADER
            is SearchItem.ThemeItem -> TYPE_THEME_ITEM
            is SearchItem.StockHeader -> TYPE_STOCK_HEADER
            is SearchItem.StockItem -> TYPE_STOCK_ITEM
            is SearchItem.NewsHeader -> TYPE_NEWS_HEADER
            is SearchItem.NewsItem -> TYPE_NEWS_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_THEME_HEADER, TYPE_STOCK_HEADER, TYPE_NEWS_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_search_section_header, parent, false)
                SectionHeaderViewHolder(view)
            }
            TYPE_THEME_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_theme_search_result, parent, false)
                ThemeViewHolder(view)
            }
            TYPE_STOCK_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_stock_search_result, parent, false)
                StockViewHolder(view)
            }
            TYPE_NEWS_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_search_news_section, parent, false)
                NewsViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is SearchItem.ThemeHeader -> (holder as SectionHeaderViewHolder).bind(item.title)
            is SearchItem.StockHeader -> (holder as SectionHeaderViewHolder).bind(item.title)
            is SearchItem.NewsHeader -> (holder as SectionHeaderViewHolder).bind(item.title)
            is SearchItem.ThemeItem -> (holder as ThemeViewHolder).bind(item.theme)
            is SearchItem.StockItem -> (holder as StockViewHolder).bind(item.stock, item.price, item.percentChange)
            is SearchItem.NewsItem -> (holder as NewsViewHolder).bind(item.news)
        }
    }

    override fun getItemCount() = items.size

    inner class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_section_title)

        fun bind(title: String) {
            titleTextView.text = title
        }
    }

    inner class ThemeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImageView: ImageView = itemView.findViewById(R.id.iv_theme_icon)
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_theme_name)
        private val countTextView: TextView = itemView.findViewById(R.id.tv_theme_stocks_count)
        private val rateTextView: TextView = itemView.findViewById(R.id.tv_theme_rate)

        fun bind(theme: ThemeFluctuation) {
            try {
                nameTextView.text = theme.thema
                
                // 종목 수만 표시 (이슈 건수 제거)
                val stockCount = try {
                    // transactionAmount에 종목 수가 저장되어 있음
                    theme.transactionAmount.toIntOrNull() ?: 0
                } catch (e: Exception) {
                    0
                }
                countTextView.text = "${stockCount}개 테마주"
                
                // 테마 등락률 설정
                val rate = theme.averageRate
                rateTextView.text = rate
                
                // 등락률에 따라 색상 변경
                try {
                    val rateValue = if (rate.isNotEmpty()) {
                        rate.replace("%", "").trim().toFloatOrNull() ?: 0f
                    } else {
                        0f
                    }
                    
                    rateTextView.setTextColor(if (rateValue > 0) 
                        itemView.context.getColor(android.R.color.holo_red_light)
                    else if (rateValue < 0) 
                        itemView.context.getColor(android.R.color.holo_blue_light)
                    else 
                        itemView.context.getColor(android.R.color.darker_gray))
                } catch (e: Exception) {
                    // 등락률 색상 설정 오류 시 기본 색상 사용
                    rateTextView.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                    Log.e("SearchResultsAdapter", "Error setting theme rate color: ${e.message}", e)
                }

                // 테마 이미지 로드
                try {
                    val encodedThemeName = URLEncoder.encode(theme.thema, StandardCharsets.UTF_8.toString())
                    val imageUrl = "https://antwinner.com/api/image/${encodedThemeName}.png"
                    
                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_theme_default)
                        .error(R.drawable.ic_theme_default)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(iconImageView)
                } catch (e: Exception) {
                    Log.e("SearchResultsAdapter", "Error loading theme image: ${e.message}", e)
                }

                // 클릭 리스너 설정
                itemView.setOnClickListener {
                    onThemeClickListener?.invoke(theme)
                }
            } catch (e: Exception) {
                Log.e("SearchResultsAdapter", "Error binding theme view: ${e.message}", e)
                
                // 오류 발생 시 기본값 설정
                nameTextView.text = theme.thema ?: "Unknown"
                countTextView.text = "0개 테마주"
                rateTextView.text = "0.00%"
                rateTextView.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
            }
        }
    }

    inner class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImageView: ImageView = itemView.findViewById(R.id.iv_stock_icon)
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_stock_name)
        private val priceTextView: TextView = itemView.findViewById(R.id.tv_stock_price)
        private val rateTextView: TextView = itemView.findViewById(R.id.tv_stock_rate)

        fun bind(stock: StockSearchResponse, price: String, percentChange: Float) {
            try {
                nameTextView.text = stock.name
                
                // 주식 가격 포맷팅 - API에서 받은 현재가 사용
                val formattedPrice = if (stock.price != null) {
                    "${stock.price}원"
                } else if (price != "N/A") {
                    price
                } else {
                    "0원"
                }
                priceTextView.text = formattedPrice
                
                // 등락률 설정 - API에서 받은 등락률 사용
                val stockPercentChange = if (stock.fluctuation != null && stock.fluctuation != "0.00%") {
                    stock.fluctuation
                } else if (percentChange != 0f) {
                    if (percentChange > 0) 
                        "+%.2f%%".format(percentChange)
                    else 
                        "%.2f%%".format(percentChange)
                } else {
                    "0.00%"
                }
                    
                rateTextView.text = stockPercentChange
                
                // 등락률에 따라 색상 변경
                try {
                    val percentValue = if (stockPercentChange.isNotEmpty()) {
                        stockPercentChange.replace("%", "").replace("+", "").trim().toFloatOrNull() ?: 0f
                    } else {
                        0f
                    }
                    
                    rateTextView.setTextColor(if (stockPercentChange.startsWith("+")) 
                        itemView.context.getColor(android.R.color.holo_red_light)
                    else if (percentValue < 0) 
                        itemView.context.getColor(android.R.color.holo_blue_light)
                    else 
                        itemView.context.getColor(android.R.color.darker_gray))
                } catch (e: Exception) {
                    // 등락률 색상 설정 오류 시 기본 색상 사용
                    rateTextView.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                    Log.e("SearchResultsAdapter", "Error setting rate color: ${e.message}", e)
                }

                // 주식 이미지 로드
                val imageUrl = "https://antwinner.com/api/stock_logos/${stock.code}"
                
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(iconImageView)

                // 클릭 리스너 설정
                itemView.setOnClickListener {
                    onStockClickListener?.invoke(stock)
                }
            } catch (e: Exception) {
                Log.e("SearchResultsAdapter", "Error binding stock view: ${e.message}", e)
                
                // 오류 발생 시 기본값 설정
                nameTextView.text = stock.name ?: "Unknown"
                priceTextView.text = "0원"
                rateTextView.text = "0.00%"
                rateTextView.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
            }
        }
    }

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailImageView: ImageView = itemView.findViewById(R.id.iv_news_thumbnail)
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_news_title)
        private val sourceTextView: TextView = itemView.findViewById(R.id.tv_news_source)
        private val dateTextView: TextView = itemView.findViewById(R.id.tv_news_date)

        fun bind(news: NewsData) {
            titleTextView.text = news.title
            sourceTextView.text = news.source
            dateTextView.text = news.date
            
            // 뉴스 이미지 로드
            Glide.with(itemView.context)
                .load(news.imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(thumbnailImageView)

            // 클릭 리스너 설정
            itemView.setOnClickListener {
                onNewsClickListener?.invoke(news)
            }
        }
    }
} 