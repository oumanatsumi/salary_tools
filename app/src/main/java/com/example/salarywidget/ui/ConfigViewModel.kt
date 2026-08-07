package com.example.salarywidget.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.salarywidget.data.InsuranceMode
import com.example.salarywidget.data.SalaryMode
import com.example.salarywidget.data.SettingsRepository
import com.example.salarywidget.data.UserSettings
import com.example.salarywidget.domain.EarningsCalculator
import com.example.salarywidget.domain.EarningsState
import com.example.salarywidget.domain.SalaryConfig
import com.example.salarywidget.util.TimeUtils
import com.example.salarywidget.widget.SalaryWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

/**
 * 设置页面的 ViewModel
 */
class ConfigViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository(application)

    private val _settings = MutableStateFlow(UserSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _earningsState = MutableStateFlow(EarningsState.empty())
    val earningsState: StateFlow<EarningsState> = _earningsState.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    // 用于实时更新预览的 Job
    private var previewJob: Job? = null

    init {
        // 加载已保存的设置
        viewModelScope.launch {
            settingsRepo.settingsFlow.collect { saved ->
                _settings.value = saved
                startPreviewUpdates()
            }
        }
    }

    /**
     * 启动预览实时更新（每秒更新一次）
     */
    private fun startPreviewUpdates() {
        previewJob?.cancel()
        previewJob = viewModelScope.launch {
            while (isActive) {
                val config = buildConfigFromSettings(_settings.value)
                _earningsState.value = EarningsCalculator.calculate(config)
                delay(1000L) // 每秒更新
            }
        }
    }

    fun updateSalaryMode(mode: SalaryMode) {
        _settings.value = _settings.value.copy(salaryMode = mode)
    }

    fun updateMonthlySalary(amount: Double) {
        _settings.value = _settings.value.copy(monthlySalary = amount)
    }

    fun updateWorkStart(hour: Int, minute: Int) {
        _settings.value = _settings.value.copy(workStartHour = hour, workStartMinute = minute)
    }

    fun updateWorkEnd(hour: Int, minute: Int) {
        _settings.value = _settings.value.copy(workEndHour = hour, workEndMinute = minute)
    }

    fun updateLunchStart(hour: Int, minute: Int) {
        _settings.value = _settings.value.copy(lunchStartHour = hour, lunchStartMinute = minute)
    }

    fun updateLunchEnd(hour: Int, minute: Int) {
        _settings.value = _settings.value.copy(lunchEndHour = hour, lunchEndMinute = minute)
    }

    fun updateInsuranceMode(mode: InsuranceMode) {
        _settings.value = _settings.value.copy(insuranceMode = mode)
    }

    fun updateInsuranceFixedAmount(amount: Double) {
        _settings.value = _settings.value.copy(insuranceFixedAmount = amount)
    }

    fun updatePensionRate(rate: Double) {
        _settings.value = _settings.value.copy(pensionRate = rate)
    }

    fun updateMedicalRate(rate: Double) {
        _settings.value = _settings.value.copy(medicalRate = rate)
    }

    fun updateUnemploymentRate(rate: Double) {
        _settings.value = _settings.value.copy(unemploymentRate = rate)
    }

    fun updateHousingFundRate(rate: Double) {
        _settings.value = _settings.value.copy(housingFundRate = rate)
    }

    fun updateWorkingDaysPerMonth(days: Double) {
        _settings.value = _settings.value.copy(workingDaysPerMonth = days)
    }

    /**
     * 保存设置
     */
    fun saveSettings() {
        viewModelScope.launch {
            _isSaving.value = true
            val updatedSettings = _settings.value.copy(isConfigured = true)
            settingsRepo.saveSettings(updatedSettings)
            _settings.value = updatedSettings
            _isSaving.value = false
            _saveSuccess.value = true

            // 保存后立即触发 Widget 更新
            try {
                SalaryWidget().updateAll(getApplication<Application>().applicationContext)
            } catch (e: Exception) {
                // Widget 可能还没被添加到桌面，忽略错误
            }
        }
    }

    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }

    /**
     * 从设置构建薪资配置（用于计算预览）
     */
    private fun buildConfigFromSettings(settings: UserSettings): SalaryConfig {
        val insuranceDeduction = when {
            settings.salaryMode == SalaryMode.POST_TAX -> 0.0
            settings.insuranceMode == InsuranceMode.FIXED_AMOUNT -> settings.insuranceFixedAmount
            else -> {
                val totalRate = settings.pensionRate + settings.medicalRate +
                        settings.unemploymentRate + settings.housingFundRate
                settings.monthlySalary * totalRate
            }
        }

        return SalaryConfig(
            monthlySalary = settings.monthlySalary,
            insuranceDeduction = insuranceDeduction,
            isPostTax = settings.salaryMode == SalaryMode.POST_TAX,
            workStart = TimeUtils.toLocalTime(settings.workStartHour, settings.workStartMinute),
            workEnd = TimeUtils.toLocalTime(settings.workEndHour, settings.workEndMinute),
            lunchStart = TimeUtils.toLocalTime(settings.lunchStartHour, settings.lunchStartMinute),
            lunchEnd = TimeUtils.toLocalTime(settings.lunchEndHour, settings.lunchEndMinute),
            workingDaysPerMonth = settings.workingDaysPerMonth
        )
    }

    override fun onCleared() {
        super.onCleared()
        previewJob?.cancel()
    }
}
