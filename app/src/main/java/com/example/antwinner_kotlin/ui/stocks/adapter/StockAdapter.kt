package com.example.antwinner_kotlin.ui.stocks.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.ui.stocks.model.IStockItem
import com.google.android.material.imageview.ShapeableImageView

class StockAdapter(
    private var stockItems: List<IStockItem>,
    private val onItemClick: (IStockItem) -> Unit
) : RecyclerView.Adapter<StockAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logoImageView: ShapeableImageView = itemView.findViewById(R.id.iv_stock_logo)
        val nameTextView: TextView = itemView.findViewById(R.id.tv_stock_name)
        val codeTextView: TextView = itemView.findViewById(R.id.tv_stock_code)
        val mainValueTextView: TextView = itemView.findViewById(R.id.tv_main_value)
        val additionalInfoTextView: TextView = itemView.findViewById(R.id.tv_additional_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stockItem = stockItems[position]
        
        // 종목명 설정
        holder.nameTextView.text = stockItem.name
        
        // 종목코드 설정
        holder.codeTextView.text = stockItem.code
        
        // 주요 값(등락률) 설정
        holder.mainValueTextView.text = stockItem.getMainValue()
        
        // 추가 정보 설정 (거래량, 거래대금, 외국인비율 등)
        if (stockItem.hasAdditionalInfo()) {
            holder.additionalInfoTextView.visibility = View.VISIBLE
            holder.additionalInfoTextView.text = stockItem.getAdditionalInfo()
        } else {
            holder.additionalInfoTextView.visibility = View.GONE
        }
        
        // 등락률에 따른 텍스트 색상 설정
        if (stockItem.fluctuation.startsWith("+")) {
            holder.mainValueTextView.setTextColor(Color.parseColor("#FF0000")) // 상승 (빨간색)
        } else if (stockItem.fluctuation.startsWith("-")) {
            holder.mainValueTextView.setTextColor(Color.parseColor("#0000FF")) // 하락 (파란색)
        } else {
            holder.mainValueTextView.setTextColor(Color.parseColor("#000000")) // 보합 (검은색)
        }
        
        // 종목 로고 로드
        Glide.with(holder.itemView.context)
            .load(stockItem.getLogoUrl())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_business_24)
            .error(R.drawable.ic_business_24)
            .into(holder.logoImageView)
        
        // 아이템 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            onItemClick(stockItem)
        }
    }

    override fun getItemCount(): Int = stockItems.size

    // 데이터 업데이트 메서드
    fun updateStocks(newStocks: List<IStockItem>) {
        stockItems = newStocks
        notifyDataSetChanged()
    }
} 