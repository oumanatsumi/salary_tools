package com.example.salarywidget.domain

import java.time.LocalTime

/**
 * 薪资配置领域模型
 * 将 UserSettings 转换为计算所需的结构化配置
 */
data class SalaryConfig(
    val monthlySalary: Double,
    val insuranceDeduction: Double,
    val isPostTax: Boolean,
    val workStart: LocalTime,
    val workEnd: LocalTime,
    val lunchStart: LocalTime,
    val lunchEnd: LocalTime,
    val workingDaysPerMonth: Double
) {
    /**
     有效的每日工作分钟数（扣除午休）
     */
    val effectiveWorkMinutesPerDay: Int by lazy {
        val totalWorkMinutes = minutesBetween(workStart, workEnd)
        val lunchMinutes = minutesBetween(lunchStart, lunchEnd)
        (totalWorkMinutes - lunchMinutes).coerceAtLeast(0)
    }

    /**
     * 税后月薪
     */
    val netMonthlySalary: Double by lazy {
        if (isPostTax) {
            // 税后模式：月薪已是到手金额
            monthlySalary
        } else {
            // 税前模式：减去五险一金
            (monthlySalary - insuranceDeduction).coerceAtLeast(0.0)
        }
    }

    /**
     * 日薪（按 21.75 工作日）
     */
    val dailySalary: Double by lazy {
        if (workingDaysPerMonth > 0) {
            netMonthlySalary / workingDaysPerMonth
        } else {
            0.0
        }
    }

    /**
     * 每秒收入
     */
    val perSecondRate: Double by lazy {
        val totalWorkSeconds = effectiveWorkMinutesPerDay * 60.0
        if (totalWorkSeconds > 0) {
            dailySalary / totalWorkSeconds
        } else {
            0.0
        }
    }

    private fun minutesBetween(start: LocalTime, end: LocalTime): Int {
        val startMinutes = start.hour * 60 + start.minute
        val endMinutes = end.hour * 60 + end.minute
        return (endMinutes - startMinutes).coerceAtLeast(0)
    }
}
