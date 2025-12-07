package com.example.antwinner_kotlin.ui.stockdetail.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.utils.MPPointF
import android.widget.Toast
import android.graphics.Canvas
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class StockInfoFragment : Fragment() {

    companion object {
        private const val TAG = "StockInfoFragment"
        private const val BASE_URL = "https://antwinner.com/api/"
        
        fun newInstance(stockName: String? = null): StockInfoFragment {
            Log.d(TAG, "Creating new StockInfoFragment instance for stock: $stockName")
            return StockInfoFragment().apply {
                arguments = Bundle().apply {
                    putString("stockName", stockName)
                }
            }
        }
    }

    private lateinit var tradingTrendRecyclerView: RecyclerView
    private lateinit var tradingTrendAdapter: TradingTrendFullAdapter
    private lateinit var apiService: TradingTrendApiService
    private lateinit var financialApiService: FinancialApiService
    private lateinit var investmentIndicatorApiService: InvestmentIndicatorApiService
    private lateinit var comprehensiveAnalysisApiService: ComprehensiveAnalysisApiService
    
    // 실적 차트 관련 UI 요소들
    private lateinit var barChartPerformance: BarChart
    private lateinit var tvPerformanceAnnual: TextView
    private lateinit var tvPerformanceQuarterly: TextView
    private lateinit var tvRevenueSummary: TextView
    private lateinit var tvOperatingProfitSummary: TextView
    private lateinit var tvNetIncomeSummary: TextView
    private lateinit var tvRevenueYear: TextView
    private lateinit var tvOperatingProfitYear: TextView
    private lateinit var tvNetIncomeYear: TextView
    
    private var isAnnualSelected = true // 연간 선택 상태
    private var stockName: String? = null
    
    // 현재 차트 데이터 저장 (클릭 이벤트용)
    private var currentAnnualData: List<PerformanceData> = emptyList()
    private var currentQuarterlyData: List<QuarterlyPerformanceData> = emptyList()
    
    // 투자 지표 관련 UI 요소들 (5단계 시스템)
    private lateinit var tvOverallComment: TextView
    private lateinit var attractionStar1: View
    private lateinit var attractionStar2: View
    private lateinit var attractionStar3: View
    private lateinit var attractionStar4: View
    private lateinit var attractionStar5: View
    private lateinit var tvPerValue: TextView
    private lateinit var tvPerIndustryAvg: TextView
    private lateinit var tvPerComment: TextView
    private lateinit var progressBarPer: android.widget.ProgressBar
    private lateinit var tvPbrValue: TextView
    private lateinit var tvPbrIndustryAvg: TextView
    private lateinit var tvPbrComment: TextView
    private lateinit var progressBarPbr: android.widget.ProgressBar
    private lateinit var tvRoeValue: TextView
    private lateinit var tvRoeIndustryAvg: TextView
    private lateinit var tvRoeComment: TextView
    private lateinit var progressBarRoe: android.widget.ProgressBar
    private lateinit var tvEpsValue: TextView
    private lateinit var tvEpsIndustryAvg: TextView
    private lateinit var tvEpsComment: TextView
    private lateinit var progressBarEps: android.widget.ProgressBar
    private lateinit var tvOperatingMarginValue: TextView
    private lateinit var tvOperatingMarginIndustryAvg: TextView
    private lateinit var tvOperatingMarginComment: TextView
    private lateinit var progressBarOperatingMargin: android.widget.ProgressBar
    private lateinit var tvRevenueGrowthValue: TextView
    private lateinit var tvRevenueGrowthIndustryAvg: TextView
    private lateinit var tvRevenueGrowthComment: TextView
    private lateinit var progressBarRevenueGrowth: android.widget.ProgressBar
    
    // 뉴스 관련 뷰들
    private lateinit var newsItem1: LinearLayout
    private lateinit var tvNewsDate1: TextView
    private lateinit var tvNewsTitle1: TextView
    private lateinit var ivNewsImage1: ImageView
    private lateinit var newsItem2: LinearLayout
    private lateinit var tvNewsDate2: TextView
    private lateinit var tvNewsTitle2: TextView
    private lateinit var ivNewsImage2: ImageView
    private lateinit var newsItem3: LinearLayout
    private lateinit var tvNewsDate3: TextView
    private lateinit var tvNewsTitle3: TextView
    private lateinit var ivNewsImage3: ImageView
    private lateinit var btnNewsMore: LinearLayout
    
    // 뉴스 API 서비스
    private lateinit var newsApiService: NewsApiService
    private var newsData: List<NewsItem> = emptyList()
    
    // 공시 관련 뷰들
    private lateinit var disclosureItem1: LinearLayout
    private lateinit var tvDisclosureTitle1: TextView
    private lateinit var tvDisclosureDate1: TextView
    private lateinit var vDisclosureDot1: View
    private lateinit var disclosureItem2: LinearLayout
    private lateinit var tvDisclosureTitle2: TextView
    private lateinit var tvDisclosureDate2: TextView
    private lateinit var vDisclosureDot2: View
    private lateinit var disclosureItem3: LinearLayout
    private lateinit var tvDisclosureTitle3: TextView
    private lateinit var tvDisclosureDate3: TextView
    private lateinit var vDisclosureDot3: View
    private lateinit var btnDisclosureMore: LinearLayout
    
    // 공시 API 서비스
    private lateinit var disclosureApiService: DisclosureApiService
    private var disclosureData: List<DisclosureItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stockName = arguments?.getString("stockName")
        
        // SSL 신뢰 설정과 함께 Retrofit 설정
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

        // 로깅 인터셉터 추가
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier(hostnameVerifier)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(TradingTrendApiService::class.java)
        financialApiService = retrofit.create(FinancialApiService::class.java)
        investmentIndicatorApiService = retrofit.create(InvestmentIndicatorApiService::class.java)
        comprehensiveAnalysisApiService = retrofit.create(ComprehensiveAnalysisApiService::class.java)
        newsApiService = retrofit.create(NewsApiService::class.java)
        disclosureApiService = retrofit.create(DisclosureApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.fragment_stock_info_simple, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")
        
        try {
            initViews(view)
            loadTradingData()
            
            // 차트 설정을 약간 지연시켜 레이아웃이 완전히 그려진 후 실행
            view.post {
                setupPerformanceChart()
                loadInvestmentIndicators()
                loadNewsData()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            setupSampleData()
        }
    }

    private fun initViews(view: View) {
        // 거래동향 관련 초기화
        tradingTrendRecyclerView = view.findViewById(R.id.rv_trading_trends)
        tradingTrendRecyclerView.layoutManager = LinearLayoutManager(context)
        
        // 스크롤 충돌 방지를 위한 설정
        tradingTrendRecyclerView.isNestedScrollingEnabled = false
        tradingTrendRecyclerView.setHasFixedSize(true)
        
        // 실적 차트 관련 초기화
        barChartPerformance = view.findViewById(R.id.bar_chart_performance)
        tvPerformanceAnnual = view.findViewById(R.id.tv_performance_annual)
        tvPerformanceQuarterly = view.findViewById(R.id.tv_performance_quarterly)
        tvRevenueSummary = view.findViewById(R.id.tv_revenue_summary)
        tvOperatingProfitSummary = view.findViewById(R.id.tv_operating_profit_summary)
        tvNetIncomeSummary = view.findViewById(R.id.tv_net_income_summary)
        tvRevenueYear = view.findViewById(R.id.tv_revenue_year)
        tvOperatingProfitYear = view.findViewById(R.id.tv_operating_profit_year)
        tvNetIncomeYear = view.findViewById(R.id.tv_net_income_year)
        
        // 탭 클릭 리스너 설정
        tvPerformanceAnnual.setOnClickListener {
            selectPerformanceTab(true)
        }
        
        tvPerformanceQuarterly.setOnClickListener {
            selectPerformanceTab(false)
        }
        
        // 투자 지표 관련 초기화 (ID는 실제 레이아웃에 맞게 수정 필요)
        initInvestmentIndicatorViews(view)
        
        // 뉴스 관련 클릭 이벤트 설정
        setupNewsClickListeners()
        
        // 공시 관련 클릭 이벤트 설정
        setupDisclosureClickListeners()
        
        // 공시 데이터 로드
        loadDisclosureData()
    }

    private fun loadTradingData() {
        val targetStockName = stockName?.takeIf { it.isNotBlank() && it != "알 수 없음" } ?: "파마리서치"
        
        lifecycleScope.launch {
            try {
                Log.d(TAG, "=== API 호출 시작 ===")
                Log.d(TAG, "Target stock name: $targetStockName")
                
                // URL 인코딩 없이 원본 종목명 사용 (서버에서 한글을 직접 처리)
                val processedStockName = targetStockName.trim()
                
                Log.d(TAG, "Original name: '$targetStockName'")
                Log.d(TAG, "Processed name: '$processedStockName'")
                Log.d(TAG, "Full API URL: ${BASE_URL}trading_data/$processedStockName")
                
                // API 호출 전 로그
                Log.d(TAG, "Calling API service...")
                val response = apiService.getTradingData(processedStockName)
                
                // 응답 상태 확인
                Log.d(TAG, "API Response received!")
                Log.d(TAG, "Response successful: ${response.isSuccessful}")
                Log.d(TAG, "Response code: ${response.code()}")
                Log.d(TAG, "Response message: ${response.message()}")
                Log.d(TAG, "Response body null: ${response.body() == null}")
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null) {
                        Log.d(TAG, "API Response received!")
                        Log.d(TAG, "  Success: ${apiResponse.success}")
                        Log.d(TAG, "  Message: ${apiResponse.message}")
                        Log.d(TAG, "  Stock name: ${apiResponse.stockName}")
                        Log.d(TAG, "  Total: ${apiResponse.total}")
                        Log.d(TAG, "  Data size: ${apiResponse.data.size}")
                        
                        if (apiResponse.success && apiResponse.data.isNotEmpty()) {
                            Log.d(TAG, "=== 실제 API 데이터 수신 성공! ===")
                            
                            // API 응답 데이터 상세 로깅 (처음 2개만)
                            apiResponse.data.take(2).forEachIndexed { index, data ->
                                Log.d(TAG, "API Record $index:")
                                Log.d(TAG, "  날짜: ${data.date}")
                                Log.d(TAG, "  종목명: ${data.stockName}")
                                Log.d(TAG, "  개인: ${data.individual}")
                                Log.d(TAG, "  외국인: ${data.foreign}")
                                Log.d(TAG, "  금융투자: ${data.securities}")
                                Log.d(TAG, "  기타법인: ${data.otherCorporation}")
                                Log.d(TAG, "  보험: ${data.insurance}")
                                Log.d(TAG, "  사모: ${data.privateEquity}")
                                Log.d(TAG, "  연기금: ${data.pension}")
                                Log.d(TAG, "  은행: ${data.bank}")
                                Log.d(TAG, "  투신: ${data.investment}")
                                Log.d(TAG, "  기타금융: ${data.otherFinance}")
                                Log.d(TAG, "  기타외국인: ${data.otherForeign}")
                            }
                            
                            // 최근 5일 데이터만 필터링
                            val recentData = filterRecentFiveDays(apiResponse.data)
                            Log.d(TAG, "Filtered to recent 5 days: ${recentData.size} records")
                            setupAdapter(recentData)
                            
                        } else {
                            Log.w(TAG, "API returned no data or failed: ${apiResponse.message}")
                            Log.w(TAG, "Using sample data instead")
                            setupSampleData()
                        }
                    } else {
                        Log.w(TAG, "API response body is null, using sample data")
                        setupSampleData()
                    }
                } else {
                    Log.e(TAG, "API call failed!")
                    Log.e(TAG, "Response code: ${response.code()}")
                    Log.e(TAG, "Response message: ${response.message()}")
                    
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error body: $errorBody")
                    
                    setupSampleData()
                }
            } catch (e: Exception) {
                Log.e(TAG, "=== API 호출 중 예외 발생 ===", e)
                Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Exception message: ${e.message}")
                e.printStackTrace()
                setupSampleData()
            }
        }
    }
    
    /**
     * 최근 5일 데이터만 필터링하는 함수
     */
    private fun filterRecentFiveDays(data: List<TradingTrendResponse>): List<TradingTrendResponse> {
        return try {
            // 날짜 기준으로 내림차순 정렬 (최신 날짜부터)
            val sortedData = data.sortedByDescending { 
                try {
                    // 날짜 형식 파싱 (예: "2025/01/15" 또는 "2025-01-15")
                    val dateStr = it.date.replace("-", "/")
                    java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault()).parse(dateStr)
                } catch (e: Exception) {
                    Log.w(TAG, "Date parsing failed for: ${it.date}")
                    java.util.Date(0) // 파싱 실패 시 가장 오래된 날짜로 설정
                }
            }
            
            // 최근 5일 데이터만 반환
            sortedData.take(5)
        } catch (e: Exception) {
            Log.e(TAG, "Error filtering recent data", e)
            // 에러 발생 시 원본 데이터의 처음 5개 반환
            data.take(5)
        }
    }

    private fun setupAdapter(data: List<TradingTrendResponse>) {
        try {
            tradingTrendAdapter = TradingTrendFullAdapter(data)
            tradingTrendRecyclerView.adapter = tradingTrendAdapter
            Log.d(TAG, "Adapter set with ${data.size} items")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up adapter", e)
            setupSampleData()
        }
    }

    private fun setupSampleData() {
        Log.d(TAG, "Setting up sample data")
        
        // 현재 날짜 기준으로 최근 5일 생성
        val calendar = java.util.Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
        val dates = mutableListOf<String>()
        
        // 오늘부터 4일 전까지 (총 5일)
        for (i in 0 until 5) {
            dates.add(dateFormat.format(calendar.time))
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        
        val sampleData = listOf(
            TradingTrendResponse(
                individual = 17705115000, // 177억원
                foreign = -17549105000, // -175억원
                securities = 0,
                otherCorporation = -306925000, // -3억원
                otherForeign = 150915000, // 1.5억원
                insurance = 0,
                privateEquity = 0,
                pension = 0,
                bank = 0,
                investment = 0,
                otherFinance = 0,
                date = dates[0], // 오늘
                stockName = stockName ?: "힘스",
                total = 0
            ),
            TradingTrendResponse(
                individual = -9778895000, // -97억원
                foreign = -3945565000, // -39억원
                securities = 0,
                otherCorporation = 13724460000, // 137억원
                otherForeign = 0,
                insurance = 0,
                privateEquity = 0,
                pension = 0,
                bank = 0,
                investment = 0,
                otherFinance = 0,
                date = dates[1], // 1일 전
                stockName = stockName ?: "힘스",
                total = 0
            ),
            TradingTrendResponse(
                individual = 8500000000, // 85억원
                foreign = 2900000000, // 29억원
                securities = 0,
                otherCorporation = -7800000000, // -78억원
                otherForeign = 5000000000, // 50억원
                insurance = -2000000000, // -20억원
                privateEquity = 10000000000, // 100억원
                pension = 3000000000, // 30억원
                bank = -1500000000, // -15억원
                investment = 0,
                otherFinance = 2500000000, // 25억원
                date = dates[2], // 2일 전
                stockName = stockName ?: "힘스",
                total = 0
            ),
            TradingTrendResponse(
                individual = -12500000000, // -125억원
                foreign = 4200000000, // 42억원
                securities = 1800000000, // 18억원
                otherCorporation = 6700000000, // 67억원
                otherForeign = -800000000, // -8억원
                insurance = 0,
                privateEquity = -3500000000, // -35억원
                pension = 0,
                bank = 900000000, // 9억원
                investment = 2100000000, // 21억원
                otherFinance = -500000000, // -5억원
                date = dates[3], // 3일 전
                stockName = stockName ?: "힘스",
                total = 0
            ),
            TradingTrendResponse(
                individual = 18900000000, // 189억원
                foreign = -15600000000, // -156억원
                securities = 3200000000, // 32억원
                otherCorporation = -4800000000, // -48억원
                otherForeign = 1200000000, // 12억원
                insurance = 700000000, // 7억원
                privateEquity = 0,
                pension = -2200000000, // -22억원
                bank = 0,
                investment = 1500000000, // 15억원
                otherFinance = 0,
                date = dates[4], // 4일 전
                stockName = stockName ?: "힘스",
                total = 0
            )
        )
        
        Log.d(TAG, "Sample data dates: ${dates.joinToString(", ")}")
        setupAdapter(sampleData)
    }
    
    private fun setupPerformanceChart() {
        Log.d(TAG, "Setting up performance chart")
        
        // 차트 초기 설정
        barChartPerformance.apply {
            description.isEnabled = false
            setTouchEnabled(true)   // 터치 이벤트 활성화 (클릭 감지용)
            isDragEnabled = false   // 드래그는 비활성화로 스크롤 충돌 방지
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setFitBars(true)
            
            // X축 설정
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(true)
                granularity = 1f
                textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
                textSize = 12f
                labelCount = 4
            }
            
            // Y축 설정 (격자와 단위 제거)
            axisLeft.apply {
                setDrawGridLines(false)  // 격자 제거
                setDrawAxisLine(false)
                setDrawLabels(false)     // Y축 라벨(단위) 제거
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = false
            
            // 차트 여백 설정 (하단 박스와 정확히 정렬)
            setExtraOffsets(16f, 10f, 16f, 10f)
            
            // 마커 뷰 설정 (말풍선)
            val markerView = CustomMarkerView(
                requireContext(),
                R.layout.custom_marker_view,
                { currentAnnualData },
                { currentQuarterlyData },
                { isAnnualSelected }
            )
            marker = markerView
            
            // 차트 클릭 리스너 설정 (하단 박스 데이터 업데이트)
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                    e?.let { entry ->
                        updateSummaryInfoBySelection(entry.x.toInt())
                    }
                }
                
                override fun onNothingSelected() {
                    // 선택 해제 시 원래 데이터로 복원
                    if (isAnnualSelected) {
                        updateSummaryInfo(currentAnnualData.lastOrNull())
                    } else {
                        updateQuarterlySummaryInfo(currentQuarterlyData)
                    }
                }
            })
        }
        
        // 초기 데이터 로드 (연간)
        loadPerformanceData(isAnnual = true)
    }
    
    private fun selectPerformanceTab(isAnnual: Boolean) {
        isAnnualSelected = isAnnual
        
        // 탭 UI 업데이트
        if (isAnnual) {
            tvPerformanceAnnual.apply {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                setBackgroundResource(R.drawable.bg_rounded_white)
                backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.black)
            }
            tvPerformanceQuarterly.apply {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                background = null
            }
        } else {
            tvPerformanceQuarterly.apply {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                setBackgroundResource(R.drawable.bg_rounded_white)
                backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.black)
            }
            tvPerformanceAnnual.apply {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                background = null
            }
        }
        
        // 데이터 로드
        loadPerformanceData(isAnnual)
    }
    
    private fun loadPerformanceData(isAnnual: Boolean) {
        val targetStockName = stockName?.takeIf { it.isNotBlank() && it != "알 수 없음" } ?: "파마리서치"
        
        lifecycleScope.launch {
            try {
                Log.d(TAG, "=== 실적 API 호출 시작 ===")
                Log.d(TAG, "Loading financial data for: $targetStockName, isAnnual: $isAnnual")
                Log.d(TAG, "API URL: ${BASE_URL}financial_data/$targetStockName")
                
                val processedStockName = targetStockName.trim()
                val response = financialApiService.getFinancialData(processedStockName)
                
                Log.d(TAG, "실적 API 응답 수신!")
                Log.d(TAG, "Response successful: ${response.isSuccessful}")
                Log.d(TAG, "Response code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val financialData = response.body()!!
                    Log.d(TAG, "=== 실제 실적 API 데이터 수신 성공! ===")
                    Log.d(TAG, "Company: ${financialData.companyName}")
                    Log.d(TAG, "Annual records: ${financialData.data.annual.size}")
                    Log.d(TAG, "Quarterly records: ${financialData.data.quarterly.size}")
                    
                    if (isAnnual) {
                        val annualData = convertToPerformanceData(financialData.data.annual, true)
                        currentAnnualData = annualData
                        Log.d(TAG, "변환된 연간 데이터: ${annualData.size}개")
                        setupAnnualBarChart(annualData)
                        updateSummaryInfo(annualData.lastOrNull())
                    } else {
                        Log.d(TAG, "=== 분기 모드 API 데이터 처리 시작 ===")
                        Log.d(TAG, "원본 분기 데이터 개수: ${financialData.data.quarterly.size}")
                        financialData.data.quarterly.forEachIndexed { index, record ->
                            Log.d(TAG, "분기 레코드 $index: 날짜=${record.date}, 매출=${record.revenue}, 영업이익=${record.operatingProfit}, 순이익=${record.netIncome}")
                        }
                        
                        val quarterlyData = convertToQuarterlyData(financialData.data.quarterly)
                        currentQuarterlyData = quarterlyData
                        Log.d(TAG, "=== 최종 변환된 분기 데이터: ${quarterlyData.size}개 ===")
                        quarterlyData.forEach { data ->
                            Log.d(TAG, "최종 분기: ${data.quarter}, 매출=${data.revenue}, 영업이익=${data.operatingProfit}, 순이익=${data.netIncome}")
                        }
                        
                        if (quarterlyData.isEmpty()) {
                            Log.w(TAG, "분기 데이터가 없음")
                            // 빈 차트 표시 또는 에러 메시지
                        } else {
                            Log.d(TAG, "API 분기 데이터만 사용: ${quarterlyData.size}개")
                            setupQuarterlyBarChart(quarterlyData)
                            updateQuarterlySummaryInfo(quarterlyData)
                        }
                    }
                } else {
                    Log.w(TAG, "실적 API 호출 실패, 샘플 데이터 사용")
                    Log.e(TAG, "Error response: ${response.errorBody()?.string()}")
                    loadSamplePerformanceData(isAnnual)
                }
            } catch (e: Exception) {
                Log.e(TAG, "실적 데이터 로딩 오류", e)
                loadSamplePerformanceData(isAnnual)
            }
        }
    }
    
    /**
     * API 응답을 PerformanceData로 변환
     */
    private fun convertToPerformanceData(records: List<FinancialRecord>, isAnnual: Boolean): List<PerformanceData> {
        return records.mapNotNull { record ->
            try {
                Log.d(TAG, "=== 데이터 변환 시작 ===")
                Log.d(TAG, "원본 데이터 - 날짜: ${record.date}")
                Log.d(TAG, "원본 데이터 - 매출액: ${record.revenue}")
                Log.d(TAG, "원본 데이터 - 영업이익: ${record.operatingProfit}")
                Log.d(TAG, "원본 데이터 - 당기순이익: ${record.netIncome}")
                
                // API에서 오는 데이터가 억 단위로 가정하고 변환
                // null 값을 0으로 처리하고, Double을 Long으로 변환 (억 단위 * 100,000,000)
                val revenue = ((record.revenue ?: 0.0) * 100_000_000).toLong()  // 억 → 원
                val operatingProfit = ((record.operatingProfit ?: 0.0) * 100_000_000).toLong()
                val netIncome = ((record.netIncome ?: 0.0) * 100_000_000).toLong()
                
                Log.d(TAG, "변환된 데이터 - 매출액: ${revenue}원")
                Log.d(TAG, "변환된 데이터 - 영업이익: ${operatingProfit}원")
                Log.d(TAG, "변환된 데이터 - 당기순이익: ${netIncome}원")
                
                // 날짜에서 연도 추출 (2024-12-01 형식)
                val year = record.date.substring(0, 4)
                Log.d(TAG, "추출된 연도: $year")
                
                // 2025년 데이터 제외 (연간 데이터가 미완성)
                if (isAnnual && year == "2025") {
                    Log.d(TAG, "2025년 연간 데이터 제외: ${record.date}")
                    return@mapNotNull null
                }
                
                // 원본 API 데이터가 모두 null이면 제외 (변환 후가 아닌 원본 기준)
                if (record.revenue == null && record.operatingProfit == null && record.netIncome == null) {
                    Log.d(TAG, "원본 데이터가 모두 null이어서 제외: ${record.date}")
                    return@mapNotNull null
                }
                
                val performanceData = PerformanceData(
                    year = year,
                    revenue = revenue,
                    operatingProfit = operatingProfit,
                    netIncome = netIncome,
                    isEstimate = false
                )
                
                Log.d(TAG, "최종 변환 완료: $performanceData")
                performanceData
                
            } catch (e: Exception) {
                Log.e(TAG, "데이터 변환 오류: ${record.date}", e)
                null
            }
        }.sortedBy { it.year } // 연도순 정렬
    }
    
    /**
     * API 응답을 QuarterlyPerformanceData로 변환
     */
    private fun convertToQuarterlyData(records: List<FinancialRecord>): List<QuarterlyPerformanceData> {
        Log.d(TAG, "=== 분기 데이터 변환 시작 (총 ${records.size}개 레코드) ===")
        
        val quarterlyList = records.mapNotNull { record ->
            try {
                Log.d(TAG, "원본 분기 데이터 - 날짜: ${record.date}")
                Log.d(TAG, "원본 분기 데이터 - 매출액: ${record.revenue}")
                Log.d(TAG, "원본 분기 데이터 - 영업이익: ${record.operatingProfit}")
                Log.d(TAG, "원본 분기 데이터 - 당기순이익: ${record.netIncome}")
                
                // 매출, 영업이익, 순이익이 모두 null이면 제외
                if (record.revenue == null && record.operatingProfit == null && record.netIncome == null) {
                    Log.d(TAG, "3개 데이터가 모두 null이어서 제외: ${record.date}")
                    return@mapNotNull null
                }
                
                // API에서 오는 데이터가 억 단위로 가정하고 변환
                val revenue = ((record.revenue ?: 0.0) * 100_000_000).toLong()  // 억 → 원
                val operatingProfit = ((record.operatingProfit ?: 0.0) * 100_000_000).toLong()
                val netIncome = ((record.netIncome ?: 0.0) * 100_000_000).toLong()
                
                Log.d(TAG, "변환된 분기 데이터 - 매출액: ${revenue}원")
                Log.d(TAG, "변환된 분기 데이터 - 영업이익: ${operatingProfit}원")
                Log.d(TAG, "변환된 분기 데이터 - 당기순이익: ${netIncome}원")
                
                // 날짜에서 분기 형식 생성 (2025-03-01 -> "25.3", 2024-12-01 -> "24.12")
                val dateParts = record.date.split("-")
                if (dateParts.size >= 3) {
                    val year = dateParts[0].substring(2) // "2025" -> "25"
                    val month = dateParts[1].toInt() // "03" -> 3, "12" -> 12
                    val quarter = "$year.$month"
                    
                    Log.d(TAG, "날짜 변환: ${record.date} -> $quarter (년도: $year, 월: $month)")
                    
                    val quarterlyData = QuarterlyPerformanceData(
                        quarter = quarter,
                        revenue = revenue,
                        operatingProfit = operatingProfit,
                        netIncome = netIncome,
                        isEstimate = year == "25" // 2025년은 예상치로 표시
                    )
                    
                    Log.d(TAG, "✅ 분기 데이터 생성 성공: $quarterlyData")
                    quarterlyData
                    
                } else {
                    Log.e(TAG, "❌ 잘못된 날짜 형식: ${record.date}")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "분기 데이터 변환 오류: ${record.date}", e)
                null
            }
        }
        
        Log.d(TAG, "=== 분기 정렬 시작 ===")
        Log.d(TAG, "정렬 전 분기 리스트: ${quarterlyList.map { it.quarter }}")
        
        // 날짜 기준으로 정렬 (모든 데이터 포함)
        val sortedData = quarterlyList.sortedByDescending { quarterlyData ->
            try {
                val parts = quarterlyData.quarter.split(".")
                val year = 2000 + parts[0].toInt() // "25" -> 2025
                val month = parts[1].toInt()
                val sortKey = year * 100 + month // 202503 같은 형태로 정렬 기준 생성
                Log.d(TAG, "분기 ${quarterlyData.quarter} -> 정렬키: $sortKey")
                sortKey
            } catch (e: Exception) {
                Log.e(TAG, "분기 정렬 오류: ${quarterlyData.quarter}", e)
                0
            }
        }.reversed() // 시간순으로 정렬
        
        Log.d(TAG, "=== 정렬 완료 ===")
        Log.d(TAG, "최종 분기 데이터: ${sortedData.size}개")
        sortedData.forEachIndexed { index, data ->
            Log.d(TAG, "분기 $index: ${data.quarter} (매출: ${data.revenue/100_000_000}억)")
        }
        return sortedData
    }
    


    private fun loadSamplePerformanceData(isAnnual: Boolean) {
        if (isAnnual) {
            val annualData = PerformanceSampleData.getAnnualSampleData()
            currentAnnualData = annualData
            setupAnnualBarChart(annualData)
            updateSummaryInfo(annualData.lastOrNull())
        } else {
            val quarterlyData = PerformanceSampleData.getQuarterlySampleData()
            currentQuarterlyData = quarterlyData
            Log.d(TAG, "=== 순수 샘플 데이터 사용 ===")
            Log.d(TAG, "샘플 분기 데이터: ${quarterlyData.size}개 (${quarterlyData.map { it.quarter }})")
            setupQuarterlyBarChart(quarterlyData)
            updateQuarterlySummaryInfo(quarterlyData)
        }
    }
    
    private fun setupAnnualBarChart(data: List<PerformanceData>) {
        Log.d(TAG, "Setting up annual bar chart with ${data.size} items")
        
        val revenueEntries = mutableListOf<BarEntry>()
        val operatingProfitEntries = mutableListOf<BarEntry>()
        val netIncomeEntries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        
        // 최소값과 최대값 추적 (Y축 범위 계산용)
        var minValue = 0f
        var maxValue = 0f
        
        data.forEachIndexed { index, item ->
            val revenue = PerformanceUtils.toTrillionUnit(item.revenue)
            val operatingProfit = PerformanceUtils.toTrillionUnit(item.operatingProfit)
            val netIncome = PerformanceUtils.toTrillionUnit(item.netIncome)
            
            Log.d(TAG, "Year ${item.year}: Revenue=$revenue, OpProfit=$operatingProfit, NetIncome=$netIncome")
            
            revenueEntries.add(BarEntry(index.toFloat(), revenue))
            operatingProfitEntries.add(BarEntry(index.toFloat(), operatingProfit))
            netIncomeEntries.add(BarEntry(index.toFloat(), netIncome))
            labels.add(item.year)
            
            // 최소값/최대값 업데이트
            minValue = minOf(minValue, revenue, operatingProfit, netIncome)
            maxValue = maxOf(maxValue, revenue, operatingProfit, netIncome)
        }
        
        // 데이터셋 생성 (연간 차트 - 기존 색상 유지)
        val revenueDataSet = BarDataSet(revenueEntries, "매출").apply {
            color = ContextCompat.getColor(requireContext(), R.color.light_green)
            setDrawValues(false)
        }
        
        val operatingProfitDataSet = BarDataSet(operatingProfitEntries, "영업이익").apply {
            color = ContextCompat.getColor(requireContext(), R.color.medium_green)
            setDrawValues(false)
        }
        
        val netIncomeDataSet = BarDataSet(netIncomeEntries, "순이익").apply {
            color = ContextCompat.getColor(requireContext(), R.color.dark_green)
            setDrawValues(false)
        }
        
        // 막대 차트 데이터 설정 (막대 크기 증가)
        val barData = BarData(revenueDataSet, operatingProfitDataSet, netIncomeDataSet)
        val groupSpace = 0.15f  // 그룹 간격 줄이기
        val barSpace = 0.02f
        val barWidth = 0.26f    // 막대 너비 증가
        
        barData.barWidth = barWidth
        
        barChartPerformance.apply {
            this.data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            
            // Y축 범위 동적 설정 (음수 값 고려)
            axisLeft.apply {
                if (minValue < 0f) {
                    // 음수 값이 있으면 여백을 주어 Y축 범위 설정
                    val range = maxValue - minValue
                    axisMinimum = minValue - (range * 0.1f)  // 하단 10% 여백
                    axisMaximum = maxValue + (range * 0.1f)  // 상단 10% 여백
                } else {
                    // 모든 값이 양수면 0부터 시작
                    axisMinimum = 0f
                    axisMaximum = maxValue * 1.1f  // 상단 10% 여백
                }
                Log.d(TAG, "연간 차트 Y축 범위 설정: min=$axisMinimum, max=$axisMaximum (데이터: $minValue ~ $maxValue)")
            }
            
            // 그룹 막대 설정 (여백 조정 및 X축 선 제한)
            if (data.isNotEmpty()) {
                groupBars(0f, groupSpace, barSpace)
                xAxis.apply {
                    axisMinimum = -0.2f  // 왼쪽 여백 최소화
                    axisMaximum = data.size.toFloat()  // 오른쪽 여백 충분히 확보 (유지)
                    setCenterAxisLabels(true)
                    // X축 선이 바를 넘어가지 않도록 설정
                    setDrawAxisLine(true)
                    setDrawGridLines(false)
                    axisLineWidth = 1f
                    axisLineColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
                }
            }
            
            // 차트 새로고침
            notifyDataSetChanged()
            invalidate()
        }
        
        Log.d(TAG, "Annual bar chart setup completed")
    }
    
    private fun setupQuarterlyBarChart(data: List<QuarterlyPerformanceData>) {
        Log.d(TAG, "=== 분기 차트 설정 시작 ===")
        Log.d(TAG, "차트 설정할 분기 데이터: ${data.size}개")
        data.forEachIndexed { index, item ->
            Log.d(TAG, "차트 분기 $index: ${item.quarter} - 매출:${item.revenue/100_000_000}억, 영업이익:${item.operatingProfit/100_000_000}억, 순이익:${item.netIncome/100_000_000}억")
        }
        
        val revenueEntries = mutableListOf<BarEntry>()
        val operatingProfitEntries = mutableListOf<BarEntry>()
        val netIncomeEntries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        
        // 최소값과 최대값 추적 (Y축 범위 계산용)
        var minValue = 0f
        var maxValue = 0f
        
        data.forEachIndexed { index, item ->
            val revenue = PerformanceUtils.toTrillionUnit(item.revenue)
            val operatingProfit = PerformanceUtils.toTrillionUnit(item.operatingProfit)
            val netIncome = PerformanceUtils.toTrillionUnit(item.netIncome)
            
            Log.d(TAG, "막대그래프 Entry 생성 $index: ${item.quarter} -> Revenue=${revenue}조, OpProfit=${operatingProfit}조, NetIncome=${netIncome}조")
            
            revenueEntries.add(BarEntry(index.toFloat(), revenue))
            operatingProfitEntries.add(BarEntry(index.toFloat(), operatingProfit))
            netIncomeEntries.add(BarEntry(index.toFloat(), netIncome))
            labels.add(item.quarter)
            
            // 최소값/최대값 업데이트
            minValue = minOf(minValue, revenue, operatingProfit, netIncome)
            maxValue = maxOf(maxValue, revenue, operatingProfit, netIncome)
        }
        
        Log.d(TAG, "생성된 Entry 개수 - Revenue: ${revenueEntries.size}, OpProfit: ${operatingProfitEntries.size}, NetIncome: ${netIncomeEntries.size}")
        Log.d(TAG, "X축 라벨: $labels")
        
        // 데이터셋 생성 (분기 차트 - 기존 색상 유지)
        val revenueDataSet = BarDataSet(revenueEntries, "매출").apply {
            color = ContextCompat.getColor(requireContext(), R.color.light_green)
            setDrawValues(false)
        }
        
        val operatingProfitDataSet = BarDataSet(operatingProfitEntries, "영업이익").apply {
            color = ContextCompat.getColor(requireContext(), R.color.medium_green)
            setDrawValues(false)
        }
        
        val netIncomeDataSet = BarDataSet(netIncomeEntries, "순이익").apply {
            color = ContextCompat.getColor(requireContext(), R.color.dark_green)
            setDrawValues(false)
        }
        
        // 막대 차트 데이터 설정 (막대 크기 증가)
        val barData = BarData(revenueDataSet, operatingProfitDataSet, netIncomeDataSet)
        val groupSpace = 0.15f  // 그룹 간격 줄이기
        val barSpace = 0.02f
        val barWidth = 0.26f    // 막대 너비 증가
        
        barData.barWidth = barWidth
        
        barChartPerformance.apply {
            this.data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            
            // Y축 범위 동적 설정 (음수 값 고려)
            axisLeft.apply {
                if (minValue < 0f) {
                    // 음수 값이 있으면 여백을 주어 Y축 범위 설정
                    val range = maxValue - minValue
                    axisMinimum = minValue - (range * 0.1f)  // 하단 10% 여백
                    axisMaximum = maxValue + (range * 0.1f)  // 상단 10% 여백
                } else {
                    // 모든 값이 양수면 0부터 시작
                    axisMinimum = 0f
                    axisMaximum = maxValue * 1.1f  // 상단 10% 여백
                }
                Log.d(TAG, "분기 차트 Y축 범위 설정: min=$axisMinimum, max=$axisMaximum (데이터: $minValue ~ $maxValue)")
            }
            
            // 그룹 막대 설정 (여백 조정 및 X축 선 제한)
            if (data.isNotEmpty()) {
                groupBars(0f, groupSpace, barSpace)
                xAxis.apply {
                    axisMinimum = -0.2f  // 왼쪽 여백 최소화
                    axisMaximum = data.size.toFloat()  // 오른쪽 여백 충분히 확보 (유지)
                    setCenterAxisLabels(true)
                    // X축 선이 바를 넘어가지 않도록 설정
                    setDrawAxisLine(true)
                    setDrawGridLines(false)
                    axisLineWidth = 1f
                    axisLineColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
                }
            }
            
            // 차트 새로고침
            notifyDataSetChanged()
            invalidate()
        }
        
        Log.d(TAG, "Quarterly bar chart setup completed")
    }
    
    private fun updateSummaryInfo(latestData: PerformanceData?) {
        latestData?.let { data ->
            // 메인 텍스트와 연도를 분리하여 표시
            tvRevenueSummary.text = PerformanceUtils.formatSummaryWithYear(data.revenue, data.year)
            tvOperatingProfitSummary.text = PerformanceUtils.formatSummaryWithYear(data.operatingProfit, data.year)
            tvNetIncomeSummary.text = PerformanceUtils.formatSummaryWithYear(data.netIncome, data.year)
            
            // 연도 정보를 별도 텍스트뷰에 표시
            val yearText = PerformanceUtils.formatYearSubtitle(data.year)
            tvRevenueYear.text = yearText
            tvOperatingProfitYear.text = yearText
            tvNetIncomeYear.text = yearText
        }
    }
    
    private fun updateQuarterlySummaryInfo(data: List<QuarterlyPerformanceData>) {
        // 최근 4분기 합계 계산
        val recentFourQuarters = data.takeLast(4)
        val totalRevenue = recentFourQuarters.sumOf { it.revenue }
        val totalOperatingProfit = recentFourQuarters.sumOf { it.operatingProfit }
        val totalNetIncome = recentFourQuarters.sumOf { it.netIncome }
        
        tvRevenueSummary.text = PerformanceUtils.formatToTrillionWon(totalRevenue)
        tvOperatingProfitSummary.text = PerformanceUtils.formatToTrillionWon(totalOperatingProfit)
        tvNetIncomeSummary.text = PerformanceUtils.formatToTrillionWon(totalNetIncome)
        
        // 분기별 표시에서는 연도를 "(최근 4분기)"로 표시
        val quarterlyYearText = "(최근 4분기)"
        tvRevenueYear.text = quarterlyYearText
        tvOperatingProfitYear.text = quarterlyYearText
        tvNetIncomeYear.text = quarterlyYearText
    }
    
    // 클릭된 막대그래프에 해당하는 데이터로 하단 박스 업데이트
    private fun updateSummaryInfoBySelection(selectedIndex: Int) {
        if (isAnnualSelected) {
            // 연간 데이터 선택
            if (selectedIndex < currentAnnualData.size) {
                val selectedData = currentAnnualData[selectedIndex]
                tvRevenueSummary.text = PerformanceUtils.formatSummaryWithYear(selectedData.revenue, selectedData.year)
                tvOperatingProfitSummary.text = PerformanceUtils.formatSummaryWithYear(selectedData.operatingProfit, selectedData.year)
                tvNetIncomeSummary.text = PerformanceUtils.formatSummaryWithYear(selectedData.netIncome, selectedData.year)
                
                // 선택된 연도 표시
                val yearText = PerformanceUtils.formatYearSubtitle(selectedData.year)
                tvRevenueYear.text = yearText
                tvOperatingProfitYear.text = yearText
                tvNetIncomeYear.text = yearText
                
                Log.d(TAG, "Selected annual data: ${selectedData.year}")
            }
        } else {
            // 분기 데이터 선택
            if (selectedIndex < currentQuarterlyData.size) {
                val selectedData = currentQuarterlyData[selectedIndex]
                tvRevenueSummary.text = PerformanceUtils.formatToTrillionWon(selectedData.revenue)
                tvOperatingProfitSummary.text = PerformanceUtils.formatToTrillionWon(selectedData.operatingProfit)
                tvNetIncomeSummary.text = PerformanceUtils.formatToTrillionWon(selectedData.netIncome)
                
                // 선택된 분기 표시
                val quarterText = "(${selectedData.quarter})"
                tvRevenueYear.text = quarterText
                tvOperatingProfitYear.text = quarterText
                tvNetIncomeYear.text = quarterText
                
                Log.d(TAG, "Selected quarterly data: ${selectedData.quarter}")
            }
        }
    }
    
    /**
     * 투자 지표 뷰 초기화
     */
    private fun initInvestmentIndicatorViews(view: View) {
        try {
            // 종합 분석 및 투자 매력도 (5단계)
            tvOverallComment = view.findViewById(R.id.tv_overall_comment)
            attractionStar1 = view.findViewById(R.id.attraction_star_1)
            attractionStar2 = view.findViewById(R.id.attraction_star_2)
            attractionStar3 = view.findViewById(R.id.attraction_star_3)
            attractionStar4 = view.findViewById(R.id.attraction_star_4)
            attractionStar5 = view.findViewById(R.id.attraction_star_5)
            
            // PER 관련 뷰들
            tvPerValue = view.findViewById(R.id.tv_per_value)
            tvPerIndustryAvg = view.findViewById(R.id.tv_per_industry_avg)
            tvPerComment = view.findViewById(R.id.tv_per_comment)
            progressBarPer = view.findViewById(R.id.progress_bar_per)
            
            // PBR 관련 뷰들
            tvPbrValue = view.findViewById(R.id.tv_pbr_value)
            tvPbrIndustryAvg = view.findViewById(R.id.tv_pbr_industry_avg)
            tvPbrComment = view.findViewById(R.id.tv_pbr_comment)
            progressBarPbr = view.findViewById(R.id.progress_bar_pbr)
            
            // ROE 관련 뷰들
            tvRoeValue = view.findViewById(R.id.tv_roe_value)
            tvRoeIndustryAvg = view.findViewById(R.id.tv_roe_industry_avg)
            tvRoeComment = view.findViewById(R.id.tv_roe_comment)
            progressBarRoe = view.findViewById(R.id.progress_bar_roe)
            
            // EPS 관련 뷰들
            tvEpsValue = view.findViewById(R.id.tv_eps_value)
            tvEpsIndustryAvg = view.findViewById(R.id.tv_eps_industry_avg)
            tvEpsComment = view.findViewById(R.id.tv_eps_comment)
            progressBarEps = view.findViewById(R.id.progress_bar_eps)
            
            // 영업이익률 관련 뷰들
            tvOperatingMarginValue = view.findViewById(R.id.tv_operating_margin_value)
            tvOperatingMarginIndustryAvg = view.findViewById(R.id.tv_operating_margin_industry_avg)
            tvOperatingMarginComment = view.findViewById(R.id.tv_operating_margin_comment)
            progressBarOperatingMargin = view.findViewById(R.id.progress_bar_operating_margin)
            
            // 매출성장률 관련 뷰들
            tvRevenueGrowthValue = view.findViewById(R.id.tv_revenue_growth_value)
            tvRevenueGrowthIndustryAvg = view.findViewById(R.id.tv_revenue_growth_industry_avg)
            tvRevenueGrowthComment = view.findViewById(R.id.tv_revenue_growth_comment)
            progressBarRevenueGrowth = view.findViewById(R.id.progress_bar_revenue_growth)
            
            // 뉴스 관련 뷰들
            newsItem1 = view.findViewById(R.id.news_item_1)
            tvNewsDate1 = view.findViewById(R.id.tv_news_date_1)
            tvNewsTitle1 = view.findViewById(R.id.tv_news_title_1)
            ivNewsImage1 = view.findViewById(R.id.iv_news_image_1)
            newsItem2 = view.findViewById(R.id.news_item_2)
            tvNewsDate2 = view.findViewById(R.id.tv_news_date_2)
            tvNewsTitle2 = view.findViewById(R.id.tv_news_title_2)
            ivNewsImage2 = view.findViewById(R.id.iv_news_image_2)
            newsItem3 = view.findViewById(R.id.news_item_3)
            tvNewsDate3 = view.findViewById(R.id.tv_news_date_3)
            tvNewsTitle3 = view.findViewById(R.id.tv_news_title_3)
            ivNewsImage3 = view.findViewById(R.id.iv_news_image_3)
            btnNewsMore = view.findViewById(R.id.btn_news_more)
            
            // 공시 관련 뷰들
            disclosureItem1 = view.findViewById(R.id.disclosure_item_1)
            tvDisclosureTitle1 = view.findViewById(R.id.tv_disclosure_title_1)
            tvDisclosureDate1 = view.findViewById(R.id.tv_disclosure_date_1)
            vDisclosureDot1 = view.findViewById(R.id.v_disclosure_dot_1)
            disclosureItem2 = view.findViewById(R.id.disclosure_item_2)
            tvDisclosureTitle2 = view.findViewById(R.id.tv_disclosure_title_2)
            tvDisclosureDate2 = view.findViewById(R.id.tv_disclosure_date_2)
            vDisclosureDot2 = view.findViewById(R.id.v_disclosure_dot_2)
            disclosureItem3 = view.findViewById(R.id.disclosure_item_3)
            tvDisclosureTitle3 = view.findViewById(R.id.tv_disclosure_title_3)
            tvDisclosureDate3 = view.findViewById(R.id.tv_disclosure_date_3)
            vDisclosureDot3 = view.findViewById(R.id.v_disclosure_dot_3)
            btnDisclosureMore = view.findViewById(R.id.btn_disclosure_more)
            
            Log.d(TAG, "Investment indicator, news and disclosure views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing investment indicator views", e)
        }
    }
    
    /**
     * 투자 지표 데이터 로드 (새로운 종합 분석 API 사용)
     */
    private fun loadInvestmentIndicators() {
        val targetStockName = stockName?.takeIf { it.isNotBlank() && it != "알 수 없음" } ?: "DSR제강"
        
        lifecycleScope.launch {
            try {
                Log.d(TAG, "=== 종합 분석 API 호출 시작 ===")
                Log.d(TAG, "Loading comprehensive analysis for: $targetStockName")
                Log.d(TAG, "API URL: ${BASE_URL}stock_comprehensive_analysis/$targetStockName")
                
                val response = comprehensiveAnalysisApiService.getComprehensiveAnalysis(targetStockName)
                
                Log.d(TAG, "종합 분석 API 응답 수신!")
                Log.d(TAG, "Response successful: ${response.isSuccessful}")
                Log.d(TAG, "Response code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val analysisResponse = response.body()!!
                    
                    if (analysisResponse.success) {
                        Log.d(TAG, "=== 종합 분석 API 데이터 수신 성공! ===")
                        Log.d(TAG, "종목명: ${analysisResponse.stockName}")
                        Log.d(TAG, "업종: ${analysisResponse.industry}")
                        Log.d(TAG, "전체 점수: ${analysisResponse.comprehensiveAnalysis?.totalScore}")
                        Log.d(TAG, "투자 매력도: ${analysisResponse.comprehensiveAnalysis?.attractionScore}")
                        Log.d(TAG, "전체 평가: ${analysisResponse.comprehensiveAnalysis?.overallRating}")
                        
                        // API 응답을 기존 분석 결과 형태로 변환
                        val analysisResults = ComprehensiveAnalysisConverter.convertToAnalysisResults(analysisResponse)
                        
                        if (analysisResults != null) {
                            displayAnalysisResults(analysisResults)
                        } else {
                            Log.w(TAG, "분석 결과 변환 실패, 샘플 데이터 사용")
                            displaySampleIndicators()
                        }
                    } else {
                        Log.w(TAG, "종합 분석 API 응답 실패, 샘플 데이터 사용")
                        displaySampleIndicators()
                    }
                } else {
                    Log.w(TAG, "종합 분석 API 호출 실패, 기존 방식으로 fallback")
                    Log.e(TAG, "Error response: ${response.errorBody()?.string()}")
                    // 기존 방식으로 fallback
                    loadInvestmentIndicatorsLegacy()
                }
            } catch (e: Exception) {
                Log.e(TAG, "종합 분석 API 호출 오류, 기존 방식으로 fallback", e)
                // 기존 방식으로 fallback
                loadInvestmentIndicatorsLegacy()
            }
        }
    }
    
    /**
     * 기존 투자 지표 API 호출 방식 (fallback용)
     */
    private fun loadInvestmentIndicatorsLegacy() {
        val targetStockName = stockName?.takeIf { it.isNotBlank() && it != "알 수 없음" } ?: "DSR제강"
        
        lifecycleScope.launch {
            try {
                Log.d(TAG, "=== 기존 투자 지표 API 호출 (fallback) ===")
                Log.d(TAG, "Loading investment indicators for: $targetStockName")
                Log.d(TAG, "API URL: ${BASE_URL}realprice?company_names=$targetStockName")
                
                val response = investmentIndicatorApiService.getInvestmentIndicators(targetStockName)
                
                Log.d(TAG, "투자 지표 API 응답 수신!")
                Log.d(TAG, "Response successful: ${response.isSuccessful}")
                Log.d(TAG, "Response code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val indicatorList = response.body()!!
                    if (indicatorList.isNotEmpty()) {
                        val indicatorData = indicatorList.first()
                        Log.d(TAG, "=== 실제 투자 지표 API 데이터 수신 성공! ===")
                        Log.d(TAG, "종목명: ${indicatorData.stockName}")
                        Log.d(TAG, "PER: ${indicatorData.per}")
                        Log.d(TAG, "PBR: ${indicatorData.pbr}")
                        Log.d(TAG, "ROE: ${indicatorData.roe}")
                        Log.d(TAG, "EPS: ${indicatorData.eps}")
                        Log.d(TAG, "업종명: ${indicatorData.industryName}")
                        Log.d(TAG, "업종평균 PER: ${indicatorData.industryAvgPer}")
                        Log.d(TAG, "업종평균 PBR: ${indicatorData.industryAvgPbr}")
                        Log.d(TAG, "업종평균 ROE: ${indicatorData.industryAvgRoe}")
                        
                        analyzeAndDisplayIndicators(indicatorData)
                    } else {
                        Log.w(TAG, "투자 지표 API 응답이 비어있음")
                        displaySampleIndicators()
                    }
                } else {
                    Log.w(TAG, "투자 지표 API 호출 실패, 샘플 데이터 사용")
                    Log.e(TAG, "Error response: ${response.errorBody()?.string()}")
                    displaySampleIndicators()
                }
            } catch (e: Exception) {
                Log.e(TAG, "투자 지표 데이터 로딩 오류", e)
                displaySampleIndicators()
            }
        }
    }
    
    /**
     * 새로운 API 응답으로부터 분석 결과 표시
     */
    private fun displayAnalysisResults(analysisResults: AnalysisResults) {
        try {
            Log.d(TAG, "=== 새로운 API 응답 UI 업데이트 시작 ===")
            
            // 기존 UI 업데이트 함수 재사용
            updateInvestmentIndicatorUI(
                analysisResults.perAnalysis,
                analysisResults.pbrAnalysis,
                analysisResults.roeAnalysis,
                analysisResults.epsAnalysis,
                analysisResults.revenueGrowthAnalysis,
                analysisResults.operatingMarginAnalysis,
                analysisResults.overallAnalysis
            )
            
            Log.d(TAG, "=== 새로운 API 응답 UI 업데이트 완료 ===")
            
        } catch (e: Exception) {
            Log.e(TAG, "새로운 API 응답 UI 업데이트 오류", e)
        }
    }
    
    /**
     * 투자 지표 분석 및 화면 표시 (기존 방식, fallback용)
     */
    private fun analyzeAndDisplayIndicators(data: InvestmentIndicatorResponse) {
        try {
            Log.d(TAG, "=== 투자 지표 분석 시작 ===")
            
            // 각 지표 분석
            val perAnalysis = InvestmentIndicatorAnalyzer.analyzePER(data.per, data.industryAvgPer)
            val pbrAnalysis = InvestmentIndicatorAnalyzer.analyzePBR(data.pbr, data.industryAvgPbr)
            val roeAnalysis = InvestmentIndicatorAnalyzer.analyzeROE(data.roe, data.industryAvgRoe)
            val epsAnalysis = InvestmentIndicatorAnalyzer.analyzeEPS(data.eps, data.industryAvgEps)
            val revenueGrowthAnalysis = InvestmentIndicatorAnalyzer.analyzeRevenueGrowthRate(data.revenueGrowthRate, data.industryAvgRevenueGrowth)
            val operatingMarginAnalysis = InvestmentIndicatorAnalyzer.analyzeOperatingMargin(data.operatingIncome, data.revenue, data.industryAvgOperatingMargin)
            
            // 종합 분석
            val overallAnalysis = InvestmentIndicatorAnalyzer.generateOverallAnalysis(
                perAnalysis, pbrAnalysis, roeAnalysis, epsAnalysis, revenueGrowthAnalysis, operatingMarginAnalysis, data.industryName
            )
            
            Log.d(TAG, "=== 분석 결과 ===")
            Log.d(TAG, "PER 분석: ${perAnalysis.value} - ${perAnalysis.comment}")
            Log.d(TAG, "PBR 분석: ${pbrAnalysis.value} - ${pbrAnalysis.comment}")
            Log.d(TAG, "ROE 분석: ${roeAnalysis.value} - ${roeAnalysis.comment}")
            Log.d(TAG, "EPS 분석: ${epsAnalysis.value} - ${epsAnalysis.comment}")
            Log.d(TAG, "매출성장률 분석: ${revenueGrowthAnalysis.value} - ${revenueGrowthAnalysis.comment}")
            Log.d(TAG, "영업이익률 분석: ${operatingMarginAnalysis.value} - ${operatingMarginAnalysis.comment}")
            Log.d(TAG, "종합 분석: ${overallAnalysis.comment}")
            Log.d(TAG, "투자 매력도: ${overallAnalysis.attractionScore}/5")
            
            // UI 업데이트
            updateInvestmentIndicatorUI(perAnalysis, pbrAnalysis, roeAnalysis, epsAnalysis, revenueGrowthAnalysis, operatingMarginAnalysis, overallAnalysis)
            
        } catch (e: Exception) {
            Log.e(TAG, "투자 지표 분석 오류", e)
        }
    }
    
    /**
     * 샘플 투자 지표 표시 (API 실패 시)
     */
    private fun displaySampleIndicators() {
        Log.d(TAG, "=== 샘플 투자 지표 데이터 사용 ===")
        
        // 샘플 데이터로 분석 예시
        val sampleData = InvestmentIndicatorResponse(
            pbr = "0.32", per = "-1.93", roa = "-11.61", roe = "-15.70", eps = "-1,953",
            industryAvgPbr = 1.27, industryAvgPer = 23.67, industryAvgRoe = -4.62, industryAvgEps = 1436.0,
            industryName = "1차 철강 제조업", stockName = "DSR제강",
            revenue = null, operatingIncome = null, netIncome = null,
            tradingValue = null, tradingVolume = null, high = null, validCompanyCount = null,
            changeRate = null, revenueGrowthRate = null, dividend = null, marketCap = null,
            industryCompanyCount = null, industryAvgRevenueGrowth = null, industryAvgOperatingMargin = null,
            operatingIncomeGrowthRate = null, currentPrice = null
        )
        
        analyzeAndDisplayIndicators(sampleData)
    }
    
    /**
     * 투자 지표 UI 업데이트
     */
    private fun updateInvestmentIndicatorUI(
        perAnalysis: IndicatorAnalysis,
        pbrAnalysis: IndicatorAnalysis,
        roeAnalysis: IndicatorAnalysis,
        epsAnalysis: IndicatorAnalysis,
        revenueGrowthAnalysis: IndicatorAnalysis,
        operatingMarginAnalysis: IndicatorAnalysis,
        overallAnalysis: OverallAnalysis
    ) {
        try {
            Log.d(TAG, "=== UI 업데이트 시작 ===")
            
            // 종합 분석 업데이트
            tvOverallComment.text = overallAnalysis.comment
            updateAttractionStars(overallAnalysis.attractionScore)
            updateOverallAnalysisBackground(overallAnalysis.rating)
            
            // PER 업데이트
            tvPerValue.text = perAnalysis.value
            tvPerIndustryAvg.text = perAnalysis.industryAvg
            tvPerComment.text = perAnalysis.comment
            progressBarPer.progress = perAnalysis.progressPercent
            updateProgressBarColor(progressBarPer, perAnalysis.rating)
            
            // PBR 업데이트
            tvPbrValue.text = pbrAnalysis.value
            tvPbrIndustryAvg.text = pbrAnalysis.industryAvg
            tvPbrComment.text = pbrAnalysis.comment
            progressBarPbr.progress = pbrAnalysis.progressPercent
            updateProgressBarColor(progressBarPbr, pbrAnalysis.rating)
            
            // ROE 업데이트
            tvRoeValue.text = roeAnalysis.value
            tvRoeIndustryAvg.text = roeAnalysis.industryAvg
            tvRoeComment.text = roeAnalysis.comment
            progressBarRoe.progress = roeAnalysis.progressPercent
            updateProgressBarColor(progressBarRoe, roeAnalysis.rating)
            
            // EPS 업데이트
            tvEpsValue.text = epsAnalysis.value
            tvEpsIndustryAvg.text = epsAnalysis.industryAvg
            tvEpsComment.text = epsAnalysis.comment
            progressBarEps.progress = epsAnalysis.progressPercent
            updateProgressBarColor(progressBarEps, epsAnalysis.rating)
            
            // 영업이익률 업데이트
            tvOperatingMarginValue.text = operatingMarginAnalysis.value
            tvOperatingMarginIndustryAvg.text = operatingMarginAnalysis.industryAvg
            tvOperatingMarginComment.text = operatingMarginAnalysis.comment
            progressBarOperatingMargin.progress = operatingMarginAnalysis.progressPercent
            updateProgressBarColor(progressBarOperatingMargin, operatingMarginAnalysis.rating)
            
            // 매출성장률 업데이트
            tvRevenueGrowthValue.text = revenueGrowthAnalysis.value
            tvRevenueGrowthIndustryAvg.text = revenueGrowthAnalysis.industryAvg
            tvRevenueGrowthComment.text = revenueGrowthAnalysis.comment
            progressBarRevenueGrowth.progress = revenueGrowthAnalysis.progressPercent
            updateProgressBarColor(progressBarRevenueGrowth, revenueGrowthAnalysis.rating)
            
            Log.d(TAG, "✅ UI 업데이트 완료 (6개 지표)")
            
        } catch (e: Exception) {
            Log.e(TAG, "UI 업데이트 오류", e)
        }
    }
    
    /**
     * 투자 매력도 별표 업데이트 (5단계 시스템)
     */
    private fun updateAttractionStars(score: Int) {
        val stars = listOf(attractionStar1, attractionStar2, attractionStar3, attractionStar4, attractionStar5)
        
        stars.forEachIndexed { index, star ->
            if (index < score) {
                // 채워진 별
                star.alpha = 1.0f
                star.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.black)
            } else {
                // 빈 별
                star.alpha = 0.3f
                star.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.black)
            }
        }
    }
    
    /**
     * 프로그레스 바 색상 업데이트 (5단계 색상 시스템)
     */
    private fun updateProgressBarColor(progressBar: android.widget.ProgressBar, rating: IndicatorRating) {
        val colorRes = when (rating) {
            IndicatorRating.EXCELLENT -> R.color.rating_excellent
            IndicatorRating.GOOD -> R.color.rating_good  
            IndicatorRating.AVERAGE -> R.color.rating_average
            IndicatorRating.POOR -> R.color.rating_poor
            IndicatorRating.BAD -> R.color.rating_bad
        }
        
        try {
            progressBar.progressTintList = ContextCompat.getColorStateList(requireContext(), colorRes)
        } catch (e: Exception) {
            // 색상 리소스가 없는 경우 기본 색상 사용
            progressBar.progressTintList = ContextCompat.getColorStateList(requireContext(), R.color.rating_average)
            Log.w(TAG, "Color resource not found: $colorRes, using default")
        }
    }
    
    /**
     * 종합 분석 박스 배경 색상 업데이트 (5단계 색상 시스템)
     */
    private fun updateOverallAnalysisBackground(rating: IndicatorRating) {
        try {
            val backgroundColorRes = when (rating) {
                IndicatorRating.EXCELLENT -> R.color.bg_rating_excellent
                IndicatorRating.GOOD -> R.color.bg_rating_good
                IndicatorRating.AVERAGE -> R.color.bg_rating_average
                IndicatorRating.POOR -> R.color.bg_rating_poor
                IndicatorRating.BAD -> R.color.bg_rating_bad
            }
            
            // 종합 분석 박스 배경 색상 변경 (둥근 모서리 유지)
            val overallAnalysisBox = view?.findViewById<LinearLayout>(R.id.overall_analysis_box)
            overallAnalysisBox?.let { box ->
                val backgroundColor = ContextCompat.getColor(requireContext(), backgroundColorRes)
                
                // 둥근 모서리를 유지하면서 배경 색상 변경
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_rounded_light_green)?.mutate()
                drawable?.setTint(backgroundColor)
                box.background = drawable
            }
            
        } catch (e: Exception) {
            Log.w(TAG, "종합 분석 박스 배경 색상 업데이트 실패", e)
        }
    }
    
    /**
     * 뉴스 클릭 이벤트 설정
     */
    private fun setupNewsClickListeners() {
        try {
            // 뉴스 아이템 1 클릭
            newsItem1.setOnClickListener {
                if (newsData.isNotEmpty()) {
                    openNewsLink(newsData[0].link)
                }
            }
            
            // 뉴스 아이템 2 클릭
            newsItem2.setOnClickListener {
                if (newsData.size > 1) {
                    openNewsLink(newsData[1].link)
                }
            }
            
            // 뉴스 아이템 3 클릭
            newsItem3.setOnClickListener {
                if (newsData.size > 2) {
                    openNewsLink(newsData[2].link)
                }
            }
            
            // 뉴스 더보기 버튼 클릭
            btnNewsMore.setOnClickListener {
                openNewsListPage()
            }
            
            Log.d(TAG, "News click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up news click listeners", e)
        }
    }
    
    /**
     * 뉴스 데이터 로드
     */
    private fun loadNewsData() {
        val targetStockName = stockName?.takeIf { it.isNotBlank() && it != "알 수 없음" } ?: "DSR제강"
        
        lifecycleScope.launch {
            try {
                Log.d(TAG, "=== 뉴스 API 호출 시작 ===")
                Log.d(TAG, "Loading news for: $targetStockName")
                Log.d(TAG, "API URL: ${BASE_URL}news_naver_real/$targetStockName")
                
                val response = newsApiService.getStockNews(targetStockName)
                
                Log.d(TAG, "뉴스 API 응답 수신")
                Log.d(TAG, "Response successful: ${response.isSuccessful}")
                Log.d(TAG, "Response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    val newsResponse = response.body()
                    if (newsResponse != null && newsResponse.data.isNotEmpty()) {
                        newsData = newsResponse.data
                        
                        Log.d(TAG, "뉴스 데이터 개수: ${newsData.size}")
                        newsData.forEachIndexed { index, news ->
                            Log.d(TAG, "뉴스 $index: ${news.title}")
                        }
                        
                        // UI 업데이트
                        updateNewsUI(newsData)
                        
                    } else {
                        Log.w(TAG, "뉴스 데이터가 비어있습니다")
                        // 샘플 데이터로 폴백
                        showSampleNews()
                    }
                } else {
                    Log.e(TAG, "뉴스 API 호출 실패: ${response.code()} - ${response.message()}")
                    // 샘플 데이터로 폴백
                    showSampleNews()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "뉴스 데이터 로드 중 오류", e)
                // 샘플 데이터로 폴백
                showSampleNews()
            }
        }
    }
    
    /**
     * 뉴스 UI 업데이트
     */
    private fun updateNewsUI(newsList: List<NewsItem>) {
        try {
            // 첫 번째 뉴스 아이템
            if (newsList.isNotEmpty()) {
                val news1 = newsList[0]
                tvNewsDate1.text = news1.pDate ?: ""
                tvNewsTitle1.text = news1.title ?: ""
                
                // 이미지 로드
                loadNewsImage(news1.ogImage, ivNewsImage1, "News 1")
            }
            
            // 두 번째 뉴스 아이템
            if (newsList.size > 1) {
                val news2 = newsList[1]
                tvNewsDate2.text = news2.pDate ?: ""
                tvNewsTitle2.text = news2.title ?: ""
                
                // 이미지 로드
                loadNewsImage(news2.ogImage, ivNewsImage2, "News 2")
            }
            
            // 세 번째 뉴스 아이템
            if (newsList.size > 2) {
                val news3 = newsList[2]
                tvNewsDate3.text = news3.pDate ?: ""
                tvNewsTitle3.text = news3.title ?: ""
                
                // 이미지 로드
                loadNewsImage(news3.ogImage, ivNewsImage3, "News 3")
            }
            
            Log.d(TAG, "✅ 뉴스 UI 업데이트 완료")
            
        } catch (e: Exception) {
            Log.e(TAG, "뉴스 UI 업데이트 오류", e)
        }
    }
    
    /**
     * 샘플 뉴스 데이터 표시 (API 실패 시 폴백)
     */
    private fun showSampleNews() {
        Log.d(TAG, "샘플 뉴스 데이터 표시")
        // 현재 레이아웃에 이미 설정된 샘플 데이터를 그대로 사용
    }
    
    /**
     * 뉴스 이미지 로드
     */
    private fun loadNewsImage(imageUrl: String?, imageView: ImageView, logTag: String) {
        try {
            if (!imageUrl.isNullOrEmpty()) {
                Log.d(TAG, "$logTag image URL: $imageUrl")
                
                // Glide를 사용한 이미지 로딩 (둥근 모서리 적용)
                com.bumptech.glide.Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_business_24)
                    .error(R.drawable.ic_business_24)
                    .centerCrop()
                    .transform(com.bumptech.glide.load.resource.bitmap.RoundedCorners(24)) // 8dp의 3배(density 고려)
                    .into(imageView)
                    
                Log.d(TAG, "$logTag 이미지 로딩 요청 완료")
            } else {
                Log.d(TAG, "$logTag 이미지 URL이 없습니다")
                // 기본 이미지도 둥근 모서리 적용
                com.bumptech.glide.Glide.with(requireContext())
                    .load(R.drawable.ic_business_24)
                    .centerCrop()
                    .transform(com.bumptech.glide.load.resource.bitmap.RoundedCorners(24))
                    .into(imageView)
            }
        } catch (e: Exception) {
            Log.e(TAG, "$logTag 이미지 로딩 오류", e)
            // 에러 시에도 둥근 모서리 적용
            com.bumptech.glide.Glide.with(requireContext())
                .load(R.drawable.ic_business_24)
                .centerCrop()
                .transform(com.bumptech.glide.load.resource.bitmap.RoundedCorners(24))
                .into(imageView)
        }
    }
    
    /**
     * 뉴스 목록 페이지 열기 (앱 내 웹뷰)
     */
    private fun openNewsListPage() {
        try {
            val stockName = stockName?.takeIf { it.isNotBlank() && it != "알 수 없음" } ?: "DSR제강"
            Log.d(TAG, "뉴스 목록 페이지 열기: $stockName")
            
            // 앱 내 웹뷰로 네이버 뉴스 검색 페이지 열기
            val searchUrl = "https://search.naver.com/search.naver?where=news&query=${java.net.URLEncoder.encode(stockName, "UTF-8")}"
            val intent = com.example.antwinner_kotlin.ui.webview.WebViewActivity.newIntent(
                requireContext(),
                searchUrl,
                "$stockName 뉴스"
            )
            startActivity(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "뉴스 목록 페이지 열기 실패", e)
        }
    }
    
    /**
     * 뉴스 링크 열기 (앱 내 웹뷰)
     */
    private fun openNewsLink(link: String?) {
        if (!link.isNullOrEmpty()) {
            try {
                val intent = com.example.antwinner_kotlin.ui.webview.WebViewActivity.newIntent(
                    requireContext(),
                    link,
                    "뉴스"
                )
                startActivity(intent)
                Log.d(TAG, "뉴스 링크 열기 (웹뷰): $link")
            } catch (e: Exception) {
                Log.e(TAG, "뉴스 링크 열기 실패", e)
            }
        }
    }

    /**
     * 공시 클릭 이벤트 설정
     */
    private fun setupDisclosureClickListeners() {
        try {
            // 공시 아이템 1 클릭
            disclosureItem1.setOnClickListener {
                if (disclosureData.isNotEmpty()) {
                    openDisclosureLink(disclosureData[0].url)
                }
            }
            
            // 공시 아이템 2 클릭
            disclosureItem2.setOnClickListener {
                if (disclosureData.size > 1) {
                    openDisclosureLink(disclosureData[1].url)
                }
            }
            
            // 공시 아이템 3 클릭
            disclosureItem3.setOnClickListener {
                if (disclosureData.size > 2) {
                    openDisclosureLink(disclosureData[2].url)
                }
            }
            
            // 공시 더보기 버튼 클릭
            btnDisclosureMore.setOnClickListener {
                openDisclosureListPage()
            }
            
            Log.d(TAG, "공시 클릭 이벤트 설정 완료")
        } catch (e: Exception) {
            Log.e(TAG, "공시 클릭 이벤트 설정 실패", e)
        }
    }
    
    /**
     * 공시 데이터 로드
     */
    private fun loadDisclosureData() {
        val targetStockName = stockName?.takeIf { it.isNotBlank() && it != "알 수 없음" } ?: "파마리서치"
        
        try {
            Log.d(TAG, "=== 공시 API 호출 시작 ===")
            Log.d(TAG, "Loading disclosure for: $targetStockName")
            Log.d(TAG, "API URL: ${BASE_URL}gongsi/company/$targetStockName")
            
            val call = disclosureApiService.getDisclosureData(targetStockName)
            call.enqueue(object : retrofit2.Callback<DisclosureResponse> {
                override fun onResponse(
                    call: retrofit2.Call<DisclosureResponse>,
                    response: retrofit2.Response<DisclosureResponse>
                ) {
                    Log.d(TAG, "공시 API 응답 수신")
                    Log.d(TAG, "Response successful: ${response.isSuccessful}")
                    Log.d(TAG, "Response code: ${response.code()}")
                    
                    if (response.isSuccessful) {
                        val disclosureResponse = response.body()
                        Log.d(TAG, "Response body: $disclosureResponse")
                        
                        if (disclosureResponse != null && disclosureResponse.data.isNotEmpty()) {
                            Log.d(TAG, "✅ 공시 API 성공! 데이터 개수: ${disclosureResponse.data.size}")
                            Log.d(TAG, "회사명: ${disclosureResponse.companyName}")
                            
                            // 첫 번째 공시 데이터 상세 로깅
                            disclosureResponse.data.take(3).forEachIndexed { index, item ->
                                Log.d(TAG, "공시 $index: ${item.reportNm} (${item.rceptDt})")
                            }
                            
                            // API 데이터를 DisclosureItem으로 변환
                            val disclosureItems = convertApiDataToDisclosureItems(disclosureResponse.data)
                            disclosureData = disclosureItems
                            
                            // UI 업데이트
                            updateDisclosureUI(disclosureItems)
                            
                        } else {
                            Log.w(TAG, "❌ 공시 데이터가 비어있습니다")
                            Log.w(TAG, "Response body null: ${disclosureResponse == null}")
                            if (disclosureResponse != null) {
                                Log.w(TAG, "Data list size: ${disclosureResponse.data.size}")
                                Log.w(TAG, "Company name: '${disclosureResponse.companyName}'")
                            }
                            // 샘플 데이터로 폴백
                            showSampleDisclosure()
                        }
                    } else {
                        Log.e(TAG, "❌ 공시 API 호출 실패: ${response.code()} - ${response.message()}")
                        
                        // 에러 바디 로깅
                        try {
                            val errorBody = response.errorBody()?.string()
                            Log.e(TAG, "Error body: $errorBody")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error reading error body", e)
                        }
                        
                        // 샘플 데이터로 폴백
                        showSampleDisclosure()
                    }
                }

                override fun onFailure(call: retrofit2.Call<DisclosureResponse>, t: Throwable) {
                    Log.e(TAG, "공시 API 호출 실패", t)
                    // 샘플 데이터로 폴백
                    showSampleDisclosure()
                }
            })
            
        } catch (e: Exception) {
            Log.e(TAG, "공시 데이터 로드 중 오류", e)
            // 샘플 데이터로 폴백
            showSampleDisclosure()
        }
    }
    
    /**
     * API 데이터를 DisclosureItem으로 변환
     */
    private fun convertApiDataToDisclosureItems(apiData: List<DisclosureApiItem>): List<DisclosureItem> {
        return apiData.take(3).map { apiItem ->
            DisclosureItem(
                title = apiItem.reportNm,
                date = formatApiDate(apiItem.rceptDt),
                category = determineDisclosureCategory(apiItem.reportNm),
                url = generateDisclosureUrl(apiItem.rceptNo),
                importance = determineDisclosureImportance(apiItem.reportNm)
            )
        }
    }
    
    /**
     * API 날짜 포맷 변환 (20250821 -> 2025-08-21)
     */
    private fun formatApiDate(apiDate: String): String {
        return try {
            if (apiDate.length == 8) {
                val year = apiDate.substring(0, 4)
                val month = apiDate.substring(4, 6)
                val day = apiDate.substring(6, 8)
                "$year-$month-$day"
            } else {
                apiDate
            }
        } catch (e: Exception) {
            Log.e(TAG, "날짜 포맷 변환 실패: $apiDate", e)
            apiDate
        }
    }
    
    /**
     * 공시 제목으로 카테고리 판단
     */
    private fun determineDisclosureCategory(reportName: String): String {
        return when {
            reportName.contains("실적") || reportName.contains("보고서") -> "실적발표"
            reportName.contains("자기주식") -> "자기주식취득"
            reportName.contains("인사") || reportName.contains("임원") -> "인사발령"
            reportName.contains("배당") -> "배당"
            reportName.contains("합병") || reportName.contains("분할") -> "기업구조변경"
            reportName.contains("증자") || reportName.contains("감자") -> "증자/감자"
            reportName.contains("공시") -> "기타공시"
            else -> "일반공시"
        }
    }
    
    /**
     * 공시 제목으로 중요도 판단
     */
    private fun determineDisclosureImportance(reportName: String): String {
        return when {
            reportName.contains("실적") || reportName.contains("보고서") || 
            reportName.contains("합병") || reportName.contains("분할") ||
            reportName.contains("증자") || reportName.contains("감자") -> "높음"
            reportName.contains("자기주식") || reportName.contains("배당") ||
            reportName.contains("불성실") -> "보통"
            else -> "낮음"
        }
    }
    
    /**
     * DART 공시 URL 생성
     */
    private fun generateDisclosureUrl(rceptNo: String?): String? {
        return if (!rceptNo.isNullOrEmpty()) {
            "https://dart.fss.or.kr/dsaf001/main.do?rcpNo=$rceptNo"
        } else {
            null
        }
    }
    
    /**
     * 샘플 공시 데이터 표시
     */
    private fun showSampleDisclosure() {
        try {
            val sampleDisclosures = listOf(
                DisclosureItem(
                    title = "3분기 실적발표",
                    date = "2024-05-23",
                    category = "실적발표",
                    url = null,
                    importance = "높음"
                ),
                DisclosureItem(
                    title = "자기주식 취득 결정",
                    date = "2024-05-23",
                    category = "자기주식취득",
                    url = null,
                    importance = "보통"
                ),
                DisclosureItem(
                    title = "임원 인사발령",
                    date = "2024-05-23",
                    category = "인사발령",
                    url = null,
                    importance = "낮음"
                )
            )
            
            disclosureData = sampleDisclosures
            updateDisclosureUI(sampleDisclosures)
            
            Log.d(TAG, "샘플 공시 데이터 표시 완료")
        } catch (e: Exception) {
            Log.e(TAG, "샘플 공시 데이터 표시 실패", e)
        }
    }
    
    /**
     * 공시 UI 업데이트
     */
    private fun updateDisclosureUI(disclosures: List<DisclosureItem>) {
        try {
            if (disclosures.isNotEmpty()) {
                // 첫 번째 공시
                tvDisclosureTitle1.text = disclosures[0].title
                tvDisclosureDate1.text = formatDisclosureDate(disclosures[0].date)
                updateDisclosureDotColor(vDisclosureDot1, disclosures[0].importance)
            }
            
            if (disclosures.size > 1) {
                // 두 번째 공시
                tvDisclosureTitle2.text = disclosures[1].title
                tvDisclosureDate2.text = formatDisclosureDate(disclosures[1].date)
                updateDisclosureDotColor(vDisclosureDot2, disclosures[1].importance)
            }
            
            if (disclosures.size > 2) {
                // 세 번째 공시
                tvDisclosureTitle3.text = disclosures[2].title
                tvDisclosureDate3.text = formatDisclosureDate(disclosures[2].date)
                updateDisclosureDotColor(vDisclosureDot3, disclosures[2].importance)
            }
            
            Log.d(TAG, "공시 UI 업데이트 완료")
        } catch (e: Exception) {
            Log.e(TAG, "공시 UI 업데이트 실패", e)
        }
    }
    
    /**
     * 공시 날짜 포맷팅
     */
    private fun formatDisclosureDate(dateString: String): String {
        return try {
            val parts = dateString.split("-")
            if (parts.size == 3) {
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                "공시 · ${month}월 ${day}일"
            } else {
                "공시 · $dateString"
            }
        } catch (e: Exception) {
            "공시 · $dateString"
        }
    }
    
    /**
     * 공시 점 색상 업데이트
     */
    private fun updateDisclosureDotColor(dotView: View, importance: String) {
        try {
            val colorRes = when (importance) {
                "높음" -> R.color.rising_color  // 빨강
                "보통" -> android.R.color.holo_orange_light  // 오렌지
                "낮음" -> R.color.excellent_green  // 초록
                else -> R.color.rising_color
            }
            
            dotView.backgroundTintList = ContextCompat.getColorStateList(requireContext(), colorRes)
        } catch (e: Exception) {
            Log.e(TAG, "공시 점 색상 업데이트 실패", e)
        }
    }
    
    /**
     * 공시 목록 페이지 열기 (앱 내 웹뷰)
     */
    private fun openDisclosureListPage() {
        try {
            val stockName = stockName?.takeIf { it.isNotBlank() && it != "알 수 없음" } ?: "DSR제강"
            Log.d(TAG, "공시 목록 페이지 열기: $stockName")
            
            // 앱 내 웹뷰로 DART 공시 검색 페이지 열기
            val searchUrl = "https://dart.fss.or.kr/dsab007/main.do?rcpNo=&crpNm=${java.net.URLEncoder.encode(stockName, "UTF-8")}"
            val intent = com.example.antwinner_kotlin.ui.webview.WebViewActivity.newIntent(
                requireContext(),
                searchUrl,
                "$stockName 공시정보"
            )
            startActivity(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "공시 목록 페이지 열기 실패", e)
        }
    }
    
    /**
     * 공시 링크 열기 (앱 내 웹뷰)
     */
    private fun openDisclosureLink(link: String?) {
        if (!link.isNullOrEmpty()) {
            try {
                val intent = com.example.antwinner_kotlin.ui.webview.WebViewActivity.newIntent(
                    requireContext(),
                    link,
                    "공시정보"
                )
                startActivity(intent)
                Log.d(TAG, "공시 링크 열기 (웹뷰): $link")
            } catch (e: Exception) {
                Log.e(TAG, "공시 링크 열기 실패", e)
            }
        } else {
            // 링크가 없는 경우 공시 목록 페이지로 이동
            openDisclosureListPage()
        }
    }


    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView called")
        super.onDestroyView()
    }
}

 