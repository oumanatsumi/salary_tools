//
//  SalaryWidgetTheme.swift
//  SalaryWidget
//
//  颜色与排版 token —— 1:1 移植自 Android 版 Color.kt + WidgetTypography.kt
//

import SwiftUI

/// 颜色 token（十六进制值与 Android Color.kt 完全一致）
enum WidgetTheme {
    // 背景
    static let background = Color(hex: 0x0F0F1A)     // 深空黑
    static let surface = Color(hex: 0x1A1A2E)        // 凸起卡片表面

    // 金额
    static let moneyPrimary = Color(hex: 0x10B981)   // emerald-500
    static let moneyMuted = Color(hex: 0x059669)     // emerald-600

    // 状态色
    static let statusIdle = Color(hex: 0x71717A)     // BEFORE_WORK
    static let statusWorking = Color(hex: 0x38BDF8)  // WORKING
    static let statusLunch = Color(hex: 0xF59E0B)    // LUNCH_BREAK
    static let statusDone = Color(hex: 0x34D399)     // AFTER_WORK
    static let statusRest = Color(hex: 0xFBBF24)     // DAY_OFF

    // 文字
    static let textPrimary = Color(hex: 0xF4F4F5)
    static let textSecondary = Color(hex: 0xA1A1AA)
    static let textTertiary = Color(hex: 0x71717A)

    // 进度条
    static let progressTrack = Color(hex: 0x27273A)
    static let divider = Color(hex: 0x3F3F46)
}

extension Color {
    /// 从 0xRRGGBB 十六进制值创建颜色
    init(hex: UInt32) {
        let r = Double((hex >> 16) & 0xFF) / 255.0
        let g = Double((hex >> 8) & 0xFF) / 255.0
        let b = Double(hex & 0xFF) / 255.0
        self.init(red: r, green: g, blue: b)
    }
}
