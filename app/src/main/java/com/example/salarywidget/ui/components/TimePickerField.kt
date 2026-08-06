package com.example.salarywidget.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.salarywidget.util.TimeUtils

/**
 * 时间选择输入框
 * 点击后弹出系统时间选择器
 */
@Composable
fun TimePickerField(
    label: String,
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    OutlinedTextField(
        value = TimeUtils.formatTime(hour, minute),
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = modifier,
        trailingIcon = {
            TextButton(onClick = {
                TimePickerDialog(
                    context,
                    { _, selectedHour, selectedMinute ->
                        onTimeChange(selectedHour, selectedMinute)
                    },
                    hour,
                    minute,
                    true
                ).show()
            }) {
                Text("选择")
            }
        }
    )
}
