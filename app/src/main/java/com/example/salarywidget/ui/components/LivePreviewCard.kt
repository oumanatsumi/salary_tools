package com.example.salarywidget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salarywidget.domain.EarningsState
import com.example.salarywidget.domain.WorkStatus

/**
 * 实时预览卡片
 * 显示当前设置下 Widget 的大致效果
 */
@Composable
fun LivePreviewCard(
    earningsState: EarningsState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 标题
            Text(
                text = "📱 小组件预览（实时更新）",
                fontSize = 14.sp,
                color = Color(0xFFCCCCCC),
                fontWeight = FontWeight.Medium
            )

            // 大号金额
            Text(
                text = "¥ ${earningsState.formattedEarnings}",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )

            // 进度条
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { earningsState.workdayProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFF333355)
                )
                Text(
                    text = earningsState.formattedProgress,
                    fontSize = 12.sp,
                    color = Color(0xFFCCCCCC)
                )
            }

            // 辅助信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "时薪 ¥${earningsState.formattedHourlyRate}",
                    fontSize = 11.sp,
                    color = Color(0xFFAAAAAA)
                )
                Text(
                    text = earningsState.statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (earningsState.status) {
                        WorkStatus.LUNCH_BREAK -> Color(0xFFFFA726)
                        WorkStatus.AFTER_WORK -> Color(0xFF66BB6A)
                        WorkStatus.WORKING -> Color(0xFF42A5F5)
                        WorkStatus.BEFORE_WORK -> Color(0xFF9E9E9E)
                    }
                )
                Text(
                    text = "剩余 ¥${earningsState.formattedRemaining}",
                    fontSize = 11.sp,
                    color = Color(0xFFAAAAAA)
                )
            }
        }
    }
}
