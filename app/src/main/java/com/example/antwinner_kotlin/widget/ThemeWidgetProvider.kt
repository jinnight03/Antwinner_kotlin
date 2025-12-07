package com.example.antwinner_kotlin.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.antwinner_kotlin.MainActivity
import com.example.antwinner_kotlin.R
import android.content.ComponentName
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class ThemeWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.antwinner_kotlin.ACTION_UPDATE_WIDGET"
        const val ACTION_THEME_CLICK = "com.example.antwinner_kotlin.ACTION_THEME_CLICK"
        private const val TAG = "ThemeWidgetProvider"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate called with ${appWidgetIds.size} widgets")
        
        // 각 위젯에 대해 업데이트 수행
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        
        when (intent?.action) {
            ACTION_UPDATE_WIDGET -> {
                Log.d(TAG, "Manual update requested")
                context?.let { ctx ->
                    val appWidgetManager = AppWidgetManager.getInstance(ctx)
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(
                        ComponentName(ctx, ThemeWidgetProvider::class.java)
                    )
                    onUpdate(ctx, appWidgetManager, appWidgetIds)
                }
            }
            ACTION_THEME_CLICK -> {
                Log.d(TAG, "Theme clicked, opening HomeFragment")
                context?.let { ctx ->
                    val mainIntent = Intent(ctx, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("navigate_to", "home") // HomeFragment로 이동하기 위한 플래그
                    }
                    ctx.startActivity(mainIntent)
                }
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        Log.d(TAG, "Updating widget $appWidgetId")
        
        // 위젯 설정 읽기 (리스트형/카드형)
        val prefs = context.getSharedPreferences("widget_$appWidgetId", Context.MODE_PRIVATE)
        val isCardType = prefs.getBoolean("is_card_type", false)
        
        // 디자인에 따른 레이아웃 선택
        val layoutId = if (isCardType) {
            R.layout.widget_theme_layout_card
        } else {
            R.layout.widget_theme_layout_list
        }
        
        val views = RemoteViews(context.packageName, layoutId)
        
        // 위젯 서비스 설정
        val serviceIntent = Intent(context, ThemeWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra("is_card_type", isCardType)
        }
        
        views.setRemoteAdapter(R.id.widget_list_view, serviceIntent)
        
        // 새로고침 버튼 클릭 이벤트
        val updateIntent = Intent(context, ThemeWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val updatePendingIntent = PendingIntent.getBroadcast(
            context, 
            0, 
            updateIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_refresh_button, updatePendingIntent)
        
        // 제목 클릭 시 HomeFragment로 이동
        val homeIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "home")
        }
        val homePendingIntent = PendingIntent.getActivity(
            context,
            0,
            homeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_title, homePendingIntent)
        
        // 아이템 클릭 템플릿 설정
        val itemClickIntent = Intent(context, ThemeWidgetProvider::class.java).apply {
            action = ACTION_THEME_CLICK
        }
        val itemClickPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            itemClickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setPendingIntentTemplate(R.id.widget_list_view, itemClickPendingIntent)
        
        // 마지막 업데이트 시간 설정
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        views.setTextViewText(R.id.widget_last_update, "업데이트: $currentTime")
        
        // 위젯 업데이트
        appWidgetManager.updateAppWidget(appWidgetId, views)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view)
        
        Log.d(TAG, "Widget $appWidgetId updated successfully")
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        
        // 위젯 삭제 시 설정 정리
        appWidgetIds?.forEach { appWidgetId ->
            context?.getSharedPreferences("widget_$appWidgetId", Context.MODE_PRIVATE)
                ?.edit()?.clear()?.apply()
        }
    }
} 