package com.example.foliohaven.data

import com.example.foliohaven.data.repository.FakeSharedPreferences
import com.example.foliohaven.data.repository.UserPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class MetadataBackupTest {

    private fun sampleBackup(): BackupMetadata {
        return BackupMetadata(
            client = "FolioHaven-Mobile",
            version = "2.0.0",
            schemaVersion = 2,
            exportedAt = "2026-11-01T12:00:00Z",
            appInfo = BackupAppInfo(
                appName = "FolioHaven Mobile",
                versionName = "1.0",
                buildNumber = 1,
                license = "MIT License"
            ),
            userPreferences = UserPreferencesBackup(
                defaultFontSize = 18.0f,
                defaultLineSpacing = 1.8f,
                defaultReaderTheme = "DARK_SEPIA",
                defaultMarginsDp = 32,
                ttsSpeechRate = 1.2f,
                ttsPitch = 0.9f,
                ttsLanguage = "en-US",
                isGreetingCompleted = true
            ),
            books = listOf(
                BookMetadataBackup(
                    fileHash = "hash123456",
                    title = "Cuộc phiêu lưu của Sherlock Holmes",
                    author = "Arthur Conan Doyle",
                    description = "Tập truyện ngắn trinh thám.",
                    format = "EPUB",
                    fileSize = 512000L,
                    category = "Trinh thám",
                    gradientStart = -14798789L,
                    gradientEnd = -15784150L,
                    createdAt = "2026-10-31T08:00:00Z"
                )
            ),
            readingProgress = listOf(
                ReadingProgressBackup(
                    fileHash = "hash123456",
                    title = "Cuộc phiêu lưu của Sherlock Holmes",
                    author = "Arthur Conan Doyle",
                    format = "EPUB",
                    currentPage = 25,
                    totalPages = 200,
                    progressPercent = 0.125,
                    readingTimeSeconds = 2400,
                    isFinished = false,
                    lastOpenedAt = "2026-10-31T10:00:00Z"
                )
            ),
            bookmarks = listOf(
                BookmarkBackup(
                    fileHash = "hash123456",
                    pageNumber = 25,
                    title = "Trang 25",
                    snippet = "Vụ án dải băng lốm đốm",
                    createdAt = "2026-10-31T10:05:00Z"
                )
            ),
            annotations = listOf(
                AnnotationBackup(
                    fileHash = "hash123456",
                    pageNumber = 25,
                    selectedText = "Sự thật thường kỳ lạ hơn tưởng tượng",
                    noteText = "Châm ngôn kinh điển",
                    colorId = 2,
                    createdAt = "2026-10-31T10:10:00Z"
                )
            )
        )
    }

    @Test
    fun serialize_generatesValidJsonWithAllRequiredSections() {
        val backup = sampleBackup()
        val json = MetadataJsonSerializer.serialize(backup)

        assertNotNull(json)
        assertTrue(json.isNotBlank())
        assertTrue("Must contain client", json.contains("\"client\": \"FolioHaven-Mobile\""))
        assertTrue("Must contain schema version", json.contains("\"schema_version\": 2"))
        assertTrue("Must contain app_info", json.contains("\"app_info\""))
        assertTrue("Must contain user_preferences", json.contains("\"user_preferences\""))
        assertTrue("Must contain reading_defaults", json.contains("\"reading_defaults\""))
        assertTrue("Must contain tts_defaults", json.contains("\"tts_defaults\""))
        assertTrue("Must contain library", json.contains("\"library\""))
        assertTrue("Must contain reading_progress", json.contains("\"reading_progress\""))
        assertTrue("Must contain bookmarks", json.contains("\"bookmarks\""))
        assertTrue("Must contain annotations", json.contains("\"annotations\""))
        assertTrue("Must contain serialized book title", json.contains("Cuộc phiêu lưu của Sherlock Holmes"))
    }

    @Test
    fun roundTrip_preservesAllMetadataFieldsAccurately() {
        val original = sampleBackup()
        val json = MetadataJsonSerializer.serialize(original)
        val deserialized = MetadataJsonSerializer.deserialize(json)

        assertEquals(original.client, deserialized.client)
        assertEquals(original.version, deserialized.version)
        assertEquals(original.schemaVersion, deserialized.schemaVersion)
        assertEquals(original.exportedAt, deserialized.exportedAt)

        // App Info
        assertNotNull(deserialized.appInfo)
        assertEquals(original.appInfo?.appName, deserialized.appInfo?.appName)
        assertEquals(original.appInfo?.versionName, deserialized.appInfo?.versionName)
        assertEquals(original.appInfo?.buildNumber, deserialized.appInfo?.buildNumber)
        assertEquals(original.appInfo?.license, deserialized.appInfo?.license)

        // Preferences
        assertNotNull(deserialized.userPreferences)
        val prefs = deserialized.userPreferences!!
        assertEquals(18.0f, prefs.defaultFontSize, 0.01f)
        assertEquals(1.8f, prefs.defaultLineSpacing, 0.01f)
        assertEquals("DARK_SEPIA", prefs.defaultReaderTheme)
        assertEquals(32, prefs.defaultMarginsDp)
        assertEquals(1.2f, prefs.ttsSpeechRate, 0.01f)
        assertEquals(0.9f, prefs.ttsPitch, 0.01f)
        assertEquals("en-US", prefs.ttsLanguage)
        assertTrue(prefs.isGreetingCompleted)

        // Books
        assertEquals(1, deserialized.books.size)
        val book = deserialized.books.first()
        assertEquals("hash123456", book.fileHash)
        assertEquals("Cuộc phiêu lưu của Sherlock Holmes", book.title)
        assertEquals("Arthur Conan Doyle", book.author)
        assertEquals("EPUB", book.format)
        assertEquals(512000L, book.fileSize)
        assertEquals("Trinh thám", book.category)

        // Reading Progress
        assertEquals(1, deserialized.readingProgress.size)
        val progress = deserialized.readingProgress.first()
        assertEquals("hash123456", progress.fileHash)
        assertEquals(25, progress.currentPage)
        assertEquals(200, progress.totalPages)
        assertEquals(0.125, progress.progressPercent, 0.001)
        assertEquals(2400, progress.readingTimeSeconds)
        assertEquals(false, progress.isFinished)

        // Bookmarks
        assertEquals(1, deserialized.bookmarks.size)
        val bookmark = deserialized.bookmarks.first()
        assertEquals("hash123456", bookmark.fileHash)
        assertEquals(25, bookmark.pageNumber)
        assertEquals("Vụ án dải băng lốm đốm", bookmark.snippet)

        // Annotations
        assertEquals(1, deserialized.annotations.size)
        val annotation = deserialized.annotations.first()
        assertEquals("hash123456", annotation.fileHash)
        assertEquals(25, annotation.pageNumber)
        assertEquals("Sự thật thường kỳ lạ hơn tưởng tượng", annotation.selectedText)
        assertEquals("Châm ngôn kinh điển", annotation.noteText)
        assertEquals(2, annotation.colorId)
    }

    @Test
    fun deserialize_parsesLegacyV1JsonWithoutCrashing() {
        val legacyJson = """
            {
              "client": "FolioHaven-Mobile",
              "version": "1.0.0",
              "exported_at": "2026-10-31T12:00:00Z",
              "reading_progress": [
                {
                  "file_hash": "legacy_hash",
                  "title": "Legacy Book",
                  "author": "Legacy Author",
                  "format": "PDF",
                  "current_page": 5,
                  "total_pages": 50,
                  "progress_percent": 0.1,
                  "reading_time_seconds": 600,
                  "is_finished": false,
                  "last_opened_at": "2026-10-31T12:00:00Z"
                }
              ],
              "bookmarks": [],
              "annotations": []
            }
        """.trimIndent()

        val parsed = MetadataJsonSerializer.deserialize(legacyJson)
        assertEquals("FolioHaven-Mobile", parsed.client)
        assertEquals("1.0.0", parsed.version)
        assertEquals(1, parsed.schemaVersion)
        assertNull("Legacy JSON has no preferences", parsed.userPreferences)
        assertTrue("Legacy JSON has no separate books catalog", parsed.books.isEmpty())
        assertEquals(1, parsed.readingProgress.size)
        assertEquals("legacy_hash", parsed.readingProgress.first().fileHash)
    }

    @Test
    fun deserialize_throwsOnEmptyOrBlankJson() {
        try {
            MetadataJsonSerializer.deserialize("")
            fail("Should throw IllegalArgumentException on empty string")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Empty or blank"))
        }

        try {
            MetadataJsonSerializer.deserialize("   \n\t  ")
            fail("Should throw IllegalArgumentException on blank string")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Empty or blank"))
        }
    }

    @Test
    fun deserialize_throwsOnMalformedJson() {
        try {
            MetadataJsonSerializer.deserialize("{ unclosed: true, broken... ")
            fail("Should throw IllegalArgumentException on malformed syntax")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Malformed JSON"))
        }
    }

    @Test
    fun deserialize_skipsCorruptedArrayItemsGracefully() {
        val corruptedJson = """
            {
              "client": "FolioHaven-Mobile",
              "version": "2.0.0",
              "schema_version": 2,
              "exported_at": "2026-11-01T12:00:00Z",
              "library": {
                "books": [
                  { "title": "Missing File Hash Book", "author": "Anonymous" },
                  { "file_hash": "valid_hash", "title": "Valid Book", "author": "Valid Author", "format": "EPUB" }
                ]
              },
              "reading_progress": [],
              "bookmarks": [],
              "annotations": []
            }
        """.trimIndent()

        val parsed = MetadataJsonSerializer.deserialize(corruptedJson)
        assertEquals(1, parsed.books.size)
        assertEquals("valid_hash", parsed.books.first().fileHash)
    }
}
