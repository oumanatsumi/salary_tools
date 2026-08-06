@echo off
echo ============================================
echo   Install Salary Widget to Phone
echo ============================================
echo.

set ANDROID_HOME=E:\proj\salary-tools\.tools\android-sdk
set ADB=%ANDROID_HOME%\platform-tools\adb.exe
set APK=E:\proj\salary-tools\app\build\outputs\apk\debug\app-debug.apk

echo [1] Checking APK...
if not exist "%APK%" (
    echo   APK not found. Run build.bat first.
    pause
    exit /b 1
)
echo   APK found.

echo.
echo [2] Checking device connection...
%ADB% devices 2>nul
for /f "delims=" %%a in ('%ADB% devices ^| findstr "device$"') do set DEVICE=%%a
if "%DEVICE%"=="" (
    echo.
    echo   *** No device detected! ***
    echo.
    echo   Please make sure:
    echo   1. Phone connected via USB
    echo   2. Developer mode enabled
    echo   3. USB debugging enabled
    echo   4. Accepted USB debugging authorization on phone
    echo.
    pause
    exit /b 1
)
echo   Device found: %DEVICE%

echo.
echo [3] Installing...
%ADB% install -r "%APK%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo   INSTALL SUCCESS!
    echo ============================================
    echo.
    echo Next steps on your phone:
    echo   1. Open [Salary Widget] app, configure salary
    echo   2. Long-press home screen
    echo   3. Add widget: [Salary Widget]
    echo   4. Follow in-app guide for battery optimization
    echo.
) else (
    echo.
    echo INSTALL FAILED
    echo.
)
pause
