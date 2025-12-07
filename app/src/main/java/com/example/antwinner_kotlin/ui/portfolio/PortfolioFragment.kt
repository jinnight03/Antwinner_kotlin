package com.example.antwinner_kotlin.ui.portfolio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.utils.SystemBarUtils

class PortfolioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 임시로 stock_tab 레이아웃 사용
        return inflater.inflate(R.layout.fragment_stock_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 시스템 바 인셋 적용
        SystemBarUtils.applyTopPaddingInset(view, 16)
    }
} 