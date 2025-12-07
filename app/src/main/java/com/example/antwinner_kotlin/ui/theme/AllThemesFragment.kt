package com.example.antwinner_kotlin.ui.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.model.ThemeItem
import com.example.antwinner_kotlin.network.ApiService
import com.example.antwinner_kotlin.network.RetrofitClient
import com.example.antwinner_kotlin.ui.theme.adapter.AllThemesGridAdapter
import com.example.antwinner_kotlin.util.NetworkUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class AllThemesFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val adapter = AllThemesGridAdapter(emptyList())
    private val apiService = RetrofitClient.apiService
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_themes, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.rv_all_themes)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        
        // 그리드 레이아웃 설정 (2열)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter
        
        // 새로고침 설정
        swipeRefreshLayout.setOnRefreshListener {
            loadThemes()
        }
        
        // 초기 데이터 로드
        loadThemes()
    }
    
    private fun loadThemes() {
        if (NetworkUtil.isNetworkAvailable(requireContext())) {
            fetchThemesFromApi()
        } else {
            // 네트워크 연결이 없을 경우 더미 데이터 표시
            showDummyData()
            swipeRefreshLayout.isRefreshing = false
            Toast.makeText(
                requireContext(),
                getString(R.string.network_error_message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun fetchThemesFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getThemeFluctuations("1d")
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val themes = response.body()!!.data
                        adapter.updateData(themes)
                        Timber.d("테마 데이터 로드 성공: ${themes.size}개")
                    } else {
                        Timber.e("테마 데이터 로드 실패: ${response.code()}")
                        showDummyData()
                    }
                    swipeRefreshLayout.isRefreshing = false
                }
            } catch (e: Exception) {
                Timber.e(e, "테마 데이터 로드 중 오류 발생")
                withContext(Dispatchers.Main) {
                    showDummyData()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }
    
    private fun showDummyData() {
        // 더미 데이터 생성
        val dummyThemes = listOf(
            ThemeItem(id = "semiconductor", name = "반도체", rate = "3.45"),
            ThemeItem(id = "battery", name = "2차전지", rate = "2.87"),
            ThemeItem(id = "ai", name = "인공지능", rate = "1.92"),
            ThemeItem(id = "metaverse", name = "메타버스", rate = "-0.75"),
            ThemeItem(id = "robot", name = "로봇", rate = "0.83"),
            ThemeItem(id = "self_driving", name = "자율주행", rate = "-1.24"),
            ThemeItem(id = "blockchain", name = "블록체인", rate = "4.16"),
            ThemeItem(id = "game", name = "게임", rate = "1.53"),
            ThemeItem(id = "bio", name = "바이오", rate = "-0.43"),
            ThemeItem(id = "aviation", name = "항공", rate = "2.31"),
            ThemeItem(id = "shipbuilding", name = "조선", rate = "0.67"),
            ThemeItem(id = "defense", name = "방산", rate = "3.79")
        )
        adapter.updateData(dummyThemes)
    }
    
    companion object {
        fun newInstance(): AllThemesFragment {
            return AllThemesFragment()
        }
    }
} 