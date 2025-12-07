package com.example.antwinner_kotlin.ui.themeschedule

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.antwinner_kotlin.databinding.FragmentSchedulePageBinding

class SchedulePageFragment : Fragment() {

    private var _binding: FragmentSchedulePageBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ThemeScheduleViewModel
    private lateinit var themeScheduleAdapter: ThemeScheduleAdapter
    private lateinit var subscriptionScheduleAdapter: SubscriptionScheduleAdapter
    private var scheduleType: ScheduleType = ScheduleType.ISSUE

    companion object {
        private const val ARG_SCHEDULE_TYPE = "schedule_type"

        fun newInstance(scheduleType: ScheduleType): SchedulePageFragment {
            val fragment = SchedulePageFragment()
            val args = Bundle()
            args.putSerializable(ARG_SCHEDULE_TYPE, scheduleType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleType = arguments?.getSerializable(ARG_SCHEDULE_TYPE) as? ScheduleType ?: ScheduleType.ISSUE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireParentFragment())[ThemeScheduleViewModel::class.java]
        _binding = FragmentSchedulePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        when (scheduleType) {
            ScheduleType.ISSUE -> {
                themeScheduleAdapter = ThemeScheduleAdapter { item ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link))
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "링크를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                binding.recyclerView.adapter = themeScheduleAdapter
            }
            ScheduleType.SUBSCRIPTION -> {
                subscriptionScheduleAdapter = SubscriptionScheduleAdapter()
                binding.recyclerView.adapter = subscriptionScheduleAdapter
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun observeViewModel() {
        when (scheduleType) {
            ScheduleType.ISSUE -> {
                viewModel.scheduleItems.observe(viewLifecycleOwner) { items ->
                    if (::themeScheduleAdapter.isInitialized) {
                        themeScheduleAdapter.submitList(items)
                    }
                }
            }
            ScheduleType.SUBSCRIPTION -> {
                viewModel.subscriptionItems.observe(viewLifecycleOwner) { items ->
                    if (::subscriptionScheduleAdapter.isInitialized) {
                        subscriptionScheduleAdapter.submitList(items)
                    }
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.recyclerView.isVisible = !isLoading && viewModel.error.value == null
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