package com.example.antwinner_kotlin.ui.stockdetail.fragments

import android.content.Context
import android.graphics.Canvas
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.example.antwinner_kotlin.R

class CustomMarkerView(
    context: Context,
    layoutResource: Int,
    private val currentAnnualData: () -> List<PerformanceData>,
    private val currentQuarterlyData: () -> List<QuarterlyPerformanceData>,
    private val isAnnualSelected: () -> Boolean
) : MarkerView(context, layoutResource) {

    private val tvContent: TextView = findViewById(R.id.tvContent)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let { entry ->
            try {
                val dataSetIndex = highlight?.dataSetIndex ?: 0
                val entryIndex = entry.x.toInt()
                
                val (categoryName, value) = if (isAnnualSelected()) {
                    val data = currentAnnualData()
                    if (entryIndex < data.size) {
                        val item = data[entryIndex]
                        when (dataSetIndex) {
                            0 -> "매출" to item.revenue
                            1 -> "영업이익" to item.operatingProfit
                            2 -> "순이익" to item.netIncome
                            else -> "매출" to item.revenue
                        }
                    } else {
                        "데이터" to 0L
                    }
                } else {
                    val data = currentQuarterlyData()
                    if (entryIndex < data.size) {
                        val item = data[entryIndex]
                        when (dataSetIndex) {
                            0 -> "매출" to item.revenue
                            1 -> "영업이익" to item.operatingProfit
                            2 -> "순이익" to item.netIncome
                            else -> "매출" to item.revenue
                        }
                    } else {
                        "데이터" to 0L
                    }
                }
                
                val formattedValue = PerformanceUtils.formatChartValue(value)
                tvContent.text = "$categoryName\n$formattedValue"
                
            } catch (e: Exception) {
                tvContent.text = "데이터 오류"
            }
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat() - 10f)
    }
} 