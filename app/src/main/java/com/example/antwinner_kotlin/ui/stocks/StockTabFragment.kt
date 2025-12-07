package com.example.antwinner_kotlin.ui.stocks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.repository.StockRepository
import com.example.antwinner_kotlin.ui.stockdetail.StockDetailActivity
import com.example.antwinner_kotlin.ui.stocks.adapter.StockAdapter
import com.example.antwinner_kotlin.ui.stocks.model.IStockItem
import com.example.antwinner_kotlin.util.NetworkUtil
import kotlinx.coroutines.launch

class StockTabFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var stockAdapter: StockAdapter
    
    private val stockRepository = StockRepository()
    private var tabIndex: Int = 0

    companion object {
        private const val ARG_TAB_INDEX = "tab_index"
        
        fun newInstance(tabIndex: Int): StockTabFragment {
            val fragment = StockTabFragment()
            val args = Bundle().apply {
                putInt(ARG_TAB_INDEX, tabIndex)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tabIndex = it.getInt(ARG_TAB_INDEX, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stock_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.rv_stocks)
        emptyView = view.findViewById(R.id.tv_empty)
        
        setupRecyclerView()
        loadStockData()
    }
    
    private fun setupRecyclerView() {
        // 초기에 빈 데이터로 어댑터 설정
        stockAdapter = StockAdapter(emptyList()) { stock ->
            // --- 주식 아이템 클릭 시 처리: Kotlin StockDetailActivity 호출 --- 
            Log.d("StockTabFragment", "Stock item clicked: Name=${stock.name}, Code=${stock.code}")
            
            // 1. 클릭된 아이템에서 종목명과 코드를 가져옵니다.
            val stockName = stock.name
            val stockCode = stock.code

            // 2. Kotlin StockDetailActivity를 시작하는 Intent 생성
            val intent = Intent(requireContext(), StockDetailActivity::class.java).apply {
                putExtra(StockDetailActivity.EXTRA_STOCK_NAME, stockName)
                putExtra(StockDetailActivity.EXTRA_STOCK_CODE, stockCode)
            }

            // 3. Activity를 시작합니다.
            try {
                startActivity(intent)
            } catch (e: Exception) {
                 // ActivityNotFoundException 등 처리 (필요 시)
                Log.e("StockTabFragment", "Error launching StockDetailActivity", e)
                Toast.makeText(context, "종목 상세 화면을 여는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
            // --- Kotlin StockDetailActivity 호출 끝 --- 
        }
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = stockAdapter
        recyclerView.setHasFixedSize(true)
    }
    
    private fun loadStockData() {
        // 인터넷 연결 확인
        if (context?.let { NetworkUtil.isNetworkAvailable(it) } != true) {
            Log.d("StockTabFragment", "No network connection, using dummy data")
            val dummyData = stockRepository.getDummyStockData(tabIndex)
            updateUI(dummyData)
            return
        }
        
        // API 호출
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val stocks = stockRepository.getStocksByTabIndex(tabIndex)
                Log.d("StockTabFragment", "Loaded ${stocks.size} stocks for tab $tabIndex")
                updateUI(stocks)
            } catch (e: Exception) {
                Log.e("StockTabFragment", "Error loading stocks for tab $tabIndex", e)
                val dummyData = stockRepository.getDummyStockData(tabIndex)
                updateUI(dummyData)
            }
        }
    }
    
    private fun updateUI(stocks: List<IStockItem>) {
        // 데이터가 없으면 빈 화면 표시
        if (stocks.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            stockAdapter.updateStocks(stocks)
        }
    }
} 