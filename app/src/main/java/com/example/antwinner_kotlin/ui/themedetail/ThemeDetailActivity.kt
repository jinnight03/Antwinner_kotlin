package com.example.antwinner_kotlin.ui.themedetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.model.Company
import com.example.antwinner_kotlin.model.ThemeResponse
import com.example.antwinner_kotlin.model.ThemeStock
import com.example.antwinner_kotlin.network.RetrofitClient
import com.example.antwinner_kotlin.ui.theme.adapter.ThemeDetailStockAdapter
import com.example.antwinner_kotlin.util.NetworkUtil
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.example.antwinner_kotlin.model.KeywordCountResponse
import androidx.core.content.ContextCompat
import com.example.antwinner_kotlin.model.DailyCount
import java.util.Calendar
import android.graphics.Color
import com.example.antwinner_kotlin.model.BracketKeywordResponse
import com.example.antwinner_kotlin.model.ThemeSurgeReason
import com.example.antwinner_kotlin.ui.themedetail.adapter.ThemeSurgeReasonNewAdapter
import com.example.antwinner_kotlin.ui.themedetail.adapter.ThemeSurgeReasonWhyRiseAdapter
import java.util.UUID
import com.example.antwinner_kotlin.ui.components.SimpleBarChartView
import com.example.antwinner_kotlin.model.ThemeRankInfo
import com.example.antwinner_kotlin.utils.SystemBarUtils
import com.example.antwinner_kotlin.model.ThemeDetailResponse
import com.example.antwinner_kotlin.model.WeeklyRankingResponse
import androidx.cardview.widget.CardView
import com.example.antwinner_kotlin.ui.news.model.NewsResponse
import com.example.antwinner_kotlin.ui.themedetail.adapter.RelatedNewsAdapter
import android.net.Uri

class ThemeDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var backButton: ImageView
    private lateinit var searchButton: ImageView
    private lateinit var themeNameTextView: TextView
    private lateinit var themeInfoTextView: TextView
    private lateinit var themeRateTextView: TextView
    private lateinit var themeIconImageView: ImageView
    private lateinit var themeIssueTitleTextView: TextView
    private lateinit var periodChipGroup: ChipGroup
    private lateinit var chartSubtitleTextView: TextView
    private lateinit var barChart: SimpleBarChartView
    private lateinit var themeStocksTitleTextView: TextView
    private lateinit var themeStocksCountTextView: TextView
    private lateinit var themeStocksRecyclerView: RecyclerView
    private lateinit var toggleStocksButton: TextView
    private lateinit var surgeReasonTitleTextView: TextView
    private lateinit var surgeReasonRecyclerView: RecyclerView
    private lateinit var toggleSurgeReasonButton: TextView
    private lateinit var contentScrollView: View
    private lateinit var noDataLayout: View
    private lateinit var noDataTextView: TextView
    private lateinit var themeRankTextView: TextView
    private lateinit var themeRankCard: CardView
    private lateinit var relatedNewsRecyclerView: RecyclerView
    private lateinit var toggleNewsButton: TextView
    
    private lateinit var themeStocksAdapter: ThemeDetailStockAdapter
    private lateinit var surgeReasonAdapter: ThemeSurgeReasonWhyRiseAdapter
    private lateinit var relatedNewsAdapter: RelatedNewsAdapter

    private var themeId: String = ""
    private var themeName: String = ""
    private var fullDailyCounts: List<DailyCount> = emptyList()
    private var currentPeriod = "6m"
    private var chartBaseTimestamp: Long = 0L
    private var fullStockList: List<ThemeStock> = emptyList()
    private var isStockListExpanded = false
    private val maxInitialStocks = 5
    private var fullSurgeReasonList: List<ThemeSurgeReason> = emptyList()
    private var currentSurgeReasonCount = 3 // 현재 표시 중인 급등 이유 개수
    private val maxInitialSurgeReasons = 3
    private val surgeReasonPageSize = 10 // 더보기 클릭 시 추가되는 개수
    private var fullNewsList: List<NewsResponse> = emptyList()
    private var isNewsListExpanded = false
    private val maxInitialNews = 3

    companion object {
        private const val EXTRA_THEME_ID = "extra_theme_id"
        private const val EXTRA_THEME_NAME = "extra_theme_name"

        fun newIntent(context: Context, themeId: String, themeName: String): Intent {
            return Intent(context, ThemeDetailActivity::class.java).apply {
                putExtra(EXTRA_THEME_ID, themeId)
                putExtra(EXTRA_THEME_NAME, themeName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_detail)

        // 시스템 바 인셋 적용
        applySystemBarInsets()

        themeId = intent.getStringExtra(EXTRA_THEME_ID) ?: ""
        themeName = intent.getStringExtra(EXTRA_THEME_NAME) ?: ""

        if (themeName.isEmpty()) {
            Toast.makeText(this, "테마 정보가 없습니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        
        setupListeners()
        
        loadThemeData()
        
        Timber.e("---- 여기 실행됨: ThemeDetailActivity.onCreate 끝 ----")
    }

    private fun applySystemBarInsets() {
        val appBarLayout = findViewById<View>(R.id.toolbar).parent as View
        SystemBarUtils.applyTopPaddingForAppBar(appBarLayout, 16)
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        backButton = findViewById(R.id.iv_back)
        searchButton = findViewById(R.id.iv_search)
        themeNameTextView = findViewById(R.id.tv_theme_name)
        themeInfoTextView = findViewById(R.id.tv_theme_info)
        themeRateTextView = findViewById(R.id.tv_theme_rate)
        themeIconImageView = findViewById(R.id.iv_theme_icon)
        themeIssueTitleTextView = findViewById(R.id.tv_theme_issue_title)
        periodChipGroup = findViewById(R.id.period_chip_group)
        chartSubtitleTextView = findViewById(R.id.tv_chart_subtitle)
        barChart = findViewById(R.id.bar_chart_theme_trend)
        themeStocksTitleTextView = findViewById(R.id.tv_theme_stocks_title_new)
        themeStocksCountTextView = findViewById(R.id.tv_theme_stocks_count_new)
        themeStocksRecyclerView = findViewById(R.id.rv_theme_stocks_new)
        toggleStocksButton = findViewById(R.id.btn_toggle_stocks)
        surgeReasonTitleTextView = findViewById(R.id.tv_surge_reason_title)
        surgeReasonRecyclerView = findViewById(R.id.rv_surge_reasons)
        toggleSurgeReasonButton = findViewById(R.id.btn_toggle_surge_reasons)
        contentScrollView = findViewById(R.id.content_scroll_view)
        noDataLayout = findViewById(R.id.layout_no_data)
        noDataTextView = findViewById(R.id.tv_no_data_message)
        themeRankTextView = findViewById(R.id.tv_theme_rank)
        themeRankCard = findViewById(R.id.card_theme_rank)
        relatedNewsRecyclerView = findViewById(R.id.rv_related_news)
        toggleNewsButton = findViewById(R.id.btn_toggle_news)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        themeNameTextView.text = themeName
        themeInfoTextView.text = "로딩 중..."
        themeIssueTitleTextView.text = "$themeName 테마 이슈 추세"
        themeStocksTitleTextView.text = "$themeName 테마주"
        
        themeStocksAdapter = ThemeDetailStockAdapter(emptyList())
        themeStocksRecyclerView.layoutManager = LinearLayoutManager(this)
        themeStocksRecyclerView.adapter = themeStocksAdapter
        
        surgeReasonAdapter = ThemeSurgeReasonWhyRiseAdapter(emptyList())
        surgeReasonRecyclerView.layoutManager = LinearLayoutManager(this)
        surgeReasonRecyclerView.adapter = surgeReasonAdapter
        surgeReasonRecyclerView.isNestedScrollingEnabled = false
        
        relatedNewsAdapter = RelatedNewsAdapter(emptyList())
        relatedNewsRecyclerView.layoutManager = LinearLayoutManager(this)
        relatedNewsRecyclerView.adapter = relatedNewsAdapter
        relatedNewsRecyclerView.isNestedScrollingEnabled = false
        
        loadThemeIcon(themeName)
        
        chartSubtitleTextView.text = "차트 데이터 로딩 중..."
        
        try {
            periodChipGroup.check(R.id.chip_6m) 
        } catch (e: Exception) {
            Timber.e(e, "초기 칩 선택 실패: R.id.chip_6m 을 찾을 수 없거나 다른 오류 발생")
            if (periodChipGroup.childCount > 0) {
                 val firstChipId = periodChipGroup.getChildAt(periodChipGroup.childCount - 1).id
                 if(firstChipId != View.NO_ID) periodChipGroup.check(firstChipId)
            }
        }
        
        updateChartSubtitle(currentPeriod, 0)
        surgeReasonTitleTextView.text = "$themeName 급등 이유"
        
        // 초기에는 순위 정보 숨기기
        themeRankCard.visibility = View.GONE
        themeRankTextView.visibility = View.GONE
    }
    
    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }
        
        searchButton.setOnClickListener {
            val intent = com.example.antwinner_kotlin.ui.search.SearchActivity.newIntent(this)
            startActivity(intent)
        }
        
        periodChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChipId = checkedIds[0]
                val selectedChip = findViewById<Chip>(selectedChipId)
                val newPeriod = when (selectedChip.text.toString()) {
                    "1주" -> "1w"
                    "1달" -> "1m"
                    "6달" -> "6m"
                    "1년" -> "1y"
                    "전체" -> "all"
                    else -> currentPeriod
                }

                if (newPeriod != currentPeriod) {
                    Timber.d("기간 변경 감지: $currentPeriod -> $newPeriod")
                    currentPeriod = newPeriod
                    
                    // 해당 기간의 이슈 횟수 계산
                    updateChart(currentPeriod)
                    
                    // 선택된 기간에 맞는 이슈 횟수 계산
                    val periodCount = calculatePeriodCount(currentPeriod, fullDailyCounts)
                    updateChartSubtitle(currentPeriod, periodCount)
                } else {
                    Timber.d("기간 변경 없음 ($currentPeriod)")
                }
            } else {
                Timber.w("선택된 기간 칩이 없습니다.")
            }
        }
        
        toggleStocksButton.setOnClickListener {
            isStockListExpanded = !isStockListExpanded
            updateDisplayStockList()
        }
        
        toggleSurgeReasonButton.setOnClickListener {
            // 더보기 버튼을 클릭할 때마다 10개씩 추가
            if (currentSurgeReasonCount < fullSurgeReasonList.size) {
                currentSurgeReasonCount = Math.min(currentSurgeReasonCount + surgeReasonPageSize, fullSurgeReasonList.size)
                updateDisplaySurgeReasonList()
            } else {
                // 모두 표시 중일 때는 다시 초기 상태로 접기
                currentSurgeReasonCount = maxInitialSurgeReasons
                updateDisplaySurgeReasonList()
            }
        }
        
        toggleNewsButton.setOnClickListener {
            // 더보기 버튼 클릭 시 브라우저로 검색 결과 페이지 열기
            val searchUrl = "https://search.naver.com/search.naver?where=news&query=${java.net.URLEncoder.encode(themeName, "UTF-8")}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
            startActivity(intent)
        }
        
        relatedNewsAdapter.setOnNewsClickListener { news ->
            // 뉴스 클릭 시 해당 뉴스 링크로 이동
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(news.link))
            startActivity(intent)
        }
        
        relatedNewsAdapter.setOnStockClickListener { stockName ->
            // 종목 클릭 시 종목 상세 화면으로 이동
            val intent = com.example.antwinner_kotlin.ui.stockdetail.StockDetailActivity.newIntent(
                this,
                stockName.trim(),
                "" // 종목코드는 빈 문자열로 전달 (뉴스 데이터에 종목코드가 없음)
            )
            startActivity(intent)
        }
    }

    private fun loadThemeData() {
        Timber.e("---- 여기 실행됨: ThemeDetailActivity.loadThemeData 시작 ----")
        if (!NetworkUtil.isNetworkAvailable(this)) {
            showErrorState(getString(R.string.network_error_loading_theme))
            return
        }

        contentScrollView.visibility = View.VISIBLE
        noDataLayout.visibility = View.GONE
        
        Timber.d("테마 데이터 로드 시작: ID='$themeId', 이름='$themeName'")
        
        lifecycleScope.launch {
            try {
                Timber.d("테마 상세 정보 API 호출 시작")
                val themeDetailResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getThemaRate(themeName)
                }
                
                val keywordCountResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getKeywordCount(themeName)
                }
                
                val bracketKeywordResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getBracketKeywords(themeName)
                }
                
                // 새로 추가: 테마 순위 정보 API 호출
                val themeIssueDetailResponse = withContext(Dispatchers.IO) {
                    try {
                        RetrofitClient.apiService.getThemeIssueDetail(themeName)
                    } catch (e: Exception) {
                        Timber.e(e, "테마 이슈 상세 정보 로드 중 오류")
                        null
                    }
                }
                
                // 주간 순위 확인 API 호출
                val weeklyRankingResponse = withContext(Dispatchers.IO) {
                    try {
                        RetrofitClient.apiService.getWeeklyThemeRanking(7)
                    } catch (e: Exception) {
                        Timber.e(e, "주간 순위 정보 로드 중 오류")
                        null
                    }
                }
                
                // 관련 뉴스 API 호출
                val relatedNewsResponse = withContext(Dispatchers.IO) {
                    try {
                        RetrofitClient.apiService.getNewsForKeyword(themeName)
                    } catch (e: Exception) {
                        Timber.e(e, "관련 뉴스 로드 중 오류")
                        emptyList<NewsResponse>()
                    }
                }
                
                Timber.d("모든 API 호출 완료")
                
                if (themeDetailResponse != null) {
                    // 테마 상세 정보 업데이트
                    val averageRate = themeDetailResponse.averageRate
                    if (themeDetailResponse.name.isNullOrEmpty() || themeDetailResponse.companies.isNullOrEmpty() || averageRate.isNullOrEmpty()) {
                        Timber.w("API 응답 데이터가 유효하지 않습니다 (getThemaRate). 테마명: ${themeDetailResponse.name}, 종목 수: ${themeDetailResponse.companies?.size}, 등락률: $averageRate")
                        showErrorState(getString(R.string.theme_info_not_found))
                        return@launch
                    }
                    
                    // 테마 순위 정보 업데이트 (주간 우선순위 적용)
                    val weeklyRank = getWeeklyRank(weeklyRankingResponse, themeName)
                    
                    when {
                        weeklyRank in 1..3 -> {
                            // 주간 상위 3위면 주간 순위 표시
                            Timber.d("주간 상위 3위 발견: $weeklyRank 위")
                            updateThemeRankInfo(weeklyRank, isWeekly = true)
                        }
                        themeIssueDetailResponse?.rankInfo != null -> {
                            // 주간 상위 3위가 아니면 월간 순위 표시
                            val period = themeIssueDetailResponse.period
                            Timber.d("API에서 받은 period 값: '$period'")
                            Timber.d("순위 정보: rank=${themeIssueDetailResponse.rankInfo.rank}, totalThemas=${themeIssueDetailResponse.rankInfo.totalThemas}")
                            updateThemeRankInfo(themeIssueDetailResponse.rankInfo, period)
                        }
                        else -> {
                            // 순위 정보가 없으면 숨김
                            themeRankCard.visibility = View.GONE
                            themeRankTextView.visibility = View.GONE
                        }
                    }
                    
                    // UI 업데이트
                    updateUIWithCombinedData(themeDetailResponse, keywordCountResponse.totalCount ?: 0, averageRate)
                    
                    // 급등 이유 섹션 업데이트
                    updateSurgeReasonSection(bracketKeywordResponse)
                    
                    // 차트 데이터 업데이트
                    if (keywordCountResponse.dailyCounts != null) {
                        // null이 아닌 데이터만 필터링하고 API 응답 형식에 맞게 매핑
                        fullDailyCounts = keywordCountResponse.dailyCounts.mapNotNull { dailyCount ->
                            val date = dailyCount.date
                            val count = dailyCount.count
                            if (date != null && count != null) {
                                DailyCount(date = date, count = count)
                            } else {
                                null
                            }
                        }
                        
                        Timber.d("차트 데이터 설정 완료: ${fullDailyCounts.size}개")
                        if (fullDailyCounts.isNotEmpty()) {
                            Timber.d("차트 데이터 샘플: 날짜=${fullDailyCounts[0].date}, 카운트=${fullDailyCounts[0].count}")
                        }
                        updateChart(currentPeriod)
                    } else {
                        fullDailyCounts = emptyList()
                        Timber.w("차트 데이터(dailyCounts) 없음")
                    }
                    
                    // 차트 부제목 업데이트 - 선택된 기간에 맞는 이슈 횟수 계산
                    val periodCount = calculatePeriodCount(currentPeriod, fullDailyCounts)
                    updateChartSubtitle(currentPeriod, periodCount)
                    
                    // 관련 뉴스 섹션 업데이트
                    updateRelatedNewsSection(relatedNewsResponse)
                } else {
                    Timber.e("테마 상세 정보 응답이 없습니다")
                    showErrorState(getString(R.string.theme_info_not_found))
                }
                
            } catch (e: Exception) {
                Timber.e(e, "테마 상세 정보 로드 중 오류 발생")
                showErrorState(getString(R.string.theme_info_not_found))
            }
        }
    }
    
    private fun updateUIWithCombinedData(themeResponse: ThemeResponse, issueCount: Int, averageRate: String) {
        Timber.e("---- 여기 실행됨: ThemeDetailActivity.updateUIWithCombinedData 시작 (종목 수: ${themeResponse.companies.size}) ----")
        // API 응답 데이터 로깅 (stockCode 확인용)
        Timber.d("updateUIWithCombinedData: 테마명=${themeResponse.name}")
        themeResponse.companies.forEachIndexed { index, company ->
            Timber.d("  Company[$index]: name=${company.name}, stockCode=${company.stockCode}")
        }

        val rateStr = averageRate.takeIf { it.isNotEmpty() } ?: "0.0%"
        
        val rate = try {
            rateStr.replace("%", "").replace(",", "").toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            Timber.e(e, "등락률 파싱 중 오류: $rateStr")
            0.0
        }
        
        themeRateTextView.text = String.format("%+.2f%%", rate)
        
        if (rate > 0) {
            themeRateTextView.setTextColor(getColor(R.color.rising_color))
        } else if (rate < 0) {
            themeRateTextView.setTextColor(getColor(R.color.falling_color))
        } else {
            themeRateTextView.setTextColor(getColor(R.color.market_neutral))
        }
        
        val stocksCount = themeResponse.companies.size
        themeInfoTextView.text = "${stocksCount}개 테마주 · 이슈 ${issueCount}건"
        themeStocksCountTextView.text = "총 ${stocksCount}개"
        
        fullStockList = themeResponse.companies.map { company ->
            val tradingAmount = generateRandomTradingAmount()
            
            val logoUrl = try {
                val rawStockCode = company.stockCode
                val stockCode = company.stockCode?.trim()
                Timber.e("종목 로고 URL 생성 시도: 종목='${company.name}', 원본 코드='${rawStockCode}', 처리된 코드='${stockCode}'")
                var isValid = false
                var reason = ""
                if (stockCode == null) {
                    reason = "null"
                } else if (stockCode.isBlank()) {
                    reason = "blank"
                } else if (stockCode.equals("N/A", ignoreCase = true) || stockCode.equals("NA", ignoreCase = true)) {
                    reason = "N/A 값"
                } else if (stockCode.length != 6) {
                    reason = "길이 오류 (${stockCode.length}자리)"
                } else if (!stockCode.matches(Regex("^[0-9]{6}$"))) {
                    reason = "형식 오류 (6자리 숫자가 아님)"
                } else {
                    isValid = true
                }
                if (isValid) {
                    val finalUrl = "https://antwinner.com/api/stock_logos/${stockCode}"
                    Timber.e("로고 URL 생성 성공: 종목='${company.name}', URL='$finalUrl'")
                    finalUrl
                } else {
                    Timber.e("유효하지 않은 종목코드 ($reason): 종목='${company.name}', 코드='$stockCode' -> 빈 URL 사용")
                    ""
                }
            } catch (e: Exception) {
                Timber.e(e, "로고 URL 생성 중 예외 발생: ${company.name}")
                ""
            }
            
            ThemeStock(
                id = company.name,
                name = company.name,
                price = parsePriceString(company.currentPrice),
                changeRate = company.fluctuation.replace("%", "").replace(",", "").toDoubleOrNull() ?: 0.0,
                tradingAmount = tradingAmount,
                logoUrl = logoUrl,
                stockCode = company.stockCode?.trim() ?: ""
            )
        }
        
        isStockListExpanded = false
        updateDisplayStockList()
    }

    private fun updateSurgeReasonSection(apiResponse: List<BracketKeywordResponse>) {
        Timber.d("updateSurgeReasonSection 시작: ${apiResponse.size}개 항목")
        val surgeReasons = apiResponse.mapNotNull { item ->
            var extractedThemeName = themeName
            var cleanedReasonTitle = item.reason ?: ""
            val reasonText = item.reason

            if (!reasonText.isNullOrBlank()) {
                val regex = Regex("^\\\\\\[(.*?)\\\\\\]\\\\s*")
                val matchResult = regex.find(reasonText)
                if (matchResult != null) {
                    extractedThemeName = matchResult.groupValues[1]
                    cleanedReasonTitle = reasonText.replaceFirst(regex, "")
                    Timber.d("상승이유 파싱: 원본='${reasonText}', 추출된 테마='${extractedThemeName}', 정리된 제목='${cleanedReasonTitle}'")
                } else {
                     Timber.d("상승이유 파싱 실패 (대괄호 없음): '${reasonText}'")
                     extractedThemeName = item.theme?.takeIf { it.isNotBlank() } ?: themeName
                }
            }

            ThemeSurgeReason(
                id = item.id?.toString() ?: UUID.randomUUID().toString(),
                relatedStockName = item.stockName,
                relatedStockRate = item.fluctuationRate,
                themeName = extractedThemeName,
                reasonTitle = cleanedReasonTitle,
                date = formatDateString(item.date),
                tradingVolume = item.tradingVolume ?: "-",
                tradingValue = item.tradingValue ?: "-"
            )
        }
        fullSurgeReasonList = surgeReasons
        currentSurgeReasonCount = maxInitialSurgeReasons // 초기화
        updateDisplaySurgeReasonList()
        
        Timber.d("급등 이유 전체 리스트 저장 및 표시 업데이트 완료: ${fullSurgeReasonList.size}개 항목")
    }

    private fun updateRelatedNewsSection(newsResponse: List<NewsResponse>) {
        Timber.d("updateRelatedNewsSection 시작: ${newsResponse.size}개 뉴스")
        
        fullNewsList = newsResponse
        
        if (fullNewsList.isNotEmpty()) {
            // 뉴스가 있으면 최대 3개까지만 표시
            val displayList = fullNewsList.take(maxInitialNews)
            relatedNewsAdapter.updateNews(displayList)
            
            // 더보기 버튼 표시 (항상 표시하여 네이버 뉴스 검색으로 연결)
            toggleNewsButton.visibility = View.VISIBLE
            toggleNewsButton.text = "검색 결과 더보기 >"
            
            Timber.d("관련 뉴스 표시 완료: ${displayList.size}개 표시, 전체 ${fullNewsList.size}개")
        } else {
            // 뉴스가 없어도 더보기 버튼은 표시 (네이버 뉴스 검색으로 연결)
            relatedNewsAdapter.updateNews(emptyList())
            toggleNewsButton.visibility = View.VISIBLE
            toggleNewsButton.text = "관련 뉴스 검색하기 >"
            
            Timber.d("관련 뉴스 없음, 검색 버튼만 표시")
        }
    }

    private fun formatDateString(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val outputFormat = SimpleDateFormat("yyyy. MM. dd", Locale.KOREA)
            val date = inputFormat.parse(dateStr)
            if (date != null) outputFormat.format(date) else dateStr
        } catch (e: Exception) {
            Timber.e(e, "날짜 형식 변환 오류: $dateStr")
            dateStr
        }
    }
    
    private fun parsePriceString(priceStr: String): Int {
        return priceStr.replace(",", "").replace("원", "").toIntOrNull() ?: 0
    }
    
    private fun generateRandomTradingAmount(): Long {
        return (100_000_000L..100_000_000_000L).random()
    }
    
    private fun updateDisplayStockList() {
        val displayList = if (isStockListExpanded || fullStockList.size <= maxInitialStocks) {
            fullStockList
        } else {
            fullStockList.take(maxInitialStocks)
        }
        
        themeStocksAdapter = ThemeDetailStockAdapter(displayList)
        themeStocksAdapter.setOnItemClickListener { stock ->
            val intent = com.example.antwinner_kotlin.ui.stockdetail.StockDetailActivity.newIntent(
                this,
                stock.name,
                stock.stockCode
            )
            startActivity(intent)
        }
        themeStocksRecyclerView.adapter = themeStocksAdapter
        
        if (fullStockList.size > maxInitialStocks) {
            toggleStocksButton.visibility = View.VISIBLE
            if (isStockListExpanded) {
                toggleStocksButton.text = getString(R.string.show_less)
            } else {
                toggleStocksButton.text = getString(R.string.show_more_count, fullStockList.size - maxInitialStocks)
            }
        } else {
            toggleStocksButton.visibility = View.GONE
        }
    }
    
    private fun updateDisplaySurgeReasonList() {
        val displayList = if (currentSurgeReasonCount <= maxInitialSurgeReasons || fullSurgeReasonList.size <= maxInitialSurgeReasons) {
            // 초기 상태 또는 전체 항목이 3개 이하인 경우
            fullSurgeReasonList.take(maxInitialSurgeReasons)
        } else {
            // 더보기 상태
            fullSurgeReasonList.take(currentSurgeReasonCount)
        }
        
        surgeReasonAdapter = ThemeSurgeReasonWhyRiseAdapter(displayList)
        surgeReasonRecyclerView.adapter = surgeReasonAdapter
        
        // 더보기 버튼 텍스트 및 가시성 업데이트
        if (fullSurgeReasonList.size > maxInitialSurgeReasons) {
            toggleSurgeReasonButton.visibility = View.VISIBLE
            
            if (currentSurgeReasonCount >= fullSurgeReasonList.size) {
                // 모두 표시 중일 때
                toggleSurgeReasonButton.text = getString(R.string.show_less)
            } else {
                // 일부만 표시 중일 때
                val remainingCount = fullSurgeReasonList.size - currentSurgeReasonCount
                val showCount = Math.min(remainingCount, surgeReasonPageSize)
                toggleSurgeReasonButton.text = getString(R.string.show_more_count_paged, showCount)
            }
        } else {
            toggleSurgeReasonButton.visibility = View.GONE
        }
    }
    
    private fun showErrorState(message: String) {
        runOnUiThread {
            contentScrollView.visibility = View.GONE
            noDataLayout.visibility = View.VISIBLE
            noDataTextView.text = message
        }
    }

    private fun loadThemeIcon(themeName: String) {
        try {
            val encodedName = java.net.URLEncoder.encode(themeName, "UTF-8")
            val iconUrl = "https://antwinner.com/api/image/${encodedName}.png"
            
            Glide.with(this)
                .load(iconUrl)
                .apply(RequestOptions()
                    .placeholder(R.drawable.ic_theme_default)
                    .error(R.drawable.ic_theme_default)
                    .centerCrop()
                )
                .into(themeIconImageView)
        } catch (e: Exception) {
            themeIconImageView.setImageResource(R.drawable.ic_theme_default)
        }
    }

    private fun updateChartSubtitle(period: String, count: Int) {
        val timeframeText = when (period) {
            "1w" -> "1주"
            "1m" -> "1개월"
            "6m" -> "6개월"
            "1y" -> "1년"
            "all" -> "전체 기간"
            else -> "전체 기간"
        }
        
        chartSubtitleTextView.text = "$timeframeText 이슈 횟수 ${count}회"
    }
    
    /**
     * 선택된 기간에 맞는 이슈 횟수를 계산하는 함수
     */
    private fun calculatePeriodCount(period: String, dailyCounts: List<DailyCount>): Int {
        if (dailyCounts.isEmpty()) return 0
        
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time
        
        // 선택된 기간의 시작 날짜 계산
        val startDate = when (period) {
            "1w" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.time
            }
            "1m" -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.time
            }
            "6m" -> {
                calendar.add(Calendar.MONTH, -6)
                calendar.time
            }
            "1y" -> {
                calendar.add(Calendar.YEAR, -1)
                calendar.time
            }
            else -> null // "all" 경우 모든 데이터 사용
        }
        
        // 해당 기간의 이슈 횟수 합산
        return if (startDate != null) {
            dailyCounts.sumOf { dailyCount ->
                val date = parseDate(dailyCount.date)
                if (date != null && (date.after(startDate) || isSameDay(date, startDate)) && 
                    (date.before(currentDate) || isSameDay(date, currentDate))) {
                    dailyCount.count
                } else {
                    0
                }
            }
        } else {
            // "all" 기간인 경우 모든 이슈 합산
            dailyCounts.sumOf { it.count }
        }
    }
    
    private fun updateChart(period: String) {
        if (fullDailyCounts.isEmpty()) {
            Timber.d("실제 차트 데이터 없음, 테스트 데이터 사용")
            val testData = generateTestChartData()
            Timber.d("테스트 데이터 생성 완료: ${testData.size}개")
            barChart.setData(testData, period)
            Timber.d("솟아오르는 애니메이션과 함께 차트 업데이트 시작")
            return
        }
        
        // 실제 데이터로 차트 업데이트
        Timber.d("실제 데이터로 차트 업데이트: 기간=$period, 데이터=${fullDailyCounts.size}개")
        barChart.setData(fullDailyCounts, period)
        Timber.d("솟아오르는 애니메이션과 함께 차트 업데이트 시작")
    }
    
    // 테스트 데이터 생성 (실제 API에서 데이터가 없을 경우 사용)
    private fun generateTestChartData(): List<DailyCount> {
        // 이미지와 유사한 데이터 패턴 생성
        val cal = Calendar.getInstance()
        val data = mutableListOf<DailyCount>()
        
        // 9월 데이터
        cal.set(Calendar.MONTH, Calendar.SEPTEMBER)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        data.add(DailyCount(formatDate(cal), 1)) // 1일
        
        cal.set(Calendar.DAY_OF_MONTH, 5)
        data.add(DailyCount(formatDate(cal), 3)) // 5일
        
        cal.set(Calendar.DAY_OF_MONTH, 10)
        data.add(DailyCount(formatDate(cal), 5)) // 10일
        
        cal.set(Calendar.DAY_OF_MONTH, 15)
        data.add(DailyCount(formatDate(cal), 5)) // 15일
        
        cal.set(Calendar.DAY_OF_MONTH, 20)
        data.add(DailyCount(formatDate(cal), 2)) // 20일
        
        cal.set(Calendar.DAY_OF_MONTH, 25)
        data.add(DailyCount(formatDate(cal), 1)) // 25일
        
        // 10월과 11월은 빈 데이터
        
        // 12월 데이터
        cal.set(Calendar.MONTH, Calendar.DECEMBER)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        data.add(DailyCount(formatDate(cal), 1)) // 1일
        
        cal.set(Calendar.DAY_OF_MONTH, 5)
        data.add(DailyCount(formatDate(cal), 3)) // 5일
        
        cal.set(Calendar.DAY_OF_MONTH, 10)
        data.add(DailyCount(formatDate(cal), 3)) // 10일
        
        cal.set(Calendar.DAY_OF_MONTH, 15)
        data.add(DailyCount(formatDate(cal), 5)) // 15일
        
        cal.set(Calendar.DAY_OF_MONTH, 20)
        data.add(DailyCount(formatDate(cal), 5)) // 20일
        
        cal.set(Calendar.DAY_OF_MONTH, 25)
        data.add(DailyCount(formatDate(cal), 5)) // 25일
        
        // 1월 데이터
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        data.add(DailyCount(formatDate(cal), 3)) // 1일
        
        cal.set(Calendar.DAY_OF_MONTH, 5)
        data.add(DailyCount(formatDate(cal), 3)) // 5일
        
        cal.set(Calendar.DAY_OF_MONTH, 10)
        data.add(DailyCount(formatDate(cal), 5)) // 10일
        
        cal.set(Calendar.DAY_OF_MONTH, 15)
        data.add(DailyCount(formatDate(cal), 5)) // 15일
        
        cal.set(Calendar.DAY_OF_MONTH, 20)
        data.add(DailyCount(formatDate(cal), 3)) // 20일
        
        cal.set(Calendar.DAY_OF_MONTH, 25)
        data.add(DailyCount(formatDate(cal), 3)) // 25일
        
        // 2월 데이터
        cal.set(Calendar.MONTH, Calendar.FEBRUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        data.add(DailyCount(formatDate(cal), 3)) // 1일
        
        cal.set(Calendar.DAY_OF_MONTH, 5)
        data.add(DailyCount(formatDate(cal), 2)) // 5일
        
        cal.set(Calendar.DAY_OF_MONTH, 10)
        data.add(DailyCount(formatDate(cal), 1)) // 10일
        
        cal.set(Calendar.DAY_OF_MONTH, 15)
        data.add(DailyCount(formatDate(cal), 1)) // 15일
        
        cal.set(Calendar.DAY_OF_MONTH, 20)
        data.add(DailyCount(formatDate(cal), 3)) // 20일
        
        cal.set(Calendar.DAY_OF_MONTH, 25)
        data.add(DailyCount(formatDate(cal), 1)) // 25일
        
        return data
    }
    
    private fun formatDate(cal: Calendar): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }
    
    // 날짜 문자열을 Date 객체로 파싱하는 유틸리티 함수
    private fun parseDate(dateStr: String): Date? {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
        } catch (e: Exception) {
            Timber.e(e, "날짜 파싱 오류: $dateStr")
            null
        }
    }
    
    // 같은 날짜인지 확인하는 유틸리티 함수
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * 주간 순위에서 현재 테마의 순위를 확인하는 함수
     */
    private fun getWeeklyRank(weeklyResponse: WeeklyRankingResponse?, themeName: String): Int {
        weeklyResponse?.themaRanking?.forEachIndexed { index, ranking ->
            if (ranking.themeName == themeName) {
                return index + 1 // 배열 인덱스는 0부터 시작하므로 +1
            }
        }
        return -1 // 순위에 없음
    }

    /**
     * 테마 순위 정보를 UI에 업데이트하는 함수 (오버로드 - 주간 순위용)
     */
    private fun updateThemeRankInfo(rank: Int, isWeekly: Boolean) {
        if (rank <= 0) {
            themeRankCard.visibility = View.GONE
            themeRankTextView.visibility = View.GONE
            return
        }
        
        val rankText = if (isWeekly) {
            "주간 상승 ${rank}위 테마"
        } else {
            "월간 상승 ${rank}위 테마"
        }
        
        // hot 아이콘을 왼쪽에 표시
        themeRankTextView.text = rankText
        themeRankTextView.setTextColor(getColor(R.color.rising_color))
        themeRankTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_hot_rank, 0, 0, 0)
        themeRankTextView.compoundDrawablePadding = 8
        themeRankTextView.visibility = View.VISIBLE
        themeRankCard.visibility = View.GONE
    }

    /**
     * 테마 순위 정보를 UI에 업데이트하는 함수 (기존 - 월간 순위용)
     */
    private fun updateThemeRankInfo(rankInfo: ThemeRankInfo, period: String? = null) {
        val rank = rankInfo.rank ?: 0
        val totalThemas = rankInfo.totalThemas ?: 0
        
        if (rank <= 0 || totalThemas <= 0) {
            themeRankCard.visibility = View.GONE
            themeRankTextView.visibility = View.GONE
            return
        }
        
        themeRankCard.visibility = View.VISIBLE
        
        // 새로운 요구사항에 따른 월간 랭킹 표시 로직
        val rankText: String
        val textColor: Int
        val iconRes: Int
        
        when {
            rank <= 30 -> {
                // 월간 1~30위: hot 아이콘 + 빨간색
                rankText = "월간 상승 ${rank}위 테마"
                textColor = getColor(R.color.rising_color) // 빨간색
                iconRes = R.drawable.ic_hot_rank
            }
            else -> {
                // 월간 31위~꼴등: 하락 아이콘 + 파란색
                rankText = "월간 상승 ${rank}위 테마"
                textColor = Color.parseColor("#1A73E8") // 파란색
                iconRes = R.drawable.ic_down_rank
            }
        }
        
        // TextView에 직접 설정 (카드뷰는 숨김)
        themeRankTextView.text = rankText
        themeRankTextView.setTextColor(textColor)
        themeRankTextView.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0)
        themeRankTextView.compoundDrawablePadding = 8
        themeRankTextView.visibility = View.VISIBLE
        themeRankCard.visibility = View.GONE
    }
} 