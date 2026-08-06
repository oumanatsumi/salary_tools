package com.example.salarywidget.domain

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime

/**
 * EarningsCalculator 核心计算逻辑单元测试
 */
class EarningsCalculatorTest {

    private val testConfig = SalaryConfig(
        monthlySalary = 20000.0,
        insuranceDeduction = 3000.0,
        isPostTax = false,
        workStart = LocalTime.of(9, 0),
        workEnd = LocalTime.of(18, 0),
        lunchStart = LocalTime.of(12, 0),
        lunchEnd = LocalTime.of(13, 0),
        workingDaysPerMonth = 21.75
    )

    @Test
    fun `test net monthly salary calculation`() {
        // 税后月薪 = 20000 - 3000 = 17000
        assertEquals(17000.0, testConfig.netMonthlySalary, 0.01)
    }

    @Test
    fun `test daily salary calculation`() {
        // 日薪 = 17000 / 21.75 ≈ 781.61
        assertEquals(781.61, testConfig.dailySalary, 0.1)
    }

    @Test
    fun `test effective work minutes per day`() {
        // 有效工作分钟 = (18:00 - 9:00) - (13:00 - 12:00) = 540 - 60 = 480
        assertEquals(480, testConfig.effectiveWorkMinutesPerDay)
    }

    @Test
    fun `test per second rate`() {
        // 每秒收入 = 日薪 / (480 * 60) = 781.61 / 28800 ≈ 0.0271
        val expectedPerSecond = 781.61 / 28800.0
        assertEquals(expectedPerSecond, testConfig.perSecondRate, 0.0001)
    }

    @Test
    fun `before work returns zero earnings`() {
        val now = LocalTime.of(8, 30) // 8:30，上班前
        val state = EarningsCalculator.calculate(testConfig, now)

        assertEquals(0.0, state.currentEarnings, 0.01)
        assertEquals(0f, state.workdayProgress, 0.001f)
        assertEquals(WorkStatus.BEFORE_WORK, state.status)
    }

    @Test
    fun `during morning work calculates correctly`() {
        val now = LocalTime.of(10, 30) // 10:30，上午工作
        val state = EarningsCalculator.calculate(testConfig, now)

        // 已工作 90 分钟 = 5400 秒
        val expectedEarnings = testConfig.perSecondRate * 5400.0
        assertEquals(expectedEarnings, state.currentEarnings, 0.1)
        assertEquals(WorkStatus.WORKING, state.status)
        assertTrue(state.workdayProgress > 0f)
        assertTrue(state.workdayProgress < 1f)
    }

    @Test
    fun `during lunch break pauses earnings`() {
        val now = LocalTime.of(12, 30) // 12:30，午休中
        val state = EarningsCalculator.calculate(testConfig, now)

        // 午休期间收入等于上午工作结束时的收入
        // 上午工作分钟 = 12:00 - 9:00 = 180 分钟
        val morningMinutes = 180
        val expectedEarnings = testConfig.perSecondRate * morningMinutes * 60
        assertEquals(expectedEarnings, state.currentEarnings, 0.1)
        assertEquals(WorkStatus.LUNCH_BREAK, state.status)
    }

    @Test
    fun `during afternoon work calculates correctly`() {
        val now = LocalTime.of(14, 30) // 14:30，下午工作
        val state = EarningsCalculator.calculate(testConfig, now)

        // 已工作 = (12:00 - 9:00) + (14:30 - 13:00) = 180 + 90 = 270 分钟
        val workedMinutes = 270
        val expectedEarnings = testConfig.perSecondRate * workedMinutes * 60
        assertEquals(expectedEarnings, state.currentEarnings, 0.1)
        assertEquals(WorkStatus.WORKING, state.status)
    }

    @Test
    fun `after work returns full daily salary`() {
        val now = LocalTime.of(19, 0) // 19:00，下班后
        val state = EarningsCalculator.calculate(testConfig, now)

        assertEquals(testConfig.dailySalary, state.currentEarnings, 0.01)
        assertEquals(1f, state.workdayProgress, 0.001f)
        assertEquals(WorkStatus.AFTER_WORK, state.status)
        assertEquals(0.0, state.remainingEarnings, 0.01)
    }

    @Test
    fun `post tax mode ignores insurance deduction`() {
        val postTaxConfig = testConfig.copy(isPostTax = true)
        // 税后模式下，月薪即为到手金额
        assertEquals(20000.0, postTaxConfig.netMonthlySalary, 0.01)
    }

    @Test
    fun `hourly rate is correct`() {
        val now = LocalTime.of(10, 30)
        val state = EarningsCalculator.calculate(testConfig, now)

        // 时薪 = 日薪 / 8 = 781.61 / 8 ≈ 97.70
        val expectedHourly = testConfig.dailySalary / 8.0
        assertEquals(expectedHourly, state.hourlyRate, 0.01)
    }

    @Test
    fun `remaining earnings decreases as time progresses`() {
        val morning = EarningsCalculator.calculate(testConfig, LocalTime.of(10, 0))
        val afternoon = EarningsCalculator.calculate(testConfig, LocalTime.of(15, 0))

        assertTrue(morning.remainingEarnings > afternoon.remainingEarnings)
    }

    @Test
    fun `zero salary returns zero earnings`() {
        val zeroConfig = testConfig.copy(monthlySalary = 0.0)
        val state = EarningsCalculator.calculate(zeroConfig, LocalTime.of(12, 0))

        assertEquals(0.0, state.currentEarnings, 0.01)
        assertEquals(0.0, state.dailySalary, 0.01)
    }
}
