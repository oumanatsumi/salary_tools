@echo off
echo ============================================
echo   Salary Widget - Build APK
echo ============================================
echo.

set JAVA_HOME=D:\Env\openjdk-23_windows-x64_bin\jdk-23
set ANDROID_HOME=E:\proj\salary-tools\.tools\android-sdk
set ANDROID_SDK_ROOT=%ANDROID_HOME%

REM Fix: Use SerialGC + small heap to avoid G1 large virtual memory reservation
set _JAVA_OPTIONS=-Xmx128m -Xms64m -XX:+UseSerialGC -XX:ReservedCodeCacheSize=32m -XX:MaxMetaspaceSize=80m
set GRADLE_OPTS=-Dorg.gradle.jvmargs="-Xmx192m -XX:+UseSerialGC -XX:ReservedCodeCacheSize=32m"

REM Clean any stale daemons
rd /s /q "%USERPROFILE%\.gradle\daemon" 2>nul

REM Run Gradle with full heap args
E:\proj\salary-tools\.tools\gradle-8.9\bin\gradle.bat assembleDebug --no-daemon --console=plain 2>&1

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo   BUILD SUCCESS!
    echo ============================================
    echo.
    echo APK: E:\proj\salary-tools\app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo To install to phone, run: install.bat
) else (
    echo.
    echo BUILD FAILED. Try these fixes:
    echo   1. Close other programs to free memory
    echo   2. Increase Windows pagefile size
    echo   3. Install Android Studio which handles this automatically
)
pause
