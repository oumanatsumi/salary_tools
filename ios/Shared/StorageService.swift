//
//  StorageService.swift
//  SalaryWidget
//
//  App 与 Widget 共享的数据存取服务
//  通过 App Group 的 UserDefaults 共享设置
//

import Foundation
import WidgetKit

/// App Group 标识符（需在 Xcode 中为 App 和 Widget 都开启此 capability）
let appGroupId = "group.com.oumanatsumi.salarytools"

enum StorageService {

    /// 共享的 UserDefaults（App 与 Widget 都从这里读写）
    static var sharedDefaults: UserDefaults {
        UserDefaults(suiteName: appGroupId) ?? .standard
    }

    // MARK: - 读取

    static func loadSettings() -> UserSettings {
        let d = sharedDefaults

        // 如果从没保存过，返回默认值
        guard d.object(forKey: "salary_mode") != nil else {
            return UserSettings()
        }

        var settings = UserSettings()

        settings.salaryMode = SalaryMode(rawValue: d.string(forKey: "salary_mode") ?? "") ?? .preTax
        settings.monthlySalary = d.double(forKey: "monthly_salary")

        settings.workStartHour = d.integer(forKey: "work_start_hour")
        settings.workStartMinute = d.integer(forKey: "work_start_minute")
        settings.workEndHour = d.integer(forKey: "work_end_hour")
        settings.workEndMinute = d.integer(forKey: "work_end_minute")

        settings.lunchStartHour = d.integer(forKey: "lunch_start_hour")
        settings.lunchStartMinute = d.integer(forKey: "lunch_start_minute")
        settings.lunchEndHour = d.integer(forKey: "lunch_end_hour")
        settings.lunchEndMinute = d.integer(forKey: "lunch_end_minute")

        settings.insuranceMode = InsuranceMode(rawValue: d.string(forKey: "insurance_mode") ?? "") ?? .fixedAmount
        settings.insuranceFixedAmount = d.double(forKey: "insurance_fixed_amount")

        settings.pensionRate = d.double(forKey: "pension_rate")
        settings.medicalRate = d.double(forKey: "medical_rate")
        settings.unemploymentRate = d.double(forKey: "unemployment_rate")
        settings.housingFundRate = d.double(forKey: "housing_fund_rate")

        settings.workingDaysPerMonth = d.double(forKey: "working_days_per_month")
        settings.isConfigured = d.bool(forKey: "is_configured")

        // 修复：当 UserDefaults 没有显式保存过这些 key 时，double() 返回 0
        // 但比例模式默认值应该是 0.08/0.02/0.005/0.12
        if settings.pensionRate == 0 && d.object(forKey: "pension_rate") == nil {
            settings.pensionRate = 0.08
        }
        if settings.medicalRate == 0 && d.object(forKey: "medical_rate") == nil {
            settings.medicalRate = 0.02
        }
        if settings.unemploymentRate == 0 && d.object(forKey: "unemployment_rate") == nil {
            settings.unemploymentRate = 0.005
        }
        if settings.housingFundRate == 0 && d.object(forKey: "housing_fund_rate") == nil {
            settings.housingFundRate = 0.12
        }
        if settings.workingDaysPerMonth == 0 && d.object(forKey: "working_days_per_month") == nil {
            settings.workingDaysPerMonth = 21.75
        }

        return settings
    }

    // MARK: - 写入

    static func saveSettings(_ settings: UserSettings) {
        let d = sharedDefaults

        d.set(settings.salaryMode.rawValue, forKey: "salary_mode")
        d.set(settings.monthlySalary, forKey: "monthly_salary")

        d.set(settings.workStartHour, forKey: "work_start_hour")
        d.set(settings.workStartMinute, forKey: "work_start_minute")
        d.set(settings.workEndHour, forKey: "work_end_hour")
        d.set(settings.workEndMinute, forKey: "work_end_minute")

        d.set(settings.lunchStartHour, forKey: "lunch_start_hour")
        d.set(settings.lunchStartMinute, forKey: "lunch_start_minute")
        d.set(settings.lunchEndHour, forKey: "lunch_end_hour")
        d.set(settings.lunchEndMinute, forKey: "lunch_end_minute")

        d.set(settings.insuranceMode.rawValue, forKey: "insurance_mode")
        d.set(settings.insuranceFixedAmount, forKey: "insurance_fixed_amount")

        d.set(settings.pensionRate, forKey: "pension_rate")
        d.set(settings.medicalRate, forKey: "medical_rate")
        d.set(settings.unemploymentRate, forKey: "unemployment_rate")
        d.set(settings.housingFundRate, forKey: "housing_fund_rate")

        d.set(settings.workingDaysPerMonth, forKey: "working_days_per_month")
        d.set(settings.isConfigured, forKey: "is_configured")

        // 保存后触发 Widget 刷新（iOS 系统决定实际刷新时机）
        WidgetCenter.shared.reloadAllTimelines()
    }
}
