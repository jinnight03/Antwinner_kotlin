package com.example.antwinner_kotlin.ui.home.layout

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.ui.home.adapter.ThemeAdapter
import com.example.antwinner_kotlin.R
import android.util.Log
import kotlin.math.abs

/**
 * 테마 맵 그리드 레이아웃 매니저
 * - 균일한 그리드 레이아웃 및 일관된 여백 제공
 */
class ThemeGridLayoutManager(
    context: Context,
    spanCount: Int,
    private val adapter: ThemeAdapter
) : GridLayoutManager(context, spanCount) {

    // 그리드 아이템 간의 고정 간격 (모든 방향에 일관된 간격 적용)
    private val spacing = context.resources.getDimensionPixelSize(R.dimen.grid_small_spacing)

    init {
        this.orientation = VERTICAL
        
        // 등락률에 따른 span size 설정 (트리맵 효과)
        spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                try {
                    Log.d("ThemeGridLayoutManager", "스팬 크기 계산 시도 position: $position, 어댑터 아이템 수: ${adapter.itemCount}")
                    
                    if (position >= adapter.itemCount) {
                        Log.d("ThemeGridLayoutManager", "범위 초과: position($position) >= itemCount(${adapter.itemCount})")
                        return 1 // 범위를 벗어나면 기본값 반환
                    }
                    
                    val theme = adapter.getThemeAt(position)
                    Log.d("ThemeGridLayoutManager", "테마: ${theme.name}, 등락률: ${theme.rate}%, 스팬 크기: 1 (고정)")
                    
                    // 모든 아이템이 동일한 그리드 셀을 차지하도록 1로 고정
                    return 1
                } catch (e: Exception) {
                    Log.e("ThemeGridLayoutManager", "getSpanSize 오류: ${e.message}")
                    return 1 // 오류 발생 시 기본값 반환
                }
            }
        }
    }

    /**
     * 균일한 그리드 간격을 위한 커스텀 아이템 데코레이션
     */
    inner class SpacingItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            
            // 더 작은 간격 적용 (2픽셀)
            val smallSpacing = 2
            
            // 모든 방향에 작은 간격 적용
            outRect.left = smallSpacing
            outRect.right = smallSpacing
            outRect.top = smallSpacing
            outRect.bottom = smallSpacing
        }
    }

    // 아이템 애니메이션 중에도 레이아웃이 변경되지 않도록 설정
    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
    
    // 레이아웃 재계산 중 예외 처리
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Log.e("ThemeGridLayoutManager", "Error during layout", e)
        }
    }
    
    // 데코레이션 메서드 - 모든 방향에 동일한 간격 적용
    override fun getLeftDecorationWidth(child: View): Int {
        return spacing / 2
    }
    
    override fun getRightDecorationWidth(child: View): Int {
        return spacing / 2
    }
    
    override fun getTopDecorationHeight(child: View): Int {
        return spacing / 2
    }
    
    override fun getBottomDecorationHeight(child: View): Int {
        return spacing / 2
    }
    
    // 항목 레이아웃 조정
    override fun layoutDecoratedWithMargins(
        child: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.layoutDecoratedWithMargins(child, left, top, right, bottom)
        
        // 아이템의 라운드 코너를 위한 클리핑 적용
        child.clipToOutline = true
    }
    
    /**
     * RecyclerView에 이 ItemDecoration 인스턴스 추가 메서드
     */
    fun addSpacingDecoration(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(SpacingItemDecoration())
    }
} 