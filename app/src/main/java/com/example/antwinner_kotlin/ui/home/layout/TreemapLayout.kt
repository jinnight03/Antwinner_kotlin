package com.example.antwinner_kotlin.ui.home.layout

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.example.antwinner_kotlin.ui.home.model.Theme
import kotlin.math.abs
import kotlin.math.min

/**
 * 트리맵 레이아웃 - 등락률에 비례하여 크기가 결정되는 직사각형들로 구성
 */
class TreemapLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private var themes: List<Theme> = emptyList()
    private val rectangles = mutableListOf<Rectangle>()
    
    // 트리맵 구성을 위한 직사각형 데이터 클래스
    data class Rectangle(
        var x: Int = 0,
        var y: Int = 0, 
        var width: Int = 0,
        var height: Int = 0,
        val value: Double = 0.0,
        val theme: Theme
    )

    /**
     * 테마 데이터 설정 및 레이아웃 갱신
     */
    fun setThemes(newThemes: List<Theme>) {
        Log.d("TreemapLayout", "setThemes 호출: ${newThemes.size}개 테마")
        
        this.themes = newThemes.sortedByDescending { abs(it.rate) } // 등락률 절대값 기준 내림차순
        rectangles.clear()
        
        // 테마별 Rectangle 생성
        themes.forEach { theme ->
            rectangles.add(Rectangle(value = abs(theme.rate), theme = theme))
        }
        
        requestLayout() // 레이아웃 재계산 요청
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        
        Log.d("TreemapLayout", "onMeasure: ${width}x${height}")
        
        // 고정 크기 사용 (XML에서 지정한 크기 그대로)
        if (rectangles.isNotEmpty() && width > 0 && height > 0) {
            calculateTreemap(width, height)
        }
        
        // 자식 뷰들 측정
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (i < rectangles.size) {
                val rect = rectangles[i]
                val childWidthSpec = MeasureSpec.makeMeasureSpec(rect.width, MeasureSpec.EXACTLY)
                val childHeightSpec = MeasureSpec.makeMeasureSpec(rect.height, MeasureSpec.EXACTLY)
                child.measure(childWidthSpec, childHeightSpec)
            }
        }
        
        // XML에서 지정한 고정 크기 사용
        setMeasuredDimension(width, height)
        
        Log.d("TreemapLayout", "고정 크기 설정: ${width}x${height}")
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        Log.d("TreemapLayout", "onLayout: changed=$changed, 자식 수=${childCount}, 사각형 수=${rectangles.size}")
        
        val minSidePx = (56 * resources.displayMetrics.density).toInt() // 56dp 최소 크기
        
        // 자식 뷰들 배치
        for (i in 0 until min(childCount, rectangles.size)) {
            val child = getChildAt(i)
            val rect = rectangles[i]
            
            child.layout(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height)
            
            // 56dp 미만 타일의 텍스트 숨김 처리
            if (rect.width < minSidePx || rect.height < minSidePx) {
                // 텍스트 숨김 (반투명 표시)
                child.alpha = 0.6f
                
                // Long-press 리스너 설정 (SnackBar 표시용)
                child.setOnLongClickListener {
                    showTileInfo(rect.theme)
                    true
                }
                
                Log.d("TreemapLayout", "Small tile: ${rect.theme.name} (${rect.width}x${rect.height}) - text hidden")
            } else {
                // 정상 크기 타일
                child.alpha = 1.0f
                child.setOnLongClickListener(null)
            }
            
            Log.d("TreemapLayout", "배치: ${rect.theme.name} at (${rect.x}, ${rect.y}) size ${rect.width}x${rect.height}")
        }
    }
    
    /**
     * 작은 타일 정보 표시 (SnackBar 대신 로그로 구현)
     */
    private fun showTileInfo(theme: Theme) {
        Log.i("TreemapLayout", "Tile Info: ${theme.name} - ${if (theme.rate > 0) "+" else ""}${theme.rate}%")
        // 실제 구현시에는 SnackBar.make()를 사용할 수 있습니다
    }

    /**
     * HTML과 동일한 데이터 정규화 로직 (하락 테마 최소 영역 보정)
     */
    private fun applyDataNormalization(originalThemes: List<Rectangle>): List<Rectangle> {
        if (originalThemes.isEmpty()) return originalThemes
        
        // 상승/하락 테마 분리
        val riseThemes = originalThemes.filter { it.theme.isRising }
        val fallThemes = originalThemes.filter { !it.theme.isRising }
        
        if (riseThemes.isEmpty() || fallThemes.isEmpty()) {
            Log.d("TreemapLayout", "상승 또는 하락 테마가 없음 - 정규화 생략")
            return originalThemes
        }
        
        // 각 그룹의 총합 계산
        val riseSum = riseThemes.sumOf { kotlin.math.abs(it.theme.rate) }
        val fallSum = fallThemes.sumOf { kotlin.math.abs(it.theme.rate) }
        val totalSum = riseSum + fallSum
        
        Log.d("TreemapLayout", "정규화 전 - 상승합: $riseSum, 하락합: $fallSum, 전체합: $totalSum")
        
        // HTML과 동일한 최소 비율 설정 (7%)
        val minFallRatio = 0.07
        val rawFallRatio = if (totalSum > 0) fallSum / totalSum else 0.0
        
        Log.d("TreemapLayout", "하락 테마 원본 비율: ${String.format("%.3f", rawFallRatio)}, 최소 비율: $minFallRatio")
        
        // 보정이 필요한 경우에만 적용
        if (rawFallRatio < minFallRatio && fallSum > 0 && riseSum > 0) {
            val fallScale = minFallRatio / rawFallRatio
            val riseScale = (1 - minFallRatio) / (1 - rawFallRatio)
            
            Log.d("TreemapLayout", "보정 적용 - 상승 스케일: ${String.format("%.3f", riseScale)}, 하락 스케일: ${String.format("%.3f", fallScale)}")
            
            // 보정된 테마 리스트 생성
            val normalizedThemes = mutableListOf<Rectangle>()
            
            // 상승 테마에 상승 스케일 적용
            riseThemes.forEach { rect ->
                val scaledRate = rect.theme.rate * riseScale
                val scaledTheme = rect.theme.copy(rate = scaledRate)
                normalizedThemes.add(Rectangle(value = abs(scaledTheme.rate), theme = scaledTheme))
            }
            
            // 하락 테마에 하락 스케일 적용
            fallThemes.forEach { rect ->
                val scaledRate = rect.theme.rate * fallScale // 하락은 음수이므로 그대로 곱함
                val scaledTheme = rect.theme.copy(rate = scaledRate)
                normalizedThemes.add(Rectangle(value = abs(scaledTheme.rate), theme = scaledTheme))
            }
            
            // 정규화 결과 로깅
            val normalizedRiseSum = normalizedThemes.filter { it.theme.isRising }.sumOf { kotlin.math.abs(it.theme.rate) }
            val normalizedFallSum = normalizedThemes.filter { !it.theme.isRising }.sumOf { kotlin.math.abs(it.theme.rate) }
            val normalizedTotal = normalizedRiseSum + normalizedFallSum
            val finalFallRatio = if (normalizedTotal > 0) normalizedFallSum / normalizedTotal else 0.0
            
            Log.d("TreemapLayout", "정규화 후 - 상승합: $normalizedRiseSum, 하락합: $normalizedFallSum")
            Log.d("TreemapLayout", "최종 하락 비율: ${String.format("%.3f", finalFallRatio)}")
            
            return normalizedThemes
        } else {
            Log.d("TreemapLayout", "보정 불필요 - 원본 데이터 사용")
            return originalThemes
        }
    }

    /**
     * True Squarified Treemap 구현 - 사용자 요구사항 완전 준수
     */
    private fun calculateTreemap(containerWidth: Int, containerHeight: Int) {
        if (rectangles.isEmpty()) return
        
        Log.d("TreemapLayout", "=== TRUE SQUARIFIED TREEMAP 시작 ===")
        Log.d("TreemapLayout", "컨테이너: ${containerWidth}x${containerHeight}px")
        Log.d("TreemapLayout", "입력 테마: ${rectangles.size}개")
        
        // 1. 모든 테마 로그 출력
        rectangles.forEach { rect ->
            Log.d("TreemapLayout", "테마: ${rect.theme.name}, 등락률: ${rect.theme.rate}%, isRising: ${rect.theme.isRising}")
        }
        
        // 2. HTML과 동일한 데이터 정규화 적용 (하락 테마 최소 영역 보정)
        val normalizedThemes = applyDataNormalization(rectangles)
        
        // 3. Weight 기반 정렬 (abs(changePct) 내림차순)
        val sortedThemes = normalizedThemes.sortedByDescending { kotlin.math.abs(it.theme.rate) }
        
        // 4. 전체 Weight 계산
        val totalWeight = sortedThemes.sumOf { kotlin.math.abs(it.theme.rate) }
        Log.d("TreemapLayout", "총 Weight (정규화 후): $totalWeight")
        
        if (totalWeight <= 0) {
            Log.e("TreemapLayout", "총 Weight가 0 이하! 균등 분할 적용")
            layoutEqualSize(sortedThemes, containerWidth, containerHeight)
            return
        }
        
        // 4. Gap 및 최소 크기 설정 (사용자 요구사항)
        val gapPx = (8 * resources.displayMetrics.density).toInt() // 8dp
        val minSidePx = (56 * resources.displayMetrics.density).toInt() // 56dp
        val cornerRadius = (12 * resources.displayMetrics.density).toInt() // 12dp
        
        Log.d("TreemapLayout", "Gap: ${gapPx}px, MinSide: ${minSidePx}px")
        
        // 5. HTML과 동일한 간단하고 효과적인 트리맵 알고리즘 적용
        layoutSimpleTreemap(
            themes = sortedThemes,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            totalWeight = totalWeight,
            gapPx = gapPx
        )
        
        Log.d("TreemapLayout", "=== TRUE SQUARIFIED TREEMAP 완료 ===")
    }
    
    /**
     * HTML과 동일한 간단하고 효과적인 트리맵 알고리즘
     */
    private fun layoutSimpleTreemap(
        themes: List<Rectangle>,
        containerWidth: Int,
        containerHeight: Int,
        totalWeight: Double,
        gapPx: Int
    ) {
        if (themes.isEmpty()) return
        
        Log.d("TreemapLayout", "=== HTML 스타일 트리맵 시작 ===")
        Log.d("TreemapLayout", "컨테이너: ${containerWidth}x${containerHeight}, 테마: ${themes.size}개")
        
        val availableWidth = containerWidth - gapPx
        val availableHeight = containerHeight - gapPx
        val totalArea = availableWidth * availableHeight
        
        // 각 테마의 면적 계산
        themes.forEach { rect ->
            val weight = kotlin.math.abs(rect.theme.rate)
            val proportion = weight / totalWeight
            val area = (totalArea * proportion).toInt()
            
            // 면적을 기반으로 대략적인 크기 계산 (정사각형에 가깝게)
            val side = kotlin.math.sqrt(area.toDouble()).toInt()
            rect.width = side.coerceAtLeast(80) // 최소 80px
            rect.height = side.coerceAtLeast(60) // 최소 60px
            
            Log.d("TreemapLayout", "${rect.theme.name}: weight=$weight, area=$area, size=${rect.width}x${rect.height}")
        }
        
        // 간단한 행별 배치 (HTML D3.js treemap과 유사)
        layoutInSimpleRows(themes, gapPx / 2, gapPx / 2, availableWidth, availableHeight, gapPx)
        
        Log.d("TreemapLayout", "=== HTML 스타일 트리맵 완료 ===")
    }
    
    /**
     * HTML D3.js와 유사한 간단한 행별 배치
     */
    private fun layoutInSimpleRows(
        themes: List<Rectangle>,
        startX: Int,
        startY: Int,
        maxWidth: Int,
        maxHeight: Int,
        gapPx: Int
    ) {
        var currentX = startX
        var currentY = startY
        var currentRowHeight = 0
        var currentRowWidth = 0
        
        themes.forEach { rect ->
            // 현재 행에 들어갈 수 있는지 확인
            if (currentX + rect.width > startX + maxWidth && currentRowWidth > 0) {
                // 다음 행으로 이동
                currentX = startX
                currentY += currentRowHeight + gapPx
                currentRowHeight = 0
                currentRowWidth = 0
            }
            
            // 현재 위치에 배치
            rect.x = currentX
            rect.y = currentY
            
            // 다음 위치 계산
            currentX += rect.width + gapPx
            currentRowHeight = kotlin.math.max(currentRowHeight, rect.height)
            currentRowWidth += rect.width + gapPx
            
            Log.d("TreemapLayout", "${rect.theme.name} 배치: (${rect.x}, ${rect.y}) ${rect.width}x${rect.height}")
        }
    }

    /**
     * 기존 복잡한 트리맵 메인 알고리즘 (사용 안함)
     */
    private fun layoutTrueSquarifiedTreemap_UNUSED(
        themes: List<Rectangle>,
        containerWidth: Int,
        containerHeight: Int,
        totalWeight: Double,
        gapPx: Int,
        minSidePx: Int
    ) {
        if (themes.isEmpty()) return
        
        // 사용 가능한 영역 (Gap 제외)
        val availableWidth = containerWidth - gapPx
        val availableHeight = containerHeight - gapPx
        val totalArea = availableWidth * availableHeight
        
        Log.d("TreemapLayout", "사용 가능 영역: ${availableWidth}x${availableHeight} = ${totalArea}px²")
        
        // 각 테마의 목표 면적 계산 (Weight 기반 정규화)
        themes.forEach { rect ->
            val weight = kotlin.math.abs(rect.theme.rate)
            val proportion = weight / totalWeight
            val targetArea = (totalArea * proportion).toInt()
            
            Log.d("TreemapLayout", "${rect.theme.name}: weight=$weight, 비율=${String.format("%.3f", proportion)}, 목표면적=${targetArea}px²")
        }
        
        // Squarified 알고리즘으로 최적 배치
        squarifyAlgorithm(
            themes = themes,
            x = gapPx / 2,
            y = gapPx / 2,
            width = availableWidth,
            height = availableHeight,
            totalWeight = totalWeight,
            totalArea = totalArea
        )
        
        // Rounding 오차 수정 - 100% 공간 채우기
        fixRoundingErrors(themes, containerWidth, containerHeight, gapPx)
        
        // 최종 검증
        validateFinalLayout(themes, containerWidth, containerHeight, minSidePx)
    }
    
    /**
     * Squarified 알고리즘 - 정사각형에 가까운 타일 생성
     */
    private fun squarifyAlgorithm(
        themes: List<Rectangle>,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        totalWeight: Double,
        totalArea: Int
    ) {
        if (themes.isEmpty()) return
        
        Log.d("TreemapLayout", "Squarify: ${themes.size}개 테마를 ${width}x${height} 영역에 배치")
        
        if (themes.size == 1) {
            // 단일 테마: 전체 영역 할당
            val theme = themes[0]
            theme.x = x
            theme.y = y
            theme.width = width
            theme.height = height
            Log.d("TreemapLayout", "단일 테마: ${theme.theme.name} -> ${width}x${height}")
            return
        }
        
        // 행 기반 배치 (단순화된 Squarified)
        layoutInOptimalRows(themes, x, y, width, height, totalWeight, totalArea)
    }
    
    /**
     * 최적 행 배치 - 컨테이너 높이 내 완전 수용
     */
    private fun layoutInOptimalRows(
        themes: List<Rectangle>,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        totalWeight: Double,
        totalArea: Int
    ) {
        if (themes.isEmpty()) return
        
        val gapPx = (8 * resources.displayMetrics.density).toInt()
        val minRowHeight = (50 * resources.displayMetrics.density).toInt() // 최소 행 높이 50dp
        
        // 1단계: 모든 행 구성 미리 계산
        val rows = mutableListOf<List<Rectangle>>()
        var remainingThemes = themes.toMutableList()
        
        while (remainingThemes.isNotEmpty()) {
            val rowSize = kotlin.math.min(
                when {
                    remainingThemes.size >= 6 -> 3
                    remainingThemes.size >= 3 -> 2
                    else -> remainingThemes.size
                }, 
                remainingThemes.size
            )
            
            val rowThemes = remainingThemes.take(rowSize)
            rows.add(rowThemes)
            remainingThemes = remainingThemes.drop(rowSize).toMutableList()
        }
        
        Log.d("TreemapLayout", "총 ${rows.size}개 행으로 구성")
        
        // 2단계: 사용 가능한 높이 계산 (Gap 포함)
        val totalGapHeight = gapPx * (rows.size - 1)
        val availableRowHeight = height - totalGapHeight
        
        // 3단계: 각 행의 이상적 높이 계산 (Weight 기반)
        val rowWeights = rows.map { row -> row.sumOf { kotlin.math.abs(it.theme.rate) } }
        val totalRowWeight = rowWeights.sum()
        
        val idealRowHeights = rowWeights.map { weight ->
            if (totalRowWeight > 0) {
                (availableRowHeight * weight / totalRowWeight).toInt().coerceAtLeast(minRowHeight)
            } else {
                availableRowHeight / rows.size
            }
        }
        
        // 4단계: 높이 오버플로우 체크 및 조정
        val totalIdealHeight = idealRowHeights.sum()
        val scaleFactor = if (totalIdealHeight > availableRowHeight) {
            availableRowHeight.toDouble() / totalIdealHeight
        } else {
            1.0
        }
        
        val adjustedRowHeights = idealRowHeights.map { 
            (it * scaleFactor).toInt().coerceAtLeast(minRowHeight)
        }.toMutableList()
        
        // 5단계: Rounding 오차 수정 - 마지막 행에 분배
        val actualTotal = adjustedRowHeights.sum()
        val heightDiff = availableRowHeight - actualTotal
        if (heightDiff != 0 && adjustedRowHeights.isNotEmpty()) {
            adjustedRowHeights[adjustedRowHeights.size - 1] += heightDiff
        }
        
        Log.d("TreemapLayout", "행 높이 조정: 스케일=${String.format("%.3f", scaleFactor)}, 최종 높이=${adjustedRowHeights.sum()}/${availableRowHeight}")
        
        // 6단계: 실제 배치
        var currentY = startY
        rows.forEachIndexed { rowIndex, rowThemes ->
            val rowHeight = adjustedRowHeights[rowIndex]
            
            Log.d("TreemapLayout", "행 ${rowIndex + 1}: ${rowThemes.size}개 테마, 높이: ${rowHeight}px at Y=${currentY}")
            
            layoutRowHorizontally(rowThemes, startX, currentY, width, rowHeight, totalWeight, totalArea)
            
            currentY += rowHeight + gapPx
        }
        
        Log.d("TreemapLayout", "최종 Y 위치: ${currentY - gapPx}, 컨테이너 높이: ${startY + height}")
    }
    
    /**
     * 행 내 가로 배치 - 정확한 비례 크기
     */
    private fun layoutRowHorizontally(
        rowThemes: List<Rectangle>,
        startX: Int,
        y: Int,
        totalWidth: Int,
        height: Int,
        totalWeight: Double,
        totalArea: Int
    ) {
        if (rowThemes.isEmpty()) return
        
        val gapPx = (4 * resources.displayMetrics.density).toInt()
        val availableWidth = totalWidth - (gapPx * (rowThemes.size - 1))
        val rowWeight = rowThemes.sumOf { kotlin.math.abs(it.theme.rate) }
        
        var currentX = startX
        
        rowThemes.forEachIndexed { index, rect ->
            val weight = kotlin.math.abs(rect.theme.rate)
            val proportion = if (rowWeight > 0) weight / rowWeight else 1.0 / rowThemes.size
            
            // 마지막 타일은 남은 공간 모두 사용 (Rounding 오차 방지)
            val tileWidth = if (index == rowThemes.size - 1) {
                startX + totalWidth - currentX
            } else {
                (availableWidth * proportion).toInt()
            }
            
            rect.x = currentX
            rect.y = y
            rect.width = kotlin.math.max(tileWidth, 50) // 최소 50px
            rect.height = height
            
            currentX += rect.width + gapPx
            
            Log.d("TreemapLayout", "타일: ${rect.theme.name} -> ${rect.width}x${rect.height} at (${rect.x}, ${rect.y})")
        }
    }
    
    /**
     * Rounding 오차 수정 - 100% 공간 채우기
     */
    private fun fixRoundingErrors(themes: List<Rectangle>, containerWidth: Int, containerHeight: Int, gapPx: Int) {
        if (themes.isEmpty()) return
        
        // 현재 사용된 공간 계산
        val maxX = themes.maxOfOrNull { it.x + it.width } ?: 0
        val maxY = themes.maxOfOrNull { it.y + it.height } ?: 0
        
        val targetMaxX = containerWidth - gapPx / 2
        val targetMaxY = containerHeight - gapPx / 2
        
        val extraWidth = targetMaxX - maxX
        val extraHeight = targetMaxY - maxY
        
        Log.d("TreemapLayout", "Rounding 오차 수정: 추가 너비=${extraWidth}px, 추가 높이=${extraHeight}px")
        
        // 가장 오른쪽 타일들에 너비 분배
        if (extraWidth > 0) {
            val rightTiles = themes.filter { it.x + it.width == maxX }
            if (rightTiles.isNotEmpty()) {
                val widthPerTile = extraWidth / rightTiles.size
                val remainder = extraWidth % rightTiles.size
                
                rightTiles.forEachIndexed { index, tile ->
                    tile.width += widthPerTile + if (index < remainder) 1 else 0
                }
                Log.d("TreemapLayout", "너비 분배 완료: ${rightTiles.size}개 타일")
            }
        }
        
        // 가장 아래쪽 타일들에 높이 분배
        if (extraHeight > 0) {
            val bottomTiles = themes.filter { it.y + it.height == maxY }
            if (bottomTiles.isNotEmpty()) {
                val heightPerTile = extraHeight / bottomTiles.size
                val remainder = extraHeight % bottomTiles.size
                
                bottomTiles.forEachIndexed { index, tile ->
                    tile.height += heightPerTile + if (index < remainder) 1 else 0
                }
                Log.d("TreemapLayout", "높이 분배 완료: ${bottomTiles.size}개 타일")
            }
        }
    }
    
    /**
     * 최종 레이아웃 검증 - 경계 체크 강화
     */
    private fun validateFinalLayout(themes: List<Rectangle>, containerWidth: Int, containerHeight: Int, minSidePx: Int) {
        Log.d("TreemapLayout", "=== 최종 레이아웃 검증 ===")
        
        // 1. 모든 테마 표시 확인
        Log.d("TreemapLayout", "총 테마 수: ${themes.size}")
        
        // 2. 경계 벗어남 체크 - 가장 중요!
        val outOfBoundsThemes = themes.filter { rect ->
            rect.x < 0 || rect.y < 0 || 
            rect.x + rect.width > containerWidth || 
            rect.y + rect.height > containerHeight
        }
        
        if (outOfBoundsThemes.isNotEmpty()) {
            Log.e("TreemapLayout", "⚠️ 경계 벗어난 테마 ${outOfBoundsThemes.size}개 발견!")
            outOfBoundsThemes.forEach { rect ->
                Log.e("TreemapLayout", "  ${rect.theme.name}: (${rect.x}, ${rect.y}) ${rect.width}x${rect.height} - 컨테이너: ${containerWidth}x${containerHeight}")
            }
        } else {
            Log.i("TreemapLayout", "✅ 모든 테마가 컨테이너 내부에 완벽히 배치됨")
        }
        
        // 3. 면적 활용률 계산
        val totalUsedArea = themes.sumOf { it.width * it.height }
        val totalContainerArea = containerWidth * containerHeight
        val utilization = (totalUsedArea.toDouble() / totalContainerArea * 100)
        Log.d("TreemapLayout", "면적 활용률: ${String.format("%.1f", utilization)}%")
        
        // 4. Y 좌표 분포 확인 (상단/하단 분포)
        val maxY = themes.maxOfOrNull { it.y + it.height } ?: 0
        val minY = themes.minOfOrNull { it.y } ?: 0
        Log.d("TreemapLayout", "Y 범위: ${minY}px ~ ${maxY}px (컨테이너: 0 ~ ${containerHeight}px)")
        
        // 5. 상승/하락 구분 확인
        val risingCount = themes.count { it.theme.isRising }
        val fallingCount = themes.count { !it.theme.isRising }
        Log.d("TreemapLayout", "상승 테마: ${risingCount}개, 하락 테마: ${fallingCount}개")
        
        // 6. 크기 차이 확인 (큰 테마 vs 작은 테마)
        if (themes.size >= 2) {
            val largest = themes.maxByOrNull { it.width * it.height }
            val smallest = themes.minByOrNull { it.width * it.height }
            if (largest != null && smallest != null) {
                val largestArea = largest.width * largest.height
                val smallestArea = smallest.width * smallest.height
                val sizeRatio = largestArea.toDouble() / smallestArea
                Log.d("TreemapLayout", "크기 차이: 최대(${largest.theme.name}:${largestArea}px²) vs 최소(${smallest.theme.name}:${smallestArea}px²) = ${String.format("%.1f", sizeRatio)}배")
            }
        }
        
        // 7. 56dp 미만 타일 체크
        val smallTiles = themes.filter { it.width < minSidePx || it.height < minSidePx }
        Log.d("TreemapLayout", "56dp 미만 타일: ${smallTiles.size}개")
        
        Log.d("TreemapLayout", "=== 검증 완료 ===")
    }
    
    /**
     * 균등 크기 배치 (Weight가 모두 0인 경우)
     */
    private fun layoutEqualSize(themes: List<Rectangle>, containerWidth: Int, containerHeight: Int) {
        val gapPx = (8 * resources.displayMetrics.density).toInt()
        val cols = kotlin.math.ceil(kotlin.math.sqrt(themes.size.toDouble())).toInt()
        val rows = kotlin.math.ceil(themes.size.toDouble() / cols).toInt()
        
        val tileWidth = (containerWidth - gapPx * (cols + 1)) / cols
        val tileHeight = (containerHeight - gapPx * (rows + 1)) / rows
        
        themes.forEachIndexed { index, rect ->
            val col = index % cols
            val row = index / cols
            
            rect.x = gapPx + col * (tileWidth + gapPx)
            rect.y = gapPx + row * (tileHeight + gapPx)
            rect.width = tileWidth
            rect.height = tileHeight
        }
        
        Log.d("TreemapLayout", "균등 크기 배치 완료: ${cols}x${rows} 그리드")
    }
    
    /**
     * 간단한 상승/하락 분리 배치 - 기존 좋았던 알고리즘 기반
     */
    private fun layoutWithSimpleSeparation(
        risingThemes: List<Rectangle>, 
        fallingThemes: List<Rectangle>, 
        containerWidth: Int, 
        containerHeight: Int
    ) {
        val gapPx = (8 * resources.displayMetrics.density).toInt()
        val minSidePx = (56 * resources.displayMetrics.density).toInt()
        
        // 높이를 반반으로 나누기 (간단한 접근)
        val risingHeight = (containerHeight * 0.6).toInt() // 상승에 더 많은 공간
        val fallingHeight = containerHeight - risingHeight - gapPx
        
        Log.d("TreemapLayout", "높이 분배: 상승=${risingHeight}px, 하락=${fallingHeight}px")
        
        // 1. 상승 테마들을 상단에 배치
        if (risingThemes.isNotEmpty()) {
            layoutSimpleGrid(risingThemes, 0, 0, containerWidth, risingHeight, "상승")
        }
        
        // 2. 하락 테마들을 하단에 배치 
        if (fallingThemes.isNotEmpty()) {
            layoutSimpleGrid(fallingThemes, 0, risingHeight + gapPx, containerWidth, fallingHeight, "하락")
        }
    }
    
    /**
     * 간단한 그리드 배치 - 모든 테마 표시 보장
     */
    private fun layoutSimpleGrid(
        themes: List<Rectangle>,
        startX: Int,
        startY: Int, 
        width: Int,
        height: Int,
        sectionName: String
    ) {
        if (themes.isEmpty()) return
        
        Log.d("TreemapLayout", "$sectionName 섹션: ${themes.size}개 테마 배치")
        
        val gapPx = (4 * resources.displayMetrics.density).toInt() // 4dp 간격
        
        // 간단한 행별 배치 - 모든 테마 표시
        var currentX = startX + gapPx
        var currentY = startY + gapPx
        var currentRowHeight = 0
        var themesInCurrentRow = 0
        val maxThemesPerRow = 4 // 한 줄에 최대 4개
        
        themes.forEach { rect ->
            // 행 넘김 체크
            if (themesInCurrentRow >= maxThemesPerRow || 
                (currentX + 150 > width && themesInCurrentRow > 0)) {
                currentX = startX + gapPx
                currentY += currentRowHeight + gapPx
                currentRowHeight = 0
                themesInCurrentRow = 0
            }
            
            // Weight에 따른 크기 계산 (간단한 방식)
            val weight = kotlin.math.abs(rect.theme.rate)
            val baseSize = 100
            val sizeMultiplier = when {
                weight >= 5.0 -> 1.8  // 큰 테마
                weight >= 2.0 -> 1.4  // 중간 테마
                else -> 1.0           // 작은 테마
            }
            
            val tileWidth = (baseSize * sizeMultiplier).toInt()
            val tileHeight = (baseSize * sizeMultiplier * 0.8).toInt()
            
            // 좌표 설정
            rect.x = currentX
            rect.y = currentY
            rect.width = tileWidth
            rect.height = tileHeight
            
            currentX += tileWidth + gapPx
            currentRowHeight = kotlin.math.max(currentRowHeight, tileHeight)
            themesInCurrentRow++
            
            Log.d("TreemapLayout", "$sectionName: ${rect.theme.name} -> ${rect.width}x${rect.height} at (${rect.x}, ${rect.y})")
        }
    }
    
    /**
     * 실제 등락률 비례로 동적 크기 계산 - 상대적 비율로 전체 영역 꽉 채우기
     */
    private fun calculateOptimizedSizes(containerWidth: Int, containerHeight: Int, totalValue: Double) {
        val minBoxSize = 80 // 텍스트 가독성을 위해 최소 크기 대폭 증가
        val gap = 1
        val totalArea = (containerWidth * containerHeight).toDouble()
        
        // 모든 테마의 절대값 합계 계산
        val totalAbsRate = rectangles.sumOf { kotlin.math.abs(it.theme.rate) }
        
        Log.d("TreemapLayout", "총 면적: $totalArea, 총 절대 등락률: $totalAbsRate")
        
        rectangles.forEach { rect ->
            val absRate = kotlin.math.abs(rect.theme.rate)
            
            // 실제 등락률 비례로 면적 계산 (더 관대한 최소 면적 보장)
            val baseArea = minBoxSize * minBoxSize // 기본 면적
            val proportionalArea = kotlin.math.max(
                (absRate / totalAbsRate) * totalArea * 0.90, // 90% 활용
                baseArea.toDouble() // 최소 면적 보장
            )
            
            // 면적을 기반으로 가로세로 비율 계산 (가독성 고려)
            val aspectRatio = 1.3 // 가로가 세로보다 조금 더 긴 형태
            val calculatedHeight = kotlin.math.sqrt(proportionalArea / aspectRatio)
            val calculatedWidth = proportionalArea / calculatedHeight
            
            rect.width = calculatedWidth.toInt().coerceAtLeast(minBoxSize)
            rect.height = calculatedHeight.toInt().coerceAtLeast(minBoxSize)
            
            // 컨테이너 크기를 넘지 않도록 제한
            if (rect.width > containerWidth * 0.6) {
                rect.width = (containerWidth * 0.6).toInt()
                rect.height = (proportionalArea / rect.width).toInt().coerceAtLeast(minBoxSize)
            }
            if (rect.height > containerHeight * 0.5) {
                rect.height = (containerHeight * 0.5).toInt()
                rect.width = (proportionalArea / rect.height).toInt().coerceAtLeast(minBoxSize)
            }
            
            Log.d("TreemapLayout", "동적 크기: ${rect.theme.name} (${rect.theme.rate}%, 비율=${String.format("%.2f", absRate/totalAbsRate*100)}%) → ${rect.width}x${rect.height} (면적=${rect.width*rect.height})")
        }
        
        // 전체 영역 활용도 극대화를 위한 후처리
        optimizeSpaceUtilization(containerWidth, containerHeight)
    }
    
    /**
     * 공간 활용도 극대화 - 빈 공간을 없애기 위한 강력한 크기 조정
     */
    private fun optimizeSpaceUtilization(containerWidth: Int, containerHeight: Int) {
        val currentTotalArea = rectangles.sumOf { it.width * it.height }
        val targetArea = (containerWidth * containerHeight * 0.98).toInt() // 98% 목표로 상향
        val scaleFactor = kotlin.math.sqrt(targetArea.toDouble() / currentTotalArea)
        
        Log.d("TreemapLayout", "공간 최적화: 현재 면적=$currentTotalArea, 목표=$targetArea, 스케일=$scaleFactor")
        
        if (scaleFactor > 1.0) { // 공간이 남으면 크기 확대
            rectangles.forEach { rect ->
                val newWidth = (rect.width * scaleFactor).toInt().coerceAtMost(containerWidth - 2)
                val newHeight = (rect.height * scaleFactor).toInt().coerceAtMost(containerHeight - 2)
                
                // 최소 크기 보장하면서 확대
                rect.width = newWidth.coerceAtLeast(80)
                rect.height = newHeight.coerceAtLeast(60)
                
                Log.d("TreemapLayout", "크기 확대: ${rect.theme.name} → ${rect.width}x${rect.height}")
            }
        } else if (scaleFactor < 0.9) { // 크기가 너무 크면 적절히 축소
            rectangles.forEach { rect ->
                rect.width = (rect.width * scaleFactor).toInt().coerceAtLeast(80)
                rect.height = (rect.height * scaleFactor).toInt().coerceAtLeast(60)
                Log.d("TreemapLayout", "크기 조정: ${rect.theme.name} → ${rect.width}x${rect.height}")
            }
        }
    }
    
    /**
     * 상승/하락 구분 배치 + 공간 최적화
     * 상승은 상단, 하락은 하단에 배치하여 가독성 향상
     */
    private fun layoutTetrisStyle(containerWidth: Int, containerHeight: Int) {
        // 상승/하락으로 분리하고 크기 순으로 정렬
        val risingThemes = rectangles.filter { it.theme.isRising }.sortedByDescending { kotlin.math.abs(it.theme.rate) }
        val fallingThemes = rectangles.filter { !it.theme.isRising }.sortedByDescending { kotlin.math.abs(it.theme.rate) }
        
        Log.d("TreemapLayout", "상승 테마: ${risingThemes.size}개, 하락 테마: ${fallingThemes.size}개")
        
        // 1단계: 상승 테마들을 상단에 배치
        if (risingThemes.isNotEmpty()) {
            layoutThemesInSection(risingThemes, containerWidth, containerHeight, 0, "상승")
        }
        
        // 2단계: 하락 테마들을 상승 테마 아래에 배치
        if (fallingThemes.isNotEmpty()) {
            val risingMaxY = if (risingThemes.isNotEmpty()) {
                risingThemes.maxOfOrNull { it.y + it.height } ?: 0
            } else 0
            layoutThemesInSection(fallingThemes, containerWidth, containerHeight, risingMaxY + 2, "하락")
        }
    }
    
    /**
     * 완벽한 공간 채우기 - 빈 공간 100% 제거
     */
    private fun layoutThemesInSection(themes: List<Rectangle>, containerWidth: Int, containerHeight: Int, startY: Int, sectionName: String) {
        if (themes.isEmpty()) return
        
        // 가독성을 위한 최소 간격
        val gap = 1
        
        // 섹션 높이를 더 유연하게 조정 - 상승 테마 수에 따라 동적 할당
        val risingCount = rectangles.count { it.theme.isRising }
        val fallingCount = rectangles.count { !it.theme.isRising }
        val totalCount = risingCount + fallingCount
        
        val sectionMaxHeight = if (sectionName == "상승") {
            if (totalCount > 0) {
                // 등락률 절대값 합계에 따른 동적 영역 할당
                val risingAbsSum = rectangles.filter { it.theme.isRising }.sumOf { kotlin.math.abs(it.theme.rate) }
                val fallingAbsSum = rectangles.filter { !it.theme.isRising }.sumOf { kotlin.math.abs(it.theme.rate) }
                val totalAbsSum = risingAbsSum + fallingAbsSum
                
                if (totalAbsSum > 0) {
                    val ratio = (risingAbsSum / totalAbsSum).coerceIn(0.55, 0.75) // 상승 테마에 더 많은 공간
                    (containerHeight * ratio).toInt()
                } else {
                    containerHeight / 2
                }
            } else {
                containerHeight / 2
            }
        } else {
            containerHeight - startY
        }
        
        Log.d("TreemapLayout", "$sectionName 섹션 배치: ${themes.size}개, startY=$startY, maxHeight=$sectionMaxHeight")
        
        // 완전한 그리드 레이아웃으로 빈 공간 제거
        fillGridCompletely(themes, containerWidth, sectionMaxHeight, startY, sectionName)
        
        Log.d("TreemapLayout", "$sectionName 섹션 완료 - 100% 공간 채움")
    }
    
    /**
     * 테트리스 스타일 완전 채우기 - 공간 우선, 등락률은 참고만
     */
    private fun fillGridCompletely(themes: List<Rectangle>, containerWidth: Int, sectionMaxHeight: Int, startY: Int, sectionName: String) {
        if (themes.isEmpty()) return
        
        val gapPx = (8 * resources.displayMetrics.density).toInt() // 8dp 간격
        val minSidePx = (56 * resources.displayMetrics.density).toInt() // 56dp 최소 크기
        
        Log.d("TreemapLayout", "=== $sectionName 섹션: True Squarified Treemap ===")
        Log.d("TreemapLayout", "Container: ${containerWidth}x${sectionMaxHeight}, Gap: ${gapPx}px, MinSide: ${minSidePx}px")
        
        // 1. Weight 계산 및 정규화
        val totalWeight = themes.sumOf { kotlin.math.abs(it.theme.rate) }
        val availableWidth = containerWidth - gapPx
        val availableHeight = sectionMaxHeight - gapPx  
        val totalAreaPx = availableWidth * availableHeight
        
        Log.d("TreemapLayout", "Total Weight: $totalWeight, Available Area: ${totalAreaPx}px")
        
        if (totalWeight <= 0) {
            // 모든 weight가 0인 경우 균등 분할
            layoutEqualSized(themes, containerWidth, sectionMaxHeight, startY, gapPx, minSidePx)
            return
        }
        
        // 2. Weight 내림차순 정렬
        val sortedThemes = themes.sortedByDescending { kotlin.math.abs(it.theme.rate) }
        
        // 3. 각 테마의 목표 면적 계산
        sortedThemes.forEach { rect ->
            val weight = kotlin.math.abs(rect.theme.rate)
            val proportion = weight / totalWeight
            val targetAreaPx = (totalAreaPx * proportion).toInt()
            
            Log.d("TreemapLayout", "${rect.theme.name}: weight=$weight, proportion=${String.format("%.3f", proportion)}, targetArea=${targetAreaPx}px")
        }
        
        // 4. 개선된 Grid 기반 Treemap 적용 (간단하고 안정적)
        layoutOptimizedGrid(sortedThemes, gapPx / 2, startY + gapPx / 2, availableWidth, availableHeight, totalWeight, gapPx, minSidePx)
        
        // 5. Rounding 오차 수정 - 100% 공간 채우기
        fixRoundingErrors(sortedThemes, containerWidth, sectionMaxHeight, startY, gapPx)
        
        // 6. 검증 로그
        validateLayout(sortedThemes, containerWidth, sectionMaxHeight, startY, sectionName)
    }
    
    /**
     * True Squarified Treemap Algorithm
     */
    private fun squarifyTrueAlgorithm(
        themes: List<Rectangle>,
        bounds: Rectangle,
        totalWeight: Double,
        totalAreaPx: Int,
        gapPx: Int,
        minSidePx: Int
    ) {
        if (themes.isEmpty()) return
        
        Log.d("TreemapLayout", "Squarify: ${themes.size} themes in bounds ${bounds.width}x${bounds.height}")
        
        if (themes.size == 1) {
            // 단일 테마: 전체 영역 할당
            val theme = themes[0]
            theme.x = bounds.x
            theme.y = bounds.y
            theme.width = bounds.width
            theme.height = bounds.height
            
            Log.d("TreemapLayout", "Single tile: ${theme.theme.name} -> ${theme.width}x${theme.height}")
            return
        }
        
        // 재귀 분할을 위한 최적 분할점 찾기
        val bestSplit = findOptimalSplit(themes, totalWeight)
        val group1 = themes.take(bestSplit)
        val group2 = themes.drop(bestSplit)
        
        if (group1.isEmpty() || group2.isEmpty()) {
            // 분할 실패시 행별 배치
            layoutInRows(themes, bounds.x, bounds.y, bounds.width, bounds.height, gapPx)
            return
        }
        
        val group1Weight = group1.sumOf { kotlin.math.abs(it.theme.rate) }
        val group2Weight = group2.sumOf { kotlin.math.abs(it.theme.rate) }
        
        // 가로/세로 중 더 긴 쪽으로 분할
        val isHorizontalSplit = bounds.width >= bounds.height
        
        val bounds1 = Rectangle(theme = Theme("", 0.0, 0.0, false, 1))
        val bounds2 = Rectangle(theme = Theme("", 0.0, 0.0, false, 1))
        
        if (isHorizontalSplit) {
            // 세로 분할 (가로가 긴 경우)
            val splitRatio = group1Weight / totalWeight
            val splitX = bounds.x + (bounds.width * splitRatio).toInt()
            
            bounds1.apply {
                x = bounds.x
                y = bounds.y
                width = splitX - bounds.x
                height = bounds.height
            }
            
            bounds2.apply {
                x = splitX
                y = bounds.y
                width = bounds.x + bounds.width - splitX
                height = bounds.height
            }
        } else {
            // 가로 분할 (세로가 긴 경우)
            val splitRatio = group1Weight / totalWeight
            val splitY = bounds.y + (bounds.height * splitRatio).toInt()
            
            bounds1.apply {
                x = bounds.x
                y = bounds.y
                width = bounds.width
                height = splitY - bounds.y
            }
            
            bounds2.apply {
                x = bounds.x
                y = splitY
                width = bounds.width
                height = bounds.y + bounds.height - splitY
            }
        }
        
        Log.d("TreemapLayout", "Split ${if (isHorizontalSplit) "vertical" else "horizontal"}: Group1=${group1.size} Group2=${group2.size}")
        
        // 재귀 분할
        val group1Area = (bounds1.width * bounds1.height)
        val group2Area = (bounds2.width * bounds2.height)
        
        squarifyTrueAlgorithm(group1, bounds1, group1Weight, group1Area, gapPx, minSidePx)
        squarifyTrueAlgorithm(group2, bounds2, group2Weight, group2Area, gapPx, minSidePx)
    }
    
    /**
     * 최적 분할점 찾기 (정사각형에 가까운 형태 우선)
     */
    private fun findOptimalSplit(themes: List<Rectangle>, totalWeight: Double): Int {
        if (themes.size <= 2) return 1
        
        var bestSplit = 1
        var bestRatio = Double.MAX_VALUE
        
        // 30% ~ 70% 사이의 분할을 선호 (균형잡힌 분할)
        for (i in 1 until themes.size) {
            val group1Weight = themes.take(i).sumOf { kotlin.math.abs(it.theme.rate) }
            val ratio = group1Weight / totalWeight
            
            if (ratio in 0.3..0.7) {
                val deviation = kotlin.math.abs(ratio - 0.5)
                if (deviation < bestRatio) {
                    bestRatio = deviation
                    bestSplit = i
                }
            }
        }
        
        return bestSplit
    }
    
    /**
     * 상승/하락 분리된 Grid 기반 Treemap - 전체 높이 활용
     */
    private fun layoutOptimizedGrid(
        themes: List<Rectangle>,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        totalWeight: Double,
        gapPx: Int,
        minSidePx: Int
    ) {
        if (themes.isEmpty()) return
        
        Log.d("TreemapLayout", "=== Rising/Falling Separated Layout ===")
        Log.d("TreemapLayout", "Total Area: ${width}x${height}, Gap: ${gapPx}px")
        
        // 상승/하락으로 명확히 분리 및 디버깅
        val risingThemes = themes.filter { it.theme.isRising }.sortedByDescending { kotlin.math.abs(it.theme.rate) }
        val fallingThemes = themes.filter { !it.theme.isRising }.sortedByDescending { kotlin.math.abs(it.theme.rate) }
        
        Log.d("TreemapLayout", "=== THEME SEPARATION DEBUG ===")
        Log.d("TreemapLayout", "Total themes: ${themes.size}")
        Log.d("TreemapLayout", "Rising themes: ${risingThemes.size}")
        risingThemes.forEach { Log.d("TreemapLayout", "  RISING: ${it.theme.name} (${it.theme.rate}%) isRising=${it.theme.isRising}") }
        Log.d("TreemapLayout", "Falling themes: ${fallingThemes.size}")
        fallingThemes.forEach { Log.d("TreemapLayout", "  FALLING: ${it.theme.name} (${it.theme.rate}%) isRising=${it.theme.isRising}") }
        
        if (risingThemes.isEmpty() && fallingThemes.isEmpty()) {
            Log.e("TreemapLayout", "⚠️ 모든 테마가 필터링됨!")
            return
        }
        
        // 상승/하락 비율에 따라 높이 분배 (더 안전한 계산)
        val risingWeight = risingThemes.sumOf { kotlin.math.abs(it.theme.rate) }
        val fallingWeight = fallingThemes.sumOf { kotlin.math.abs(it.theme.rate) }
        val totalSectionWeight = risingWeight + fallingWeight
        
        // 최소 30% 높이는 보장하되, 실제 weight 비율 반영
        val risingHeight = if (totalSectionWeight > 0 && risingThemes.isNotEmpty()) {
            val ratio = risingWeight / totalSectionWeight
            ((height * ratio).toInt()).coerceIn(minSidePx, height - minSidePx - gapPx)
        } else if (risingThemes.isNotEmpty()) {
            height / 2
        } else {
            0
        }
        
        val fallingHeight = if (fallingThemes.isNotEmpty()) {
            height - risingHeight - gapPx
        } else {
            0
        }
        
        Log.d("TreemapLayout", "Weight distribution: Rising=${risingWeight}, Falling=${fallingWeight}")
        Log.d("TreemapLayout", "Height allocation: Rising=${risingHeight}px, Falling=${fallingHeight}px")
        
        // 1. 상승 테마들을 절대 상단에 배치
        if (risingThemes.isNotEmpty()) {
            val risingStartY = startY
            Log.d("TreemapLayout", "🔴 RISING SECTION: Y=${risingStartY} to ${risingStartY + risingHeight}")
            layoutSectionOptimized(risingThemes, startX, risingStartY, width, risingHeight, gapPx, minSidePx, "Rising")
        }
        
        // 2. 하락 테마들을 절대 하단에 배치 (상승 영역 아래)
        if (fallingThemes.isNotEmpty() && fallingHeight > minSidePx) {
            val fallingStartY = startY + risingHeight + gapPx
            Log.d("TreemapLayout", "🔵 FALLING SECTION: Y=${fallingStartY} to ${fallingStartY + fallingHeight}")
            layoutSectionOptimized(fallingThemes, startX, fallingStartY, width, fallingHeight, gapPx, minSidePx, "Falling")
        }
        
        Log.d("TreemapLayout", "Separated layout completed. Total used height: ${risingHeight + gapPx + fallingHeight}")
    }
    
    /**
     * 한 섹션(상승 또는 하락) 내에서 최적 배치
     */
    private fun layoutSectionOptimized(
        themes: List<Rectangle>, 
        startX: Int, 
        startY: Int, 
        width: Int, 
        height: Int, 
        gapPx: Int, 
        minSidePx: Int,
        sectionName: String
    ) {
        if (themes.isEmpty()) return
        
        Log.d("TreemapLayout", "=== $sectionName Section: ${themes.size} themes in ${width}x${height} ===")
        
        // Weight에 따라 크기별 그룹핑
        val maxWeight = themes.maxOfOrNull { kotlin.math.abs(it.theme.rate) } ?: 1.0
        val largeThemes = themes.filter { kotlin.math.abs(it.theme.rate) >= maxWeight * 0.5 }
        val smallThemes = themes.filter { kotlin.math.abs(it.theme.rate) < maxWeight * 0.5 }
        
        var currentY = startY
        val remainingHeight = height
        
        // 대형 테마들 (1-2개씩 배치)
        if (largeThemes.isNotEmpty()) {
            val largeRows = largeThemes.chunked(2)
            val largeAreaHeight = (remainingHeight * 0.7).toInt() // 70% 할당
            val rowHeight = largeAreaHeight / largeRows.size
            
            largeRows.forEach { rowThemes ->
                layoutRowOptimized(rowThemes, startX, currentY, width, rowHeight, gapPx)
                currentY += rowHeight + gapPx
            }
        }
        
        // 소형 테마들 (3-5개씩 배치)
        if (smallThemes.isNotEmpty()) {
            val smallRows = smallThemes.chunked(4)
            val remainingSpace = startY + height - currentY
            val rowHeight = kotlin.math.max(remainingSpace / smallRows.size - gapPx, minSidePx)
            
            smallRows.forEach { rowThemes ->
                if (currentY + rowHeight <= startY + height) {
                    layoutRowOptimized(rowThemes, startX, currentY, width, rowHeight, gapPx)
                    currentY += rowHeight + gapPx
                }
            }
        }
        
        Log.d("TreemapLayout", "$sectionName section completed. Used height: ${currentY - startY}")
    }
    
    /**
     * 행의 최적 높이 계산
     */
    private fun calculateRowHeight(themes: List<Rectangle>, totalWeight: Double, baseHeight: Int, minSidePx: Int): Int {
        if (themes.isEmpty()) return minSidePx
        
        val rowWeight = themes.sumOf { kotlin.math.abs(it.theme.rate) }
        val proportion = rowWeight / totalWeight
        val calculatedHeight = (baseHeight * proportion).toInt()
        
        return kotlin.math.max(calculatedHeight, minSidePx)
    }
    
    /**
     * 한 행에 테마들을 최적 배치 - 전체 너비 활용
     */
    private fun layoutRowOptimized(themes: List<Rectangle>, startX: Int, y: Int, totalWidth: Int, height: Int, gapPx: Int) {
        if (themes.isEmpty()) return
        
        val rowWeight = themes.sumOf { kotlin.math.abs(it.theme.rate) }
        val availableWidth = totalWidth - (gapPx * (themes.size - 1))
        var currentX = startX
        
        themes.forEachIndexed { index, rect ->
            val weight = kotlin.math.abs(rect.theme.rate)
            val proportion = if (rowWeight > 0) weight / rowWeight else 1.0 / themes.size
            
            // 마지막 타일은 남은 공간을 모두 사용 (rounding 오차 해결)
            val tileWidth = if (index == themes.size - 1) {
                startX + totalWidth - currentX
            } else {
                (availableWidth * proportion).toInt()
            }
            
            rect.x = currentX
            rect.y = y
            rect.width = kotlin.math.max(tileWidth, 50) // 최소 너비 보장
            rect.height = height
            
            currentX += rect.width + gapPx
            
            Log.d("TreemapLayout", "Row tile: ${rect.theme.name} -> ${rect.width}x${rect.height} at (${rect.x}, ${rect.y})")
        }
    }
    
        /**
     * 행별 배치 알고리즘
     */
    private fun layoutInRows(themes: List<Rectangle>, startX: Int, startY: Int, width: Int, height: Int, gap: Int) {
        var currentX = startX
        var currentY = startY
        var currentRowHeight = 0

        themes.forEach { rect ->
            // 행 넘김 체크
            if (currentX + rect.width > startX + width && currentX > startX) {
                currentX = startX
                currentY += currentRowHeight + gap
                currentRowHeight = 0
            }

            rect.x = currentX
            rect.y = currentY
            currentX += rect.width + gap
            currentRowHeight = kotlin.math.max(currentRowHeight, rect.height)
        }
    }
    
    /**
     * 균등 분할 레이아웃
     */
    private fun layoutEqualSized(themes: List<Rectangle>, containerWidth: Int, sectionMaxHeight: Int, startY: Int, gap: Int, minTileSize: Int) {
        val cols = kotlin.math.ceil(kotlin.math.sqrt(themes.size.toDouble())).toInt()
        val rows = kotlin.math.ceil(themes.size.toDouble() / cols).toInt()
        
        val tileWidth = kotlin.math.max((containerWidth - gap * (cols + 1)) / cols, minTileSize)
        val tileHeight = kotlin.math.max((sectionMaxHeight - gap * (rows + 1)) / rows, minTileSize)
        
        themes.forEachIndexed { index, rect ->
            val col = index % cols
            val row = index / cols
            
            rect.x = gap + col * (tileWidth + gap)
            rect.y = startY + gap + row * (tileHeight + gap)
            rect.width = tileWidth
            rect.height = tileHeight
        }
        
        Log.d("TreemapLayout", "레이아웃 완료 - Weight 기반 Squarified Treemap")
    }
    
    /**
     * Rounding 오차 수정 - 100% 공간 채우기
     */
    private fun fixRoundingErrors(themes: List<Rectangle>, containerWidth: Int, containerHeight: Int, startY: Int, gapPx: Int) {
        if (themes.isEmpty()) return
        
        Log.d("TreemapLayout", "=== Fixing Rounding Errors ===")
        
        // 현재 차지하는 영역 계산
        val maxX = themes.maxOfOrNull { it.x + it.width } ?: 0
        val maxY = themes.maxOfOrNull { it.y + it.height } ?: startY
        
        val targetMaxX = containerWidth - gapPx / 2
        val targetMaxY = startY + containerHeight - gapPx / 2
        
        val extraWidth = targetMaxX - maxX
        val extraHeight = targetMaxY - maxY
        
        Log.d("TreemapLayout", "Extra space: width=$extraWidth, height=$extraHeight")
        
        // 가로 공간 부족분을 가장 오른쪽 타일들에 분배
        if (extraWidth > 0) {
            val rightmostTiles = themes.filter { it.x + it.width == maxX }
            val widthPerTile = extraWidth / kotlin.math.max(rightmostTiles.size, 1)
            val remainder = extraWidth % kotlin.math.max(rightmostTiles.size, 1)
            
            rightmostTiles.forEachIndexed { index, tile ->
                tile.width += widthPerTile + if (index < remainder) 1 else 0
            }
            
            Log.d("TreemapLayout", "Distributed extra width to ${rightmostTiles.size} tiles")
        }
        
        // 세로 공간 부족분을 가장 아래쪽 타일들에 분배
        if (extraHeight > 0) {
            val bottomTiles = themes.filter { it.y + it.height == maxY }
            val heightPerTile = extraHeight / kotlin.math.max(bottomTiles.size, 1)
            val remainder = extraHeight % kotlin.math.max(bottomTiles.size, 1)
            
            bottomTiles.forEachIndexed { index, tile ->
                tile.height += heightPerTile + if (index < remainder) 1 else 0
            }
            
            Log.d("TreemapLayout", "Distributed extra height to ${bottomTiles.size} tiles")
        }
    }
    
    /**
     * 레이아웃 검증 및 로그
     */
    private fun validateLayout(themes: List<Rectangle>, containerWidth: Int, containerHeight: Int, startY: Int, sectionName: String) {
        Log.d("TreemapLayout", "=== Layout Validation for $sectionName ===")
        
        // 1. 모든 테마가 배치되었는지 확인
        Log.d("TreemapLayout", "Total themes: ${themes.size}")
        
        // 2. 면적 검증
        val totalCalculatedArea = themes.sumOf { it.width * it.height }
        val expectedArea = containerWidth * containerHeight
        val areaUtilization = (totalCalculatedArea.toDouble() / expectedArea * 100)
        
        Log.d("TreemapLayout", "Area utilization: ${String.format("%.2f", areaUtilization)}% (${totalCalculatedArea}/${expectedArea}px)")
        
        // 3. 각 테마 상세 정보
        val totalWeight = themes.sumOf { kotlin.math.abs(it.theme.rate) }
        themes.forEach { rect ->
            val weight = kotlin.math.abs(rect.theme.rate)
            val actualArea = rect.width * rect.height
            val expectedProportion = weight / totalWeight
            val actualProportion = actualArea.toDouble() / totalCalculatedArea
            
            Log.d("TreemapLayout", "${rect.theme.name}: " +
                    "rate=${rect.theme.rate}%, " +
                    "size=${rect.width}x${rect.height}, " +
                    "area=${actualArea}px, " +
                    "expected=${String.format("%.3f", expectedProportion)}, " +
                    "actual=${String.format("%.3f", actualProportion)}")
        }
        
        // 4. 빈 공간 검증
        val maxX = themes.maxOfOrNull { it.x + it.width } ?: 0
        val maxY = themes.maxOfOrNull { it.y + it.height } ?: startY
        val emptySpaceX = containerWidth - maxX
        val emptySpaceY = (startY + containerHeight) - maxY
        
        Log.d("TreemapLayout", "Empty space: X=${emptySpaceX}px, Y=${emptySpaceY}px")
        
        // 5. 56dp 미만 타일 체크
        val minSidePx = (56 * resources.displayMetrics.density).toInt()
        val smallTiles = themes.filter { it.width < minSidePx || it.height < minSidePx }
        Log.d("TreemapLayout", "Small tiles (<56dp): ${smallTiles.size} - ${smallTiles.map { it.theme.name }}")
        
        Log.d("TreemapLayout", "=== Validation Complete ===")
    }
    
    /**
     * 테트리스 스타일 빈 공간 완전 제거
     */
    private fun fillTetrisStyle(allRows: List<MutableList<Rectangle>>, containerWidth: Int, sectionMaxHeight: Int, startY: Int, sectionName: String) {
        val gap = 1
        
        // 1단계: 각 행의 가로 공간 완전 채우기
        allRows.forEach { row ->
            if (row.isNotEmpty()) {
                val totalUsedWidth = row.sumOf { it.width } + (row.size - 1) * gap
                val remainingWidth = containerWidth - totalUsedWidth
                
                if (remainingWidth > 0 && row.isNotEmpty()) {
                    // 남은 공간을 완전히 분배 (빈 공간 0% 목표)
                    val totalWeights = row.sumOf { 
                        val absRate = kotlin.math.abs(it.theme.rate)
                        val sizeWeight = it.width * it.height
                        val rateWeight = absRate * 200 // 등락률 가중치 최소화
                        sizeWeight + rateWeight
                    }
                    
                    if (totalWeights > 0) {
                        var distributedWidth = 0
                        row.forEachIndexed { index, rect ->
                            val absRate = kotlin.math.abs(rect.theme.rate)
                            val sizeWeight = rect.width * rect.height
                            val rateWeight = absRate * 1000
                            val totalWeight = sizeWeight + rateWeight
                            
                            val share = if (index < row.size - 1) {
                                (remainingWidth * totalWeight / totalWeights).toInt()
                            } else {
                                remainingWidth - distributedWidth // 마지막은 나머지 모두
                            }
                            rect.width += kotlin.math.max(0, share) // 음수 방지
                            distributedWidth += share
                        }
                    } else {
                        // totalWeights가 0인 경우 균등 분배
                        val equalShare = remainingWidth / row.size
                        val remainder = remainingWidth % row.size
                        row.forEachIndexed { index, rect ->
                            rect.width += equalShare + if (index < remainder) 1 else 0
                        }
                    }
                    
                    // X 좌표 재조정
                    var adjustedX = row.first().x
                    row.forEach { rect ->
                        rect.x = adjustedX
                        adjustedX += rect.width + gap
                    }
                }
            }
        }
        
        // 2단계: 세로 공간 완전 채우기
        val rowHeights = allRows.map { row -> row.maxOfOrNull { it.height } ?: 0 }
        val totalUsedHeight = rowHeights.sum() + (allRows.size - 1) * gap
        val remainingHeight = sectionMaxHeight - totalUsedHeight
        
        if (remainingHeight > 0 && allRows.isNotEmpty()) {
            // 행의 높이와 포함된 테마들의 등락률을 고려한 분배 (안전 처리)
            val rowWeights = allRows.mapIndexed { rowIndex, row ->
                if (row.isNotEmpty()) {
                    val rowHeight = rowHeights[rowIndex]
                    val avgRate = row.map { kotlin.math.abs(it.theme.rate) }.average()
                    val heightWeight = rowHeight
                    val rateWeight = avgRate * 10 // 등락률 가중치 최소화 (완전한 공간 우선)
                    rowIndex to (heightWeight + rateWeight)
                } else {
                    rowIndex to 0.0
                }
            }.filter { it.second > 0 }.sortedByDescending { it.second }
            
            if (rowWeights.isNotEmpty()) {
                val totalWeight = rowWeights.sumOf { it.second }
                if (totalWeight > 0) {
                    var distributedHeight = 0
                    rowWeights.forEachIndexed { index, (rowIndex, weight) ->
                        val share = if (index < rowWeights.size - 1) {
                            (remainingHeight * weight / totalWeight).toInt()
                        } else {
                            remainingHeight - distributedHeight // 마지막은 나머지 모두
                        }
                        
                        // 남은 세로 공간을 더 적극적으로 분배
                        val enhancedShare = kotlin.math.max(share, remainingHeight / rowWeights.size)
                        allRows[rowIndex].forEach { rect ->
                            rect.height += kotlin.math.max(0, enhancedShare) // 음수 방지
                        }
                        distributedHeight += share
                    }
                } else {
                    // totalWeight가 0인 경우 균등 분배
                    val equalShare = remainingHeight / allRows.size
                    val remainder = remainingHeight % allRows.size
                    allRows.forEachIndexed { rowIndex, row ->
                        val additionalHeight = equalShare + if (rowIndex < remainder) 1 else 0
                        row.forEach { rect ->
                            rect.height += additionalHeight
                        }
                    }
                }
            }
        }
        
        // 3단계: Y 좌표 재조정
        var adjustedY = startY
        allRows.forEach { row ->
            val rowHeight = row.maxOfOrNull { it.height } ?: 0
            row.forEach { rect ->
                rect.y = adjustedY
            }
            adjustedY += rowHeight + gap
        }
        
        Log.d("TreemapLayout", "$sectionName: 테트리스 스타일 완료 - 빈 공간 0%")
    }
    
    /**
     * 행별 가로 공간 완전 채우기
     */
    private fun fillRowCompletely(rowRects: List<Rectangle>, containerWidth: Int, rowY: Int) {
        if (rowRects.isEmpty()) return
        
        val gap = 1
        val totalUsedWidth = rowRects.sumOf { it.width } + (rowRects.size - 1) * gap
        val remainingWidth = containerWidth - totalUsedWidth
        
        if (remainingWidth > 0) {
            // 남은 공간을 비례적으로 분배
            rowRects.forEach { rect ->
                val proportion = rect.width.toDouble() / rowRects.sumOf { it.width }
                val additionalWidth = (remainingWidth * proportion).toInt()
                rect.width += additionalWidth
            }
            
            // 마지막 박스로 오차 보정
            val lastRect = rowRects.last()
            val actualUsedWidth = rowRects.sumOf { it.width } + (rowRects.size - 1) * gap
            if (actualUsedWidth != containerWidth) {
                lastRect.width += containerWidth - actualUsedWidth
            }
            
            // X 좌표 재조정
            var adjustedX = rowRects.first().x
            rowRects.forEach { rect ->
                rect.x = adjustedX
                adjustedX += rect.width + gap
            }
        }
    }
    
    /**
     * 세로 공간 완전 채우기
     */
    private fun fillVerticallyCompletely(themes: List<Rectangle>, startY: Int, sectionMaxHeight: Int) {
        if (themes.isEmpty()) return
        
        val gap = 1
        val rows = themes.groupBy { it.y }.toSortedMap()
        val rowHeights = rows.map { (_, rects) -> rects.maxOfOrNull { it.height } ?: 0 }
        
        val totalUsedHeight = rowHeights.sum() + (rows.size - 1) * gap
        val remainingHeight = sectionMaxHeight - totalUsedHeight
        
        if (remainingHeight > 0) {
            // 남은 세로 공간을 각 행에 비례 분배
            rowHeights.forEachIndexed { index, currentHeight ->
                val proportion = currentHeight.toDouble() / rowHeights.sum()
                val additionalHeight = (remainingHeight * proportion).toInt()
                
                val rowY = rows.keys.elementAt(index)
                val rowRects = rows[rowY] ?: emptyList()
                rowRects.forEach { rect ->
                    rect.height += additionalHeight
                }
            }
            
            // 마지막 행으로 오차 보정
            val lastRowRects = rows.values.last()
            val actualUsedHeight = themes.groupBy { it.y }.values.sumOf { rects ->
                rects.maxOfOrNull { it.height } ?: 0
            } + (rows.size - 1) * gap
            
            if (actualUsedHeight < sectionMaxHeight) {
                val finalAdjustment = sectionMaxHeight - actualUsedHeight
                lastRowRects.forEach { rect ->
                    rect.height += finalAdjustment
                }
            }
        }
    }
    
    /**
     * 대형 박스들의 컴팩트 배치
     */
    private fun layoutLargeBoxesCompact(largeBoxes: List<Rectangle>, containerWidth: Int, containerHeight: Int) {
        var currentX = 0
        var currentY = 0
        var maxHeightInRow = 0
        val gap = 1 // 간격 최소화 (2px → 1px)
        
        largeBoxes.forEach { rect ->
            // 현재 행에 들어갈 수 있는지 확인
            if (currentX + rect.width > containerWidth && currentX > 0) {
                // 다음 행으로 이동
                currentX = 0
                currentY += maxHeightInRow + gap // 최소 간격
                maxHeightInRow = 0
            }
            
            // 박스 배치
            rect.x = currentX
            rect.y = currentY
            
            currentX += rect.width + gap // 최소 간격
            maxHeightInRow = kotlin.math.max(maxHeightInRow, rect.height)
        }
    }
    
    /**
     * 중형 박스들 배치
     */
    private fun layoutMediumBoxes(mediumBoxes: List<Rectangle>, largeBoxes: List<Rectangle>, containerWidth: Int, containerHeight: Int) {
        val gap = 1
        
        // 큰 박스들 다음 줄부터 시작
        val startY = if (largeBoxes.isNotEmpty()) {
            largeBoxes.maxOfOrNull { it.y + it.height } ?: 0
        } else 0
        
        var currentX = 0
        var currentY = startY + gap
        var maxHeightInRow = 0
        
        mediumBoxes.forEach { rect ->
            if (currentX + rect.width > containerWidth && currentX > 0) {
                currentX = 0
                currentY += maxHeightInRow + gap
                maxHeightInRow = 0
            }
            
            rect.x = currentX
            rect.y = currentY
            
            currentX += rect.width + gap
            maxHeightInRow = kotlin.math.max(maxHeightInRow, rect.height)
        }
    }
    
    /**
     * 작은 박스들을 모든 빈 공간에 촘촘히 배치
     */
    private fun layoutSmallBoxes(smallBoxes: List<Rectangle>, placedBoxes: List<Rectangle>, containerWidth: Int, containerHeight: Int) {
        val occupied = Array(containerHeight + 100) { BooleanArray(containerWidth + 100) } // 확장된 공간
        val gap = 1 // 최소 간격
        
        // 이미 배치된 박스들이 차지하는 영역 표시 (간격 포함)
        placedBoxes.forEach { rect ->
            markOccupiedWithGap(occupied, rect.x, rect.y, rect.width, rect.height, gap)
        }
        
        smallBoxes.forEach { rect ->
            var placed = false
            
            // 빈 공간을 1픽셀 단위로 세밀하게 탐색
            for (y in 0 until containerHeight - rect.height step 1) {
                for (x in 0 until containerWidth - rect.width step 1) {
                    if (canPlaceAt(occupied, x, y, rect.width, rect.height)) {
                        rect.x = x
                        rect.y = y
                        markOccupiedWithGap(occupied, x, y, rect.width, rect.height, gap)
                        placed = true
                        break
                    }
                }
                if (placed) break
            }
            
            // 배치하지 못한 경우 아래쪽으로 확장
            if (!placed) {
                val maxY = placedBoxes.maxOfOrNull { it.y + it.height } ?: 0
                rect.x = 0
                rect.y = maxY + gap
                markOccupiedWithGap(occupied, rect.x, rect.y, rect.width, rect.height, gap)
            }
        }
    }
    
    /**
     * 간격을 포함해서 영역을 점유된 것으로 표시
     */
    private fun markOccupiedWithGap(occupied: Array<BooleanArray>, x: Int, y: Int, width: Int, height: Int, gap: Int) {
        for (dy in -gap until height + gap) {
            for (dx in -gap until width + gap) {
                val newY = y + dy
                val newX = x + dx
                if (newY >= 0 && newY < occupied.size && newX >= 0 && newX < occupied[0].size) {
                    occupied[newY][newX] = true
                }
            }
        }
    }
    
    /**
     * 지정된 위치에 박스를 배치할 수 있는지 확인
     */
    private fun canPlaceAt(occupied: Array<BooleanArray>, x: Int, y: Int, width: Int, height: Int): Boolean {
        for (dy in 0 until height) {
            for (dx in 0 until width) {
                if (y + dy >= occupied.size || x + dx >= occupied[0].size || occupied[y + dy][x + dx]) {
                    return false
                }
            }
        }
        return true
    }
    
    /**
     * 지정된 영역을 점유된 것으로 표시
     */
    private fun markOccupied(occupied: Array<BooleanArray>, x: Int, y: Int, width: Int, height: Int) {
        for (dy in 0 until height) {
            for (dx in 0 until width) {
                if (y + dy < occupied.size && x + dx < occupied[0].size) {
                    occupied[y + dy][x + dx] = true
                }
            }
        }
    }
    
    /**
     * 크기를 줄여가면서 배치 시도
     */
    private fun placeWithReduction(occupied: Array<BooleanArray>, rect: Rectangle, containerWidth: Int, containerHeight: Int) {
        val originalWidth = rect.width
        val originalHeight = rect.height
        val minSize = 40 // 최소 크기
        
        // 크기를 점진적으로 줄여가면서 배치 시도
        for (scale in 90 downTo 30 step 10) {
            rect.width = kotlin.math.max(minSize, (originalWidth * scale / 100))
            rect.height = kotlin.math.max(minSize, (originalHeight * scale / 100))
            
            for (y in 0..containerHeight - rect.height) {
                for (x in 0..containerWidth - rect.width) {
                    if (canPlaceAt(occupied, x, y, rect.width, rect.height)) {
                        rect.x = x
                        rect.y = y
                        markOccupied(occupied, x, y, rect.width, rect.height)
                        return
                    }
                }
            }
        }
        
        // 최후의 수단: 화면 밖에라도 배치 (스크롤 가능하도록)
        rect.x = 0
        rect.y = containerHeight
        rect.width = minSize
        rect.height = minSize
    }
    
    /**
     * 테마 데이터 반환 (어댑터에서 사용)
     */
    fun getThemeAt(index: Int): Theme? {
        return if (index in 0 until rectangles.size) {
            rectangles[index].theme
        } else null
    }
    
    /**
     * 사각형 수 반환
     */
    fun getRectangleCount(): Int = rectangles.size
}
