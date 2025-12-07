package com.example.antwinner_kotlin.ui.stockdetail

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.antwinner_kotlin.ui.stockdetail.fragments.ChartFragment
import com.example.antwinner_kotlin.ui.stockdetail.fragments.StockInfoFragment
import com.example.antwinner_kotlin.ui.stockdetail.fragments.WhyRiseFragment

class MainTabPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val stockCode: String,
    private val stockName: String
) : FragmentStateAdapter(fragmentActivity) {
    
    companion object {
        private const val TAG = "MainTabPagerAdapter"
    }
    
    override fun getItemCount(): Int = 3
    
    override fun createFragment(position: Int): Fragment {
        Log.d(TAG, "Creating fragment for position: $position")
        return try {
            when (position) {
                0 -> {
                    Log.d(TAG, "Creating ChartFragment")
                    ChartFragment().apply {
                        arguments = Bundle().apply {
                            putString("stock_code", stockCode)
                            putString("stock_name", stockName)
                        }
                    }
                }
                1 -> {
                    Log.d(TAG, "Creating WhyRiseFragment")
                    WhyRiseFragment.newInstance(stockCode)
                }
                2 -> {
                    Log.d(TAG, "Creating StockInfoFragment with stockName: $stockName")
                    try {
                        StockInfoFragment.newInstance(stockName)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error creating StockInfoFragment, creating fallback", e)
                        // 기본 Fragment 반환
                        StockInfoFragment.newInstance("기본")
                    }
                }
                else -> {
                    Log.e(TAG, "Invalid position: $position")
                    throw IllegalArgumentException("Invalid position: $position")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating fragment for position $position", e)
            throw e
        }
    }
} 