package com.example.foliohaven.data

import com.example.foliohaven.data.db.FolioHavenDbHelper
import com.example.foliohaven.data.model.ReaderTheme
import com.example.foliohaven.data.repository.UserPreferencesRepository
import com.example.foliohaven.model.AppInfo
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Domain model representing the full v2.0 offline metadata backup schema for FolioHaven Mobile.
 */
data class BackupMetadata(
    val client: String = "FolioHaven-Mobile",
    val version: String = "2.0.0",
    val schemaVersion: Int = 2,
    val exportedAt: String = "",
    val appInfo: BackupAppInfo? = null,
    val userPreferences: UserPreferencesBackup? = null,
    val books: List<BookMetadataBackup> = emptyList(),
    val readingProgress: List<ReadingProgressBackup> = emptyList(),
    val bookmarks: List<BookmarkBackup> = emptyList(),
    val annotations: List<AnnotationBackup> = emptyList()
)

data class BackupAppInfo(
    val appName: String = AppInfo.APP_NAME,
    val versionName: String = AppInfo.VERSION_NAME,
    val buildNumber: Int = AppInfo.BUILD_NUMBER,
    val license: String = AppInfo.LICENSE
)

data class UserPreferencesBackup(
    val defaultFontSize: Float = 16.0f,
    val defaultLineSpacing: Float = 1.6f,
    val defaultReaderTheme: String = "SEPIA",
    val defaultMarginsDp: Int = 24,
    val ttsSpeechRate: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val ttsLanguage: String = "vi-VN",
    val isGreetingCompleted: Boolean = false
)

data class BookMetadataBackup(
    val fileHash: String,
    val title: String,
    val author: String,
    val description: String? = null,
    val format: String,
    val fileSize: Long = 0L,
    val category: String = "Tất cả sách",
    val gradientStart: Long = -14798789L,
    val gradientEnd: Long = -15784150L,
    val createdAt: String = ""
)

data class ReadingProgressBackup(
    val fileHash: String,
    val title: String,
    val author: String,
    val format: String,
    val currentPage: Int,
    val totalPages: Int,
    val progressPercent: Double,
    val readingTimeSeconds: Int,
    val isFinished: Boolean,
    val lastOpenedAt: String
)

data class BookmarkBackup(
    val fileHash: String,
    val pageNumber: Int,
    val title: String,
    val snippet: String? = null,
    val createdAt: String
)

data class AnnotationBackup(
    val fileHash: String,
    val pageNumber: Int,
    val selectedText: String,
    val noteText: String? = null,
    val colorId: Int = 1,
    val createdAt: String
)

data class ImportBackupResult(
    val bookmarksRestored: Int,
    val annotationsRestored: Int,
    val readingProgressRestored: Int = 0,
    val preferencesRestored: Boolean = false
)

/**
 * Pure JSON serializer/deserializer handling v2.0 schema and v1.0 backwards compatibility.
 */
