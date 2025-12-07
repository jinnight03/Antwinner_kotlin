package com.example.antwinner_kotlin.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.TextView
import android.widget.Toast
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.repository.TrendRepository
import com.example.antwinner_kotlin.ui.home.adapter.HotThemeAdapter
import com.example.antwinner_kotlin.ui.home.adapter.ThemeAdapter
import com.example.antwinner_kotlin.ui.home.adapter.TreemapAdapter
import com.example.antwinner_kotlin.ui.home.adapter.TrendDayAdapter
import com.example.antwinner_kotlin.ui.home.adapter.PromisingThemeAdapter
import com.example.antwinner_kotlin.ui.home.adapter.TopRisingStockAdapter
import com.example.antwinner_kotlin.ui.home.layout.ThemeGridLayoutManager
import com.example.antwinner_kotlin.ui.home.layout.TreemapLayout
import com.example.antwinner_kotlin.ui.home.webview.TreemapWebView
import com.example.antwinner_kotlin.ui.home.model.HotTheme
import com.example.antwinner_kotlin.ui.home.model.MarketIndex
import com.example.antwinner_kotlin.ui.home.model.MarketIndexResponse
import com.example.antwinner_kotlin.ui.home.model.Theme
import com.example.antwinner_kotlin.ui.home.model.ThemeCompany
import com.example.antwinner_kotlin.ui.home.model.ThemeFluctuation
import com.example.antwinner_kotlin.ui.home.model.TopRisingStock
import com.example.antwinner_kotlin.ui.home.model.TrendDay
import com.example.antwinner_kotlin.ui.home.model.TrendKeywordResponse
import com.example.antwinner_kotlin.ui.home.model.TrendTheme
import com.example.antwinner_kotlin.ui.stocks.StockTabFragment
import com.example.antwinner_kotlin.ui.home.model.PromisingTheme
import com.example.antwinner_kotlin.util.NetworkUtil
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max
import android.graphics.Rect
import android.view.inputmethod.InputMethodManager
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.text.method.KeyListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.widget.HorizontalScrollView
import com.example.antwinner_kotlin.ui.home.adapter.TopRisingStocksPagerAdapter
import android.graphics.drawable.AnimationDrawable
import com.example.antwinner_kotlin.ui.theme.AllThemesActivity
import com.example.antwinner_kotlin.util.ThemeCache
import com.example.antwinner_kotlin.ui.themedetail.ThemeDetailActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import android.text.SpannableString
import android.text.Spanned
import java.util.Timer
import java.util.TimerTask
// AdFit SDK import
import com.kakao.adfit.ads.ba.BannerAdView
import com.kakao.adfit.ads.AdListener
// import androidx.compose.ui.platform.ComposeView
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.runtime.mutableStateOf
// import androidx.compose.runtime.remember
// import androidx.compose.runtime.getValue
// import androidx.compose.runtime.setValue
// import com.example.antwinner_kotlin.ui.home.compose.ThemeTreemap
// import com.example.antwinner_kotlin.ui.home.compose.ThemeDataConverter

class HomeFragment : Fragment() {

    // WebView 트리맵 사용 여부 플래그
    private val useWebViewTreemap = true
    
    private lateinit var treemapLayout: TreemapLayout
    private lateinit var treemapWebView: TreemapWebView
    // private lateinit var composeTreemapView: ComposeView
    private lateinit var hotThemeRecyclerView: RecyclerView
    private lateinit var trendsRecyclerView: RecyclerView
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var marketTickerView: TextView
    private lateinit var tvTime: TextView
    
    private lateinit var treemapAdapter: TreemapAdapter
    private lateinit var hotThemeAdapter: HotThemeAdapter
    
    // Compose Treemap 사용 여부 플래그 (true로 설정하면 새로운 Compose 버전 사용)
    private val useComposeTreemap = false // 임시로 비활성화
    
    private val trendRepository = TrendRepository()
    private val trendDayAdapter = TrendDayAdapter(emptyList())
    private lateinit var promisingThemeRecyclerView: RecyclerView
    private lateinit var promisingThemeAdapter: PromisingThemeAdapter

    // 상승 종목 관련 변수 추가
    private lateinit var topRisingStocksViewPager: ViewPager2
    private lateinit var topRisingStocksPagerAdapter: TopRisingStocksPagerAdapter
    private lateinit var chipWeekly: Chip
    private lateinit var chipMonthly: Chip
    private lateinit var chip3Months: Chip
    private lateinit var chip6Months: Chip
    private var currentPeriod: String = "1W" // 기본값은 주간 수익률

    // 필요한 변수 추가
    private lateinit var searchEditText: EditText
    private lateinit var searchIcon: ImageView

    // 데이터가 로드되었는지 추적하는 변수 추가 -> 로딩 상태 관리로 변경
    private var isLoading = false
    
    // 데이터 자동 갱신을 위한 변수들
    private var lastDataLoadTime: Long = 0L
    private val DATA_REFRESH_INTERVAL = 5 * 60 * 1000L // 5분 (밀리초)
    private var backgroundTime: Long = 0L
    private var autoRefreshTimer: Timer? = null

    // SwipeRefreshLayout 추가
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // SwipeRefreshLayout 초기화
            swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

            // 검색 관련 뷰 초기화
            searchEditText = view.findViewById(R.id.et_search)
            searchIcon = view.findViewById(R.id.iv_search)

            // 마켓 티커 설정
            marketTickerView = view.findViewById(R.id.tv_market_ticker)
            setupMarketTicker()
            
            // 트리맵 설정 (WebView 또는 기존 Layout)
            if (useWebViewTreemap) {
                treemapWebView = view.findViewById(R.id.treemap_webview)
                setupTreemapWebView()
            } else {
                treemapLayout = view.findViewById(R.id.treemap_layout)
                // composeTreemapView = view.findViewById(R.id.compose_treemap_view)
                setupTreemapLayout()
            }
            
            // AdFit 배너 광고 설정
            setupBannerAd(view)
            
            // 오늘 핫한 테마 RecyclerView 설정
            hotThemeRecyclerView = view.findViewById(R.id.rv_hot_themes)
            setupHotThemeRecyclerView()
            
            // 주식 탭 ViewPager 설정
            viewPager = view.findViewById(R.id.view_pager)
            tabLayout = view.findViewById(R.id.tab_layout)
            setupStockTabViewPager()
            
            // 상승 종목 관련 초기화
            topRisingStocksViewPager = view.findViewById(R.id.vp_top_rising_stocks)
            setupTopRisingStocks(view)
            
            // 오늘 투자 트렌드 RecyclerView 설정
            trendsRecyclerView = view.findViewById(R.id.rv_trends)
            setupTrendsRecyclerView()
            
            // 노려볼만한 테마 RecyclerView 설정
            promisingThemeRecyclerView = view.findViewById(R.id.rv_promising_themes)
            setupPromisingThemeRecyclerView()
            
            // 텍스트뷰 변수 선언 추가
            tvTime = view.findViewById(R.id.tv_time)
            updateReferenceTime()

            // 검색바 클릭 이벤트 설정
            searchIcon.setOnClickListener {
                // 검색 전용 화면으로 이동
                startActivity(com.example.antwinner_kotlin.ui.search.SearchActivity.newIntent(requireContext()))
            }

