<#
.SYNOPSIS
    Build salary widget APK (portable)
.DESCRIPTION
    Auto-detects JDK and Android SDK, then runs assembleDebug.
#>
$ErrorActionPreference = "Stop"
$ProjectRoot = $PSScriptRoot
$Gradlew = Join-Path $ProjectRoot "gradlew.bat"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Salary Widget - Build APK" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Find JDK
Write-Host "[1/3] Detecting JDK..." -ForegroundColor Yellow
$javaExe = $null

if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
    $javaExe = Join-Path $env:JAVA_HOME "bin\java.exe"
    Write-Host "  Found via JAVA_HOME: $javaExe" -ForegroundColor Green
} else {
    $candidates = @(
        "D:\Environment\openjdk-*_windows-x64_bin\jdk-*",
        "D:\Env\openjdk-*_windows-x64_bin\jdk-*",
        "D:\Env\jdk-*",
        "C:\Program Files\Java\jdk-*",
        "C:\Program Files\Eclipse Adoptium\jdk-*",
        "C:\Program Files\Microsoft\jdk-*"
    )
    foreach ($pattern in $candidates) {
        $resolved = Resolve-Path $pattern -ErrorAction SilentlyContinue | Select-Object -Last 1
        if ($resolved) {
            $candidatePath = Join-Path $resolved "bin\java.exe"
            if (Test-Path $candidatePath) {
                $javaExe = $candidatePath
                $env:JAVA_HOME = $resolved.ToString()
                Write-Host "  Found: $javaExe" -ForegroundColor Green
                break
            }
        }
    }
}

if (-not $javaExe) {
    try {
        $javaExe = (Get-Command java -ErrorAction Stop).Source
        Write-Host "  Found via PATH: $javaExe" -ForegroundColor Green
    } catch {
        Write-Host "  ERROR: JDK not found. Set JAVA_HOME env var." -ForegroundColor Red
        exit 1
    }
}

$javaDir = Split-Path $javaExe -Parent
$env:PATH = "$javaDir;$env:PATH"

# Step 2: Find Android SDK
Write-Host ""
Write-Host "[2/3] Detecting Android SDK..." -ForegroundColor Yellow
$sdkDir = $null

if ($env:ANDROID_HOME -and (Test-Path $env:ANDROID_HOME)) {
    $sdkDir = $env:ANDROID_HOME
    Write-Host "  Found via ANDROID_HOME: $sdkDir" -ForegroundColor Green
} elseif (Test-Path (Join-Path $ProjectRoot "local.properties")) {
    $sdkLine = Get-Content (Join-Path $ProjectRoot "local.properties") | Where-Object { $_ -match "^sdk\.dir=" } | Select-Object -First 1
    if ($sdkLine) {
        $sdkDir = ($sdkLine -replace "^sdk\.dir=", "") -replace '\\\\', '\'
        if (Test-Path $sdkDir) {
            Write-Host "  Found via local.properties: $sdkDir" -ForegroundColor Green
        } else {
            Write-Host "  local.properties points to missing path: $sdkDir" -ForegroundColor Yellow
            $sdkDir = $null
        }
    }
}

if (-not $sdkDir) {
    $legacySdkPaths = @(
        "E:\proj\salary-tools\.tools\android-sdk",
        (Join-Path $ProjectRoot ".tools\android-sdk")
    )
    foreach ($legacy in $legacySdkPaths) {
        if (Test-Path $legacy) {
            $sdkDir = $legacy
            Write-Host "  Using fallback: $sdkDir" -ForegroundColor Yellow
            break
        }
    }
}

if (-not $sdkDir) {
    Write-Host "  ERROR: Android SDK not found." -ForegroundColor Red
    Write-Host "  Options:" -ForegroundColor White
    Write-Host "    1. Run setup.ps1 to download SDK" -ForegroundColor White
    Write-Host "    2. Set ANDROID_HOME env var" -ForegroundColor White
    Write-Host "    3. Edit local.properties with sdk.dir=<path>" -ForegroundColor White
    exit 1
}

$env:ANDROID_HOME = $sdkDir
$env:ANDROID_SDK_ROOT = $sdkDir

# Step 3: Build
Write-Host ""
Write-Host "[3/3] Building debug APK..." -ForegroundColor Yellow
Push-Location $ProjectRoot
try {
    & $Gradlew assembleDebug --no-daemon 2>&1 | ForEach-Object { Write-Host $_ }

    if ($LASTEXITCODE -eq 0) {
        $apkPath = Join-Path $ProjectRoot "app\build\outputs\apk\debug\app-debug.apk"
        Write-Host ""
        Write-Host "============================================" -ForegroundColor Green
        Write-Host "  BUILD SUCCESS" -ForegroundColor Green
        Write-Host "============================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "APK: $apkPath" -ForegroundColor Cyan
    } else {
        Write-Host ""
        Write-Host "BUILD FAILED. Exit code: $LASTEXITCODE" -ForegroundColor Red
        exit 1
    }
} finally {
    Pop-Location
}
