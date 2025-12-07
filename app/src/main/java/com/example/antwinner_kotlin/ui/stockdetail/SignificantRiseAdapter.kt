package com.example.antwinner_kotlin.ui.stockdetail

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.example.antwinner_kotlin.R
import com.google.android.material.chip.Chip
import timber.log.Timber
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SignificantRiseAdapter(private val items: List<SignificantRiseDay>) :
    RecyclerView.Adapter<SignificantRiseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stockChangeChip: Chip = view.findViewById(R.id.chip_stock_change)
        val themeChip: Chip = view.findViewById(R.id.chip_theme)
        val newsTitleTextView: TextView = view.findViewById(R.id.tv_news_title)
        val dateTextView: TextView = view.findViewById(R.id.tv_date)
        val tradingInfoTextView: TextView = view.findViewById(R.id.tv_trading_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_significant_rise, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        // 등락률 뱃지에서 종목명 제거
        holder.stockChangeChip.text = "+${String.format("%.2f", item.changePercent)}%"
        
        // 상승이유에서 테마명 추출 - 대괄호([]) 안의 내용
        val themeName = extractThemeFromReason(item.newsTitle)
        
        // 테마 정보가 없는 경우(대괄호가 없거나 추출 실패) 테마 칩 숨기기
        if (themeName.isNullOrBlank()) {
            holder.themeChip.visibility = View.GONE
            Timber.d("테마명이 없어 테마 칩을 숨깁니다: ${item.newsTitle}")
        } else {
            holder.themeChip.visibility = View.VISIBLE
            holder.themeChip.text = themeName
            
            try {
                // 테마 로고 이미지 로드 - URL 인코딩 없이 시도
                val themeImageUrl = "https://antwinner.com/api/image/${themeName}.png"
                
                Timber.d("테마 로고 URL (인코딩 없음): $themeImageUrl, 원본 테마명: $themeName")
                
                // 기본 로고 설정
                holder.themeChip.chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_korea_flag)
                
                // 로그 추가: 이미지 로드 시도 중
                Timber.d("테마 이미지 로드 시도 중: $themeImageUrl")
                
                // Glide를 사용하여 Drawable 로드 후 Chip의 아이콘으로 설정
                Glide.with(context)
                    .asDrawable()
                    .load(themeImageUrl)
                    .apply(RequestOptions()
                        .transform(CircleCrop()) // 둥근 이미지로 변환
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            Timber.e(e, "테마 이미지 로드 실패 - URL: $themeImageUrl")
                            // 실패 시 인코딩된 URL로 재시도
                            loadEncodedThemeImage(context, holder.themeChip, themeName)
                            return true // Glide의 기본 오류 처리를 막음
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            Timber.d("테마 이미지 로드 성공 (인코딩 없음) - URL: $themeImageUrl, 소스: $dataSource")
                            return false // false를 반환하여 resource를 targetView에 설정
                        }
                    })
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            Timber.d("이미지가 CustomTarget에 전달됨 (인코딩 없음): $themeImageUrl")
                            holder.themeChip.chipIcon = resource
                            Timber.d("이미지가 Chip 아이콘으로 설정됨 (인코딩 없음): $themeImageUrl")
                        }
                        
                        override fun onLoadCleared(placeholder: Drawable?) {
                            Timber.d("이미지 로드 취소/클리어됨 (인코딩 없음): $themeImageUrl")
                            holder.themeChip.chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_korea_flag)
                        }
                    })
            } catch (e: Exception) {
                Timber.e(e, "테마 로고 로드 중 예외 발생: $themeName")
                // 로드 실패 시 기본 아이콘 사용
                holder.themeChip.chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_korea_flag)
            }
        }
        
        // 상승이유에서 대괄호([]) 부분 제거하고 표시
        val cleanNewsTitle = item.newsTitle.replace(Regex("\\[.*?\\]"), "").trim()
        holder.newsTitleTextView.text = cleanNewsTitle
        
        holder.dateTextView.text = item.date
        holder.tradingInfoTextView.text = "거래대금 : ${item.tradingValue} · 거래량 : ${item.tradingVolume}"

        // 등락률에 따른 텍스트 색상 (항상 빨간색, 상승 기준이므로)
        holder.stockChangeChip.setTextColor(ContextCompat.getColor(context, R.color.rising_color))
    }
    
    /**
     * 인코딩된 URL로 이미지 로드를 재시도하는 함수
     */
    private fun loadEncodedThemeImage(context: android.content.Context, chip: Chip, themeName: String) {
        try {
            val encodedThemeName = URLEncoder.encode(themeName, StandardCharsets.UTF_8.toString())
            val encodedUrl = "https://antwinner.com/api/image/$encodedThemeName.png"
            Timber.d("인코딩된 URL로 재시도: $encodedUrl")

            Glide.with(context)
                .asDrawable()
                .load(encodedUrl)
                .apply(RequestOptions()
                    .transform(CircleCrop())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        Timber.e(e, "인코딩된 URL 로드도 실패: $encodedUrl")
                        return false
                    }
                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        Timber.d("인코딩된 URL 로드 성공 - URL: $encodedUrl, 소스: $dataSource")
                        return false
                    }
                })
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                         Timber.d("이미지가 CustomTarget에 전달됨 (인코딩): $encodedUrl")
                        chip.chipIcon = resource
                        Timber.d("이미지가 Chip 아이콘으로 설정됨 (인코딩): $encodedUrl")
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        Timber.d("이미지 로드 취소/클리어됨 (인코딩): $encodedUrl")
                        chip.chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_korea_flag)
                    }
                })
        } catch (e: Exception) {
            Timber.e(e, "인코딩된 URL 로드 중 예외 발생: $themeName")
            chip.chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_korea_flag)
        }
    }
    
    /**
     * 상승이유 텍스트에서 대괄호([]) 안의 내용을 테마명으로 추출
     * 예: "[방산] 민주, K-방산 컨트롤타워 신설 공약 검토" -> "방산"
     * 대괄호가 없으면 null을 반환
     */
    private fun extractThemeFromReason(reason: String): String? {
        val pattern = Regex("\\[(.*?)\\]")
        val matchResult = pattern.find(reason)
        val themeName = matchResult?.groupValues?.get(1)
        
        // 테마명이 추출되었는지 확인하고 공백 제거
        return if (!themeName.isNullOrBlank()) {
            themeName.trim()
        } else {
            null
        }
    }

    override fun getItemCount() = items.size
} 