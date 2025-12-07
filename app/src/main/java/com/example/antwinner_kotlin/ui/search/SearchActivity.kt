package com.example.antwinner_kotlin.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.network.RetrofitClient
import com.example.antwinner_kotlin.repository.TrendRepository
import com.example.antwinner_kotlin.ui.home.model.ThemeFluctuation
import com.example.antwinner_kotlin.ui.search.adapter.IssueStockAdapter
import com.example.antwinner_kotlin.ui.search.adapter.RecommendedKeywordAdapter
import com.example.antwinner_kotlin.ui.search.adapter.SearchResultsAdapter
import com.example.antwinner_kotlin.ui.search.model.IssueStockResponse
import com.example.antwinner_kotlin.ui.search.model.LatestKeywordResponse
import com.example.antwinner_kotlin.ui.search.model.TopStock
import com.example.antwinner_kotlin.ui.stockdetail.StockDetailActivity
import com.example.antwinner_kotlin.ui.stocks.model.StockSearchResponse
import com.example.antwinner_kotlin.ui.themedetail.ThemeDetailActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import com.example.antwinner_kotlin.utils.SystemBarUtils

class SearchActivity : AppCompatActivity() {
    
    private lateinit var backButton: ImageView
    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var recommendedKeywordsRecyclerView: RecyclerView
    private lateinit var issueStocksRecyclerView: RecyclerView
    private lateinit var scrollMainContent: NestedScrollView
    
