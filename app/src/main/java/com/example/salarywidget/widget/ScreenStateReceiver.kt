package com.example.salarywidget.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.salarywidget.SalaryWidgetApplication

/**
 * 屏幕状态接收器
 * 屏幕亮起时启动高频更新，屏幕关闭时停止以节省电量
 */
class ScreenStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? SalaryWidgetApplication ?: return

        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> {
                // 屏幕亮起，启动高频更新
                app.startFrequentUpdates()
            }
            Intent.ACTION_SCREEN_OFF -> {
                // 屏幕关闭，停止高频更新以省电
                app.stopFrequentUpdates()
            }
        }
    }
}
