package com.example.salarywidget.util

import java.text.DecimalFormat

/**
 * 货币格式化工具
 */
object CurrencyFormatter {
    private val twoDecimalFormat = DecimalFormat("#,##0.00")
    private val fourDecimalFormat = DecimalFormat("0.0000")

    /**
     * 格式化金额（两位小数，千位分隔）
     */
    fun formatMoney(amount: Double): String {
        return twoDecimalFormat.format(amount)
    }

    /**
     * 格式化每秒收入（四位小数）
     */
    fun formatPerSecond(amount: Double): String {
        return fourDecimalFormat.format(amount)
    }

    /**
     * 格式化百分比
     */
    fun formatPercent(value: Float): String {
        return String.format("%.1f%%", value * 100)
    }
}
