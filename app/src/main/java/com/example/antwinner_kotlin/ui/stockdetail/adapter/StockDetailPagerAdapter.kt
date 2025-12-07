package com.example.antwinner_kotlin.ui.stockdetail.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.antwinner_kotlin.ui.stockdetail.fragments.*

class StockDetailPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BasicInfoFragment()
            1 -> InvestorsFragment()
            2 -> NewsFragment()
            3 -> DisclosureFragment()
            4 -> FinanceFragment()
            else -> BasicInfoFragment()
        }
    }
} 