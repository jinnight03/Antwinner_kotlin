package com.example.antwinner_kotlin.ui.webview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.utils.SystemBarUtils
import timber.log.Timber

class WebViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
        private const val TAG = "WebViewActivity"

        fun newIntent(context: Context, url: String, title: String = "웹페이지"): Intent {
            return Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TITLE, title)
            }
        }
    }

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        // 시스템 바 인셋 적용
        applySystemBarInsets()

        val url = intent.getStringExtra(EXTRA_URL) ?: ""
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "웹페이지"

        initViews()
        setupToolbar(title)
        setupWebView()
        
        if (url.isNotEmpty()) {
            loadUrl(url)
        } else {
            finish()
        }
    }

    private fun applySystemBarInsets() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        SystemBarUtils.applyTopPaddingForAppBar(toolbar, 16)
    }

    private fun initViews() {
        webView = findViewById(R.id.webview)
        progressBar = findViewById(R.id.progress_bar)
        toolbar = findViewById(R.id.toolbar)
    }

    private fun setupToolbar(title: String) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            this.title = title
        }
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupWebView() {
        webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                cacheMode = WebSettings.LOAD_DEFAULT
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    progressBar.visibility = View.VISIBLE
                    Timber.tag(TAG).d("페이지 로딩 시작: $url")
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    progressBar.visibility = View.GONE
                    Timber.tag(TAG).d("페이지 로딩 완료: $url")
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    progressBar.visibility = View.GONE
                    Timber.tag(TAG).e("웹페이지 로딩 오류: ${error?.description}")
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString()
                    return if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                        view?.loadUrl(url)
                        false
                    } else {
                        true
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    progressBar.progress = newProgress
                    
                    if (newProgress >= 100) {
                        progressBar.visibility = View.GONE
                    }
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    if (!title.isNullOrEmpty()) {
                        supportActionBar?.title = title
                    }
                }
            }
        }
    }

    private fun loadUrl(url: String) {
        try {
            Timber.tag(TAG).d("URL 로딩: $url")
            webView.loadUrl(url)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "URL 로딩 실패: $url")
            progressBar.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        try {
            webView.apply {
                loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
                clearCache(true)
                destroy()
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "WebView 정리 중 오류")
        }
        super.onDestroy()
    }
}
