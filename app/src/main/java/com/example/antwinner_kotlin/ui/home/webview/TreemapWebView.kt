package com.example.antwinner_kotlin.ui.home.webview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.antwinner_kotlin.ui.home.model.Theme
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * HTML/D3.js 기반 트리맵을 표시하는 WebView
 */
class TreemapWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    private var onThemeClickListener: ((String) -> Unit)? = null
    private val gson = Gson()
    
    init {
        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            
            // 터치 이벤트 최적화
            allowUniversalAccessFromFileURLs = true
            allowFileAccessFromFileURLs = true
        }
        
        // 터치 이벤트 개선
        isClickable = true
        isFocusable = true
        isFocusableInTouchMode = true

        // JavaScript 인터페이스 추가
        addJavascriptInterface(WebAppInterface(), "Android")
        
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("TreemapWebView", "WebView 로딩 완료")
            }
            
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.e("TreemapWebView", "WebView 오류: $description")
            }
        }

        // HTML 파일 로드
        loadUrl("file:///android_asset/treemap.html")
    }

    /**
     * 테마 데이터 업데이트
     */
    fun updateThemes(themes: List<Theme>) {
        Log.d("TreemapWebView", "테마 데이터 업데이트: ${themes.size}개")
        
        val webData = WebTreemapData(themes = themes)
        val jsonData = gson.toJson(webData)
        
        Log.d("TreemapWebView", "JSON 데이터: $jsonData")
        
        // JavaScript 함수 호출
        post {
            evaluateJavascript("updateTreemapData('${jsonData.replace("'", "\\'")}')", null)
        }
    }

    /**
     * 테마 클릭 리스너 설정
     */
    fun setOnThemeClickListener(listener: (String) -> Unit) {
        this.onThemeClickListener = listener
    }

    /**
     * JavaScript에서 Android로 통신하기 위한 인터페이스
     */
    inner class WebAppInterface {
        
        @JavascriptInterface
        fun onWebViewReady() {
            Log.d("TreemapWebView", "WebView 준비 완료")
        }
        
        @JavascriptInterface
        fun onThemeClick(themeName: String) {
            Log.d("TreemapWebView", "JavaScript에서 테마 클릭 이벤트 수신: $themeName")
            post {
                Log.d("TreemapWebView", "Android 클릭 리스너 호출: $themeName")
                onThemeClickListener?.invoke(themeName)
            }
        }
    }

    /**
     * WebView에 전달할 데이터 구조
     */
    data class WebTreemapData(
        @SerializedName("themes")
        val themes: List<Theme>
    )
}

