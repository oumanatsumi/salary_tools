package com.example.salarywidget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salarywidget.R
import com.example.salarywidget.domain.EarningsState
import com.example.salarywidget.domain.WorkStatus
import com.example.salarywidget.ui.theme.*

/**
 * 实时预览卡片
 * 显示当前设置下 Widget 的大致效果
 *
 * 与 WidgetContent 保持视觉一致：
 * - 相同的颜色 token（来自 Color.kt）
 * - 相同的 4-step type scale（Material 版）
 * - 相同的矢量 icons
 * - 相同的 per-digit 金额渲染
 */
@Composable
fun LivePreviewCard(
    earningsState: EarningsState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = WidgetBackground
        ),
        shape = MaterialTheme.shapes.medium
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
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )

            // 标题行（icon + text）
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_coin),
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "今日已赚",
                    fontSize = 16.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            // 金额（per-character 等宽 cell，"数字屏" 视觉）
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "¥",
                    fontSize = 16.sp,
                    color = MoneyPrimary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(6.dp))
                earningsState.formattedEarnings.forEach { char ->
                    val cellWidth = if (char == '.') 10.dp else 22.dp
                    Box(
                        modifier = Modifier.width(cellWidth),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            fontSize = 36.sp,
                            color = MoneyPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

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
                        .height(6.dp),
                    color = MoneyPrimary,
                    trackColor = ProgressTrack
                )
                Text(
                    text = earningsState.formattedProgress,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            // 状态行（icon + text）
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(statusIconRes(earningsState.status)),
                    contentDescription = null,
                    tint = statusColor(earningsState.status),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = earningsState.statusText,
                    fontSize = 13.sp,
                    color = statusColor(earningsState.status),
                    fontWeight = FontWeight.Medium
                )
            }

            // Footer 行（时薪 | 剩余）
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "时薪 ¥${earningsState.formattedHourlyRate}",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(11.dp)
                        .background(Divider)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "剩余 ¥${earningsState.formattedRemaining}",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
        }
    }
}

private fun statusIconRes(status: WorkStatus): Int = when (status) {
    WorkStatus.BEFORE_WORK -> R.drawable.ic_status_idle
    WorkStatus.WORKING -> R.drawable.ic_status_working
    WorkStatus.LUNCH_BREAK -> R.drawable.ic_status_lunch
    WorkStatus.AFTER_WORK -> R.drawable.ic_status_done
    WorkStatus.DAY_OFF -> R.drawable.ic_status_rest
}

private fun statusColor(status: WorkStatus): Color = when (status) {
    WorkStatus.BEFORE_WORK -> StatusIdle
    WorkStatus.WORKING -> StatusWorking
    WorkStatus.LUNCH_BREAK -> StatusLunch
    WorkStatus.AFTER_WORK -> StatusDone
    WorkStatus.DAY_OFF -> StatusRest
}
