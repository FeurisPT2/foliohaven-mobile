# FolioHaven Mobile (Android)

<p align="center">
  <img src="app/src/main/assets/branding/app_logo_512.png" alt="FolioHaven Mobile Logo" width="128" height="128" style="border-radius: 28px;" />
</p>

<p align="center">
  <strong>Serene Digital Bookshelf & Distraction-Free Ebook Reader for Android</strong><br>
  Built 100% native with Kotlin &amp; Jetpack Compose • Material 3 • Offline-First
</p>

<p align="center">
  <a href="https://github.com/FeurisPT2/foliohaven-mobile"><img src="https://img.shields.io/badge/Platform-Android%2014%2B-brightgreen?style=flat-square&logo=android" alt="Android Platform" /></a>
  <a href="https://github.com/FeurisPT2/Foliohaven-pc"><img src="https://img.shields.io/badge/PC%20Client-Flutter-blue?style=flat-square&logo=flutter" alt="PC Client" /></a>
  <a href="https://github.com/FeurisPT2/foliohaven"><img src="https://img.shields.io/badge/Web%20Portal-Vercel-black?style=flat-square&logo=vercel" alt="Web Portal" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-orange?style=flat-square" alt="MIT License" /></a>
</p>

---

## 🌟 Overview

**FolioHaven Mobile** is the official mobile Android companion for the FolioHaven literary ecosystem. Built from scratch with **Jetpack Compose** and **Material 3**, it delivers a tranquil, distraction-free reading experience optimized for handheld screens and OLED displays.

### Core Highlights
- 📖 **100% Offline & Private:** Zero account logins, zero telemetry tracking, and zero cloud lock-ins. All books, bookmarks, reading notes, and reading states remain entirely on your device.
- 🎨 **Archival Reading Palettes:** High-contrast OLED Pure Black, Warm Parchment Day, Antique Sepia, and Twilight Dark Sepia modes with custom font size, line spacing, and typography controls.
- 📚 **Comprehensive Library:** Universal reading progress tracking, custom tags, favorite shelves, and instant multi-format search (EPUB, PDF, MOBI, TXT).
- 💎 **Bespoke Craftsmanship:** Master 2D FH monogram identity, rich terracotta accents (`#C2410C`), archival embossed book covers, and fluid 60/120fps Compose animations.

---

## 🏛️ Ecosystem Repositories

| Repository | Platform / Technology | Purpose |
| :--- | :--- | :--- |
| **[foliohaven-mobile](https://github.com/FeurisPT2/foliohaven-mobile)** | Android (Jetpack Compose / Kotlin) | Native Android mobile application |
| **[Foliohaven-pc](https://github.com/FeurisPT2/Foliohaven-pc)** | Desktop (Flutter / Dart) | Desktop Windows, Linux, and macOS application |
| **[foliohaven](https://github.com/FeurisPT2/foliohaven)** | Web (HTML5 / Vanilla JS / Vercel) | Official web portal, landing page & in-browser reader |

---

## 🛠️ Building & Running Locally

### Prerequisites
- **Android Studio** Ladybug (2024.2+) or newer
- **JDK 17** or **JDK 21**
- **Android SDK Platform 34 / 36**

### Build Commands
```bash
# Clone the repository
git clone https://github.com/FeurisPT2/foliohaven-mobile.git
cd foliohaven-mobile

# Build Debug APK
./gradlew assembleDebug

# Install on connected device or emulator
./gradlew installDebug
```

---

## 📄 License
Released under the [MIT License](https://opensource.org/licenses/MIT).
