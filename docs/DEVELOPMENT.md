# FolioHaven Mobile Development Guide

This guide covers setting up your local development environment, building APK artifacts, executing the test suite, and debugging FolioHaven Mobile on Windows, macOS, or Linux.

---

## 🛠️ Prerequisites

| Dependency | Minimum Version | Recommended Version |
| :--- | :--- | :--- |
| **JDK** | Java 17 | JDK 17 (Eclipse Temurin or Microsoft OpenJDK) |
| **Android Studio** | Ladybug (2024.2+) | Android Studio Ladybug Feature Drop (2024.2.2+) |
| **Android SDK Platform** | API 34 | API 36 (Android 16 preview / Android 15) |
| **Gradle** | 8.11+ | Bundled Gradle Wrapper 9.1.0 |
| **Kotlin** | 2.0+ | 2.3.20 (Compose Compiler Plugin) |

---

## ⚙️ Local Configuration

### 1. `local.properties`
Ensure a `local.properties` file exists in the repository root specifying your Android SDK directory:

```properties
# Windows example:
sdk.dir=C\:\\Users\\<username>\\AppData\\Local\\Android\\Sdk

# macOS/Linux example:
sdk.dir=/Users/<username>/Library/Android/sdk
```

> [!NOTE]
> `local.properties` is tracked in `.gitignore` and should never be committed.

### 2. Windows Environment Notice (Unquoted PATH)
On Windows, ensure your system and user `PATH` environment variables do not contain stray unescaped double quotes (e.g., `"C:\Program Files\..."` or trailing `\"`). These quotes can break the Java child process launcher for Gradle's test executor.

To sanitize your session in PowerShell before building:
```powershell
$env:PATH = $env:PATH.Replace('"', '')
```

---

## 🚀 Build Commands

Run the following commands using the Gradle wrapper (`./gradlew` on macOS/Linux, `.\gradlew.bat` on Windows):

### Build Debug APK
Compiles the application and generates the debug APK with Compose tooling:
```bash
./gradlew assembleDebug
```
Output artifact:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Build Release APK
Compiles an optimized, R8-minified production APK:
```bash
./gradlew assembleRelease
```
Output artifact:
```
app/build/outputs/apk/release/app-release-unsigned.apk
```

### Run Comprehensive Unit Tests
Executes all 110 local JVM unit tests (covering document engines, database helper, user preferences repository, and storage manager):
```bash
./gradlew testDebugUnitTest
```

### Install on Device or Emulator
Ensure an Android device or emulator is running (`adb devices`), then execute:
```bash
./gradlew installDebug
```

### Static Analysis & Lint
Run Android Lint to detect potential performance, accessibility, and code issues:
```bash
./gradlew lintDebug
```

---

## 📦 Release Artifacts & GitHub Distribution

Pre-built binaries are tracked in the repository under [`release-apks/`](../release-apks/):
- **[`release-apks/app-release.apk`](../release-apks/app-release.apk):** Production release build (~8.6 MB).
- **[`release-apks/app-debug.apk`](../release-apks/app-debug.apk):** Debug build with Compose inspection support (~9.5 MB).

Official tagged releases are published on GitHub:
- [**GitHub Release v1.0.0**](https://github.com/FeurisPT2/foliohaven-mobile/releases/tag/v1.0.0)

---

## 📱 Multi-Device Adaptive Testing

FolioHaven Mobile dynamically adjusts its layout between **Compact** (< 600dp), **Medium** (600dp - 839dp), and **Expanded** (≥ 840dp) screens:

1. **Standard Phone Mode (< 600dp):**
   - Test using standard phone emulators (e.g. Pixel 8, 1080x2400, 420dpi).
   - Verifies bottom navigation bar, single-column reader, and 2-column library shelf.
2. **Foldable Outer & Inner Screen Mode:**
   - Test using foldable emulators (e.g. 7.6" Fold-in Display 1812x2176).
   - Verifies seamless transition between folded phone mode and unfolded tablet mode.
3. **Tablet Mode (≥ 600dp):**
   - Test using tablet emulators (e.g. Pixel Tablet 2560x1600 or Galaxy Tab S9).
   - Verifies left-hand `NavigationRail`, 3-column split hero card, responsive library grid, and **two-column printed book spread** (Trang đôi 2 cột như sách in).

---

## 📐 Architecture & Quality Guidelines

1. **100% Privacy Floor (Strict Zero Internet):**
   - **Never** declare `android.permission.INTERNET` in `AndroidManifest.xml`.
   - **Never** import network analytics, crash telemetry, or ad SDKs.
   - All document reading, searching, text-to-speech, and storage operations must work entirely offline.

2. **State Management & Unidirectional Data Flow (UDF):**
   - State flows down via immutable data classes.
   - Events flow up via lambda callbacks.
   - Always collect states in Composables using `collectAsStateWithLifecycle()` to conserve battery and avoid leaking subscriptions in the background.

3. **Zero-Dependency Vector Graphics:**
   - Avoid adding large third-party icon libraries.
   - Author custom vector icons as `@Composable` Canvas drawing routines in [`SettingsIcons.kt`](file:///C:/Users/epicg/orca/foliohaven-mobile/app/src/main/kotlin/com/example/foliohaven/ui/icons/SettingsIcons.kt) and [`ReaderIcons.kt`](file:///C:/Users/epicg/orca/foliohaven-mobile/app/src/main/kotlin/com/example/foliohaven/ui/icons/ReaderIcons.kt).

4. **Input Sanitization & Boundary Clamping:**
   - All numeric settings (font size, line spacing, margins, speech rate, pitch) must be clamped to safe ranges in [`UserPreferencesRepository.kt`](file:///C:/Users/epicg/orca/foliohaven-mobile/app/src/main/kotlin/com/example/foliohaven/data/repository/UserPreferencesRepository.kt) to prevent rendering exceptions.
