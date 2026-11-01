package com.example.foliohaven.data

import android.content.SharedPreferences
import com.example.foliohaven.data.model.ReaderTheme
import com.example.foliohaven.data.repository.UserPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Empirical Stress Verification Test Suite for Milestone M1 (Data & Domain Layer).
 * Stress tests:
 * 1. UserPreferencesRepository (concurrency, extreme inputs, multiple simulated restarts)
 * 2. StorageManager (deeply nested directories, empty folders, large arithmetic)
 * 3. MetadataBackupManager (corrupted JSON, truncated JSON, unexpected types)
 */
class M1EmpiricalStressTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    // =========================================================================
    // 1. UserPreferencesRepository Stress Tests
    // =========================================================================

    @Test
    fun userPrefs_extremeNumericInputs_clampWithinSafeBounds() {
        val prefs = ThreadSafeFakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = prefs)

        // Extremely high inputs
        repo.setDefaultFontSize(1000.0f)
        assertEquals(UserPreferencesRepository.MAX_FONT_SIZE, repo.defaultFontSize.value, 0.001f)
        assertEquals(UserPreferencesRepository.MAX_FONT_SIZE, repo.getDefaultFontSize(), 0.001f)

        repo.setDefaultLineSpacing(500.0f)
        assertEquals(UserPreferencesRepository.MAX_LINE_SPACING, repo.defaultLineSpacing.value, 0.001f)

        repo.setDefaultMarginsDp(10000)
        assertEquals(UserPreferencesRepository.MAX_MARGINS, repo.defaultMarginsDp.value)

        repo.setTtsSpeechRate(999.0f)
        assertEquals(UserPreferencesRepository.MAX_TTS_SPEECH_RATE, repo.ttsSpeechRate.value, 0.001f)

        repo.setTtsPitch(999.0f)
        assertEquals(UserPreferencesRepository.MAX_TTS_PITCH, repo.ttsPitch.value, 0.001f)

        // Extremely low / negative inputs
        repo.setDefaultFontSize(-500.0f)
        assertEquals(UserPreferencesRepository.MIN_FONT_SIZE, repo.defaultFontSize.value, 0.001f)

        repo.setDefaultLineSpacing(-100.0f)
        assertEquals(UserPreferencesRepository.MIN_LINE_SPACING, repo.defaultLineSpacing.value, 0.001f)

        repo.setDefaultMarginsDp(-999)
        assertEquals(UserPreferencesRepository.MIN_MARGINS, repo.defaultMarginsDp.value)

        repo.setTtsSpeechRate(-50.0f)
        assertEquals(UserPreferencesRepository.MIN_TTS_SPEECH_RATE, repo.ttsSpeechRate.value, 0.001f)

        repo.setTtsPitch(-50.0f)
        assertEquals(UserPreferencesRepository.MIN_TTS_PITCH, repo.ttsPitch.value, 0.001f)

        // Positive and Negative Infinity
        repo.setDefaultFontSize(Float.POSITIVE_INFINITY)
        assertEquals(UserPreferencesRepository.MAX_FONT_SIZE, repo.defaultFontSize.value, 0.001f)

        repo.setDefaultFontSize(Float.NEGATIVE_INFINITY)
        assertEquals(UserPreferencesRepository.MIN_FONT_SIZE, repo.defaultFontSize.value, 0.001f)

        repo.setDefaultLineSpacing(Float.POSITIVE_INFINITY)
        assertEquals(UserPreferencesRepository.MAX_LINE_SPACING, repo.defaultLineSpacing.value, 0.001f)

        repo.setDefaultLineSpacing(Float.NEGATIVE_INFINITY)
        assertEquals(UserPreferencesRepository.MIN_LINE_SPACING, repo.defaultLineSpacing.value, 0.001f)

        repo.setTtsSpeechRate(Float.POSITIVE_INFINITY)
        assertEquals(UserPreferencesRepository.MAX_TTS_SPEECH_RATE, repo.ttsSpeechRate.value, 0.001f)

        repo.setTtsSpeechRate(Float.NEGATIVE_INFINITY)
        assertEquals(UserPreferencesRepository.MIN_TTS_SPEECH_RATE, repo.ttsSpeechRate.value, 0.001f)

        repo.setTtsPitch(Float.POSITIVE_INFINITY)
        assertEquals(UserPreferencesRepository.MAX_TTS_PITCH, repo.ttsPitch.value, 0.001f)

        repo.setTtsPitch(Float.NEGATIVE_INFINITY)
        assertEquals(UserPreferencesRepository.MIN_TTS_PITCH, repo.ttsPitch.value, 0.001f)
    }

    @Test
    fun userPrefs_nanHandling_safelySanitizedToDefaults() {
        val prefs = ThreadSafeFakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = prefs)

        // Float.NaN must be safely sanitized to defaults without bypassing clamping
        repo.setDefaultFontSize(Float.NaN)
        assertEquals(UserPreferencesRepository.DEFAULT_FONT_SIZE, repo.defaultFontSize.value, 0.001f)
        assertEquals(UserPreferencesRepository.DEFAULT_FONT_SIZE, repo.getDefaultFontSize(), 0.001f)
        assertFalse(repo.defaultFontSize.value.isNaN())

        repo.setDefaultLineSpacing(Float.NaN)
        assertEquals(UserPreferencesRepository.DEFAULT_LINE_SPACING, repo.defaultLineSpacing.value, 0.001f)
        assertEquals(UserPreferencesRepository.DEFAULT_LINE_SPACING, repo.getDefaultLineSpacing(), 0.001f)
        assertFalse(repo.defaultLineSpacing.value.isNaN())

        repo.setTtsSpeechRate(Float.NaN)
        assertEquals(UserPreferencesRepository.DEFAULT_TTS_SPEECH_RATE, repo.ttsSpeechRate.value, 0.001f)
        assertEquals(UserPreferencesRepository.DEFAULT_TTS_SPEECH_RATE, repo.getTtsSpeechRate(), 0.001f)
        assertFalse(repo.ttsSpeechRate.value.isNaN())

        repo.setTtsPitch(Float.NaN)
        assertEquals(UserPreferencesRepository.DEFAULT_TTS_PITCH, repo.ttsPitch.value, 0.001f)
        assertEquals(UserPreferencesRepository.DEFAULT_TTS_PITCH, repo.getTtsPitch(), 0.001f)
        assertFalse(repo.ttsPitch.value.isNaN())

        // Simulated app restart with Float.NaN in SharedPreferences falls back to default values
        prefs.edit().putFloat(UserPreferencesRepository.KEY_DEFAULT_FONT_SIZE, Float.NaN).apply()
        prefs.edit().putFloat(UserPreferencesRepository.KEY_DEFAULT_LINE_SPACING, Float.NaN).apply()
        prefs.edit().putFloat(UserPreferencesRepository.KEY_DEFAULT_TTS_SPEECH_RATE, Float.NaN).apply()
        prefs.edit().putFloat(UserPreferencesRepository.KEY_DEFAULT_TTS_PITCH, Float.NaN).apply()

        val restartedRepo = UserPreferencesRepository(injectedPrefs = prefs)
        assertEquals(UserPreferencesRepository.DEFAULT_FONT_SIZE, restartedRepo.defaultFontSize.value, 0.001f)
        assertEquals(UserPreferencesRepository.DEFAULT_LINE_SPACING, restartedRepo.defaultLineSpacing.value, 0.001f)
        assertEquals(UserPreferencesRepository.DEFAULT_TTS_SPEECH_RATE, restartedRepo.ttsSpeechRate.value, 0.001f)
        assertEquals(UserPreferencesRepository.DEFAULT_TTS_PITCH, restartedRepo.ttsPitch.value, 0.001f)
        assertFalse(restartedRepo.defaultFontSize.value.isNaN())
    }

    @Test
    fun userPrefs_extremeLanguageInputs_handledGracefully() {
        val prefs = ThreadSafeFakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = prefs)

        // Blank, whitespace, newlines, tabs
        repo.setTtsLanguage("")
        assertEquals("vi-VN", repo.ttsLanguage.value)

        repo.setTtsLanguage("    ")
        assertEquals("vi-VN", repo.ttsLanguage.value)

        repo.setTtsLanguage("\n\t\r  ")
        assertEquals("vi-VN", repo.ttsLanguage.value)

        // Surrounding whitespace trimmed
        repo.setTtsLanguage("   ja-JP   ")
        assertEquals("ja-JP", repo.ttsLanguage.value)

        // Huge string
        val hugeLang = "a".repeat(10_000)
        repo.setTtsLanguage(hugeLang)
        assertEquals(hugeLang, repo.ttsLanguage.value)
    }

    @Test
    fun userPrefs_rapidConcurrentUpdates_threadSafeAndConsistent() {
        val prefs = ThreadSafeFakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = prefs)

        val threadCount = 20
        val updatesPerThread = 50
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val errorCount = AtomicInteger(0)

        for (t in 0 until threadCount) {
            executor.submit {
                try {
                    for (i in 0 until updatesPerThread) {
                        val size = 12.0f + (i % 16)
                        val spacing = 1.2f + (i % 10) * 0.1f
                        val margins = 12 + (i % 24)
                        val rate = 0.5f + (i % 15) * 0.1f
                        val pitch = 0.5f + (i % 15) * 0.1f
                        val theme = ReaderTheme.values()[i % ReaderTheme.values().size]
                        val lang = if (i % 2 == 0) "en-US" else "vi-VN"

                        repo.setDefaultFontSize(size)
                        repo.setDefaultLineSpacing(spacing)
                        repo.setDefaultMarginsDp(margins)
                        repo.setTtsSpeechRate(rate)
                        repo.setTtsPitch(pitch)
                        repo.setDefaultReaderTheme(theme)
                        repo.setTtsLanguage(lang)
                        repo.setGreetingCompleted(i % 2 == 0)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    errorCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue("Concurrent updates must complete within 10s", latch.await(10, TimeUnit.SECONDS))
        executor.shutdown()
        assertEquals("No exceptions should occur during concurrent updates", 0, errorCount.get())

        // Ensure final StateFlow values match the underlying SharedPreferences
        assertEquals(prefs.getFloat(UserPreferencesRepository.KEY_DEFAULT_FONT_SIZE, 0f), repo.defaultFontSize.value, 0.001f)
        assertEquals(prefs.getFloat(UserPreferencesRepository.KEY_DEFAULT_LINE_SPACING, 0f), repo.defaultLineSpacing.value, 0.001f)
        assertEquals(prefs.getInt(UserPreferencesRepository.KEY_DEFAULT_MARGINS, 0), repo.defaultMarginsDp.value)
        assertEquals(prefs.getFloat(UserPreferencesRepository.KEY_DEFAULT_TTS_SPEECH_RATE, 0f), repo.ttsSpeechRate.value, 0.001f)
        assertEquals(prefs.getFloat(UserPreferencesRepository.KEY_DEFAULT_TTS_PITCH, 0f), repo.ttsPitch.value, 0.001f)
        assertEquals(prefs.getString(UserPreferencesRepository.KEY_DEFAULT_TTS_LANGUAGE, ""), repo.ttsLanguage.value)
    }

    @Test
    fun userPrefs_simulatedSequentialRestarts_preservesStateOverMultipleCycles() {
        val prefs = ThreadSafeFakeSharedPreferences()

        // Execute 25 sequential simulated app restart cycles
        for (cycle in 1..25) {
            val repo = UserPreferencesRepository(injectedPrefs = prefs)

            val targetFontSize = 12.0f + (cycle % 15)
            val targetLineSpacing = 1.2f + (cycle % 9) * 0.1f
            val targetTheme = ReaderTheme.values()[cycle % ReaderTheme.values().size]
            val targetMargins = 12 + (cycle % 20)
            val targetRate = 0.5f + (cycle % 14) * 0.1f
            val targetPitch = 0.5f + (cycle % 14) * 0.1f
            val targetLang = "lang-$cycle"
            val targetGreeting = (cycle % 2 == 0)

            repo.setDefaultFontSize(targetFontSize)
            repo.setDefaultLineSpacing(targetLineSpacing)
            repo.setDefaultReaderTheme(targetTheme)
            repo.setDefaultMarginsDp(targetMargins)
            repo.setTtsSpeechRate(targetRate)
            repo.setTtsPitch(targetPitch)
            repo.setTtsLanguage(targetLang)
            repo.setGreetingCompleted(targetGreeting)

            // Simulate immediate restart
            val restartedRepo = UserPreferencesRepository(injectedPrefs = prefs)
            assertEquals(targetFontSize, restartedRepo.defaultFontSize.value, 0.001f)
            assertEquals(targetLineSpacing, restartedRepo.defaultLineSpacing.value, 0.001f)
            assertEquals(targetTheme, restartedRepo.defaultReaderTheme.value)
            assertEquals(targetMargins, restartedRepo.defaultMarginsDp.value)
            assertEquals(targetRate, restartedRepo.ttsSpeechRate.value, 0.001f)
            assertEquals(targetPitch, restartedRepo.ttsPitch.value, 0.001f)
            assertEquals(targetLang, restartedRepo.ttsLanguage.value)
            assertEquals(targetGreeting, restartedRepo.isGreetingCompleted())
        }
    }

    // =========================================================================
    // 2. StorageManager Stress Tests
    // =========================================================================

    @Test
    fun storageManager_deeplyNestedDirectories_calculatedAndDeletedSafely() {
        val rootDir = tempFolder.newFolder("deep_root")
        var currentDir = rootDir
        val depth = 15
        var expectedBytes = 0L

        for (level in 1..depth) {
            currentDir = File(currentDir, "level_$level").apply { mkdir() }
            if (level % 2 == 0) {
                val f = File(currentDir, "data_$level.bin")
                val size = level * 10
                f.writeBytes(ByteArray(size))
                expectedBytes += size
            }
        }

        assertEquals(expectedBytes, StorageManager.calculateDirectorySize(rootDir))

        // Safely delete all contents
        val result = StorageManager.deleteDirectoryContents(rootDir)
        assertTrue(result)
        assertTrue("Root must still exist", rootDir.exists())
        assertEquals("Root must be empty", 0, rootDir.listFiles()?.size)
        assertEquals(0L, StorageManager.calculateDirectorySize(rootDir))
    }

    @Test
    fun storageManager_nestedEmptyDirectories_handledSafely() {
        val rootDir = tempFolder.newFolder("empty_tree_root")
        var currentDir = rootDir
        for (i in 1..20) {
            currentDir = File(currentDir, "empty_$i").apply { mkdir() }
        }

        assertEquals(0L, StorageManager.calculateDirectorySize(rootDir))
        val result = StorageManager.deleteDirectoryContents(rootDir)
        assertTrue(result)
        assertTrue(rootDir.exists())
        assertEquals(0, rootDir.listFiles()?.size)
    }

    @Test
    fun storageManager_largeByteSizingArithmetic_doesNotOverflowOrCrash() {
        assertEquals("0 B", StorageManager.formatStorageSize(0L))
        assertEquals("0 B", StorageManager.formatStorageSize(-1L))
        assertEquals("0 B", StorageManager.formatStorageSize(-999999999L))

        // 1 TB (1024 GB)
        val oneTb = 1024L * 1024L * 1024L * 1024L
        assertEquals("1024.0 GB", StorageManager.formatStorageSize(oneTb))

        // 10 TB
        val tenTb = 10L * oneTb
        assertEquals("10240.0 GB", StorageManager.formatStorageSize(tenTb))

        // Long.MAX_VALUE
        val maxFormatted = StorageManager.formatStorageSize(Long.MAX_VALUE)
        assertNotNull(maxFormatted)
        assertTrue("Should contain GB", maxFormatted.endsWith("GB"))

        // Large reading minutes arithmetic in LibraryStorageStats
        val hugeStats = LibraryStorageStats(
            totalBooks = 100_000,
            readingMinutes = 600_000L, // 10,000 hours
            totalStorageBytes = 500L * 1024L * 1024L * 1024L // 500 GB
        )
        assertEquals(10_000.0, hugeStats.readingHours, 0.001)
        assertEquals(10_000L, hugeStats.readingHoursWhole)
        assertEquals("500.0 GB", hugeStats.formattedStorage)
        assertEquals("10000 giờ", hugeStats.formattedReadingTime)

        // Negative reading minutes edge case
        val negativeStats = LibraryStorageStats(
            totalBooks = 0,
            readingMinutes = -15L,
            totalStorageBytes = 0L
        )
        assertEquals(0.0, negativeStats.readingHours, 0.001)
        assertEquals(0L, negativeStats.readingHoursWhole)
    }

    // =========================================================================
    // 3. MetadataBackupManager Stress Tests
    // =========================================================================

    @Test
    fun metadataBackup_corruptedJsonStrings_throwIllegalArgumentException() {
        val corruptedStrings = listOf(
            "not json at all",
            "{{{ broken",
            "[1, 2, 3]",
            "\"just a string\"",
            "42",
            "true",
            "null",
            "{ \"client\": }",
            "{ \"library\": { \"books\": [ { incomplete"
        )

        for (corrupted in corruptedStrings) {
            try {
                MetadataJsonSerializer.deserialize(corrupted)
                fail("Should fail to parse corrupted string: $corrupted")
            } catch (e: IllegalArgumentException) {
                // Expected
            }
        }
    }

    @Test
    fun metadataBackup_truncatedJson_throwsIllegalArgumentException() {
        val validJson = MetadataJsonSerializer.serialize(
            BackupMetadata(
                books = listOf(
                    BookMetadataBackup(
                        fileHash = "h1",
                        title = "Title 1",
                        author = "Author 1",
                        format = "EPUB"
                    )
                ),
                readingProgress = listOf(
                    ReadingProgressBackup(
                        fileHash = "h1",
                        title = "Title 1",
                        author = "Author 1",
                        format = "EPUB",
                        currentPage = 5,
                        totalPages = 100,
                        progressPercent = 0.05,
                        readingTimeSeconds = 300,
                        isFinished = false,
                        lastOpenedAt = "2026-11-01"
                    )
                )
            )
        )

        val truncationFractions = listOf(0.1, 0.25, 0.5, 0.75, 0.9, 0.95, 0.99)
        for (fraction in truncationFractions) {
            val cutIndex = (validJson.length * fraction).toInt().coerceAtLeast(1)
            val truncated = validJson.substring(0, cutIndex)
            try {
                MetadataJsonSerializer.deserialize(truncated)
                fail("Should fail on truncated JSON of length $cutIndex / ${validJson.length}")
            } catch (e: IllegalArgumentException) {
                // Expected
            }
        }
    }

    @Test
    fun metadataBackup_unexpectedDataTypesInFields_handlesGracefullyWithDefaults() {
        val weirdTypesJson = """
            {
              "client": 12345,
              "version": true,
              "schema_version": "not_an_int",
              "app_info": "should be an object but is string",
              "user_preferences": {
                "reading_defaults": {
                  "font_size": "twenty",
                  "line_spacing": null,
                  "reader_theme": "INVALID_THEME_NAME",
                  "margins_dp": "thirty"
                },
                "tts_defaults": {
                  "speech_rate": "fast",
                  "pitch": null,
                  "language": 99999
                },
                "greeting_completed": "yes"
              },
              "library": "should be an object",
              "books": "should be an array",
              "reading_progress": [
                "not an object",
                { "missing_file_hash": true },
                { "file_hash": "valid_hash", "current_page": "ten", "progress_percent": "zero" }
              ],
              "bookmarks": [
                42,
                { "file_hash": "", "page_number": 1 },
                { "file_hash": "bm_hash", "page_number": "five" }
              ],
              "annotations": [
                null,
                { "file_hash": "an_hash", "page_number": 2, "color_id": "red" }
              ]
            }
        """.trimIndent()

        val backup = MetadataJsonSerializer.deserialize(weirdTypesJson)
        assertNotNull(backup)
        // app_info was invalid type -> should be null
        assertNull(backup.appInfo)

        // user preferences had invalid types -> should safely use defaults
        assertNotNull(backup.userPreferences)
        val prefs = backup.userPreferences!!
        assertEquals(16.0f, prefs.defaultFontSize, 0.001f)
        assertEquals(1.6f, prefs.defaultLineSpacing, 0.001f)
        assertEquals("INVALID_THEME_NAME", prefs.defaultReaderTheme)
        assertEquals(24, prefs.defaultMarginsDp)
        assertEquals(1.0f, prefs.ttsSpeechRate, 0.001f)
        assertEquals(1.0f, prefs.ttsPitch, 0.001f)

        // reading progress skips corrupted entries, keeps valid one with defaults
        assertEquals(1, backup.readingProgress.size)
        assertEquals("valid_hash", backup.readingProgress.first().fileHash)
        assertEquals(1, backup.readingProgress.first().currentPage) // default fallback

        // bookmarks skips corrupted entry, keeps valid hash
        assertEquals(1, backup.bookmarks.size)
        assertEquals("bm_hash", backup.bookmarks.first().fileHash)

        // annotations keeps valid hash
        assertEquals(1, backup.annotations.size)
        assertEquals("an_hash", backup.annotations.first().fileHash)
    }
}

/**
 * Thread-safe implementation of SharedPreferences for concurrency stress testing.
 */
class ThreadSafeFakeSharedPreferences : SharedPreferences {
    private val storage = ConcurrentHashMap<String, Any>()

    override fun getAll(): MutableMap<String, *> = HashMap(storage)

    override fun getString(key: String?, defValue: String?): String? {
        val v = if (key != null) storage[key] else null
        return v as? String ?: defValue
    }

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        val v = if (key != null) storage[key] else null
        return (v as? Set<String>)?.toMutableSet() ?: defValues
    }

    override fun getInt(key: String?, defValue: Int): Int {
        val v = if (key != null) storage[key] else null
        return (v as? Number)?.toInt() ?: defValue
    }

    override fun getLong(key: String?, defValue: Long): Long {
        val v = if (key != null) storage[key] else null
        return (v as? Number)?.toLong() ?: defValue
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        val v = if (key != null) storage[key] else null
        return (v as? Number)?.toFloat() ?: defValue
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        val v = if (key != null) storage[key] else null
        return (v as? Boolean) ?: defValue
    }

    override fun contains(key: String?): Boolean = key != null && storage.containsKey(key)

    override fun edit(): SharedPreferences.Editor = ThreadSafeEditor(storage)

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    class ThreadSafeEditor(private val storage: ConcurrentHashMap<String, Any>) : SharedPreferences.Editor {
        private val delta = ConcurrentHashMap<String, Any?>()

        override fun putString(key: String?, value: String?): SharedPreferences.Editor = apply {
            if (key != null) {
                if (value != null) delta[key] = value else delta[key] = REMOVED_MARKER
            }
        }

        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = apply {
            if (key != null) {
                if (values != null) delta[key] = values else delta[key] = REMOVED_MARKER
            }
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor = apply {
            if (key != null) delta[key] = value
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = apply {
            if (key != null) delta[key] = value
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = apply {
            if (key != null) delta[key] = value
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = apply {
            if (key != null) delta[key] = value
        }

        override fun remove(key: String?): SharedPreferences.Editor = apply {
            if (key != null) delta[key] = REMOVED_MARKER
        }

        override fun clear(): SharedPreferences.Editor = apply {
            delta[CLEAR_MARKER] = CLEAR_MARKER
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        @Synchronized
        override fun apply() {
            if (delta.containsKey(CLEAR_MARKER)) {
                storage.clear()
            }
            for ((k, v) in delta) {
                if (k == CLEAR_MARKER) continue
                if (v === REMOVED_MARKER) {
                    storage.remove(k)
                } else if (v != null) {
                    storage[k] = v
                }
            }
            delta.clear()
        }

        companion object {
            private val REMOVED_MARKER = Any()
            private const val CLEAR_MARKER = "__CLEAR_ALL__"
        }
    }
}