object MetadataJsonSerializer {
    private fun currentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        return sdf.format(Date())
    }

    fun serialize(backup: BackupMetadata): String {
        val root = JSONObject()
        root.put("client", backup.client)
        root.put("version", backup.version)
        root.put("schema_version", backup.schemaVersion)
        root.put("exported_at", backup.exportedAt.ifBlank { currentTimestamp() })

        // 1. App Info
        backup.appInfo?.let { info ->
            val appObj = JSONObject().apply {
                put("app_name", info.appName)
                put("version_name", info.versionName)
                put("build_number", info.buildNumber)
                put("license", info.license)
            }
            root.put("app_info", appObj)
        }

        // 2. User Preferences
        backup.userPreferences?.let { prefs ->
            val prefsObj = JSONObject().apply {
                val readingObj = JSONObject().apply {
                    put("font_size", prefs.defaultFontSize.toDouble())
                    put("line_spacing", prefs.defaultLineSpacing.toDouble())
                    put("reader_theme", prefs.defaultReaderTheme)
                    put("margins_dp", prefs.defaultMarginsDp)
                }
                put("reading_defaults", readingObj)

                val ttsObj = JSONObject().apply {
                    put("speech_rate", prefs.ttsSpeechRate.toDouble())
                    put("pitch", prefs.ttsPitch.toDouble())
                    put("language", prefs.ttsLanguage)
                }
                put("tts_defaults", ttsObj)

                put("greeting_completed", prefs.isGreetingCompleted)
            }
            root.put("user_preferences", prefsObj)
        }

        // 3. Library Books
        val booksArray = JSONArray()
        for (b in backup.books) {
            val bObj = JSONObject().apply {
                put("file_hash", b.fileHash)
                put("title", b.title)
                put("author", b.author)
                b.description?.let { put("description", it) }
                put("format", b.format)
                put("file_size", b.fileSize)
                put("category", b.category)
                put("gradient_start", b.gradientStart)
                put("gradient_end", b.gradientEnd)
                put("created_at", b.createdAt)
            }
            booksArray.put(bObj)
        }
        val libraryObj = JSONObject().apply {
            put("total_books", backup.books.size)
            put("books", booksArray)
        }
        root.put("library", libraryObj)

        // 4. Reading Progress
        val progressArray = JSONArray()
        for (rp in backup.readingProgress) {
            val rpObj = JSONObject().apply {
                put("file_hash", rp.fileHash)
                put("title", rp.title)
                put("author", rp.author)
                put("format", rp.format)
                put("current_page", rp.currentPage)
                put("total_pages", rp.totalPages)
                put("progress_percent", rp.progressPercent)
                put("reading_time_seconds", rp.readingTimeSeconds)
                put("is_finished", rp.isFinished)
                put("last_opened_at", rp.lastOpenedAt)
            }
            progressArray.put(rpObj)
        }
        root.put("reading_progress", progressArray)

        // 5. Bookmarks
        val bmArray = JSONArray()
        for (bm in backup.bookmarks) {
            val bmObj = JSONObject().apply {
                put("file_hash", bm.fileHash)
                put("page_number", bm.pageNumber)
                put("title", bm.title)
                bm.snippet?.let { put("snippet", it) }
                put("created_at", bm.createdAt)
            }
            bmArray.put(bmObj)
        }
        root.put("bookmarks", bmArray)

        // 6. Annotations
        val anArray = JSONArray()
        for (an in backup.annotations) {
            val anObj = JSONObject().apply {
                put("file_hash", an.fileHash)
                put("page_number", an.pageNumber)
                put("selected_text", an.selectedText)
                an.noteText?.let { put("note_text", it) }
                put("color_id", an.colorId)
                put("created_at", an.createdAt)
            }
            anArray.put(anObj)
        }
        root.put("annotations", anArray)

        return root.toString(2)
    }

    fun deserialize(jsonString: String): BackupMetadata {
        if (jsonString.isBlank()) {
            throw IllegalArgumentException("Empty or blank metadata JSON string")
        }

        val root = try {
            JSONObject(jsonString)
        } catch (e: Exception) {
            throw IllegalArgumentException("Malformed JSON string: ${e.message}", e)
        }

        val client = root.optString("client", "FolioHaven-Mobile")
        val version = root.optString("version", "1.0.0")
        val schemaVersion = root.optInt("schema_version", 1)
        val exportedAt = root.optString("exported_at", "")

        val appInfo = if (root.has("app_info")) {
            val obj = root.optJSONObject("app_info")
            if (obj != null) {
                BackupAppInfo(
                    appName = obj.optString("app_name", AppInfo.APP_NAME),
                    versionName = obj.optString("version_name", AppInfo.VERSION_NAME),
                    buildNumber = obj.optInt("build_number", AppInfo.BUILD_NUMBER),
                    license = obj.optString("license", AppInfo.LICENSE)
                )
            } else null
        } else null

        val userPrefs = if (root.has("user_preferences")) {
            val prefsObj = root.optJSONObject("user_preferences")
            if (prefsObj != null) {
                val readingObj = prefsObj.optJSONObject("reading_defaults")
                val ttsObj = prefsObj.optJSONObject("tts_defaults")
                UserPreferencesBackup(
                    defaultFontSize = readingObj?.optDouble("font_size", 16.0)?.toFloat() ?: 16.0f,
                    defaultLineSpacing = readingObj?.optDouble("line_spacing", 1.6)?.toFloat() ?: 1.6f,
                    defaultReaderTheme = readingObj?.optString("reader_theme", "SEPIA") ?: "SEPIA",
                    defaultMarginsDp = readingObj?.optInt("margins_dp", 24) ?: 24,
                    ttsSpeechRate = ttsObj?.optDouble("speech_rate", 1.0)?.toFloat() ?: 1.0f,
                    ttsPitch = ttsObj?.optDouble("pitch", 1.0)?.toFloat() ?: 1.0f,
                    ttsLanguage = ttsObj?.optString("language", "vi-VN") ?: "vi-VN",
                    isGreetingCompleted = prefsObj.optBoolean("greeting_completed", false)
                )
            } else null
        } else null

        val booksList = mutableListOf<BookMetadataBackup>()
        val libraryObj = root.optJSONObject("library")
        val booksArray = libraryObj?.optJSONArray("books") ?: root.optJSONArray("books")
        if (booksArray != null) {
            for (i in 0 until booksArray.length()) {
                val obj = booksArray.optJSONObject(i) ?: continue
                val hash = obj.optString("file_hash")
                if (hash.isBlank()) continue
                booksList.add(
                    BookMetadataBackup(
                        fileHash = hash,
                        title = obj.optString("title", "Untitled"),
                        author = obj.optString("author", "Unknown"),
                        description = if (obj.isNull("description")) null else obj.optString("description"),
                        format = obj.optString("format", "TXT"),
                        fileSize = obj.optLong("file_size", 0L),
                        category = obj.optString("category", "Tất cả sách"),
                        gradientStart = obj.optLong("gradient_start", -14798789L),
                        gradientEnd = obj.optLong("gradient_end", -15784150L),
                        createdAt = obj.optString("created_at", "")
                    )
                )
            }
        }

        val progressList = mutableListOf<ReadingProgressBackup>()
        val progressArray = root.optJSONArray("reading_progress")
        if (progressArray != null) {
            for (i in 0 until progressArray.length()) {
                val obj = progressArray.optJSONObject(i) ?: continue
                val hash = obj.optString("file_hash")
                if (hash.isBlank()) continue
                progressList.add(
                    ReadingProgressBackup(
                        fileHash = hash,
                        title = obj.optString("title", ""),
                        author = obj.optString("author", ""),
                        format = obj.optString("format", ""),
                        currentPage = obj.optInt("current_page", 1),
                        totalPages = obj.optInt("total_pages", 1),
                        progressPercent = obj.optDouble("progress_percent", 0.0),
                        readingTimeSeconds = obj.optInt("reading_time_seconds", 0),
                        isFinished = obj.optBoolean("is_finished", false),
                        lastOpenedAt = obj.optString("last_opened_at", "")
                    )
                )
            }
        }

        val bookmarksList = mutableListOf<BookmarkBackup>()
        val bmArray = root.optJSONArray("bookmarks")
        if (bmArray != null) {
            for (i in 0 until bmArray.length()) {
                val obj = bmArray.optJSONObject(i) ?: continue
                val hash = obj.optString("file_hash")
                if (hash.isBlank()) continue
                bookmarksList.add(
                    BookmarkBackup(
                        fileHash = hash,
                        pageNumber = obj.optInt("page_number", 1),
                        title = obj.optString("title", ""),
                        snippet = if (obj.isNull("snippet")) null else obj.optString("snippet"),
                        createdAt = obj.optString("created_at", "")
                    )
                )
            }
        }

        val annotationsList = mutableListOf<AnnotationBackup>()
        val anArray = root.optJSONArray("annotations")
        if (anArray != null) {
            for (i in 0 until anArray.length()) {
                val obj = anArray.optJSONObject(i) ?: continue
                val hash = obj.optString("file_hash")
                if (hash.isBlank()) continue
                annotationsList.add(
                    AnnotationBackup(
                        fileHash = hash,
                        pageNumber = obj.optInt("page_number", 1),
                        selectedText = obj.optString("selected_text", ""),
                        noteText = if (obj.isNull("note_text")) null else obj.optString("note_text"),
                        colorId = obj.optInt("color_id", 1),
                        createdAt = obj.optString("created_at", "")
                    )
                )
            }
        }

        return BackupMetadata(
            client = client,
            version = version,
            schemaVersion = schemaVersion,
            exportedAt = exportedAt,
            appInfo = appInfo,
            userPreferences = userPrefs,
            books = booksList,
            readingProgress = progressList,
            bookmarks = bookmarksList,
            annotations = annotationsList
        )
    }
}

