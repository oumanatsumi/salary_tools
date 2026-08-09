package com.example.salarywidget.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Widget 调色板（refined dark fintech 方向）
 *
 * 这是 widget + app 的单一 token 源。Widget 通过 Widget* 语义 token 消费，
 * Material Theme.kt 通过 Legacy* 别名消费（保证向后兼容）。
 *
 * 反俗套原则（借鉴 design-taste-frontend）：
 * - 避免纯黑 (#000000) 和纯白 (#FFFFFF)
 * - 用带蓝调的深色做表面，避免冷灰
 * - 主强调使用 emerald 而非 Material green，更显质感
 */

// === Widget 语义 token（新设计用这些） ===

// Surfaces
val WidgetBackground = Color(0xFF0F0F1A)    // 深空黑（outer background，带微蓝调）
val WidgetSurface = Color(0xFF1A1A2E)       // 凸起卡片表面（raised surface）

// Accent
val MoneyPrimary = Color(0xFF10B981)        // emerald-500，hero 金额
val MoneyMuted = Color(0xFF059669)          // emerald-600，小金额元素

// Status 语义（每个 WorkStatus 对应一个 token）
val StatusIdle = Color(0xFF71717A)          // zinc-500, BEFORE_WORK
val StatusWorking = Color(0xFF38BDF8)       // sky-400, WORKING
val StatusLunch = Color(0xFFF59E0B)         // amber-500, LUNCH_BREAK
val StatusDone = Color(0xFF34D399)          // emerald-400, AFTER_WORK
val StatusRest = Color(0xFFFBBF24)          // amber-400, DAY_OFF (休息日)

// Text
val TextPrimary = Color(0xFFF4F4F5)         // zinc-100（主文字，避免纯白）
val TextSecondary = Color(0xFFA1A1AA)       // zinc-400（次文字）
val TextTertiary = Color(0xFF71717A)        // zinc-500（三级文字）

// Structural
val ProgressTrack = Color(0xFF27273A)       // 进度条轨道（带蓝调深灰）
val Divider = Color(0xFF3F3F46)             // zinc-700，分隔符

// === Legacy token（供 Material Theme.kt 使用，更新为 refined palette） ===

val Green500 = MoneyPrimary
val Green700 = MoneyMuted
val Green200 = Color(0xFFA7F3D0)            // emerald-200

val DarkSurface = WidgetSurface
val DarkSurfaceVariant = WidgetBackground
val DarkOnSurface = TextPrimary

val MoneyGreen = MoneyPrimary
val LunchOrange = StatusLunch
val WorkBlue = StatusWorking
val DoneGreen = StatusDone
val DisabledGray = StatusIdle
