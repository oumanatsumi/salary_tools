package com.example.salarywidget.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.salarywidget.domain.EarningsState
import com.example.salarywidget.domain.WorkStatus

/**
 * Widget 内容布局（Glance Composable）
 * 4x2 桌面小组件
 *
 * Glance 使用受限的 Compose 子集，注意：
 * - 颜色用 ColorProvider 包装
 * - 对齐用 Alignment.Top / Alignment.Start 等（不是 Vertical.Top）
 * - LinearProgressIndicator 在 androidx.glance.appwidget 包下
 */
@Composable
fun WidgetContent(
    earnings: EarningsState,
    isConfigured: Boolean
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // 标题行
        Text(
            text = if (isConfigured) "💰 今日已赚" else "💰 薪资小组件",
            style = TextStyle(
                color = ColorProvider(Color(0xFFCCCCCC)),
                fontSize = 13.sp
            )
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // 大号金额显示
        Text(
            text = if (isConfigured) "¥ ${earnings.formattedEarnings}" else "点击设置薪资",
            style = TextStyle(
                color = if (isConfigured) {
                    ColorProvider(Color(0xFF4CAF50))
                } else {
                    ColorProvider(Color(0xFFAAAAAA))
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        // 进度条
        if (isConfigured) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = earnings.workdayProgress,
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(8.dp),
                    color = ColorProvider(Color(0xFF4CAF50)),
                    trackColor = ColorProvider(Color(0xFF333355))
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = earnings.formattedProgress,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFCCCCCC)),
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            // 状态文字
            Text(
                text = earnings.statusText,
                style = TextStyle(
                    color = when (earnings.status) {
                        WorkStatus.LUNCH_BREAK -> ColorProvider(Color(0xFFFFA726))
                        WorkStatus.AFTER_WORK -> ColorProvider(Color(0xFF66BB6A))
                        WorkStatus.WORKING -> ColorProvider(Color(0xFF42A5F5))
                        WorkStatus.BEFORE_WORK -> ColorProvider(Color(0xFF9E9E9E))
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = GlanceModifier.height(6.dp))

            // 辅助信息行
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "时薪 ¥${earnings.formattedHourlyRate}",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFAAAAAA)),
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = "|",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF555555)),
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = "剩余 ¥${earnings.formattedRemaining}",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFAAAAAA)),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
