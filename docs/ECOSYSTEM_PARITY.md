# Ecosystem & Feature Parity

FolioHaven is a unified literary ecosystem spanning Web, Desktop, and Mobile platforms. This document tracks feature parity between the parent ecosystem ([foliohaven](https://github.com/FeurisPT2/foliohaven)), the desktop client ([Foliohaven-pc](https://github.com/FeurisPT2/Foliohaven-pc)), and this Android mobile application (`foliohaven-mobile`).

---

## 🏛️ Ecosystem Overview

| Attribute | Web Portal / Core | Desktop (PC Client) | Mobile (Android) |
| :--- | :--- | :--- | :--- |
| **Repository** | [FeurisPT2/foliohaven](https://github.com/FeurisPT2/foliohaven) | [FeurisPT2/Foliohaven-pc](https://github.com/FeurisPT2/Foliohaven-pc) | [FeurisPT2/foliohaven-mobile](https://github.com/FeurisPT2/foliohaven-mobile) |
| **Technology** | HTML5 / Vanilla JS / Vercel | Flutter / Dart 3 | Kotlin / Jetpack Compose |
| **Primary Platforms** | Modern Web Browsers | Windows, Linux, macOS | Android 14+ (minSdk 24, targetSdk 36) |
| **Local Storage** | IndexedDB / LocalStorage | SQLite (WAL mode) | Native SQLite Database (WAL mode) |
| **Target Form Factors** | Desktop / Ultrawide / Tablet | Desktop / Multi-window | Handheld Phone / Foldable / Tablet |
| **Release Artifact** | Web Deployment | Native Executable | Universal APKs (Release & Debug v1.0.0) |

---

## 📊 Feature Parity Matrix

| Feature | Upstream (foliohaven) | Desktop (Foliohaven-pc) | Mobile Status | Mobile Notes & Implementation |
| :--- | :---: | :---: | :---: | :--- |
| **Brand Identity & Tokens** | ✅ Completed | ✅ Completed | ✅ **Completed** | Terracotta `#C2410C`, Archival espresso palettes, 2D FH Monogram vector. |
| **Home Shelf UI** | ✅ Completed | ✅ Completed | ✅ **Completed** | 3D archival book covers, reading streak cards, continue reading hero. |
| **EPUB 2.0 / 3.2 Ingestion** | ✅ Completed | ✅ Completed | ✅ **Completed** | Clean-room ZIP + XHTML reflow parser without third-party bloat. |
| **PDF Rendering** | ✅ Completed | ✅ Completed | ✅ **Completed** | Native hardware-accelerated `android.graphics.pdf.PdfRenderer`. |
| **MOBI / AZW Support** | ✅ Completed | ⚠️ In Progress | ✅ **Completed** | Clean-room PalmDOC LZ77 decompression reflow engine with text search. |
| **Plain Text (.TXT) Reflow** | ✅ Completed | ✅ Completed | ✅ **Completed** | UTF-8 paragraph chunking, chapter heading detection, and word boundary reflow. |
| **Table of Contents (TOC)** | ✅ Completed | ✅ Completed | ✅ **Completed** | Interactive chapter drawer/sheet with 1-tap direct page jump across EPUB, TXT, MOBI. |
| **In-Book Live Search** | ✅ Completed | ✅ Completed | ✅ **Completed** | Real-time text search with match counts, surrounding context snippets, and page navigation. |
| **Reading Customization** | ✅ Completed | ✅ Completed | ✅ **Completed** | 4 Archival palettes, dynamic `A-`/`A+` font scaling, line height (`1.2x-2.4x`), margins (`8-48dp`). |
| **Active Reading Timer** | ✅ Completed | ✅ Completed | ✅ **Completed** | Session timer tracking active seconds read and persisting to SQLite statistics. |
| **Book Management & Shelf Actions** | ✅ Completed | ✅ Completed | ✅ **Completed** | Card overflow menus (`⋮`), edit metadata dialog, mark finished/in-progress toggle, safe book deletion. |
| **Storage Access Framework (SAF)** | N/A | N/A | ✅ **Completed** | Native document picker for EPUB, PDF, MOBI, and TXT files. |
| **Local SQLite Persistence** | ✅ IndexedDB | ✅ SQLite | ✅ **Completed** | WAL-mode SQLite database mirroring upstream `schema.sql`. |
| **Category Filters & Live Search**| ✅ Completed | ✅ Completed | ✅ **Completed** | Real-time title/author search, M3 28dp pill `SearchBar`, and M3 `FilterChip` selectors. |
| **Interactive Bookmarks & Notes** | ✅ Completed | ✅ Completed | ✅ **Completed** | Unified tab with real-time bookmark listing, snippet display, and direct page jump. |
| **4-Color Annotation Engine** | ✅ Completed | ✅ Completed | ✅ **Completed** | Amber (`#FFE270`), Emerald (`#A8F29A`), Azure (`#7BE5F8`), Rose (`#FFA0C5`). |
| **Reading Statistics Tracking** | ✅ Completed | ✅ Completed | ✅ **Completed** | Real-time SQLite aggregation (books finished, total reading hours, streaks). |
| **Text-to-Speech (TTS)** | ✅ Web Speech | ✅ Native TTS | ✅ **Completed** | Native Android `TextToSpeech` service with customizable speed, pitch, and locale. |
| **Cross-Client JSON Export/Import**| ✅ Completed | ✅ Completed | ✅ **Completed** | Portable JSON interchange format across PC, Web, and Mobile via SAF. |
| **Stepped Onboarding Carousel** | ✅ Completed | ✅ Completed | ✅ **Completed** | 3-step animated slide/fade onboarding flow with persistent completion state. |
| **Bi-lingual Support (EN / VI)** | ✅ Completed | ⚠️ In Progress | ✅ **Completed** | Live language switcher with instant UI updates and synchronized TTS voice locale. |
| **Tablet & Foldable Adaptations** | ✅ Completed | ✅ Completed | ✅ **Completed** | `WindowWidthSizeClass` adaptive engine, left `NavigationRail`, 2-column printed book spread. |
| **Material 3 & Material You** | N/A | N/A | ✅ **Completed** | M3 shapes, M3 surface containers, and dynamic color Monet theming on Android 12+. |
| **Settings & Storage Suite** | ✅ Completed | ✅ Completed | ✅ **Completed** | Live disk usage breakdown, instant cache purge, history reset, and reading defaults preview. |
| **Zero Telemetry / 100% Offline**| ✅ 100% Offline | ✅ 100% Offline | ✅ **Strict 100%** | Zero internet permissions required or declared in `AndroidManifest.xml`. |

---

## 🔄 Cross-Ecosystem Compatibility

To allow readers to move seamlessly between FolioHaven Web, PC, and Mobile:
1. **Unified Schema:** Mobile entity definitions mirror the upstream SQLite schema (`schema.sql` in parent repo).
2. **Deterministic Identifiers:** Books are identified by their SHA-256 binary hash across all three clients, preventing duplicates during transfer.
3. **Export/Import Interchange (.json):** User reading positions, bookmarks, and highlight notes can be exported and imported across PC and Android.
4. **Offline Parity:** All clients guarantee zero reliance on cloud servers or external accounts.
