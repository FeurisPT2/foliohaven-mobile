# FolioHaven Mobile (Android)

<p align="center">
  <img src="app/src/main/assets/branding/app_logo_512.png" alt="FolioHaven Mobile Logo" width="128" height="128" style="border-radius: 28px;" />
</p>

<p align="center">
  <strong>Serene Digital Bookshelf & Distraction-Free Ebook Reader for Android</strong><br>
  Built 100% native with Kotlin &amp; Jetpack Compose • Material 3 • Material You • Adaptive Layout • 100% Offline
</p>

<p align="center">
  <a href="https://github.com/FeurisPT2/foliohaven-mobile/releases/tag/v1.0.0"><img src="https://img.shields.io/badge/Release-v1.0.0-brightgreen?style=flat-square&logo=github" alt="Latest Release" /></a>
  <a href="https://github.com/FeurisPT2/foliohaven-mobile"><img src="https://img.shields.io/badge/Platform-Android%2014%2B%20(API%2024%2B)-brightgreen?style=flat-square&logo=android" alt="Android Platform" /></a>
  <a href="https://github.com/FeurisPT2/Foliohaven-pc"><img src="https://img.shields.io/badge/PC%20Client-Flutter-blue?style=flat-square&logo=flutter" alt="PC Client" /></a>
  <a href="https://github.com/FeurisPT2/foliohaven"><img src="https://img.shields.io/badge/Web%20Portal-Vercel-black?style=flat-square&logo=vercel" alt="Web Portal" /></a>
  <a href="#"><img src="https://img.shields.io/badge/Network%20Permissions-Zero%20(100%25%20Offline)-success?style=flat-square" alt="Zero Network Permissions" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-orange?style=flat-square" alt="MIT License" /></a>
</p>

---

## 📥 Download Pre-built APKs (v1.0.0)

Directly installable APKs are compiled and available in this repository and on GitHub Releases:

