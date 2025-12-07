package com.example.antwinner_kotlin.ui.home.transformer

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class DepthPageTransformer : ViewPager2.PageTransformer {

    private val MIN_SCALE = 0.75f

    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            when {
                position < -1 -> { // [-Infinity,-1)
                    // 화면 왼쪽 바깥에 있는 페이지
                    alpha = 0f
                }
                position <= 0 -> { // [-1,0]
                    // 현재 페이지에서 왼쪽으로 이동하는 페이지
                    alpha = 1f
                    translationX = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
                position <= 1 -> { // (0,1]
                    // 현재 페이지에서 오른쪽으로 이동하는 페이지
                    alpha = 1 - position
                    translationX = pageWidth * -position
                    // 페이지가 이동할수록 축소
                    val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position))
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                else -> { // (1,+Infinity]
                    // 화면 오른쪽 바깥에 있는 페이지
                    alpha = 0f
                }
            }
        }
    }
} 