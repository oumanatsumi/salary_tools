package com.example.salarywidget.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.salarywidget.R
import com.example.salarywidget.domain.EarningsState
import com.example.salarywidget.domain.WorkStatus
import com.example.salarywidget.ui.theme.Divider
import com.example.salarywidget.ui.theme.MoneyPrimary
import com.example.salarywidget.ui.theme.ProgressTrack
import com.example.salarywidget.ui.theme.StatusDone
import com.example.salarywidget.ui.theme.StatusIdle
import com.example.salarywidget.ui.theme.StatusLunch
import com.example.salarywidget.ui.theme.StatusRest
import com.example.salarywidget.ui.theme.StatusWorking
import com.example.salarywidget.ui.theme.TextPrimary
import com.example.salarywidget.ui.theme.TextSecondary
import com.example.salarywidget.ui.theme.TextTertiary
import com.example.salarywidget.ui.theme.WidgetBackground
import com.example.salarywidget.ui.theme.WidgetTypography

/**
 * Widget 内容布局（Glance Composable）
 *
 * 紧凑布局适配 4×2 widget（~78dp 可用高度）：
 * - 单行 Text 金额（28sp），不拆 cell —— 解决宽度溢出截断
 * - 合并 footer → status 行右边 —— 解决高度溢出裁掉
 * - sparse spacing（2-4dp）—— 最大化信息密度
 *
 * 动态效果经过充分评估已放弃：
 * Glance 底层是 RemoteViews 无 Compose 动画引擎，
 * Xiaomi HyperOS 冻结频繁 widget update。
 */
@Composable
fun WidgetContent(
    earnings: EarningsState,
    isConfigured: Boolean
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(WidgetBackground))
            .padding(12.dp),
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Top
    ) {
        // 标题行（icon + text）
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(R.drawable.ic_coin),
                contentDescription = null,
                modifier = GlanceModifier.size(14.dp)
            )
            Spacer(GlanceModifier.width(6.dp))
            Text(
                text = if (isConfigured) "今日已赚" else "薪资小组件",
                style = WidgetTypography.title(ColorProvider(TextSecondary))
            )
        }

        Spacer(GlanceModifier.height(4.dp))

        // 金额（单行 Text，避免 cell 截断）
        if (isConfigured && earnings.isWorkday) {
            Text(
                text = "¥ ${earnings.formattedEarnings}",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(MoneyPrimary)
                )
            )
        } else if (isConfigured && !earnings.isWorkday) {
            // 休息日：显示日薪 reference
            Text(
                text = "¥ ${earnings.formattedRemaining.maxOrNull()} / 天",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(StatusRest)
                )
            )
        } else {
            Text(
                text = "点击设置薪资",
                style = WidgetTypography.display(ColorProvider(TextTertiary))
            )
        }

        Spacer(GlanceModifier.height(4.dp))

        if (isConfigured && earnings.isWorkday) {
            // 进度条 + 百分比
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = earnings.workdayProgress,
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(5.dp),
                    color = ColorProvider(MoneyPrimary),
                    backgroundColor = ColorProvider(ProgressTrack)
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = earnings.formattedProgress,
                    style = WidgetTypography.caption(ColorProvider(TextSecondary))
                )
            }

            Spacer(GlanceModifier.height(2.dp))

            // 状态行：icon + status 左 | 时薪 右（合并 footer，省一行）
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(statusIconRes(earnings.status)),
                    contentDescription = null,
                    modifier = GlanceModifier.size(12.dp)
                )
                Spacer(GlanceModifier.width(4.dp))
                Text(
                    text = earnings.statusText,
                    style = WidgetTypography.statusLine(ColorProvider(statusColor(earnings.status)))
                )
                // 时薪挤右边
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = "时薪 ¥${earnings.formattedHourlyRate}",
                    style = WidgetTypography.caption(ColorProvider(TextTertiary))
                )
            }

            // 剩余金额（小字，紧贴 status 行）
            Spacer(GlanceModifier.height(1.dp))
            Text(
                text = "剩余 ¥${earnings.formattedRemaining}",
                style = WidgetTypography.caption(ColorProvider(TextTertiary))
            )
        } else if (isConfigured && !earnings.isWorkday) {
            // 休息日画面
            Spacer(GlanceModifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider = ImageProvider(R.drawable.ic_status_rest),
                    contentDescription = null,
                    modifier = GlanceModifier.size(14.dp)
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = "今日休息",
                    style = WidgetTypography.title(ColorProvider(StatusRest))
                )
            }
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = "休息是为了走更远的路",
                style = WidgetTypography.body(ColorProvider(StatusRest))
            )
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