| Artifact | Architecture | Download Link | Description |
| :--- | :--- | :--- | :--- |
| **Release APK** | Universal (`arm64-v8a`, `armeabi-v7a`, `x86_64`) | [**Download `app-release.apk`**](release-apks/app-release.apk) | Optimized, R8 minified, production-ready build |
| **Debug APK** | Universal | [**Download `app-debug.apk`**](release-apks/app-debug.apk) | Debug build with logging & Compose tooling |
| **GitHub Release** | All Platforms | [**GitHub Release v1.0.0**](https://github.com/FeurisPT2/foliohaven-mobile/releases/tag/v1.0.0) | Official release notes & verified assets |

---

## 🌟 Overview

**FolioHaven Mobile** is the official Android companion for the FolioHaven literary ecosystem. Written entirely in **Kotlin** with modern **Jetpack Compose**, it provides an elegant, distraction-free sanctuary for serious readers, optimized across phone, foldable, and tablet form factors.

### ✨ Core Highlights

- 🛡️ **100% Offline & Private:** Zero account logins, zero telemetry tracking, and **zero network permissions** (`INTERNET` permission is completely omitted from `AndroidManifest.xml`). All books, reading positions, highlights, and annotations remain strictly on your personal device.
- 📱 **Adaptive Form Factor Support:**
  - **Phone (< 600dp):** Ergonomic bottom navigation bar, single-column reading canvas, and compact cards.
  - **Tablets & Foldables (≥ 600dp):** Left `NavigationRail`, 3-column split hero card, responsive library grid (`GridCells.Adaptive`), multi-column notes/bookmarks grid, and **two-column book spread** (Trang đôi 2 cột như sách in) with subtle vertical gutter divider.
- 🎨 **Material Design 3 & Material You:**
  - Full M3 token adoption (`surfaceContainer`, `surfaceTint`, `inverseSurface`, M3 `FilterChip`, M3 28dp pill `SearchBar`).
  - Optional **Material You (Dynamic Color Monet)** support on Android 12+ (API 31+) extracting accents from device wallpaper, with fallback to FolioHaven's signature Terracotta (`#C2410C`) & Archival Charcoal theme.
- 📖 **Four Archival Reading Palettes:** High-contrast OLED Pure Black, Warm Parchment Day, Antique Sepia, and Twilight Dark Sepia modes with live font size scaling (`12sp` - `32sp`), line spacing (`1.2x` - `2.4x`), and margin controls.
- 📚 **Comprehensive Format Freedom:** Clean-room native parsers for **EPUB 2/3**, **PDF** (hardware-accelerated `PdfRenderer`), **MOBI** (PalmDOC LZ77 clean-room decompressor), and **Plain Text (TXT)**.
- 🎙️ **On-Device Text-to-Speech (TTS):** Native Android speech engine with customizable reading rate (`0.5x` - `2.0x`), pitch adjustments, language selection (Vietnamese, English, Auto), and live sample test.
- 🌐 **Bi-lingual Support (Tiếng Việt & English):** Seamless in-app language switching with a 3-step animated onboarding carousel in `GreetingScreen`.
- ⚙️ **Production Settings & Storage Suite:** Real-time disk storage analytics (books, cache, SQLite database), instant cache cleaner, reading history reset, and portable JSON backup & restore via Android Storage Access Framework (SAF).

---

## 🏛️ Ecosystem Repositories

| Repository | Platform / Technology | Purpose |
| :--- | :--- | :--- |
| **[foliohaven-mobile](https://github.com/FeurisPT2/foliohaven-mobile)** | Android (Jetpack Compose / Kotlin) | Native Android mobile & tablet application |
| **[Foliohaven-pc](https://github.com/FeurisPT2/Foliohaven-pc)** | Desktop (Flutter / Dart) | Desktop Windows, Linux, and macOS application |
| **[foliohaven](https://github.com/FeurisPT2/foliohaven)** | Web (HTML5 / Vanilla JS / Vercel) | Official web portal, landing page & in-browser reader |

---

## 📚 Technical Documentation

Comprehensive architectural and engineering documentation is available in the [`docs/`](docs/) directory:

- [**Architecture Guide**](docs/ARCHITECTURE.md): Jetpack Compose UDF architecture, adaptive layout engine, domain models, and offline persistence layer.
- [**Implementation Roadmap**](docs/ROADMAP.md): Complete 10-phase development roadmap from prototype to v1.0.0 release.
- [**Design Tokens & Brand Identity**](docs/DESIGN_TOKENS.md): Color tokens, Material 3 shapes, typography scale, 3D archival book covers, and adaptive layout measures.
- [**Ecosystem & Feature Parity**](docs/ECOSYSTEM_PARITY.md): Feature matrix comparing Web, Desktop, and Mobile clients.
- [**Development & Build Guide**](docs/DEVELOPMENT.md): Local prerequisites, Windows setup, Gradle tasks, test suites, and release packaging.

---

## 🛠️ Building & Testing Locally

### Prerequisites
- **Android Studio** Ladybug (2024.2+) or newer
- **JDK 17** or **JDK 21** (e.g. Eclipse Temurin or Microsoft OpenJDK)
- **Android SDK Platform API 34 / 36**

### Build Commands
```bash
# Clone the repository
git clone https://github.com/FeurisPT2/foliohaven-mobile.git
cd foliohaven-mobile

# Run full unit test suite (110 tests)
./gradlew testDebugUnitTest

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease

# Install on connected device or emulator
./gradlew installDebug
```

> [!TIP]
> On Windows PowerShell, sanitize your `$env:PATH` to remove stray double quotes before executing Gradle:
> ```powershell
> $env:PATH = $env:PATH.Replace('"', '')
> .\gradlew.bat testDebugUnitTest
> ```

---

## 📄 License
Released under the [MIT License](LICENSE).
