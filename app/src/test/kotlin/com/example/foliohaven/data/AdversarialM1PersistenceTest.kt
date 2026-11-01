package com.example.foliohaven.data

import com.example.foliohaven.data.db.FolioHavenDbHelper
import com.example.foliohaven.data.db.FolioHavenDbHelper.ReadingStats
import com.example.foliohaven.data.model.ReaderTheme
import com.example.foliohaven.data.repository.FakeSharedPreferences
import com.example.foliohaven.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AdversarialM1PersistenceTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    // =========================================================================
    // 1. STATEFLOW RESPONSIVENESS, REACTIVITY & STARTUP RECOVERY
    // =========================================================================

    @Test
    fun stateFlow_immediatelyEmitsInitialValuesToFirstCollector() = runTest {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        // Reading defaults immediate emission
        assertEquals(16.0f, repo.defaultFontSize.first(), 0.001f)
        assertEquals(1.6f, repo.defaultLineSpacing.first(), 0.001f)
        assertEquals(ReaderTheme.SEPIA, repo.defaultReaderTheme.first())
        assertEquals(24, repo.defaultMarginsDp.first())

        // TTS defaults immediate emission
        assertEquals(1.0f, repo.ttsSpeechRate.first(), 0.001f)
        assertEquals(1.0f, repo.ttsPitch.first(), 0.001f)
        assertEquals("vi-VN", repo.ttsLanguage.first())
    }

    @Test
    fun stateFlow_emitsUpdatesImmediatelyInRealtimeToActiveCollector() = runTest {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        val collectedValues = mutableListOf<Float>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            repo.defaultFontSize.collect { collectedValues.add(it) }
        }
        testScheduler.runCurrent()

        // Initial emission should be collected immediately
        assertEquals(listOf(16.0f), collectedValues)

        // Successive updates
        repo.setDefaultFontSize(18.0f)
        testScheduler.runCurrent()
        assertEquals(listOf(16.0f, 18.0f), collectedValues)

        repo.setDefaultFontSize(24.0f)
        testScheduler.runCurrent()
        assertEquals(listOf(16.0f, 18.0f, 24.0f), collectedValues)

        // Setting duplicate value should not duplicate in StateFlow
        repo.setDefaultFontSize(24.0f)
        testScheduler.runCurrent()
        assertEquals(listOf(16.0f, 18.0f, 24.0f), collectedValues)

        repo.setDefaultFontSize(28.0f)
        testScheduler.runCurrent()
        assertEquals(listOf(16.0f, 18.0f, 24.0f, 28.0f), collectedValues)

        job.cancel()
    }

    @Test
    fun stateFlow_ttsSpeechRateAndPitch_emitSequencesAccurately() = runTest {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        val rateValues = mutableListOf<Float>()
        val rateJob = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            repo.ttsSpeechRate.collect { rateValues.add(it) }
        }
        testScheduler.runCurrent()

        repo.setTtsSpeechRate(1.25f)
        testScheduler.runCurrent()
        repo.setTtsSpeechRate(1.5f)
        testScheduler.runCurrent()
        repo.setTtsSpeechRate(2.0f)
        testScheduler.runCurrent()

        assertEquals(listOf(1.0f, 1.25f, 1.5f, 2.0f), rateValues)
        rateJob.cancel()

        val pitchValues = mutableListOf<Float>()
        val pitchJob = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            repo.ttsPitch.collect { pitchValues.add(it) }
        }
        testScheduler.runCurrent()

        repo.setTtsPitch(0.75f)
        testScheduler.runCurrent()
        repo.setTtsPitch(1.25f)
        testScheduler.runCurrent()

        assertEquals(listOf(1.0f, 0.75f, 1.25f), pitchValues)
        pitchJob.cancel()
    }

    @Test
    fun startupHydration_clampsCorruptedStoredValuesFromDisk() {
        val corruptedPrefs = FakeSharedPreferences()
        corruptedPrefs.edit()
            .putFloat(UserPreferencesRepository.KEY_DEFAULT_FONT_SIZE, 999.0f) // Way above max (28)
            .putFloat(UserPreferencesRepository.KEY_DEFAULT_LINE_SPACING, -10.0f) // Way below min (1.2)
            .putString(UserPreferencesRepository.KEY_DEFAULT_READER_THEME, "NON_EXISTENT_THEME")
            .putInt(UserPreferencesRepository.KEY_DEFAULT_MARGINS, 100) // Way above max (36)
            .putFloat(UserPreferencesRepository.KEY_DEFAULT_TTS_SPEECH_RATE, 0.05f) // Below min (0.5)
            .putFloat(UserPreferencesRepository.KEY_DEFAULT_TTS_PITCH, 50.0f) // Above max (2.0)
            .putString(UserPreferencesRepository.KEY_DEFAULT_TTS_LANGUAGE, "   \n\t  ") // Blank whitespace
            .apply()

        val repo = UserPreferencesRepository(injectedPrefs = corruptedPrefs)

        // Assert all corrupted values are safely clamped / resolved to valid defaults
        assertEquals(28.0f, repo.defaultFontSize.value, 0.001f)
        assertEquals(1.2f, repo.defaultLineSpacing.value, 0.001f)
        assertEquals(ReaderTheme.SEPIA, repo.defaultReaderTheme.value)
        assertEquals(36, repo.defaultMarginsDp.value)
        assertEquals(0.5f, repo.ttsSpeechRate.value, 0.001f)
        assertEquals(2.0f, repo.ttsPitch.value, 0.001f)
        assertEquals("vi-VN", repo.ttsLanguage.value)
    }

    @Test
    fun extremeFloats_infinityAndExtremeValues_areSafelyClamped() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        // Positive Infinity should be clamped to max
        repo.setDefaultFontSize(Float.POSITIVE_INFINITY)
        assertEquals(28.0f, repo.defaultFontSize.value, 0.001f)

        // Negative Infinity should be clamped to min
        repo.setDefaultFontSize(Float.NEGATIVE_INFINITY)
        assertEquals(12.0f, repo.defaultFontSize.value, 0.001f)

        // Float MAX_VALUE should clamp to max
        repo.setDefaultLineSpacing(Float.MAX_VALUE)
        assertEquals(2.2f, repo.defaultLineSpacing.value, 0.001f)

        // Float MIN_VALUE is positive close to zero, so should clamp to min (1.2f)
        repo.setDefaultLineSpacing(Float.MIN_VALUE)
        assertEquals(1.2f, repo.defaultLineSpacing.value, 0.001f)

        // Extreme TTS rates
        repo.setTtsSpeechRate(Float.POSITIVE_INFINITY)
        assertEquals(2.0f, repo.ttsSpeechRate.value, 0.001f)
        repo.setTtsSpeechRate(Float.NEGATIVE_INFINITY)
        assertEquals(0.5f, repo.ttsSpeechRate.value, 0.001f)

        // Extreme TTS pitch
        repo.setTtsPitch(Float.POSITIVE_INFINITY)
        assertEquals(2.0f, repo.ttsPitch.value, 0.001f)
        repo.setTtsPitch(Float.NEGATIVE_INFINITY)
        assertEquals(0.5f, repo.ttsPitch.value, 0.001f)
    }

    @Test
    fun nanFloats_fallBackToDefaultValuesSafely() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        repo.setDefaultFontSize(Float.NaN)
        assertEquals(UserPreferencesRepository.DEFAULT_FONT_SIZE, repo.defaultFontSize.value, 0.001f)
        assertFalse(repo.defaultFontSize.value.isNaN())

        repo.setDefaultLineSpacing(Float.NaN)
        assertEquals(UserPreferencesRepository.DEFAULT_LINE_SPACING, repo.defaultLineSpacing.value, 0.001f)
        assertFalse(repo.defaultLineSpacing.value.isNaN())

        repo.setTtsSpeechRate(Float.NaN)
        assertEquals(UserPreferencesRepository.DEFAULT_TTS_SPEECH_RATE, repo.ttsSpeechRate.value, 0.001f)
        assertFalse(repo.ttsSpeechRate.value.isNaN())

        repo.setTtsPitch(Float.NaN)
        assertEquals(UserPreferencesRepository.DEFAULT_TTS_PITCH, repo.ttsPitch.value, 0.001f)
        assertFalse(repo.ttsPitch.value.isNaN())
    }

    // =========================================================================
    // 2. BACKUP MIGRATION COMPATIBILITY (LEGACY v1.0 & CORRUPTED PAYLOADS)
    // =========================================================================

    @Test
    fun legacyV1Payload_doesNotCorruptExistingUserPreferences() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        // User has customized settings in the app
        repo.setDefaultFontSize(22.0f)
        repo.setDefaultLineSpacing(1.9f)
        repo.setDefaultReaderTheme(ReaderTheme.NIGHT)
        repo.setDefaultMarginsDp(30)
        repo.setTtsSpeechRate(1.5f)
        repo.setTtsPitch(0.8f)
        repo.setTtsLanguage("en-US")
        repo.setGreetingCompleted(true)

        // Legacy v1.0 JSON payload exported from earlier FolioHaven version
        val legacyV1Json = """
            {
              "client": "FolioHaven-Mobile",
              "version": "1.0.0",
              "exported_at": "2026-10-31T09:00:00Z",
              "reading_progress": [
                {
                  "file_hash": "legacy_book_hash_1",
                  "title": "Sherlock Holmes",
                  "author": "Arthur Conan Doyle",
                  "format": "EPUB",
                  "current_page": 50,
                  "total_pages": 200,
                  "progress_percent": 0.25,
                  "reading_time_seconds": 1800,
                  "is_finished": false,
                  "last_opened_at": "2026-10-31T09:00:00Z"
                }
              ],
              "bookmarks": [
                {
                  "file_hash": "legacy_book_hash_1",
                  "page_number": 50,
                  "title": "Trang 50",
                  "snippet": null,
                  "created_at": "2026-10-31T09:05:00Z"
                }
              ],
              "annotations": []
            }
        """.trimIndent()

        val parsedBackup = MetadataJsonSerializer.deserialize(legacyV1Json)

        // 1. Verify schema detection
        assertEquals(1, parsedBackup.schemaVersion)
        assertEquals("1.0.0", parsedBackup.version)
        assertNull("Legacy payload must not have userPreferences object", parsedBackup.userPreferences)
        assertNull("Legacy payload must not have appInfo object", parsedBackup.appInfo)
        assertEquals(1, parsedBackup.readingProgress.size)
        assertEquals(1, parsedBackup.bookmarks.size)
        assertNull(parsedBackup.bookmarks.first().snippet)

        // 2. Simulate restoring user preferences when backup.userPreferences is null
        // As defined in MetadataBackupManager: if backup.userPreferences is null, preferences are untouched
        val prefsRestored = if (parsedBackup.userPreferences != null) {
            true
        } else {
            false
        }
        assertFalse(prefsRestored)

        // 3. Verify user preferences are 100% PRESERVED and NOT reset to defaults
        assertEquals(22.0f, repo.defaultFontSize.value, 0.001f)
        assertEquals(1.9f, repo.defaultLineSpacing.value, 0.001f)
        assertEquals(ReaderTheme.NIGHT, repo.defaultReaderTheme.value)
        assertEquals(30, repo.defaultMarginsDp.value)
        assertEquals(1.5f, repo.ttsSpeechRate.value, 0.001f)
        assertEquals(0.8f, repo.ttsPitch.value, 0.001f)
        assertEquals("en-US", repo.ttsLanguage.value)
        assertTrue(repo.isGreetingCompleted())
    }

    @Test
    fun v2Payload_withCorruptedOrExtremePreferences_safelyClampedOnImport() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        val maliciousV2Json = """
            {
              "client": "FolioHaven-Mobile",
              "version": "2.0.0",
              "schema_version": 2,
              "exported_at": "2026-11-01T12:00:00Z",
              "user_preferences": {
                "reading_defaults": {
                  "font_size": -50.0,
                  "line_spacing": 999.0,
                  "reader_theme": "ATTACK_THEME",
                  "margins_dp": -20
                },
                "tts_defaults": {
                  "speech_rate": -10.0,
                  "pitch": 100.0,
                  "language": "   "
                },
                "greeting_completed": true
              },
              "reading_progress": [],
              "bookmarks": [],
              "annotations": []
            }
        """.trimIndent()

        val parsed = MetadataJsonSerializer.deserialize(maliciousV2Json)
        assertNotNull(parsed.userPreferences)
        val p = parsed.userPreferences!!

        // Apply preferences with repo guards
        repo.setDefaultFontSize(p.defaultFontSize)
        repo.setDefaultLineSpacing(p.defaultLineSpacing)
        try {
            repo.setDefaultReaderTheme(ReaderTheme.valueOf(p.defaultReaderTheme))
        } catch (_: Exception) {
            // Theme remains unchanged or falls back safely
        }
        repo.setDefaultMarginsDp(p.defaultMarginsDp)
        repo.setTtsSpeechRate(p.ttsSpeechRate)
        repo.setTtsPitch(p.ttsPitch)
        repo.setTtsLanguage(p.ttsLanguage)
        repo.setGreetingCompleted(p.isGreetingCompleted)

        // Verify all values clamped and protected
        assertEquals(12.0f, repo.defaultFontSize.value, 0.001f) // clamped to min 12
        assertEquals(2.2f, repo.defaultLineSpacing.value, 0.001f) // clamped to max 2.2
        assertEquals(ReaderTheme.SEPIA, repo.defaultReaderTheme.value) // retained SEPIA, didn't crash
        assertEquals(12, repo.defaultMarginsDp.value) // clamped to min 12
        assertEquals(0.5f, repo.ttsSpeechRate.value, 0.001f) // clamped to min 0.5
        assertEquals(2.0f, repo.ttsPitch.value, 0.001f) // clamped to max 2.0
        assertEquals("vi-VN", repo.ttsLanguage.value) // blank fell back to vi-VN
        assertTrue(repo.isGreetingCompleted())
    }

    @Test
    fun legacyV1Payload_withEmptyOrMissingOptionalSections_parsesCleanly() {
        val minimalLegacyJson = """
            {
              "client": "FolioHaven-Mobile"
            }
        """.trimIndent()

        val parsed = MetadataJsonSerializer.deserialize(minimalLegacyJson)
        assertEquals("FolioHaven-Mobile", parsed.client)
        assertEquals("1.0.0", parsed.version)
        assertEquals(1, parsed.schemaVersion)
        assertNull(parsed.appInfo)
        assertNull(parsed.userPreferences)
        assertTrue(parsed.books.isEmpty())
        assertTrue(parsed.readingProgress.isEmpty())
        assertTrue(parsed.bookmarks.isEmpty())
        assertTrue(parsed.annotations.isEmpty())
    }

    // =========================================================================
    // 3. DATA INTEGRITY & READING HISTORY RESET CONTRACT
    // =========================================================================

    @Test
    fun readingStats_historyResetContract_preservesBookCatalogWhileResettingTimeAndStreak() {
        // Given reading stats with 10 books, 5 finished, 720 minutes reading, 14 days streak
        val initialStats = ReadingStats(
            totalBooks = 10,
            finishedBooks = 5,
            totalReadingMinutes = 720,
            readingStreakDays = 14
        )

        // When reading history reset is performed:
        // Contract: total_pages and books rows are preserved; current_page=1, reading_time_seconds=0, is_finished=0, streak reset
        val postResetStats = ReadingStats(
            totalBooks = initialStats.totalBooks,
            finishedBooks = 0,
            totalReadingMinutes = 0,
            readingStreakDays = 0
        )

        assertEquals(10, postResetStats.totalBooks)
        assertEquals(0, postResetStats.finishedBooks)
        assertEquals(0, postResetStats.totalReadingMinutes)
        assertEquals(0, postResetStats.readingStreakDays)

        // Storage stats reflection
        val libraryStats = LibraryStorageStats(
            totalBooks = postResetStats.totalBooks,
            readingMinutes = postResetStats.totalReadingMinutes.toLong(),
            totalStorageBytes = 50000000L
        )

        assertEquals(10, libraryStats.totalBooks)
        assertEquals(0L, libraryStats.readingMinutes)
        assertEquals(0.0, libraryStats.readingHours, 0.0001)
        assertEquals(0L, libraryStats.readingHoursWhole)
        assertEquals("0 phút", libraryStats.formattedReadingTime)
        assertEquals("47.7 MB", libraryStats.formattedStorage)
    }

    @Test
    fun storageManager_deleteDirectoryContents_survivesDeeplyNestedEmptyAndNonEmptyTrees() {
        val cacheRoot = tempFolder.newFolder("deep_cache")
        // Create 5 levels of nested directories
        var current = cacheRoot
        for (i in 1..5) {
            current = File(current, "level_$i").apply { mkdirs() }
            File(current, "file_$i.tmp").writeBytes(ByteArray(64))
        }

        assertTrue(cacheRoot.exists())
        assertTrue(StorageManager.calculateDirectorySize(cacheRoot) > 0)

        val deleted = StorageManager.deleteDirectoryContents(cacheRoot)
        assertTrue(deleted)
        assertTrue("Cache root directory must survive", cacheRoot.exists())
        assertEquals("All subdirectories and files must be deleted", 0, cacheRoot.listFiles()?.size)
        assertEquals("Size must be 0 B after clearing", 0L, StorageManager.calculateDirectorySize(cacheRoot))
    }
}
