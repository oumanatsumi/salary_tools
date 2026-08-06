<#
.SYNOPSIS
    构建薪资小组件 APK
#>
$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$Gradlew = Join-Path $ProjectRoot "gradlew.bat"

# 设置环境变量
$env:ANDROID_HOME = Join-Path $ProjectRoot ".tools\android-sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME

if (-not (Test-Path $Gradlew)) {
    Write-Host "✗ 未找到 gradlew，请先运行 setup.ps1" -ForegroundColor Red
    exit 1
}

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  构建薪资小组件 APK" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

Push-Location $ProjectRoot
try {
    Write-Host "开始构建 debug APK..." -ForegroundColor Yellow
    & $Gradlew assembleDebug --no-daemon 2>&1 | ForEach-Object {
        Write-Host $_
    }

    if ($LASTEXITCODE -eq 0) {
        $apkPath = Join-Path $ProjectRoot "app\build\outputs\apk\debug\app-debug.apk"
        Write-Host ""
        Write-Host "============================================" -ForegroundColor Green
        Write-Host "  ✓ 构建成功！" -ForegroundColor Green
        Write-Host "============================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "APK 路径: $apkPath" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "安装到手机: 连接 USB 后运行 install.ps1" -ForegroundColor White
    } else {
        Write-Host ""
        Write-Host "✗ 构建失败，退出码: $LASTEXITCODE" -ForegroundColor Red
        exit 1
    }
} finally {
    Pop-Location
}
