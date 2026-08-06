package com.example.salarywidget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salarywidget.data.InsuranceMode
import com.example.salarywidget.data.SalaryMode
import com.example.salarywidget.ui.ConfigViewModel
import com.example.salarywidget.ui.components.*
import com.example.salarywidget.util.CurrencyFormatter
import com.example.salarywidget.util.XiaomiOptimizationHelper
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    viewModel: ConfigViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val earningsState by viewModel.earningsState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val context = LocalContext.current

    // 保存成功提示
    if (saveSuccess) {
        LaunchedEffect(saveSuccess) {
            viewModel.clearSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "💰 薪资小组件",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 实时预览卡片
            LivePreviewCard(earningsState = earningsState)

            // 薪资设置
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
                        text = "薪资设置",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 薪资模式选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.salaryMode == SalaryMode.PRE_TAX,
                            onClick = { viewModel.updateSalaryMode(SalaryMode.PRE_TAX) },
                            label = { Text("税前月薪") }
                        )
                        FilterChip(
                            selected = settings.salaryMode == SalaryMode.POST_TAX,
                            onClick = { viewModel.updateSalaryMode(SalaryMode.POST_TAX) },
                            label = { Text("税后到手") }
                        )
                    }

                    // 月薪输入
                    SalaryInputField(
                        label = if (settings.salaryMode == SalaryMode.PRE_TAX) "税前月薪" else "到手月薪",
                        value = settings.monthlySalary,
                        onValueChange = { viewModel.updateMonthlySalary(it) }
                    )
                }
            }

            // 工作时间设置
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
                        text = "工作时间",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TimePickerField(
                            label = "上班时间",
                            hour = settings.workStartHour,
                            minute = settings.workStartMinute,
                            onTimeChange = { h, m -> viewModel.updateWorkStart(h, m) },
                            modifier = Modifier.weight(1f)
                        )
                        TimePickerField(
                            label = "下班时间",
                            hour = settings.workEndHour,
                            minute = settings.workEndMinute,
                            onTimeChange = { h, m -> viewModel.updateWorkEnd(h, m) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 午休时间设置
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
                        text = "午休时间",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TimePickerField(
                            label = "午休开始",
                            hour = settings.lunchStartHour,
                            minute = settings.lunchStartMinute,
                            onTimeChange = { h, m -> viewModel.updateLunchStart(h, m) },
                            modifier = Modifier.weight(1f)
                        )
                        TimePickerField(
                            label = "午休结束",
                            hour = settings.lunchEndHour,
                            minute = settings.lunchEndMinute,
                            onTimeChange = { h, m -> viewModel.updateLunchEnd(h, m) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 五险一金设置
            InsuranceDeductionSection(
                settings = settings,
                viewModel = viewModel
            )

            // 保存按钮
            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isSaving && settings.monthlySalary > 0
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "💾 保存设置",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 保存成功提示
            if (saveSuccess) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2E7D32)
                    )
                ) {
                    Text(
                        text = "✅ 保存成功！现在可以将小组件添加到桌面了",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 澎湃OS 设置引导
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚠️ 澎湃OS 设置建议",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "为确保小组件能实时更新，建议完成以下设置：",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )

                    OutlinedButton(
                        onClick = {
                            XiaomiOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("1. 关闭电池优化")
                    }

                    OutlinedButton(
                        onClick = {
                            XiaomiOptimizationHelper.openXiaomiAutoStartSettings(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("2. 开启自启动权限")
                    }

                    OutlinedButton(
                        onClick = {
                            XiaomiOptimizationHelper.openBatterySettings(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("3. 锁屏清理白名单")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
