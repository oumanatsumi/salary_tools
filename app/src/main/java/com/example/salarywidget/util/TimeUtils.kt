package com.example.salarywidget.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 时间工具类
 */
object TimeUtils {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * 格式化时间为 HH:mm
     */
    fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

    /**
     * 将小时和分钟转换为 LocalTime
     * 添加范围校验防止 DateTimeException
     */
    fun toLocalTime(hour: Int, minute: Int): LocalTime {
        return LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
    }

    /**
     * 计算两个时间点之间的分钟数
     */
    fun minutesBetween(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Int {
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute
        return (endMinutes - startMinutes).coerceAtLeast(0)
    }
}
