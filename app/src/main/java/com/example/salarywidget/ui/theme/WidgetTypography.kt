package com.example.salarywidget.ui.theme

import androidx.compose.ui.unit.sp
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

/**
 * Widget 4-step type scale
 *
 * - Display: 36sp SemiBold — hero 金额（视觉焦点）
 * - Title: 16sp Medium — section headers（"今日已赚"）
 * - Body: 13sp Regular — 状态文字
 * - Caption: 11sp Regular — footer（时薪 / 剩余）
 *
 * 字体：当前使用系统默认 sans-serif。
 * 升级到 Geist：
 *  1. 从 https://fonts.google.com/specimen/Geist 下载 TTF 包
 *  2. 把 Geist-Regular.ttf / Geist-Medium.ttf / Geist-SemiBold.ttf
 *     复制到 `app/src/main/res/font/` 并重命名为
 *     `geist_regular.ttf` / `geist_medium.ttf` / `geist_semibold.ttf`
 *  3. 取消下面 `FontFamily(ResourceFont(...))` 行的注释即可
 */
object WidgetTypography {

    fun display(color: ColorProvider): TextStyle = TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = color
        // fontFamily = FontFamily(ResourceFont(R.font.geist_semibold))
    )

    fun title(color: ColorProvider): TextStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = color
        // fontFamily = FontFamily(ResourceFont(R.font.geist_medium))
    )

    fun body(color: ColorProvider, fontWeight: FontWeight = FontWeight.Normal): TextStyle = TextStyle(
        fontSize = 13.sp,
        fontWeight = fontWeight,
        color = color
        // fontFamily = FontFamily(ResourceFont(R.font.geist_regular))
    )

    fun statusLine(color: ColorProvider): TextStyle = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = color
        // fontFamily = FontFamily(ResourceFont(R.font.geist_regular))
    )

    fun caption(color: ColorProvider): TextStyle = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        color = color
        // fontFamily = FontFamily(ResourceFont(R.font.geist_regular))
    )
}
