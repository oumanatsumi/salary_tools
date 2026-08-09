package com.example.salarywidget.domain

/**
 * 当前收入状态
 * 包含所有展示所需的计算结果
 */
data class EarningsState(
    val currentEarnings: Double,       // 当前已赚金额
    val dailyTotal: Double,            // 今日总收入（日薪）
    val hourlyRate: Double,            // 时薪
    val perSecondRate: Double,         // 每秒收入
    val workdayProgress: Float,        // 工作日进度 0.0~1.0
    val remainingEarnings: Double,     // 今日剩余可赚金额
    val status: WorkStatus,            // 当前工作状态
    val statusText: String,            // 状态文案
    val isWorkday: Boolean = true      // 是否是工作日（休息日显示不同画面）
) {
    /**
     * 格式化的当前收入
     */
    val formattedEarnings: String
        get() = String.format("%.2f", currentEarnings)

    /**
     * 格式化的进度百分比
     */
    val formattedProgress: String
        get() = String.format("%.1f%%", workdayProgress * 100)

    /**
     * 格式化的时薪
     */
    val formattedHourlyRate: String
        get() = String.format("%.2f", hourlyRate)

    /**
     * 格式化的剩余金额
     */
    val formattedRemaining: String
        get() = String.format("%.2f", remainingEarnings)

    /**
     * 格式化的每秒收入
     */
    val formattedPerSecond: String
        get() = String.format("%.4f", perSecondRate)

    companion object {
        fun empty(): EarningsState = EarningsState(
            currentEarnings = 0.0,
            dailyTotal = 0.0,
            hourlyRate = 0.0,
            perSecondRate = 0.0,
            workdayProgress = 0f,
            remainingEarnings = 0.0,
            status = WorkStatus.BEFORE_WORK,
            statusText = "请先完成设置"
        )
    }
}

/**
 * 工作状态枚举
 */
enum class WorkStatus {
    BEFORE_WORK,    // 上班前
    WORKING,        // 工作中
    LUNCH_BREAK,    // 午休中
    AFTER_WORK,     // 下班后
    DAY_OFF         // 休息日（周末/假日）
}
