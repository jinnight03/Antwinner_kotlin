package com.example.antwinner_kotlin.ui.themeschedule

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.data.model.Schedule
import com.example.antwinner_kotlin.data.network.RetrofitClient as DataRetrofitClient
import com.example.antwinner_kotlin.network.RetrofitClient as NetworkRetrofitClient
import kotlinx.coroutines.launch
import com.example.antwinner_kotlin.ui.themeschedule.SubscriptionScheduleItem

// 탭 상태를 나타내는 Enum
enum class ScheduleType { ISSUE, SUBSCRIPTION }

class ThemeScheduleViewModel : ViewModel() {

    private val _scheduleItems = MutableLiveData<List<ThemeScheduleItem>>()
    val scheduleItems: LiveData<List<ThemeScheduleItem>> = _scheduleItems

    private val _subscriptionItems = MutableLiveData<List<SubscriptionScheduleItem>>()
    val subscriptionItems: LiveData<List<SubscriptionScheduleItem>> = _subscriptionItems

    // 로딩 상태를 관리하는 LiveData (옵션)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>() // 오류 메시지 LiveData
    val error: LiveData<String?> = _error

    // 현재 선택된 탭 상태
    private val _currentTab = MutableLiveData<ScheduleType>(ScheduleType.ISSUE) // 기본값 이슈일정

    init {
        loadSchedulesBasedOnTab()
    }

    fun setCurrentTab(type: ScheduleType) {
        if (_currentTab.value != type) {
            _currentTab.value = type
            loadSchedulesBasedOnTab()
        }
    }

    private fun loadSchedulesBasedOnTab() {
        when (_currentTab.value) {
            ScheduleType.ISSUE -> loadIssueSchedules()
            ScheduleType.SUBSCRIPTION -> loadSubscriptionSchedules()
            else -> { /* 기본값 또는 오류 처리 */ }
        }
    }

    private fun loadIssueSchedules() {
        _isLoading.value = true
        _error.value = null // 오류 메시지 초기화
        viewModelScope.launch {
            try {
                val response = DataRetrofitClient.instance.getSchedules()
                if (response.isSuccessful) {
                    val schedules = response.body()?.schedules ?: emptyList()
                    _scheduleItems.value = mapSchedulesToItems(schedules)
                } else {
                    Log.e("ThemeScheduleVM", "API Error: ${response.code()} ${response.message()}")
                    _error.value = "데이터 로딩에 실패했습니다. (코드: ${response.code()})"
                    _scheduleItems.value = emptyList() // 오류 시 빈 리스트
                }
            } catch (e: Exception) {
                Log.e("ThemeScheduleVM", "API Exception: ${e.message}", e)
                _error.value = "네트워크 오류 또는 데이터 처리 중 문제가 발생했습니다."
                _scheduleItems.value = emptyList() // 오류 시 빈 리스트
            }
            _isLoading.value = false
        }
    }

    // 청약 일정 로드 로직 (더미 데이터 사용)
    private fun loadSubscriptionSchedules() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val response = NetworkRetrofitClient.apiService.getIpos()
                if (response.isSuccessful) {
                    val ipos = response.body()?.ipos ?: emptyList()
                    _subscriptionItems.value = ipos.mapIndexed { index, ipo ->
                        SubscriptionScheduleItem(
                            id = index,
                            date = ipo.publicDate,
                            title = ipo.company,
                            subscriptionPrice = ipo.price,
                            expectedPrice = ipo.band,
                            competitionRate = ipo.competitionRate,
                            underwriter = ipo.underwriter,
                            showRedDot = ipo.price == "-",
                            companyName = ipo.company // 회사명을 API 호출용으로 사용
                        )
                    }
                } else {
                    Log.e("ThemeScheduleVM", "IPO API Error: ${response.code()} ${response.message()}")
                    _error.value = "청약 일정 로딩에 실패했습니다. (코드: ${response.code()})"
                    // 오류 시 샘플 데이터 제공
                    _subscriptionItems.value = createSampleSubscriptionData()
                }
            } catch (e: Exception) {
                Log.e("ThemeScheduleVM", "IPO API Exception: ${e.message}", e)
                _error.value = "청약 일정 네트워크 오류가 발생했습니다."
                // 오류 시 샘플 데이터 제공
                _subscriptionItems.value = createSampleSubscriptionData()
            }
            _isLoading.value = false
        }
    }

    // 샘플 청약일정 데이터 생성
    private fun createSampleSubscriptionData(): List<SubscriptionScheduleItem> {
        return listOf(
            SubscriptionScheduleItem(
                id = 1,
                date = "2024. 6. 18 ~ 6. 19",
                title = "파인원",
                subscriptionPrice = "-",
                expectedPrice = "3,600 ~ 4,000 원",
                competitionRate = "-",
                underwriter = "미래에셋증권",
                showRedDot = true,
                companyName = "파인원"
            ),
            SubscriptionScheduleItem(
                id = 2,
                date = "2024. 6. 20 ~ 6. 21",
                title = "에이치이엠파마",
                subscriptionPrice = "4,500원",
                expectedPrice = "4,500~5,500원",
                competitionRate = "12.5:1",
                underwriter = "한국투자증권",
                showRedDot = false,
                companyName = "에이치이엠파마"
            ),
            SubscriptionScheduleItem(
                id = 3,
                date = "2024. 6. 22 ~ 6. 23",
                title = "테크노스마트",
                subscriptionPrice = "-",
                expectedPrice = "2,800 ~ 3,200 원",
                competitionRate = "-",
                underwriter = "삼성증권",
                showRedDot = true,
                companyName = "테크노스마트"
            )
        )
    }

    private fun mapSchedulesToItems(schedules: List<Schedule>): List<ThemeScheduleItem> {
        return schedules.mapNotNull { schedule ->
            val categoryMatch = Regex("""^\[(.*?)\]\s*""").find(schedule.content)
            val category = categoryMatch?.groups?.get(1)?.value ?: "기타"
            val title = categoryMatch?.let { schedule.content.substring(it.range.last + 1).trim() } ?: schedule.content

            // categoryIcon 로직 복원
            val categoryIcon = getIconForCategory(category)

            // 투자신호에 따른 색상 매핑
            val opinionColorValue = when (schedule.investmentSignal) {
                "대기" -> R.color.text_secondary
                "긍정" -> R.color.theme_detail_positive_color
                "강한긍정" -> R.color.theme_detail_positive
                "부정" -> R.color.theme_detail_negative_color
                "강한부정" -> R.color.theme_detail_negative
                else -> R.color.text_secondary
            }

            ThemeScheduleItem(
                id = schedule.id,
                category = category,
                categoryIcon = categoryIcon, // 복원된 categoryIcon 사용
                date = schedule.date.replace("-중", ""),
                title = title,
                relatedStocks = schedule.stockName,
                link = schedule.link,
                impact = schedule.issueImpact,
                opinion = schedule.investmentSignal ?: "-",
                opinionColor = opinionColorValue
            )
        }
    }

    // getIconForCategory 함수 복원
    private fun getIconForCategory(category: String): Int? {
        return when (category) {
            "우크라이나 재건" -> R.drawable.ic_launcher_foreground
            "희토류" -> R.drawable.ic_launcher_foreground
            "로봇" -> R.drawable.ic_launcher_foreground
            "비트코인" -> R.drawable.ic_launcher_foreground
            "남북경협" -> R.drawable.ic_launcher_foreground
            else -> null
        }
    }
} 