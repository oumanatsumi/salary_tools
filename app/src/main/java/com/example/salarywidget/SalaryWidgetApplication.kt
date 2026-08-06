package com.example.salarywidget

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.salarywidget.widget.BootReceiver
import com.example.salarywidget.widget.ScreenStateReceiver
import com.example.salarywidget.widget.WidgetAlarmReceiver
import com.example.salarywidget.widget.WidgetUpdateWorker
import java.util.concurrent.TimeUnit

/**
 * 应用主类
 * 初始化后台任务调度和广播接收器
 */
class SalaryWidgetApplication : android.app.Application() {

    private var screenStateReceiver: ScreenStateReceiver? = null
    private var isReceiverRegistered = false

    override fun onCreate() {
        super.onCreate()

        // 创建通知渠道
        createNotificationChannel()

        // 注册屏幕状态接收器
        registerScreenStateReceiver()

        // 调度 Widget 更新任务
        scheduleWidgetUpdates()
    }

    /**
     * 创建通知渠道（用于前台服务/持久通知）
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "薪资小组件服务",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "用于保持小组件后台运行"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * 注册屏幕状态广播接收器（动态注册）
     */
    private fun registerScreenStateReceiver() {
        screenStateReceiver = ScreenStateReceiver()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, filter)
        isReceiverRegistered = true
    }

    /**
     * 调度 Widget 更新任务
     */
    fun scheduleWidgetUpdates() {
        schedulePeriodicWork()
        startFrequentUpdates()
    }

    /**
     * 调度 WorkManager 周期任务（保底机制，每 15 分钟）
     */
    private fun schedulePeriodicWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WidgetUpdateWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )
    }

    /**
     * 启动高频 AlarmManager 更新（每 30 秒）
     * 屏幕关闭时应调用 stopFrequentUpdates() 以省电
     */
    fun startFrequentUpdates() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, WidgetAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, REQUEST_CODE_ALARM, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // 尝试精确 30 秒重复闹钟
            alarmManager.setRepeating(
                AlarmManager.RTC,
                System.currentTimeMillis(),
                UPDATE_INTERVAL_MS,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // 如果没有精确闹钟权限，降级到 15 分钟
            alarmManager.setRepeating(
                AlarmManager.RTC,
                System.currentTimeMillis(),
                FALLBACK_INTERVAL_MS,
                pendingIntent
            )
        }
    }

    /**
     * 停止高频更新（屏幕关闭时调用以省电）
     */
    fun stopFrequentUpdates() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, WidgetAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, REQUEST_CODE_ALARM, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    override fun onTerminate() {
        super.onTerminate()
        if (isReceiverRegistered && screenStateReceiver != null) {
            unregisterReceiver(screenStateReceiver)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "salary_widget_channel"
        const val REQUEST_CODE_ALARM = 1001
        const val UPDATE_INTERVAL_MS = 30_000L       // 30 秒
        const val FALLBACK_INTERVAL_MS = 15 * 60 * 1000L  // 15 分钟
    }
}
