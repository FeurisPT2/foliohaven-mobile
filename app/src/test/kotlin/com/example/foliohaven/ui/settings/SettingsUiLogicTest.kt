package com.example.foliohaven.ui.settings

import com.example.foliohaven.data.LibraryStorageStats
import com.example.foliohaven.data.StorageManager
import com.example.foliohaven.data.model.ReaderTheme
import com.example.foliohaven.data.repository.FakeSharedPreferences
import com.example.foliohaven.data.repository.UserPreferencesRepository
import com.example.foliohaven.model.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * Unit tests verifying the UI logic and reactive state coordination
 * of the FolioHaven Settings screen.
 */
class SettingsUiLogicTest {

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var preferencesRepository: UserPreferencesRepository

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
        preferencesRepository = UserPreferencesRepository(injectedPrefs = fakePrefs)
    }

    // ========================================================================
    // 1. Reading Preferences Defaults UI Logic
    // ========================================================================

    @Test
    fun readingPreferences_defaultValues_areCompliantWithArchivalSpecification() {
        assertEquals(16.0f, preferencesRepository.defaultFontSize.value, 0.001f)
        assertEquals(1.6f, preferencesRepository.defaultLineSpacing.value, 0.001f)
        assertEquals(ReaderTheme.SEPIA, preferencesRepository.defaultReaderTheme.value)
        assertEquals(24, preferencesRepository.defaultMarginsDp.value)
    }

    @Test
    fun readingPreferences_fontSizeStepper_incrementsAndDecrementsWithinBounds() {
        var currentSize = preferencesRepository.defaultFontSize.value

        // Step up
        currentSize += 1f
        preferencesRepository.setDefaultFontSize(currentSize)
        assertEquals(17.0f, preferencesRepository.defaultFontSize.value, 0.001f)

        // Step down
        currentSize -= 2f
        preferencesRepository.setDefaultFontSize(currentSize)
        assertEquals(15.0f, preferencesRepository.defaultFontSize.value, 0.001f)

        // Bound clamping at minimum (12sp)
        preferencesRepository.setDefaultFontSize(8.0f)
        assertEquals(12.0f, preferencesRepository.defaultFontSize.value, 0.001f)

        // Bound clamping at maximum (28sp)
        preferencesRepository.setDefaultFontSize(36.0f)
        assertEquals(28.0f, preferencesRepository.defaultFontSize.value, 0.001f)
    }

    @Test
    fun readingPreferences_themeSelection_updatesAllFourModesReactively() {
        val themes = listOf(ReaderTheme.DAY, ReaderTheme.SEPIA, ReaderTheme.NIGHT, ReaderTheme.DARK_SEPIA)
        for (theme in themes) {
            preferencesRepository.setDefaultReaderTheme(theme)
            assertEquals(theme, preferencesRepository.defaultReaderTheme.value)
            assertNotNull(theme.title)
            assertNotNull(theme.backgroundColor)
            assertNotNull(theme.textColor)
        }
    }

    @Test
    fun readingPreferences_lineSpacingPresets_updateCorrectly() {
        val presets = listOf(1.4f, 1.6f, 1.8f, 2.0f)
        for (preset in presets) {
            preferencesRepository.setDefaultLineSpacing(preset)
            assertEquals(preset, preferencesRepository.defaultLineSpacing.value, 0.01f)
            val formatted = String.format(Locale.US, "%.1fx", preferencesRepository.defaultLineSpacing.value)
            assertEquals("${preset}x", formatted)
        }
    }

    @Test
    fun readingPreferences_marginsPresets_updateCorrectly() {
        val marginPresets = listOf(16, 24, 32)
        for (margin in marginPresets) {
            preferencesRepository.setDefaultMarginsDp(margin)
            assertEquals(margin, preferencesRepository.defaultMarginsDp.value)
        }
    }

    // ========================================================================
    // 2. Text-to-Speech (TTS) Defaults UI Logic
    // ========================================================================

    @Test
    fun ttsDefaults_initialValues_areCompliant() {
        assertEquals(1.0f, preferencesRepository.ttsSpeechRate.value, 0.001f)
        assertEquals(1.0f, preferencesRepository.ttsPitch.value, 0.001f)
        assertEquals("vi-VN", preferencesRepository.ttsLanguage.value)
    }

    @Test
    fun ttsDefaults_speechRatePresets_updateAndFormatCorrectly() {
        val rates = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        for (rate in rates) {
            preferencesRepository.setTtsSpeechRate(rate)
            assertEquals(rate, preferencesRepository.ttsSpeechRate.value, 0.01f)
            val formatted = String.format(Locale.US, "%.2fx", preferencesRepository.ttsSpeechRate.value)
            assertEquals(String.format(Locale.US, "%.2fx", rate), formatted)
        }
    }

    @Test
    fun ttsDefaults_pitchPresets_clampAndFormatCorrectly() {
        val pitches = listOf(0.8f, 1.0f, 1.2f)
        for (p in pitches) {
            preferencesRepository.setTtsPitch(p)
            assertEquals(p, preferencesRepository.ttsPitch.value, 0.01f)
        }

        // Clamp below min 0.5f
        preferencesRepository.setTtsPitch(0.2f)
        assertEquals(0.5f, preferencesRepository.ttsPitch.value, 0.001f)

        // Clamp above max 2.0f
        preferencesRepository.setTtsPitch(3.0f)
        assertEquals(2.0f, preferencesRepository.ttsPitch.value, 0.001f)
    }

    @Test
    fun ttsDefaults_languageSelection_persistsAndSanitizes() {
        preferencesRepository.setTtsLanguage("en-US")
        assertEquals("en-US", preferencesRepository.ttsLanguage.value)

        preferencesRepository.setTtsLanguage("vi-VN")
        assertEquals("vi-VN", preferencesRepository.ttsLanguage.value)

        // Blank falls back to default "vi-VN"
        preferencesRepository.setTtsLanguage("   ")
        assertEquals("vi-VN", preferencesRepository.ttsLanguage.value)
    }

    // ========================================================================
    // 3. Storage & Library Management UI Statistics & Formatting Logic
    // ========================================================================

    @Test
    fun storageStats_readingTimeFormatting_producesHumanReadableStrings() {
        val zeroStats = LibraryStorageStats(totalBooks = 0, readingMinutes = 0L, totalStorageBytes = 0L)
        assertEquals("0 phút", zeroStats.formattedReadingTime)
        assertEquals(0L, zeroStats.readingHoursWhole)

        val minutesOnly = LibraryStorageStats(totalBooks = 2, readingMinutes = 45L, totalStorageBytes = 1024L)
        assertEquals("45 phút", minutesOnly.formattedReadingTime)
        assertEquals(0L, minutesOnly.readingHoursWhole)

        val hoursAndMinutes = LibraryStorageStats(totalBooks = 5, readingMinutes = 150L, totalStorageBytes = 5000000L)
        assertEquals("2g 30p", hoursAndMinutes.formattedReadingTime)
        assertEquals(2L, hoursAndMinutes.readingHoursWhole)
        assertEquals(2.5, hoursAndMinutes.readingHours, 0.001)

        val exactHours = LibraryStorageStats(totalBooks = 10, readingMinutes = 180L, totalStorageBytes = 10000000L)
        assertEquals("3 giờ", exactHours.formattedReadingTime)
        assertEquals(3L, exactHours.readingHoursWhole)
    }

    @Test
    fun storageStats_storageSizeFormatting_handlesAllUnitScales() {
        assertEquals("0 B", StorageManager.formatStorageSize(0L))
        assertEquals("500 B", StorageManager.formatStorageSize(500L))
        assertEquals("1.0 KB", StorageManager.formatStorageSize(1024L))
        assertEquals("12.5 MB", StorageManager.formatStorageSize((12.5 * 1024 * 1024).toLong()))
        assertEquals("1.2 GB", StorageManager.formatStorageSize((1.2 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun storageStats_aggregatedBreakdown_sumsCorrectly() {
        val booksBytes = 10 * 1024 * 1024L // 10 MB
        val dbBytes = 2 * 1024 * 1024L    // 2 MB
        val cacheBytes = 4 * 1024 * 1024L // 4 MB
        val totalBytes = booksBytes + dbBytes + cacheBytes

        val stats = LibraryStorageStats(
            totalBooks = 15,
            readingMinutes = 120L,
            totalStorageBytes = totalBytes,
            booksStorageBytes = booksBytes,
            databaseStorageBytes = dbBytes,
            cacheStorageBytes = cacheBytes
        )

        assertEquals(15, stats.totalBooks)
        assertEquals("2 giờ", stats.formattedReadingTime)
        assertEquals("16.0 MB", stats.formattedStorage)
        assertEquals(16 * 1024 * 1024L, stats.totalStorageBytes)
    }

    // ========================================================================
    // 4. About & App Info Verification
    // ========================================================================

    @Test
    fun appInfo_metadataAdheresToProjectSpecification() {
        val appInfo = AppInfo.DEFAULT
        assertEquals("FolioHaven Mobile", appInfo.appName)
        assertEquals("1.0", appInfo.versionName)
        assertEquals(1, appInfo.buildNumber)
        assertEquals("MIT License", appInfo.license)
        assertTrue(appInfo.offlineGuarantee.contains("100% Offline"))
        assertTrue(appInfo.offlineGuarantee.contains("0 network permissions"))
        assertFalse(appInfo.credits.isEmpty())

        // Verify credit categories
        val categories = appInfo.credits.map { it.category }.toSet()
        assertTrue(categories.any { it.contains("Kiến trúc") })
        assertTrue(categories.any { it.contains("Đọc Sách") })
        assertTrue(categories.any { it.contains("Bản sắc") })
    }
}
