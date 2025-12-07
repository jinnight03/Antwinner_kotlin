package com.example.antwinner_kotlin.ui.home.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.ui.home.model.HotTheme
import java.text.DecimalFormat
import com.google.android.material.imageview.ShapeableImageView

class HotThemeAdapter(private var themes: List<HotTheme>) :
    RecyclerView.Adapter<HotThemeAdapter.HotThemeViewHolder>() {
    
    // 기본 테마 로고 URL 형식
    private val baseLogoUrl = "https://antwinner.com/api/image/"
    
    // 클릭 리스너 인터페이스 정의
    interface OnItemClickListener {
        fun onItemClick(v: View, data: HotTheme, position: Int)
    }
    
    private var listener: OnItemClickListener? = null
    
    // 리스너 설정 메서드
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
    
    // 데이터 업데이트
    fun updateHotThemes(newThemes: List<HotTheme>) {
        this.themes = newThemes
        notifyDataSetChanged()
    }
    
    // updateData 메서드 추가 (fetchHotThemes에서 호출하는 메서드)
    fun updateData(newThemes: List<HotTheme>) {
        this.themes = newThemes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotThemeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hot_theme, parent, false)
        return HotThemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HotThemeViewHolder, position: Int) {
        val theme = themes[position]
        holder.bind(theme)
        
        // 테마명과 테마 아이콘 영역에 대한 클릭 이벤트 처리
        holder.itemView.findViewById<View>(R.id.theme_header_container)?.setOnClickListener {
            listener?.onItemClick(it, theme, position)
        }
    }

    override fun getItemCount(): Int = themes.size

    class HotThemeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val themeIcon: ShapeableImageView = itemView.findViewById(R.id.iv_theme_icon)
        private val themeName: TextView = itemView.findViewById(R.id.tv_theme_name)
        private val themePercent: TextView = itemView.findViewById(R.id.tv_theme_percent)
        
        private val company1Name: TextView = itemView.findViewById(R.id.tv_theme_company1)
        private val company1Percent: TextView = itemView.findViewById(R.id.tv_theme_company1_percent)
        private val company1MarketCap: TextView = itemView.findViewById(R.id.tv_theme_company1_market_cap)
        
        private val company2Name: TextView = itemView.findViewById(R.id.tv_theme_company2)
        private val company2Percent: TextView = itemView.findViewById(R.id.tv_theme_company2_percent)
        private val company2MarketCap: TextView = itemView.findViewById(R.id.tv_theme_company2_market_cap)
        
        private val company3Name: TextView = itemView.findViewById(R.id.tv_theme_company3)
        private val company3Percent: TextView = itemView.findViewById(R.id.tv_theme_company3_percent)
        private val company3MarketCap: TextView = itemView.findViewById(R.id.tv_theme_company3_market_cap)
        
        private val decimalFormat = DecimalFormat("+#,##0.00%;-#,##0.00%")
        
        fun bind(theme: HotTheme) {
            // 테마 로고 이미지 로드
            Glide.with(itemView.context)
                .load(theme.logoUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(RequestOptions().centerCrop())
                .placeholder(R.drawable.ic_theme_default)
                .error(R.drawable.ic_theme_default)
                .into(themeIcon)
            
            themeName.text = theme.name
            
            // 테마 상승률 표시
            val percentText = decimalFormat.format(theme.percent / 100)
            themePercent.text = percentText
            themePercent.setTextColor(if (theme.percent > 0) Color.parseColor("#FF0000") else Color.parseColor("#0000FF"))
            
            // 기업별 정보 표시 (최대 3개 표시)
            if (theme.companies.isNotEmpty()) {
                val company1 = theme.companies[0]
                company1Name.text = company1.name
                company1Percent.text = decimalFormat.format(company1.percent / 100)
                company1Percent.setTextColor(if (company1.percent > 0) Color.parseColor("#FF0000") else Color.parseColor("#0000FF"))
                company1MarketCap.text = "거래대금: ${company1.marketCap}"
                
                // 회사 정보 View 표시/숨김 처리
                company1Name.visibility = View.VISIBLE
                company1Percent.visibility = View.VISIBLE
                company1MarketCap.visibility = View.VISIBLE
                
                // 종목명 클릭 이벤트 추가
                company1Name.setOnClickListener {
                    val intent = com.example.antwinner_kotlin.ui.stockdetail.StockDetailActivity.newIntent(
                        itemView.context, 
                        company1.name
                    )
                    itemView.context.startActivity(intent)
                }
                
                if (theme.companies.size > 1) {
                    val company2 = theme.companies[1]
                    company2Name.text = company2.name
                    company2Percent.text = decimalFormat.format(company2.percent / 100)
                    company2Percent.setTextColor(if (company2.percent > 0) Color.parseColor("#FF0000") else Color.parseColor("#0000FF"))
                    company2MarketCap.text = "거래대금: ${company2.marketCap}"
                    
                    // 회사 정보 View 표시/숨김 처리
                    company2Name.visibility = View.VISIBLE
                    company2Percent.visibility = View.VISIBLE
                    company2MarketCap.visibility = View.VISIBLE
                    
                    // 종목명 클릭 이벤트 추가
                    company2Name.setOnClickListener {
                        val intent = com.example.antwinner_kotlin.ui.stockdetail.StockDetailActivity.newIntent(
                            itemView.context,
                            company2.name
                        )
                        itemView.context.startActivity(intent)
                    }
                    
                    if (theme.companies.size > 2) {
                        val company3 = theme.companies[2]
                        company3Name.text = company3.name
                        company3Percent.text = decimalFormat.format(company3.percent / 100)
                        company3Percent.setTextColor(if (company3.percent > 0) Color.parseColor("#FF0000") else Color.parseColor("#0000FF"))
                        company3MarketCap.text = "거래대금: ${company3.marketCap}"
                        
                        // 회사 정보 View 표시/숨김 처리
                        company3Name.visibility = View.VISIBLE
                        company3Percent.visibility = View.VISIBLE
                        company3MarketCap.visibility = View.VISIBLE
                        
                        // 종목명 클릭 이벤트 추가
                        company3Name.setOnClickListener {
                            val intent = com.example.antwinner_kotlin.ui.stockdetail.StockDetailActivity.newIntent(
                                itemView.context,
                                company3.name
                            )
                            itemView.context.startActivity(intent)
                        }
                    } else {
                        // 3번째 회사 데이터가 없으면 숨김
                        company3Name.visibility = View.GONE
                        company3Percent.visibility = View.GONE
                        company3MarketCap.visibility = View.GONE
                    }
                } else {
                    // 2, 3번째 회사 데이터가 없으면 숨김
                    company2Name.visibility = View.GONE
                    company2Percent.visibility = View.GONE
                    company2MarketCap.visibility = View.GONE
                    company3Name.visibility = View.GONE
                    company3Percent.visibility = View.GONE
                    company3MarketCap.visibility = View.GONE
                }
            } else {
                // 회사 데이터가 없으면 모두 숨김
                company1Name.visibility = View.GONE
                company1Percent.visibility = View.GONE
                company1MarketCap.visibility = View.GONE
                company2Name.visibility = View.GONE
                company2Percent.visibility = View.GONE
                company2MarketCap.visibility = View.GONE
                company3Name.visibility = View.GONE
                company3Percent.visibility = View.GONE
                company3MarketCap.visibility = View.GONE
            }
        }
    }
} 