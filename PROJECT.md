# Project: FolioHaven Mobile Settings Screen

## Architecture
- Architecture pattern: Clean Architecture / Unidirectional Data Flow (UDF) in Jetpack Compose
- UI Layer: Jetpack Compose (`ui/settings/SettingsScreen.kt`, `ui/settings/SettingsIcons.kt`, integrated into `HomeScreen.kt`)
- Data & Domain Layer: `UserPreferencesRepository`, `BookRepository`, `FolioHavenDbHelper`, `StorageManager`
- Platform Layer: Android Storage Access Framework (SAF), SharedPreferences, Context cache/files directories
- Constraints: 100% Offline, Zero Internet Permissions in AndroidManifest.xml, Native Canvas Vector Icons only, Literary Brand Aesthetic

## Feature Inventory
Every feature from user requirements and the survey phase is inventoried and assigned to milestones:
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | Reading Preferences Defaults | Font size (12-28sp, default 16), line spacing (1.2-2.2, default 1.6), reader theme (DAY, SEPIA, NIGHT, DARK_SEPIA), margins (12-36dp, default 24) persisted via UserPreferencesRepository | M1 | ORIGINAL_REQUEST §R1 |
| 2 | TTS Defaults | Speech rate (0.5x-2.0x, default 1.0x), pitch (0.5x-2.0x, default 1.0x), language preference ("vi-VN", "en-US", etc.) persisted via UserPreferencesRepository | M1 | ORIGINAL_REQUEST §R1 |
| 3 | Library Statistics Display | Compute & display total books count, total reading hours (from reading minutes), total storage used (app files, database, cache) | M1, M2 | ORIGINAL_REQUEST §R1 |
| 4 | Clear Cache & History | Clear temporary book cache and reset reading history progress with confirmation dialogs | M1, M2 | ORIGINAL_REQUEST §R1 |
| 5 | Metadata Backup & Export | Export library and preferences metadata to JSON via Android SAF | M1, M2 | ORIGINAL_REQUEST §R1 |
| 6 | About & App Info Section | Display version name ("1.0"), build number (1), MIT licensing, credits, and offline commitment guarantee | M2 | ORIGINAL_REQUEST §R1 |
| 7 | FolioHaven Literary Branding | Archival dark palette (Terracotta #C2410C, Espresso #1C1917, Card Surface #24201D, Border Charcoal #2E2A27, Text Parchment #FAF7F2, Text Linen #E7E5E4, Muted #A8A29E) and serif/sans typography | M2 | ORIGINAL_REQUEST §R2 |
| 8 | Native Canvas Vector Icons | Custom Canvas/DrawScope icons (SettingsGear, SlidersTune, TtsVoice, StorageDb, ShieldCheck, Trash) with 0 third-party icon dependencies | M2 | ORIGINAL_REQUEST §R2 |
| 9 | Zero Internet Permission Check | Strict 100% offline architecture with zero permissions in AndroidManifest.xml | M3 | ORIGINAL_REQUEST §R2 |
| 10 | Unit Test Suite | Comprehensive unit tests for repository, preferences, storage calculation, and logic components | M1, M3 | ORIGINAL_REQUEST §R3 |
| 11 | Static Analysis & Lint | Execute and pass Gradle lintDebug / lint with 0 fatal errors | M3 | ORIGINAL_REQUEST §R3 |
| 12 | Debug & Release APK Builds | Execute assembleDebug and assembleRelease successfully via Gradle | M3 | ORIGINAL_REQUEST §R3 |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | Data & Domain Layer | Extend UserPreferencesRepository with reading & TTS defaults; add storage calculation, cache/history clear, and metadata export logic to repositories; add comprehensive unit tests | none | DONE |
| M2 | UI Components, Canvas Icons & Settings Screen | Build SettingsScreen with 4 dedicated sections, native Canvas vector icons, color palette compliance, dialogs, and integrate into HomeScreen Tab 3 | M1 | DONE |
| M3 | Comprehensive Verification & Builds | Execute unit test suite, lintDebug static analysis, assembleDebug, assembleRelease, zero-permission manifest audit | M1, M2 | DONE |

## Interface Contracts
### Data Layer ↔ UI Layer
- `UserPreferencesRepository`:
  - `defaultFontSize: StateFlow<Float>`, `setDefaultFontSize(Float)`
  - `defaultLineSpacing: StateFlow<Float>`, `setDefaultLineSpacing(Float)`
  - `defaultReaderTheme: StateFlow<ReaderTheme>`, `setDefaultReaderTheme(ReaderTheme)`
  - `defaultMarginsDp: StateFlow<Int>`, `setDefaultMarginsDp(Int)`
  - `ttsSpeechRate: StateFlow<Float>`, `setTtsSpeechRate(Float)`
  - `ttsPitch: StateFlow<Float>`, `setTtsPitch(Float)`
  - `ttsLanguage: StateFlow<String>`, `setTtsLanguage(String)`
- `BookRepository` / `StorageManager`:
  - `getLibraryStorageStats(): LibraryStats` (totalBooks: Int, readingMinutes: Long, storageBytes: Long)
  - `clearCache(): Boolean`
  - `clearReadingHistory(): Boolean`
  - `exportMetadataJson(): String`
- `AppInfo`:
  - `versionName: String = "1.0"`, `buildNumber: Int = 1`, `license: String = "MIT License"`, `offlineGuarantee: String`

## Code Layout
- UI:
  - `app/src/main/kotlin/com/example/foliohaven/ui/settings/SettingsScreen.kt`
  - `app/src/main/kotlin/com/example/foliohaven/ui/settings/SettingsIcons.kt`
  - `app/src/main/kotlin/com/example/foliohaven/HomeScreen.kt`
- Data & Domain:
  - `app/src/main/kotlin/com/example/foliohaven/data/UserPreferencesRepository.kt`
  - `app/src/main/kotlin/com/example/foliohaven/data/BookRepository.kt`
  - `app/src/main/kotlin/com/example/foliohaven/data/FolioHavenDbHelper.kt`
  - `app/src/main/kotlin/com/example/foliohaven/data/StorageManager.kt`
  - `app/src/main/kotlin/com/example/foliohaven/model/AppInfo.kt`
- Tests:
  - `app/src/test/kotlin/com/example/foliohaven/data/UserPreferencesRepositoryTest.kt`
  - `app/src/test/kotlin/com/example/foliohaven/data/SettingsStorageTest.kt`
