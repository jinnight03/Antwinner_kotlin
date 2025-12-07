package com.example.antwinner_kotlin.ui.themeschedule

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.antwinner_kotlin.databinding.FragmentThemeScheduleBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.antwinner_kotlin.ui.themeschedule.SubscriptionScheduleAdapter
import com.example.antwinner_kotlin.utils.SystemBarUtils

class ThemeScheduleFragment : Fragment() {

    private var _binding: FragmentThemeScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ThemeScheduleViewModel
    private lateinit var pagerAdapter: SchedulePagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[ThemeScheduleViewModel::class.java]
        _binding = FragmentThemeScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSystemBars()
        setupViewPager()
        observeViewModel()
    }

    private fun setupSystemBars() {
        // 시스템 바 인셋 처리 (노치, 상태바 등)
        SystemBarUtils.applyTopMarginInset(binding.tabLayoutScheduleType, 16)
    }

    private fun setupViewPager() {
        // ViewPager2 어댑터 설정
        pagerAdapter = SchedulePagerAdapter(this)
        binding.viewPagerSchedule.adapter = pagerAdapter

        // TabLayout과 ViewPager2 연결
        TabLayoutMediator(binding.tabLayoutScheduleType, binding.viewPagerSchedule) { tab, position ->
            tab.text = when (position) {
                0 -> "이슈일정"
                1 -> "청약일정"
                else -> ""
            }
        }.attach()

        // ViewPager2 페이지 변경 시 ViewModel에 알림
        binding.viewPagerSchedule.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val scheduleType = when (position) {
                    0 -> ScheduleType.ISSUE
                    1 -> ScheduleType.SUBSCRIPTION
                    else -> ScheduleType.ISSUE
                }
                viewModel.setCurrentTab(scheduleType)
            }
        })
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 