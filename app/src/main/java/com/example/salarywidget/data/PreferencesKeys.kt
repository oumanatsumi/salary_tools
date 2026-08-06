package com.example.salarywidget.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * DataStore 存储键定义
 */
object PreferencesKeys {
    val SALARY_MODE = stringPreferencesKey("salary_mode")
    val MONTHLY_SALARY = doublePreferencesKey("monthly_salary")

    val WORK_START_HOUR = intPreferencesKey("work_start_hour")
    val WORK_START_MINUTE = intPreferencesKey("work_start_minute")
    val WORK_END_HOUR = intPreferencesKey("work_end_hour")
    val WORK_END_MINUTE = intPreferencesKey("work_end_minute")

    val LUNCH_START_HOUR = intPreferencesKey("lunch_start_hour")
    val LUNCH_START_MINUTE = intPreferencesKey("lunch_start_minute")
    val LUNCH_END_HOUR = intPreferencesKey("lunch_end_hour")
    val LUNCH_END_MINUTE = intPreferencesKey("lunch_end_minute")

    val INSURANCE_MODE = stringPreferencesKey("insurance_mode")
    val INSURANCE_FIXED_AMOUNT = doublePreferencesKey("insurance_fixed_amount")

    val PENSION_RATE = doublePreferencesKey("pension_rate")
    val MEDICAL_RATE = doublePreferencesKey("medical_rate")
    val UNEMPLOYMENT_RATE = doublePreferencesKey("unemployment_rate")
    val HOUSING_FUND_RATE = doublePreferencesKey("housing_fund_rate")

    val WORKING_DAYS_PER_MONTH = doublePreferencesKey("working_days_per_month")
    val IS_CONFIGURED = booleanPreferencesKey("is_configured")
}
