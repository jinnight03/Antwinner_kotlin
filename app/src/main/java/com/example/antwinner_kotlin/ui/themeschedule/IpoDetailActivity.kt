package com.example.antwinner_kotlin.ui.themeschedule

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.databinding.ActivityIpoDetailBinding
import com.example.antwinner_kotlin.network.RetrofitClient
import com.example.antwinner_kotlin.utils.SystemBarUtils
import kotlinx.coroutines.launch

class IpoDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIpoDetailBinding
    private lateinit var companyName: String

    companion object {
        const val EXTRA_COMPANY_NAME = "company_name"
        private const val TAG = "IpoDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIpoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent에서 회사명 가져오기
        companyName = intent.getStringExtra(EXTRA_COMPANY_NAME) ?: ""
        
        if (companyName.isEmpty()) {
            Toast.makeText(this, "회사 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 시스템 바 인셋 적용
        applySystemBarInsets()

        setupToolbar()
        loadIpoDetail()
    }

    private fun applySystemBarInsets() {
        SystemBarUtils.applyTopPaddingForAppBar(binding.toolbar, 16)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = companyName
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadIpoDetail() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                Log.d(TAG, "IPO 상세 정보 요청: $companyName")
                val ipoDetail = RetrofitClient.apiService.getIpoDetailedInfo(companyName)
                
                Log.d(TAG, "IPO 상세 정보 응답 수신: ${ipoDetail.companyName}")
                
                // UI 업데이트
                bindData(ipoDetail)
                showLoading(false)
                
            } catch (e: Exception) {
                Log.e(TAG, "IPO 상세 정보 로드 실패: ${e.message}", e)
                showLoading(false)
                Toast.makeText(
                    this@IpoDetailActivity,
                    "상세 정보를 불러오는데 실패했습니다: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                
                // 샘플 데이터로 대체
                bindSampleData()
            }
        }
    }

    private fun bindData(ipoDetail: IpoDetailInfo) {
        binding.apply {
            // 헤더 정보
            tvCompanyName.text = ipoDetail.companyName ?: companyName
            tvBusinessType.text = ipoDetail.businessType ?: ""

            // 기업 정보
            tvRepresentative.text = ipoDetail.representative ?: "-"
            tvSales.text = ipoDetail.sales ?: "-"
            tvHomepage.text = ipoDetail.homepage ?: "-"
            tvAddress.text = ipoDetail.address ?: "-"

            // 공모 정보
            tvPriceBand.text = ipoDetail.priceBand ?: "-"
            tvOfferingAmount.text = ipoDetail.offeringAmount ?: "-"
            tvUnderwriter.text = ipoDetail.underwriter ?: "-"
            tvTotalShares.text = ipoDetail.totalShares ?: "-"

            // 기업 기본정보
            tvBusinessTypeDetail.text = ipoDetail.businessType ?: "-"

            // 주요 재무지표 (2024년) - 프로그레스 바 포함
            setupFinancialMetrics(ipoDetail)

            // 밸류에이션 지표 - 프로그레스 바 포함
            setupValuationMetrics(ipoDetail)

            // 홈페이지 클릭 이벤트
            tvHomepage.setOnClickListener {
                ipoDetail.homepage?.let { homepage ->
                    openWebsite(homepage)
                }
            }
        }
    }

    private fun setupFinancialMetrics(ipoDetail: IpoDetailInfo) {
        binding.apply {
            // ROE 설정
            val roeValue = ipoDetail.roe2024?.replace("%", "")?.trim()?.toFloatOrNull() ?: 11.45f
            tvRoe2024.text = ipoDetail.roe2024 ?: "11.45 %"
            progressRoe.progress = (roeValue * 5).coerceIn(0f, 100f).toInt() // ROE를 0-100 스케일로 변환

            // 영업이익률 설정
            val marginValue = ipoDetail.operatingMargin2024?.replace("%", "")?.trim()?.toFloatOrNull() ?: 4.35f
            tvOperatingMargin2024.text = ipoDetail.operatingMargin2024 ?: "4.35 %"
            progressOperatingMargin.progress = (marginValue * 5).coerceIn(0f, 100f).toInt()

            // 매출성장률 설정
            val growthValue = ipoDetail.revenueGrowth2024?.replace("%", "")?.trim()?.toFloatOrNull() ?: 41.91f
            tvRevenueGrowth2024.text = ipoDetail.revenueGrowth2024 ?: "41.91 %"
            progressRevenueGrowth.progress = (growthValue * 2).coerceIn(0f, 100f).toInt() // 성장률은 더 높을 수 있으므로 스케일 조정
        }
    }

    private fun setupValuationMetrics(ipoDetail: IpoDetailInfo) {
        binding.apply {
            // PER 설정 (낮을수록 좋음 - 역산)
            val perValue = ipoDetail.per2024?.toFloatOrNull() ?: 8.65f
            tvPer2024.text = ipoDetail.per2024 ?: "8.65"
            progressPer.progress = (100 - (perValue * 4)).coerceIn(0f, 100f).toInt() // PER이 낮을수록 높은 점수

            // PBR 설정 (낮을수록 좋음 - 역산)
            val pbrValue = ipoDetail.pbr2024?.toFloatOrNull() ?: 0.99f
            tvPbr2024.text = ipoDetail.pbr2024 ?: "0.99"
            progressPbr.progress = (100 - (pbrValue * 30)).coerceIn(0f, 100f).toInt() // PBR이 낮을수록 높은 점수

            // EPS 설정
            val epsValue = ipoDetail.eps2024?.replace("원", "")?.replace(",", "")?.trim()?.toFloatOrNull() ?: 462f
            tvEps2024.text = ipoDetail.eps2024 ?: "462 원"
            progressEps.progress = (epsValue / 10).coerceIn(0f, 100f).toInt() // EPS를 0-100 스케일로 변환
        }
    }

    private fun bindSampleData() {
        // 파인원 샘플 데이터로 UI 바인딩
        binding.apply {
            tvCompanyName.text = "파인원"
            tvBusinessType.text = "유기발광 표시장치 제조업"
            tvRepresentative.text = "고재생"
            tvSales.text = "67,242 (백만원)"
            tvHomepage.text = "fineone.kr"
            tvAddress.text = "서울 송파구 문정동 642번지 12층 1207, 1208호"
            tvPriceBand.text = "3,600 ~ 4,000 원"
            tvOfferingAmount.text = "신주모집 : 3,600,000 주 (100%)"
            tvUnderwriter.text = "미래에셋증권"
            tvTotalShares.text = "3,600,000 주"
            tvBusinessTypeDetail.text = "유기발광 표시장치 제조업"
            
            // 재무지표 - 프로그레스 바 포함
            tvRoe2024.text = "11.45 %"
            progressRoe.progress = 57
            
            tvOperatingMargin2024.text = "4.35 %"
            progressOperatingMargin.progress = 22
            
            tvRevenueGrowth2024.text = "41.91 %"
            progressRevenueGrowth.progress = 84
            
            // 밸류에이션 지표 - 프로그레스 바 포함
            tvPer2024.text = "8.65"
            progressPer.progress = 90
            
            tvPbr2024.text = "0.99"
            progressPbr.progress = 95
            
            tvEps2024.text = "462 원"
            progressEps.progress = 70

            tvHomepage.setOnClickListener {
                openWebsite("https://fineone.kr")
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.scrollView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun openWebsite(url: String) {
        try {
            val websiteUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else {
                url
            }
            
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "웹사이트 열기 실패: ${e.message}", e)
            Toast.makeText(this, "웹사이트를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
} 