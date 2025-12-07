package com.example.antwinner_kotlin.ui.home.adapter

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

class AutocompleteAdapter : RecyclerView.Adapter<AutocompleteAdapter.ViewHolder>() {
    
    // 자동완성 결과 타입
    enum class ResultType {
        STOCK, THEME
    }
    
    // 자동완성 결과 데이터 클래스
    data class AutocompleteResult(
        val type: ResultType,
        val name: String,
        val code: String = "",
        val theme: ThemeFluctuation? = null,
        val stock: StockSearchResponse? = null
    )
    
    private val resultsList = mutableListOf<AutocompleteResult>()
    private var itemClickListener: ((result: AutocompleteResult) -> Unit)? = null
    
    fun setOnItemClickListener(listener: (result: AutocompleteResult) -> Unit) {
        itemClickListener = listener
    }
    
    // 종목 검색 결과 설정
    fun setStockResults(stocks: List<StockSearchResponse>) {
        val stockResults = stocks.map { stock ->
            AutocompleteResult(
                type = ResultType.STOCK,
                name = stock.name,
                code = stock.code,
                stock = stock
            )
        }
        
        resultsList.clear()
        resultsList.addAll(stockResults)
        notifyDataSetChanged()
    }
    
    // 테마 검색 결과 설정
    fun setThemeResults(themes: List<ThemeFluctuation>) {
        Log.d("AutocompleteAdapter", "Setting ${themes.size} theme results")
        
        if (themes.isEmpty()) {
            Log.d("AutocompleteAdapter", "Theme results list is empty")
        } else {
            Log.d("AutocompleteAdapter", "Theme names: ${themes.joinToString(", ") { it.thema ?: "Unknown" }}")
        }
        
        val themeResults = themes.mapNotNull { theme ->
            // thema가 null이면 해당 항목 제외
            theme.thema?.let { themeName ->
                AutocompleteResult(
                    type = ResultType.THEME,
                    name = themeName,  // String으로 변환된 non-null 값
                    theme = theme
                )
            }
        }
        
        resultsList.clear()
        resultsList.addAll(themeResults)
        notifyDataSetChanged()
    }
    
    // 종목과 테마 검색 결과 모두 설정
    fun setMixedResults(stocks: List<StockSearchResponse>, themes: List<ThemeFluctuation>) {
        Log.d("AutocompleteAdapter", "Setting mixed results: ${stocks.size} stocks, ${themes.size} themes")
        
        if (themes.isNotEmpty()) {
            Log.d("AutocompleteAdapter", "Theme names: ${themes.joinToString(", ") { it.thema ?: "Unknown" }}")
        }
        if (stocks.isNotEmpty()) {
            Log.d("AutocompleteAdapter", "Stock names: ${stocks.joinToString(", ") { it.name }}")
        }
        
        val combinedResults = mutableListOf<AutocompleteResult>()
        
        // 종목 결과 추가
        combinedResults.addAll(stocks.map { stock ->
            AutocompleteResult(
                type = ResultType.STOCK,
                name = stock.name,
                code = stock.code,
                stock = stock
            )
        })
        
        // 테마 결과 추가 - null 테마 이름 처리
        combinedResults.addAll(themes.mapNotNull { theme ->
            theme.thema?.let { themeName ->
                AutocompleteResult(
                    type = ResultType.THEME,
                    name = themeName,  // String으로 변환된 non-null 값
                    theme = theme
                )
            }
        })
        
        resultsList.clear()
        resultsList.addAll(combinedResults)
        notifyDataSetChanged()
    }
    
    // 모든 결과 지우기
    fun clearResults() {
        resultsList.clear()
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_autocomplete_result, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = resultsList[position]
        holder.bind(result)
    }
    
    override fun getItemCount(): Int = resultsList.size
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImageView: ImageView = itemView.findViewById(R.id.iv_result_icon)
        private val textView: TextView = itemView.findViewById(R.id.tv_result_text)
        
        fun bind(result: AutocompleteResult) {
            // 텍스트 설정
            textView.text = when (result.type) {
                ResultType.STOCK -> "${result.name} (${result.code})"
                ResultType.THEME -> "${result.name} (테마)"
            }
            
            // 이미지 URL 생성 및 로딩
            val imageUrl = when (result.type) {
                ResultType.STOCK -> {
                    val url = "https://antwinner.com/api/stock_logos/${result.code}"
                    Log.d("AutocompleteAdapter", "Loading stock logo: $url")
                    url
                }
                ResultType.THEME -> {
                    // 테마 이름에 특수문자가 있을 수 있으므로 URL 인코딩
                    val encodedThemeName = URLEncoder.encode(result.name, StandardCharsets.UTF_8.toString())
                    val url = "https://antwinner.com/api/image/${encodedThemeName}.png"
                    Log.d("AutocompleteAdapter", "Loading theme logo: $url")
                    url
                }
            }
            
            // Glide로 이미지 로딩
            try {
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_theme_default) // 로딩 중 표시할 이미지
                    .error(R.drawable.ic_theme_default) // 로딩 실패 시 표시할 이미지
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // 디스크 캐싱 활성화
                    .circleCrop() // 원형 이미지로 표시
                    .into(iconImageView)
            } catch (e: Exception) {
                // 이미지 로딩 실패 시 기본 이미지 표시
                Log.e("AutocompleteAdapter", "Error loading image: ${e.message}")
                iconImageView.setImageResource(R.drawable.ic_theme_default)
            }
            
            // 클릭 리스너 설정
            itemView.setOnClickListener {
                itemClickListener?.invoke(result)
            }
        }
    }
} 