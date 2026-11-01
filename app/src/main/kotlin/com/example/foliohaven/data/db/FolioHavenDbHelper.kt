package com.example.foliohaven.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.foliohaven.data.model.AnnotationRecord
import com.example.foliohaven.data.model.BookRecord
import com.example.foliohaven.data.model.BookmarkRecord
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FolioHavenDbHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        const val DATABASE_NAME = "foliohaven.sqlite3"
        const val DATABASE_VERSION = 1

        private fun currentTimestamp(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            return sdf.format(Date())
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
        db.enableWriteAheadLogging()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS books (
                id                  INTEGER PRIMARY KEY AUTOINCREMENT,
                title               TEXT NOT NULL,
                author              TEXT NOT NULL,
                description         TEXT,
                file_path           TEXT NOT NULL UNIQUE,
                file_size           INTEGER NOT NULL DEFAULT 0,
                file_hash           TEXT NOT NULL UNIQUE,
                format              TEXT NOT NULL,
                cover_path          TEXT,
                gradient_start      INTEGER NOT NULL DEFAULT -14798789,
                gradient_end        INTEGER NOT NULL DEFAULT -15784150,
                category            TEXT NOT NULL DEFAULT 'Tất cả sách',
                is_deleted          INTEGER NOT NULL DEFAULT 0,
                created_at          TEXT NOT NULL,
                updated_at          TEXT NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS reading_progress (
                id                  INTEGER PRIMARY KEY AUTOINCREMENT,
                book_id             INTEGER NOT NULL UNIQUE REFERENCES books(id) ON DELETE CASCADE,
                current_cfi         TEXT,
                current_page        INTEGER NOT NULL DEFAULT 1,
                total_pages         INTEGER NOT NULL DEFAULT 1,
                progress_percent    REAL NOT NULL DEFAULT 0.0,
                reading_time_seconds INTEGER NOT NULL DEFAULT 0,
                last_opened_at      TEXT NOT NULL,
                is_finished         INTEGER NOT NULL DEFAULT 0,
                updated_at          TEXT NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS bookmarks (
                id                  INTEGER PRIMARY KEY AUTOINCREMENT,
                book_id             INTEGER NOT NULL REFERENCES books(id) ON DELETE CASCADE,
                cfi                 TEXT,
                page_number         INTEGER NOT NULL DEFAULT 1,
                title               TEXT NOT NULL,
                snippet             TEXT,
                created_at          TEXT NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS annotations (
                id                  INTEGER PRIMARY KEY AUTOINCREMENT,
                book_id             INTEGER NOT NULL REFERENCES books(id) ON DELETE CASCADE,
                cfi_start           TEXT,
                cfi_end             TEXT,
                page_number         INTEGER NOT NULL DEFAULT 1,
                selected_text       TEXT NOT NULL,
                note_text           TEXT,
                color_id            INTEGER NOT NULL DEFAULT 1,
                created_at          TEXT NOT NULL,
                updated_at          TEXT NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_books_format ON books(format);")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_books_is_deleted ON books(is_deleted);")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_reading_progress_last_opened ON reading_progress(last_opened_at);")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_bookmarks_book_id ON bookmarks(book_id);")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_annotations_book_id ON annotations(book_id);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun insertOrUpdateBook(
        title: String,
        author: String,
        description: String?,
        filePath: String,
        fileSize: Long,
        fileHash: String,
        format: String,
        coverPath: String?,
        gradientStart: Long = 0xFF1E293BL,
        gradientEnd: Long = 0xFF0F172AL,
        category: String = "Tất cả sách",
        initialTotalPages: Int = 1
    ): Long {
        val now = currentTimestamp()
        val db = writableDatabase

        val cursor = db.query(
            "books",
            arrayOf("id"),
            "file_hash = ? AND is_deleted = 0",
            arrayOf(fileHash),
            null, null, null
        )
        if (cursor.moveToFirst()) {
            val existingId = cursor.getLong(0)
            cursor.close()
            return existingId
        }
        cursor.close()

        val values = ContentValues().apply {
            put("title", title)
            put("author", author)
            put("description", description)
            put("file_path", filePath)
            put("file_size", fileSize)
            put("file_hash", fileHash)
            put("format", format.uppercase(Locale.ROOT))
            put("cover_path", coverPath)
            put("gradient_start", gradientStart)
            put("gradient_end", gradientEnd)
            put("category", category)
            put("is_deleted", 0)
            put("created_at", now)
            put("updated_at", now)
        }
        val bookId = db.insertWithOnConflict("books", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        if (bookId > 0) {
            val progressValues = ContentValues().apply {
                put("book_id", bookId)
                put("current_page", 1)
                put("total_pages", initialTotalPages.coerceAtLeast(1))
                put("progress_percent", 0.0f)
                put("reading_time_seconds", 0)
                put("last_opened_at", now)
                put("is_finished", 0)
                put("updated_at", now)
            }
            db.insertWithOnConflict("reading_progress", null, progressValues, SQLiteDatabase.CONFLICT_IGNORE)
        }
        return bookId
    }

    fun getAllBooks(categoryFilter: String? = null, searchQuery: String? = null): List<BookRecord> {
        val db = readableDatabase
        val selectionClauses = mutableListOf("b.is_deleted = 0")
        val selectionArgs = mutableListOf<String>()

        if (!categoryFilter.isNullOrBlank() && categoryFilter != "Tất cả sách") {
            selectionClauses.add("(b.category = ? OR b.format = ?)")
            selectionArgs.add(categoryFilter)
            selectionArgs.add(categoryFilter)
        }

        if (!searchQuery.isNullOrBlank()) {
            selectionClauses.add("(b.title LIKE ? OR b.author LIKE ?)")
            val arg = "%$searchQuery%"
            selectionArgs.add(arg)
            selectionArgs.add(arg)
        }

        val whereClause = selectionClauses.joinToString(" AND ")
        val sql = """
            SELECT b.id, b.title, b.author, b.description, b.file_path, b.file_size,
                   b.file_hash, b.format, b.cover_path, b.gradient_start, b.gradient_end,
                   b.category, rp.current_page, rp.total_pages, rp.progress_percent,
                   rp.last_opened_at, rp.is_finished
            FROM books b
            LEFT JOIN reading_progress rp ON b.id = rp.book_id
            WHERE $whereClause
            ORDER BY rp.last_opened_at DESC, b.id DESC
        """.trimIndent()

        val list = mutableListOf<BookRecord>()
        val cursor: Cursor = db.rawQuery(sql, selectionArgs.toTypedArray())
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    BookRecord(
                        id = c.getLong(0),
                        title = c.getString(1),
                        author = c.getString(2),
                        description = c.getString(3),
                        filePath = c.getString(4),
                        fileSize = c.getLong(5),
                        fileHash = c.getString(6),
                        format = c.getString(7),
                        coverPath = c.getString(8),
                        coverGradientStart = c.getLong(9),
                        coverGradientEnd = c.getLong(10),
                        category = c.getString(11),
                        currentPage = c.getInt(12).coerceAtLeast(1),
                        totalPages = c.getInt(13).coerceAtLeast(1),
                        progressPercent = c.getFloat(14),
                        lastOpenedAt = c.getString(15) ?: "",
                        isFinished = c.getInt(16) == 1
                    )
                )
            }
        }
        return list
    }

    fun getBookById(id: Long): BookRecord? {
        val db = readableDatabase
        val sql = """
            SELECT b.id, b.title, b.author, b.description, b.file_path, b.file_size,
                   b.file_hash, b.format, b.cover_path, b.gradient_start, b.gradient_end,
                   b.category, rp.current_page, rp.total_pages, rp.progress_percent,
                   rp.last_opened_at, rp.is_finished
            FROM books b
            LEFT JOIN reading_progress rp ON b.id = rp.book_id
            WHERE b.id = ? AND b.is_deleted = 0
        """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(id.toString()))
        cursor.use { c ->
            if (c.moveToFirst()) {
                return BookRecord(
                    id = c.getLong(0),
                    title = c.getString(1),
                    author = c.getString(2),
                    description = c.getString(3),
                    filePath = c.getString(4),
                    fileSize = c.getLong(5),
                    fileHash = c.getString(6),
                    format = c.getString(7),
                    coverPath = c.getString(8),
                    coverGradientStart = c.getLong(9),
                    coverGradientEnd = c.getLong(10),
                    category = c.getString(11),
                    currentPage = c.getInt(12).coerceAtLeast(1),
                    totalPages = c.getInt(13).coerceAtLeast(1),
                    progressPercent = c.getFloat(14),
                    lastOpenedAt = c.getString(15) ?: "",
                    isFinished = c.getInt(16) == 1
                )
            }
        }
        return null
    }

    fun updateReadingProgress(bookId: Long, currentPage: Int, totalPages: Int, elapsedSeconds: Int = 0) {
        val db = writableDatabase
        val now = currentTimestamp()
        val percent = if (totalPages > 0) (currentPage.toFloat() / totalPages.toFloat()).coerceIn(0f, 1f) else 0f
        val isFinished = if (percent >= 0.99f) 1 else 0

        val values = ContentValues().apply {
            put("current_page", currentPage)
            put("total_pages", totalPages)
            put("progress_percent", percent)
            put("last_opened_at", now)
            put("is_finished", isFinished)
            put("updated_at", now)
        }

        val rows = db.update("reading_progress", values, "book_id = ?", arrayOf(bookId.toString()))
        if (rows == 0) {
            values.put("book_id", bookId)
            values.put("reading_time_seconds", elapsedSeconds)
            db.insert("reading_progress", null, values)
        } else if (elapsedSeconds > 0) {
            db.execSQL(
                "UPDATE reading_progress SET reading_time_seconds = reading_time_seconds + ? WHERE book_id = ?",
                arrayOf<Any>(elapsedSeconds, bookId)
            )
        }
    }

    fun updateBookMetadata(bookId: Long, title: String, author: String, category: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", title.trim())
            put("author", author.trim())
            put("category", category.trim())
            put("updated_at", currentTimestamp())
        }
        return db.update("books", values, "id = ?", arrayOf(bookId.toString())) > 0
    }

    fun deleteBook(bookId: Long, permanent: Boolean = false): Boolean {
        val db = writableDatabase
        return if (permanent) {
            db.delete("books", "id = ?", arrayOf(bookId.toString())) > 0
        } else {
            val values = ContentValues().apply {
                put("is_deleted", 1)
                put("updated_at", currentTimestamp())
            }
            db.update("books", values, "id = ?", arrayOf(bookId.toString())) > 0
        }
    }

    fun toggleBookFinished(bookId: Long, isFinished: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("is_finished", if (isFinished) 1 else 0)
            if (isFinished) {
                put("progress_percent", 1.0f)
            }
            put("updated_at", currentTimestamp())
        }
        val rows = db.update("reading_progress", values, "book_id = ?", arrayOf(bookId.toString()))
        return rows > 0
    }

    fun addBookmark(bookId: Long, pageNumber: Int, title: String, snippet: String? = null): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("book_id", bookId)
            put("page_number", pageNumber)
            put("title", title)
            put("snippet", snippet)
            put("created_at", currentTimestamp())
        }
        return db.insert("bookmarks", null, values)
    }

    fun getBookmarks(bookId: Long): List<BookmarkRecord> {
        val db = readableDatabase
        val list = mutableListOf<BookmarkRecord>()
        val cursor = db.query(
            "bookmarks",
            arrayOf("id", "book_id", "page_number", "title", "snippet", "created_at"),
            "book_id = ?",
            arrayOf(bookId.toString()),
            null, null, "page_number ASC"
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    BookmarkRecord(
                        id = c.getLong(0),
                        bookId = c.getLong(1),
                        pageNumber = c.getInt(2),
                        title = c.getString(3),
                        snippet = c.getString(4),
                        createdAt = c.getString(5)
                    )
                )
            }
        }
        return list
    }

    data class BookmarkWithBook(
        val id: Long,
        val bookId: Long,
        val bookTitle: String,
        val pageNumber: Int,
        val title: String,
        val snippet: String?,
        val createdAt: String
    )

    fun getAllBookmarks(): List<BookmarkWithBook> {
        val db = readableDatabase
        val list = mutableListOf<BookmarkWithBook>()
        val sql = """
            SELECT bm.id, bm.book_id, b.title, bm.page_number, bm.title, bm.snippet, bm.created_at
            FROM bookmarks bm
            INNER JOIN books b ON bm.book_id = b.id
            ORDER BY bm.id DESC
        """.trimIndent()
        val cursor = db.rawQuery(sql, null)
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    BookmarkWithBook(
                        id = c.getLong(0),
                        bookId = c.getLong(1),
                        bookTitle = c.getString(2),
                        pageNumber = c.getInt(3),
                        title = c.getString(4),
                        snippet = c.getString(5),
                        createdAt = c.getString(6)
                    )
                )
            }
        }
        return list
    }

    fun deleteBookmark(id: Long): Boolean {
        val db = writableDatabase
        return db.delete("bookmarks", "id = ?", arrayOf(id.toString())) > 0
    }

    fun addAnnotation(
        bookId: Long,
        pageNumber: Int,
        selectedText: String,
        noteText: String? = null,
        colorId: Int = 1
    ): Long {
        val db = writableDatabase
        val now = currentTimestamp()
        val values = ContentValues().apply {
            put("book_id", bookId)
            put("page_number", pageNumber)
            put("selected_text", selectedText)
            put("note_text", noteText)
            put("color_id", colorId)
            put("created_at", now)
            put("updated_at", now)
        }
        return db.insert("annotations", null, values)
    }

    fun getAnnotations(bookId: Long): List<AnnotationRecord> {
        val db = readableDatabase
        val list = mutableListOf<AnnotationRecord>()
        val cursor = db.query(
            "annotations",
            arrayOf("id", "book_id", "page_number", "selected_text", "note_text", "color_id", "created_at"),
            "book_id = ?",
            arrayOf(bookId.toString()),
            null, null, "page_number ASC"
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    AnnotationRecord(
                        id = c.getLong(0),
                        bookId = c.getLong(1),
                        pageNumber = c.getInt(2),
                        selectedText = c.getString(3),
                        noteText = c.getString(4),
                        colorId = c.getInt(5),
                        createdAt = c.getString(6)
                    )
                )
            }
        }
        return list
    }

    data class AnnotationWithBook(
        val id: Long,
        val bookId: Long,
        val bookTitle: String,
        val pageNumber: Int,
        val selectedText: String,
        val noteText: String?,
        val colorId: Int,
        val createdAt: String
    )

    fun getAllAnnotations(): List<AnnotationWithBook> {
        val db = readableDatabase
        val list = mutableListOf<AnnotationWithBook>()
        val sql = """
            SELECT an.id, an.book_id, b.title, an.page_number, an.selected_text, an.note_text, an.color_id, an.created_at
            FROM annotations an
            INNER JOIN books b ON an.book_id = b.id
            ORDER BY an.id DESC
        """.trimIndent()
        val cursor = db.rawQuery(sql, null)
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    AnnotationWithBook(
                        id = c.getLong(0),
                        bookId = c.getLong(1),
                        bookTitle = c.getString(2),
                        pageNumber = c.getInt(3),
                        selectedText = c.getString(4),
                        noteText = c.getString(5),
                        colorId = c.getInt(6),
                        createdAt = c.getString(7)
                    )
                )
            }
        }
        return list
    }

    fun deleteAnnotation(id: Long): Boolean {
        val db = writableDatabase
        return db.delete("annotations", "id = ?", arrayOf(id.toString())) > 0
    }

    data class ReadingStats(
        val totalBooks: Int,
        val finishedBooks: Int,
        val totalReadingMinutes: Int,
        val readingStreakDays: Int
    )

    fun getReadingStats(): ReadingStats {
        val db = readableDatabase
        var totalBooks = 0
        var finishedBooks = 0
        var totalSeconds = 0

        val cursor = db.rawQuery(
            """
            SELECT COUNT(b.id),
                   SUM(CASE WHEN rp.is_finished = 1 THEN 1 ELSE 0 END),
                   SUM(COALESCE(rp.reading_time_seconds, 0))
            FROM books b
            LEFT JOIN reading_progress rp ON b.id = rp.book_id
            WHERE b.is_deleted = 0
            """.trimIndent(),
            null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                totalBooks = c.getInt(0)
                finishedBooks = c.getInt(1)
                totalSeconds = c.getInt(2)
            }
        }

        val minutes = totalSeconds / 60
        var streak = 0
        val streakCursor = db.rawQuery(
            """
            SELECT COUNT(DISTINCT substr(last_opened_at, 1, 10))
            FROM reading_progress
            WHERE last_opened_at IS NOT NULL AND last_opened_at != ''
            """.trimIndent(),
            null
        )
        streakCursor.use { sc ->
            if (sc.moveToFirst()) {
                streak = sc.getInt(0)
            }
        }

        return ReadingStats(
            totalBooks = totalBooks,
            finishedBooks = finishedBooks,
            totalReadingMinutes = minutes,
            readingStreakDays = streak
        )
    }

    fun exportUserDataJson(): String {
        val root = JSONObject()
        root.put("client", "FolioHaven-Mobile")
        root.put("version", "1.0.0")
        root.put("exported_at", currentTimestamp())

        val db = readableDatabase

        // 1. Export reading progress
        val progressArray = JSONArray()
        val pCursor = db.rawQuery(
            """
            SELECT b.file_hash, b.title, b.author, b.format, rp.current_page, rp.total_pages,
                   rp.progress_percent, rp.reading_time_seconds, rp.is_finished, rp.last_opened_at
            FROM books b
            INNER JOIN reading_progress rp ON b.id = rp.book_id
            WHERE b.is_deleted = 0
            """.trimIndent(),
            null
        )
        pCursor.use { c ->
            while (c.moveToNext()) {
                val obj = JSONObject().apply {
                    put("file_hash", c.getString(0))
                    put("title", c.getString(1))
                    put("author", c.getString(2))
                    put("format", c.getString(3))
                    put("current_page", c.getInt(4))
                    put("total_pages", c.getInt(5))
                    put("progress_percent", c.getDouble(6))
                    put("reading_time_seconds", c.getInt(7))
                    put("is_finished", c.getInt(8) == 1)
                    put("last_opened_at", c.getString(9) ?: "")
                }
                progressArray.put(obj)
            }
        }
        root.put("reading_progress", progressArray)

        // 2. Export bookmarks
        val bookmarksArray = JSONArray()
        val bCursor = db.rawQuery(
            """
            SELECT b.file_hash, bm.page_number, bm.title, bm.snippet, bm.created_at
            FROM bookmarks bm
            INNER JOIN books b ON bm.book_id = b.id
            """.trimIndent(),
            null
        )
        bCursor.use { c ->
            while (c.moveToNext()) {
                val obj = JSONObject().apply {
                    put("file_hash", c.getString(0))
                    put("page_number", c.getInt(1))
                    put("title", c.getString(2))
                    put("snippet", c.getString(3) ?: "")
                    put("created_at", c.getString(4))
                }
                bookmarksArray.put(obj)
            }
        }
        root.put("bookmarks", bookmarksArray)

        // 3. Export annotations
        val annotationsArray = JSONArray()
        val aCursor = db.rawQuery(
            """
            SELECT b.file_hash, an.page_number, an.selected_text, an.note_text, an.color_id, an.created_at
            FROM annotations an
            INNER JOIN books b ON an.book_id = b.id
            """.trimIndent(),
            null
        )
        aCursor.use { c ->
            while (c.moveToNext()) {
                val obj = JSONObject().apply {
                    put("file_hash", c.getString(0))
                    put("page_number", c.getInt(1))
                    put("selected_text", c.getString(2))
                    put("note_text", c.getString(3) ?: "")
                    put("color_id", c.getInt(4))
                    put("created_at", c.getString(5))
                }
                annotationsArray.put(obj)
            }
        }
        root.put("annotations", annotationsArray)

        return root.toString(2)
    }

    fun importUserDataJson(jsonString: String): Pair<Int, Int> {
        val root = JSONObject(jsonString)
        val db = writableDatabase
        var importedBookmarks = 0
        var importedAnnotations = 0

        // Map existing books by file_hash
        val hashMap = mutableMapOf<String, Long>()
        val c = db.rawQuery("SELECT id, file_hash FROM books WHERE is_deleted = 0", null)
        c.use {
            while (it.moveToNext()) {
                hashMap[it.getString(1)] = it.getLong(0)
            }
        }

        // 1. Import reading progress
        if (root.has("reading_progress")) {
            val progressArray = root.getJSONArray("reading_progress")
            for (i in 0 until progressArray.length()) {
                val obj = progressArray.getJSONObject(i)
                val hash = obj.optString("file_hash")
                val bookId = hashMap[hash] ?: continue

                val currentPage = obj.optInt("current_page", 1)
                val totalPages = obj.optInt("total_pages", 1)
                val timeSeconds = obj.optInt("reading_time_seconds", 0)

                updateReadingProgress(bookId, currentPage, totalPages, timeSeconds)
            }
        }

        // 2. Import bookmarks
        if (root.has("bookmarks")) {
            val bookmarksArray = root.getJSONArray("bookmarks")
            for (i in 0 until bookmarksArray.length()) {
                val obj = bookmarksArray.getJSONObject(i)
                val hash = obj.optString("file_hash")
                val bookId = hashMap[hash] ?: continue
                val page = obj.optInt("page_number", 1)
                val title = obj.optString("title", "Trang $page")
                val snippet = if (obj.isNull("snippet")) null else obj.optString("snippet")

                // Check duplicate bookmark
                val existing = db.query(
                    "bookmarks",
                    arrayOf("id"),
                    "book_id = ? AND page_number = ?",
                    arrayOf(bookId.toString(), page.toString()),
                    null, null, null
                )
                val isDup = existing.count > 0
                existing.close()

                if (!isDup) {
                    addBookmark(bookId, page, title, snippet)
                    importedBookmarks++
                }
            }
        }

        // 3. Import annotations
        if (root.has("annotations")) {
            val annotationsArray = root.getJSONArray("annotations")
            for (i in 0 until annotationsArray.length()) {
                val obj = annotationsArray.getJSONObject(i)
                val hash = obj.optString("file_hash")
                val bookId = hashMap[hash] ?: continue
                val page = obj.optInt("page_number", 1)
                val selected = obj.optString("selected_text", "")
                val note = if (obj.isNull("note_text")) null else obj.optString("note_text")
                val colorId = obj.optInt("color_id", 1)

                val existing = db.query(
                    "annotations",
                    arrayOf("id"),
                    "book_id = ? AND page_number = ? AND selected_text = ?",
                    arrayOf(bookId.toString(), page.toString(), selected),
                    null, null, null
                )
                val isDup = existing.count > 0
                existing.close()

                if (!isDup) {
                    addAnnotation(bookId, page, selected, note, colorId)
                    importedAnnotations++
                }
            }
        }

        return Pair(importedBookmarks, importedAnnotations)
    }
}
