//
//  ContentView.swift
//  SalaryWidget
//
//  设置页面 —— 移植自 Android ConfigScreen.kt
//  输入薪资、工作时间、午休时间、五险一金
//

import SwiftUI

struct ContentView: View {
    @State private var settings = StorageService.loadSettings()

    // 用于文本输入框的字符串状态
    @State private var salaryText: String = ""
    @State private var fixedAmountText: String = ""

    // 实时预览
    @State private var previewState: EarningsState = .empty()
    private let previewTimer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        NavigationView {
            Form {
                // 实时预览
                Section {
                    LivePreviewCard(state: previewState)
                        .listRowInsets(EdgeInsets())
                        .listRowBackground(Color.clear)
                }

                // 薪资设置
                Section(header: Text("薪资设置")) {
                    Picker("薪资模式", selection: $settings.salaryMode) {
                        Text("税前月薪").tag(SalaryMode.preTax)
                        Text("税后到手").tag(SalaryMode.postTax)
                    }
                    .pickerStyle(.segmented)

                    HStack {
                        Text(settings.salaryMode == .preTax ? "税前月薪" : "到手月薪")
                        TextField("0", text: $salaryText)
                            .keyboardType(.decimalPad)
                            .multilineTextAlignment(.trailing)
                        Text("元/月")
                            .foregroundColor(.secondary)
                    }
                }

                // 工作时间
                Section(header: Text("工作时间")) {
                    DatePicker("上班时间", selection: workStartBinding, displayedComponents: .hourAndMinute)
                    DatePicker("下班时间", selection: workEndBinding, displayedComponents: .hourAndMinute)
                }

                // 午休时间
                Section(header: Text("午休时间")) {
                    DatePicker("午休开始", selection: lunchStartBinding, displayedComponents: .hourAndMinute)
                    DatePicker("午休结束", selection: lunchEndBinding, displayedComponents: .hourAndMinute)
                }

                // 五险一金
                Section(header: Text("五险一金")) {
                    if settings.salaryMode == .preTax {
                        Picker("扣款模式", selection: $settings.insuranceMode) {
                            Text("固定金额").tag(InsuranceMode.fixedAmount)
                            Text("比例模式").tag(InsuranceMode.rate)
                        }
                        .pickerStyle(.segmented)

                        if settings.insuranceMode == .fixedAmount {
                            HStack {
                                Text("每月扣除金额")
                                TextField("0", text: $fixedAmountText)
                                    .keyboardType(.decimalPad)
                                    .multilineTextAlignment(.trailing)
                                Text("元")
                                    .foregroundColor(.secondary)
                            }
                        } else {
                            rateRow("养老保险", value: $settings.pensionRate)
                            rateRow("医疗保险", value: $settings.medicalRate)
                            rateRow("失业保险", value: $settings.unemploymentRate)
                            rateRow("住房公积金", value: $settings.housingFundRate)

                            let totalRate = settings.pensionRate + settings.medicalRate +
                                settings.unemploymentRate + settings.housingFundRate
                            let totalAmount = settings.monthlySalary * totalRate
                            Text("合计：\(String(format: "%.1f%%", totalRate * 100)) ≈ ¥\(String(format: "%.0f", totalAmount))/月")
                                .font(.caption)
                                .foregroundColor(.accentColor)
                        }
                    } else {
                        Text("税后模式下无需计算五险一金")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

                // 保存按钮
                Section {
                    Button(action: save) {
                        HStack {
                            Spacer()
                            Text("💾 保存设置").fontWeight(.bold)
                            Spacer()
                        }
                    }
                    .disabled(settings.monthlySalary <= 0)
                }

                // 说明
                Section(footer: Text("iOS Widget 由系统调度刷新（约 10-15 分钟一次），显示金额带时间戳。")) {
                    Text("保存后小组件会自动刷新")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            .navigationTitle("💰 薪资小组件")
            .onAppear(perform: load)
            .onChange(of: settings) { _ in updatePreview() }
            .onReceive(previewTimer) { _ in updatePreview() }
        }
    }

    // MARK: - 时间绑定

    private var workStartBinding: Binding<Date> {
        Binding(
            get: { dateFrom(hour: settings.workStartHour, minute: settings.workStartMinute) },
            set: { date in
                let c = Calendar.current.dateComponents([.hour, .minute], from: date)
                settings.workStartHour = c.hour ?? 9
                settings.workStartMinute = c.minute ?? 0
            }
        )
    }

    private var workEndBinding: Binding<Date> {
        Binding(
            get: { dateFrom(hour: settings.workEndHour, minute: settings.workEndMinute) },
            set: { date in
                let c = Calendar.current.dateComponents([.hour, .minute], from: date)
                settings.workEndHour = c.hour ?? 18
                settings.workEndMinute = c.minute ?? 0
            }
        )
    }

    private var lunchStartBinding: Binding<Date> {
        Binding(
            get: { dateFrom(hour: settings.lunchStartHour, minute: settings.lunchStartMinute) },
            set: { date in
                let c = Calendar.current.dateComponents([.hour, .minute], from: date)
                settings.lunchStartHour = c.hour ?? 12
                settings.lunchStartMinute = c.minute ?? 0
            }
        )
    }

    private var lunchEndBinding: Binding<Date> {
        Binding(
            get: { dateFrom(hour: settings.lunchEndHour, minute: settings.lunchEndMinute) },
            set: { date in
                let c = Calendar.current.dateComponents([.hour, .minute], from: date)
                settings.lunchEndHour = c.hour ?? 13
                settings.lunchEndMinute = c.minute ?? 0
            }
        )
    }

    // MARK: - 辅助

    private func dateFrom(hour: Int, minute: Int) -> Date {
        Calendar.current.date(bySettingHour: hour, minute: minute, second: 0, of: Date()) ?? Date()
    }

    private func rateRow(_ label: String, value: Binding<Double>) -> some View {
        HStack {
            Text(label)
            TextField(
                String(format: "%.1f", value.wrappedValue * 100),
                text: Binding(
                    get: { String(format: "%.1f", value.wrappedValue * 100) },
                    set: { newValue in
                        let filtered = newValue.filter { $0.isNumber || $0 == "." }
                        if let pct = Double(filtered) {
                            value.wrappedValue = pct / 100.0
                        }
                    }
                )
            )
            .keyboardType(.decimalPad)
            .multilineTextAlignment(.trailing)
            Text("%").foregroundColor(.secondary)
        }
    }

    private func load() {
        settings = StorageService.loadSettings()
        salaryText = settings.monthlySalary > 0 ? String(Int(settings.monthlySalary)) : ""
        fixedAmountText = settings.insuranceFixedAmount > 0 ? String(Int(settings.insuranceFixedAmount)) : ""
        updatePreview()
    }

    private func updatePreview() {
        // 同步文本框到 settings
        settings.monthlySalary = Double(salaryText) ?? 0
        if settings.insuranceMode == .fixedAmount {
            settings.insuranceFixedAmount = Double(fixedAmountText) ?? 0
        }
        let config = SalaryConfig.from(settings)
        previewState = EarningsCalculator.calculate(config: config)
    }

    private func save() {
        settings.monthlySalary = Double(salaryText) ?? 0
        if settings.insuranceMode == .fixedAmount {
            settings.insuranceFixedAmount = Double(fixedAmountText) ?? 0
        }
        settings.isConfigured = true
        StorageService.saveSettings(settings)
    }
}

/// 实时预览卡片（App 内模拟 Widget 效果）
struct LivePreviewCard: View {
    let state: EarningsState

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("📱 小组件预览")
                .font(.caption)
                .foregroundColor(WidgetTheme.textSecondary)

            Text("¥ \(state.formattedEarnings)")
                .font(.system(size: 34, weight: .bold))
                .foregroundColor(WidgetTheme.moneyPrimary)

            // 进度条
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    Capsule()
                        .fill(WidgetTheme.progressTrack)
                        .frame(height: 8)
                    Capsule()
                        .fill(WidgetTheme.moneyPrimary)
                        .frame(width: geo.size.width * state.workdayProgress, height: 8)
                }
            }
            .frame(height: 8)

            HStack {
                Text(state.statusText)
                    .font(.caption)
                    .foregroundColor(statusColor)
                Spacer()
                Text("剩余 ¥\(state.formattedRemaining)")
                    .font(.caption)
                    .foregroundColor(WidgetTheme.textTertiary)
            }
        }
        .padding(16)
        .background(WidgetTheme.background)
        .cornerRadius(20)
    }

    private var statusColor: Color {
        switch state.status {
        case .beforeWork: return WidgetTheme.statusIdle
        case .working: return WidgetTheme.statusWorking
        case .lunchBreak: return WidgetTheme.statusLunch
        case .afterWork: return WidgetTheme.statusDone
        case .dayOff: return WidgetTheme.statusRest
        }
    }
}

#Preview {
    ContentView()
}
