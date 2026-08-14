# iOS 薪资小组件 — 搭建指南

这是薪资小组件的 iOS 版本（SwiftUI + WidgetKit），与 Android 版功能对应。

> ⚠️ **iOS 开发需要 Mac + Xcode**，无法在 Windows 上编译。本目录是完整的 Swift 源码，需在 Mac 上导入 Xcode 构建。

## 与 Android 版的差异

| 维度 | Android | iOS |
|------|---------|-----|
| 刷新频率 | 30 秒 | 10-15 分钟（系统控制，不可突破） |
| 数据实时性 | 实时 | 延迟 + 时间戳标注 |
| Widget 框架 | Jetpack Glance | WidgetKit + SwiftUI |
| 数据存储 | DataStore | UserDefaults (App Group) |

## 目录结构

```
ios/
├── Shared/                        # App 与 Widget 共享
│   ├── SalaryModel.swift          # 数据模型 + 枚举
│   ├── EarningsCalculator.swift   # 核心计算逻辑（移植 Kotlin）
│   ├── StorageService.swift       # App Group 数据存取
│   └── SalaryWidgetTheme.swift    # 颜色 token
├── SalaryWidget/                  # 主 App
│   ├── SalaryWidgetApp.swift      # @main 入口
│   └── ContentView.swift          # 设置页
├── SalaryWidgetExtension/         # Widget 扩展
│   ├── SalaryWidget.swift         # TimelineProvider
│   ├── SalaryWidgetView.swift     # Widget 视图
│   └── SalaryWidgetBundle.swift   # Widget 入口
└── SalaryWidgetTests/             # 单元测试
    └── EarningsCalculatorTests.swift
```

## 搭建步骤（Mac + Xcode 15+）

### 1. 创建 Xcode 项目

1. 打开 Xcode → `File → New → Project`
2. 选择 **iOS → App**
3. 填写信息：
   - Product Name: `SalaryWidget`
   - Interface: `SwiftUI`
   - Language: `Swift`
4. 点击 Create 保存

### 2. 导入源码

把本目录的文件拖入 Xcode 项目，按 target 分配：

| 文件 | 加入的 Target |
|------|--------------|
| `Shared/` 下全部 4 个文件 | **App target + Widget extension target**（都勾选） |
| `SalaryWidget/` 下 2 个文件 | 仅 App target |
| `SalaryWidgetExtension/` 下 3 个文件 | 仅 Widget extension target |
| `SalaryWidgetTests/` 下 1 个文件 | 仅 Test target |

> 拖入时勾选 **"Copy items if needed"**，并确保每个文件的 Target Membership 正确。

### 3. 添加 Widget Extension Target

1. `File → New → Target`
2. 选择 **Widget Extension**
3. Product Name: `SalaryWidgetExtension`
4. 勾选 "Include Configuration App Intent" 可取消（本组件不需要）

### 4. 配置 App Group（关键步骤）

App 和 Widget 是独立进程，必须通过 App Group 共享数据：

1. 选中 **App target** → `Signing & Capabilities` → `+ Capability` → **App Groups**
2. 添加一个 App Group：`group.com.oumanatsumi.salarytools`
3. 选中 **Widget extension target** → 同样操作，添加**同一个** App Group ID
4. 两个 target 的 App Group 必须完全一致

### 5. 设置最低系统版本

- App target 和 Widget target 都设为 **iOS 16.0+**

### 6. 运行

1. 选择真机（iPhone）或模拟器
2. 选择 **SalaryWidget** scheme
3. `Cmd + R` 运行 App
4. 配置薪资数据 → 保存
5. 回到桌面 → 长按 → 添加小组件 → 搜索"今日已赚"

## 验证清单

- [ ] App 配置薪资后，桌面 Widget 显示金额
- [ ] Widget 显示时间戳（"截至 HH:mm"）
- [ ] 保存设置后 Widget 刷新（`WidgetCenter.reloadAllTimelines()`）
- [ ] 周末显示"今日休息"
- [ ] 上班前显示"还没开始工作"
- [ ] 午休中显示"午休中…"
- [ ] 下班后显示"今日已完成"

## 常见问题

**Q: Widget 显示"点击设置"但 App 已配置？**
A: App Group 未正确配置。确认两个 target 都开启了相同的 `group.com.oumanatsumi.salarytools`。

**Q: Widget 不刷新？**
A: iOS 系统控制刷新节奏，约 10-15 分钟一次。这是 Apple 的硬性限制，无法突破。用户可通过点开 App 触发主动刷新。

**Q: 编译报 "Cannot find 'WidgetKit'"?**
A: WidgetKit 只对 Widget extension target 可用。确认 Shared 文件加入 App target 时，`StorageService.swift` 的 `import WidgetKit` 需要在 App target 中也能编译 —— 如果报错，把 `WidgetCenter.shared.reloadAllTimelines()` 移到 Widget extension 侧调用。

## 颜色对照（与 Android 一致）

| Token | 十六进制 | 用途 |
|-------|---------|------|
| background | #0F0F1A | 背景 |
| moneyPrimary | #10B981 | hero 金额 |
| statusWorking | #38BDF8 | 工作中 |
| statusLunch | #F59E0B | 午休中 |
| statusDone | #34D399 | 下班 |
| statusRest | #FBBF24 | 休息日 |
| statusIdle | #71717A | 上班前 |
