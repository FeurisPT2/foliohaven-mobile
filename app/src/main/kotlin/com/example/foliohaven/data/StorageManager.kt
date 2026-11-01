package com.example.foliohaven.data

import android.content.Context
import com.example.foliohaven.data.db.FolioHavenDbHelper
import java.io.File
import java.util.Locale

/**
 * Statistics snapshot representing the user's library and local storage usage.
 */
data class LibraryStorageStats(
    val totalBooks: Int,
    val readingMinutes: Long,
    val totalStorageBytes: Long,
    val booksStorageBytes: Long = 0L,
    val databaseStorageBytes: Long = 0L,
    val cacheStorageBytes: Long = 0L
) {
    /**
     * Exact reading hours as a decimal (e.g. 150 minutes -> 2.5 hours).
     */
    val readingHours: Double
        get() = if (readingMinutes > 0) readingMinutes / 60.0 else 0.0

    /**
     * Whole hours component of reading time (e.g. 150 minutes -> 2 hours).
     */
    val readingHoursWhole: Long
        get() = readingMinutes / 60

    /**
     * Human-readable formatted storage string (e.g. "12.4 MB").
     */
    val formattedStorage: String
        get() = StorageManager.formatStorageSize(totalStorageBytes)

    /**
     * Localized reading time string for the literary UI (e.g. "2g 30p" or "45 phút").
     */
    val formattedReadingTime: String
        get() {
            val hours = readingMinutes / 60
            val mins = readingMinutes % 60
            return when {
                hours > 0 && mins > 0 -> "${hours}g ${mins}p"
                hours > 0 -> "${hours} giờ"
                else -> "${mins} phút"
            }
        }
}

/**
 * Typealias ensuring strict compliance with PROJECT.md Interface Contract:
 * `getLibraryStorageStats(): LibraryStats (totalBooks: Int, readingMinutes: Long, storageBytes: Long)`
 */
typealias LibraryStats = LibraryStorageStats

class StorageManager(
    private val context: Context? = null,
    private val dbHelper: FolioHavenDbHelper? = null,
    // JVM Unit Testing injection points:
    private val booksDirOverride: File? = null,
    private val cacheDirOverride: File? = null,
    private val databaseDirOverride: File? = null,
    private val readingStatsProvider: (() -> FolioHavenDbHelper.ReadingStats)? = null
) {
    companion object {
        /**
         * Converts raw byte count to clean, human-readable string.
         * Uses binary units (1024 basis) formatted to 1 decimal place.
         */
        fun formatStorageSize(bytes: Long): String {
            if (bytes <= 0L) return "0 B"
            val kilo = 1024.0
            val mega = kilo * 1024.0
            val giga = mega * 1024.0

            return when {
                bytes >= giga -> String.format(Locale.US, "%.1f GB", bytes / giga)
                bytes >= mega -> String.format(Locale.US, "%.1f MB", bytes / mega)
                bytes >= kilo -> String.format(Locale.US, "%.1f KB", bytes / kilo)
                else -> "$bytes B"
            }
        }

        /**
         * Recursively calculates the byte size of a file or directory.
         * Gracefully handles null, non-existent directories, and empty folders.
         */
        fun calculateDirectorySize(dir: File?): Long {
            if (dir == null || !dir.exists()) return 0L
            if (dir.isFile) return dir.length()
            var total = 0L
            val children = dir.listFiles() ?: return 0L
            for (child in children) {
                total += if (child.isDirectory) {
                    calculateDirectorySize(child)
                } else {
                    child.length()
                }
            }
            return total
        }

        /**
         * Safely deletes all files and subdirectories INSIDE [dir],
         * keeping [dir] itself existing.
         */
        fun deleteDirectoryContents(dir: File?): Boolean {
            if (dir == null || !dir.exists()) return true
            if (dir.isFile) return dir.delete()
            var allDeleted = true
            val children = dir.listFiles() ?: return true
            for (child in children) {
                allDeleted = if (child.isDirectory) {
                    deleteDirectoryContents(child) && child.delete() && allDeleted
                } else {
                    child.delete() && allDeleted
                }
            }
            return allDeleted
        }
    }

    fun getBooksDirectory(): File? {
        return booksDirOverride ?: context?.let { File(it.filesDir, "books") }
    }

    fun getCacheDirectory(): File? {
        return cacheDirOverride ?: context?.cacheDir
    }

    fun getDatabaseFiles(): List<File> {
        databaseDirOverride?.let { dir ->
            if (dir.exists()) {
                return dir.listFiles { f -> f.name.startsWith(FolioHavenDbHelper.DATABASE_NAME) }?.toList() ?: emptyList()
            }
            return emptyList()
        }
        val ctx = context ?: return emptyList()
        return try {
            val dbFile = ctx.getDatabasePath(FolioHavenDbHelper.DATABASE_NAME)
            val dir = dbFile?.parentFile
            if (dir != null && dir.exists()) {
                dir.listFiles { f -> f.name.startsWith(FolioHavenDbHelper.DATABASE_NAME) }?.toList() ?: emptyList()
            } else if (dbFile != null && dbFile.exists()) {
                listOf(dbFile)
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getBooksStorageBytes(): Long = calculateDirectorySize(getBooksDirectory())

    fun getCacheStorageBytes(): Long {
        val internalCache = calculateDirectorySize(getCacheDirectory())
        val externalCache = try {
            context?.externalCacheDir?.let { calculateDirectorySize(it) } ?: 0L
        } catch (_: Exception) {
            0L
        }
        return internalCache + externalCache
    }

    fun getDatabaseStorageBytes(): Long {
        return getDatabaseFiles().sumOf { it.length() }
    }

    fun getTotalStorageBytes(): Long {
        return getBooksStorageBytes() + getDatabaseStorageBytes() + getCacheStorageBytes()
    }

    /**
     * Clears all temporary cache files. Leaves context.cacheDir existing.
     */
    fun clearCache(): Boolean {
        var success = true
        getCacheDirectory()?.let { dir ->
            success = deleteDirectoryContents(dir) && success
        }
        try {
            context?.externalCacheDir?.let { extDir ->
                success = deleteDirectoryContents(extDir) && success
            }
        } catch (_: Exception) {
            // Safe fallback for external storage restrictions
        }
        return success
    }

    /**
     * Retrieves aggregated library storage statistics.
     */
    fun getLibraryStorageStats(): LibraryStorageStats {
        val stats = readingStatsProvider?.invoke() ?: dbHelper?.getReadingStats()
        val totalBooks = stats?.totalBooks ?: 0
        val readingMinutes = (stats?.totalReadingMinutes ?: 0).toLong()

        val booksBytes = getBooksStorageBytes()
        val dbBytes = getDatabaseStorageBytes()
        val cacheBytes = getCacheStorageBytes()
        val totalBytes = booksBytes + dbBytes + cacheBytes

        return LibraryStorageStats(
            totalBooks = totalBooks,
            readingMinutes = readingMinutes,
            totalStorageBytes = totalBytes,
            booksStorageBytes = booksBytes,
            databaseStorageBytes = dbBytes,
            cacheStorageBytes = cacheBytes
        )
    }
}
