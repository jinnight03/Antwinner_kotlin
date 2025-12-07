package com.example.antwinner_kotlin.ui.themedetail

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.data.model.CompanyData
import com.example.antwinner_kotlin.data.model.ThemeData
import com.example.antwinner_kotlin.databinding.ItemThemeDetailBinding
import com.example.antwinner_kotlin.util.dpToPx
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import android.text.TextUtils

class ThemeDetailAdapter(private val context: Context) : RecyclerView.Adapter<ThemeDetailAdapter.ThemeDetailViewHolder>() {

    private var themeList: List<ThemeData> = listOf()
    private var onThemeClickListener: ((ThemeData) -> Unit)? = null
    private var onStockClickListener: ((CompanyData) -> Unit)? = null

    fun submitList(newList: List<ThemeData>) {
        themeList = newList
        notifyDataSetChanged()
    }
    
    fun setOnThemeClickListener(listener: (ThemeData) -> Unit) {
        onThemeClickListener = listener
    }
    
    fun setOnStockClickListener(listener: (CompanyData) -> Unit) {
        onStockClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeDetailViewHolder {
        val binding = ItemThemeDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThemeDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ThemeDetailViewHolder, position: Int) {
        holder.bind(themeList[position])
    }

    override fun getItemCount(): Int = themeList.size

    inner class ThemeDetailViewHolder(private val binding: ItemThemeDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(theme: ThemeData) {
            // 테마명 설정 및 조건부 줄바꿈/말줄임 처리
            binding.tvThemeName.text = theme.thema
            if (theme.thema.contains("(") || theme.thema.contains(")")) {
                binding.tvThemeName.maxLines = Integer.MAX_VALUE // 여러 줄 허용
                binding.tvThemeName.ellipsize = null // 말줄임 제거
            } else {
                binding.tvThemeName.maxLines = 1 // 한 줄 제한
                binding.tvThemeName.ellipsize = TextUtils.TruncateAt.END // 말줄임 설정
            }

            binding.tvThemeAverageRate.text = theme.averageRate

            // 등락률 색상 변경 (양수: market_up, 음수: market_down, 0: black)
            val avgFluctuationValue = theme.averageRate.replace("%", "").toDoubleOrNull() ?: 0.0
            val avgColorResId = when {
                avgFluctuationValue > 0 -> R.color.market_up
                avgFluctuationValue < 0 -> R.color.market_down
                else -> android.R.color.black
            }
            binding.tvThemeAverageRate.setTextColor(ContextCompat.getColor(context, avgColorResId))

            // 테마 아이콘 로드 (Glide 사용)
            try {
                val encodedThemeName = URLEncoder.encode(theme.thema, StandardCharsets.UTF_8.toString())
                val imageUrl = "https://antwinner.com/api/image/$encodedThemeName.png"
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_theme_default) // 로딩 중 이미지
                    .error(R.drawable.ic_theme_default) // 에러 시 이미지
                    .into(binding.ivThemeIcon)
            } catch (e: Exception) {
                // URL 인코딩 실패 또는 Glide 관련 예외 처리
                binding.ivThemeIcon.setImageResource(R.drawable.ic_theme_default) // 기본 이미지 설정
            }
            
            // 테마 헤더 클릭 리스너 설정 (테마명, 이미지, 등락률 클릭 시 ThemeDetailActivity로 이동)
            val themeHeaderClickListener = android.view.View.OnClickListener {
                onThemeClickListener?.invoke(theme)
            }
            
            binding.ivThemeIcon.setOnClickListener(themeHeaderClickListener)
            binding.tvThemeName.setOnClickListener(themeHeaderClickListener)
            binding.tvThemeAverageRate.setOnClickListener(themeHeaderClickListener)

            // 종목 리스트 동적 추가 (최대 5개)
            binding.layoutCompanies.removeAllViews() // 이제 헤더가 없으므로 모든 뷰 제거
            theme.companies.take(5).forEach { company -> // .take(5) 추가
                val companyView = createCompanyView(company)
                binding.layoutCompanies.addView(companyView)
            }
        }

        // 각 종목 정보를 표시할 View 생성 (이미지 디자인 반영)
        private fun createCompanyView(company: CompanyData): ViewGroup {
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 4.dpToPx(context) } // dpToPx 사용
                 weightSum = 3f // XML 헤더와 동일한 weightSum 설정
            }

            val nameTextView = TextView(context).apply {
                text = company.stockName
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f) // weight 설정
                textSize = 13f // 이미지와 유사하게 조정
                setTextColor(ContextCompat.getColor(context, R.color.text_primary)) // colors.xml 참고
                maxLines = 1 // 한 줄로 제한
                ellipsize = TextUtils.TruncateAt.END // 넘칠 경우 ... 처리
                gravity = android.view.Gravity.END // 오른쪽 정렬 추가
                
                // 종목명 클릭 리스너 추가
                setOnClickListener {
                    onStockClickListener?.invoke(company)
                }
                
                // 클릭 가능하다는 것을 시각적으로 표시
                background = ContextCompat.getDrawable(context, android.R.drawable.list_selector_background)
                isClickable = true
                isFocusable = true
            }

            val fluctuationTextView = TextView(context).apply {
                text = company.fluctuation
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.8f) // weight 설정
                textSize = 13f // 이미지와 유사하게 조정
                gravity = android.view.Gravity.END // 오른쪽 정렬
                // 등락률 색상 설정
                val fluctuationValueCompany = company.fluctuation.replace("%", "").toDoubleOrNull() ?: 0.0
                val colorResIdCompany = when {
                    fluctuationValueCompany > 0 -> R.color.market_up
                    fluctuationValueCompany < 0 -> R.color.market_down
                    else -> android.R.color.black
                }
                setTextColor(ContextCompat.getColor(context, colorResIdCompany))
            }

            val volumeTextView = TextView(context).apply {
                text = company.volume
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.7f) // weight 설정
                textSize = 13f // 이미지와 유사하게 조정
                gravity = android.view.Gravity.END // 오른쪽 정렬
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            }

            layout.addView(nameTextView)
            layout.addView(fluctuationTextView)
            layout.addView(volumeTextView)

            return layout
        }
    }
} 