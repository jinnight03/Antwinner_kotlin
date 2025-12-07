package com.example.antwinner_kotlin.ui.theme

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.repository.TrendRepository
import com.example.antwinner_kotlin.model.ThemeCategory
import com.example.antwinner_kotlin.model.ThemeStock
import com.example.antwinner_kotlin.model.ThemeResponse
import com.example.antwinner_kotlin.model.Company
import com.example.antwinner_kotlin.ui.theme.adapter.ThemeCategoryAdapter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.util.Log
import android.view.View
import android.widget.Toast
import android.content.res.ColorStateList
import android.graphics.Color
import com.example.antwinner_kotlin.network.RetrofitClient
import com.example.antwinner_kotlin.util.NetworkUtil
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Intent
import com.example.antwinner_kotlin.MainActivity
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import com.example.antwinner_kotlin.utils.SystemBarUtils

class AllThemesActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalCountTextView: TextView
    private lateinit var adapter: ThemeCategoryAdapter
    private lateinit var backButton: ImageView
    private lateinit var searchButton: ImageView
    private lateinit var filterAscendingTextView: TextView
    private lateinit var btnCurrentPrice: TextView
    private lateinit var btnTradingVolume: TextView
    private lateinit var slidingIndicator: View
    
    private val trendRepository = TrendRepository()
    private var currentFilter = FILTER_CURRENT_PRICE
    private var isAscending = false
    
    // 원본 API 응답 데이터를 캐시
    private var cachedApiResponse: List<ThemeResponse> = emptyList()
    // 변환 후 카테고리 데이터를 캐시
    private var cachedCategories: List<ThemeCategory> = emptyList()

    companion object {
        const val FILTER_CURRENT_PRICE = 0
        const val FILTER_TRADING_VOLUME = 1
        private const val TAG = "AllThemesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_themes)

        // 시스템 바 인셋 적용
        applySystemBarInsets()

        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.rv_all_themes)
        totalCountTextView = findViewById(R.id.tv_total_count)
        backButton = findViewById(R.id.iv_back)
        searchButton = findViewById(R.id.iv_search)
        filterAscendingTextView = findViewById(R.id.tv_filter_ascending)
        btnCurrentPrice = findViewById(R.id.btn_current_price)
        btnTradingVolume = findViewById(R.id.btn_trading_volume)
        slidingIndicator = findViewById(R.id.sliding_indicator)

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        // 뒤로가기 버튼 설정
        backButton.setOnClickListener {
            finish()
        }
        
        // 검색 버튼 설정
        searchButton.setOnClickListener {
            val intent = com.example.antwinner_kotlin.ui.search.SearchActivity.newIntent(this)
            startActivity(intent)
        }
        
        // 정렬 필터 설정
        filterAscendingTextView.setOnClickListener {
            isAscending = !isAscending
            updateSortingText()
            updateCategoriesFromCache()
        }

        // Setup segment control
        setupSegmentControl()
        
        // 초기 상태 설정 (현재가 선택됨)
        slidingIndicator.post {
            slidingIndicator.translationX = 0f
            updateSegmentColors(true)
        }
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // RecyclerView 기본 변경 애니메이션 설정 (부드러운 전환)
        recyclerView.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator().apply {
            changeDuration = 300 // 변경 애니메이션 지속 시간 (ms)
            addDuration = 200 // 추가 애니메이션 지속 시간 (ms)
            removeDuration = 200 // 제거 애니메이션 지속 시간 (ms)
        }

        // Load theme data
        fetchThemeData()
    }
    
    private fun applySystemBarInsets() {
        val appBarLayout = findViewById<View>(R.id.toolbar).parent as View
        SystemBarUtils.applyTopPaddingForAppBar(appBarLayout, 16)
    }
    
    private fun updateSortingText() {
        filterAscendingTextView.text = if (isAscending) "상승률 낮은 순" else "상승률 높은 순"
    }
    
    private fun setupSegmentControl() {
        btnCurrentPrice.setOnClickListener {
            if (currentFilter != FILTER_CURRENT_PRICE) {
                currentFilter = FILTER_CURRENT_PRICE
                animateSegmentToPosition(0) // 왼쪽으로 슬라이드
                updateSegmentColors(true)
                
                // 어댑터에 필터 변경 알림
                adapter.setFilterType(true)
                
                fetchThemeData() // 필터 변경 시에만 API 다시 호출
            }
        }
        
        btnTradingVolume.setOnClickListener {
            if (currentFilter != FILTER_TRADING_VOLUME) {
                currentFilter = FILTER_TRADING_VOLUME
                animateSegmentToPosition(1) // 오른쪽으로 슬라이드
                updateSegmentColors(false)
                
                // 어댑터에 필터 변경 알림
                adapter.setFilterType(false)
                
                fetchThemeData() // 필터 변경 시에만 API 다시 호출
            }
        }
    }
    
    private fun animateSegmentToPosition(position: Int) {
        // 뷰가 측정된 후에 위치 계산
        btnCurrentPrice.post {
            val targetX = when (position) {
                0 -> 0f // 첫 번째 버튼 (현재가)
                1 -> btnCurrentPrice.width.toFloat() // 두 번째 버튼 (거래대금) - 첫 번째 버튼 너비만큼 이동
                else -> 0f
            }
            
            val animator = ObjectAnimator.ofFloat(slidingIndicator, "translationX", targetX)
            animator.duration = 250 // 250ms 애니메이션 (조금 더 부드럽게)
            animator.interpolator = android.view.animation.DecelerateInterpolator() // 감속 애니메이션
            animator.start()
        }
    }
    
    private fun animateSegmentTo(targetX: Int) {
        val animator = ObjectAnimator.ofFloat(slidingIndicator, "translationX", targetX.toFloat())
        animator.duration = 250 // 250ms 애니메이션 (조금 더 부드럽게)
        animator.interpolator = android.view.animation.DecelerateInterpolator() // 감속 애니메이션
        animator.start()
    }
    
    private fun updateSegmentColors(isCurrentPriceSelected: Boolean) {
        if (isCurrentPriceSelected) {
            // 현재가 선택됨
            btnCurrentPrice.setTextColor(getColor(android.R.color.white))
            btnTradingVolume.setTextColor(getColor(android.R.color.black))
        } else {
            // 거래대금 선택됨
            btnCurrentPrice.setTextColor(getColor(android.R.color.black))
            btnTradingVolume.setTextColor(getColor(android.R.color.white))
        }
    }
    
    private fun setupRecyclerView() {
        adapter = ThemeCategoryAdapter(emptyList())
        
        // GridLayoutManager 설정
        val layoutManager = GridLayoutManager(this, 2)
        
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        
        // RecyclerView 설정 
        recyclerView.setHasFixedSize(false)  // 아이템 크기가 변하므로 false로 설정
        
        // 카테고리 클릭 이벤트 설정 (펼치기/접기)
        adapter.setOnCategoryClickListener { category, position ->
            // 테마명 클릭 시 ThemeDetailActivity로 직접 이동
            val intent = com.example.antwinner_kotlin.ui.themedetail.ThemeDetailActivity.newIntent(
                this,
                category.id,
                category.name
            )
            startActivity(intent)
        }
        
        // 종목 클릭 이벤트 설정
        adapter.setOnStockClickListener { stock, categoryPosition, stockPosition ->
            // 종목명 클릭 시 StockDetailActivity로 이동
            val intent = com.example.antwinner_kotlin.ui.stockdetail.StockDetailActivity.newIntent(
                this,
                stock.name,
                stock.id // 종목코드로 사용
            )
            startActivity(intent)
        }
        
    }


    private fun fetchThemeData() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // 로딩 표시 (필요시 구현)
                
                // API 호출하여 테마 데이터 가져오기
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getAllThemes()
                }
                
                // 원본 응답 데이터 캐시
                cachedApiResponse = response
                
                // 데이터 변환 및 화면 업데이트
                updateCategoriesFromCache()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading theme data", e)
                Toast.makeText(this@AllThemesActivity, "데이터를 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateCategoriesFromCache() {
        if (cachedApiResponse.isEmpty()) return
        
        val categories = cachedApiResponse.map { response ->
            createCategoryFromResponse(response)
        }
        
        // 정렬 적용
        val sortedCategories = if (isAscending) {
            categories.sortedBy { it.fluctuationRate }
        } else {
            categories.sortedByDescending { it.fluctuationRate }
        }
        
        // 캐시된 카테고리 업데이트
        cachedCategories = sortedCategories
        
        // 총 개수 업데이트
        totalCountTextView.text = "총 ${sortedCategories.size}개"
        
        // 어댑터 업데이트
        adapter.updateData(sortedCategories)
    }
    
    private fun createCategoryFromResponse(response: ThemeResponse): ThemeCategory {
        // 문자열에서 숫자만 추출하고 Double로 변환
        val rateStr = response.averageRate.replace("%", "")
        val rate = rateStr.toDoubleOrNull() ?: 0.0
        
        // 테마 내 종목들 변환
        val themeStocks = response.companies.map { company ->
            // API에서 받은 volume 값 그대로 사용
            val volumeStr = company.volume
            // 로고 URL 생성
            val logoUrl = if (!company.stockCode.isNullOrBlank()) {
                "https://antwinner.com/api/stock_logos/${company.stockCode}.png"
            } else {
                "" // 기본 URL 또는 빈 문자열
            }
            
            ThemeStock(
                id = company.name, // 종목명을 ID로 사용
                name = company.name,
                price = parsePriceString(company.currentPrice),
                changeRate = company.fluctuation.replace("%", "").toDoubleOrNull() ?: 0.0,
                tradingAmount = parseVolumeString(volumeStr), // 볼륨 문자열을 숫자로 변환
                logoUrl = logoUrl // logoUrl 전달
            )
        }
        
        // 항상 3개만 표시
        val filteredStocks = themeStocks.take(3)
        
        return ThemeCategory(
            id = response.name,
            name = response.name,
            fluctuationRate = rate,
            stocks = filteredStocks
        )
    }

    // 임의의 거래대금 생성 함수 제거하고 볼륨 문자열 파싱 함수로 대체
    private fun parseVolumeString(volumeStr: String): Long {
        // "1662억", "29억" 등의 형식에서 숫자와 단위로 분리
        val numericPart = volumeStr.replace("[^0-9]".toRegex(), "")
        val unit = volumeStr.replace("[0-9]".toRegex(), "")
        
        val baseValue = numericPart.toLongOrNull() ?: 0L
        
        return when {
            unit.contains("조") -> baseValue * 1_000_000_000_000L // 조 단위
            unit.contains("억") -> baseValue * 100_000_000L // 억 단위
            unit.contains("만") -> baseValue * 10000L // 만 단위
            else -> baseValue // 기본 단위
        }
    }

    private fun parsePriceString(priceStr: String): Int {
        // "8,710", "1,230원" 등의 형식에서 숫자만 추출
        return priceStr.replace(",", "").replace("원", "").toIntOrNull() ?: 0
    }

    private fun updateThemeList(categories: List<ThemeCategory>) {
        // 정렬 순서에 따라 데이터 정렬
        val sortedCategories = if (isAscending) {
            categories.sortedBy { it.fluctuationRate }
        } else {
            categories.sortedByDescending { it.fluctuationRate }
        }
        
        // 총 개수 업데이트
        totalCountTextView.text = "총 ${sortedCategories.size}개"
        
        // 어댑터 업데이트
        adapter.updateData(sortedCategories)
    }

    // 원본 응답 데이터 가져오기
    fun getCachedResponse(categoryId: String): ThemeResponse? {
        return cachedApiResponse.find { it.name == categoryId }
    }
} 