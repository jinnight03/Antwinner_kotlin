package com.example.antwinner_kotlin.ui.stockdetail.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.antwinner_kotlin.R

class BasicInfoFragment : Fragment() {

    companion object {
        fun newInstance(stockSymbol: String): BasicInfoFragment {
            return BasicInfoFragment().apply {
                arguments = Bundle().apply {
                    putString("stock_symbol", stockSymbol)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_basic_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 뷰 초기화 및 데이터 로드
    }
} 