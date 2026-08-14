//
//  EarningsCalculatorTests.swift
//  SalaryWidgetTests
//
//  计算逻辑单元测试 —— 对照 Android 版 EarningsCalculatorTest.kt
//  验证 Swift 移植与 Kotlin 结果一致
//

import XCTest
@testable import SalaryWidget

final class EarningsCalculatorTests: XCTestCase {

    // 与 Kotlin 测试相同的配置：月薪 20000，五险一金 3000，9:00-18:00，午休 12:00-13:00
    private var testConfig: SalaryConfig {
        SalaryConfig(
            monthlySalary: 20000,
            insuranceDeduction: 3000,
            isPostTax: false,
            workStart: (9, 0),
            workEnd: (18, 0),
            lunchStart: (12, 0),
            lunchEnd: (13, 0),
            workingDaysPerMonth: 21.75
        )
    }

    private func date(_ hour: Int, _ minute: Int) -> Date {
        // 使用一个固定的工作日（周三）避免周末干扰
        var comps = DateComponents()
        comps.year = 2026
        comps.month = 8
        comps.day = 12   // 2026-08-12 是周三
        comps.hour = hour
        comps.minute = minute
        return Calendar.current.date(from: comps)!
    }

    func testNetMonthlySalary() {
        // 税后月薪 = 20000 - 3000 = 17000
        XCTAssertEqual(testConfig.netMonthlySalary, 17000.0, accuracy: 0.01)
    }

    func testDailySalary() {
        // 日薪 = 17000 / 21.75 ≈ 781.61
        XCTAssertEqual(testConfig.dailySalary, 781.61, accuracy: 0.1)
    }

    func testEffectiveWorkMinutesPerDay() {
        // (18:00 - 9:00) - (13:00 - 12:00) = 540 - 60 = 480
        XCTAssertEqual(testConfig.effectiveWorkMinutesPerDay, 480)
    }

    func testPerSecondRate() {
        // 每秒 = 日薪 / (480*60)
        let expected = 781.61 / 28800.0
        XCTAssertEqual(testConfig.perSecondRate, expected, accuracy: 0.0001)
    }

    func testBeforeWorkReturnsZero() {
        let state = EarningsCalculator.calculate(config: testConfig, now: date(8, 30))
        XCTAssertEqual(state.currentEarnings, 0, accuracy: 0.01)
        XCTAssertEqual(state.workdayProgress, 0, accuracy: 0.001)
        XCTAssertEqual(state.status, .beforeWork)
    }

    func testDuringMorningWork() {
        let state = EarningsCalculator.calculate(config: testConfig, now: date(10, 30))
        // 已工作 90 分钟 = 5400 秒
        let expected = testConfig.perSecondRate * 5400
        XCTAssertEqual(state.currentEarnings, expected, accuracy: 0.1)
        XCTAssertEqual(state.status, .working)
        XCTAssertGreaterThan(state.workdayProgress, 0)
        XCTAssertLessThan(state.workdayProgress, 1)
    }

    func testDuringLunchBreakPauses() {
        let state = EarningsCalculator.calculate(config: testConfig, now: date(12, 30))
        // 上午工作 = 12:00 - 9:00 = 180 分钟
        let morningMinutes = 180.0
        let expected = testConfig.perSecondRate * morningMinutes * 60
        XCTAssertEqual(state.currentEarnings, expected, accuracy: 0.1)
        XCTAssertEqual(state.status, .lunchBreak)
    }

    func testDuringAfternoonWork() {
        let state = EarningsCalculator.calculate(config: testConfig, now: date(14, 30))
        // (12:00-9:00) + (14:30-13:00) = 180 + 90 = 270 分钟
        let workedMinutes = 270.0
        let expected = testConfig.perSecondRate * workedMinutes * 60
        XCTAssertEqual(state.currentEarnings, expected, accuracy: 0.1)
        XCTAssertEqual(state.status, .working)
    }

    func testAfterWorkReturnsFullSalary() {
        let state = EarningsCalculator.calculate(config: testConfig, now: date(19, 0))
        XCTAssertEqual(state.currentEarnings, testConfig.dailySalary, accuracy: 0.01)
        XCTAssertEqual(state.workdayProgress, 1.0, accuracy: 0.001)
        XCTAssertEqual(state.status, .afterWork)
        XCTAssertEqual(state.remainingEarnings, 0, accuracy: 0.01)
    }

    func testPostTaxIgnoresInsurance() {
        let postTaxConfig = SalaryConfig(
            monthlySalary: 20000,
            insuranceDeduction: 3000,
            isPostTax: true,
            workStart: (9, 0),
            workEnd: (18, 0),
            lunchStart: (12, 0),
            lunchEnd: (13, 0),
            workingDaysPerMonth: 21.75
        )
        // 税后模式：月薪即为到手金额
        XCTAssertEqual(postTaxConfig.netMonthlySalary, 20000.0, accuracy: 0.01)
    }

    func testHourlyRate() {
        let state = EarningsCalculator.calculate(config: testConfig, now: date(10, 30))
        // 时薪 = 日薪 / 8
        let expected = testConfig.dailySalary / 8.0
        XCTAssertEqual(state.hourlyRate, expected, accuracy: 0.01)
    }

    func testRemainingDecreasesOverTime() {
        let morning = EarningsCalculator.calculate(config: testConfig, now: date(10, 0))
        let afternoon = EarningsCalculator.calculate(config: testConfig, now: date(15, 0))
        XCTAssertGreaterThan(morning.remainingEarnings, afternoon.remainingEarnings)
    }

    func testWeekendIsDayOff() {
        // 周六
        var comps = DateComponents()
        comps.year = 2026
        comps.month = 8
        comps.day = 15   // 2026-08-15 是周六
        comps.hour = 10
        comps.minute = 0
        let saturday = Calendar.current.date(from: comps)!

        let state = EarningsCalculator.calculate(config: testConfig, now: saturday)
        XCTAssertEqual(state.status, .dayOff)
        XCTAssertFalse(state.isWorkday)
        XCTAssertEqual(state.statusText, "今日休息")
    }
}
