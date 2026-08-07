package com.example.salarywidget.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AlarmManager 闹钟接收器
 * 每 30 秒触发一次，更新 Widget 显示
 *
 * 关键修复：onReceive 返回后 context 会被回收，
 * 必须在 launch 之前捕获 applicationContext
 */
class WidgetAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 必须在协程启动前捕获 applicationContext
        // ReceiverRestrictedContext 在 onReceive 返回后失效
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                SalaryWidget().updateAll(appContext)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
