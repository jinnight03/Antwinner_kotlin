package com.example.antwinner_kotlin.ui.theme.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.model.ThemeStock
import java.text.NumberFormat
import java.util.Locale
import timber.log.Timber

class ThemeDetailStockAdapter(private var stocks: List<ThemeStock>) :
    RecyclerView.Adapter<ThemeDetailStockAdapter.StockViewHolder>() {

    private var onItemClickListener: ((ThemeStock) -> Unit)? = null

    fun setOnItemClickListener(listener: (ThemeStock) -> Unit) {
        onItemClickListener = listener
    }

    fun updateData(newStocks: List<ThemeStock>) {
        stocks = newStocks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme_detail_stock, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        Timber.e("---- 여기 실행됨: ThemeDetailStockAdapter.onBindViewHolder 시작 (position: $position) ----")
        holder.bind(stocks[position])
    }

    override fun getItemCount(): Int = stocks.size

    inner class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stockName: TextView = itemView.findViewById(R.id.tv_stock_name)
        private val stockPrice: TextView = itemView.findViewById(R.id.tv_stock_price)
        private val stockRate: TextView = itemView.findViewById(R.id.tv_stock_rate)
        private val stockLogoImageView: ImageView = itemView.findViewById(R.id.iv_stock_logo)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(stocks[position])
                }
            }
        }

        fun bind(stock: ThemeStock) {
            stockName.text = stock.name
            
            // 가격 포맷팅 - 가격과 함께 거래대금도 포함
            val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
            val priceText = formatter.format(stock.price) + "원"
            
            // 거래대금 포맷팅
            val tradingAmount = formatTradingAmount(stock.tradingAmount)
            stockPrice.text = "$priceText ($tradingAmount)"
            
            // 변화율 처리
            val formattedRate = if (stock.changeRate > 0) {
                "+${String.format("%.2f", stock.changeRate)}%"
            } else {
                "${String.format("%.2f", stock.changeRate)}%"
            }
            
            stockRate.text = formattedRate
            
            // 색상 처리
            val rateColor = when {
                stock.changeRate > 0 -> ContextCompat.getColor(itemView.context, R.color.rising_color)
                stock.changeRate < 0 -> ContextCompat.getColor(itemView.context, R.color.falling_color)
                else -> ContextCompat.getColor(itemView.context, R.color.market_neutral)
            }
            stockRate.setTextColor(rateColor)

            // 종목 로고 로드 로직 개선
            try {
                // 종목 코드 추출 - 기존 stock.logoUrl에서 종목 코드 추출 시도
                // 또는 stock 모델에 stockCode 필드가 있다면 그것을 사용
                val stockCode = extractStockCode(stock)
                
                // 새로운 로고 URL 생성 (.png 제거)
                val newLogoUrl = if (stockCode.isNotEmpty()) {
                    "https://antwinner.com/api/stock_logos/$stockCode"
                } else {
                    // 기존 URL 사용 (fallback)
                    stock.logoUrl
                }
                
                Timber.e("로고 로드 - 종목: ${stock.name}, 종목코드: $stockCode, URL: $newLogoUrl")
                
                if (newLogoUrl.isNotBlank()) {
                    val requestOptions = RequestOptions()
                        .placeholder(R.drawable.placeholder_circle)
                        .error(R.drawable.placeholder_circle)
                        .circleCrop()
                    
                    Glide.with(itemView.context)
                        .load(newLogoUrl)
                        .apply(requestOptions)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>,
                                isFirstResource: Boolean
                            ): Boolean {
                                Timber.e("로고 로드 실패 - ${stock.name}: ${e?.message}")
                                return false
                            }
                            
                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable>,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                Timber.d("로고 로드 성공 - ${stock.name}")
                                return false
                            }
                        })
                        .into(stockLogoImageView)
                } else {
                    Timber.e("빈 로고 URL - 기본 이미지 표시: ${stock.name}")
                    Glide.with(itemView.context)
                         .load(R.drawable.placeholder_circle)
                         .circleCrop()
                         .into(stockLogoImageView)
                }
            } catch (e: Exception) {
                Timber.e(e, "로고 로딩 중 오류 발생: ${stock.name}")
                Glide.with(itemView.context)
                    .load(R.drawable.placeholder_circle)
                    .circleCrop()
                    .into(stockLogoImageView)
            }
        }
        
        // 종목 코드 추출 메서드
        private fun extractStockCode(stock: ThemeStock): String {
            // 0. 먼저 stockCode 필드 확인 (직접 추가된 필드)
            if (stock.stockCode.isNotBlank()) {
                // N/A 값이거나 유효하지 않은 형식이면 즉시 빈 문자열 반환
                if (stock.stockCode.equals("N/A", ignoreCase = true) || 
                    stock.stockCode.equals("NA", ignoreCase = true)) {
                    Timber.d("종목코드 필드가 N/A 값이므로 무시: ${stock.name}")
                    return ""
                }
                
                // 6자리 숫자 형식 확인
                if (stock.stockCode.length == 6 && stock.stockCode.matches(Regex("^[0-9]{6}$"))) {
                    Timber.d("stockCode 필드에서 종목코드 바로 사용: ${stock.stockCode}")
                    return stock.stockCode
                } else {
                    // 유효하지 않은 형식이면 즉시 빈 문자열 반환 (다른 방법 시도 안 함)
                    Timber.d("유효하지 않은 형식의 종목코드(${stock.stockCode}) -> 빈 문자열 반환")
                    return ""
                }
            }
            
            // stockCode 필드가 비어있거나 공백인 경우 아래 로직 실행
            Timber.d("stockCode 필드가 비어있어 다른 방법 시도: ${stock.name}")

            // 1. stockCode 필드가 있는 경우 (리플렉션 - 제거 또는 주석 처리 권장)
            // 이 부분은 위에서 stock.stockCode로 직접 접근하므로 중복임
            /*
            if (stock::class.java.declaredFields.any { it.name == "stockCode" }) {
                try {
                    val field = stock::class.java.getDeclaredField("stockCode")
                    field.isAccessible = true
                    val stockCodeReflected = field.get(stock) as? String
                    if (!stockCodeReflected.isNullOrBlank() && 
                        stockCodeReflected.length == 6 && 
                        stockCodeReflected.matches(Regex("^[0-9]{6}$")) &&
                        !stockCodeReflected.equals("N/A", ignoreCase = true)) {
                        Timber.d("리플렉션으로 stockCode 필드에서 종목코드 찾음: $stockCodeReflected")
                        return stockCodeReflected
                    }
                } catch (e: Exception) {
                    Timber.e(e, "stockCode 필드 접근 실패 (리플렉션)")
                }
            }
            */
            
            // 2. id에서 추출 시도
            try {
                val match = Regex(".*?([0-9]{6}).*?").find(stock.id)
                val potentialCode = match?.groupValues?.get(1)
                if (potentialCode != null && potentialCode.length == 6) {
                    Timber.d("ID에서 종목코드 추출 성공: $potentialCode")
                    return potentialCode
                }
            } catch (e: Exception) {
                 Timber.e(e, "ID에서 종목코드 추출 중 오류")
            }
            
            // 3. logoUrl에서 추출 시도
            try {
                if (stock.logoUrl.contains("stock_logos")) {
                    val match = Regex("stock_logos/([0-9]{6})").find(stock.logoUrl)
                    val potentialCode = match?.groupValues?.get(1)
                    if (potentialCode != null && potentialCode.length == 6) {
                         Timber.d("logoUrl에서 종목코드 추출 성공: $potentialCode")
                        return potentialCode
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "URL에서 종목코드 추출 실패")
            }
            
            // 아무것도 찾지 못한 경우 빈 문자열 반환
            Timber.d("종목 ${stock.name}의 유효한 종목코드를 찾을 수 없음 -> 최종 빈 문자열 반환")
            return ""
        }
        
        private fun formatTradingAmount(amount: Long): String {
            return when {
                amount >= 1_000_000_000_000 -> String.format("%.1f조", amount / 1_000_000_000_000.0)
                amount >= 100_000_000_000 -> String.format("%.1f천억", amount / 100_000_000_000.0)
                amount >= 10_000_000_000 -> String.format("%.1f백억", amount / 10_000_000_000.0)
                amount >= 1_000_000_000 -> String.format("%.1f억", amount / 1_000_000_000.0)
                amount >= 100_000_000 -> String.format("%.1f천만", amount / 100_000_000.0)
                amount >= 10_000_000 -> String.format("%.1f백만", amount / 10_000_000.0)
                else -> String.format("%.1f만", amount / 10_000.0)
            }
        }
    }
} 