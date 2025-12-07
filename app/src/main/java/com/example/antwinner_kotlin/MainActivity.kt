package com.example.antwinner_kotlin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.antwinner_kotlin.utils.AppUpdateManager

class MainActivity : AppCompatActivity() {
    
    private lateinit var appUpdateManager: AppUpdateManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 상태바를 투명하게 설정하고 전체 화면 모드로 설정
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        
        setContentView(R.layout.activity_main)
        
        // 업데이트 매니저 초기화
        initializeUpdateManager()
        
        setupNavigation()
        
        // 위젯에서 호출된 경우 HomeFragment로 이동
        handleWidgetIntent()
        
        // 앱 업데이트 확인 (네비게이션 설정 후에 실행)
        checkForAppUpdates()
    }
    
    private fun handleWidgetIntent() {
        val navigateTo = intent?.getStringExtra("navigate_to")
        when (navigateTo) {
            "home" -> {
                Log.d("MainActivity", "Navigating to HomeFragment from widget")
                // HomeFragment는 기본 화면이므로 특별한 처리 불필요
                // 필요시 여기서 특정 탭으로 이동하는 로직 추가 가능
            }
            "theme_detail" -> {
                Log.d("MainActivity", "Navigating to ThemeDetailFragment from AllThemesActivity")
                navigateToThemeDetail()
            }
        }
    }
    
    private fun navigateToThemeDetail() {
        try {
            // NavHostFragment를 안전하게 찾는 방법
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
                
            if (navHostFragment != null) {
                val navController = navHostFragment.navController
                
                // ThemeDetailFragment로 이동 (bottom navigation의 theme_detail 탭)
                navController.navigate(R.id.navigation_theme_detail)
                
                // BottomNavigationView에서 해당 탭 선택
                val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
                navView.selectedItemId = R.id.navigation_theme_detail
                
                Log.d("MainActivity", "Successfully navigated to ThemeDetailFragment")
            } else {
                Log.e("MainActivity", "NavHostFragment not found for theme detail navigation")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error navigating to theme detail: ${e.message}", e)
        }
    }
    
    private fun setupNavigation() {
        try {
            // NavHostFragment를 안전하게 찾는 방법
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
                
            if (navHostFragment != null) {
                val navController = navHostFragment.navController
                val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
                navView.setupWithNavController(navController)
            } else {
                Log.e("MainActivity", "NavHostFragment not found")
                Toast.makeText(this, "네비게이션 초기화 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // 오류 발생 시 로그만 출력
            Log.e("MainActivity", "Error initializing navigation: ${e.message}", e)
            Toast.makeText(this, "네비게이션 초기화 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 업데이트 매니저를 초기화합니다.
     */
    private fun initializeUpdateManager() {
        try {
            appUpdateManager = AppUpdateManager(this)
            Log.d("MainActivity", "AppUpdateManager 초기화 완료")
        } catch (e: Exception) {
            Log.e("MainActivity", "AppUpdateManager 초기화 실패: ${e.message}", e)
        }
    }
    
    /**
     * 앱 업데이트를 확인합니다.
     */
    private fun checkForAppUpdates() {
        try {
            Log.d("MainActivity", "앱 업데이트 확인 시작")
            
            // 진행 중인 업데이트가 있는지 먼저 확인
            appUpdateManager.checkForInProgressUpdate()
            
            // 새로운 업데이트가 있는지 확인 (약간의 지연을 두어 UI 로딩 완료 후 실행)
            window.decorView.postDelayed({
                appUpdateManager.checkForUpdate()
            }, 1000) // 1초 지연
            
        } catch (e: Exception) {
            Log.e("MainActivity", "앱 업데이트 확인 중 오류: ${e.message}", e)
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // 앱이 다시 활성화될 때 진행 중인 업데이트 확인
        try {
            if (::appUpdateManager.isInitialized) {
                appUpdateManager.checkForInProgressUpdate()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "onResume에서 업데이트 확인 실패: ${e.message}", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // 리소스 정리
        try {
            if (::appUpdateManager.isInitialized) {
                appUpdateManager.cleanup()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "AppUpdateManager 정리 실패: ${e.message}", e)
        }
    }
} 