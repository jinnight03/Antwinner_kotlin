package com.example.antwinner_kotlin.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.util.Log
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.repository.TrendRepository
import com.example.antwinner_kotlin.ui.home.model.ThemeFluctuation
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat

class ThemeWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ThemeWidgetRemoteViewsFactory(this.applicationContext, intent)
    }
}

class ThemeWidgetRemoteViewsFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    companion object {
        private const val TAG = "ThemeWidgetFactory"
        private const val MAX_THEMES = 3 // 3개 테마만 표시
    }

    private val appWidgetId = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    )
    private val isCardType = intent.getBooleanExtra("is_card_type", false)
    private val trendRepository = TrendRepository()
    private var themeData: List<ThemeFluctuation> = emptyList()
    private val decimalFormat = DecimalFormat("+#,##0.00%;-#,##0.00%")

    override fun onCreate() {
        Log.d(TAG, "onCreate for widget $appWidgetId")
    }

    override fun onDataSetChanged() {
        Log.d(TAG, "onDataSetChanged for widget $appWidgetId")
        
        // 코루틴을 사용하여 데이터 로드
        try {
            runBlocking {
                // HomeFragment와 동일한 방식으로 데이터 가져오기
                val allThemes = trendRepository.getThemeFluctuations()
                
                // 등락률 절댓값 기준으로 정렬하여 상위 3개만 선택
                themeData = allThemes
                    .sortedByDescending { kotlin.math.abs(it.averageRateValue) }
                    .take(MAX_THEMES)
                
                Log.d(TAG, "Loaded ${themeData.size} themes for widget")
                themeData.forEachIndexed { index, theme ->
                    Log.d(TAG, "Theme[$index]: ${theme.thema} - ${theme.averageRate}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading theme data: ${e.message}", e)
            // 에러 발생 시 더미 데이터 사용
            themeData = getDummyThemeData()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy for widget $appWidgetId")
        themeData = emptyList()
    }

    override fun getCount(): Int {
        return themeData.size
    }

    override fun getViewAt(position: Int): RemoteViews? {
        if (position >= themeData.size) {
            return null
        }

        val theme = themeData[position]
        
        // 디자인 타입에 따른 레이아웃 선택
        val layoutId = if (isCardType) {
            R.layout.widget_theme_item_card
        } else {
            R.layout.widget_theme_item_list
        }
        
        val views = RemoteViews(context.packageName, layoutId)
        
        // 테마 데이터 설정
        views.setTextViewText(R.id.widget_theme_name, theme.thema)
        
        // 등락률 포맷팅 및 색상 설정
        val rateValue = theme.averageRateValue / 100.0 // 퍼센트를 소수로 변환
        val formattedRate = decimalFormat.format(rateValue)
        views.setTextViewText(R.id.widget_theme_rate, formattedRate)
        
        // 등락률에 따른 색상 설정
        val colorResId = if (theme.isRising) {
            R.color.market_up // 빨간색
        } else {
            R.color.market_down // 파란색
        }
        views.setTextColor(R.id.widget_theme_rate, context.getColor(colorResId))
        
        // 카드형인 경우 추가 정보 표시
        if (isCardType) {
            // 상승비율 표시
            views.setTextViewText(
                R.id.widget_theme_rising_ratio, 
                "상승비율: ${theme.risingRatioString}"
            )
            
            // 종목 수 표시
            views.setTextViewText(
                R.id.widget_theme_stock_count,
                "종목수: ${theme.companies.size}개"
            )
        }
        
        // 클릭 이벤트를 위한 FillInIntent 설정
        val fillInIntent = Intent().apply {
            putExtra("theme_name", theme.thema)
            putExtra("theme_id", "")
        }
        views.setOnClickFillInIntent(R.id.widget_theme_container, fillInIntent)
        
        return views
    }

    override fun getLoadingView(): RemoteViews? {
        // 로딩 중 표시할 뷰
        val layoutId = if (isCardType) {
            R.layout.widget_theme_item_card
        } else {
            R.layout.widget_theme_item_list
        }
        
        val views = RemoteViews(context.packageName, layoutId)
        views.setTextViewText(R.id.widget_theme_name, "로딩 중...")
        views.setTextViewText(R.id.widget_theme_rate, "...")
        
        return views
    }

    override fun getViewTypeCount(): Int {
        return 1 // 하나의 뷰 타입만 사용
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    // 더미 데이터 생성 (에러 발생 시 사용)
    private fun getDummyThemeData(): List<ThemeFluctuation> {
        return listOf(
            ThemeFluctuation(
                averageRate = "15.42%",
                thema = "인공지능(AI)",
                risingRatioString = "87.5%",
                companies = emptyList()
            ),
            ThemeFluctuation(
                averageRate = "8.72%",
                thema = "2차전지",
                risingRatioString = "80.0%",
                companies = emptyList()
            ),
            ThemeFluctuation(
                averageRate = "-2.15%",
                thema = "반도체",
                risingRatioString = "45.2%",
                companies = emptyList()
            )
        )
    }
} 