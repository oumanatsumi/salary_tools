"""
Android SDK auto-license acceptance and build helper.
Runs sdkmanager with proper stdin handling (Java tools break with
PowerShell pipe).
"""
import subprocess
import os
import sys

SDK_ROOT = r"E:\proj\salary-tools\.tools\android-sdk"
SDKMANAGER = os.path.join(SDK_ROOT, "cmdline-tools", "latest", "bin", "sdkmanager.bat")
PROJECT_ROOT = r"E:\proj\salary-tools"
JAVA_HOME = r"D:\Env\openjdk-23_windows-x64_bin\jdk-23"

def run_sdkmanager(args):
    """Run sdkmanager with 'y' piped to stdin."""
    env = os.environ.copy()
    env["ANDROID_HOME"] = SDK_ROOT
    env["ANDROID_SDK_ROOT"] = SDK_ROOT
    env["JAVA_HOME"] = JAVA_HOME

    p = subprocess.Popen(
        [SDKMANAGER] + args,
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        env=env,
        text=True
    )
    stdout, stderr = p.communicate(input="y\n" * 20, timeout=300)
    return p.returncode, stdout, stderr


def main():
    # Step 1: Accept all licenses
    print("=" * 50)
    print("[1/5] Accepting SDK licenses...")
    rc, out, err = run_sdkmanager(["--licenses", "--sdk_root=" + SDK_ROOT])
    print(out[-500:] if len(out) > 500 else out)
    if "All SDK package licenses accepted" in out:
        print("  OK: All licenses accepted")
    else:
        print("  Continuing (licenses may already exist)...")

    # Step 2: Install platform-tools
    print("\n[2/5] Installing platform-tools...")
    rc, out, err = run_sdkmanager([
        "--install", "platform-tools",
        "--sdk_root=" + SDK_ROOT
    ])
    print(out[-300:] if len(out) > 300 else out)

    # Step 3: Install build-tools 35.0.0
    print("\n[3/5] Installing build-tools 35.0.0...")
    rc, out, err = run_sdkmanager([
        "--install", "build-tools;35.0.0",
        "--sdk_root=" + SDK_ROOT
    ])
    print(out[-300:] if len(out) > 300 else out)

    # Step 4: Install platform android-35
    print("\n[4/5] Installing platform android-35...")
    rc, out, err = run_sdkmanager([
        "--install", "platforms;android-35",
        "--sdk_root=" + SDK_ROOT
    ])
    print(out[-300:] if len(out) > 300 else out)

    # Step 5: Build APK
    print("\n[5/5] Building APK...")
    gradle_bat = os.path.join(PROJECT_ROOT, ".tools", "gradle-8.9", "bin", "gradle.bat")

    env = os.environ.copy()
    env["JAVA_HOME"] = JAVA_HOME
    env["ANDROID_HOME"] = SDK_ROOT
    env["ANDROID_SDK_ROOT"] = SDK_ROOT
    env["_JAVA_OPTIONS"] = "-Xmx192m -Xms128m -XX:+UseSerialGC"
    env["PATH"] = os.path.join(JAVA_HOME, "bin") + os.pathsep + env["PATH"]

    p = subprocess.Popen(
        [gradle_bat, "assembleDebug", "--no-daemon", "--console=plain"],
        cwd=PROJECT_ROOT,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        env=env,
        text=True
    )

    # Stream output
    for line in p.stdout:
        print(line, end="")

    p.wait()

    if p.returncode == 0:
        apk_path = os.path.join(
            PROJECT_ROOT, "app", "build", "outputs", "apk", "debug", "app-debug.apk"
        )
        print("\n" + "=" * 50)
        print("  BUILD SUCCESS!")
        print("=" * 50)
        print(f"\nAPK: {apk_path}")
        print(f"\nTo install on phone:")
        print(f'  ".tools\\android-sdk\\platform-tools\\adb.exe" install -r "{apk_path}"')
    else:
        print("\nBUILD FAILED. Exit code:", p.returncode)
        sys.exit(1)


if __name__ == "__main__":
    main()
