package com.example.antwinner_kotlin.ui.stockdetail

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View

import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import timber.log.Timber
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

import android.view.ViewGroup
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import com.example.antwinner_kotlin.utils.SystemBarUtils

class StockDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STOCK_NAME = "extra_stock_name"
        const val EXTRA_STOCK_CODE = "extra_stock_code"
        private const val TAG = "StockDetailActivity"  // Timber 태그
        private const val MAX_ITEMS_COLLAPSE = 5  // 접힌 상태에서 보여줄 최대 아이템 수
        

        
        // newIntent 메서드 추가
        fun newIntent(context: Context, stockName: String, stockCode: String = ""): android.content.Intent {
            return android.content.Intent(context, StockDetailActivity::class.java).apply {
                putExtra(EXTRA_STOCK_NAME, stockName)
                putExtra(EXTRA_STOCK_CODE, stockCode)
            }
        }
    }

    private var stockName: String = ""
    private var stockCode: String = ""
    
    // 첫 번째 섹션 UI 요소 선언
    private lateinit var tvStockName: TextView
    private lateinit var tvStockPrice: TextView
    private lateinit var tvChangeRate: TextView
    private lateinit var chipCategory: Chip
    private lateinit var tvInvestorSummary: TextView
    private lateinit var tvIndustry: TextView
    private lateinit var tvProducts: TextView
    private lateinit var tvInvestors: TextView
    private lateinit var tvAvgReturn: TextView
    private lateinit var chipGroupThemes: ChipGroup
    
    // 최근 상승 정보 섹션 UI 요소 선언
    private lateinit var tvRecentRiseHeader: TextView
    private lateinit var tvRecentRiseReason: TextView
    private lateinit var tvRiseHistoryCount: TextView
    private lateinit var btnRiseHistory: View
    
    // 삭제된 두 번째 섹션 UI 요소들 (더 이상 사용하지 않음)
    

    
    // 메인 탭 관련 변수
    private lateinit var mainTabLayout: TabLayout
    private lateinit var mainViewPager: ViewPager2
    private lateinit var mainTabPagerAdapter: MainTabPagerAdapter
    
    // API 서비스 생성
    private val apiService by lazy {
        // 로깅 인터셉터 설정
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        // SSL 신뢰 관리자 설정 (개발용, 실제 배포에는 사용하지 마세요)
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                // 개발 중이므로 모든 클라이언트 인증서 허용
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                // 개발 중이므로 모든 서버 인증서 허용
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        })

        // SSL 소켓 팩토리 설정
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        // 호스트네임 검증기 생성 - 모든 호스트네임 허용
        val hostnameVerifier = HostnameVerifier { _, _ -> true }
        
        // OkHttp 클라이언트 설정
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier(hostnameVerifier)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://antwinner.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        retrofit.create(StockApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_detail)

        // 시스템 바 인셋 적용
        applySystemBarInsets()

        // 인텐트에서 데이터 가져오기
        stockName = intent.getStringExtra(EXTRA_STOCK_NAME) ?: "알 수 없음"
        stockCode = intent.getStringExtra(EXTRA_STOCK_CODE) ?: "알 수 없음"
        
        Timber.tag(TAG).d("StockDetailActivity 생성됨 - 종목: $stockName ($stockCode)")
        
        // 스크롤 동작 개선을 위한 터치 이벤트 처리
        setupTouchEventHandling()
        
        // 뷰 초기화 및 리스너 설정
        initViews()
        
        // 네트워크 연결 테스트 - 다양한 URL로 시도
        testUrlVariations()
        
        // 기본 API 연결 테스트
        testApiConnection()
        
        // 첫 번째 섹션: 기본 주식 정보 가져오기
        if (stockName != "알 수 없음") {
            Timber.tag(TAG).d("유효한 종목명이 있습니다. API 호출 시작...")
            fetchStockData(stockName)
        } else {
            Timber.tag(TAG).e("유효한 종목명이 없습니다! 샘플 데이터로 대체합니다.")
            populateWithSampleData()
            Toast.makeText(this, "종목명이 없어 샘플 데이터를 표시합니다.", Toast.LENGTH_SHORT).show()
        }
        
        // 삭제된 두 번째 섹션 관련 코드 (더 이상 사용하지 않음)
        

    }
    
    private fun applySystemBarInsets() {
        val rootView = findViewById<View>(android.R.id.content)
        SystemBarUtils.applyTopPaddingInset(rootView, 16)
    }
    
    // 터치 이벤트 처리 설정 (개선된 버전)
    private fun setupTouchEventHandling() {
        try {
            // 메인 NestedScrollView 찾기
            val nestedScrollView = findViewById<androidx.core.widget.NestedScrollView>(R.id.content_scroll_view)
            
            nestedScrollView?.let { scrollView ->
                // 부드러운 스크롤 활성화
                scrollView.isSmoothScrollingEnabled = true
                
                // 페이딩 효과 활성화
                scrollView.isVerticalFadingEdgeEnabled = true
                scrollView.setFadingEdgeLength((40 * resources.displayMetrics.density).toInt())
                
                // 중첩 스크롤 활성화
                scrollView.isNestedScrollingEnabled = true
                
                // 삭제된 RecyclerView 관련 코드 (더 이상 사용하지 않음)
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "터치 이벤트 처리 설정 중 오류 발생")
        }
    }
    
    private fun initViews() {
        // 첫 번째 섹션 뷰 바인딩
        tvStockName = findViewById(R.id.tv_stock_name)
        tvStockPrice = findViewById(R.id.tv_stock_price)
        tvChangeRate = findViewById(R.id.tv_change_rate)
        chipCategory = findViewById(R.id.chip_category)
        tvInvestorSummary = findViewById(R.id.tv_investor_summary)
        tvIndustry = findViewById(R.id.tv_industry)
        tvProducts = findViewById(R.id.tv_products)
        tvInvestors = findViewById(R.id.tv_investors)
        tvAvgReturn = findViewById(R.id.tv_avg_return)
        chipGroupThemes = findViewById(R.id.chip_group_themes)
        
        // 최근 상승 정보 섹션 뷰 바인딩
        tvRecentRiseHeader = findViewById(R.id.tv_recent_rise_header)
        tvRecentRiseReason = findViewById(R.id.tv_recent_rise_reason)
        tvRiseHistoryCount = findViewById(R.id.tv_rise_history_count)
        btnRiseHistory = findViewById(R.id.btn_rise_history)
        
        // 테마 칩 표시
        chipCategory.visibility = View.VISIBLE
        
        // 상승 히스토리 버튼 클릭 리스너 - "왜 올랐을까?" 탭으로 이동
        btnRiseHistory.setOnClickListener {
            // "왜 올랐을까?" 탭 (인덱스 1)으로 이동
            if (::mainViewPager.isInitialized) {
                mainViewPager.currentItem = 1
                Timber.tag(TAG).d("상승 히스토리 버튼 클릭 - '왜 올랐을까?' 탭으로 이동")
            } else {
                Toast.makeText(this, "탭 초기화 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 삭제된 두 번째 섹션 관련 코드들 (더 이상 사용하지 않음)
        

        
        // 메인 탭 설정 (차트, 왜 올랐을까?, 종목정보)
        setupMainTabs()
    }
    
    // 메인 탭 설정 (차트, 왜 올랐을까?, 종목정보)
    private fun setupMainTabs() {
        mainTabLayout = findViewById(R.id.main_tab_layout)
        mainViewPager = findViewById(R.id.main_view_pager)
        
        // 메인 탭 어댑터 설정
        mainTabPagerAdapter = MainTabPagerAdapter(this, stockCode, stockName)
        mainViewPager.adapter = mainTabPagerAdapter
        
        // 스와이프 비활성화 (터치로만 탭 전환 가능)
        mainViewPager.isUserInputEnabled = false
        
        // 탭과 뷰페이저 연결
        TabLayoutMediator(mainTabLayout, mainViewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "차트"
                1 -> tab.text = "왜 올랐을까?"
                2 -> tab.text = "종목정보"
            }
        }.attach()
        
        // 기본으로 차트 탭 선택
        mainViewPager.currentItem = 0
    }
    
    // 삭제된 하단 탭 레이아웃 설정 함수 (더 이상 사용하지 않음)
    
    private fun fetchStockData(stockName: String) {
        Timber.tag(TAG).d("종목 정보 불러오기 시작: $stockName")
        
        // URL 인코딩 적용
        val encodedStockName = try {
            java.net.URLEncoder.encode(stockName.trim(), "UTF-8")
        } catch (e: Exception) {
            stockName.trim()
        }
        
        Timber.tag(TAG).d("인코딩된 종목명: $encodedStockName")
        
        try {
            // API 호출 URL 로깅
            val call = apiService.getStockDetail(encodedStockName)
            Timber.tag(TAG).d("API 호출 URL: ${call.request().url}")
            
            call.enqueue(object : Callback<List<StockDetailResponse>> {
                override fun onResponse(
                    call: Call<List<StockDetailResponse>>,
                    response: Response<List<StockDetailResponse>>
                ) {
                    Timber.tag(TAG).d("API 응답 수신: 코드=${response.code()}")
                    
                    if (response.isSuccessful) {
                        val stockDetailList = response.body()
                        if (!stockDetailList.isNullOrEmpty()) {
                            val stockDetail = stockDetailList[0]  // 첫 번째 항목 사용
                            Timber.tag(TAG).d("API 응답 성공: ${stockDetail.companyName} 데이터 로드됨")
                            Timber.tag(TAG).d("응답 데이터: $stockDetail")
                            updateUI(stockDetail)
                        } else {
                            Timber.tag(TAG).e("API 응답은 성공했지만 데이터가 비어 있습니다")
                            
                            // 원본 종목명으로 다시 시도
                            if (encodedStockName != stockName.trim()) {
                                Timber.tag(TAG).d("원본 종목명으로 재시도: ${stockName.trim()}")
                                retryWithOriginalStockName(stockName.trim())
                            } else {
                                showError("해당 종목의 데이터를 찾을 수 없습니다: $stockName")
                                populateWithSampleData()
                            }
                        }
                    } else {
                        Timber.tag(TAG).e("API 오류 - 코드: ${response.code()}, 메시지: ${response.message()}")
                        
                        // 404 오류인 경우 원본 종목명으로 재시도
                        if (response.code() == 404 && encodedStockName != stockName.trim()) {
                            Timber.tag(TAG).d("404 오류로 원본 종목명으로 재시도: ${stockName.trim()}")
                            retryWithOriginalStockName(stockName.trim())
                        } else {
                            try {
                                val errorBody = response.errorBody()?.string()
                                Timber.tag(TAG).e("에러 바디: $errorBody")
                            } catch (e: Exception) {
                                Timber.tag(TAG).e(e, "에러 바디 파싱 실패")
                            }
                            showError("종목 정보를 가져올 수 없습니다 (${response.code()}): $stockName")
                            populateWithSampleData()
                        }
                    }
                }

                override fun onFailure(call: Call<List<StockDetailResponse>>, t: Throwable) {
                    Timber.tag(TAG).e(t, "API 호출 실패: URL=${call.request().url}, 에러=${t.message}")
                    
                    // 네트워크 오류인 경우와 일반 오류 구분
                    when (t) {
                        is java.net.UnknownHostException -> {
                            showError("네트워크 연결을 확인해주세요")
                        }
                        is java.net.SocketTimeoutException -> {
                            showError("서버 응답 시간이 초과되었습니다")
                        }
                        else -> {
                            showError("종목 정보를 가져오는 중 오류가 발생했습니다: $stockName")
                        }
                    }
                    
                    // 오류 발생시 임시 데이터로 UI 채우기
                    populateWithSampleData()
                }
            })
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "API 호출 준비 중 예외 발생")
            showError("API 호출 준비 오류: ${e.message}")
            populateWithSampleData()
        }
    }
    
    // 원본 종목명으로 재시도하는 메서드 추가
    private fun retryWithOriginalStockName(originalStockName: String) {
        Timber.tag(TAG).d("원본 종목명으로 재시도: $originalStockName")
        
        try {
            val call = apiService.getStockDetail(originalStockName)
            Timber.tag(TAG).d("재시도 API 호출 URL: ${call.request().url}")
            
            call.enqueue(object : Callback<List<StockDetailResponse>> {
                override fun onResponse(
                    call: Call<List<StockDetailResponse>>,
                    response: Response<List<StockDetailResponse>>
                ) {
                    if (response.isSuccessful) {
                        val stockDetailList = response.body()
                        if (!stockDetailList.isNullOrEmpty()) {
                            val stockDetail = stockDetailList[0]
                            Timber.tag(TAG).d("재시도 API 응답 성공: ${stockDetail.companyName}")
                            updateUI(stockDetail)
                        } else {
                            Timber.tag(TAG).e("재시도에서도 데이터가 비어 있습니다")
                            // 마지막 시도: 대체 API 엔드포인트 사용
                            tryAlternativeApiEndpoint(originalStockName)
                        }
                    } else {
                        Timber.tag(TAG).e("재시도 API 오류 - 코드: ${response.code()}")
                        // 마지막 시도: 대체 API 엔드포인트 사용
                        tryAlternativeApiEndpoint(originalStockName)
                    }
                }

                override fun onFailure(call: Call<List<StockDetailResponse>>, t: Throwable) {
                    Timber.tag(TAG).e(t, "재시도 API 호출 실패: ${t.message}")
                    // 마지막 시도: 대체 API 엔드포인트 사용
                    tryAlternativeApiEndpoint(originalStockName)
                }
            })
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "재시도 API 호출 준비 중 예외 발생")
            tryAlternativeApiEndpoint(originalStockName)
        }
    }
    
    // 대체 API 엔드포인트 시도 메서드 추가
    private fun tryAlternativeApiEndpoint(stockName: String) {
        Timber.tag(TAG).d("대체 API 엔드포인트 시도: $stockName")
        
        // 여기서는 기존 엔드포인트로 한번 더 시도하고, 
        // 실제 운영에서는 다른 베이스 URL이나 엔드포인트를 사용할 수 있음
        
        try {
            // 현재는 샘플 데이터로 대체하지만, 
            // 실제로는 다른 API 서버나 엔드포인트를 시도할 수 있음
            Timber.tag(TAG).w("모든 API 시도 실패, 샘플 데이터 사용: $stockName")
            showError("'$stockName' 종목 정보를 서버에서 가져올 수 없습니다")
            populateWithSampleData()
            
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "대체 API 시도 중 오류")
            showError("모든 API 시도 실패: ${e.message}")
            populateWithSampleData()
        }
    }
    
    private fun updateUI(data: StockDetailResponse) {
        // 주식 이름과 현재가 업데이트
        tvStockName.text = data.companyName
        tvStockPrice.text = data.currentPrice + "원"
        
        // 변동률 업데이트 (등락률 앞에 상승/하락/보합 상태 텍스트 추가)
        val changeRate = data.changeRate.trim()
        val changeStatus = when {
            changeRate == "0.00%" || changeRate == "0%" -> "보합 "
            changeRate.startsWith("+") -> "상승 "
            changeRate.startsWith("-") -> "하락 "
            else -> ""  // 부호가 없는 경우는 그대로 표시
        }
        
        tvChangeRate.text = changeStatus + changeRate
        
        // 변동률에 따라 색상 설정
        val textColor = when {
            changeRate == "0.00%" || changeRate == "0%" -> getColor(R.color.price_unchanged)
            changeRate.startsWith("+") -> getColor(R.color.price_up) 
            changeRate.startsWith("-") -> getColor(R.color.price_down)
            else -> getColor(android.R.color.black)  // 기본값
        }
        tvChangeRate.setTextColor(textColor)
        
        // 테마 정보를 칩에 표시 (테마 목록의 첫 번째 항목 사용)
        if (data.themeList.isNotEmpty()) {
            val themeName = data.themeList[0]
            chipCategory.text = themeName
            
            // 테마 이미지 동적 로딩
            loadThemeImage(themeName)
        } else {
            chipCategory.text = "테마 정보 없음"
        }
        
        // 투자자 정보를 한 줄로 표시 (색상 분리 적용)
        updateInvestorSummaryWithColors(data.totalInvestors, data.averageReturn)
        
        // 최근 상승 정보는 별도 API에서 가져옴
        fetchRecentRiseData(data.companyName)
        
        // 기존 상세 정보는 여전히 저장 (숨겨진 뷰에)
        tvIndustry.text = data.industry
        tvProducts.text = data.mainProduct
        tvInvestors.text = data.totalInvestors
        tvAvgReturn.text = data.averageReturn
        
        val returnPercentage = parsePercentage(data.averageReturn)
        tvAvgReturn.setTextColor(getColorForPercentage(returnPercentage))
        
        // 테마 목록이 있다면 테마 칩 추가 (필요시)
        updateThemeChips(data.themeList)
    }
    
    // 투자자 정보 색상 분리 적용
    private fun updateInvestorSummaryWithColors(totalInvestors: String, averageReturn: String) {
        val fullText = "투자자 수 ${totalInvestors}  평균 수익률 ${averageReturn}"
        val spannableString = android.text.SpannableString(fullText)
        
        // 기본 색상을 짙은 회색으로 설정
        val baseColor = getColor(R.color.text_primary) // #333333 또는 짙은 회색
        spannableString.setSpan(
            android.text.style.ForegroundColorSpan(baseColor),
            0,
            fullText.length,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // 투자자 수 부분 볼드 처리
        val investorStartIndex = fullText.indexOf(totalInvestors)
        if (investorStartIndex != -1) {
            spannableString.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                investorStartIndex,
                investorStartIndex + totalInvestors.length,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        // 수익률 부분만 색상 적용
        val returnStartIndex = fullText.indexOf(averageReturn)
        if (returnStartIndex != -1) {
            val returnPercentage = parsePercentage(averageReturn)
            val returnColor = when {
                returnPercentage > 0 -> getColor(R.color.price_up) // 빨간색
                returnPercentage < 0 -> getColor(R.color.price_down) // 파란색
                else -> getColor(R.color.price_unchanged) // 회색
            }
            
            // 수익률 색상 적용
            spannableString.setSpan(
                android.text.style.ForegroundColorSpan(returnColor),
                returnStartIndex,
                returnStartIndex + averageReturn.length,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            
            // 수익률 볼드 처리
            spannableString.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                returnStartIndex,
                returnStartIndex + averageReturn.length,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        tvInvestorSummary.text = spannableString
    }
    
    // 테마 이미지 로딩
    private fun loadThemeImage(themeName: String) {
        try {
            // 테마 이미지 URL 생성: antwinner.com/api/image/테마.png
            val encodedThemeName = java.net.URLEncoder.encode(themeName, "UTF-8")
            val imageUrl = "https://antwinner.com/api/image/${encodedThemeName}.png"
            Timber.tag(TAG).d("테마 이미지 로딩 시도: $imageUrl")
            
            // Glide를 사용하여 이미지를 원형으로 로드하고 Drawable로 변환하여 칩 아이콘에 설정
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(48, 48) // 칩 아이콘 크기에 맞게 조정
                .transform(CircleCrop()) // 원형으로 변환
                .placeholder(R.drawable.ic_chip_semiconductor) // 로딩 중 기본 아이콘
                .error(R.drawable.ic_chip_semiconductor) // 에러 시 기본 아이콘
            
            Glide.with(this)
                .asDrawable()
                .load(imageUrl)
                .apply(requestOptions)
                .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable,
                        transition: com.bumptech.glide.request.transition.Transition<in android.graphics.drawable.Drawable>?
                    ) {
                        // 이미지 로드 성공 시 칩 아이콘에 설정
                        chipCategory.chipIcon = resource
                        Timber.tag(TAG).d("테마 이미지 로드 성공 (원형): $themeName")
                    }
                    
                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                        // 기본 아이콘 설정
                        chipCategory.chipIcon = placeholder ?: getDrawable(R.drawable.ic_chip_semiconductor)
                    }
                    
                    override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                        // 이미지 로드 실패 시 기본 아이콘 설정
                        chipCategory.chipIcon = errorDrawable ?: getDrawable(R.drawable.ic_chip_semiconductor)
                        Timber.tag(TAG).w("테마 이미지 로드 실패, 기본 아이콘 사용: $themeName")
                    }
                })
            
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "테마 이미지 로딩 실패: $themeName")
            // 기본 아이콘 사용
            chipCategory.chipIcon = getDrawable(R.drawable.ic_chip_semiconductor)
        }
    }
    
    private fun updateThemeChips(themes: List<String>) {
        // 기존 칩 모두 제거
        chipGroupThemes.removeAllViews()
        
        // 각 테마에 대해 칩 생성 및 추가
        themes.forEach { theme ->
            val chip = Chip(this).apply {
                text = theme
                isClickable = true
                isCheckable = false
                chipBackgroundColor = resources.getColorStateList(R.color.chip_background, null)
                setTextColor(getColor(R.color.chip_text))
            }
            chipGroupThemes.addView(chip)
        }
    }
    
    private fun parsePercentage(percentStr: String): Float {
        return try {
            percentStr.replace("%", "").replace(",", "").toFloat()
        } catch (e: Exception) {
            0f
        }
    }
    
    private fun getColorForPercentage(percentage: Float): Int {
        return when {
            percentage > 0 -> getColor(R.color.price_up)
            percentage < 0 -> getColor(R.color.price_down)
            else -> getColor(R.color.price_unchanged)
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Timber.tag(TAG).e(message)
    }
    
    private fun populateWithSampleData() {
        Timber.tag(TAG).w("샘플 데이터로 UI를 채웁니다 (API 오류 또는 테스트용)")
        
        // 실제 종목명이 있으면 사용, 없으면 기본값 사용
        val displayStockName = if (stockName != "알 수 없음" && stockName.isNotBlank()) {
            stockName
        } else {
            "마이크로컨텍솔"
        }
        
        // 샘플 데이터로 UI 채우기 (첫 번째 이미지 스타일로)
        tvStockName.text = displayStockName
        tvStockPrice.text = "13,710원"
        tvChangeRate.text = "(+29.95%)"
        tvChangeRate.setTextColor(getColor(R.color.price_up))
        
        // 테마 칩 설정
        chipCategory.text = "반도체(인공지능)"
        loadThemeImage("반도체(인공지능)")
        
        // 투자자 정보를 한 줄로 표시 (색상 분리 적용)
        updateInvestorSummaryWithColors("48,142명", "+14.52%")
        
        // 최근 상승 정보도 API에서 가져옴 (샘플 데이터에서도)
        fetchRecentRiseData(displayStockName)
        
        // 기존 상세 정보도 설정 (숨겨진 뷰에)
        tvIndustry.text = "소프트웨어 개발 및 공급업"
        tvProducts.text = "모바일 앱 개발 플랫폼"
        tvInvestors.text = "48,142명"
        tvAvgReturn.text = "+14.52%"
        tvAvgReturn.setTextColor(getColor(R.color.price_up))
        
        // 실제 종목명을 포함한 메시지 표시
        val message = if (stockName != "알 수 없음" && stockName.isNotBlank()) {
            "'$stockName' 종목의 실시간 데이터를 가져올 수 없어 샘플 데이터를 표시합니다."
        } else {
            "샘플 데이터가 표시됩니다 (API 연결 오류)"
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * 다양한 URL 변형으로 API 연결을 시도합니다.
     * 개발 중에 올바른 API 엔드포인트를 찾는데 도움이 됩니다.
     */
    private fun testUrlVariations() {
        Timber.tag(TAG).d("다양한 URL로 연결 테스트 시작")
        
        // 테스트할 URL 목록
        val urls = listOf(
            "https://antwinner.com/api/stock_people/${java.net.URLEncoder.encode(stockName, "UTF-8")}",
            "https://antwinner.com/api/stock_people/파마리서치",
            "https://antwinner.com/api/ping",
            "https://antwinner.com/api/stocks",
            "http://antwinner.com/api/stock_people/${java.net.URLEncoder.encode(stockName, "UTF-8")}",
            "http://antwinner.com/api/stock_people/파마리서치"
        )
        
        // SSL 신뢰 관리자 설정 (개발용)
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        // SSL 설정
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        
        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
        
        // 각 URL마다 순차적으로 테스트
        Thread {
            urls.forEach { url ->
                try {
                    Timber.tag(TAG).d("URL 테스트 중: $url")
                    val request = okhttp3.Request.Builder()
                        .url(url)
                        .build()
                    
                    val response = client.newCall(request).execute()
                    val isSuccessful = response.isSuccessful
                    val code = response.code
                    val message = response.message
                    val body = response.body?.string()?.take(100) ?: "빈 응답"
                    
                    Timber.tag(TAG).d("URL 테스트 결과 ($url): 성공=$isSuccessful, 코드=$code, 응답=$body")
                    
                    // 성공적인 응답이 있으면 로그에만 기록 (Toast 제거)
                    if (isSuccessful) {
                        Timber.tag(TAG).d("성공한 URL: $url (코드: $code)")
                        // 첫 번째 성공적인 URL을 발견하면 나머지 테스트는 중단
                        return@forEach
                    }
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "URL 테스트 실패 ($url): ${e.message}")
                }
                
                // URL 사이에 약간의 지연을 줌
                Thread.sleep(500)
            }
        }.start()
    }

    /**
     * 기본 API 서버 연결을 테스트합니다.
     */
    private fun testApiConnection() {
        Timber.tag(TAG).d("API 서버 연결 테스트 시작")
        
        // SSL 신뢰 관리자 설정 (개발용)
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        // SSL 설정
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
        
        val request = okhttp3.Request.Builder()
            .url("https://antwinner.com/api/stock_people/파마리서치")
            .build()
        
        Thread {
            try {
                val response = client.newCall(request).execute()
                val isSuccessful = response.isSuccessful
                val code = response.code
                val message = response.message
                val body = response.body?.string() ?: "빈 응답"
                
                runOnUiThread {
                    if (isSuccessful) {
                        Timber.tag(TAG).d("API 서버 연결 테스트 성공: $code $message - $body")
                        // Toast 제거
                    } else {
                        Timber.tag(TAG).e("API 서버 연결 테스트 실패: $code $message - $body")
                        // Toast 제거
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    val errorMsg = when (e) {
                        is javax.net.ssl.SSLPeerUnverifiedException -> 
                            "SSL 인증서 오류: ${e.message} (SSL 옵션을 수정했으므로 다시 시도해보세요)"
                        is java.net.UnknownHostException -> 
                            "알 수 없는 호스트: ${e.message} (서버 주소가 올바른지 확인하세요)"
                        is java.net.SocketTimeoutException -> 
                            "서버 연결 시간 초과: ${e.message}"
                        else -> "API 서버 연결 실패: ${e.message}"
                    }
                    
                    Timber.tag(TAG).e(e, "API 서버 연결 테스트 예외 발생: $errorMsg")
                    // Toast 제거
                }
            }
        }.start()
    }

    // 삭제된 아이템 가시성 토글 관련 메서드들 (더 이상 사용하지 않음)

    // 삭제된 크게 상승한 날 관련 메서드들 (더 이상 사용하지 않음)
    
    /**
     * 최근 상승 데이터를 API에서 가져와서 UI 업데이트
     */
    private fun fetchRecentRiseData(stockName: String) {
        Timber.tag(TAG).d("최근 상승 데이터 불러오기 시작: $stockName")
        
        // WhyRiseFragment와 동일한 방식으로 처리 - URL 인코딩 제거
        val cleanStockName = stockName.trim()
        
        try {
            val call = apiService.getBigRiseDays(cleanStockName)
            Timber.tag(TAG).d("최근 상승 API 호출 URL: ${call.request().url}")
            
            call.enqueue(object : Callback<List<BigRiseDayResponse>> {
                override fun onResponse(
                    call: Call<List<BigRiseDayResponse>>,
                    response: Response<List<BigRiseDayResponse>>
                ) {
                    Timber.tag(TAG).d("최근 상승 API 응답 코드: ${response.code()}")
                    
                    if (response.isSuccessful) {
                        val riseDataList = response.body()
                        if (!riseDataList.isNullOrEmpty()) {
                            Timber.tag(TAG).d("최근 상승 API 응답 성공: ${riseDataList.size}개 데이터")
                            updateRecentRiseUIFromBigRiseData(riseDataList)
                        } else {
                            Timber.tag(TAG).w("최근 상승 데이터가 비어 있음 - WhyRise 방식으로 재시도")
                            fetchRecentRiseDataAlternative(cleanStockName)
                        }
                    } else {
                        Timber.tag(TAG).e("최근 상승 API 오류 - 코드: ${response.code()} - WhyRise 방식으로 재시도")
                        fetchRecentRiseDataAlternative(cleanStockName)
                    }
                }

                override fun onFailure(call: Call<List<BigRiseDayResponse>>, t: Throwable) {
                    Timber.tag(TAG).e(t, "최근 상승 API 호출 실패: ${t.message} - WhyRise 방식으로 재시도")
                    fetchRecentRiseDataAlternative(cleanStockName)
                }
            })
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "최근 상승 API 호출 준비 중 예외 발생")
            fetchRecentRiseDataAlternative(cleanStockName)
        }
    }
    
    /**
     * Retrofit 인스턴스 생성 (apiService와 동일한 설정)
     */
    private fun createRetrofitInstance(): Retrofit {
        // 로깅 인터셉터 설정
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        // SSL 신뢰 관리자 설정 (개발용)
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        val hostnameVerifier = HostnameVerifier { _, _ -> true }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier(hostnameVerifier)
            .build()
        
        return Retrofit.Builder()
            .baseUrl("https://antwinner.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * WhyRiseFragment와 동일한 방식으로 API 재시도
     */
    private fun fetchRecentRiseDataAlternative(stockName: String) {
        Timber.tag(TAG).d("WhyRise 방식으로 재시도: $stockName")
        
        // WhyRiseFragment와 동일한 API 서비스 생성 (동일한 retrofit 인스턴스 사용)
        val retrofit = createRetrofitInstance()
        val whyRiseApiService = retrofit.create(WhyRiseApiService::class.java)
        
        try {
            val call = whyRiseApiService.getWhyRiseData(stockName)
            call.enqueue(object : Callback<List<WhyRiseApiResponse>> {
                override fun onResponse(
                    call: Call<List<WhyRiseApiResponse>>,
                    response: Response<List<WhyRiseApiResponse>>
                ) {
                    Timber.tag(TAG).d("WhyRise API 응답 코드: ${response.code()}")
                    
                    if (response.isSuccessful) {
                        val apiData = response.body()
                        if (!apiData.isNullOrEmpty()) {
                            Timber.tag(TAG).d("WhyRise API 응답 성공: ${apiData.size}개 데이터")
                            updateRecentRiseUIFromWhyRiseData(apiData)
                        } else {
                            Timber.tag(TAG).w("WhyRise API도 빈 데이터 - 기본값 표시")
                            showDefaultRecentRiseData()
                        }
                    } else {
                        Timber.tag(TAG).e("WhyRise API 오류 - 코드: ${response.code()}")
                        showDefaultRecentRiseData()
                    }
                }

                override fun onFailure(call: Call<List<WhyRiseApiResponse>>, t: Throwable) {
                    Timber.tag(TAG).e(t, "WhyRise API 호출 실패: ${t.message}")
                    showDefaultRecentRiseData()
                }
            })
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "WhyRise API 호출 준비 중 예외 발생")
            showDefaultRecentRiseData()
        }
    }

    /**
     * BigRiseDayResponse 데이터로 UI 업데이트
     */
    private fun updateRecentRiseUIFromBigRiseData(riseDataList: List<BigRiseDayResponse>) {
        if (riseDataList.isEmpty()) {
            showDefaultRecentRiseData()
            return
        }
        
        // 가장 최근 데이터 (첫 번째) 사용
        val latestRise = riseDataList[0]
        
        // 날짜 계산 (며칠 전인지)
        val daysAgo = calculateDaysAgo(latestRise.date)
        val period = if (daysAgo <= 0) "오늘" else "${daysAgo}일전"
        
        // 상승률 포맷팅 (API에서 0.2224 = 22.24%로 제공)
        val riseRatePercent = latestRise.riseRate * 100
        val riseRate = String.format("▲ %.1f%%", riseRatePercent)
        
        Timber.tag(TAG).d("BigRise 데이터로 UI 업데이트: 날짜=${latestRise.date}, ${daysAgo}일전, 상승률=${latestRise.riseRate} -> $riseRate")
        
        // 헤더 업데이트
        updateRecentRiseHeader(period, riseRate)
        
        // 상승 이유 업데이트 (대괄호 부분 제거)
        val cleanReason = cleanRiseReason(latestRise.riseReason)
        tvRecentRiseReason.text = cleanReason
        
        // 히스토리 개수 업데이트
        tvRiseHistoryCount.text = "${riseDataList.size}개 상승 히스토리 보기"
    }
    
    /**
     * WhyRiseApiResponse 데이터로 UI 업데이트 (WhyRiseFragment와 동일한 데이터)
     */
    private fun updateRecentRiseUIFromWhyRiseData(apiData: List<WhyRiseApiResponse>) {
        if (apiData.isEmpty()) {
            showDefaultRecentRiseData()
            return
        }
        
        // 가장 최근 데이터 (첫 번째) 사용
        val latestRise = apiData[0]
        
        // 날짜 계산 (며칠 전인지)
        val daysAgo = calculateDaysAgo(latestRise.날자)
        val period = if (daysAgo <= 0) "오늘" else "${daysAgo}일전"
        
        // 상승률 포맷팅 (WhyRiseFragment와 동일한 방식)
        val riseRatePercent = latestRise.상승률 * 100
        val riseRate = String.format("▲ %.1f%%", riseRatePercent)
        
        Timber.tag(TAG).d("WhyRise 데이터로 UI 업데이트: 날짜=${latestRise.날자}, ${daysAgo}일전, 상승률=${latestRise.상승률} -> $riseRate")
        
        // 헤더 업데이트
        updateRecentRiseHeader(period, riseRate)
        
        // 상승 이유 업데이트 (대괄호 부분 제거)
        val cleanReason = cleanRiseReason(latestRise.상승이유)
        tvRecentRiseReason.text = cleanReason
        
        // 히스토리 개수 업데이트
        tvRiseHistoryCount.text = "${apiData.size}개 상승 히스토리 보기"
    }
    
    /**
     * 기본 상승 데이터 표시 (API 오류 시)
     */
    private fun showDefaultRecentRiseData() {
        updateRecentRiseHeader("데이터 없음", "")
        tvRecentRiseReason.text = "최근 상승 정보를 불러올 수 없습니다"
        tvRiseHistoryCount.text = "상승 히스토리 보기"
    }
    
    /**
     * 날짜 차이 계산 (며칠 전인지)
     */
    private fun calculateDaysAgo(dateString: String?): Int {
        if (dateString.isNullOrEmpty()) return 0
        
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val riseDate = dateFormat.parse(dateString)
            val currentDate = Date()
            
            if (riseDate != null) {
                val diffInMillis = currentDate.time - riseDate.time
                val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
                diffInDays
            } else {
                0
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "날짜 계산 오류: $dateString")
            0
        }
    }
    
    /**
     * 상승 이유에서 대괄호 부분 제거
     */
    private fun cleanRiseReason(reason: String?): String {
        if (reason.isNullOrEmpty()) return "상승 이유 정보 없음"
        
        // [대괄호] 부분 제거
        val cleanedReason = reason.replace(Regex("\\[.*?\\]\\s*"), "")
        return cleanedReason.trim().takeIf { it.isNotEmpty() } ?: "상승 이유 정보 없음"
    }

    // 최근 상승 헤더 색상 적용
    private fun updateRecentRiseHeader(period: String, riseRate: String) {
        val fullText = if (riseRate.isEmpty()) {
            "최근 상승 $period"
        } else {
            "최근 상승 $period $riseRate"
        }
        val spannableString = android.text.SpannableString(fullText)
        
        // 기본 색상을 짙은 회색으로 설정
        val baseColor = getColor(R.color.text_primary)
        spannableString.setSpan(
            android.text.style.ForegroundColorSpan(baseColor),
            0,
            fullText.length,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // 등락률 부분만 빨간색 적용 (있는 경우에만)
        if (riseRate.isNotEmpty()) {
            val rateStartIndex = fullText.indexOf(riseRate)
            if (rateStartIndex != -1) {
                val rateColor = getColor(R.color.price_up) // 빨간색
                spannableString.setSpan(
                    android.text.style.ForegroundColorSpan(rateColor),
                    rateStartIndex,
                    rateStartIndex + riseRate.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        
        tvRecentRiseHeader.text = spannableString
    }
}

// API 인터페이스 (getStockDetail, getBigRiseDays, getChartData 복원)
interface StockApiService {
    @GET("api/stock_people/{stockName}")
    fun getStockDetail(@Path("stockName", encoded = false) stockName: String): Call<List<StockDetailResponse>>
    
    @GET("api/stocks/{stockName}")
    fun getBigRiseDays(@Path("stockName", encoded = false) stockName: String): Call<List<BigRiseDayResponse>>
    
    @GET("api/chart/{stockName}")
    fun getChartData(@Path("stockName", encoded = false) stockName: String): Call<List<ChartData>>
}

// WhyRise API 인터페이스 (WhyRiseFragment와 동일)
interface WhyRiseApiService {
    @GET("api/stocks/{stockName}")
    fun getWhyRiseData(@Path("stockName") stockName: String): Call<List<WhyRiseApiResponse>>
}

// 데이터 모델 클래스 (StockDetailResponse, BigRiseDayResponse, BigRiseDayItem 유지, ChartData 복원)
data class StockDetailResponse(
    @SerializedName("industry") val industry: String = "",
    @SerializedName("main_product") val mainProduct: String = "",
    @SerializedName("날짜") val date: String = "",
    @SerializedName("등락률") val changeRate: String = "",
    @SerializedName("전일비") val prevDayChange: String = "",
    @SerializedName("종목코드") val stockCode: String = "",
    @SerializedName("총투자자") val totalInvestors: String = "",
    @SerializedName("테마목록") val themeList: List<String> = emptyList(),
    @SerializedName("평균단가") val averagePrice: String = "",
    @SerializedName("평균수익률") val averageReturn: String = "",
    @SerializedName("현재가") val currentPrice: String = "",
    @SerializedName("회사명") val companyName: String = ""
)

data class BigRiseDayResponse(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("거래대금") val tradeAmount: String? = "",
    @SerializedName("거래량") val tradeVolume: String? = "",
    @SerializedName("날자") val date: String? = "",
    @SerializedName("상승률") val riseRate: Double = 0.0,
    @SerializedName("상승이유") val riseReason: String? = "",
    @SerializedName("종목명") val stockName: String? = "",
    @SerializedName("종목코드") val stockCode: Int = 0,
    @SerializedName("테마") val theme: String? = ""
)

data class BigRiseDayItem(
    val stockName: String,
    val changeRate: String,
    val profileImageUrl: String,
    val personName: String,
    val eventDescription: String,
    val date: String,
    val tradeAmount: String,
    val tradeVolume: String,
    val themeText: String = "" 
)

// ChartData 데이터 모델 복원
data class ChartData(
    @SerializedName("Change") val Change: Double,
    @SerializedName("Close") val Close: Double,
    @SerializedName("High") val High: Double,
    @SerializedName("Low") val Low: Double,
    @SerializedName("Open") val Open: Double,
    @SerializedName("Volume") val Volume: Long,
    @SerializedName("date") val date: String
)

// WhyRise API 응답 데이터 클래스 (WhyRiseFragment와 동일)
data class WhyRiseApiResponse(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("거래대금") val 거래대금: String? = null,
    @SerializedName("거래량") val 거래량: String? = null,
    @SerializedName("날자") val 날자: String? = null,
    @SerializedName("상승률") val 상승률: Double = 0.0,
    @SerializedName("상승이유") val 상승이유: String? = null,
    @SerializedName("종목명") val 종목명: String? = null,
    @SerializedName("종목코드") val 종목코드: String? = null,
    @SerializedName("테마") val 테마: String? = null
)
