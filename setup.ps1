<#
.SYNOPSIS
    Salary Widget - One-Click Environment Setup
    Downloads Android SDK tools and Gradle, no Android Studio needed.
.DESCRIPTION
    Requires: JDK 17+, Internet connection
    First run downloads ~1-2 GB
#>

$ErrorActionPreference = "Stop"
$ProjectRoot = $PSScriptRoot
$ToolsDir = Join-Path $ProjectRoot ".tools"
$SdkRoot = Join-Path $ToolsDir "android-sdk"
$GradleHome = Join-Path $ToolsDir "gradle-8.9"
$GradleZip = Join-Path $ToolsDir "gradle-8.9-bin.zip"

$BuildToolsVersion = "35.0.0"
$PlatformVersion = "android-35"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Salary Widget - Environment Setup" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check JDK
Write-Host "[1/6] Checking JDK..." -ForegroundColor Yellow
$javaFound = $false
try {
    $javaOutput = cmd /c "java -version 2>&1"
    if ($javaOutput -match "version") {
        $javaFound = $true
        Write-Host "  OK: JDK found" -ForegroundColor Green
        Write-Host "  $($javaOutput -replace '\r?\n', ' ') " -ForegroundColor Gray
    }
} catch {}

if (-not $javaFound) {
    # Try direct path
    $javaHome = [Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")
    if (-not $javaHome) { $javaHome = [Environment]::GetEnvironmentVariable("JAVA_HOME", "User") }
    if (-not $javaHome) { $javaHome = $env:JAVA_HOME }

    if ($javaHome) {
        $javaExe = Join-Path $javaHome "bin\java.exe"
        if (Test-Path $javaExe) {
            Write-Host "  OK: Found Java at $javaExe" -ForegroundColor Green
            $javaFound = $true
        }
    }
}

if (-not $javaFound) {
    Write-Host "  ERROR: JDK 17+ is required!" -ForegroundColor Red
    Write-Host "  Download from: https://adoptium.net/" -ForegroundColor White
    Write-Host ""
    Write-Host "  If you have JDK installed, set JAVA_HOME manually:" -ForegroundColor Yellow
    Write-Host '  $env:JAVA_HOME = "C:\path\to\jdk"' -ForegroundColor White
    Write-Host '  .\setup.ps1' -ForegroundColor White
    exit 1
}

# Step 2: Download Gradle
Write-Host ""
Write-Host "[2/6] Checking Gradle..." -ForegroundColor Yellow

if (-not (Test-Path (Join-Path $GradleHome "bin\gradle.bat"))) {
    Write-Host "  Downloading Gradle 8.9..." -ForegroundColor Cyan
    New-Item -ItemType Directory -Path $ToolsDir -Force | Out-Null

    $gradleUrl = "https://services.gradle.org/distributions/gradle-8.9-bin.zip"

    if (-not (Test-Path $GradleZip)) {
        try {
            [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
            (New-Object System.Net.WebClient).DownloadFile($gradleUrl, $GradleZip)
        } catch {
            Write-Host "  Retry with curl..." -ForegroundColor Yellow
            & curl -L -o $GradleZip $gradleUrl
        }
    }

    Write-Host "  Extracting Gradle..." -ForegroundColor Cyan
    Expand-Archive -Path $GradleZip -DestinationPath $ToolsDir -Force
    Write-Host "  OK: Gradle ready" -ForegroundColor Green
} else {
    Write-Host "  OK: Gradle already exists" -ForegroundColor Green
}

$GradleBat = Join-Path $GradleHome "bin\gradle.bat"

# Step 3: Download Android SDK Command-line Tools
Write-Host ""
Write-Host "[3/6] Checking Android SDK Tools..." -ForegroundColor Yellow

$CmdlineToolsPath = Join-Path $SdkRoot "cmdline-tools\latest\bin\sdkmanager.bat"

if (-not (Test-Path $CmdlineToolsPath)) {
    Write-Host "  Downloading Android SDK command-line tools..." -ForegroundColor Cyan
    $CmdlineToolsZip = Join-Path $ToolsDir "commandlinetools-win.zip"
    $cmdlineUrl = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"

    if (-not (Test-Path $CmdlineToolsZip)) {
        try {
            [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
            (New-Object System.Net.WebClient).DownloadFile($cmdlineUrl, $CmdlineToolsZip)
        } catch {
            Write-Host "  Retry with curl..." -ForegroundColor Yellow
            & curl -L -o $CmdlineToolsZip $cmdlineUrl
        }
    }

    # Extract
    $CmdlineToolsTemp = Join-Path $ToolsDir "cmdline-tools-temp"
    if (Test-Path $CmdlineToolsTemp) { Remove-Item -Recurse -Force $CmdlineToolsTemp }
    Expand-Archive -Path $CmdlineToolsZip -DestinationPath $CmdlineToolsTemp -Force

    # Move to correct location: cmdline-tools/latest/
    $LatestDir = Join-Path $SdkRoot "cmdline-tools\latest"
    $ParentDir = Join-Path $SdkRoot "cmdline-tools"
    New-Item -ItemType Directory -Path $ParentDir -Force | Out-Null
    if (Test-Path $LatestDir) { Remove-Item -Recurse -Force $LatestDir }

    # Handle possible sub-directory structure
    $ExtractedDir = Join-Path $CmdlineToolsTemp "cmdline-tools"
    if (-not (Test-Path $ExtractedDir)) {
        $items = Get-ChildItem $CmdlineToolsTemp -Directory
        $ExtractedDir = $items[0].FullName
    }
    Move-Item -Path $ExtractedDir -Destination $LatestDir
    Remove-Item -Recurse -Force $CmdlineToolsTemp -ErrorAction SilentlyContinue

    Write-Host "  OK: SDK command-line tools ready" -ForegroundColor Green
} else {
    Write-Host "  OK: SDK command-line tools already exist" -ForegroundColor Green
}

# Step 4: Install SDK components
Write-Host ""
Write-Host "[4/6] Installing Android SDK components..." -ForegroundColor Yellow

$SdkManagerBat = Join-Path $SdkRoot "cmdline-tools\latest\bin\sdkmanager.bat"
$env:ANDROID_HOME = $SdkRoot
$env:ANDROID_SDK_ROOT = $SdkRoot

Write-Host "  SDK Root: $SdkRoot" -ForegroundColor Gray

# Install platform-tools
Write-Host "  Installing platform-tools..." -ForegroundColor Cyan
echo "y" | & $SdkManagerBat --install "platform-tools" --sdk_root=$SdkRoot 2>&1 | Out-Null

# Install build-tools
Write-Host "  Installing build-tools $BuildToolsVersion ..." -ForegroundColor Cyan
echo "y" | & $SdkManagerBat --install "build-tools;$BuildToolsVersion" --sdk_root=$SdkRoot 2>&1 | Out-Null

# Install platform
Write-Host "  Installing platforms/$PlatformVersion ..." -ForegroundColor Cyan
echo "y" | & $SdkManagerBat --install "platforms;$PlatformVersion" --sdk_root=$SdkRoot 2>&1 | Out-Null

# Accept licenses
Write-Host "  Accepting licenses..." -ForegroundColor Cyan
echo "y" | & $SdkManagerBat --licenses --sdk_root=$SdkRoot 2>&1 | Out-Null

Write-Host "  OK: SDK components installed" -ForegroundColor Green

# Step 5: Create local.properties
Write-Host ""
Write-Host "[5/6] Creating local.properties..." -ForegroundColor Yellow

$sdkDir = $SdkRoot.Replace('\', '\\')
$localPropsPath = Join-Path $ProjectRoot "local.properties"
Set-Content -Path $localPropsPath -Value "sdk.dir=$sdkDir" -Encoding ASCII
Write-Host "  OK: local.properties created" -ForegroundColor Green

# Step 6: Generate Gradle Wrapper
Write-Host ""
Write-Host "[6/6] Generating Gradle Wrapper..." -ForegroundColor Yellow
Push-Location $ProjectRoot
try {
    $env:JAVA_HOME = $null  # Let Gradle find Java itself
    & $GradleBat wrapper --gradle-version 8.9 2>&1 | Out-Null
    Write-Host "  OK: Gradle Wrapper generated" -ForegroundColor Green
} catch {
    Write-Host "  WARNING: Gradle Wrapper may need manual setup" -ForegroundColor Yellow
} finally {
    Pop-Location
}

# Done
Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "  SETUP COMPLETE!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  1. Run .\build.ps1 to build the APK" -ForegroundColor White
Write-Host "  2. Run .\install.ps1 to build and install to phone" -ForegroundColor White
Write-Host ""
