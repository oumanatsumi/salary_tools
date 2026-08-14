//
//  SalaryWidget.swift
//  SalaryWidgetExtension
//
//  WidgetKit 小组件定义 + TimelineProvider
//  iOS 系统控制刷新节奏（约 10-15 分钟）
//

import WidgetKit
import SwiftUI

/// Timeline 条目
struct SalaryEntry: TimelineEntry {
    let date: Date          // 生成时间（用于显示时间戳）
    let state: EarningsState
    let isConfigured: Bool
}

/// TimelineProvider
struct SalaryTimelineProvider: TimelineProvider {
    func placeholder(in context: Context) -> SalaryEntry {
        SalaryEntry(date: Date(), state: .empty(), isConfigured: false)
    }

    func getSnapshot(in context: Context, completion: @escaping (SalaryEntry) -> Void) {
        let entry = makeEntry()
        completion(entry)
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<SalaryEntry>) -> Void) {
        let entry = makeEntry()

        // 关键：请求系统约 15 分钟后刷新（iOS 系统决定实际时机）
        let nextUpdate = Calendar.current.date(byAdding: .minute, value: 15, to: Date()) ?? Date()
        let timeline = Timeline(entries: [entry], policy: .after(nextUpdate))
        completion(timeline)
    }

    private func makeEntry() -> SalaryEntry {
        let settings = StorageService.loadSettings()
        if settings.isConfigured && settings.monthlySalary > 0 {
            let config = SalaryConfig.from(settings)
            let state = EarningsCalculator.calculate(config: config)
            return SalaryEntry(date: Date(), state: state, isConfigured: true)
        } else {
            return SalaryEntry(date: Date(), state: .empty(), isConfigured: false)
        }
    }
}

/// Widget 主入口
struct SalaryWidget: Widget {
    let kind = "SalaryWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: SalaryTimelineProvider()) { entry in
            SalaryWidgetView(entry: entry)
        }
        .configurationDisplayName("今日已赚")
        .description("实时显示你今天赚了多少")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}
