package com.example.salarywidget.domain

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.LocalDate

/**
 * 核心收入计算器
 * 根据当前时间和薪资配置计算实时收入
 */
object EarningsCalculator {

    /**
     * 计算当前收入状态
     *
     * @param config 薪资配置
     * @param now 当前时间（传入参数便于测试）
     * @param today 当前日期（传入参数便于测试/模拟休息日）
     * @return 当前收入状态
     */
    fun calculate(
        config: SalaryConfig,
        now: LocalTime = LocalTime.now(),
        today: LocalDate = LocalDate.now()
    ): EarningsState {
        val workStartMinutes = config.workStart.hour * 60 + config.workStart.minute
        val workEndMinutes = config.workEnd.hour * 60 + config.workEnd.minute
        val lunchStartMinutes = config.lunchStart.hour * 60 + config.lunchStart.minute
        val lunchEndMinutes = config.lunchEnd.hour * 60 + config.lunchEnd.minute
        val nowMinutes = now.hour * 60 + now.minute

        val totalEffectiveMinutes = config.effectiveWorkMinutesPerDay

        // 无效配置
        if (totalEffectiveMinutes <= 0 || config.dailySalary <= 0) {
            return EarningsState(
                currentEarnings = 0.0,
                dailyTotal = config.dailySalary,
                hourlyRate = 0.0,
                perSecondRate = 0.0,
                workdayProgress = 0f,
                remainingEarnings = 0.0,
                status = WorkStatus.BEFORE_WORK,
                statusText = "配置有误"
            )
        }

        // 休息日检测（周六/周日）
        val dayOfWeek = today.dayOfWeek
        val isWorkday = dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY

        if (!isWorkday) {
            val dailySalary = config.dailySalary
            val effectiveWorkHours = totalEffectiveMinutes / 60.0
            val hourlyRate = if (effectiveWorkHours > 0) dailySalary / effectiveWorkHours else 0.0
            val perSecondRate = config.perSecondRate

            return EarningsState(
                currentEarnings = 0.0,
                dailyTotal = dailySalary,
                hourlyRate = hourlyRate,
                perSecondRate = perSecondRate,
                workdayProgress = 0f,
                remainingEarnings = dailySalary,
                status = WorkStatus.DAY_OFF,
                statusText = "今日休息",
                isWorkday = false
            )
        }

        // 计算时薪
        val effectiveWorkHours = totalEffectiveMinutes / 60.0
        val hourlyRate = if (effectiveWorkHours > 0) {
            config.dailySalary / effectiveWorkHours
        } else {
            0.0
        }

        val perSecondRate = config.perSecondRate

        // 根据当前时间判断状态
        val (status, workedMinutes, statusText) = when {
            // 上班前
            nowMinutes < workStartMinutes -> Triple(
                WorkStatus.BEFORE_WORK,
                0,
                "还没开始工作"
            )

            // 下班后
            nowMinutes >= workEndMinutes -> Triple(
                WorkStatus.AFTER_WORK,
                totalEffectiveMinutes,
                "今日已完成"
            )

            // 午休中
            nowMinutes >= lunchStartMinutes && nowMinutes < lunchEndMinutes -> Triple(
                WorkStatus.LUNCH_BREAK,
                lunchStartMinutes - workStartMinutes,
                "午休中…"
            )

            // 工作中（午休前）
            nowMinutes < lunchStartMinutes -> Triple(
                WorkStatus.WORKING,
                nowMinutes - workStartMinutes,
                "赚钱中…"
            )

            // 工作中（午休后）
            else -> Triple(
                WorkStatus.WORKING,
                (lunchStartMinutes - workStartMinutes) + (nowMinutes - lunchEndMinutes),
                "赚钱中…"
            )
        }

        // 计算当前收入
        val workedSeconds = workedMinutes * 60L
        val currentEarnings = perSecondRate * workedSeconds
        val progress = workedMinutes.toFloat() / totalEffectiveMinutes.toFloat()
        val remainingEarnings = (config.dailySalary - currentEarnings).coerceAtLeast(0.0)

        return EarningsState(
            currentEarnings = currentEarnings.coerceAtLeast(0.0),
            dailyTotal = config.dailySalary,
            hourlyRate = hourlyRate,
            perSecondRate = perSecondRate,
            workdayProgress = progress.coerceIn(0f, 1f),
            remainingEarnings = remainingEarnings,
            status = status,
            statusText = statusText,
            isWorkday = true
        )
    }
}
