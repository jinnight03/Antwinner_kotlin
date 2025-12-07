package com.example.antwinner_kotlin.util

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import okhttp3.OkHttpClient
import java.io.InputStream
import java.util.concurrent.TimeUnit

@GlideModule
class AntwinnerGlideModule : AppGlideModule() {
    
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // 디스크 캐시 크기 설정 (50MB)
        val diskCacheSizeBytes = 50 * 1024 * 1024L
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeBytes))
        
        // 기본 요청 옵션 설정
        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL) // 모든 이미지 캐싱
            .placeholder(android.R.drawable.ic_menu_gallery) // 기본 플레이스홀더
            .error(android.R.drawable.ic_menu_report_image) // 에러 이미지
        
        builder.setDefaultRequestOptions(requestOptions)
    }
    
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // OkHttp 클라이언트 설정 (타임아웃 등)
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        
        // OkHttp를 Glide의 네트워크 요청 처리기로 등록
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(okHttpClient)
        )
    }
} 