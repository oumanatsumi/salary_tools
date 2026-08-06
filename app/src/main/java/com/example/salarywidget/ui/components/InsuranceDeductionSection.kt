package com.example.salarywidget.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salarywidget.data.InsuranceMode
import com.example.salarywidget.data.SalaryMode
import com.example.salarywidget.data.UserSettings
import com.example.salarywidget.ui.ConfigViewModel

/**
 * 五险一金设置区域
 * 支持固定金额模式和比例模式切换
 */
@Composable
fun InsuranceDeductionSection(
    settings: UserSettings,
    viewModel: ConfigViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "五险一金",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (settings.salaryMode == SalaryMode.PRE_TAX) {
                // 模式切换
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = settings.insuranceMode == InsuranceMode.FIXED_AMOUNT,
                        onClick = { viewModel.updateInsuranceMode(InsuranceMode.FIXED_AMOUNT) },
                        label = { Text("固定金额") }
                    )
                    FilterChip(
                        selected = settings.insuranceMode == InsuranceMode.RATE,
                        onClick = { viewModel.updateInsuranceMode(InsuranceMode.RATE) },
                        label = { Text("比例模式") }
                    )
                }

                when (settings.insuranceMode) {
                    InsuranceMode.FIXED_AMOUNT -> {
                        FixedAmountInput(settings, viewModel)
                    }
                    InsuranceMode.RATE -> {
                        RateInputs(settings, viewModel)
                    }
                }
            } else {
                Text(
                    text = "税后模式下无需计算五险一金",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FixedAmountInput(
    settings: UserSettings,
    viewModel: ConfigViewModel
) {
    var textValue by remember(settings.insuranceFixedAmount) {
        mutableStateOf(
            if (settings.insuranceFixedAmount > 0) settings.insuranceFixedAmount.toLong().toString() else ""
        )
    }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() }
            textValue = filtered
            viewModel.updateInsuranceFixedAmount(filtered.toDoubleOrNull() ?: 0.0)
        },
        label = { Text("每月扣除金额") },
        suffix = { Text("元") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RateInputs(
    settings: UserSettings,
    viewModel: ConfigViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RateInputRow(
            label = "养老保险",
            rate = settings.pensionRate,
            onRateChange = { viewModel.updatePensionRate(it) }
        )
        RateInputRow(
            label = "医疗保险",
            rate = settings.medicalRate,
            onRateChange = { viewModel.updateMedicalRate(it) }
        )
        RateInputRow(
            label = "失业保险",
            rate = settings.unemploymentRate,
            onRateChange = { viewModel.updateUnemploymentRate(it) }
        )
        RateInputRow(
            label = "住房公积金",
            rate = settings.housingFundRate,
            onRateChange = { viewModel.updateHousingFundRate(it) }
        )

        // 合计显示
        val totalRate = settings.pensionRate + settings.medicalRate +
                settings.unemploymentRate + settings.housingFundRate
        val totalAmount = settings.monthlySalary * totalRate
        Text(
            text = "合计：${String.format("%.1f%%", totalRate * 100)} ≈ ¥${String.format("%.0f", totalAmount)}/月",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RateInputRow(
    label: String,
    rate: Double,
    onRateChange: (Double) -> Unit
) {
    var textValue by remember(rate) {
        mutableStateOf(String.format("%.1f", rate * 100))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(80.dp),
            fontSize = 14.sp
        )
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() || it == '.' }
                textValue = filtered
                val percentage = filtered.toDoubleOrNull() ?: 0.0
                onRateChange(percentage / 100.0)
            },
            suffix = { Text("%") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
    }
}
