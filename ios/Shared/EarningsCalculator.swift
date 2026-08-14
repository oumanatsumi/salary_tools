//
//  EarningsCalculator.swift
//  SalaryWidget
//
//  薪资配置与核心计算器 —— 1:1 移植自 Android 版
//  SalaryConfig.kt + EarningsCalculator.kt
//

import Foundation

/// 薪资配置领域模型
struct SalaryConfig {
    let monthlySalary: Double
    let insuranceDeduction: Double
    let isPostTax: Bool

    // 时间用 (hour, minute) 元组表示，替代 Kotlin 的 LocalTime
    let workStart: (hour: Int, minute: Int)
    let workEnd: (hour: Int, minute: Int)
    let lunchStart: (hour: Int, minute: Int)
    let lunchEnd: (hour: Int, minute: Int)
    let workingDaysPerMonth: Double

    /// 有效的每日工作分钟数（扣除午休）
    var effectiveWorkMinutesPerDay: Int {
        let totalWorkMinutes = minutesBetween(workStart, workEnd)
        let lunchMinutes = minutesBetween(lunchStart, lunchEnd)
        return max(0, totalWorkMinutes - lunchMinutes)
    }

    /// 税后月薪
    var netMonthlySalary: Double {
        if isPostTax {
            return monthlySalary
        } else {
            return max(0, monthlySalary - insuranceDeduction)
        }
    }

    /// 日薪（按 21.75 工作日）
    var dailySalary: Double {
        workingDaysPerMonth > 0 ? netMonthlySalary / workingDaysPerMonth : 0
    }

    /// 每秒收入
    var perSecondRate: Double {
        let totalWorkSeconds = effectiveWorkMinutesPerDay * 60
        if totalWorkSeconds > 0 {
            return dailySalary / Double(totalWorkSeconds)
        }
        return 0
    }

    private func minutesBetween(_ start: (hour: Int, minute: Int), _ end: (hour: Int, minute: Int)) -> Int {
        let startMinutes = start.hour * 60 + start.minute
        let endMinutes = end.hour * 60 + end.minute
        return max(0, endMinutes - startMinutes)
    }

    /// 从 UserSettings 构建配置
    static func from(_ settings: UserSettings) -> SalaryConfig {
        SalaryConfig(
            monthlySalary: settings.monthlySalary,
            insuranceDeduction: settings.insuranceDeduction,
            isPostTax: settings.salaryMode == .postTax,
            workStart: (settings.workStartHour, settings.workStartMinute),
            workEnd: (settings.workEndHour, settings.workEndMinute),
            lunchStart: (settings.lunchStartHour, settings.lunchStartMinute),
            lunchEnd: (settings.lunchEndHour, settings.lunchEndMinute),
            workingDaysPerMonth: settings.workingDaysPerMonth
        )
    }
}

/// 核心收入计算器
enum EarningsCalculator {

    /// 计算当前收入状态
    /// - Parameters:
    ///   - config: 薪资配置
    ///   - now: 当前时间（传入便于测试）
    /// - Returns: 当前收入状态
    static func calculate(config: SalaryConfig, now: Date = Date()) -> EarningsState {
        let calendar = Calendar.current
        let comps = calendar.dateComponents([.hour, .minute, .weekday], from: now)

        let workStartMinutes = config.workStart.hour * 60 + config.workStart.minute
        let workEndMinutes = config.workEnd.hour * 60 + config.workEnd.minute
        let lunchStartMinutes = config.lunchStart.hour * 60 + config.lunchStart.minute
        let lunchEndMinutes = config.lunchEnd.hour * 60 + config.lunchEnd.minute
        let nowMinutes = (comps.hour ?? 0) * 60 + (comps.minute ?? 0)

        let totalEffectiveMinutes = config.effectiveWorkMinutesPerDay

        // 无效配置
        if totalEffectiveMinutes <= 0 || config.dailySalary <= 0 {
            return EarningsState(
                currentEarnings: 0,
                dailyTotal: config.dailySalary,
                hourlyRate: 0,
                perSecondRate: 0,
                workdayProgress: 0,
                remainingEarnings: 0,
                status: .beforeWork,
                statusText: "配置有误"
            )
        }

        // 休息日检测（周六/周日）
        // Calendar weekday: 1=周日 2=周一 ... 7=周六
        let weekday = comps.weekday ?? 2
        let isWorkday = weekday != 1 && weekday != 7

        if !isWorkday {
            let dailySalary = config.dailySalary
            let effectiveWorkHours = Double(totalEffectiveMinutes) / 60.0
            let hourlyRate = effectiveWorkHours > 0 ? dailySalary / effectiveWorkHours : 0
            let perSecondRate = config.perSecondRate

            return EarningsState(
                currentEarnings: 0,
                dailyTotal: dailySalary,
                hourlyRate: hourlyRate,
                perSecondRate: perSecondRate,
                workdayProgress: 0,
                remainingEarnings: dailySalary,
                status: .dayOff,
                statusText: "今日休息",
                isWorkday: false
            )
        }

        // 计算时薪
        let effectiveWorkHours = Double(totalEffectiveMinutes) / 60.0
        let hourlyRate = effectiveWorkHours > 0 ? config.dailySalary / effectiveWorkHours : 0
        let perSecondRate = config.perSecondRate

        // 根据当前时间判断状态
        let status: WorkStatus
        let workedMinutes: Int
        let statusText: String

        if nowMinutes < workStartMinutes {
            // 上班前
            status = .beforeWork
            workedMinutes = 0
            statusText = "还没开始工作"
        } else if nowMinutes >= workEndMinutes {
            // 下班后
            status = .afterWork
            workedMinutes = totalEffectiveMinutes
            statusText = "今日已完成"
        } else if nowMinutes >= lunchStartMinutes && nowMinutes < lunchEndMinutes {
            // 午休中
            status = .lunchBreak
            workedMinutes = lunchStartMinutes - workStartMinutes
            statusText = "午休中…"
        } else if nowMinutes < lunchStartMinutes {
            // 工作中（午休前）
            status = .working
            workedMinutes = nowMinutes - workStartMinutes
            statusText = "赚钱中…"
        } else {
            // 工作中（午休后）
            status = .working
            workedMinutes = (lunchStartMinutes - workStartMinutes) + (nowMinutes - lunchEndMinutes)
            statusText = "赚钱中…"
        }

        // 计算当前收入
        let workedSeconds = workedMinutes * 60
        let currentEarnings = perSecondRate * Double(workedSeconds)
        let progress = Double(workedMinutes) / Double(totalEffectiveMinutes)
        let remainingEarnings = max(0, config.dailySalary - currentEarnings)

        return EarningsState(
            currentEarnings: max(0, currentEarnings),
            dailyTotal: config.dailySalary,
            hourlyRate: hourlyRate,
            perSecondRate: perSecondRate,
            workdayProgress: min(1, max(0, progress)),
            remainingEarnings: remainingEarnings,
            status: status,
            statusText: statusText,
            isWorkday: true
        )
    }
}