    private val trendRepository = TrendRepository()
    private val searchResultsAdapter = SearchResultsAdapter()
    private val recommendedKeywordAdapter = RecommendedKeywordAdapter()
    private val issueStockAdapter = IssueStockAdapter()
    private var searchJob: Job? = null
    
    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SearchActivity::class.java)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 소프트 키보드가 화면을 가리지 않도록 설정
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        
        setContentView(R.layout.activity_search)
        
        // 시스템 바 인셋 적용
        applySystemBarInsets()
        
        // 뷰 초기화
        initViews()
        
        // 검색 결과 RecyclerView 설정
        setupSearchResultsRecyclerView()
        
        // 추천 검색어 RecyclerView 설정
        setupRecommendedKeywordsRecyclerView()
        
        // 오늘의 이슈종목 RecyclerView 설정
        setupIssueStocksRecyclerView()
        
        // 클릭 리스너 설정
        setupClickListeners()
        
        // 검색창 자동완성 기능 설정
        setupSearchAutocomplete()
        
        // 키보드 자동으로 표시
        showKeyboard()
        
        // 데이터 로드
        loadData()
        
        // 초기에는 검색 결과 화면 숨기고 메인 화면 표시
        hideSearchResults()
    }
    
    private fun applySystemBarInsets() {
        val searchBarLayout = findViewById<LinearLayout>(R.id.layout_search_bar)
        SystemBarUtils.applyTopPaddingInset(searchBarLayout, 12)
    }
    
    private fun initViews() {
        try {
            backButton = findViewById(R.id.iv_back)
            searchEditText = findViewById(R.id.et_search)
            clearButton = findViewById(R.id.iv_clear)
            searchResultsRecyclerView = findViewById(R.id.rv_search_results)
            recommendedKeywordsRecyclerView = findViewById(R.id.rv_recommended_keywords)
            issueStocksRecyclerView = findViewById(R.id.rv_issue_stocks)
            scrollMainContent = findViewById(R.id.scroll_main_content)
            
            // 검색 결과 RecyclerView의 초기 상태 설정
            searchResultsRecyclerView.visibility = View.GONE
        } catch (e: Exception) {
            Log.e("SearchActivity", "Error initializing views: ${e.message}", e)
        }
    }
    
    private fun setupSearchResultsRecyclerView() {
        try {
            searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
            searchResultsRecyclerView.adapter = searchResultsAdapter
            
            // 테마 클릭 이벤트 처리
            searchResultsAdapter.setOnThemeClickListener { theme ->
                try {
                    val intent = ThemeDetailActivity.newIntent(
                        this,
                        "",
                        theme.thema
                    )
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("SearchActivity", "Error handling theme click: ${e.message}", e)
                    // 오류 발생 시 사용자에게 알림
                    Toast.makeText(this, "테마 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                }
            }
            
            // 종목 클릭 이벤트 처리
            searchResultsAdapter.setOnStockClickListener { stock ->
                try {
                    val intent = StockDetailActivity.newIntent(
                        this,
                        stock.name,
                        stock.code
                    )
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("SearchActivity", "Error handling stock click: ${e.message}", e)
                    // 오류 발생 시 사용자에게 알림
                    Toast.makeText(this, "종목 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("SearchActivity", "Error setting up search results recycler view: ${e.message}", e)
        }
    }
    
    private fun setupRecommendedKeywordsRecyclerView() {
        try {
            // 추천 검색어 RecyclerView 설정 - 그리드 레이아웃으로 변경 (한 줄에 3개)
            val gridLayoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 3)
            recommendedKeywordsRecyclerView.layoutManager = gridLayoutManager
            recommendedKeywordsRecyclerView.adapter = recommendedKeywordAdapter
            
            // 클릭 이벤트 설정
            recommendedKeywordAdapter.setOnKeywordClickListener { keyword ->
                // 검색창에 키워드 설정하고 검색 실행
                searchEditText.setText(keyword)
                performSearch(keyword)
            }
        } catch (e: Exception) {
            Log.e("SearchActivity", "Error setting up recommended keywords recycler view: ${e.message}", e)
        }
    }
    
    private fun setupIssueStocksRecyclerView() {
        try {
            // 오늘의 이슈종목 RecyclerView 설정
            issueStocksRecyclerView.layoutManager = LinearLayoutManager(this)
            issueStocksRecyclerView.adapter = issueStockAdapter
            issueStocksRecyclerView.isNestedScrollingEnabled = false
            
            // 클릭 이벤트 설정
            issueStockAdapter.setOnStockClickListener { stock ->
                try {
                    // 종목 상세 페이지로 이동
                    val intent = StockDetailActivity.newIntent(
                        this,
                        stock.stockName,
                        stock.stockCode
                    )
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("SearchActivity", "Error opening stock detail: ${e.message}", e)
                    Toast.makeText(this, "종목 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("SearchActivity", "Error setting up issue stocks recycler view: ${e.message}", e)
        }
    }
    
    private fun setupClickListeners() {
        // 뒤로가기 버튼
        backButton.setOnClickListener {
            finish()
        }
        
        // 검색창 지우기 버튼
        clearButton.setOnClickListener {
            searchEditText.text.clear()
            hideSearchResults()
        }
        
        // 검색 액션 처리
        searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                performSearch(searchEditText.text.toString())
                return@setOnEditorActionListener true
            }
            false
        }
    }
    
    private fun setupSearchAutocomplete() {
        // 검색창 텍스트 변경 이벤트 처리
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    // 텍스트 변경될 때마다 즉시 UI 변경
                    val query = s?.toString()?.trim() ?: ""
                    Log.d("SearchActivity", "Text changed: '$query'")
                    
                    if (query.isEmpty()) {
                        hideSearchResults()
                        return
                    }
                    
                    // 검색 결과 화면만 보이게 설정
                    hideMainContent()
                    
                    // 검색 결과 RecyclerView는 결과가 있을 때만 보여줄 것이므로 일단 숨김
                    searchResultsRecyclerView.visibility = View.GONE
                } catch (e: Exception) {
                    Log.e("SearchActivity", "Error in onTextChanged: ${e.message}", e)
                }
            }
            
            override fun afterTextChanged(s: Editable?) {
                try {
                    // 검색어가 변경되면 이전 검색 작업 취소
                    searchJob?.cancel()
                    
                    val query = s?.toString()?.trim() ?: ""
                    
                    // 검색창이 비어있으면 검색 결과 숨기기
                    if (query.isEmpty()) {
                        hideSearchResults()
                        return
                    }
                    
                    // 백그라운드 작업 취소 플래그
                    val currentJob = Job()
                    searchJob = currentJob
                    
                    // 1글자만 입력된 경우에도 바로 검색 시작
                    if (query.length >= 1) {
                        Log.d("SearchActivity", "Starting search for query: '$query'")
                        
                        lifecycleScope.launch(Dispatchers.IO + currentJob) {
                            try {
                                delay(300) // 타이핑 중에 너무 많은 요청을 방지하기 위한 지연
                                
                                if (!currentJob.isActive) return@launch
                                
                                // 테마 검색
                                Log.d("SearchActivity", "Searching themes for query: '$query'")
                                val themeResults = try {
                                    // 새로운 테마 자동완성 API 사용
                                    val response = RetrofitClient.apiService.getThemeAutocomplete(query)
                                    Log.d("SearchActivity", "API theme response: keyword=${response.keyword}, suggestions count=${response.suggestionsCount}")
                                    
                                    // ThemeAutocompleteResponse를 ThemeFluctuation 목록으로 변환
                                    if (response.suggestions.isNotEmpty()) {
                                        response.suggestions.map { suggestion ->
                                            ThemeFluctuation(
                                                averageRate = suggestion.averageRate ?: "0.0%", // API 응답의 등락률 사용
                                                companies = emptyList(), // 종목 정보가 없으므로 빈 리스트 설정
                                                thema = suggestion.themeName,
                                                transactionAmount = suggestion.stockCount.toString() // 종목 수
                                            )
                                        }
                                    } else {
                                        emptyList()
                                    }
                                } catch (e: Exception) {
                                    Log.e("SearchActivity", "Error in theme search API: ${e.message}", e)
                                    // API 호출 실패 시 더미 데이터 사용 - 일치하는 내용만 표시
                                    if (query.contains("반")) {
                                        getDummyThemesForQuery(query)
                                    } else {
                                        emptyList() // 일치하지 않으면 빈 리스트 반환
                                    }
                                }
                                
                                if (!currentJob.isActive) return@launch
                                
                                // 종목 검색
                                Log.d("SearchActivity", "Searching stocks for query: '$query'")
                                val stockResults = try {
                                    val response = RetrofitClient.apiService.getAutocomplete(query)
                                    Log.d("SearchActivity", "API stock response: ${response.size} stocks found")
                                    response
                                } catch (e: Exception) {
                                    Log.e("SearchActivity", "Error in stock search API: ${e.message}", e)
                                    // API 호출 실패 시 더미 데이터 사용 - 일치하는 내용만 표시
                                    if (query.contains("반")) {
                                        getDummyStocksForQuery(query)
                                    } else {
                                        emptyList() // 일치하지 않으면 빈 리스트 반환
                                    }
                                }
                                
                                if (!currentJob.isActive) return@launch
                                
                                // 뉴스 검색
                                Log.d("SearchActivity", "Searching news for query: '$query'")
                                val newsResults = try {
                                    val response = RetrofitClient.apiService.getNewsForKeyword(query)
                                    Log.d("SearchActivity", "API news response: ${response.size} news found")
                                    
                                    // NewsResponse를 SearchResultsAdapter.NewsData로 변환
                                    response.map { news ->
                                        SearchResultsAdapter.NewsData(
                                            id = news.id.toString(),
                                            title = news.title,
                                            source = news.stockName ?: "뉴스",
                                            date = formatDate(news.date),
                                            imageUrl = news.imageUrl
                                        )
                                    }
                                } catch (e: Exception) {
                                    Log.e("SearchActivity", "Error in news search API: ${e.message}", e)
                                    // API 호출 실패 시 더미 데이터 사용 - 일치하는 내용만 표시
                                    if (query.contains("반")) {
                                        getDummyNewsForQuery(query)
                                    } else {
                                        emptyList() // 일치하지 않으면 빈 리스트 반환
                                    }
                                }
                                
                                if (!currentJob.isActive) return@launch
                                
                                // UI 업데이트는 메인 스레드에서 실행
                                withContext(Dispatchers.Main) {
                                    try {
                                        if (!currentJob.isActive) return@withContext
                                        
                                        Log.d("SearchActivity", "Updating UI with results - themes: ${themeResults.size}, stocks: ${stockResults.size}, news: ${newsResults.size}")
                                        
                                        // 검색 결과가 있는지 확인
                                        val hasResults = themeResults.isNotEmpty() || stockResults.isNotEmpty() || newsResults.isNotEmpty()
                                        
                                        if (hasResults) {
                                            // 검색 결과가 있는 경우
                                            searchResultsAdapter.setData(themeResults, stockResults, newsResults)
                                            searchResultsRecyclerView.visibility = View.VISIBLE
                                        } else {
                                            // 검색 결과가 없는 경우
                                            searchResultsAdapter.clearData()
                                            // 결과가 없으면 RecyclerView 숨김
                                            searchResultsRecyclerView.visibility = View.GONE
                                        }
                                    } catch (e: Exception) {
                                        Log.e("SearchActivity", "Error updating UI: ${e.message}", e)
                                        // 검색 결과 RecyclerView 숨김
                                        searchResultsRecyclerView.visibility = View.GONE
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("SearchActivity", "Error in autocomplete search: ${e.message}", e)
                                withContext(Dispatchers.Main) {
                                    try {
                                        // 오류 발생 시 RecyclerView 숨김
                                        searchResultsAdapter.clearData()
                                        searchResultsRecyclerView.visibility = View.GONE
                                    } catch (e: Exception) {
                                        Log.e("SearchActivity", "Error clearing data: ${e.message}", e)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SearchActivity", "Error in afterTextChanged: ${e.message}", e)
                }
            }
        })
    }
    
    private fun performSearch(query: String) {
        if (query.isEmpty()) return
        
        try {
            // 키보드 숨기기
            hideKeyboard()
            
            // 검색 시작 즉시 메인 콘텐츠 숨기기 (결과는 나중에 표시)
            hideMainContent()
            
            Log.d("SearchActivity", "Performing search for query: '$query'")
            
            // 전체 검색 수행 로직 (종목, 테마, 뉴스 검색 등)
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // 테마 검색
                    Log.d("SearchActivity", "Searching themes for query: '$query'")
                    val themeResults = try {
                        // 새로운 테마 자동완성 API 사용
                        val response = RetrofitClient.apiService.getThemeAutocomplete(query)
                        Log.d("SearchActivity", "API theme response: keyword=${response.keyword}, suggestions count=${response.suggestionsCount}")
                        
                        // ThemeAutocompleteResponse를 ThemeFluctuation 목록으로 변환
                        if (response.suggestions.isNotEmpty()) {
                            response.suggestions.map { suggestion ->
                                ThemeFluctuation(
                                    averageRate = suggestion.averageRate ?: "0.0%", // API 응답의 등락률 사용
                                    companies = emptyList(), // 종목 정보가 없으므로 빈 리스트 설정
                                    thema = suggestion.themeName,
                                    transactionAmount = suggestion.stockCount.toString() // 종목 수
                                )
                            }
                        } else {
                            emptyList()
                        }
                    } catch (e: Exception) {
                        Log.e("SearchActivity", "Error in theme search API: ${e.message}", e)
                        // API 호출 실패 시 더미 데이터 사용 - 일치하는 내용만 표시
                        if (query.contains("반")) {
                            getDummyThemesForQuery(query)
                        } else {
                            emptyList() // 일치하지 않으면 빈 리스트 반환
                        }
                    }
                    
                    Log.d("SearchActivity", "Theme search results: ${themeResults.size} themes found")
                    
                    // 종목 검색
                    Log.d("SearchActivity", "Searching stocks for query: '$query'")
                    val stockResults = try {
                        val response = RetrofitClient.apiService.getAutocomplete(query)
                        Log.d("SearchActivity", "API stock response: ${response.size} stocks found")
                        response
                    } catch (e: Exception) {
                        Log.e("SearchActivity", "Error in stock search API: ${e.message}", e)
                        // API 호출 실패 시 더미 데이터 사용 - 일치하는 내용만 표시
                        if (query.contains("반")) {
                            getDummyStocksForQuery(query)
                        } else {
                            emptyList() // 일치하지 않으면 빈 리스트 반환
                        }
                    }
                    
                    Log.d("SearchActivity", "Stock search result: ${stockResults.size} stocks found")
                    
                    // 뉴스 검색
                    Log.d("SearchActivity", "Searching news for query: '$query'")
                    val newsResults = try {
                        val response = RetrofitClient.apiService.getNewsForKeyword(query)
                        Log.d("SearchActivity", "API news response: ${response.size} news found")
                        
                        // NewsResponse를 SearchResultsAdapter.NewsData로 변환
                        response.map { news ->
                            SearchResultsAdapter.NewsData(
                                id = news.id.toString(),
                                title = news.title,
                                source = news.stockName ?: "뉴스",
                                date = formatDate(news.date),
                                imageUrl = news.imageUrl
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("SearchActivity", "Error in news search API: ${e.message}", e)
                        // API 호출 실패 시 더미 데이터 사용 - 일치하는 내용만 표시
                        if (query.contains("반")) {
                            getDummyNewsForQuery(query)
                        } else {
                            emptyList() // 일치하지 않으면 빈 리스트 반환
                        }
                    }
                    
                    Log.d("SearchActivity", "News search results: ${newsResults.size} news found")
                    
                    // UI 업데이트는 메인 스레드에서 수행
                    withContext(Dispatchers.Main) {
                        try {
                            Log.d("SearchActivity", "Updating UI with results - themes: ${themeResults.size}, stocks: ${stockResults.size}, news: ${newsResults.size}")
                            
                            // 검색 결과가 있는지 확인
                            val hasResults = themeResults.isNotEmpty() || stockResults.isNotEmpty() || newsResults.isNotEmpty()
                            
                            if (hasResults) {
                                // 검색 결과가 있는 경우, 결과 표시
                                searchResultsAdapter.setData(themeResults, stockResults, newsResults)
                                searchResultsRecyclerView.visibility = View.VISIBLE
                            } else {
                                // 검색 결과가 없는 경우
                                searchResultsAdapter.clearData()
                                searchResultsRecyclerView.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            Log.e("SearchActivity", "Error updating UI after search: ${e.message}", e)
                            // 예외 발생 시 기본 화면 유지, RecyclerView 숨김
                            searchResultsAdapter.clearData()
                            searchResultsRecyclerView.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SearchActivity", "Error in search: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        try {
                            // 오류 발생 시 기본 화면 유지, RecyclerView 숨김
                            searchResultsAdapter.clearData()
                            searchResultsRecyclerView.visibility = View.GONE
                        } catch (e: Exception) {
                            Log.e("SearchActivity", "Error showing error toast: ${e.message}", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SearchActivity", "Error starting search: ${e.message}", e)
        }
    }
    
    // 주식 등락률 평균 계산
    private fun calculateAverageRate(stocks: List<TopStock>): String {
        if (stocks.isEmpty()) return "0.0%"
        
        try {
            val rates = stocks.mapNotNull { stock ->
                val rateString = stock.fluctuationRate.replace("%", "").replace("+", "")
                rateString.toFloatOrNull()
            }
            
            if (rates.isEmpty()) return "0.0%"
            
            val average = rates.average()
            return if (average > 0) {
                "+%.2f%%".format(average)
            } else {
                "%.2f%%".format(average)
            }
        } catch (e: Exception) {
            Log.e("SearchActivity", "Error calculating average rate: ${e.message}", e)
            return "0.0%"
        }
    }
    
    // 날짜 포맷팅
    private fun formatDate(dateString: String): String {
        return try {
            // 현재는 간단히 그대로 사용, 필요시 포맷팅 로직 추가
            dateString
        } catch (e: Exception) {
            Log.e("SearchActivity", "Error formatting date: ${e.message}", e)
            "오늘"
        }
    }
    
    // 임시 반도체 테마 더미 데이터 생성
    private fun getDummyThemesForQuery(query: String): List<ThemeFluctuation> {
        try {
            Log.d("SearchActivity", "Getting dummy themes for query: '$query'")
            val dummyThemes = listOf(
                ThemeFluctuation(
                    averageRate = "+14.61%",
                    companies = emptyList(),
                    thema = "반도체(인공지능)",
                    transactionAmount = "16"  // 종목 수
                ),
                ThemeFluctuation(
                    averageRate = "0.0%",
                    companies = emptyList(),
                    thema = "반도체(CXL)",
                    transactionAmount = "8"  // 종목 수
                ),
                ThemeFluctuation(
                    averageRate = "-1.46%",
                    companies = emptyList(),
                    thema = "반도체",
                    transactionAmount = "16"  // 종목 수
                ),
                ThemeFluctuation(
                    averageRate = "-1.46%",
                    companies = emptyList(),
                    thema = "반도체(전력)",
                    transactionAmount = "16"  // 종목 수
                )
            )
            
            val filteredThemes = dummyThemes.filter {
                it.thema.contains(query, ignoreCase = true) || query.uppercase() in it.thema.uppercase()
            }
            
            Log.d("SearchActivity", "Found ${filteredThemes.size} dummy themes for query: '$query'")
            return filteredThemes
        } catch (e: Exception) {
            Log.e("SearchActivity", "Error in getDummyThemesForQuery: ${e.message}", e)
            return emptyList()
        }
    }
    
    // 임시 반도체 관련 더미 데이터 생성 (API 호출 실패 시 사용)
    private fun getDummyStocksForQuery(query: String): List<StockSearchResponse> {
        try {
            Log.d("SearchActivity", "Getting dummy stocks for query: '$query'")
            
            // 새로 추가한 더미 데이터 모델 사용
            return com.example.antwinner_kotlin.ui.search.model.DummyModels.getDummySemiconductorStocks()
        } catch (e: Exception) {
            Log.e("SearchActivity", "Error in getDummyStocksForQuery: ${e.message}", e)
            return emptyList()
        }
    }
    
    // 일반적인 종목 더미 데이터 생성 (모든 검색어에 대응)
    private fun getDummyGenericStocks(query: String): List<StockSearchResponse> {
        try {
            Log.d("SearchActivity", "Getting generic dummy stocks for query: '$query'")
            
            // 새로 추가한 더미 데이터 모델 사용
            return com.example.antwinner_kotlin.ui.search.model.DummyModels.getDummyGenericStocks(query)
        } catch (e: Exception) {
            Log.e("SearchActivity", "Error in getDummyGenericStocks: ${e.message}", e)
            return emptyList()
        }
    }
    
    // 임시 뉴스 더미 데이터
    private fun getDummyNewsForQuery(query: String): List<SearchResultsAdapter.NewsData> {
        try {
            Log.d("SearchActivity", "Getting dummy news for query: '$query'")
            
            val defaultNews = listOf(
                SearchResultsAdapter.NewsData(
                    id = "1",
                    title = "$query 관련 최신 뉴스입니다",
                    source = "경제일보",
                    date = "2시간 전",
                    imageUrl = "https://picsum.photos/200/300"
                ),
                SearchResultsAdapter.NewsData(
                    id = "2",
                    title = "$query 시장 동향 분석",
                    source = "투자저널",
                    date = "오늘",
                    imageUrl = "https://picsum.photos/201/300"
                )
            )
            
            val semiconductorNews = if (query.contains("반")) {
                listOf(
                    SearchResultsAdapter.NewsData(
                        id = "3",
                        title = "삼성전자, 반도체 신제품 발표...AI 반도체 시장 선도",
                        source = "한국경제",
                        date = "3시간 전",
                        imageUrl = "https://picsum.photos/202/300"
                    ),
                    SearchResultsAdapter.NewsData(
                        id = "4",
                        title = "반도체주 상승세...시장 전반적인 회복 기대감",
                        source = "매일경제",
                        date = "오늘",
                        imageUrl = "https://picsum.photos/203/300"
                    ),
                    SearchResultsAdapter.NewsData(
                        id = "5",
                        title = "반도체 소부장 산업 국산화 가속화...정부 지원책 발표",
                        source = "전자신문",
                        date = "어제",
                        imageUrl = "https://picsum.photos/204/300"
                    ),
                    SearchResultsAdapter.NewsData(
                        id = "6",
                        title = "ITF K-AI반도체코어테크, 신규 AI칩 출시 임박",
                        source = "디지털타임스",
                        date = "2일 전",
                        imageUrl = "https://picsum.photos/205/300"
                    )
                )
            } else {
                emptyList()
            }
            
            val allNews = defaultNews + semiconductorNews
            
            Log.d("SearchActivity", "Found ${allNews.size} news items for query: '$query'")
            return allNews
        } catch (e: Exception) {
            Log.e("SearchActivity", "Error in getDummyNewsForQuery: ${e.message}", e)
            return emptyList()
        }
    }
    
    // UI 상태 관리를 위한 메소드들
    
    // 메인 화면 콘텐츠를 숨기는 메소드
    private fun hideMainContent() {
        try {
            // 메인 콘텐츠 스크롤 영역 숨김
            scrollMainContent.visibility = View.GONE
        } catch (e: Exception) {
            Log.e("SearchActivity", "Error in hideMainContent: ${e.message}", e)
        }
    }
    
    // 검색 결과를 숨기고 메인 화면 표시
    private fun hideSearchResults() {
        try {
            // 검색 결과 화면 숨김
            searchResultsRecyclerView.visibility = View.GONE
            
            // 어댑터 데이터 초기화 (메모리 누수 방지)
            searchResultsAdapter.clearData()
            
            // 메인 콘텐츠 스크롤 영역 표시
            scrollMainContent.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e("SearchActivity", "Error in hideSearchResults: ${e.message}", e)
        }
    }
    
    private fun showKeyboard() {
        searchEditText.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }
    
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }
    
    override fun onBackPressed() {
        try {
            Log.d("SearchActivity", "onBackPressed called, searchResultsRecyclerView visibility: " + 
                (if (searchResultsRecyclerView.visibility == View.VISIBLE) "VISIBLE" else "GONE"))
            
            // 검색창에 텍스트가 있으면 텍스트 지우기
            if (searchEditText.text.isNotEmpty()) {
                searchEditText.text.clear()
                hideSearchResults()
                showKeyboard()
                return
            }
            
            // 메인 화면으로 돌아가기
            super.onBackPressed()
        } catch (e: Exception) {
            Log.e("SearchActivity", "Error in onBackPressed: ${e.message}", e)
            super.onBackPressed()
        }
    }
    
    // 데이터 로드
    private fun loadData() {
        try {
            // 추천 검색어 로드
            loadRecommendedKeywords()
            
            // 오늘의 이슈종목 로드
            loadIssueStocks()
        } catch (e: Exception) {
            Log.e("SearchActivity", "Error loading data: ${e.message}", e)
        }
    }
    
    // 추천 검색어 로드
    private fun loadRecommendedKeywords() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // API에서 추천 검색어 로드
                val keywords = RetrofitClient.apiService.getLatestKeywords()
                
                withContext(Dispatchers.Main) {
                    // UI 업데이트
                    if (keywords.isNotEmpty()) {
                        recommendedKeywordAdapter.updateKeywords(keywords)
                    } else {
                        // 임시 데이터 사용
                        recommendedKeywordAdapter.updateKeywords(getDummyKeywords())
                    }
                }
            } catch (e: Exception) {
                Log.e("SearchActivity", "Error loading recommended keywords: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    // 오류 발생 시 임시 데이터 사용
                    recommendedKeywordAdapter.updateKeywords(getDummyKeywords())
                }
            }
        }
    }
    
    // 추천 검색어 더미 데이터
    private fun getDummyKeywords(): List<LatestKeywordResponse> {
        return listOf(
            LatestKeywordResponse(count = 15, keyword = "제약바이오"),
            LatestKeywordResponse(count = 12, keyword = "반도체"),
            LatestKeywordResponse(count = 7, keyword = "변압기"),
            LatestKeywordResponse(count = 5, keyword = "화장품"),
            LatestKeywordResponse(count = 4, keyword = "철강")
        )
    }
    
    // 오늘의 이슈종목 로드
    private fun loadIssueStocks() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // API에서 이슈종목 로드
                Log.d("SearchActivity", "Fetching issue stocks from API...")
                val response = RetrofitClient.apiService.getIssueStocks()
                
                // 최신 날짜 찾기 (API 응답에서 가장 최근 날짜)
                val latestDate = response.stocks.maxByOrNull { it.date }?.date
                
                // 최신 날짜의 종목만 필터링
                val filteredStocks = if (latestDate != null) {
                    response.stocks.filter { stock ->
                        stock.date == latestDate
                    }
                } else {
                    response.stocks // 날짜가 없으면 전체 데이터 사용
                }
                
                // 로그 추가
                Log.d("SearchActivity", "API Response received - total stocks: ${response.stocks.size}")
                Log.d("SearchActivity", "Latest date found: $latestDate")
                Log.d("SearchActivity", "Filtered stocks for latest date: ${filteredStocks.size}")
                
                            withContext(Dispatchers.Main) {
                // UI 업데이트
                if (filteredStocks.isNotEmpty()) {
                    Log.d("SearchActivity", "Updating UI with ${filteredStocks.size} stocks for date: $latestDate")
                    // 등락률 기준으로 내림차순 정렬 (높은 등락률 순)
                    val sortedStocks = filteredStocks.sortedByDescending { it.riseRate }
                    // 데이터 확인을 위한 로그 추가
                    for (i in 0 until minOf(5, sortedStocks.size)) {
                        Log.d("SearchActivity", "Stock $i: ${sortedStocks[i].stockName}, riseRate: ${sortedStocks[i].riseRate}, date: ${sortedStocks[i].date}")
                    }
                    issueStockAdapter.updateStocks(sortedStocks)
                    // 어댑터 업데이트 후 아이템 개수 확인
                    Log.d("SearchActivity", "Adapter item count after update: ${issueStockAdapter.itemCount}")
                } else {
                    // 데이터가 없는 경우 임시 데이터 사용
                    Log.d("SearchActivity", "No stocks available, using dummy data")
                    issueStockAdapter.updateStocks(getDummyIssueStocks())
                }
                }
            } catch (e: Exception) {
                Log.e("SearchActivity", "Error loading issue stocks: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    // 오류 발생 시 임시 데이터 사용
                    issueStockAdapter.updateStocks(getDummyIssueStocks())
                }
            }
        }
    }
    
    // 오늘의 이슈종목 더미 데이터
    private fun getDummyIssueStocks(): List<IssueStockResponse> {
        val dummyStocks = listOf(
            IssueStockResponse(
                id = 25326,
                tradingAmount = "555억",
                volume = "350만",
                date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                riseRate = 0.2998,
                riseReason = "[이재명] 장동의 사외이사, 이재명 대통령 후보 미래기술 특보로 임명 소식에 상한가",
                stockName = "유진로봇",
                stockCode = "056080",
                theme = ""
            ),
            IssueStockResponse(
                id = 25336,
                tradingAmount = "1088억",
                volume = "228만",
                date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                riseRate = 0.1478,
                riseReason = "[풍력] 이재명 정책테마로 친환경 관련주 중심으로 테마, 섹터 강세 흐름",
                stockName = "씨에스윈드",
                stockCode = "112610",
                theme = ""
            ),
            IssueStockResponse(
                id = 25335,
                tradingAmount = "1594억",
                volume = "5471만",
                date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                riseRate = 0.1815,
                riseReason = "[이재명] 강충경 프롬바이오 이사, 이재명 대선캠프 합류",
                stockName = "프롬바이오",
                stockCode = "377220",
                theme = ""
            ),
            // 더 많은 더미 데이터 추가
            IssueStockResponse(
                id = 25340,
                tradingAmount = "2250억",
                volume = "480만",
                date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                riseRate = 0.1265,
                riseReason = "[반도체] TSMC 호실적에 동반 상승",
                stockName = "삼성전자",
                stockCode = "005930",
                theme = "반도체"
            ),
            IssueStockResponse(
                id = 25341,
                tradingAmount = "1870억",
                volume = "320만",
                date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                riseRate = 0.1542,
                riseReason = "[2차전지] 글로벌 수주 소식에 상승세",
                stockName = "LG에너지솔루션",
                stockCode = "373220",
                theme = "2차전지"
            ),
            IssueStockResponse(
                id = 25342,
                tradingAmount = "982억",
                volume = "215만",
                date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                riseRate = 0.0984,
                riseReason = "[AI] 엔비디아 협력 소식에 상승",
                stockName = "네이버",
                stockCode = "035420",
                theme = "AI"
            ),
            IssueStockResponse(
                id = 25343,
                tradingAmount = "765억",
                volume = "189만",
                date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                riseRate = 0.1127,
                riseReason = "[게임] 신작 발표 호재로 상승",
                stockName = "넷마블",
                stockCode = "251270",
                theme = "게임"
            ),
            IssueStockResponse(
                id = 25344,
                tradingAmount = "845억",
                volume = "167만",
                date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                riseRate = 0.0856,
                riseReason = "[제약] 신약 개발 중간 결과 호조로 상승",
                stockName = "셀트리온",
                stockCode = "068270",
                theme = "제약바이오"
            ),
            IssueStockResponse(
                id = 25345,
                tradingAmount = "678억",
                volume = "142만",
                date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                riseRate = 0.1633,
                riseReason = "[로봇] 산업용 로봇 수출 증가 소식에 강세",
                stockName = "현대로보틱스",
                stockCode = "267250",
                theme = "로봇"
            ),
            IssueStockResponse(
                id = 25346,
                tradingAmount = "521억",
                volume = "98만",
                date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                riseRate = 0.1245,
                riseReason = "[금융] 금리 인하 기대감에 상승",
                stockName = "KB금융",
                stockCode = "105560",
                theme = "금융"
            )
        )
        
        // 등락률 기준으로 내림차순 정렬하여 반환 (높은 등락률 순)
        return dummyStocks.sortedByDescending { it.riseRate }
    }
} 