package com.example.salarywidget.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Widget 接收器
 * 系统通过此 Receiver 管理与 Widget 的交互
 */
class SalaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SalaryWidget()
}
