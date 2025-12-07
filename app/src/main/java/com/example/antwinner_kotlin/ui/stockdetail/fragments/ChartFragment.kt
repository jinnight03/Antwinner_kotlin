package com.example.antwinner_kotlin.ui.stockdetail.fragments

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.databinding.FragmentChartBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.random.Random

class ChartFragment : Fragment() {
    
    companion object {
        private const val ARG_STOCK_CODE = "stock_code"
        private const val TAG = "ChartFragment"
        
        fun newInstance(stockCode: String): ChartFragment {
            return ChartFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STOCK_CODE, stockCode)
                }
            }
        }
    }
    
    private var _binding: FragmentChartBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var candlestickChart: CombinedChart
    private lateinit var volumeChart: BarChart
    
    private var currentPeriod = "3M"
    private lateinit var stockData: List<CandleData>
    private lateinit var allStockData: List<CandleData> // 전체 데이터 저장용
    private lateinit var stockCode: String
    private lateinit var stockName: String
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChartBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Arguments에서 stockCode 가져오기
        stockCode = arguments?.getString(ARG_STOCK_CODE) ?: "000000"
        stockName = arguments?.getString("stock_name") ?: stockCode
        
        initializeCharts()
        setupPeriodButtons()
        loadChartData()
    }
    
    private fun initializeCharts() {
        candlestickChart = binding.candlestickChart
        volumeChart = binding.volumeChart
        
        setupCandlestickChart()
        setupVolumeChart()
    }
    
    private fun setupCandlestickChart() {
        candlestickChart.apply {
            setDrawGridBackground(false)
            setDrawBorders(false)
            setScaleEnabled(true)
            setPinchZoom(true)
            setDoubleTapToZoomEnabled(true)
            
            description.isEnabled = false
            legend.isEnabled = true
            legend.textColor = Color.BLACK
            
            // X축 설정
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.BLACK
                valueFormatter = DateValueFormatter()
                granularity = 1f
                setAvoidFirstLastClipping(true)
            }
            
            // 좌측 Y축 설정
            axisLeft.apply {
                setDrawGridLines(false)
                textColor = Color.BLACK
                setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                valueFormatter = PriceValueFormatter()
            }
            
            // 우측 Y축 비활성화
            axisRight.isEnabled = false
            
            // 차트 터치 리스너 제거 (정보 영역이 없으므로)
            setOnChartValueSelectedListener(null)
        }
    }
    
    private fun setupVolumeChart() {
        volumeChart.apply {
            setDrawGridBackground(false)
            setDrawBorders(false)
            setScaleEnabled(true)
            setPinchZoom(true)
            
            description.isEnabled = false
            legend.isEnabled = false
            
            // X축 설정
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.BLACK
                valueFormatter = DateValueFormatter()
                granularity = 1f
                setAvoidFirstLastClipping(true)
            }
            
            // 좌측 Y축 설정
            axisLeft.apply {
                setDrawGridLines(false)
                textColor = Color.BLACK
                valueFormatter = VolumeValueFormatter()
            }
            
            // 우측 Y축 비활성화
            axisRight.isEnabled = false
        }
    }
    
    /**
     * 차트 데이터 로드 (API 호출 + 폴백)
     */
    private fun loadChartData() {
        Log.d(TAG, "Loading chart data for: $stockName")
        
        lifecycleScope.launch {
            try {
                val apiData = fetchChartDataFromApi(stockName)
                if (apiData.isNotEmpty()) {
                    Log.d(TAG, "API data loaded: ${apiData.size} records")
                    allStockData = apiData.map { it.toCandleData() }
                    // 기본 3개월 기간으로 필터링
                    stockData = filterDataByPeriod(allStockData, currentPeriod)
                    updateCharts()
                } else {
                    Log.w(TAG, "No API data, using sample data")
                    generateSampleData()
                    updateCharts()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading API data, using sample data", e)
                generateSampleData()
                updateCharts()
            }
        }
    }
    
    /**
     * API에서 차트 데이터 가져오기
     */
    private suspend fun fetchChartDataFromApi(stockName: String): List<ChartApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val client = createTrustAllOkHttpClient()
                val url = "https://antwinner.com/api/chart/$stockName"
                
                Log.d(TAG, "Fetching chart data from: $url")
                
                val request = Request.Builder()
                    .url(url)
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "API Response received, length: ${responseBody?.length}")
                    
                    if (!responseBody.isNullOrEmpty()) {
                        val gson = Gson()
                        val listType = object : TypeToken<List<ChartApiResponse>>() {}.type
                        val apiData: List<ChartApiResponse> = gson.fromJson(responseBody, listType)
                        
                        Log.d(TAG, "Parsed ${apiData.size} chart records")
                        return@withContext apiData.sortedBy { it.date } // 날짜순 정렬
                    }
                }
                
                Log.w(TAG, "API call failed or empty response")
                return@withContext emptyList<ChartApiResponse>()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching chart data", e)
                return@withContext emptyList<ChartApiResponse>()
            }
        }
    }
    
    /**
     * SSL 인증서 검증을 무시하는 OkHttpClient 생성
     */
    private fun createTrustAllOkHttpClient(): OkHttpClient {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            
            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating trust-all client", e)
            OkHttpClient()
        }
    }
    
    private fun generateSampleData() {
        val calendar = Calendar.getInstance()
        val dataPoints = when (currentPeriod) {
            "1D" -> 24 // 1일: 시간별
            "1W" -> 7  // 1주: 일별
            "1M" -> 30 // 1개월: 일별
            "3M" -> 90 // 3개월: 일별
            "1Y" -> 252 // 1년: 거래일
            else -> 500 // 전체: 2년 정도
        }
        
        // 시작 날짜 설정
        calendar.add(Calendar.DAY_OF_YEAR, -dataPoints)
        
        val data = mutableListOf<CandleData>()
        var currentPrice = 13000f + Random.nextFloat() * 2000f // 기준가 13000-15000원
        
        for (i in 0 until dataPoints) {
            val date = calendar.time
            
            // 가격 변동 (전일 대비 ±5%)
            val changePercent = (Random.nextFloat() - 0.5f) * 0.1f // ±5%
            val newPrice = currentPrice * (1 + changePercent)
            
            // OHLC 생성
            val open = currentPrice
            val close = newPrice
            val high = maxOf(open, close) * (1 + Random.nextFloat() * 0.02f) // 최대 2% 더 높게
            val low = minOf(open, close) * (1 - Random.nextFloat() * 0.02f)  // 최대 2% 더 낮게
            
            // 거래량 생성 (100만 ~ 500만주)
            val volume = (1000000L + Random.nextLong(4000000L))
            
            data.add(CandleData(date, open, high, low, close, volume))
            
            currentPrice = newPrice
            
            // 다음 날로
            if (currentPeriod == "1D") {
                calendar.add(Calendar.HOUR, 1) // 시간별
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, 1) // 일별
            }
        }
        
        allStockData = data
        stockData = filterDataByPeriod(allStockData, currentPeriod)
    }
    
    private fun updateCharts() {
        // 날짜 포맷터 업데이트
        candlestickChart.xAxis.valueFormatter = DateValueFormatter(stockData)
        volumeChart.xAxis.valueFormatter = DateValueFormatter(stockData)
        
        updateCandlestickChart()
        updateVolumeChart()
    }
    
    private fun updateCandlestickChart() {
        val candleEntries = mutableListOf<CandleEntry>()
        val ma5Entries = mutableListOf<Entry>()
        val ma20Entries = mutableListOf<Entry>()
        
        stockData.forEachIndexed { index, data ->
            candleEntries.add(
                CandleEntry(
                    index.toFloat(),
                    data.high,
                    data.low,
                    data.open,
                    data.close
                )
            )
        }
        
        // 이동평균선 계산
        val ma5Data = ChartIndicators.calculateSMA(stockData, 5)
        val ma20Data = ChartIndicators.calculateSMA(stockData, 20)
        
        ma5Data.forEachIndexed { index, data ->
            val dataIndex = stockData.indexOfFirst { it.date == data.date }
            if (dataIndex >= 0) {
                ma5Entries.add(Entry(dataIndex.toFloat(), data.value))
            }
        }
        
        ma20Data.forEachIndexed { index, data ->
            val dataIndex = stockData.indexOfFirst { it.date == data.date }
            if (dataIndex >= 0) {
                ma20Entries.add(Entry(dataIndex.toFloat(), data.value))
            }
        }
        
        // 캔들스틱 데이터셋
        val candleDataSet = CandleDataSet(candleEntries, "Price").apply {
            color = Color.BLACK
            shadowColor = Color.GRAY
            shadowWidth = 1f
            decreasingColor = Color.BLUE
            decreasingPaintStyle = Paint.Style.FILL
            increasingColor = Color.RED
            increasingPaintStyle = Paint.Style.FILL
            neutralColor = Color.GRAY
            setDrawValues(false)
        }
        
        // 5일 이동평균선
        val ma5DataSet = LineDataSet(ma5Entries, "MA5").apply {
            color = Color.parseColor("#FF6B35") // 주황색
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        
        // 20일 이동평균선
        val ma20DataSet = LineDataSet(ma20Entries, "MA20").apply {
            color = Color.parseColor("#4ECDC4") // 청록색
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        
        // Combined 데이터 생성
        val combinedData = CombinedData().apply {
            setData(CandleData(candleDataSet))
            setData(LineData(ma5DataSet, ma20DataSet))
        }
        
        candlestickChart.data = combinedData
        candlestickChart.invalidate()
    }
    
    private fun updateVolumeChart() {
        val volumeEntries = mutableListOf<BarEntry>()
        
        stockData.forEachIndexed { index, data ->
            volumeEntries.add(BarEntry(index.toFloat(), data.volume.toFloat()))
        }
        
        val volumeDataSet = BarDataSet(volumeEntries, "Volume").apply {
            setColors(stockData.mapIndexed { index, data ->
                if (index == 0) Color.GRAY
                else if (data.close >= stockData[index - 1].close) Color.RED
                else Color.BLUE
            })
            setDrawValues(false)
        }
        
        volumeChart.data = BarData(volumeDataSet)
        volumeChart.invalidate()
    }
    

    
    private fun setupPeriodButtons() {
        binding.btnPeriod1w.setOnClickListener { changePeriod("1W") }
        binding.btnPeriod1m.setOnClickListener { changePeriod("1M") }
        binding.btnPeriod3m.setOnClickListener { changePeriod("3M") }
        binding.btnPeriod1y.setOnClickListener { changePeriod("1Y") }
        binding.btnPeriodAll.setOnClickListener { changePeriod("ALL") }
        
        // 초기 선택: 3개월
        updatePeriodButtonSelection("3M")
    }
    
    private fun changePeriod(period: String) {
        currentPeriod = period
        updatePeriodButtonSelection(period)
        
        // 전체 데이터가 있으면 필터링만, 없으면 새로 로드
        if (::allStockData.isInitialized && allStockData.isNotEmpty()) {
            val filteredData = filterDataByPeriod(allStockData, period)
            stockData = if (filteredData.isNotEmpty()) {
                filteredData
            } else {
                allStockData // 필터링 결과가 없으면 전체 데이터 사용
            }
            updateCharts()
        } else {
            loadChartData()
        }
    }
    
    /**
     * 기간에 따라 데이터 필터링
     */
    private fun filterDataByPeriod(allData: List<CandleData>, period: String): List<CandleData> {
        if (allData.isEmpty()) return allData
        
        return when (period) {
            "1W" -> {
                // 최근 1주치 데이터 (7일)
                allData.takeLast(minOf(7, allData.size))
            }
            "1M" -> {
                // 최근 1개월치 데이터 (약 30일)
                allData.takeLast(minOf(30, allData.size))
            }
            "3M" -> {
                // 최근 3개월치 데이터 (약 90일)
                allData.takeLast(minOf(90, allData.size))
            }
            "1Y" -> {
                // 최근 1년치 데이터 (약 252 거래일)
                allData.takeLast(minOf(252, allData.size))
            }
            "ALL" -> {
                // 전체 데이터
                allData
            }
            else -> allData
        }
    }
    
    private fun updatePeriodButtonSelection(selectedPeriod: String) {
        val buttons = listOf(
            binding.btnPeriod1w, binding.btnPeriod1m,
            binding.btnPeriod3m, binding.btnPeriod1y, binding.btnPeriodAll
        )
        
        val periods = listOf("1W", "1M", "3M", "1Y", "ALL")
        
        buttons.forEachIndexed { index, button ->
            val isSelected = periods[index] == selectedPeriod
            button.isSelected = isSelected
            
            // 텍스트 색상 직접 설정
            if (isSelected) {
                button.setTextColor(Color.parseColor("#333333")) // 선택시: 진한 회색 (가독성 좋음)
            } else {
                button.setTextColor(Color.parseColor("#888888")) // 기본: 연한 회색
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// 날짜 포맷터
class DateValueFormatter(private val stockData: List<CandleData> = emptyList()) : ValueFormatter() {
    private val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
    
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return try {
            val index = value.toInt()
            if (index >= 0 && index < stockData.size) {
                dateFormat.format(stockData[index].date)
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}

// 가격 포맷터
class PriceValueFormatter : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return String.format("%,.0f", value)
    }
}

// 거래량 포맷터
class VolumeValueFormatter : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return when {
            value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000)
            value >= 1_000 -> String.format("%.1fK", value / 1_000)
            else -> String.format("%.0f", value)
        }
    }
} 