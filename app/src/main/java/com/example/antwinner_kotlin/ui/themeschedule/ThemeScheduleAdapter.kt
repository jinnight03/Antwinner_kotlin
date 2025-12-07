package com.example.antwinner_kotlin.ui.themeschedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.antwinner_kotlin.databinding.ItemThemeScheduleBinding

class ThemeScheduleAdapter(
    private val onItemClicked: (ThemeScheduleItem) -> Unit // 아이템 클릭 리스너 콜백
) : ListAdapter<ThemeScheduleItem, ThemeScheduleAdapter.ThemeScheduleViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeScheduleViewHolder {
        val binding = ItemThemeScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThemeScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ThemeScheduleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ThemeScheduleViewHolder(private val binding: ItemThemeScheduleBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // 아이템 클릭 리스너 설정
            binding.root.setOnClickListener {
                val position = adapterPosition 
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position))
                }
            }
        }

        fun bind(item: ThemeScheduleItem) {
            binding.apply {
                // 테마 카테고리 이름 설정
                tvThemeName.text = item.category
                
                // 테마 로고 이미지 로딩 (antwinner.com/api/image/테마명.png)
                val logoUrl = "https://antwinner.com/api/image/${item.category}.png"
                ivThemeLogo.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(logoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .into(ivThemeLogo)

                // 나머지 데이터 설정
                tvScheduleDate.text = item.date
                tvScheduleTitle.text = item.title
                tvRelatedStocks.text = item.relatedStocks
                tvImpactValue.text = item.impact
                tvOpinionValue.text = item.opinion
                tvOpinionValue.setTextColor(ContextCompat.getColor(itemView.context, item.opinionColor))
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ThemeScheduleItem>() {
        override fun areItemsTheSame(oldItem: ThemeScheduleItem, newItem: ThemeScheduleItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ThemeScheduleItem, newItem: ThemeScheduleItem): Boolean {
            return oldItem == newItem
        }
    }
} 