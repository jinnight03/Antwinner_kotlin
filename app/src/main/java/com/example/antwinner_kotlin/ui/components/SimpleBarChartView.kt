package com.example.antwinner_kotlin.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.model.DailyCount
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SimpleBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dailyCounts = listOf<DailyCount>()
    private var displayCounts = listOf<DailyCount>()
    private var period = "6m" // 기본값은 6개월
    private var maxCount = 0
    
    // 애니메이션을 위한 속성 추가
    private var animationProgress = 0f
    private var isAnimating = false
    private val animationDuration = 500L // 애니메이션 지속 시간 (밀리초)
    private val startTime = 0L
    private var lastFrameTime = 0L
    
    private val barPaint = Paint().apply { 
        color = Color.rgb(33, 33, 33) // 이미지의 차트와 같은 검정색 막대
        isAntiAlias = true
    }
    
    private val gridPaint = Paint().apply {
        color = Color.LTGRAY
        isAntiAlias = true
        alpha = 100
    }
    
    private val textPaint = Paint().apply {
        color = Color.GRAY
        textSize = 35f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    
    // 건수 말풍선용 Paint
    private val bubbleBackgroundPaint = Paint().apply {
        color = Color.rgb(50, 50, 50) // 어두운 회색 배경
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val bubbleTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 28f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    
    private val monthLabelHeight = 60f
    private val horizontalPadding = 40f
    private val verticalPadding = 40f
    private val barCornerRadius = 8f // 막대 모서리 둥글기
    private val bubblePadding = 8f // 말풍선 내부 패딩
    private val bubbleSpacing = 12f // 막대와 말풍선 사이 간격

    fun setData(data: List<DailyCount>, selectedPeriod: String) {
        dailyCounts = data.sortedBy { it.date }
        period = selectedPeriod
        updateDisplayData()
        
        // 애니메이션 시작
        startAnimation()
        
        invalidate()
    }
    
    private fun updateDisplayData() {
        if (dailyCounts.isEmpty()) {
            displayCounts = emptyList()
            maxCount = 0
            return
        }
        
        // 날짜 기준으로 필터링 및 그룹화
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time
        
        displayCounts = when (period) {
            "1w" -> {
                // 1주일은 일별로 표시
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val startDate = calendar.time
                val weekData = groupByDay(dailyCounts, startDate, currentDate)
                weekData
            }
            "1m" -> {
                // 1달은 주차별로 표시 
                calendar.add(Calendar.MONTH, -1)
                val startDate = calendar.time
                val monthData = groupByWeek(dailyCounts, startDate, currentDate)
                monthData
            }
            "6m" -> {
                // 6개월 데이터는 월별로 그룹화
                calendar.add(Calendar.MONTH, -6)
                val startDate = calendar.time
                groupByMonth(dailyCounts, startDate, currentDate, false)
            }
            "1y" -> {
                // 1년은 모든 월을 표시
                calendar.add(Calendar.YEAR, -1)
                val startDate = calendar.time
                groupByMonth(dailyCounts, startDate, currentDate, true)
            }
            "all" -> {
                // 전체 기간은 최대 3년 정도를 표시
                groupByYearMonth(dailyCounts)
            }
            else -> dailyCounts
        }
        
        maxCount = displayCounts.maxOfOrNull { it.count } ?: 0
        // 최대값이 5보다 작으면 5로 설정
        if (maxCount < 5) maxCount = 5
        
        Timber.d("차트 데이터 업데이트: ${displayCounts.size}개 항목, 최대값: $maxCount, 기간: $period")
    }
    
    // 일별 데이터 그룹화 (1주일 기간용)
    private fun groupByDay(data: List<DailyCount>, startDate: Date, endDate: Date): List<DailyCount> {
        val dailyData = mutableMapOf<String, Int>()
        
        // 모든 날짜에 대해 초기화 (7일)
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        while (calendar.time.before(endDate) || isSameDay(calendar.time, endDate)) {
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            dailyData[dateKey] = 0
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        // 실제 데이터 추가
        data.forEach { dailyCount ->
            val dateKey = dailyCount.date
            if (dailyData.containsKey(dateKey)) {
                dailyData[dateKey] = dailyCount.count
            }
        }
        
        // 결과 생성
        return dailyData.map { (dateKey, count) ->
            DailyCount(dateKey, count)
        }.sortedBy { it.date }
    }
    
    // 주별 데이터 그룹화 (1달 기간용)
    private fun groupByWeek(data: List<DailyCount>, startDate: Date, endDate: Date): List<DailyCount> {
        val weeklyData = mutableMapOf<String, Int>()
        
        // 날짜별로 주차 계산하여 그룹화
        data.forEach { dailyCount ->
            val date = parseDate(dailyCount.date)
            if (date != null && (date.after(startDate) || isSameDay(date, startDate)) && 
                (date.before(endDate) || isSameDay(date, endDate))) {
                
                val cal = Calendar.getInstance()
                cal.time = date
                
                // 해당 월의 주차 계산
                val month = cal.get(Calendar.MONTH) + 1
                val week = cal.get(Calendar.WEEK_OF_MONTH)
                // 간결한 형식으로 키 생성 "MM.W"
                val weekKey = String.format("%02d.%d", month, week)
                
                val currentCount = weeklyData[weekKey] ?: 0
                weeklyData[weekKey] = currentCount + dailyCount.count
            }
        }
        
        // 결과 생성 (주차 순으로 정렬)
        return weeklyData.entries.sortedBy { it.key }.map { (weekKey, count) ->
            // date 필드에 주차 정보를 저장하고, 표시할 때 사용
            DailyCount("week:$weekKey", count)
        }
    }
    
    // 월별 데이터 그룹화 (6개월, 1년 기간용)
    private fun groupByMonth(data: List<DailyCount>, startDate: Date, endDate: Date, showAllMonths: Boolean): List<DailyCount> {
        val monthlyData = mutableMapOf<String, Int>()
        
        // 시작일과 종료일 기준으로 월 범위 계산
        val startCal = Calendar.getInstance()
        startCal.time = startDate
        val startYear = startCal.get(Calendar.YEAR)
        val startMonth = startCal.get(Calendar.MONTH)
        
        val endCal = Calendar.getInstance()
        endCal.time = endDate
        val endYear = endCal.get(Calendar.YEAR)
        val endMonth = endCal.get(Calendar.MONTH)
        
        // 모든 월에 대해 초기화
        if (showAllMonths) {
            val cal = Calendar.getInstance()
            cal.set(startYear, startMonth, 1)
            
            while (cal.get(Calendar.YEAR) < endYear || 
                  (cal.get(Calendar.YEAR) == endYear && cal.get(Calendar.MONTH) <= endMonth)) {
                val monthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.time)
                monthlyData[monthKey] = 0
                cal.add(Calendar.MONTH, 1)
            }
        }
        
        // 실제 데이터 추가
        data.forEach { dailyCount ->
            val date = parseDate(dailyCount.date)
            if (date != null && (date.after(startDate) || isSameDay(date, startDate)) && 
                (date.before(endDate) || isSameDay(date, endDate))) {
                
                val monthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(date)
                val currentCount = monthlyData[monthKey] ?: 0
                monthlyData[monthKey] = currentCount + dailyCount.count
            }
        }
        
        // 결과 생성
        return monthlyData.entries.sortedBy { it.key }.map { (monthKey, count) ->
            DailyCount("$monthKey-15", count)
        }
    }
    
    // 년월별 데이터 그룹화 (전체 기간용 - 최대 3년)
    private fun groupByYearMonth(data: List<DailyCount>): List<DailyCount> {
        val yearMonthData = mutableMapOf<String, Int>()
        
        // 전체 데이터 중 날짜 범위 결정
        val dates = data.mapNotNull { parseDate(it.date) }
        if (dates.isEmpty()) return emptyList()
        
        val minDate = dates.minByOrNull { it.time } ?: return emptyList()
        val maxDate = dates.maxByOrNull { it.time } ?: return emptyList()
        
        // 최근 3년만 표시하도록 제한
        val startCal = Calendar.getInstance()
        startCal.time = maxDate
        startCal.add(Calendar.YEAR, -3) // 3년 전
        
        val adjustedMinDate = if (minDate.before(startCal.time)) startCal.time else minDate
        
        // 범위 내 모든 년월 초기화
        val cal = Calendar.getInstance()
        cal.time = adjustedMinDate
        cal.set(Calendar.DAY_OF_MONTH, 1) // 월 시작일
        
        val endCal = Calendar.getInstance()
        endCal.time = maxDate
        
        while (cal.before(endCal) || cal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH) && 
               cal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR)) {
            val yearMonthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.time)
            yearMonthData[yearMonthKey] = 0
            cal.add(Calendar.MONTH, 1)
        }
        
        // 실제 데이터 추가
        data.forEach { dailyCount ->
            val date = parseDate(dailyCount.date)
            if (date != null && !date.before(adjustedMinDate) && !date.after(maxDate)) {
                val yearMonthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(date)
                val currentCount = yearMonthData[yearMonthKey] ?: 0
                yearMonthData[yearMonthKey] = currentCount + dailyCount.count
            }
        }
        
        // 결과 생성
        return yearMonthData.entries.sortedBy { it.key }.map { (yearMonthKey, count) ->
            DailyCount("$yearMonthKey-15", count)
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
    
    private fun parseDate(dateStr: String): Date? {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
        } catch (e: Exception) {
            Timber.e(e, "날짜 파싱 오류: $dateStr")
            null
        }
    }
    
    private fun startAnimation() {
        animationProgress = 0f
        isAnimating = true
        lastFrameTime = System.currentTimeMillis()
        
        post(object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val deltaTime = currentTime - lastFrameTime
                lastFrameTime = currentTime
                
                animationProgress += deltaTime / animationDuration.toFloat()
                if (animationProgress >= 1f) {
                    animationProgress = 1f
                    isAnimating = false
                }
                
                invalidate()
                
                if (isAnimating) {
                    postDelayed(this, 16) // 약 60fps
                }
            }
        })
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (displayCounts.isEmpty()) {
            return
        }
        
        val chartWidth = width.toFloat() - 2 * horizontalPadding
        val chartHeight = height.toFloat() - monthLabelHeight - 2 * verticalPadding
        
        // 그리드 라인 그리기 (5개의 수평선)
        val gridStep = chartHeight / 5
        for (i in 0..5) {
            val y = verticalPadding + chartHeight - i * gridStep
            canvas.drawLine(
                horizontalPadding, 
                y, 
                width.toFloat() - horizontalPadding, 
                y, 
                gridPaint
            )
            
            // 오른쪽에 숫자 표시 (이미지처럼)
            if (i > 0) { // 0은 표시하지 않음
                val value = (maxCount * i / 5).toString()
                canvas.drawText(
                    value,
                    width.toFloat() - horizontalPadding / 2,
                    y - 10, // 텍스트를 선 위에 표시
                    textPaint
                )
            }
        }
        
        val barCount = displayCounts.size
        if (barCount == 0) return
        
        // 이미지와 비슷하게 막대 간격 조정
        val barWidth = chartWidth / (barCount * 3) // 더 얇은 막대
        val sectionWidth = chartWidth / barCount
        
        // 막대 그리기
        displayCounts.forEachIndexed { index, dailyCount ->
            val barHeight = if (dailyCount.count > 0) {
                (dailyCount.count.toFloat() / maxCount) * chartHeight
            } else {
                0f
            }
            
            // 애니메이션 적용
            val animatedBarHeight = barHeight * animationProgress
            
            val centerX = horizontalPadding + (index * sectionWidth) + (sectionWidth / 2)
            val left = centerX - (barWidth / 2)
            val right = centerX + (barWidth / 2)
            val bottom = height.toFloat() - monthLabelHeight - verticalPadding
            val top = if (animatedBarHeight > 0) bottom - animatedBarHeight else bottom
            
            // 둥근 모서리 막대 그리기
            if (animatedBarHeight > 0) {
                val rect = RectF(left, top, right, bottom)
                canvas.drawRoundRect(rect, barCornerRadius, barCornerRadius, barPaint)
                
                // 건수 말풍선 그리기
                if (dailyCount.count > 0) {
                    drawCountBubble(canvas, centerX, top - bubbleSpacing, dailyCount.count)
                }
            }
            
            // x축 레이블 표시 - 기간별로 다르게 표시
            // 전체 기간일 때는 간격을 두고 표시
            val shouldShowLabel = when (period) {
                "all" -> {
                    // 전체 기간에서는 일정 간격으로만 레이블 표시 (3개월마다 또는 데이터 양에 따라 조정)
                    val interval = if (barCount > 24) 6 else if (barCount > 12) 3 else 2
                    index % interval == 0
                }
                else -> true // 다른 기간에서는 모든 레이블 표시
            }
            
            if (shouldShowLabel) {
                val xLabel = when {
                    // 1주일 기간은 일자만 표시 (15일)
                    period == "1w" -> {
                        val date = parseDate(dailyCount.date)
                        if (date != null) {
                            SimpleDateFormat("d일", Locale.KOREAN).format(date)
                        } else ""
                    }
                    // 1달 기간은 MM.W 형식으로 표시 (예: 04.2)
                    period == "1m" && dailyCount.date.startsWith("week:") -> {
                        // "week:04.2"와 같은 형식에서 "04.2" 부분만 추출
                        dailyCount.date.substringAfter("week:")
                    }
                    // 1년, 6개월 기간은 월만 표시 (N월)
                    period == "1y" || period == "6m" -> {
                        val date = parseDate(dailyCount.date)
                        if (date != null) {
                            SimpleDateFormat("M월", Locale.KOREAN).format(date)
                        } else ""
                    }
                    // 전체 기간은 연도-월 표시 (YY.MM)
                    period == "all" -> {
                        val date = parseDate(dailyCount.date)
                        if (date != null) {
                            SimpleDateFormat("yy.MM", Locale.KOREAN).format(date)
                        } else ""
                    }
                    // 기본은 월 표시
                    else -> {
                        val date = parseDate(dailyCount.date)
                        if (date != null) {
                            SimpleDateFormat("M월", Locale.KOREAN).format(date)
                        } else ""
                    }
                }
                
                canvas.drawText(
                    xLabel,
                    centerX,
                    height.toFloat() - verticalPadding / 2,
                    textPaint
                )
            }
        }
        
        // 6달 선택 시 동그란 표시 제거 - 이 부분을 주석 처리하거나 제거
        /*
        if (period == "6m") {
            val selectedPeriodPaint = Paint().apply {
                color = Color.BLACK
                alpha = 50
                style = Paint.Style.FILL
            }
            
            // 선택된 기간을 나타내는 동그라미
            val circleRadius = 50f
            val circleY = height.toFloat() - 25f
            canvas.drawCircle(width.toFloat() / 2, circleY, circleRadius, selectedPeriodPaint)
        }
        */
    }
    
    /**
     * 건수 말풍선 그리기
     */
    private fun drawCountBubble(canvas: Canvas, centerX: Float, y: Float, count: Int) {
        val countText = count.toString()
        
        // 텍스트 크기 측정
        val textBounds = Rect()
        bubbleTextPaint.getTextBounds(countText, 0, countText.length, textBounds)
        val textWidth = textBounds.width().toFloat()
        val textHeight = textBounds.height().toFloat()
        
        // 말풍선 크기 계산
        val bubbleWidth = textWidth + (bubblePadding * 2)
        val bubbleHeight = textHeight + (bubblePadding * 2)
        val bubbleLeft = centerX - (bubbleWidth / 2)
        val bubbleRight = centerX + (bubbleWidth / 2)
        val bubbleTop = y - bubbleHeight
        val bubbleBottom = y
        
        // 말풍선 배경 그리기 (둥근 사각형)
        val bubbleRect = RectF(bubbleLeft, bubbleTop, bubbleRight, bubbleBottom)
        val bubbleRadius = bubbleHeight / 2 // 완전히 둥근 말풍선
        canvas.drawRoundRect(bubbleRect, bubbleRadius, bubbleRadius, bubbleBackgroundPaint)
        
        // 텍스트 그리기 (중앙 정렬)
        val textY = bubbleTop + (bubbleHeight / 2) + (textHeight / 2) - textBounds.bottom
        canvas.drawText(countText, centerX, textY, bubbleTextPaint)
    }
} 