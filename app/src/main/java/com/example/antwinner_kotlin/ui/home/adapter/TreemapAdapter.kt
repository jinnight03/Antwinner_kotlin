package com.example.antwinner_kotlin.ui.home.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.ui.home.layout.TreemapLayout
import com.example.antwinner_kotlin.ui.home.model.Theme
import java.text.DecimalFormat
import kotlin.math.abs

/**
 * 트리맵 레이아웃용 어댑터
 */
class TreemapAdapter(
    private val context: Context,
    private val treemapLayout: TreemapLayout
) {
    
    private var themes: List<Theme> = emptyList()
    private val decimalFormat = DecimalFormat("0.00")
    
    // 클릭 리스너 인터페이스
    interface OnItemClickListener {
        fun onItemClick(theme: Theme)
    }
    
    private var listener: OnItemClickListener? = null
    
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    /**
     * 테마 데이터 업데이트
     */
    fun updateThemes(newThemes: List<Theme>) {
        Log.d("TreemapAdapter", "updateThemes 호출: ${newThemes.size}개 테마")
        
        this.themes = newThemes
        
        // HTML과 동일한 최대값 계산
        val riseThemes = newThemes.filter { it.isRising }
        val fallThemes = newThemes.filter { !it.isRising }
        
        val maxRiseValue = riseThemes.maxOfOrNull { abs(it.rate) } ?: 0.0
        val maxFallValue = fallThemes.maxOfOrNull { abs(it.rate) } ?: 0.0
        
        Log.d("TreemapAdapter", "최대값 - 상승: $maxRiseValue, 하락: $maxFallValue")
        
        // TreemapLayout에 데이터 전달
        treemapLayout.setThemes(newThemes)
        
        // 기존 뷰들 제거
        treemapLayout.removeAllViews()
        
        // 새로운 뷰들 추가 (최적화된 색상 시스템 사용)
        themes.forEach { theme ->
            val view = createThemeView(theme, maxRiseValue, maxFallValue)
            treemapLayout.addView(view)
        }
        
        Log.d("TreemapAdapter", "updateThemes 완료: ${treemapLayout.childCount}개 뷰 추가")
    }
    
    /**
     * 개별 테마 뷰 생성
     */
    private fun createThemeView(theme: Theme): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_theme_treemap, treemapLayout, false)
        
        bindThemeView(view, theme)
        
        return view
    }
    
    /**
     * 개별 테마 뷰 생성 (최적화된 색상 시스템)
     */
    private fun createThemeView(theme: Theme, maxRiseValue: Double, maxFallValue: Double): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_theme_treemap, treemapLayout, false)
        
        bindThemeView(view, theme, 100, 80, maxRiseValue, maxFallValue)
        
        return view
    }
    
    /**
     * 테마 뷰에 데이터 바인딩 (박스 크기 포함)
     */
    private fun bindThemeView(view: View, theme: Theme, width: Int = 100, height: Int = 80) {
        val themeCard = view.findViewById<CardView>(R.id.theme_card)
        val themeBackground = view.findViewById<LinearLayout>(R.id.theme_background)
        val themeName = view.findViewById<TextView>(R.id.tv_theme_name)
        val themePercent = view.findViewById<TextView>(R.id.tv_theme_percent)
        
        try {
            Log.d("TreemapAdapter", "바인딩: ${theme.name}, 등락률: ${theme.rate}%, 크기: ${width}x${height}")
            
            // 테마명 설정
            themeName.text = theme.name
            
            // 퍼센트 설정
            val percentText = "${if (theme.rate > 0) "+" else ""}${decimalFormat.format(theme.rate)}%"
            themePercent.text = percentText
            
            // 박스 크기에 따른 동적 텍스트 크기 조절 (등락률 기반으로 추정)
            val estimatedWidth = when {
                kotlin.math.abs(theme.rate) >= 10.0 -> 150
                kotlin.math.abs(theme.rate) >= 5.0 -> 120
                kotlin.math.abs(theme.rate) >= 2.0 -> 100
                else -> 80
            }
            val estimatedHeight = (estimatedWidth * 0.7).toInt()
            adjustTextSize(themeName, themePercent, theme, estimatedWidth, estimatedHeight)
            
            // 상승/하락에 따른 배경색 설정
            val backgroundColor = getBackgroundColor(theme)
            themeBackground.setBackgroundColor(backgroundColor)
            
            // 클릭 리스너 설정
            view.setOnClickListener {
                listener?.onItemClick(theme)
            }
            
            Log.d("TreemapAdapter", "바인딩 완료: ${theme.name}")
            
        } catch (e: Exception) {
            Log.e("TreemapAdapter", "바인딩 오류: ${theme.name} - ${e.message}")
        }
    }
    
    /**
     * 테마 뷰에 데이터 바인딩 (최적화된 색상 시스템)
     */
    private fun bindThemeView(
        view: View, 
        theme: Theme, 
        width: Int, 
        height: Int, 
        maxRiseValue: Double, 
        maxFallValue: Double
    ) {
        val themeCard = view.findViewById<CardView>(R.id.theme_card)
        val themeBackground = view.findViewById<LinearLayout>(R.id.theme_background)
        val themeName = view.findViewById<TextView>(R.id.tv_theme_name)
        val themePercent = view.findViewById<TextView>(R.id.tv_theme_percent)
        
        try {
            Log.d("TreemapAdapter", "바인딩: ${theme.name}, 등락률: ${theme.rate}%, 크기: ${width}x${height}")
            
            // 테마명 설정
            themeName.text = theme.name
            
            // 퍼센트 설정 (HTML과 동일한 형식)
            val percentText = "${if (theme.rate > 0) "+" else ""}${decimalFormat.format(theme.rate)}%"
            themePercent.text = percentText
            
            // 임시로 기본 텍스트 피팅 사용 (디버깅용)
            adjustTextSize(themeName, themePercent, theme, width, height)
            
            // 최적화된 배경색 설정 (HTML과 동일한 알파 기반 시스템)
            val backgroundColor = getOptimizedBackgroundColor(theme, maxRiseValue, maxFallValue)
            themeBackground.setBackgroundColor(backgroundColor)
            
            // 클릭 리스너 설정
            view.setOnClickListener {
                listener?.onItemClick(theme)
            }
            
            Log.d("TreemapAdapter", "바인딩 완료: ${theme.name}")
            
        } catch (e: Exception) {
            Log.e("TreemapAdapter", "바인딩 오류: ${theme.name} - ${e.message}")
        }
    }
    
    /**
     * 뷰를 다시 바인딩 (TreemapLayout에서 호출)
     */
    fun rebindView(view: View, theme: Theme, width: Int, height: Int) {
        // 전체 테마 데이터에서 최대값 계산
        val riseThemes = themes.filter { it.isRising }
        val fallThemes = themes.filter { !it.isRising }
        
        val maxRiseValue = riseThemes.maxOfOrNull { abs(it.rate) } ?: 0.0
        val maxFallValue = fallThemes.maxOfOrNull { abs(it.rate) } ?: 0.0
        
        bindThemeView(view, theme, width, height, maxRiseValue, maxFallValue)
    }
    
    /**
     * 등락률에 따른 배경색 결정 (HTML 버전과 동일한 알파 기반 시스템)
     */
    private fun getBackgroundColor(theme: Theme): Int {
        val absValue = abs(theme.rate)
        
        // HTML과 동일한 색상 값 및 로직
        val riseColor = Triple(255, 0, 0) // RGB(255, 0, 0) - 빨간색
        val fallColor = Triple(33, 150, 243) // RGB(33, 150, 243) - 모던한 파란색 (Material Design Blue)
        
        // HTML과 동일한 최대값 설정 (5 미만이면 5를 최대값으로 사용)
        val adjustedMaxValue = if (absValue < 5) 5.0 else {
            // 전체 테마에서 최대 절댓값을 구해야 하지만, 개별 계산에서는 현재 값 기준으로 처리
            maxOf(absValue, 5.0)
        }
        
        // 정규화된 값 계산 (0 ~ adjustedMaxValue 범위를 0 ~ 1로 변환)
        val normalizedValue = minOf(maxOf(absValue, 0.0), adjustedMaxValue)
        
        // 알파값 계산: 0.1 + (정규화된값 / 최대값) * 0.75
        val alpha = 0.1 + (normalizedValue / adjustedMaxValue) * 0.75
        
        return if (theme.isRising) {
            Color.argb(
                (alpha * 255).toInt(),
                riseColor.first,
                riseColor.second,
                riseColor.third
            )
        } else {
            Color.argb(
                (alpha * 255).toInt(),
                fallColor.first,
                fallColor.second,
                fallColor.third
            )
        }
    }
    
    /**
     * 전체 테마 리스트를 기반으로 최적화된 배경색 결정
     * (전체 데이터의 최대값을 고려한 정확한 알파 계산)
     */
    private fun getOptimizedBackgroundColor(theme: Theme, maxRiseValue: Double, maxFallValue: Double): Int {
        val absValue = abs(theme.rate)
        
        // HTML과 동일한 색상 값
        val riseColor = Triple(255, 0, 0) // RGB(255, 0, 0)
        val fallColor = Triple(0, 0, 255) // RGB(0, 0, 255)
        
        // HTML과 동일한 최대값 보정 로직
        val adjustedMaxRise = if (maxRiseValue < 5) 5.0 else maxRiseValue
        val adjustedMaxFall = if (maxFallValue < 5) 5.0 else maxFallValue
        
        val alpha = if (theme.isRising) {
            val normalizedValue = minOf(maxOf(absValue, 0.0), adjustedMaxRise)
            0.1 + (normalizedValue / adjustedMaxRise) * 0.75
        } else {
            val normalizedValue = minOf(maxOf(absValue, 0.0), adjustedMaxFall)
            0.1 + (normalizedValue / adjustedMaxFall) * 0.75
        }
        
        return if (theme.isRising) {
            Color.argb(
                (alpha * 255).toInt(),
                riseColor.first,
                riseColor.second,
                riseColor.third
            )
        } else {
            Color.argb(
                (alpha * 255).toInt(),
                fallColor.first,
                fallColor.second,
                fallColor.third
            )
        }
    }
    
    /**
     * 박스 크기에 따른 동적 텍스트 크기 조절 (더 세밀한 조정)
     */
    private fun adjustTextSize(themeName: TextView, themePercent: TextView, theme: Theme, width: Int, height: Int) {
        val area = width * height
        val minDimension = kotlin.math.min(width, height)
        
        // 박스 크기에 따른 동적 텍스트 크기 계산
        when {
            area >= 15000 || minDimension >= 120 -> {
                // 매우 큰 박스: 큰 텍스트
                themeName.textSize = 18f
                themePercent.textSize = 22f
            }
            area >= 10000 || minDimension >= 100 -> {
                // 큰 박스: 중큰 텍스트
                themeName.textSize = 16f
                themePercent.textSize = 20f
            }
            area >= 7000 || minDimension >= 80 -> {
                // 중간 박스: 중간 텍스트
                themeName.textSize = 14f
                themePercent.textSize = 18f
            }
            area >= 5000 || minDimension >= 70 -> {
                // 중소형 박스: 중소 텍스트
                themeName.textSize = 13f
                themePercent.textSize = 16f
            }
            area >= 3500 || minDimension >= 60 -> {
                // 소형 박스: 소형 텍스트
                themeName.textSize = 12f
                themePercent.textSize = 15f
            }
            area >= 2500 || minDimension >= 50 -> {
                // 아주 작은 박스: 작은 텍스트
                themeName.textSize = 11f
                themePercent.textSize = 14f
            }
            else -> {
                // 극소형 박스: 극소 텍스트
                themeName.textSize = 10f
                themePercent.textSize = 12f
            }
        }
        
        Log.d("TreemapAdapter", "${theme.name}: 박스 ${width}x${height} (면적=$area) → 텍스트 크기 ${themeName.textSize}sp/${themePercent.textSize}sp")
    }
    
    /**
     * HTML과 동일한 세밀한 텍스트 피팅 로직
     */
    private fun adjustTextSizeAdvanced(themeName: TextView, themePercent: TextView, theme: Theme, width: Int, height: Int) {
        // HTML과 동일한 초기 폰트 크기 (dp를 sp로 변환)
        var nameFontSize = 50f // HTML: 50px
        var valueFontSize = 40f // HTML: 40px
        
        val density = context.resources.displayMetrics.density
        nameFontSize = (nameFontSize / density) // px to sp 변환
        valueFontSize = (valueFontSize / density) // px to sp 변환
        
        // 사용 가능한 영역 (20dp 패딩 제외)
        val paddingPx = (20 * density).toInt()
        val rectWidth = width - paddingPx
        val rectHeight = height - paddingPx
        
        val name = theme.name
        val value = "${if (theme.rate > 0) "+" else ""}${decimalFormat.format(theme.rate)}%"
        
        // 초기 설정
        themeName.textSize = nameFontSize
        themePercent.textSize = valueFontSize
        themePercent.visibility = View.VISIBLE
        
        // HTML과 동일한 조정 로직
        adjustFontSizeAndSpacing(themeName, themePercent, name, value, rectWidth, rectHeight)
        
        Log.d("TreemapAdapter", "${theme.name}: 박스 ${width}x${height} → 최종 텍스트 크기 ${themeName.textSize}sp/${themePercent.textSize}sp")
    }
    
    /**
     * HTML의 adjustFontSizeAndSpacing 로직을 Kotlin으로 구현
     */
    private fun adjustFontSizeAndSpacing(
        nameView: TextView, 
        valueView: TextView, 
        name: String, 
        value: String, 
        rectWidth: Int, 
        rectHeight: Int
    ) {
        var nameFontSize = nameView.textSize
        var valueFontSize = valueView.textSize
        
        // 1️⃣ 폰트 크기 줄여서 시도
        fun updateAndMeasure(): Pair<Int, Int> {
            nameView.textSize = nameFontSize / context.resources.displayMetrics.scaledDensity
            valueView.textSize = valueFontSize / context.resources.displayMetrics.scaledDensity
            
            nameView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            valueView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            
            val totalWidth = maxOf(nameView.measuredWidth, valueView.measuredWidth)
            val totalHeight = nameView.measuredHeight + valueView.measuredHeight
            
            return Pair(totalWidth, totalHeight)
        }
        
        // 폰트 크기 조정 시도
        var attempts = 0
        while (attempts < 10) { // 무한 루프 방지
            val (textWidth, textHeight) = updateAndMeasure()
            
            if (textWidth <= rectWidth && textHeight <= rectHeight) break
            
            if ((textWidth > rectWidth || textHeight > rectHeight) && nameFontSize > 36) {
                nameFontSize -= 2
                valueFontSize -= 1
                attempts++
            } else {
                break
            }
        }
        
        // 2️⃣ 높이 초과 시 숫자 숨김
        val (finalWidth, finalHeight) = updateAndMeasure()
        if (finalHeight > rectHeight) {
            valueView.visibility = View.GONE
            nameView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            
            // 숫자 숨기고도 높이가 안 들어가는 경우 말줄임표 처리
            if (nameView.measuredHeight > rectHeight) {
                nameView.text = "..."
                return
            }
        }
        
        // 3️⃣ 너비 기준 말줄임 처리
        nameView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        if (nameView.measuredWidth > rectWidth) {
            truncateText(nameView, name, rectWidth)
        }
        
        if (valueView.visibility == View.VISIBLE) {
            valueView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            if (valueView.measuredWidth > rectWidth) {
                truncateText(valueView, value, rectWidth)
            }
        }
    }
    
    /**
     * 텍스트 말줄임 처리 (HTML과 동일한 로직)
     */
    private fun truncateText(textView: TextView, originalText: String, maxWidth: Int) {
        var truncated = originalText
        textView.text = truncated
        textView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        
        while (textView.measuredWidth > maxWidth && truncated.isNotEmpty()) {
            truncated = truncated.dropLast(1)
            textView.text = "$truncated..."
            textView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        }
    }
}
