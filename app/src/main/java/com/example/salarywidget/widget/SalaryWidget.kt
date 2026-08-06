package com.example.salarywidget.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.example.salarywidget.data.SettingsRepository
import com.example.salarywidget.domain.EarningsCalculator
import com.example.salarywidget.domain.EarningsState
import com.example.salarywidget.domain.SalaryConfig
import com.example.salarywidget.data.SalaryMode
import com.example.salarywidget.data.InsuranceMode
import com.example.salarywidget.util.TimeUtils
import kotlinx.coroutines.flow.first
import java.time.LocalTime

/**
 * 薪资桌面小组件
 * 显示今日已赚金额和工作日进度
 */
class SalaryWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settingsRepo = SettingsRepository(context)
        val settings = settingsRepo.settingsFlow.first()

        // 构建计算配置
        val config = if (settings.isConfigured && settings.monthlySalary > 0) {
            // 计算五险一金扣除额
            val insuranceDeduction = when {
                settings.salaryMode == SalaryMode.POST_TAX -> 0.0
                settings.insuranceMode == InsuranceMode.FIXED_AMOUNT -> settings.insuranceFixedAmount
                else -> {
                    val totalRate = settings.pensionRate + settings.medicalRate +
                            settings.unemploymentRate + settings.housingFundRate
                    settings.monthlySalary * totalRate
                }
            }

            SalaryConfig(
                monthlySalary = settings.monthlySalary,
                insuranceDeduction = insuranceDeduction,
                isPostTax = settings.salaryMode == SalaryMode.POST_TAX,
                workStart = TimeUtils.toLocalTime(settings.workStartHour, settings.workStartMinute),
                workEnd = TimeUtils.toLocalTime(settings.workEndHour, settings.workEndMinute),
                lunchStart = TimeUtils.toLocalTime(settings.lunchStartHour, settings.lunchStartMinute),
                lunchEnd = TimeUtils.toLocalTime(settings.lunchEndHour, settings.lunchEndMinute),
                workingDaysPerMonth = settings.workingDaysPerMonth
            )
        } else {
            null
        }

        // 计算当前收入
        val earningsState = if (config != null) {
            EarningsCalculator.calculate(config)
        } else {
            EarningsState.empty()
        }

        provideContent {
            WidgetContent(
                earnings = earningsState,
                isConfigured = settings.isConfigured && settings.monthlySalary > 0
            )
        }
    }
}
