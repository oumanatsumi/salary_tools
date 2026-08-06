package com.example.salarywidget.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * 薪资输入框
 * 带"元"后缀的数字输入
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryInputField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var textValue by remember(value) {
        mutableStateOf(if (value > 0) value.toLong().toString() else "")
    }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            // 只允许数字输入
            val filtered = newValue.filter { it.isDigit() }
            textValue = filtered
            onValueChange(filtered.toDoubleOrNull() ?: 0.0)
        },
        label = { Text(label) },
        suffix = { Text("元/月") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}
