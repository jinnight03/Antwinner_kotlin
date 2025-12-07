package com.example.antwinner_kotlin.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.utils.SystemBarUtils

class ThemeWidgetConfigActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ThemeWidgetConfig"
    }

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioList: RadioButton
    private lateinit var radioCard: RadioButton
    private lateinit var btnConfirm: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_widget_config)

        // 기본적으로 RESULT_CANCELED 설정
        setResult(RESULT_CANCELED)

        // 위젯 ID 가져오기
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // 유효하지 않은 위젯 ID인 경우 종료
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        Log.d(TAG, "Configuring widget $appWidgetId")

        // 시스템 바 인셋 적용
        applySystemBarInsets()

        initViews()
        setupClickListeners()
    }

    private fun applySystemBarInsets() {
        val rootView = findViewById<android.view.View>(android.R.id.content)
        SystemBarUtils.applyTopPaddingInset(rootView, 16)
    }

    private fun initViews() {
        radioGroup = findViewById(R.id.radio_group_design)
        radioList = findViewById(R.id.radio_list_type)
        radioCard = findViewById(R.id.radio_card_type)
        btnConfirm = findViewById(R.id.btn_confirm)
        btnCancel = findViewById(R.id.btn_cancel)

        // 기본값으로 리스트형 선택
        radioList.isChecked = true
    }

    private fun setupClickListeners() {
        btnConfirm.setOnClickListener {
            saveConfigAndFinish()
        }

        btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun saveConfigAndFinish() {
        // 선택된 디자인 타입 확인
        val isCardType = radioCard.isChecked
        
        Log.d(TAG, "Saving config for widget $appWidgetId: isCardType = $isCardType")

        // SharedPreferences에 설정 저장
        val prefs = getSharedPreferences("widget_$appWidgetId", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_card_type", isCardType)
            apply()
        }

        // 위젯 업데이트
        val appWidgetManager = AppWidgetManager.getInstance(this)
        ThemeWidgetProvider().also { provider ->
            provider.onUpdate(this, appWidgetManager, intArrayOf(appWidgetId))
        }

        // 성공 결과 반환
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }
} 