# 薪资小组件 — 项目进度

> 最后更新：2026-08-14

## 项目概述

「今日已赚」薪资桌面小组件：根据月薪、上下班时间、午休时间、五险一金，实时计算"今天目前为止赚了多少钱"，缓解上班怨气。

- **Android 版**：小米15 / 澎湃OS 3.0，已完成并可运行
- **iOS 版**：源码完成，待 Mac + Xcode 环境构建

---

## 当前进度总览

| 平台 | 状态 | 说明 |
|------|------|------|
| Android | ✅ 完成 | 小米15 桌面小组件，30 秒实时刷新 |
| iOS | 📝 源码完成 | SwiftUI + WidgetKit，待 Mac 构建 |

---

## Android 版（小米15）

### 技术栈

| 组件 | 技术 |
|------|------|
| 语言 | Kotlin |
| App UI | Jetpack Compose + Material3 |
| Widget | Jetpack Glance |
| 数据存储 | DataStore (Preferences) |
| 刷新机制 | AlarmManager（30s）+ WorkManager（15min 保底） |

### 功能清单

- [x] 税前/税后月薪双模式
- [x] 五险一金固定金额 / 比例模式
- [x] 上下班时间 + 午休起止时间
- [x] 实时收入计算（每秒累加）
- [x] 工作日进度条 + 状态图标
- [x] 周末休息日画面
- [x] 澎湃OS 电池优化适配引导
- [x] 屏幕感知省电（亮屏30s刷新 / 熄屏15min）

### 已解决的 Bug

1. **"无法显示内容"** — Compose BOM 版本与 Glance 不兼容，降级到 `2024.06.00` 解决
2. **"点击设置薪资"回退** — DataStore 委托绑定 Context 实例导致多实例，改为顶层单例 + `applicationContext` 解决

---

## iOS 版

### 技术栈

| 组件 | 技术 |
|------|------|
| 语言 | Swift |
| App UI | SwiftUI |
| Widget | WidgetKit + TimelineProvider |
| 数据存储 | UserDefaults (App Group) |
| 刷新机制 | Timeline（系统控制，10-15min） |

### 与 Android 版差异

| 维度 | Android | iOS |
|------|---------|-----|
| 刷新频率 | 30 秒 | 10-15 分钟 |
| 数据实时性 | 实时 | 延迟 + 时间戳标注 |
| 数据共享 | DataStore（同进程） | App Group（跨进程） |

### 源码文件

```
ios/
├── Shared/                        # App 与 Widget 共享
│   ├── SalaryModel.swift
│   ├── EarningsCalculator.swift   # 核心计算（1:1 移植 Kotlin）
│   ├── StorageService.swift
│   └── SalaryWidgetTheme.swift
├── SalaryWidget/
│   ├── SalaryWidgetApp.swift
│   └── ContentView.swift
├── SalaryWidgetExtension/
│   ├── SalaryWidget.swift
│   ├── SalaryWidgetView.swift
│   └── SalaryWidgetBundle.swift
├── SalaryWidgetTests/
│   └── EarningsCalculatorTests.swift
└── README.md                      # 搭建指南
```

### 待办（需 Mac + Xcode）

- [ ] Xcode 项目创建 + 源码导入
- [ ] App Group 配置（`group.com.oumanatsumi.salarytools`）
- [ ] Widget Extension target
- [ ] 真机调试

---

## 计算逻辑（双平台一致）

```
税后月薪 = 税前月薪 - 五险一金扣除
日薪 = 税后月薪 / 21.75（标准工作日）
时薪 = 日薪 / 有效工作小时
每秒收入 = 时薪 / 3600

实时收入：
  上班前 → 0
  下班后 → 日薪
  午休中 → 暂停累加
  其他 → 每秒收入 × 已工作秒数

周末 → 显示"今日休息"
```

---

## 构建方式

### Android（Windows 可构建）

```powershell
# 构建 APK
python build.py
# 或
.\build.ps1

# 安装到手机
.\install.bat
```

### iOS（需 Mac + Xcode）

见 `ios/README.md` 的 6 步搭建指南。

---

## 仓库信息

- GitHub: https://github.com/oumanatsumi/salary_tools
- 分支: `main`
