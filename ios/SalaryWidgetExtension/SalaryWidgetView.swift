//
//  SalaryWidgetView.swift
//  SalaryWidgetExtension
//
//  Widget 视图 —— 移植自 Android WidgetContent.kt
//  增加时间戳（因 iOS 刷新延迟）
//

import SwiftUI
import WidgetKit

struct SalaryWidgetView: View {
    let entry: SalaryEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        Group {
            switch family {
            case .systemSmall:
                smallView
            default:
                mediumView
            }
        }
        .containerBackground(for: .widget) {
            WidgetTheme.background
        }
    }

    // MARK: - Medium (与 Android 4x2 对应)

    private var mediumView: some View {
        VStack(alignment: .leading, spacing: 4) {
            // 标题
            HStack(spacing: 6) {
                Image(systemName: "yensign.circle.fill")
                    .font(.system(size: 14))
                    .foregroundColor(WidgetTheme.moneyPrimary)
                Text(entry.isConfigured ? "今日已赚" : "薪资小组件")
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(WidgetTheme.textSecondary)
            }

            // 金额
            if entry.isConfigured && entry.state.isWorkday {
                Text("¥ \(entry.state.formattedEarnings)")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(WidgetTheme.moneyPrimary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.6)
            } else if entry.isConfigured && !entry.state.isWorkday {
                Text("¥ \(entry.state.formattedRemaining) / 天")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(WidgetTheme.statusRest)
                    .lineLimit(1)
                    .minimumScaleFactor(0.6)
            } else {
                Text("点击设置薪资")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(WidgetTheme.textTertiary)
            }

            if entry.isConfigured && entry.state.isWorkday {
                // 进度条
                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        Capsule().fill(WidgetTheme.progressTrack)
                        Capsule().fill(WidgetTheme.moneyPrimary)
                            .frame(width: geo.size.width * entry.state.workdayProgress)
                    }
                }
                .frame(height: 5)

                // 状态行
                HStack(spacing: 4) {
                    Image(systemName: statusIcon)
                        .font(.system(size: 11))
                        .foregroundColor(statusColor)
                    Text(entry.state.statusText)
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(statusColor)
                    Spacer()
                    Text("时薪 ¥\(entry.state.formattedHourlyRate)")
                        .font(.system(size: 11))
                        .foregroundColor(WidgetTheme.textTertiary)
                }

                // footer：剩余 + 时间戳
                HStack {
                    Text("剩余 ¥\(entry.state.formattedRemaining)")
                        .font(.system(size: 11))
                        .foregroundColor(WidgetTheme.textTertiary)
                    Spacer()
                    Text("截至 \(Self.timeFormatter.string(from: entry.date))")
                        .font(.system(size: 9))
                        .foregroundColor(WidgetTheme.textTertiary.opacity(0.7))
                }
            } else if entry.isConfigured && !entry.state.isWorkday {
                // 休息日画面
                HStack(spacing: 6) {
                    Image(systemName: "cup.and.saucer.fill")
                        .font(.system(size: 14))
                        .foregroundColor(WidgetTheme.statusRest)
                    Text("今日休息")
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(WidgetTheme.statusRest)
                }
                Text("休息是为了走更远的路")
                    .font(.system(size: 12))
                    .foregroundColor(WidgetTheme.statusRest)
            }
        }
        .padding(12)
    }

    // MARK: - Small

    private var smallView: some View {
        VStack(alignment: .leading, spacing: 2) {
            Image(systemName: "yensign.circle.fill")
                .font(.system(size: 14))
                .foregroundColor(WidgetTheme.moneyPrimary)

            if entry.isConfigured && entry.state.isWorkday {
                Text("¥\(entry.state.formattedEarnings)")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(WidgetTheme.moneyPrimary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.5)

                Text(entry.state.statusText)
                    .font(.system(size: 10))
                    .foregroundColor(statusColor)

                Text("截至 \(Self.timeFormatter.string(from: entry.date))")
                    .font(.system(size: 8))
                    .foregroundColor(WidgetTheme.textTertiary.opacity(0.7))
            } else {
                Text("点击设置")
                    .font(.system(size: 13))
                    .foregroundColor(WidgetTheme.textTertiary)
            }
        }
        .padding(10)
    }

    // MARK: - 状态映射

    private var statusIcon: String {
        switch entry.state.status {
        case .beforeWork: return "moon.zzz.fill"
        case .working: return "briefcase.fill"
        case .lunchBreak: return "fork.knife"
        case .afterWork: return "checkmark.circle.fill"
        case .dayOff: return "cup.and.saucer.fill"
        }
    }

    private var statusColor: Color {
        switch entry.state.status {
        case .beforeWork: return WidgetTheme.statusIdle
        case .working: return WidgetTheme.statusWorking
        case .lunchBreak: return WidgetTheme.statusLunch
        case .afterWork: return WidgetTheme.statusDone
        case .dayOff: return WidgetTheme.statusRest
        }
    }

    private static let timeFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "HH:mm"
        return f
    }()
}
