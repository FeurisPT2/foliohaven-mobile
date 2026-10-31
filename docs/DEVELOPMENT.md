# FolioHaven Mobile Development Guide

This guide covers setting up your local development environment, building the APK, executing tests, and debugging FolioHaven Mobile on Windows, macOS, or Linux.

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
Compiles the application and generates the debug APK:
```bash
./gradlew assembleDebug
```
Output artifact:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Run Unit Tests
Executes local JVM unit tests:
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

## 📐 Code Style & Architecture Guidelines

1. **State Management & Unidirectional Data Flow (UDF):**
   - State flows down via immutable data classes.
   - Events flow up via lambda callbacks.
   - Always collect states in Composables using `collectAsStateWithLifecycle()` to conserve battery and avoid leaking subscriptions in the background.

2. **Compose Previews:**
   - Every reusable component and screen should provide `@Preview` annotations for both Light and Dark themes:
   ```kotlin
   @Preview(showBackground = true, name = "Component - Light")
   @Composable
   fun ComponentLightPreview() {
       FolioHavenComposeTheme(darkTheme = false) { Component() }
   }

   @Preview(showBackground = true, name = "Component - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
   @Composable
   fun ComponentDarkPreview() {
       FolioHavenComposeTheme(darkTheme = true) { Component() }
   }
   ```

3. **Zero-Dependency Vector Graphics:**
   - Avoid adding large third-party icon libraries.
   - Use self-contained `@Composable` Canvas icons (like `HomeIcon`, `SearchIcon`, `BookShelfIcon`) or author vector drawables in `res/drawable/`.

4. **100% Privacy Floor:**
   - Never declare `android.permission.INTERNET` in `AndroidManifest.xml`.
   - Never import network-reliant tracking or analytics SDKs.
