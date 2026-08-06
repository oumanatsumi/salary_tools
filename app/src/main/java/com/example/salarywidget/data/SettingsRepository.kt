package com.example.salarywidget.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 用户设置的 DataStore 仓库
 * 负责读取和持久化所有用户配置
 */
class SettingsRepository(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "salary_settings"
    )

    /**
     * 观察设置变化（Flow），设置变更时自动通知下游
     */
    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            salaryMode = prefs[PreferencesKeys.SALARY_MODE]
                ?.let { runCatching { SalaryMode.valueOf(it) }.getOrNull() }
                ?: SalaryMode.PRE_TAX,

            monthlySalary = prefs[PreferencesKeys.MONTHLY_SALARY] ?: 0.0,

            workStartHour = prefs[PreferencesKeys.WORK_START_HOUR] ?: 9,
            workStartMinute = prefs[PreferencesKeys.WORK_START_MINUTE] ?: 0,
            workEndHour = prefs[PreferencesKeys.WORK_END_HOUR] ?: 18,
            workEndMinute = prefs[PreferencesKeys.WORK_END_MINUTE] ?: 0,

            lunchStartHour = prefs[PreferencesKeys.LUNCH_START_HOUR] ?: 12,
            lunchStartMinute = prefs[PreferencesKeys.LUNCH_START_MINUTE] ?: 0,
            lunchEndHour = prefs[PreferencesKeys.LUNCH_END_HOUR] ?: 13,
            lunchEndMinute = prefs[PreferencesKeys.LUNCH_END_MINUTE] ?: 0,

            insuranceMode = prefs[PreferencesKeys.INSURANCE_MODE]
                ?.let { runCatching { InsuranceMode.valueOf(it) }.getOrNull() }
                ?: InsuranceMode.FIXED_AMOUNT,

            insuranceFixedAmount = prefs[PreferencesKeys.INSURANCE_FIXED_AMOUNT] ?: 0.0,

            pensionRate = prefs[PreferencesKeys.PENSION_RATE] ?: 0.08,
            medicalRate = prefs[PreferencesKeys.MEDICAL_RATE] ?: 0.02,
            unemploymentRate = prefs[PreferencesKeys.UNEMPLOYMENT_RATE] ?: 0.005,
            housingFundRate = prefs[PreferencesKeys.HOUSING_FUND_RATE] ?: 0.12,

            workingDaysPerMonth = prefs[PreferencesKeys.WORKING_DAYS_PER_MONTH] ?: 21.75,
            isConfigured = prefs[PreferencesKeys.IS_CONFIGURED] ?: false
        )
    }

    /**
     * 保存设置到 DataStore
     */
    suspend fun saveSettings(settings: UserSettings) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SALARY_MODE] = settings.salaryMode.name
            prefs[PreferencesKeys.MONTHLY_SALARY] = settings.monthlySalary

            prefs[PreferencesKeys.WORK_START_HOUR] = settings.workStartHour
            prefs[PreferencesKeys.WORK_START_MINUTE] = settings.workStartMinute
            prefs[PreferencesKeys.WORK_END_HOUR] = settings.workEndHour
            prefs[PreferencesKeys.WORK_END_MINUTE] = settings.workEndMinute

            prefs[PreferencesKeys.LUNCH_START_HOUR] = settings.lunchStartHour
            prefs[PreferencesKeys.LUNCH_START_MINUTE] = settings.lunchStartMinute
            prefs[PreferencesKeys.LUNCH_END_HOUR] = settings.lunchEndHour
            prefs[PreferencesKeys.LUNCH_END_MINUTE] = settings.lunchEndMinute

            prefs[PreferencesKeys.INSURANCE_MODE] = settings.insuranceMode.name
            prefs[PreferencesKeys.INSURANCE_FIXED_AMOUNT] = settings.insuranceFixedAmount

            prefs[PreferencesKeys.PENSION_RATE] = settings.pensionRate
            prefs[PreferencesKeys.MEDICAL_RATE] = settings.medicalRate
            prefs[PreferencesKeys.UNEMPLOYMENT_RATE] = settings.unemploymentRate
            prefs[PreferencesKeys.HOUSING_FUND_RATE] = settings.housingFundRate

            prefs[PreferencesKeys.WORKING_DAYS_PER_MONTH] = settings.workingDaysPerMonth
            prefs[PreferencesKeys.IS_CONFIGURED] = settings.isConfigured
        }
    }
}
