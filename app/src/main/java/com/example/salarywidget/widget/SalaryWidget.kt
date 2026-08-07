package com.example.salarywidget.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.salarywidget.data.InsuranceMode
import com.example.salarywidget.data.SalaryMode
import com.example.salarywidget.data.SettingsRepository
import com.example.salarywidget.domain.EarningsCalculator
import com.example.salarywidget.domain.EarningsState
import com.example.salarywidget.domain.SalaryConfig
import com.example.salarywidget.util.TimeUtils
import kotlinx.coroutines.flow.first
import java.time.LocalTime

/**
 * 薪资桌面小组件
 * 显示今日已赚金额和工作日进度
 */
class SalaryWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        var earningsState = EarningsState.empty()
        var isConfigured = false

        try {
            val settingsRepo = SettingsRepository(context)
            val settings = settingsRepo.settingsFlow.first()

            isConfigured = settings.isConfigured && settings.monthlySalary > 0

            if (isConfigured) {
                val insuranceDeduction = when {
                    settings.salaryMode == SalaryMode.POST_TAX -> 0.0
                    settings.insuranceMode == InsuranceMode.FIXED_AMOUNT -> settings.insuranceFixedAmount
                    else -> {
                        val totalRate = settings.pensionRate + settings.medicalRate +
                                settings.unemploymentRate + settings.housingFundRate
                        settings.monthlySalary * totalRate
                    }
                }

                val config = SalaryConfig(
                    monthlySalary = settings.monthlySalary,
                    insuranceDeduction = insuranceDeduction,
                    isPostTax = settings.salaryMode == SalaryMode.POST_TAX,
                    workStart = TimeUtils.toLocalTime(settings.workStartHour, settings.workStartMinute),
                    workEnd = TimeUtils.toLocalTime(settings.workEndHour, settings.workEndMinute),
                    lunchStart = TimeUtils.toLocalTime(settings.lunchStartHour, settings.lunchStartMinute),
                    lunchEnd = TimeUtils.toLocalTime(settings.lunchEndHour, settings.lunchEndMinute),
                    workingDaysPerMonth = settings.workingDaysPerMonth
                )

                earningsState = EarningsCalculator.calculate(config)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compute widget state", e)
            // earningsState stays as empty(), isConfigured stays false
        }

        provideContent {
            WidgetContent(
                earnings = earningsState,
                isConfigured = isConfigured
            )
        }
    }

    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable
    ) {
        Log.e(TAG, "Composition error for widget $appWidgetId", throwable)
    }

    companion object {
        private const val TAG = "SalaryWidget"
    }
}
