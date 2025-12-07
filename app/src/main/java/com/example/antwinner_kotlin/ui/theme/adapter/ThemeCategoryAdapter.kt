package com.example.antwinner_kotlin.ui.theme.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.model.ThemeCategory
import com.example.antwinner_kotlin.model.ThemeStock
import com.example.antwinner_kotlin.ui.theme.AllThemesActivity
import com.example.antwinner_kotlin.model.ThemeResponse
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.antwinner_kotlin.ui.themedetail.ThemeDetailActivity

class ThemeCategoryAdapter(private var categories: List<ThemeCategory>) :
    RecyclerView.Adapter<ThemeCategoryAdapter.CategoryViewHolder>() {

    private var onCategoryClickListener: ((ThemeCategory, Int) -> Unit)? = null
    private var onStockClickListener: ((ThemeStock, Int, Int) -> Unit)? = null
    private var isCurrentPriceFilter = true  // true: 현재가 필터, false: 거래대금 필터

    fun setOnCategoryClickListener(listener: (ThemeCategory, Int) -> Unit) {
        onCategoryClickListener = listener
    }

    fun setOnStockClickListener(listener: (ThemeStock, Int, Int) -> Unit) {
        onStockClickListener = listener
    }

    // 필터 타입 설정 메서드 추가
    fun setFilterType(isCurrentPrice: Boolean) {
        if (isCurrentPriceFilter != isCurrentPrice) {
            isCurrentPriceFilter = isCurrentPrice
            notifyDataSetChanged()
        }
    }

    fun updateData(newCategories: List<ThemeCategory>) {
        categories = newCategories
        notifyDataSetChanged()
    }
    
    /**
     * 특정 위치의 아이템만 업데이트합니다.
     */
    fun updateItemAt(position: Int, category: ThemeCategory) {
        if (position >= 0 && position < categories.size) {
            val mutableList = categories.toMutableList()
            mutableList[position] = category
            categories = mutableList
            notifyItemChanged(position)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int, payloads: List<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryIcon: ImageView = itemView.findViewById(R.id.iv_category_icon)
        private val categoryName: TextView = itemView.findViewById(R.id.tv_category_name)
        private val stockCount: TextView = itemView.findViewById(R.id.tv_stock_count)
        private val fluctuationRate: TextView = itemView.findViewById(R.id.tv_fluctuation_rate)
        private val stocksContainer: View = itemView.findViewById(R.id.layout_stocks_container)
        private val stocksDivider: View = itemView.findViewById(R.id.stocks_divider)
        private val stocksRecyclerView: RecyclerView = itemView.findViewById(R.id.rv_theme_stocks)
        private lateinit var stocksAdapter: ThemeStockAdapter
        private val cardView: androidx.cardview.widget.CardView = itemView.findViewById(R.id.card_view)

        fun bind(category: ThemeCategory, position: Int) {
            // 카테고리 정보 설정
            categoryName.text = category.name
            
            // 표시되는 종목수가 아니라 전체 종목수로 표시
            val themeResponse = getOriginalResponse(category.id)
            val originalStockCount = themeResponse?.companies?.size ?: category.stocks.size
            stockCount.text = "${originalStockCount}종목"
            
            // 등락률 포맷팅 및 색상 설정
            if (category.fluctuationRate > 0) {
                fluctuationRate.setTextColor(itemView.context.getColor(android.R.color.holo_red_light))
                fluctuationRate.text = String.format("+%.2f%%", category.fluctuationRate)
            } else if (category.fluctuationRate < 0) {
                fluctuationRate.setTextColor(itemView.context.getColor(android.R.color.holo_blue_light))
                fluctuationRate.text = String.format("%.2f%%", category.fluctuationRate)
            } else {
                fluctuationRate.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                fluctuationRate.text = String.format("%.2f%%", category.fluctuationRate)
            }

            // 카테고리 이미지 설정 - API에서 로고 가져오기
            loadCategoryIcon(category.name)
            
            // 주식 목록 어댑터 설정 - 항상 3개만 표시
            stocksAdapter = ThemeStockAdapter(category.stocks.take(3))
            
            // 현재가/거래대금 필터 설정
            stocksAdapter.setFilter(isCurrentPriceFilter)
            
            stocksRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            stocksRecyclerView.adapter = stocksAdapter
            
            // 종목이 없는 경우 구분선 숨김
            stocksDivider.visibility = if (category.stocks.isEmpty()) View.GONE else View.VISIBLE
            
            // 클릭 리스너 설정
            stocksAdapter.setOnItemClickListener { stock, stockPosition ->
                onStockClickListener?.invoke(stock, position, stockPosition)
            }
            
            // 카테고리 헤더 클릭 시 상세 페이지로 이동
            val headerClickListener = View.OnClickListener {
                onCategoryClickListener?.invoke(category, position)
            }
            
            // 헤더 영역 클릭 리스너 설정
            categoryIcon.setOnClickListener(headerClickListener)
            categoryName.setOnClickListener(headerClickListener)
            stockCount.setOnClickListener(headerClickListener)
            fluctuationRate.setOnClickListener(headerClickListener)
        }
        
        // 원본 데이터 가져오기
        private fun getOriginalResponse(categoryId: String): ThemeResponse? {
            return try {
                (itemView.context as? AllThemesActivity)?.getCachedResponse(categoryId)
            } catch (e: Exception) {
                null
            }
        }

        // 카테고리 아이콘을 로드하는 메서드 추가
        private fun loadCategoryIcon(categoryName: String) {
            try {
                // URL 인코딩 (한글 테마명 처리)
                val encodedName = java.net.URLEncoder.encode(categoryName, "UTF-8")
                val iconUrl = "https://antwinner.com/api/image/${encodedName}.png"
                
                Glide.with(itemView.context)
                    .load(iconUrl)
                    .apply(RequestOptions()
                        .placeholder(R.drawable.ic_theme_default)
                        .error(R.drawable.ic_theme_default)
                        .centerCrop()
                    )
                    .into(categoryIcon)
            } catch (e: Exception) {
                // 에러 발생 시 기본 이미지 설정
                categoryIcon.setImageResource(R.drawable.ic_theme_default)
            }
        }

    }
} 