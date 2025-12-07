package com.example.antwinner_kotlin.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.antwinner_kotlin.ui.home.model.Theme
import com.example.antwinner_kotlin.ui.home.model.ThemeFluctuation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 * 테마 데이터를 메모리와 디스크에 캐싱하는 유틸리티 클래스
 */
object ThemeCache {
    private const val PREF_NAME = "theme_cache"
    private const val KEY_THEME_FLUCTUATIONS = "theme_fluctuations"
    private const val KEY_THEME_LIST = "theme_list"
    private const val KEY_LAST_UPDATED = "last_updated"
    
    // 메모리 캐시
    private var cachedThemeFluctuations: List<ThemeFluctuation>? = null
    private var cachedThemeList: List<Theme>? = null
    private var lastUpdated: Long = 0
    
    // 캐시 만료 시간 (1시간)
    private val CACHE_EXPIRY_MS = TimeUnit.HOURS.toMillis(1)
    
    /**
     * 테마 데이터를 메모리와 디스크에 저장
     */
    fun cacheThemeData(context: Context, themeFluctuations: List<ThemeFluctuation>, themeList: List<Theme>) {
        try {
            // 메모리 캐시 업데이트
            cachedThemeFluctuations = themeFluctuations
            cachedThemeList = themeList
            lastUpdated = System.currentTimeMillis()
            
            // 디스크 캐시 업데이트
            val prefs = getPrefs(context)
            val gson = Gson()
            
            prefs.edit().apply {
                putString(KEY_THEME_FLUCTUATIONS, gson.toJson(themeFluctuations))
                putString(KEY_THEME_LIST, gson.toJson(themeList))
                putLong(KEY_LAST_UPDATED, lastUpdated)
                apply()
            }
            
            Log.d("ThemeCache", "테마 데이터 캐싱 완료 (${themeList.size}개)")
        } catch (e: Exception) {
            Log.e("ThemeCache", "테마 데이터 캐싱 실패", e)
        }
    }
    
    /**
     * 캐시된 ThemeFluctuation 데이터 가져오기
     * 유효한 캐시가 있으면 캐시된 데이터 반환, 없으면 null 반환
     */
    fun getCachedThemeFluctuations(context: Context): List<ThemeFluctuation>? {
        // 메모리 캐시가 있고 만료되지 않았으면 메모리 캐시 사용
        if (cachedThemeFluctuations != null && !isCacheExpired()) {
            Log.d("ThemeCache", "메모리 캐시된 ThemeFluctuation 사용")
            return cachedThemeFluctuations
        }
        
        // 디스크 캐시 확인
        try {
            val prefs = getPrefs(context)
            val json = prefs.getString(KEY_THEME_FLUCTUATIONS, null) ?: return null
            val savedTime = prefs.getLong(KEY_LAST_UPDATED, 0)
            
            // 캐시 만료 확인
            if (System.currentTimeMillis() - savedTime > CACHE_EXPIRY_MS) {
                Log.d("ThemeCache", "디스크 캐시 만료됨")
                return null
            }
            
            // 디스크 캐시 데이터 파싱
            val gson = Gson()
            val type: Type = object : TypeToken<List<ThemeFluctuation>>() {}.type
            val themeFluctuations: List<ThemeFluctuation> = gson.fromJson(json, type)
            
            // 메모리 캐시 업데이트
            cachedThemeFluctuations = themeFluctuations
            lastUpdated = savedTime
            
            Log.d("ThemeCache", "디스크 캐시된 ThemeFluctuation 사용 (${themeFluctuations.size}개)")
            return themeFluctuations
        } catch (e: Exception) {
            Log.e("ThemeCache", "캐시된 ThemeFluctuation 로드 실패", e)
            return null
        }
    }
    
    /**
     * 캐시된 Theme 리스트 가져오기
     * 유효한 캐시가 있으면 캐시된 데이터 반환, 없으면 null 반환
     */
    fun getCachedThemeList(context: Context): List<Theme>? {
        // 메모리 캐시가 있고 만료되지 않았으면 메모리 캐시 사용
        if (cachedThemeList != null && !isCacheExpired()) {
            Log.d("ThemeCache", "메모리 캐시된 Theme 리스트 사용")
            return cachedThemeList
        }
        
        // 디스크 캐시 확인
        try {
            val prefs = getPrefs(context)
            val json = prefs.getString(KEY_THEME_LIST, null) ?: return null
            val savedTime = prefs.getLong(KEY_LAST_UPDATED, 0)
            
            // 캐시 만료 확인
            if (System.currentTimeMillis() - savedTime > CACHE_EXPIRY_MS) {
                Log.d("ThemeCache", "디스크 캐시 만료됨")
                return null
            }
            
            // 디스크 캐시 데이터 파싱
            val gson = Gson()
            val type: Type = object : TypeToken<List<Theme>>() {}.type
            val themeList: List<Theme> = gson.fromJson(json, type)
            
            // 메모리 캐시 업데이트
            cachedThemeList = themeList
            lastUpdated = savedTime
            
            Log.d("ThemeCache", "디스크 캐시된 Theme 리스트 사용 (${themeList.size}개)")
            return themeList
        } catch (e: Exception) {
            Log.e("ThemeCache", "캐시된 Theme 리스트 로드 실패", e)
            return null
        }
    }
    
    /**
     * 캐시가 만료되었는지 확인
     */
    private fun isCacheExpired(): Boolean {
        return System.currentTimeMillis() - lastUpdated > CACHE_EXPIRY_MS
    }
    
    /**
     * SharedPreferences 인스턴스 가져오기
     */
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 캐시 지우기
     */
    fun clearCache(context: Context) {
        cachedThemeFluctuations = null
        cachedThemeList = null
        lastUpdated = 0
        
        getPrefs(context).edit().clear().apply()
    }
} 