/**
 * Manager coordinating database records and user preferences export/import.
 */
class MetadataBackupManager(
    private val dbHelper: FolioHavenDbHelper,
    private val preferencesRepository: UserPreferencesRepository? = null
) {
    fun exportMetadataJson(): String {
        val allBooks = dbHelper.getAllBooks()
        val booksBackup = allBooks.map { b ->
            BookMetadataBackup(
                fileHash = b.fileHash,
                title = b.title,
                author = b.author,
                description = b.description,
                format = b.format,
                fileSize = b.fileSize,
                category = b.category,
                gradientStart = b.coverGradientStart,
                gradientEnd = b.coverGradientEnd
            )
        }

        val progressBackup = allBooks.filter { it.currentPage > 1 || it.progressPercent > 0f || it.isFinished }.map { b ->
            ReadingProgressBackup(
                fileHash = b.fileHash,
                title = b.title,
                author = b.author,
                format = b.format,
                currentPage = b.currentPage,
                totalPages = b.totalPages,
                progressPercent = b.progressPercent.toDouble(),
                readingTimeSeconds = 0,
                isFinished = b.isFinished,
                lastOpenedAt = b.lastOpenedAt
            )
        }

        val allBookmarks = dbHelper.getAllBookmarks()
        val bookHashMap = allBooks.associate { it.id to it.fileHash }
        val bookmarksBackup = allBookmarks.mapNotNull { bm ->
            val hash = bookHashMap[bm.bookId] ?: return@mapNotNull null
            BookmarkBackup(
                fileHash = hash,
                pageNumber = bm.pageNumber,
                title = bm.title,
                snippet = bm.snippet,
                createdAt = bm.createdAt
            )
        }

        val allAnnotations = dbHelper.getAllAnnotations()
        val annotationsBackup = allAnnotations.mapNotNull { an ->
            val hash = bookHashMap[an.bookId] ?: return@mapNotNull null
            AnnotationBackup(
                fileHash = hash,
                pageNumber = an.pageNumber,
                selectedText = an.selectedText,
                noteText = an.noteText,
                colorId = an.colorId,
                createdAt = an.createdAt
            )
        }

        val userPrefsBackup = preferencesRepository?.let { prefs ->
            UserPreferencesBackup(
                defaultFontSize = prefs.defaultFontSize.value,
                defaultLineSpacing = prefs.defaultLineSpacing.value,
                defaultReaderTheme = prefs.defaultReaderTheme.value.name,
                defaultMarginsDp = prefs.defaultMarginsDp.value,
                ttsSpeechRate = prefs.ttsSpeechRate.value,
                ttsPitch = prefs.ttsPitch.value,
                ttsLanguage = prefs.ttsLanguage.value,
                isGreetingCompleted = prefs.isGreetingCompleted()
            )
        }

        val backup = BackupMetadata(
            client = "FolioHaven-Mobile",
            version = "2.0.0",
            schemaVersion = 2,
            exportedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
            appInfo = BackupAppInfo(
                appName = AppInfo.APP_NAME,
                versionName = AppInfo.VERSION_NAME,
                buildNumber = AppInfo.BUILD_NUMBER,
                license = AppInfo.LICENSE
            ),
            userPreferences = userPrefsBackup,
            books = booksBackup,
            readingProgress = progressBackup,
            bookmarks = bookmarksBackup,
            annotations = annotationsBackup
        )

        return MetadataJsonSerializer.serialize(backup)
    }

    fun importMetadataJson(jsonString: String): ImportBackupResult {
        val backup = MetadataJsonSerializer.deserialize(jsonString)

        var prefsRestored = false
        if (backup.userPreferences != null && preferencesRepository != null) {
            val p = backup.userPreferences
            preferencesRepository.setDefaultFontSize(p.defaultFontSize)
            preferencesRepository.setDefaultLineSpacing(p.defaultLineSpacing)
            try {
                preferencesRepository.setDefaultReaderTheme(
                    ReaderTheme.valueOf(p.defaultReaderTheme)
                )
            } catch (_: Exception) {}
            preferencesRepository.setDefaultMarginsDp(p.defaultMarginsDp)
            preferencesRepository.setTtsSpeechRate(p.ttsSpeechRate)
            preferencesRepository.setTtsPitch(p.ttsPitch)
            preferencesRepository.setTtsLanguage(p.ttsLanguage)
            preferencesRepository.setGreetingCompleted(p.isGreetingCompleted)
            prefsRestored = true
        }

        val (bookmarksCount, annotationsCount) = dbHelper.importUserDataJson(jsonString)

        return ImportBackupResult(
            bookmarksRestored = bookmarksCount,
            annotationsRestored = annotationsCount,
            readingProgressRestored = backup.readingProgress.size,
            preferencesRestored = prefsRestored
        )
    }
}
