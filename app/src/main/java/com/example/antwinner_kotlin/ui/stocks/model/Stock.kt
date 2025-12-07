package com.example.antwinner_kotlin.ui.stocks.model

data class Stock(
    val name: String,
    val code: String,
    val changeRate: String,
    val price: String,
    val iconResId: Int,
    val isUp: Boolean
) 