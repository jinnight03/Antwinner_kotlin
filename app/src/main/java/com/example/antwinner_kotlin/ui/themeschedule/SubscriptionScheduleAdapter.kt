package com.example.antwinner_kotlin.ui.themeschedule

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.databinding.ItemSubscriptionScheduleBinding

class SubscriptionScheduleAdapter : ListAdapter<SubscriptionScheduleItem, SubscriptionScheduleAdapter.SubscriptionScheduleViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionScheduleViewHolder {
        val binding = ItemSubscriptionScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubscriptionScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubscriptionScheduleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubscriptionScheduleViewHolder(private val binding: ItemSubscriptionScheduleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SubscriptionScheduleItem) {
            binding.apply {
                tvSubscriptionDate.text = item.date
                tvSubscriptionTitle.text = item.title
                tvSubscriptionPrice.text = item.subscriptionPrice
                tvExpectedPrice.text = item.expectedPrice
                tvCompetitionRate.text = item.competitionRate
                tvUnderwriter.text = item.underwriter

                // 빨간 점 visibility 설정
                ivRedDot.visibility = if (item.showRedDot) View.VISIBLE else View.GONE

                // 아이템 클릭 이벤트 - IPO 상세 화면으로 이동
                root.setOnClickListener {
                    val context = root.context
                    val intent = Intent(context, IpoDetailActivity::class.java).apply {
                        putExtra(IpoDetailActivity.EXTRA_COMPANY_NAME, item.companyName)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SubscriptionScheduleItem>() {
        override fun areItemsTheSame(oldItem: SubscriptionScheduleItem, newItem: SubscriptionScheduleItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SubscriptionScheduleItem, newItem: SubscriptionScheduleItem): Boolean {
            return oldItem == newItem
        }
    }
} 