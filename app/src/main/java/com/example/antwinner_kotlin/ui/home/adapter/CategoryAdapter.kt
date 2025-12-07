package com.example.antwinner_kotlin.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.antwinner_kotlin.R
import com.example.antwinner_kotlin.ui.home.model.Category

class CategoryAdapter(private val categories: List<Category>) : 
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconImageView: ImageView = itemView.findViewById(R.id.iv_category_icon)
        val nameTextView: TextView = itemView.findViewById(R.id.tv_category_name)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.iconImageView.setImageResource(category.iconResId)
        holder.nameTextView.text = category.name
    }
    
    override fun getItemCount() = categories.size
} 