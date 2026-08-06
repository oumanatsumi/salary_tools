package com.example.salarywidget.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.salarywidget.SalaryWidgetApplication

/**
 * 开机广播接收器
 * 设备重启后重新注册 AlarmManager 闹钟和 WorkManager 任务
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val app = context.applicationContext as? SalaryWidgetApplication
            app?.scheduleWidgetUpdates()
        }
    }
}
