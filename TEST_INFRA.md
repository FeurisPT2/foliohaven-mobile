# E2E Test Infra: FolioHaven Mobile Settings Screen

## Test Philosophy
- Opaque-box, requirement-driven. Derived from ORIGINAL_REQUEST.md.
- Verification channels: Unit test assertions via JUnit 4 / Kotlin Coroutines Test, Gradle build tasks (`testDebugUnitTest`, `lintDebug`, `assembleDebug`, `assembleRelease`), manifest inspection for 0 internet permissions, and Compose state/canvas integrity.
- Methodology: Category-Partition + Boundary Value Analysis (BVA) + Pairwise Combinatorial Testing + Real-World Workload Testing.

## Feature Inventory & Test Coverage Goals
| # | Feature | Source (Requirement) | Tier 1 (Coverage) | Tier 2 (Boundary/Edge) | Tier 3 (Cross-Feature) | Tier 4 (Workload) |
|---|---------|----------------------|:-----------------:|:----------------------:|:----------------------:|:-----------------:|
| 1 | Reading Preferences Defaults | ORIGINAL_REQUEST §R1 | 5 | 5 | ✓ | ✓ |
| 2 | TTS Defaults | ORIGINAL_REQUEST §R1 | 5 | 5 | ✓ | ✓ |
| 3 | Storage & Library Statistics | ORIGINAL_REQUEST §R1 | 5 | 5 | ✓ | ✓ |
| 4 | Clear Cache & History | ORIGINAL_REQUEST §R1 | 5 | 5 | ✓ | ✓ |
| 5 | Metadata Backup & Export | ORIGINAL_REQUEST §R1 | 5 | 5 | ✓ | ✓ |
| 6 | About & App Info Section | ORIGINAL_REQUEST §R1 | 5 | 5 | ✓ | ✓ |
| 7 | FolioHaven Literary Branding | ORIGINAL_REQUEST §R2 | 5 | 5 | ✓ | ✓ |
| 8 | Native Canvas Vector Icons | ORIGINAL_REQUEST §R2 | 5 | 5 | ✓ | ✓ |
| 9 | Zero Internet Permission Check | ORIGINAL_REQUEST §R2 | 5 | 5 | ✓ | ✓ |

## Test Architecture
- Test runner: `./gradlew testDebugUnitTest`
- Static analysis: `./gradlew lintDebug`
- Build packages: `./gradlew assembleDebug` and `./gradlew assembleRelease`
- Pass/Fail semantics: All assertions must pass with 0 test failures, lint with 0 fatal errors, both APKs assembled with exit code 0.

## Real-World Application Scenarios (Tier 4)
| # | Scenario | Features Exercised | Complexity |
|---|----------|--------------------|------------|
| 1 | First Launch to Custom Reading Defaults | F1 (Theme, Font, Spacing, Margins), F7 (Branding) | Medium |
| 2 | Offline TTS Customization & Persistence Across App Restarts | F2 (Rate, Pitch, Language), F9 (Offline) | Medium |
| 3 | Heavy Library Reading, Stats Calculation & Storage Cleanup | F3 (Stats), F4 (Clear Cache & History) | High |
| 4 | Library Metadata Backup & Offline Recovery | F5 (Export JSON), F3 (Stats), F9 (Offline) | High |
| 5 | Full Settings Audit: Branding, Canvas Icons, App Info, Zero Network | F6 (About), F7 (Palette), F8 (Canvas Icons), F9 (Zero Internet) | High |

## Coverage Thresholds
- Tier 1: ≥5 per feature (Total ≥ 45 test cases)
- Tier 2: ≥5 per feature (Total ≥ 45 test cases)
- Tier 3: Pairwise coverage of major feature interactions (≥ 9 combination cases)
- Tier 4: ≥5 realistic application scenarios
