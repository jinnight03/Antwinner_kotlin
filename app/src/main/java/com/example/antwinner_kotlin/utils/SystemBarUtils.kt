package com.example.antwinner_kotlin.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

/**
 * 시스템 바(상태바, 네비게이션 바) 관련 유틸리티 클래스
 * 동적으로 상단 여백을 처리하여 시스템 바와의 겹침 문제를 해결합니다.
 */
object SystemBarUtils {
    
    /**
     * Activity의 루트 뷰에 시스템 바 인셋을 적용합니다.
     * @param activity 대상 Activity
     * @param rootView 루트 뷰 (일반적으로 최상위 레이아웃)
     * @param applyTopInset 상단 인셋 적용 여부 (기본값: true)
     * @param applyBottomInset 하단 인셋 적용 여부 (기본값: false)
     */
    fun applySystemBarInsets(
        activity: Activity,
        rootView: View,
        applyTopInset: Boolean = true,
        applyBottomInset: Boolean = false
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            
            if (applyTopInset) {
                layoutParams.topMargin = systemBars.top
            }
            
            if (applyBottomInset) {
                layoutParams.bottomMargin = systemBars.bottom
            }
            
            view.layoutParams = layoutParams
            insets
        }
    }
    
    /**
     * Fragment의 루트 뷰에 시스템 바 인셋을 적용합니다.
     * @param fragment 대상 Fragment
     * @param rootView 루트 뷰
     * @param applyTopInset 상단 인셋 적용 여부 (기본값: true)
     * @param applyBottomInset 하단 인셋 적용 여부 (기본값: false)
     */
    fun applySystemBarInsets(
        fragment: Fragment,
        rootView: View,
        applyTopInset: Boolean = true,
        applyBottomInset: Boolean = false
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams
                ?: return@setOnApplyWindowInsetsListener insets
            
            if (applyTopInset) {
                layoutParams.topMargin = systemBars.top
            }
            
            if (applyBottomInset) {
                layoutParams.bottomMargin = systemBars.bottom
            }
            
            view.layoutParams = layoutParams
            insets
        }
    }
    
    /**
     * 특정 뷰에 상단 패딩을 동적으로 적용합니다.
     * @param view 대상 뷰
     * @param additionalPadding 추가 패딩 (dp 단위, 기본값: 0)
     */
    fun applyTopPaddingInset(view: View, additionalPadding: Int = 0) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val additionalPaddingPx = (additionalPadding * view.context.resources.displayMetrics.density).toInt()
            
            v.setPadding(
                v.paddingLeft,
                systemBars.top + additionalPaddingPx,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }
    }
    
    /**
     * AppBarLayout이나 Toolbar에 상단 패딩을 동적으로 적용합니다.
     * @param view 대상 뷰 (AppBarLayout 또는 Toolbar)
     * @param additionalPadding 추가 패딩 (dp 단위, 기본값: 16)
     */
    fun applyTopPaddingForAppBar(view: View, additionalPadding: Int = 16) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val additionalPaddingPx = (additionalPadding * view.context.resources.displayMetrics.density).toInt()
            
            v.setPadding(
                v.paddingLeft,
                systemBars.top + additionalPaddingPx,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }
    }
    
    /**
     * 특정 뷰의 상단 마진을 동적으로 적용합니다.
     * @param view 대상 뷰
     * @param additionalMargin 추가 마진 (dp 단위, 기본값: 0)
     */
    fun applyTopMarginInset(view: View, additionalMargin: Int = 0) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val additionalMarginPx = (additionalMargin * view.context.resources.displayMetrics.density).toInt()
            
            val layoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = systemBars.top + additionalMarginPx
            v.layoutParams = layoutParams
            
            insets
        }
    }
    
    /**
     * 시스템 바 높이를 가져옵니다.
     * @param view 뷰 (WindowInsets를 가져오기 위해 필요)
     * @return 시스템 바 높이 (px 단위)
     */
    fun getSystemBarHeight(view: View): Int {
        val insets = ViewCompat.getRootWindowInsets(view)
        return insets?.getInsets(WindowInsetsCompat.Type.systemBars())?.top ?: 0
    }
}
