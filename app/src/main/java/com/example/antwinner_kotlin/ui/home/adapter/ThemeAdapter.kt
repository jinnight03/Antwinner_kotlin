package com.example.antwinner_kotlin.ui.home.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.cardview.widget.CardView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.ui.home.model.Theme
import com.example.antwinner_kotlin.ui.home.model.ThemeFluctuation
import java.text.DecimalFormat
import android.util.Log
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ThemeAdapter(private var themes: List<Theme>) : 
    RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder>() {
    
    // API 응답 데이터 저장
    private var themeFluctuations: List<ThemeFluctuation> = emptyList()
    
    // 클릭 리스너 인터페이스 정의
    interface OnItemClickListener {
        fun onItemClick(theme: Theme)
    }
    
    private var listener: OnItemClickListener? = null
    
    // 리스너 설정 메서드
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
    
    // 스팬 크기를 가져오는 메서드 (그리드 레이아웃에서 아이템이 차지하는 칸 수)
    fun getSpanSize(position: Int): Int {
        if (position >= themes.size) {
            return 2 // 기본값
        }
        
        // 테마 크기에 따라 스팬 크기 결정
        return when (themes[position].size) {
            3 -> 3 // 큰 박스 (3칸 차지)
            2 -> 2 // 중간 박스 (2칸 차지)
            else -> 1 // 작은 박스 (1칸 차지)
        }
    }
    
    // DiffUtil을 사용한 효율적인 테마 리스트 업데이트 - 단순화
    fun updateThemes(newThemes: List<Theme>) {
        // 로그 추가
        Log.d("ThemeAdapter", "updateThemes 호출됨: ${newThemes.size}개 테마")
        
        val oldThemes = this.themes
        
        // DiffUtil로 변경사항 계산
        val diffCallback = ThemeDiffCallback(oldThemes, newThemes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        // 데이터 업데이트
        this.themes = newThemes
        
        // 변경사항 적용
        diffResult.dispatchUpdatesTo(this)
        
        // 로그 추가
        Log.d("ThemeAdapter", "updateThemes 완료: 아이템 개수 ${themes.size}")
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme, parent, false)
        return ThemeViewHolder(view, parent.context)
    }
    
    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        val theme = themes[position]
        holder.bind(theme)
        
        // 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            listener?.onItemClick(theme)
        }
    }
    
    override fun getItemCount(): Int = themes.size
    
    // 테마 아이템 가져오기
    fun getThemeAt(position: Int): Theme {
        try {
            if (position < 0 || position >= themes.size) {
                Log.e("ThemeAdapter", "인덱스 범위 오류: position($position) 범위 밖 (0..${themes.size-1})")
                return Theme("오류", 0.0, 0.0, true, 1) // 더미 테마 반환
            }
            Log.d("ThemeAdapter", "getThemeAt($position): ${themes[position].name}")
            return themes[position]
        } catch (e: Exception) {
            Log.e("ThemeAdapter", "getThemeAt 예외: ${e.message}")
            return Theme("오류", 0.0, 0.0, true, 1) // 예외 발생 시 더미 테마 반환
        }
    }
    
    inner class ThemeViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val themeCard: CardView = itemView.findViewById(R.id.theme_card)
        private val themeFront: View = itemView.findViewById(R.id.theme_front)
        private val themeBack: View = itemView.findViewById(R.id.theme_back)
        private val themeBackground: LinearLayout = itemView.findViewById(R.id.theme_background)
        private val themeName: TextView = itemView.findViewById(R.id.tv_theme_name)
        private val themePercent: TextView = itemView.findViewById(R.id.tv_theme_percent)
        private val themeVolume: TextView = itemView.findViewById(R.id.tv_theme_volume)
        
        // 소수점 두 자리까지만 표시하도록 포맷터 설정
        private val decimalFormat = DecimalFormat("0.00")
        
        fun bind(theme: Theme) {
            try {
                // 로그 추가
                Log.d("ThemeAdapter", "bind 호출: ${theme.name}, 크기: ${theme.size}, 등락률: ${theme.rate}%")
                
                // 등락률에 따른 상대적 크기 조절 (컨테이너 내부에서)
                adjustCardSizeByRate(theme.rate)
                
                // 테마명을 간결하게 표시 (이름이 너무 길면 자동으로 줄임)
                themeName.text = theme.name
                
                // 퍼센트 포맷팅 및 표시 (항상 한 줄에 표시되도록 설정)
                val percentText = "${if (theme.rate > 0) "+" else ""}${decimalFormat.format(theme.rate)}%"
                themePercent.text = percentText
                themePercent.maxLines = 1
                
                // 긴 테마명이나 높은 등락률의 경우 텍스트 크기 자동 조절
                if (theme.name.length > 8) {
                    themeName.textSize = 12f // 긴 테마명은 작게 표시
                } else {
                    themeName.textSize = 13f // 짧은 테마명은 기본 크기
                }
                
                // 상승비율 표시 제거 (더 이상 사용하지 않음)
                themeVolume.visibility = View.GONE
                
                // 상승/하락에 따른 배경색 설정 - 상대적 농도 적용
                val backgroundColor = if (theme.isRising) {
                    // 상승 테마는 빨간색 - 상승률에 따라 농도 조절
                    when {
                        theme.rate >= 30 -> Color.parseColor("#FF3B30") // 진한 빨강 (30% 이상)
                        theme.rate >= 20 -> Color.parseColor("#FF4F4F") // 중간-진한 빨강
                        theme.rate >= 10 -> Color.parseColor("#FF6B6B") // 중간 빨강
                        theme.rate >= 5 -> Color.parseColor("#FF8787") // 약간 연한 빨강
                        else -> Color.parseColor("#FFA5A5") // 연한 빨강
                    }
                } else {
                    // 하락 테마는 파란색 - 하락률에 따라 농도 조절 (절대값으로 계산)
                    val absRate = Math.abs(theme.rate)
                    when {
                        absRate >= 30 -> Color.parseColor("#0066CC") // 진한 파랑 (30% 이상)
                        absRate >= 20 -> Color.parseColor("#0D7AE7") // 중간-진한 파랑
                        absRate >= 10 -> Color.parseColor("#2196F3") // 중간 파랑
                        absRate >= 5 -> Color.parseColor("#42A5F5") // 약간 연한 파랑
                        else -> Color.parseColor("#64B5F6") // 연한 파랑
                    }
                }
                
                themeBackground.setBackgroundColor(backgroundColor)
                
                // 패딩 설정 (카드 크기가 커진 만큼 패딩도 조정)
                val padding = (8 * context.resources.displayMetrics.density).toInt()
                themeBackground.setPadding(padding, padding, padding, padding)
                
                // 항상 앞면만 표시하고 뒷면은 숨김
                themeFront.visibility = View.VISIBLE
                themeBack.visibility = View.GONE
                
                // 로그 추가 - 바인딩 완료
                Log.d("ThemeAdapter", "bind 완료: ${theme.name}")
            } catch (e: Exception) {
                Log.e("ThemeAdapter", "bind 오류: ${e.message}")
            }
        }
        
        /**
         * 등락률에 따른 CardView 상대적 크기 조절
         * 컨테이너(FrameLayout) 내부에서 CardView 크기를 등락률에 비례하여 조절
         * @param rate 등락률 (퍼센트)
         */
        private fun adjustCardSizeByRate(rate: Double) {
            val layoutParams = themeCard.layoutParams as android.widget.FrameLayout.LayoutParams
            
            // 등락률에 따른 크기 비율 계산 (50% ~ 100%)
            val absRate = abs(rate)
            val sizeRatio = when {
                absRate >= 30.0 -> 1.0f      // 30% 이상: 100% 크기
                absRate >= 20.0 -> 0.9f      // 20-30%: 90% 크기
                absRate >= 15.0 -> 0.8f      // 15-20%: 80% 크기
                absRate >= 10.0 -> 0.75f     // 10-15%: 75% 크기
                absRate >= 5.0 -> 0.65f      // 5-10%: 65% 크기
                absRate >= 2.0 -> 0.6f       // 2-5%: 60% 크기
                else -> 0.5f                 // 2% 미만: 50% 크기
            }
            
            // 부모 컨테이너 크기 대비 상대적 크기 설정
            layoutParams.width = android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            layoutParams.height = android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            
            // 실제 크기 조절은 scaleX, scaleY로 처리
            themeCard.scaleX = sizeRatio
            themeCard.scaleY = sizeRatio
            
            themeCard.layoutParams = layoutParams
            
            Log.d("ThemeAdapter", "CardView 크기 조절: 등락률: ${rate}%, 크기 비율: $sizeRatio")
        }
    }
}

/**
 * Theme 리스트의 변경사항을 효율적으로 계산하는 DiffUtil
 */
class ThemeDiffCallback(
    private val oldThemes: List<Theme>,
    private val newThemes: List<Theme>
) : DiffUtil.Callback() {
    
    override fun getOldListSize(): Int = oldThemes.size
    
    override fun getNewListSize(): Int = newThemes.size
    
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldThemes[oldItemPosition].name == newThemes[newItemPosition].name
    }
    
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldTheme = oldThemes[oldItemPosition]
        val newTheme = newThemes[newItemPosition]
        
        return oldTheme.name == newTheme.name &&
               oldTheme.rate == newTheme.rate &&
               oldTheme.risingRatio == newTheme.risingRatio &&
               oldTheme.isRising == newTheme.isRising &&
               oldTheme.size == newTheme.size
    }
} 