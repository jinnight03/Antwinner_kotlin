package com.example.antwinner_kotlin.ui.themeschedule

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class SchedulePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SchedulePageFragment.newInstance(ScheduleType.ISSUE)
            1 -> SchedulePageFragment.newInstance(ScheduleType.SUBSCRIPTION)
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
} 