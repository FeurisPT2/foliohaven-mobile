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
| **Target Form Factor** | Desktop / Ultrawide / Tablet | Desktop / Multi-window | Handheld Phone / Foldable / Tablet |

---

## 📊 Feature Parity Matrix

| Feature | Upstream (foliohaven) | Desktop (Foliohaven-pc) | Mobile Status | Mobile Notes & Implementation |
| :--- | :---: | :---: | :---: | :--- |
| **Brand Identity & Tokens** | ✅ Completed | ✅ Completed | ✅ **Completed** | Terracotta `#C2410C`, Archival espresso palettes, 2D FH Monogram vector. |
| **Home Shelf UI** | ✅ Completed | ✅ Completed | ✅ **Completed** | 3D archival book covers, reading streak cards, hero card. |
| **EPUB 2.0 / 3.2 Ingestion** | ✅ Completed | ✅ Completed | ✅ **Completed** | Clean-room ZIP + XHTML reflow parser without third-party bloat. |
| **PDF Rendering** | ✅ Completed | ✅ Completed | ✅ **Completed** | Native hardware-accelerated `android.graphics.pdf.PdfRenderer`. |
| **MOBI / AZW Support** | ✅ Completed | ⚠️ In Progress | ✅ **Completed** | Clean-room PalmDOC LZ77 decompression reflow engine with text search. |
| **Plain Text (.TXT) Reflow** | ✅ Completed | ✅ Completed | ✅ **Completed** | UTF-8 paragraph chunking, chapter heading detection, and word boundary reflow. |
| **Table of Contents (TOC)** | ✅ Completed | ✅ Completed | ✅ **Completed** | Interactive chapter drawer/sheet with 1-tap direct page jump across EPUB, TXT, MOBI. |
| **In-Book Live Search** | ✅ Completed | ✅ Completed | ✅ **Completed** | Real-time text search with match counts, surrounding context snippets, and page navigation. |
| **Reading Customization** | ✅ Completed | ✅ Completed | ✅ **Completed** | 4 Archival palettes, dynamic `A-`/`A+` font scaling, line height (`1.4x-2.0x`), margins (`16-32dp`). |
| **Active Reading Timer** | ✅ Completed | ✅ Completed | ✅ **Completed** | Session timer tracking active seconds read and persisting to SQLite statistics. |
| **Book Management & Shelf Actions** | ✅ Completed | ✅ Completed | ✅ **Completed** | Card overflow menus (`⋮`), edit metadata dialog, mark finished/in-progress toggle, safe book deletion. |
| **Storage Access Framework (SAF)** | N/A | N/A | ✅ **Completed** | Native document picker for EPUB, PDF, MOBI, and TXT files. |
| **Local SQLite Persistence** | ✅ IndexedDB | ✅ SQLite | ✅ **Completed** | WAL-mode SQLite database mirroring upstream `schema.sql`. |
| **Category Filters & Live Search**| ✅ Completed | ✅ Completed | ✅ **Completed** | Real-time title/author search and category/format chip filters. |
| **Interactive Bookmarks & Notes** | ✅ Completed | ✅ Completed | ✅ **Completed** | Unified tab with real-time bookmark listing, snippet display, and direct page jump. |
| **4-Color Annotation Engine** | ✅ Completed | ✅ Completed | ✅ **Completed** | Amber (`#FFE270`), Emerald (`#A8F29A`), Azure (`#7BE5F8`), Rose (`#FFA0C5`). |
| **Reading Statistics Tracking** | ✅ Completed | ✅ Completed | ✅ **Completed** | Real-time SQLite aggregation (books finished, total reading hours, streaks). |
| **Text-to-Speech (TTS)** | ✅ Web Speech | ✅ Native TTS | ✅ **Completed** | Native Android `TextToSpeech` service with audio controls. |
| **Cross-Client JSON Export/Import**| ✅ Completed | ✅ Completed | ✅ **Completed** | Portable JSON interchange format across PC, Web, and Mobile. |
| **Greeting & Onboarding Flow** | ✅ Completed | ✅ Completed | ✅ **Completed** | 3 feature pillars, reading palette picker, sample books toggle, persistent completion state. |
| **Zero Telemetry / 100% Offline**| ✅ 100% Offline | ✅ 100% Offline | ✅ **Strict 100%** | Zero internet permissions required or declared. |

---

## 🔄 Cross-Ecosystem Compatibility

To allow readers to move seamlessly between FolioHaven Web, PC, and Mobile:
1. **Unified Schema:** Mobile entity definitions mirror the upstream SQLite schema (`schema.sql` in parent repo).
2. **Deterministic Identifiers:** Books are identified by their SHA-256 binary hash across all three clients, preventing duplicates during transfer.
3. **Export/Import Interchange (.json):** User reading positions, bookmarks, and highlight notes can be exported and imported across PC and Android.
