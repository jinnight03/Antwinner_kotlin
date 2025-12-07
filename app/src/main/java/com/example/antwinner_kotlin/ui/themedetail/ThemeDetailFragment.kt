package com.example.antwinner_kotlin.ui.themedetail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
// import androidx.navigation.fragment.findNavController // 필요 없어짐
import com.example.antwinner_kotlin.databinding.FragmentThemeDetailBinding
import timber.log.Timber


class ThemeDetailFragment : Fragment() {

    private var _binding: FragmentThemeDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ThemeDetailViewModel by viewModels()
    private lateinit var themeDetailAdapter: ThemeDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("ThemeDetailFragment onViewCreated")

        setupRecyclerView()
        setupSwipeToRefresh()
        observeViewModel()
        // setupBackButton() // 호출 제거
    }

    private fun setupRecyclerView() {
        themeDetailAdapter = ThemeDetailAdapter(requireContext()) // Context 전달
        
        // 테마 클릭 리스너 설정 (ThemeDetailActivity로 이동)
        themeDetailAdapter.setOnThemeClickListener { theme ->
            val intent = com.example.antwinner_kotlin.ui.themedetail.ThemeDetailActivity.newIntent(
                requireContext(),
                theme.thema, // 테마 ID로 테마명 사용
                theme.thema  // 테마명
            )
            startActivity(intent)
        }
        
        // 종목 클릭 리스너 설정 (StockDetailActivity로 이동)
        themeDetailAdapter.setOnStockClickListener { company ->
            // 종목명 정리해서 전달
            val cleanStockName = company.stockName.trim().replace(Regex("\\s+"), " ")
            
            val intent = com.example.antwinner_kotlin.ui.stockdetail.StockDetailActivity.newIntent(
                requireContext(),
                cleanStockName,
                "" // 종목코드가 CompanyData에 없어서 빈 문자열 사용
            )
            startActivity(intent)
        }
        
        binding.rvThemeList.apply {
            adapter = themeDetailAdapter
            // LinearLayoutManager는 XML에서 설정했으므로 생략 가능
            // addItemDecoration(...) // 필요시 구분선 등 추가
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            Timber.d("Swipe to refresh triggered")
            viewModel.fetchThemeDetails() // 데이터 새로고침
        }
        // 초기 로딩 시에도 SwipeRefreshLayout 인디케이터를 사용하려면 여기서 isRefreshing = true 설정 가능
        // viewModel.isLoading.value?.let { binding.swipeRefreshLayout.isRefreshing = it }
    }

    private fun observeViewModel() {
        viewModel.themeDetails.observe(viewLifecycleOwner) { themes ->
            Timber.d("Observed ${themes.size} themes")
            themeDetailAdapter.submitList(themes)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Timber.d("isLoading: $isLoading")
            // 로딩 상태에 따라 SwipeRefreshLayout 인디케이터 표시/숨김
            if (!isLoading) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
            // 초기 로딩 시 인디케이터를 보여주려면 여기서 true일 때 설정할 수도 있음
            // binding.swipeRefreshLayout.isRefreshing = isLoading 
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Timber.e("Error: $it")
                binding.swipeRefreshLayout.isRefreshing = false // 에러 발생 시에도 인디케이터 숨김
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }

        viewModel.updateTime.observe(viewLifecycleOwner) { time ->
             binding.tvUpdateTime.text = time
        }
    }

    // setupBackButton 함수 제거
    /*
    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            // findNavController().navigateUp() // Navigation Component 사용 시
            requireActivity().onBackPressedDispatcher.onBackPressed() // Activity의 기본 뒤로가기 동작 사용
        }
    }
    */



    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvThemeList.adapter = null // 어댑터 참조 해제 (메모리 누수 방지)
        _binding = null
        Timber.d("ThemeDetailFragment onDestroyView")
    }
} 