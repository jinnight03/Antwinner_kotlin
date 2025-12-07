package com.example.antwinner_kotlin.ui.stockdetail.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import timber.log.Timber
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
import java.text.SimpleDateFormat
import java.util.*

class WhyRiseFragment : Fragment() {

    companion object {
        private const val ARG_STOCK_CODE = "stock_code"
        private const val ARG_STOCK_NAME = "stock_name"
        private const val TAG = "WhyRiseFragment"

        fun newInstance(stockCode: String): WhyRiseFragment {
            Timber.tag(TAG).d("Creating new WhyRiseFragment instance")
            return WhyRiseFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STOCK_CODE, stockCode)
                }
            }
        }
    }

    private var stockCode: String? = null
    private var stockName: String? = null
    private lateinit var tvTotalCount: TextView
    private lateinit var rvWhyRiseList: RecyclerView
    private lateinit var whyRiseAdapter: WhyRiseAdapter

    // API 서비스
    private val apiService by lazy {
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
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://antwinner.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        retrofit.create(WhyRiseApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            stockCode = it.getString(ARG_STOCK_CODE)
        }
        
        // 부모 Activity에서 stockName 가져오기
        stockName = (activity as? com.example.antwinner_kotlin.ui.stockdetail.StockDetailActivity)?.let { activity ->
            activity.intent.getStringExtra("extra_stock_name") ?: "알 수 없음"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.tag(TAG).d("onCreateView called")
        return inflater.inflate(R.layout.fragment_why_rise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.tag(TAG).d("onViewCreated called")

        initViews(view)
        setupRecyclerView()
        
        // 실제 API 호출 또는 샘플 데이터 로드
        if (!stockName.isNullOrEmpty() && stockName != "알 수 없음") {
            fetchWhyRiseData(stockName!!)
        } else {
            loadSampleData()
        }
    }

    private fun initViews(view: View) {
        tvTotalCount = view.findViewById(R.id.tv_total_count)
        rvWhyRiseList = view.findViewById(R.id.rv_why_rise_list)
        
        // 정보 버튼 클릭 리스너
        view.findViewById<ImageButton>(R.id.btn_info).setOnClickListener {
            Toast.makeText(
                context,
                "왜 올랐을까?: 해당 종목이 크게 상승한 날의 상승 이유와 관련 정보를 보여줍니다.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupRecyclerView() {
        whyRiseAdapter = WhyRiseAdapter(emptyList())
        rvWhyRiseList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = whyRiseAdapter
            isNestedScrollingEnabled = true
        }
    }

    private fun fetchWhyRiseData(stockName: String) {
        Timber.tag(TAG).d("API 호출 시작 - 종목명: $stockName")
        
        // URL 인코딩 제거 - Retrofit이 자동으로 처리
        val cleanStockName = stockName.trim()
        Timber.tag(TAG).d("정리된 종목명: $cleanStockName")
        
        val call = apiService.getWhyRiseData(cleanStockName)
        call.enqueue(object : Callback<List<WhyRiseApiResponse>> {
            override fun onResponse(
                call: Call<List<WhyRiseApiResponse>>,
                response: Response<List<WhyRiseApiResponse>>
            ) {
                val requestUrl = call.request().url.toString()
                Timber.tag(TAG).d("실제 요청 URL: $requestUrl")
                Timber.tag(TAG).d("응답 코드: ${response.code()}")
                
                if (response.isSuccessful) {
                    val apiData = response.body()
                    Timber.tag(TAG).d("응답 데이터: $apiData")
                    
                    if (!apiData.isNullOrEmpty()) {
                        Timber.tag(TAG).d("API 응답 성공: ${apiData.size}개 항목")
                        val whyRiseItems = apiData.map { item ->
                            WhyRiseItem(
                                date = formatDate(item.날자 ?: ""),
                                riseReason = item.상승이유 ?: "",
                                riseRate = formatRiseRate(item.상승률),
                                tradeInfo = "거래량 ${item.거래량 ?: ""} · 거래대금 ${item.거래대금 ?: ""}",
                                theme = item.테마?.takeIf { it.isNotEmpty() } ?: extractThemeFromReason(item.상승이유 ?: "")
                            )
                        }
                        
                        whyRiseAdapter.updateData(whyRiseItems)
                        tvTotalCount.text = "총 ${whyRiseItems.size}회"
                    } else {
                        Timber.tag(TAG).e("API 응답은 성공했지만 데이터가 비어 있습니다")
                        loadSampleData()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Timber.tag(TAG).e("API 오류 - 코드: ${response.code()}, 메시지: ${response.message()}, 에러 바디: $errorBody")
                    loadSampleData()
                }
            }

            override fun onFailure(call: Call<List<WhyRiseApiResponse>>, t: Throwable) {
                Timber.tag(TAG).e(t, "API 호출 실패: ${t.message}")
                loadSampleData()
            }
        })
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "날짜 없음"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy년\nM월 d일", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "날짜 포맷 오류: $dateString")
            dateString
        }
    }

    private fun formatRiseRate(riseRate: Double): String {
        return "+${String.format("%.1f", riseRate * 100)}%"
    }

    private fun extractThemeFromReason(reason: String?): String {
        if (reason.isNullOrEmpty()) return ""
        // [테마명] 형태에서 테마명 추출
        val regex = "\\[(.*?)\\]".toRegex()
        val match = regex.find(reason)
        return match?.groupValues?.get(1) ?: ""
    }

    private fun loadSampleData() {
        val sampleData = listOf(
            WhyRiseItem(
                date = "2025년\n4월 10일",
                riseReason = "트럼프, 중국 제의 국가별 상호관세 90일간 전격 유예",
                riseRate = "+12%",
                tradeInfo = "거래량 170만 · 거래대금 43억",
                theme = "반도체(인공지능)"
            ),
            WhyRiseItem(
                date = "2025년\n4월 10일",
                riseReason = "트럼프, 중국 제의 국가별 상호관세 90일간 전격 유예",
                riseRate = "+12%",
                tradeInfo = "거래량 170만 · 거래대금 43억",
                theme = "반도체(인공지능)"
            ),
            WhyRiseItem(
                date = "2024년\n12월 15일",
                riseReason = "AI 반도체 수요 급증으로 인한 관련주 동반 상승",
                riseRate = "+8.5%",
                tradeInfo = "거래량 230만 · 거래대금 67억",
                theme = "AI반도체"
            ),
            WhyRiseItem(
                date = "2024년\n11월 28일",
                riseReason = "정부 반도체 지원 정책 발표에 따른 업종 전반 상승",
                riseRate = "+15.2%",
                tradeInfo = "거래량 340만 · 거래대금 89억",
                theme = "정부정책"
            ),
            WhyRiseItem(
                date = "2024년\n10월 5일",
                riseReason = "글로벌 반도체 공급망 정상화 기대감 확산",
                riseRate = "+6.8%",
                tradeInfo = "거래량 180만 · 거래대금 52억",
                theme = "공급망"
            )
        )

        whyRiseAdapter.updateData(sampleData)
        tvTotalCount.text = "총 ${sampleData.size}회"
    }
}

// API 인터페이스
interface WhyRiseApiService {
    @GET("api/stocks/{stockName}")
    fun getWhyRiseData(@Path("stockName") stockName: String): Call<List<WhyRiseApiResponse>>
}

// API 응답 데이터 클래스
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

// 데이터 클래스
data class WhyRiseItem(
    val date: String,
    val riseReason: String,
    val riseRate: String,
    val tradeInfo: String,
    val theme: String
) 