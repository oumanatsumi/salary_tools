<#
.SYNOPSIS
    构建并安装 APK 到连接的小米手机
#>
$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$Gradlew = Join-Path $ProjectRoot "gradlew.bat"
$ApkPath = Join-Path $ProjectRoot "app\build\outputs\apk\debug\app-debug.apk"

# 设置环境变量
$env:ANDROID_HOME = Join-Path $ProjectRoot ".tools\android-sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$AdbPath = Join-Path $env:ANDROID_HOME "platform-tools\adb.exe"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  安装薪资小组件到手机" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# 检查设备连接
Write-Host "[1/3] 检查设备连接..." -ForegroundColor Yellow
$devices = & $AdbPath devices 2>&1
$deviceCount = ($devices | Select-String "device$" | Measure-Object).Count

if ($deviceCount -eq 0) {
    Write-Host ""
    Write-Host "✗ 未检测到设备！请确认：" -ForegroundColor Red
    Write-Host "  1. 手机通过 USB 连接电脑" -ForegroundColor White
    Write-Host "  2. 手机开启开发者模式（连续点击 设置→关于手机→MIUI版本 7次）" -ForegroundColor White
    Write-Host "  3. 开启 USB 调试（设置→更多设置→开发者选项→USB调试）" -ForegroundColor White
    Write-Host "  4. 手机上允许 USB 调试授权弹窗" -ForegroundColor White
    exit 1
}
Write-Host "  ✓ 检测到 $deviceCount 个设备" -ForegroundColor Green

# 构建 APK（如果不存在）
if (-not (Test-Path $ApkPath)) {
    Write-Host ""
    Write-Host "[2/3] APK 不存在，开始构建..." -ForegroundColor Yellow
    Push-Location $ProjectRoot
    try {
        & $Gradlew assembleDebug --no-daemon
        if ($LASTEXITCODE -ne 0) {
            Write-Host "✗ 构建失败" -ForegroundColor Red
            exit 1
        }
    } finally {
        Pop-Location
    }
} else {
    Write-Host "[2/3] APK 已存在，跳过构建" -ForegroundColor Green
}

# 安装到设备
Write-Host ""
Write-Host "[3/3] 安装到设备..." -ForegroundColor Yellow
& $AdbPath install -r $ApkPath 2>&1 | ForEach-Object {
    Write-Host "  $_"
}

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Green
    Write-Host "  ✓ 安装成功！" -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "接下来：" -ForegroundColor Cyan
    Write-Host "  1. 在手机上打开'薪资小组件'App，配置薪资信息" -ForegroundColor White
    Write-Host "  2. 桌面长按 → 添加小组件 → 选择'今日已赚'" -ForegroundColor White
    Write-Host "  3. 按 App 引导完成澎湃OS电池优化设置" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "✗ 安装失败" -ForegroundColor Red
    exit 1
}
