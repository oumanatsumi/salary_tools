//
//  SalaryModel.swift
//  SalaryWidget
//
//  薪资模型与枚举 —— 1:1 移植自 Android 版 UserSettings.kt
//

import Foundation

/// 薪资计算模式
enum SalaryMode: String, Codable {
    case preTax    // 税前月薪（需要扣除五险一金）
    case postTax   // 税后到手月薪（不再扣除五险一金）
}

/// 五险一金输入模式
enum InsuranceMode: String, Codable {
    case fixedAmount  // 固定金额
    case rate         // 比例模式
}

/// 工作状态枚举
enum WorkStatus: String, Codable {
    case beforeWork  // 上班前
    case working     // 工作中
    case lunchBreak  // 午休中
    case afterWork   // 下班后
    case dayOff      // 休息日（周末/假日）
}

/// 用户薪资设置
struct UserSettings: Codable {
    var salaryMode: SalaryMode = .preTax

    // 月薪金额（税前或税后，取决于 salaryMode）
    var monthlySalary: Double = 0

    // 工作时间
    var workStartHour: Int = 9
    var workStartMinute: Int = 0
    var workEndHour: Int = 18
    var workEndMinute: Int = 0

    // 午休时间
    var lunchStartHour: Int = 12
    var lunchStartMinute: Int = 0
    var lunchEndHour: Int = 13
    var lunchEndMinute: Int = 0

    // 五险一金模式
    var insuranceMode: InsuranceMode = .fixedAmount

    // 固定金额模式下的每月扣除金额
    var insuranceFixedAmount: Double = 0

    // 比例模式下的各项比例（小数形式，0.08 表示 8%）
    var pensionRate: Double = 0.08       // 养老保险
    var medicalRate: Double = 0.02       // 医疗保险
    var unemploymentRate: Double = 0.005 // 失业保险
    var housingFundRate: Double = 0.12   // 住房公积金

    // 每月工作天数（默认中国标准 21.75）
    var workingDaysPerMonth: Double = 21.75

    // 是否已完成初始设置
    var isConfigured: Bool = false

    /// 计算五险一金扣除额（与 Android ConfigViewModel.buildConfigFromSettings 逻辑一致）
    var insuranceDeduction: Double {
        if salaryMode == .postTax {
            return 0
        }
        switch insuranceMode {
        case .fixedAmount:
            return insuranceFixedAmount
        case .rate:
            let totalRate = pensionRate + medicalRate + unemploymentRate + housingFundRate
            return monthlySalary * totalRate
        }
    }
}

/// 当前收入状态
struct EarningsState {
    var currentEarnings: Double      // 当前已赚金额
    var dailyTotal: Double           // 今日总收入（日薪）
    var hourlyRate: Double           // 时薪
    var perSecondRate: Double        // 每秒收入
    var workdayProgress: Double      // 工作日进度 0.0~1.0
    var remainingEarnings: Double    // 今日剩余可赚金额
    var status: WorkStatus           // 当前工作状态
    var statusText: String           // 状态文案
    var isWorkday: Bool = true       // 是否是工作日

    /// 格式化的当前收入
    var formattedEarnings: String {
        String(format: "%.2f", currentEarnings)
    }

    /// 格式化的进度百分比
    var formattedProgress: String {
        String(format: "%.1f%%", workdayProgress * 100)
    }

    /// 格式化的时薪
    var formattedHourlyRate: String {
        String(format: "%.2f", hourlyRate)
    }

    /// 格式化的剩余金额
    var formattedRemaining: String {
        String(format: "%.2f", remainingEarnings)
    }

    /// 空状态（未配置时）
    static func empty() -> EarningsState {
        EarningsState(
            currentEarnings: 0,
            dailyTotal: 0,
            hourlyRate: 0,
            perSecondRate: 0,
            workdayProgress: 0,
            remainingEarnings: 0,
            status: .beforeWork,
            statusText: "请先完成设置"
        )
    }
}