            // EditText 키 이벤트 설정
            searchEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // 검색 전용 화면으로 이동
                    startActivity(com.example.antwinner_kotlin.ui.search.SearchActivity.newIntent(requireContext()))
                    return@setOnEditorActionListener true
                }
                false
            }
            
            // 검색 텍스트 변경 리스너 추가 (자동완성 구현)
            searchEditText.addTextChangedListener(object : TextWatcher {
                private var searchJob: Job? = null
                
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                
                override fun afterTextChanged(s: Editable?) {
                    // 이전 검색 작업 취소
                    searchJob?.cancel()
                    
                    val query = s.toString().trim()
                    if (query.length >= 2) { // 2글자 이상일 때만 검색
                        searchJob = viewLifecycleOwner.lifecycleScope.launch {
                            delay(300) // 타이핑 중에 너무 많은 요청을 방지하기 위한 지연
                            // 검색 전용 화면으로 이동
                            activity?.runOnUiThread {
                                startActivity(com.example.antwinner_kotlin.ui.search.SearchActivity.newIntent(requireContext()))
                            }
                        }
                    }
                }
            })
            
            // 전체테마 보러가기 버튼 클릭 리스너 설정 추가
            val viewAllThemesButton = view.findViewById<View>(R.id.btn_view_all_themes)
            viewAllThemesButton.setOnClickListener {
                // 전체 테마 화면으로 이동
                val intent = Intent(activity, AllThemesActivity::class.java)
                startActivity(intent)
            }
            
            // SwipeRefreshLayout 리스너 설정
            swipeRefreshLayout.setOnRefreshListener {
                Log.d("HomeFragment", "Swipe to refresh triggered.")
                // 이미 로딩 중이면 무시
                if (!isLoading) {
                    loadData(isRefreshing = true)
                } else {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
            
            // 초기 데이터 로드 시작 - 백그라운드에서 실행 (새로고침 아님)
            loadData(isRefreshing = false)
            
        } catch (e: Exception) {
            // 오류 발생 시 사용자에게 알림
            Toast.makeText(context, "UI 로딩 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("HomeFragment", "Error loading UI", e)
            // 오류 발생 시 로딩 상태 및 새로고침 인디케이터 초기화
            isLoading = false
            if (::swipeRefreshLayout.isInitialized) {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
        
        // 데이터가 아직 로드되지 않은 경우에만 데이터 로드
        if (!isLoading) {
            loadData(isRefreshing = false)
        }
    }

    override fun onResume() {
        super.onResume()
        
        // 자동 스크롤 시작 - marketTickerView가 초기화되었는지 확인
        if (::marketTickerView.isInitialized) {
            marketTickerView.isSelected = true
        }
        
        // 데이터 자동 갱신 체크
        checkAndRefreshDataIfNeeded()
        
        // 주기적 자동 갱신 타이머 시작
        startAutoRefreshTimer()
        
        // 광고 재개
        bannerAdView?.resume()
    }
    
    override fun onPause() {
        super.onPause()
        // 자동 스크롤 중지 - marketTickerView가 초기화되었는지 확인
        if (::marketTickerView.isInitialized) {
            marketTickerView.isSelected = false
        }
        
        // 백그라운드 진입 시간 기록
        backgroundTime = System.currentTimeMillis()
        
        // 자동 갱신 타이머 중지
        stopAutoRefreshTimer()
        
        // 광고 일시정지
        bannerAdView?.pause()
    }
    
    private fun setupMarketTicker() {
        // 마켓 티커 설정
        // TextView의 isSelected를 true로 설정하면 marquee 효과가 활성화됨
        marketTickerView.isSelected = true
        
        // 기본 텍스트에서도 이미지 이모지 적용
        setupDefaultMarketTickerWithImages()
    }
    
    private fun setupDefaultMarketTickerWithImages() {
        // 기본 더미 텍스트 (이미지 이모지 적용)
        val dummyText = "원유 선물 ICON_DOWN 61.33 (-0.33%)    두바이유 선물 ICON_UP 67.265 (0.05%)    금($/온스) ICON_UP 3240.4 (0.44%)    은(COMEX) ICON_UP 32.297 (0.40%)    미국 (USD/KRW) ICON_DOWN 1416.3 (-0.85%)    미국 필라델피아 반도체 ICON_DOWN 3855.829 (-4.13%)    코스피 ICON_UP 2718.45 (0.54%)    코스닥 ICON_UP 866.91 (0.79%)    S&P 500 ICON_DOWN 5,204.35 (-0.34%)    나스닥 종합 ICON_DOWN 16,315.70 (-0.30%)"
        
        val spannableString = SpannableString(dummyText)
        
        // ICON_UP과 ICON_DOWN을 이미지로 교체
        var searchIndex = 0
        while (true) {
            val upIndex = spannableString.indexOf("ICON_UP", searchIndex)
            val downIndex = spannableString.indexOf("ICON_DOWN", searchIndex)
            
            val nextIndex = when {
                upIndex == -1 && downIndex == -1 -> break
                upIndex == -1 -> downIndex
                downIndex == -1 -> upIndex
                else -> kotlin.math.min(upIndex, downIndex)
            }
            
            val isUp = nextIndex == upIndex
            val iconRes = if (isUp) R.drawable.ic_market_up else R.drawable.ic_market_down
            val color = if (isUp) android.graphics.Color.RED else android.graphics.Color.BLUE
            val placeholder = if (isUp) "ICON_UP" else "ICON_DOWN"
            
            try {
                // 이미지 스팬 생성
                val drawable = requireContext().getDrawable(iconRes)
                drawable?.setBounds(0, 0, 32, 32)
                val imageSpan = android.text.style.ImageSpan(drawable!!, android.text.style.ImageSpan.ALIGN_BASELINE)
                
                // 이미지 스팬 적용
                spannableString.setSpan(imageSpan, nextIndex, nextIndex + placeholder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                
                // 해당 항목의 가격과 퍼센트에만 색상 적용 (마켓 이름은 검은색 유지)
                val priceStart = nextIndex + placeholder.length + 1 // 공백 다음부터
                val nextPlaceholderIndex = kotlin.math.min(
                    spannableString.indexOf("ICON_UP", priceStart).takeIf { it != -1 } ?: spannableString.length,
                    spannableString.indexOf("ICON_DOWN", priceStart).takeIf { it != -1 } ?: spannableString.length
                )
                val colorEnd = if (nextPlaceholderIndex < spannableString.length) {
                    // 다음 아이템이 있으면 그 아이템의 마켓 이름 전까지
                    val nextSpaceIndex = spannableString.lastIndexOf("    ", nextPlaceholderIndex)
                    if (nextSpaceIndex != -1) nextSpaceIndex else nextPlaceholderIndex
                } else {
                    spannableString.length
                }
                val colorSpan = android.text.style.ForegroundColorSpan(color)
                spannableString.setSpan(colorSpan, priceStart, colorEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error applying default image span", e)
            }
            
            searchIndex = nextIndex + placeholder.length
        }
        
        marketTickerView.text = spannableString
    }
    
    // 데이터 자동 갱신 체크 함수
    private fun checkAndRefreshDataIfNeeded() {
        val currentTime = System.currentTimeMillis()
        
        // 1. 데이터가 만료되었는지 확인 (5분 경과)
        val isDataExpired = (currentTime - lastDataLoadTime) > DATA_REFRESH_INTERVAL
        
        // 2. 백그라운드에서 일정 시간 이상 있었는지 확인 (1분 이상)
        val wasInBackground = backgroundTime > 0 && (currentTime - backgroundTime) > 60 * 1000L
        
        // 3. 조건에 따른 자동 갱신 실행
        when {
            isDataExpired -> {
                Log.d("HomeFragment", "데이터 만료로 인한 자동 갱신 (${(currentTime - lastDataLoadTime) / 1000}초 경과)")
                loadData(isRefreshing = false)
            }
            wasInBackground -> {
                Log.d("HomeFragment", "백그라운드 복귀로 인한 자동 갱신 (${(currentTime - backgroundTime) / 1000}초 백그라운드)")
                loadData(isRefreshing = false)
                backgroundTime = 0L // 리셋
            }
            lastDataLoadTime == 0L -> {
                Log.d("HomeFragment", "초기 데이터 로드")
                loadData(isRefreshing = false)
            }
            else -> {
                Log.d("HomeFragment", "데이터 갱신 불필요 (마지막 로드: ${(currentTime - lastDataLoadTime) / 1000}초 전)")
            }
        }
    }
    
    // 주기적 자동 갱신 타이머 시작
    private fun startAutoRefreshTimer() {
        stopAutoRefreshTimer() // 기존 타이머가 있으면 중지
        
        autoRefreshTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    activity?.runOnUiThread {
                        val currentTime = System.currentTimeMillis()
                        if ((currentTime - lastDataLoadTime) > DATA_REFRESH_INTERVAL) {
                            Log.d("HomeFragment", "타이머에 의한 주기적 데이터 갱신")
                            loadData(isRefreshing = false)
                        }
                    }
                }
            }, DATA_REFRESH_INTERVAL, DATA_REFRESH_INTERVAL) // 5분마다 실행
        }
    }
    
    // 자동 갱신 타이머 중지
    private fun stopAutoRefreshTimer() {
        autoRefreshTimer?.cancel()
        autoRefreshTimer = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 메모리 누수 방지를 위해 타이머 정리
        stopAutoRefreshTimer()
    }
    
    private fun setupTreemapLayout() {
        Log.d("HomeFragment", "=== setupTreemapLayout 시작 ===")
        
        // 기존 View 시스템 Treemap 사용
        Log.d("HomeFragment", "기존 View Treemap 사용")
        treemapAdapter = TreemapAdapter(requireContext(), treemapLayout)
        
        // 클릭 리스너 추가
        treemapAdapter.setOnItemClickListener(object : TreemapAdapter.OnItemClickListener {
            override fun onItemClick(theme: Theme) {
                val intent = ThemeDetailActivity.newIntent(requireActivity(), "", theme.name)
                startActivity(intent)
            }
        })
        
        Log.d("HomeFragment", "TreemapAdapter 설정 완료")
    }
    
    private fun setupTreemapWebView() {
        Log.d("HomeFragment", "=== setupTreemapWebView 시작 ===")
        
        // WebView 트리맵 클릭 리스너 설정
        treemapWebView.setOnThemeClickListener { themeName ->
            Log.d("HomeFragment", "WebView 트리맵 클릭: $themeName")
            val intent = ThemeDetailActivity.newIntent(requireActivity(), "", themeName)
            startActivity(intent)
        }
        
        Log.d("HomeFragment", "TreemapWebView 설정 완료")
    }
    
    // AdFit 배너 광고 뷰 변수
    private var bannerAdView: BannerAdView? = null
    
    /**
     * AdFit 배너 광고 설정
     */
    private fun setupBannerAd(view: View) {
        Log.d("HomeFragment", "=== setupBannerAd 함수 호출됨 ===")
        try {
            Log.d("HomeFragment", "광고 뷰 찾기 시도...")
            bannerAdView = view.findViewById<BannerAdView>(R.id.banner_ad_view)
            
            if (bannerAdView == null) {
                Log.e("HomeFragment", "❌ 배너 광고 뷰를 찾을 수 없습니다. 레이아웃 확인 필요!")
                // 레이아웃에서 직접 찾기 시도
                val adView = view.findViewById<View>(R.id.banner_ad_view)
                Log.d("HomeFragment", "일반 View로 찾기 결과: $adView")
                return
            }
            
            Log.d("HomeFragment", "✅ 광고 뷰 찾기 성공: $bannerAdView")
            Log.d("HomeFragment", "배너 광고 설정 시작 - 광고 단위 ID: DAN-i0idA4lyPWuhyvhd")
            
            // 광고 영역을 먼저 표시 (로딩 중임을 표시)
            bannerAdView?.visibility = View.VISIBLE
            Log.d("HomeFragment", "광고 뷰 visibility를 VISIBLE로 설정")
            
            // 광고 단위 ID 설정
            bannerAdView?.setClientId("DAN-i0idA4lyPWuhyvhd")
            Log.d("HomeFragment", "광고 단위 ID 설정 완료")
            
            // 로딩 텍스트 찾기
            val adLoadingText = view.findViewById<TextView>(R.id.tv_ad_loading)
            
            // 광고 로드 리스너 설정
            Log.d("HomeFragment", "광고 리스너 설정 중...")
            bannerAdView?.setAdListener(object : AdListener {
                override fun onAdLoaded() {
                    Log.d("HomeFragment", "✅✅✅ 배너 광고 로드 완료 - 광고 표시")
                    activity?.runOnUiThread {
                        bannerAdView?.visibility = View.VISIBLE
                        adLoadingText?.visibility = View.GONE
                        Log.d("HomeFragment", "광고 뷰 VISIBLE로 변경 완료, 로딩 텍스트 숨김")
                    }
                }
                
                override fun onAdFailed(errorCode: Int) {
                    Log.w("HomeFragment", "❌❌❌ 배너 광고 로드 실패: 에러 코드 $errorCode")
                    // 에러 코드 설명
                    val errorMsg = when (errorCode) {
                        202 -> "네트워크 오류"
                        301 -> "유효하지 않은 광고 응답"
                        302 -> "보여줄 광고 없음 (NO_AD)"
                        400 -> "잘못된 요청 (HTTP 400) - 광고 단위 ID 확인 필요"
                        501 -> "광고 로딩 실패"
                        601 -> "SDK 내부 오류"
                        else -> "알 수 없는 오류"
                    }
                    Log.w("HomeFragment", "오류 상세: $errorMsg")
                    Log.w("HomeFragment", "⚠️ HTTP 400 오류 해결 방법:")
                    Log.w("HomeFragment", "   1. AdFit 플랫폼(https://adfit.kakao.com)에서 앱 등록 확인")
                    Log.w("HomeFragment", "   2. 광고 단위 ID 'DAN-i0idA4lyPWuhyvhd' 활성화 상태 확인")
                    Log.w("HomeFragment", "   3. 광고 유형이 '배너'인지 확인 (네이티브가 아닌)")
                    Log.w("HomeFragment", "   4. 앱 패키지명 'com.mycompany.antwinner' 등록 확인")
                    Log.w("HomeFragment", "   5. 실제 기기에서 테스트 (에뮬레이터는 제한될 수 있음)")
                    activity?.runOnUiThread {
                        // 실패해도 영역은 보이도록 유지
                        bannerAdView?.visibility = View.GONE
                        adLoadingText?.visibility = View.VISIBLE
                        adLoadingText?.text = "광고를 불러올 수 없습니다 (에러: $errorCode)"
                        Log.w("HomeFragment", "광고 로드 실패 - 로딩 메시지 표시")
                    }
                }
                
                override fun onAdClicked() {
                    Log.d("HomeFragment", "배너 광고 클릭됨")
                }
            })
            Log.d("HomeFragment", "광고 리스너 설정 완료")
            
            // 광고 로드 시작
            Log.d("HomeFragment", "🚀 광고 로드 요청 시작...")
            bannerAdView?.loadAd()
            Log.d("HomeFragment", "loadAd() 호출 완료")
            
        } catch (e: Exception) {
            Log.e("HomeFragment", "❌ 배너 광고 설정 중 예외 발생", e)
            Log.e("HomeFragment", "예외 메시지: ${e.message}")
            e.printStackTrace()
            // 예외 발생해도 영역은 보이도록 유지
            try {
                val adView = view.findViewById<View>(R.id.banner_ad_view)
                adView?.visibility = View.VISIBLE
                Log.d("HomeFragment", "예외 발생했지만 광고 영역은 표시 유지")
            } catch (ex: Exception) {
                Log.e("HomeFragment", "광고 영역 표시도 실패", ex)
            }
        }
        Log.d("HomeFragment", "=== setupBannerAd 함수 종료 ===")
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // 광고 정리
        bannerAdView?.destroy()
        bannerAdView = null
    }
    
    /* AdFit SDK - Android Studio에서 Gradle Sync 후 주석 해제
    private fun setupNativeAd(view: View) {
        try {
            val adSection = view.findViewById<android.widget.FrameLayout>(R.id.native_ad_section)
            
            if (adSection == null) {
                Log.w("HomeFragment", "광고 컨테이너를 찾을 수 없습니다.")
                return
            }
            
            // AdFit NativeAdLoader 생성
            val nativeAdLoader = NativeAdLoader.Builder(requireContext())
                .adUnitId("DAN-i0idA4lyPWuhyvhd")
                .build()
            
            // 네이티브 광고 로드
            nativeAdLoader.loadAd(object : NativeAdLoader.OnAdLoadedListener {
                override fun onAdLoaded(nativeAd: NativeAd) {
                    try {
                        // 광고 레이아웃 인플레이트
                        val adView = LayoutInflater.from(requireContext())
                            .inflate(R.layout.item_native_ad, adSection, false)
                        
                        // 광고 데이터 바인딩
                        val mainImage = adView.findViewById<ImageView>(R.id.native_ad_main_image)
                        val iconImage = adView.findViewById<ImageView>(R.id.native_ad_icon)
                        val titleText = adView.findViewById<TextView>(R.id.native_ad_title)
                        val descriptionText = adView.findViewById<TextView>(R.id.native_ad_description)
                        
                        // 메인 이미지 설정 (1200x600)
                        nativeAd.getMainImage()?.let { image: com.kakao.adfit.ads.na.NativeAd.Image ->
                            Glide.with(requireContext())
                                .load(image.url)
                                .into(mainImage)
                        }
                        
                        // 아이콘 설정
                        nativeAd.getIcon()?.let { icon: com.kakao.adfit.ads.na.NativeAd.Image ->
                            Glide.with(requireContext())
                                .load(icon.url)
                                .into(iconImage)
                        }
                        
                        // 제목 설정
                        titleText.text = nativeAd.getTitle()
                        
                        // 설명 설정
                        descriptionText.text = nativeAd.getDescription()
                        
                        // 광고 뷰를 네이티브 광고에 등록
                        nativeAd.registerView(adView)
                        
                        // 컨테이너에 추가
                        adSection.removeAllViews()
                        adSection.addView(adView)
                        
                        // 광고 영역 표시
                        adSection.visibility = View.VISIBLE
                        
                        Log.d("HomeFragment", "네이티브 광고 로드 완료")
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "광고 뷰 생성 중 오류", e)
                        adSection.visibility = View.GONE
                    }
                }
            })
            
            // 광고 로드 실패 시 처리
            nativeAdLoader.setOnAdFailedListener { error: com.kakao.adfit.ads.AdError ->
                Log.w("HomeFragment", "네이티브 광고 로드 실패: ${error.message}")
                adSection?.visibility = View.GONE
            }
            
        } catch (e: Exception) {
            Log.e("HomeFragment", "네이티브 광고 설정 중 오류", e)
            view.findViewById<View>(R.id.native_ad_section)?.visibility = View.GONE
        }
    }
    */
    
    /* Compose 관련 함수 임시 비활성화
    private fun setupComposeTreemap() {
        composeTreemapView.setContent {
            MaterialTheme {
                var selectedTheme by remember { mutableStateOf<com.example.antwinner_kotlin.ui.home.compose.ThemeChange?>(null) }
                
                // 샘플 데이터 또는 실제 API 데이터 사용
                val themeChanges = remember {
                    ThemeDataConverter.getSampleThemeChanges()
                }
                
                ThemeTreemap(
                    items = themeChanges,
                    onClick = { themeChange ->
                        selectedTheme = themeChange
                        // ThemeDetailActivity로 이동
                        val intent = ThemeDetailActivity.newIntent(requireActivity(), "", themeChange.name)
                        startActivity(intent)
                    },
                    selected = selectedTheme,
                    showWatermark = true
                )
            }
        }
    }
    */

    // 그리드 아이템 간격을 위한 ItemDecoration 클래스 추가
    inner class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {
        
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount
            
            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount
                
                if (position < spanCount) {
                    outRect.top = spacing
                }
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                
                if (position >= spanCount) {
                    outRect.top = spacing
                }
            }
        }
    }

    private fun setupHotThemeRecyclerView() {
        // 어댑터 생성 시 리스트만 전달
        hotThemeAdapter = HotThemeAdapter(emptyList()) 
        
        // 가로 스크롤을 위한 LinearLayoutManager 사용
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        
        hotThemeRecyclerView.layoutManager = layoutManager
        hotThemeRecyclerView.adapter = hotThemeAdapter
        hotThemeRecyclerView.setHasFixedSize(true)
        
        // HotThemeAdapter에 맞는 클릭 리스너 설정 (이 부분은 유지)
        hotThemeAdapter.setOnItemClickListener(object : HotThemeAdapter.OnItemClickListener {
            override fun onItemClick(v: View, data: HotTheme, position: Int) {
                // Toast.makeText(requireContext(), "${data.name} 핫테마 클릭됨", Toast.LENGTH_SHORT).show()
                // ThemeDetailActivity로 이동
                val intent = ThemeDetailActivity.newIntent(requireActivity(), "", data.name) // ID는 비워둠
                startActivity(intent)
            }
        })
    }

    private fun setupStockTabViewPager() {
        // ViewPager 어댑터 설정 (동일)
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 4
            override fun createFragment(position: Int): Fragment {
                return StockTabFragment.newInstance(position)
            }
        }
        
        // 애니메이션 속도 조정 - 기본 애니메이션보다 좀 더 빠르게 설정
        viewPager.setPageTransformer { page, position ->
            page.alpha = 1f - 0.25f * kotlin.math.abs(position)
        }
        
        // 부드러운 스크롤을 위한 설정
        val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<Chip>(R.id.tab_chip)?.isChecked = true
                
                // 선택된 탭 위치로 ViewPager 부드럽게 이동
                tab?.position?.let { position ->
                    if (viewPager.currentItem != position) {
                        // 애니메이션과 함께 이동
                        viewPager.setCurrentItem(position, true)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<Chip>(R.id.tab_chip)?.isChecked = false
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // 재선택 시에도 동일하게 처리
                tab?.position?.let { position ->
                    if (viewPager.currentItem != position) {
                        viewPager.setCurrentItem(position, true)
                    }
                }
            }
        }

        // TabLayout과 ViewPager 연동 (커스텀 뷰 사용)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // 1. 커스텀 탭 레이아웃 inflate
            val customTabView = LayoutInflater.from(requireContext())
                                  .inflate(R.layout.custom_tab, tabLayout, false)
            val tabChip: Chip = customTabView.findViewById(R.id.tab_chip)

            // 2. 탭 이름 설정 (올바른 문자열 리소스 ID 사용)
            tabChip.text = when (position) {
                0 -> getString(R.string.tab_rising_top)
                1 -> getString(R.string.tab_trading_top)
                2 -> getString(R.string.tab_trade_amount_top)
                3 -> getString(R.string.tab_foreign_top)
                else -> null
            }
            
            // 3. 커스텀 뷰를 탭에 설정 (텍스트는 null로 설정해야 커스텀 뷰만 보임)
            tab.customView = customTabView
            tab.text = null 
            
            // 4. 초기 선택 상태 설정 (첫 번째 탭)
            if (position == 0) {
                 tabChip.isChecked = true
            }
            
            // 5. 탭 클릭 리스너 추가 - 클릭 시 애니메이션 효과를 위해
            customTabView.setOnClickListener {
                if (tab.position != viewPager.currentItem) {
                    viewPager.setCurrentItem(tab.position, true)
                }
            }

        }.attach()
        
        // 리스너 추가
        tabLayout.addOnTabSelectedListener(tabSelectedListener)
    }
    
    private fun setupTrendsRecyclerView() {
        trendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        trendsRecyclerView.adapter = trendDayAdapter
        
        // 클릭 리스너 추가
        trendDayAdapter.setOnItemClickListener { trendDay ->
            val intent = ThemeDetailActivity.newIntent(requireActivity(), "", trendDay.themeName)
            startActivity(intent)
        }
    }
    
    private fun setupPromisingThemeRecyclerView() {
        // 클릭 리스너를 포함해 PromisingThemeAdapter 생성
        promisingThemeAdapter = PromisingThemeAdapter(
            emptyList()
        ) { theme ->
            // 클릭 이벤트 처리
            // Toast.makeText(requireContext(), "${theme.name} 유망테마 클릭됨", Toast.LENGTH_SHORT).show()
            // ThemeDetailActivity로 이동
            val intent = ThemeDetailActivity.newIntent(requireActivity(), theme.id, theme.name)
            startActivity(intent)
        }
        
        // 가로 스크롤을 위한 LinearLayoutManager 사용
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        
        promisingThemeRecyclerView.layoutManager = layoutManager
        promisingThemeRecyclerView.adapter = promisingThemeAdapter
        promisingThemeRecyclerView.setHasFixedSize(true)
    }
    
    // 데이터 로드 메서드 - API나 더미 데이터를 로드
    private fun loadData(isRefreshing: Boolean) {
        // 이미 로딩 중이거나 Fragment가 attached되지 않았다면 시작하지 않음
        if (isLoading || !isAdded) {
            if (isRefreshing) swipeRefreshLayout.isRefreshing = false
            return
        }

        Log.d("HomeFragment", "loadData called. isRefreshing: $isRefreshing")
        isLoading = true
        // 새로고침 제스처로 시작된 경우에만 인디케이터 표시
        if (isRefreshing && ::swipeRefreshLayout.isInitialized) {
            swipeRefreshLayout.isRefreshing = true
        }

        try {
            Log.d("HomeFragment", "데이터 로드 시작")
            
            // 인터넷 연결 확인
            context?.let { ctx ->
                if (!NetworkUtil.isNetworkAvailable(ctx)) {
                    Log.d("HomeFragment", "No network connection, using dummy data")
                    // 네트워크가 없으면 더미 데이터 로드
                    loadDummyData()
                    return
                }
                
                // 참조 시간 업데이트
                updateReferenceTime()
                
                // 각 API 데이터 로드
                fetchMarketIndices()
                fetchThemeFluctuations()
                fetchTrendData()
                fetchHotThemes()
                fetchPromisingThemes()
                fetchTopRisingStocks(currentPeriod)
                
                // 데이터 로드 상태 업데이트
                isLoading = false
                // 마지막 데이터 로드 시간 업데이트
                lastDataLoadTime = System.currentTimeMillis()
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "데이터 로드 오류", e)
            loadDummyData()
        } finally {
            if (::swipeRefreshLayout.isInitialized) {
                swipeRefreshLayout.isRefreshing = false
            }
            Log.d("HomeFragment", "isLoading set to false. Swipe indicator stopped.")
        }
    }
    
    private fun fetchMarketIndices() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = trendRepository.getMarketIndices()
                
                // 로그로 응답 확인
                Log.d("MarketIndices", "Received ${response.global.size} market indices")
                
                // 전광판 데이터 설정
                updateMarketTicker(response)
                
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching market indices", e)
                // 에러 발생 시 기본 마켓 티커 유지
            }
        }
    }
    
    private fun fetchThemeFluctuations() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d("HomeFragment", "테마 등락률 데이터 가져오기 시작")
                val themeFluctuations = trendRepository.getThemeFluctuations()
                
                // API 응답 데이터 로깅
                Log.d("HomeFragment", "API 응답 themeFluctuations 크기: ${themeFluctuations.size}")
                themeFluctuations.forEachIndexed { index, theme ->
                    Log.d("HomeFragment", "테마[$index]: thema=${theme.thema}, averageRate=${theme.averageRate}, risingRatio=${theme.risingRatioString}, companies=${theme.companies.size}개")
                }
                
                if (themeFluctuations.isEmpty()) {
                    Log.w("HomeFragment", "API 응답 themeFluctuations 비어있음, 더미 데이터 사용")
                    updateThemeMap(listOf())  // 빈 리스트 전달하면 내부에서 더미 데이터 사용
                } else {
                    Log.d("HomeFragment", "테마 등락률 데이터 성공적으로 가져옴: ${themeFluctuations.size}개")
                    updateThemeMap(themeFluctuations)
                }
                
            } catch (e: Exception) {
                Log.e("HomeFragment", "테마 등락률 데이터 가져오기 오류: ${e.message}", e)
                // 더미 데이터 사용
                updateThemeMap(listOf())  // 빈 리스트 전달하면 내부에서 더미 데이터 사용
            }
        }
    }
    
    private fun updateMarketTicker(response: MarketIndexResponse) {
        // 전광판 텍스트 생성 (이미지 이모지 포함)
        val spannableText = buildMarketTickerSpannableText(response.global)
        
        // UI 업데이트
        activity?.runOnUiThread {
            marketTickerView.text = spannableText
            marketTickerView.isSelected = true // 마키 효과 활성화
        }
    }
    
    private fun buildMarketTickerSpannableText(indices: List<MarketIndex>): SpannableString {
        val builder = StringBuilder()
        
        indices.forEachIndexed { index, marketIndex ->
            // 등락률 계산 (API에서 소수점으로 제공되므로 100 곱함)
            val rateInPercent = try {
                marketIndex.changeRate.toDouble() * 100
            } catch (e: Exception) {
                0.0
            }
            
            builder.append(marketIndex.name)
            builder.append(" ")
            builder.append("ICON_PLACEHOLDER") // 이모지 자리표시자
            builder.append(" ${marketIndex.tradePrice} (")
            builder.append(String.format("%.2f", rateInPercent))
            builder.append("%)")
            
            if (index < indices.size - 1) {
                builder.append("    ")  // 구분자를 간단한 띄어쓰기로 변경
            }
        }
        
        val spannableString = SpannableString(builder.toString())
        var currentPosition = 0
        
        // 각 마켓 인덱스에 대해 이모지 적용
        indices.forEach { marketIndex ->
            val iconPlaceholderIndex = spannableString.indexOf("ICON_PLACEHOLDER", currentPosition)
            if (iconPlaceholderIndex != -1) {
                // 이모지 리소스 선택
                val iconRes = if (marketIndex.isRising) R.drawable.ic_market_up else R.drawable.ic_market_down
                val changeColor = if (marketIndex.isRising) android.graphics.Color.RED else android.graphics.Color.BLUE
                
                try {
                    // 이미지 스팬 생성
                    val drawable = requireContext().getDrawable(iconRes)
                    drawable?.setBounds(0, 0, 32, 32) // 이모지 크기 설정 (32px)
                    val imageSpan = android.text.style.ImageSpan(drawable!!, android.text.style.ImageSpan.ALIGN_BASELINE)
                    
                    // 색상 스팬 생성 (가격과 퍼센트에 적용)
                    val colorSpan = android.text.style.ForegroundColorSpan(changeColor)
                    
                // 이미지 스팬 적용
                spannableString.setSpan(imageSpan, iconPlaceholderIndex, iconPlaceholderIndex + "ICON_PLACEHOLDER".length, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                
                // 색상 스팬 적용 (이모지 이후 숫자 부분에만)
                val priceStart = iconPlaceholderIndex + "ICON_PLACEHOLDER".length + 1 // 공백 다음부터
                val nextIconIndex = spannableString.indexOf("ICON_PLACEHOLDER", priceStart)
                val colorEnd = if (nextIconIndex != -1) {
                    // 다음 아이템이 있으면 그 아이템의 마켓 이름 전까지
                    val nextSpaceIndex = spannableString.lastIndexOf("    ", nextIconIndex)
                    if (nextSpaceIndex != -1) nextSpaceIndex else nextIconIndex
                } else {
                    spannableString.length
                }
                spannableString.setSpan(colorSpan, priceStart, colorEnd, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error applying image span", e)
                }
                
                currentPosition = iconPlaceholderIndex + "ICON_PLACEHOLDER".length
            }
        }
        
        return spannableString
    }
    
    private fun buildMarketTickerText(indices: List<MarketIndex>): String {
        val builder = StringBuilder()
        
        indices.forEachIndexed { index, marketIndex ->
            val changeSymbol = if (marketIndex.isRising) "▲" else "▼"
            val changeColor = if (marketIndex.isRising) "#FF0000" else "#0000FF"
            
            // 등락률 계산 (API에서 소수점으로 제공되므로 100 곱함)
            val rateInPercent = try {
                marketIndex.changeRate.toDouble() * 100
            } catch (e: Exception) {
                0.0
            }
            
            builder.append(marketIndex.name)
            builder.append(" <font color='$changeColor'>$changeSymbol ${marketIndex.tradePrice} (")
            builder.append(String.format("%.2f", rateInPercent))
            builder.append("%)</font>")
            
            if (index < indices.size - 1) {
                builder.append("    ")  // 구분자를 간단한 띄어쓰기로 변경
            }
        }
        
        return builder.toString()
    }
    
    private fun updateThemeMap(themes: List<ThemeFluctuation>) {
        try {
            Log.d("HomeFragment", "updateThemeMap 호출됨 - 테마 수: ${themes.size}")
            
            // ThemeFluctuation 리스트를 Theme 리스트로 변환
            val themeList = themes.map { themeFluctuation ->
                Theme(
                    name = themeFluctuation.thema,
                    rate = themeFluctuation.averageRateValue,
                    risingRatio = themeFluctuation.risingRatioValue,
                    isRising = themeFluctuation.isRising,
                    size = 1  // 모두 동일한 크기로 설정
                )
            }
            
            // 혹시 리스트가 비어있는지 확인
            if (themeList.isEmpty()) {
                Log.w("HomeFragment", "변환된 테마 리스트가 비어있습니다. 더미 데이터로 대체합니다.")
                updateTreemapWithData(getThemeData())
                return
            }
            
            Log.d("HomeFragment", "테마 데이터 변환 완료: ${themeList.size}개")
            updateTreemapWithData(themeList)
        } catch (e: Exception) {
            Log.e("HomeFragment", "updateThemeMap 오류: ${e.message}", e)
            // 예외 발생 시 더미 데이터 사용
            updateTreemapWithData(getThemeData())
        }
    }
    
    private fun updateTreemapWithData(themeList: List<Theme>) {
        activity?.runOnUiThread {
            if (useWebViewTreemap) {
                // WebView 트리맵 업데이트
                Log.d("HomeFragment", "WebView Treemap UI 업데이트 시작")
                treemapWebView.updateThemes(themeList)
                Log.d("HomeFragment", "WebView Treemap UI 업데이트 완료")
            } else {
                // 기존 View Treemap 업데이트
                Log.d("HomeFragment", "기존 View Treemap UI 업데이트 시작")
                treemapAdapter.updateThemes(themeList)
                Log.d("HomeFragment", "기존 View Treemap UI 업데이트 완료")
            }
        }
    }
    
    private fun fetchTrendData() {
        context?.let { ctx ->
            if (!NetworkUtil.isNetworkAvailable(ctx)) {
                Log.d("HomeFragment", "No network connection, using dummy data")
                val dummyData = getTrendData()
                trendDayAdapter.updateData(dummyData)
                
                return
            }
            
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val response = trendRepository.getStockKeywords()
                    
                    // API 응답 로깅
                    TrendLogTest.logResponse(response)
                    
                    // API 응답을 TrendDay 형식으로 변환
                    val trendDays = convertToTrendDays(response)
                    
                    // 어댑터에 데이터 설정
                    trendDayAdapter.updateData(trendDays)
                    
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error fetching trend data", e)
                    Toast.makeText(context, "투자 트렌드 데이터를 가져오는 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                    
                    // 에러 발생 시 더미 데이터로 대체
                    val dummyData = getTrendData()
                    trendDayAdapter.updateData(dummyData)
                }
            }
        }
    }
    
    private fun convertToTrendDays(responses: List<TrendKeywordResponse>): List<TrendDay> {
        // 오늘 날짜를 기준으로 0일전(오늘)부터 4일전까지의 데이터 가져옴 (총 5일)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        
        // 날짜별 인덱스 매핑 (오늘 기준으로 몇 일전인지)
        val dateToIndex = mutableMapOf<String, Int>()
        
        // 오늘(0일전) 날짜 추가
        val todayString = dateFormat.format(calendar.time)
        dateToIndex[todayString] = 0 // 오늘은 0일전
        
        // 1일전부터 4일전까지 날짜 계산 (총 5일: 오늘 + 4일전)
        for (i in 1..4) { // 1일전부터 4일전까지
            calendar.add(Calendar.DAY_OF_YEAR, -1) // 하루 전으로 이동
            val dateString = dateFormat.format(calendar.time) // 날짜 문자열 가져오기
            dateToIndex[dateString] = i // 날짜와 인덱스 매핑 (i=1은 1일전, i=2는 2일전, ...)
        }
        
        // 필요한 날짜의 데이터만 필터링하고 TrendDay 리스트로 변환
        return responses
            .filter { it.date in dateToIndex.keys } // 오늘 ~ 4일전 날짜 포함
            .map { response ->
                val dayIndex = dateToIndex[response.date] ?: -1 // 0, 1, 2, 3, 4 값 또는 오류 시 -1
                // dayIndex가 -1인 경우는 없어야 하지만, 안전을 위해 추가
                if (dayIndex == -1) return@map null // 유효하지 않은 날짜는 제외 (선택적)

                // 각 키워드를 TrendTheme으로 변환
                val trendThemes = response.keywords.map { keywordItem ->
                    TrendTheme(
                        name = keywordItem.keyword,
                        stockCount = keywordItem.count,
                        isPositive = true // API에서 부정/긍정 여부가 없으므로 기본값 사용
                    )
                }
                
                // 첫 번째 키워드의 이름을 themeName으로 사용
                val themeName = if (trendThemes.isNotEmpty()) trendThemes.first().name else ""
                
                TrendDay(dayIndex, trendThemes, themeName)
            }
            .filterNotNull() // map에서 null이 반환된 경우 제거 (선택적)
            .sortedBy { it.day } // 날짜순으로 정렬 (0, 1, 2, 3, 4 순서)
    }
    
    private fun getThemeData(): List<Theme> {
        // 함수 호출 로깅
        Log.d("HomeFragment", "getThemeData() 호출됨")
        
        // 사용자 스크린샷과 완전히 동일한 테마들
        val themes = listOf(
            // 상승 테마들 (빨간색/분홍색) - 스크린샷 순서대로
            Theme("콘텐츠", 7.85, 100.00, true, 3),     // 가장 큰 박스
            Theme("농기계", 3.94, 85.00, true, 2),      // 큰 박스
            Theme("중국기업", 2.25, 70.00, true, 2),    // 중형 박스
            Theme("김동연", 2.16, 65.00, true, 2),      // 중형 박스
            Theme("하락장", 1.00, 60.00, true, 2),      // 중형 박스
            Theme("페베터리", 2.37, 55.00, true, 2),    // 중형 박스
            Theme("유심", 1.20, 50.00, true, 2),        // 소형 박스
            Theme("CBDC", 0.96, 45.00, true, 2),        // 소형 박스
            Theme("게임", 0.61, 40.00, true, 2),        // 소형 박스
            
            // 하락 테마들 (파란색) - 스크린샷의 하락 테마들
            Theme("조선기업", -2.05, 35.00, false, 1),  // 이게 화면에 상단에 나타나고 있던 문제 테마
            Theme("전기차충전", -1.17, 32.00, false, 1),
            Theme("우주항공", -1.73, 30.00, false, 1),
            Theme("풍력", -2.05, 28.00, false, 1),
            Theme("비만치료", -2.16, 25.00, false, 1),
            Theme("조선기자재", -1.24, 22.00, false, 1),
            Theme("제약바이오", -1.03, 20.00, false, 1),
            Theme("태양광", -1.16, 18.00, false, 1),
            Theme("전고체배터리", -1.24, 15.00, false, 1),
            Theme("HBM", -1.40, 12.00, false, 1)
        )
        
        Log.d("HomeFragment", "getThemeData() 결과: ${themes.size}개 테마 (상승:${themes.count { it.isRising }}, 하락:${themes.count { !it.isRising }})")
        return themes
    }

    private fun getHotThemeData(): List<HotTheme> {
        return listOf(
            HotTheme(
                "인공지능(AI)",
                56.55,
                "https://antwinner.com/api/image/인공지능(AI).png",
                listOf(
                    ThemeCompany("포바이포", 30.00, "25억"),
                    ThemeCompany("DSC인베스트먼트", 29.98, "291억"),
                    ThemeCompany("TS인베스트먼트", 29.97, "388억")
                )
            ),
            HotTheme(
                "이준석",
                25.84,
                "https://antwinner.com/api/image/이준석.png",
                listOf(
                    ThemeCompany("넥스트아이", 30.00, "45억"),
                    ThemeCompany("삼보산업", 25.98, "121억"),
                    ThemeCompany("YBM넷", 15.97, "68억")
                )
            ),
            HotTheme(
                "삼보산업",
                21.35,
                "https://antwinner.com/api/image/삼보산업.png",
                listOf(
                    ThemeCompany("삼보산업", 25.98, "121억"),
                    ThemeCompany("삼화네트웍스", 18.54, "78억"),
                    ThemeCompany("넥스트리밍", 15.12, "43억")
                )
            ),
            HotTheme(
                "반도체",
                18.72,
                "https://antwinner.com/api/image/반도체.png",
                listOf(
                    ThemeCompany("삼성전자", 8.25, "1,245억"),
                    ThemeCompany("SK하이닉스", 7.98, "984억"),
                    ThemeCompany("DB하이텍", 6.54, "124억")
                )
            )
        )
    }

    private fun getTrendData(): List<TrendDay> {
        return listOf(
            TrendDay(
                1,
                listOf(
                    TrendTheme("인공지능(AI)", 17, true),
                    TrendTheme("인공지능", 0, true)
                ),
                "인공지능(AI)"
            ),
            TrendDay(
                2,
                listOf(
                    TrendTheme("인공지능(의료)", 9, true),
                    TrendTheme("콘텐츠", 0, true)
                ),
                "인공지능(의료)"
            ),
            TrendDay(
                3,
                listOf(
                    TrendTheme("반도체", 17, true),
                    TrendTheme("2차전지", 11, true)
                ),
                "반도체"
            ),
            TrendDay(
                4,
                listOf(
                    TrendTheme("이재명", 5, true),
                    TrendTheme("가스관", 4, true)
                ),
                "이재명"
            )
        )
    }

    // 핫한 테마 데이터 가져오기
    private fun fetchHotThemes() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = trendRepository.getHotThemes()
                
                // 상승률 순으로 정렬
                val sortedResponse = response.sortedByDescending { it.averageRateValue }
                
                // HotTheme 객체로 변환
                val hotThemes = convertThemeFluctuationsToHotThemes(sortedResponse)
                
                // UI 업데이트
                activity?.runOnUiThread {
                    val adapter = hotThemeRecyclerView.adapter as? HotThemeAdapter
                    adapter?.updateData(hotThemes) ?: run {
                        val newAdapter = HotThemeAdapter(hotThemes)
                        hotThemeRecyclerView.adapter = newAdapter
                    }
                }
                
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching hot themes", e)
                
                // 더미 데이터 사용
                val dummyData = getDummyHotThemes()
                activity?.runOnUiThread {
                    val adapter = hotThemeRecyclerView.adapter as? HotThemeAdapter
                    adapter?.updateData(dummyData) ?: run {
                        val newAdapter = HotThemeAdapter(dummyData)
                        hotThemeRecyclerView.adapter = newAdapter
                    }
                }
            }
        }
    }

    // ThemeFluctuation을 HotTheme으로 변환하는 함수
    private fun convertThemeFluctuationsToHotThemes(fluctuations: List<ThemeFluctuation>): List<HotTheme> {
        // 상위 6개 테마만 표시 (UI에 맞게 제한)
        return fluctuations.take(6)
            .map { fluctuation ->
                // 등락률 값 추출 (% 제거하고 Double로 변환)
                val averageRate = try {
                    fluctuation.averageRate.replace("%", "").trim().toDouble()
                } catch (e: Exception) {
                    0.0
                }
                
                // 테마에 속한 회사들을 ThemeCompany로 변환 (상위 3개만)
                val companies = fluctuation.companies.take(3).map { company ->
                    val fluctuationValue = try {
                        company.fluctuation.replace("%", "").trim().toDouble()
                    } catch (e: Exception) {
                        0.0
                    }
                    
                    ThemeCompany(
                        name = company.stockName,
                        percent = fluctuationValue,
                        marketCap = company.volume
                    )
                }
                
                // 테마 로고 URL 생성
                val logoUrl = "https://antwinner.com/api/image/${fluctuation.thema}.png"
                
                HotTheme(
                    name = fluctuation.thema,
                    percent = averageRate,
                    logoUrl = logoUrl,
                    companies = companies
                )
            }
    }

    private fun fetchPromisingThemes() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // AI 키워드 데이터 가져오기
                val aiKeywords = trendRepository.getAIKeywords()
                
                // API 응답 로깅
                Log.d("HomeFragment", "Received ${aiKeywords.size} AI keywords")
                // 더 자세한 로깅 추가
                aiKeywords.forEach { keyword ->
                    Log.d("HomeFragment", "AI Keyword: ${keyword.keyword}, Frequency: ${keyword.frequency}, Stocks: ${keyword.stock_names.joinToString()}")
                }
                
                if (aiKeywords.isNotEmpty()) {
                    // AI 키워드를 PromisingTheme으로 변환
                    val promisingThemes = trendRepository.convertAIKeywordsToPromisingThemes(aiKeywords)
                    
                    // 어댑터에 데이터 설정
                    activity?.runOnUiThread {
                        promisingThemeAdapter.updateData(promisingThemes)
                    }
                } else {
                    // 추천 테마 API 데이터 가져오기 (대체 방법)
                    val promisingThemes = trendRepository.getPromisingThemes()
                    
                    // 어댑터에 데이터 설정
                    activity?.runOnUiThread {
                        promisingThemeAdapter.updateData(promisingThemes)
                    }
                }
                
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching promising themes", e)
                
                // 에러 발생 시 더미 데이터 사용
                try {
                    val dummyData = trendRepository.getPromisingThemes()
                    activity?.runOnUiThread {
                        promisingThemeAdapter.updateData(dummyData)
                    }
                } catch (innerE: Exception) {
                    Log.e("HomeFragment", "Error fetching promising themes from backup source", innerE)
                }
            }
        }
    }

    // 참조 시간 업데이트 메소드 추가
    private fun updateReferenceTime() {
        // 현재 시간에서 2분 전 시간을 계산
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, -2)
        val twoMinutesAgo = calendar.time
        
        // 날짜 및 시간 포맷 설정
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val formattedTime = dateFormat.format(twoMinutesAgo)
        
        // 텍스트뷰에 시간 설정
        tvTime.text = "$formattedTime 기준"
    }

    // 상승 종목 초기화 및 설정
    private fun setupTopRisingStocks(view: View) {
        // ViewPager2 초기화
        topRisingStocksViewPager = view.findViewById(R.id.vp_top_rising_stocks)
        
        // 어댑터 초기화
        topRisingStocksPagerAdapter = TopRisingStocksPagerAdapter { stock ->
            // 종목 클릭 시 처리 - 종목명 정리해서 전달
            val cleanStockName = stock.name.trim().replace(Regex("\\s+"), " ")
            
            val intent = com.example.antwinner_kotlin.ui.stockdetail.StockDetailActivity.newIntent(
                requireContext(),
                cleanStockName
            )
            startActivity(intent)
        }
        
        // ViewPager2 설정
        topRisingStocksViewPager.apply {
            adapter = topRisingStocksPagerAdapter
            // 페이지 변경 이벤트 리스너
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    // 페이지 변경 시 필터도 함께 변경
                    val period = TopRisingStocksPagerAdapter.getPeriodByPageIndex(position)
                    
                    // 현재 선택된 필터가 페이지와 다른 경우에만 업데이트
                    if (currentPeriod != period) {
                        when (position) {
                            TopRisingStocksPagerAdapter.PAGE_WEEKLY -> updateSelectedChip(chipWeekly, period)
                            TopRisingStocksPagerAdapter.PAGE_MONTHLY -> updateSelectedChip(chipMonthly, period)
                            TopRisingStocksPagerAdapter.PAGE_THREE_MONTHS -> updateSelectedChip(chip3Months, period)
                            TopRisingStocksPagerAdapter.PAGE_SIX_MONTHS -> updateSelectedChip(chip6Months, period)
                        }
                        
                        // 해당 페이지에 데이터가 아직 없으면 로드
                        fetchTopRisingStocks(period)
                    }
                }
            })
        }
        
        // 필터 칩 초기화
        chipWeekly = view.findViewById(R.id.chip_weekly)
        chipMonthly = view.findViewById(R.id.chip_monthly)
        chip3Months = view.findViewById(R.id.chip_3months)
        chip6Months = view.findViewById(R.id.chip_6months)
        
        // 가로 스크롤뷰 참조
        val horizontalScrollView = view.findViewById<HorizontalScrollView>(R.id.hsv_period_filter)
        
        // 칩 클릭 리스너 설정
        chipWeekly.setOnClickListener { 
            updateSelectedChip(it as Chip, "1W")
            fetchTopRisingStocks("1W")
            horizontalScrollView.smoothScrollTo(0, 0) // 스크롤 맨 왼쪽으로 이동
            topRisingStocksViewPager.setCurrentItem(TopRisingStocksPagerAdapter.PAGE_WEEKLY, true)
        }
        
        chipMonthly.setOnClickListener { 
            updateSelectedChip(it as Chip, "1M")
            fetchTopRisingStocks("1M")
            // 스크롤 위치 계산
            val chipWidth = chipMonthly.width + 8 // 칩 너비 + 마진
            horizontalScrollView.smoothScrollTo(chipWidth, 0)
            topRisingStocksViewPager.setCurrentItem(TopRisingStocksPagerAdapter.PAGE_MONTHLY, true)
        }
        
        chip3Months.setOnClickListener { 
            updateSelectedChip(it as Chip, "3M")
            fetchTopRisingStocks("3M")
            // 스크롤 위치 계산
            val chipWidth = chipMonthly.width + 8 // 칩 너비 + 마진
            horizontalScrollView.smoothScrollTo(chipWidth * 2, 0)
            topRisingStocksViewPager.setCurrentItem(TopRisingStocksPagerAdapter.PAGE_THREE_MONTHS, true)
        }
        
        chip6Months.setOnClickListener { 
            updateSelectedChip(it as Chip, "6M")
            fetchTopRisingStocks("6M") 
            // 스크롤 맨 오른쪽으로 이동
            horizontalScrollView.post {
                horizontalScrollView.smoothScrollTo(horizontalScrollView.getChildAt(0).width, 0)
            }
            topRisingStocksViewPager.setCurrentItem(TopRisingStocksPagerAdapter.PAGE_SIX_MONTHS, true)
        }
        
        // 기본값으로 주간 수익률 필터 적용
        updateSelectedChip(chipWeekly, "1W")
        fetchTopRisingStocks("1W")
        topRisingStocksViewPager.setCurrentItem(TopRisingStocksPagerAdapter.PAGE_WEEKLY, false)
    }
    
    // 선택된 칩 업데이트
    private fun updateSelectedChip(selectedChip: Chip, period: String) {
        // 모든 칩 초기화
        chipWeekly.apply {
            isChecked = false
            setChipBackgroundColorResource(R.color.background_light)
            setTextColor(resources.getColor(R.color.black, null))
        }
        
        chipMonthly.apply {
            isChecked = false
            setChipBackgroundColorResource(R.color.background_light)
            setTextColor(resources.getColor(R.color.black, null))
        }
        
        chip3Months.apply {
            isChecked = false
            setChipBackgroundColorResource(R.color.background_light)
            setTextColor(resources.getColor(R.color.black, null))
        }
        
        chip6Months.apply {
            isChecked = false
            setChipBackgroundColorResource(R.color.background_light)
            setTextColor(resources.getColor(R.color.black, null))
        }
        
        // 선택된 칩 스타일 변경
        selectedChip.apply {
            isChecked = true
            setChipBackgroundColorResource(R.color.black)
            setTextColor(resources.getColor(R.color.white, null))
        }
        
        // 현재 선택된 기간 저장
        currentPeriod = period
    }
    
    // 상승 종목 데이터 가져오기
    private fun fetchTopRisingStocks(period: String = "1W") {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // API 호출
                val responses = trendRepository.getTopRisingStocks(period)
                
                if (responses.isNotEmpty()) {
                    // API 응답을 TopRisingStock 모델로 변환
                    val topRisingStocks = trendRepository.convertToTopRisingStocks(responses)
                    
                    // 디버깅용 로그 추가 - 첫 번째 아이템의 정보만 출력
                    if (topRisingStocks.isNotEmpty()) {
                        val firstStock = topRisingStocks.first()
                        Log.d("HomeFragment", "Period: $period, First stock: ${firstStock.name}, Logo URL: ${firstStock.logoUrl}")
                        
                        // 응답도 함께 로깅
                        val firstResponse = responses.first()
                        Log.d("HomeFragment", "Raw Response - stockCode: ${firstResponse.stockCode}, stockImageCode: ${firstResponse.stockImageCode}")
                    }
                    
                    // 어댑터에 데이터 설정
                    activity?.runOnUiThread {
                        // 페이지 인덱스 계산
                        val pageIndex = TopRisingStocksPagerAdapter.getPageIndexByPeriod(period)
                        topRisingStocksPagerAdapter.updatePageData(pageIndex, topRisingStocks)
                    }
                } else {
                    // 응답이 비어있는 경우 더미 데이터 사용
                    activity?.runOnUiThread {
                        val dummyData = getTopRisingStockData()
                        val pageIndex = TopRisingStocksPagerAdapter.getPageIndexByPeriod(period)
                        topRisingStocksPagerAdapter.updatePageData(pageIndex, dummyData)
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching top rising stocks", e)
                
                // 에러 발생 시 더미 데이터 사용
                activity?.runOnUiThread {
                    val dummyData = getTopRisingStockData()
                    val pageIndex = TopRisingStocksPagerAdapter.getPageIndexByPeriod(period)
                    topRisingStocksPagerAdapter.updatePageData(pageIndex, dummyData)
                }
            }
        }
    }
    
    // 상승 종목 더미 데이터
    private fun getTopRisingStockData(): List<TopRisingStock> {
        return listOf(
            TopRisingStock(
                rank = 1,
                name = "오리엔트정공",
                logoUrl = "https://ssl.pstatic.net/imgfinance/chart/mobile/candle/day/A065500_end.png",
                percentChange = 925.16,
                newsDate = "2025-04-02",
                dailyChange = 21.6,
                newsContent = "[이재명] 윤석열 선고일 확정 소식에 대선 후보자 관련주들 폭등"
            ),
            TopRisingStock(
                rank = 2,
                name = "상지건설",
                logoUrl = "https://ssl.pstatic.net/imgfinance/chart/mobile/candle/day/A090730_end.png",
                percentChange = 462.04,
                newsDate = "2025-04-17",
                dailyChange = 29.9,
                newsContent = "[이재명] 대선후보 경쟁 속 유력 주자인 이재명 관련주 지속 급등중"
            ),
            TopRisingStock(
                rank = 3,
                name = "형지I&C",
                logoUrl = "https://ssl.pstatic.net/imgfinance/chart/mobile/candle/day/A011080_end.png",
                percentChange = 278.31,
                newsDate = "2025-04-15",
                dailyChange = 29.8,
                newsContent = "[이재명] 대선 경쟁 구도 속 이재명 관련주 연일 상승"
            ),
            TopRisingStock(
                rank = 4,
                name = "오리엔트바이오",
                logoUrl = "https://ssl.pstatic.net/imgfinance/chart/mobile/candle/day/A002630_end.png",
                percentChange = 271.92,
                newsDate = "2025-04-01",
                dailyChange = 29.7,
                newsContent = "[이재명] 윤석열 선고일 확정 소식에 대선 후보자 관련주들 폭등"
            ),
            TopRisingStock(
                rank = 5,
                name = "펩트론",
                logoUrl = "https://ssl.pstatic.net/imgfinance/chart/mobile/candle/day/A087010_end.png",
                percentChange = 261.55,
                newsDate = "2025-04-18",
                dailyChange = 11.0,
                newsContent = "[바이오] 다이어트 타겟 약물 출시 소식 임박 추측 시장 일각서 확산"
            )
        )
    }

    // 핫한 테마 더미 데이터
    private fun getDummyHotThemes(): List<HotTheme> {
        return listOf(
            HotTheme(
                name = "2차전지",
                percent = 5.43,
                logoUrl = "https://antwinner.com/api/image/2차전지.png",
                companies = listOf(
                    ThemeCompany("LG에너지솔루션", 4.2, "520억"),
                    ThemeCompany("삼성SDI", 3.8, "380억"),
                    ThemeCompany("SK이노베이션", 2.5, "260억")
                )
            ),
            HotTheme(
                name = "자율주행",
                percent = 4.21,
                logoUrl = "https://antwinner.com/api/image/자율주행.png",
                companies = listOf(
                    ThemeCompany("현대모비스", 3.9, "410억"),
                    ThemeCompany("만도", 2.8, "150억"),
                    ThemeCompany("SNT모티브", 2.2, "85억")
                )
            ),
            HotTheme(
                name = "인공지능(AI)",
                percent = 3.87,
                logoUrl = "https://antwinner.com/api/image/인공지능(AI).png",
                companies = listOf(
                    ThemeCompany("KT", 3.5, "320억"),
                    ThemeCompany("삼성전자", 2.9, "1,350억"),
                    ThemeCompany("네이버", 2.7, "480억")
                )
            ),
            HotTheme(
                name = "폴더블폰",
                percent = 3.15,
                logoUrl = "https://antwinner.com/api/image/폴더블폰.png",
                companies = listOf(
                    ThemeCompany("삼성전자", 2.9, "1,350억"),
                    ThemeCompany("LG이노텍", 2.7, "120억"),
                    ThemeCompany("파트론", 2.5, "75억")
                )
            ),
            HotTheme(
                name = "반도체",
                percent = 2.89,
                logoUrl = "https://antwinner.com/api/image/반도체.png",
                companies = listOf(
                    ThemeCompany("SK하이닉스", 2.8, "850억"),
                    ThemeCompany("삼성전자", 2.5, "1,350억"),
                    ThemeCompany("DB하이텍", 2.1, "120억")
                )
            )
        )
    }

    inner class StockTabAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return StockTabFragment.newInstance(position)
        }
    }

    // 더미 데이터 로드 메서드
    private fun loadDummyData() {
        try {
            Log.d("HomeFragment", "더미 데이터 로드")
            
            // 트리맵 더미 데이터
            val themeList = getThemeData()
            updateTreemapWithData(themeList)
            
            // 로그 확인을 위해 추가
            Log.d("HomeFragment", "더미 테마 데이터 로드 완료: ${themeList.size}개 항목")
            
            // 핫한 테마 더미 데이터
            val hotThemes = getDummyHotThemes()
            activity?.runOnUiThread {
                val adapter = hotThemeRecyclerView.adapter as? HotThemeAdapter
                adapter?.updateData(hotThemes)
            }
            
            // 투자 트렌드 더미 데이터
            val trendDays = getTrendData()
            activity?.runOnUiThread {
                trendDayAdapter.updateData(trendDays)
            }
            
            // 노려볼만한 테마 더미 데이터
            try {
                val promisingThemes = getDummyPromisingThemes()
                activity?.runOnUiThread {
                    promisingThemeAdapter.updateData(promisingThemes)
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "노려볼만한 테마 더미 데이터 로드 실패", e)
            }
            
            // 상승 종목 더미 데이터
            val topRisingStocks = getTopRisingStockData()
            activity?.runOnUiThread {
                val pageIndex = TopRisingStocksPagerAdapter.getPageIndexByPeriod(currentPeriod)
                topRisingStocksPagerAdapter.updatePageData(pageIndex, topRisingStocks)
            }
            
        } catch (e: Exception) {
            Log.e("HomeFragment", "더미 데이터 로드 실패", e)
        }
    }
    
    // 노려볼만한 테마 더미 데이터 메서드 추가
    private fun getDummyPromisingThemes(): List<PromisingTheme> {
        return listOf(
            PromisingTheme(
                id = "battery",
                name = "2차전지",
                logoUrl = "https://antwinner.com/api/image/2차전지.png",
                stockNames = listOf("LG에너지솔루션", "삼성SDI", "SK이노베이션"),
                isHot = true
            ),
            PromisingTheme(
                id = "ai",
                name = "인공지능(AI)",
                logoUrl = "https://antwinner.com/api/image/인공지능(AI).png",
                stockNames = listOf("KT", "삼성전자", "네이버"),
                isHot = true
            ),
            PromisingTheme(
                id = "semiconductor",
                name = "반도체",
                logoUrl = "https://antwinner.com/api/image/반도체.png",
                stockNames = listOf("SK하이닉스", "삼성전자", "DB하이텍"),
                isHot = false
            ),
            PromisingTheme(
                id = "autonomous",
                name = "자율주행",
                logoUrl = "https://antwinner.com/api/image/자율주행.png",
                stockNames = listOf("현대모비스", "만도", "SNT모티브"),
                isHot = false
            ),
            PromisingTheme(
                id = "foldable",
                name = "폴더블폰",
                logoUrl = "https://antwinner.com/api/image/폴더블폰.png",
                stockNames = listOf("삼성전자", "LG이노텍", "파트론"),
                isHot = false
            )
        )
    }
} 