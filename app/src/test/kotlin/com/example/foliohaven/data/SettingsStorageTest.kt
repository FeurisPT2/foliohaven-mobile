package com.example.foliohaven.data

import com.example.foliohaven.data.db.FolioHavenDbHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SettingsStorageTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    // --- Group A: Size Formatting & Calculation Tests ---

    @Test
    fun formatStorageSize_formatsVariousRangesCorrectly() {
        assertEquals("0 B", StorageManager.formatStorageSize(0L))
        assertEquals("0 B", StorageManager.formatStorageSize(-100L))
        assertEquals("512 B", StorageManager.formatStorageSize(512L))
        assertEquals("1023 B", StorageManager.formatStorageSize(1023L))

        assertEquals("1.0 KB", StorageManager.formatStorageSize(1024L))
        assertEquals("1.5 KB", StorageManager.formatStorageSize(1536L))

        assertEquals("1.0 MB", StorageManager.formatStorageSize(1024L * 1024L))
        assertEquals("2.5 MB", StorageManager.formatStorageSize((2.5 * 1024 * 1024).toLong()))

        assertEquals("1.0 GB", StorageManager.formatStorageSize(1024L * 1024L * 1024L))
        assertEquals("3.2 GB", StorageManager.formatStorageSize((3.2 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun calculateDirectorySize_calculatesRecursiveDirectoryAndFileSizes() {
        assertEquals(0L, StorageManager.calculateDirectorySize(null))

        val nonExistent = File(tempFolder.root, "does_not_exist")
        assertEquals(0L, StorageManager.calculateDirectorySize(nonExistent))

        val singleFile = tempFolder.newFile("sample.txt")
        singleFile.writeBytes(ByteArray(150))
        assertEquals(150L, StorageManager.calculateDirectorySize(singleFile))

        val dir = tempFolder.newFolder("books_dir")
        val file1 = File(dir, "b1.epub").apply { writeBytes(ByteArray(200)) }
        val subDir = File(dir, "nested").apply { mkdirs() }
        val file2 = File(subDir, "b2.pdf").apply { writeBytes(ByteArray(300)) }

        val totalSize = StorageManager.calculateDirectorySize(dir)
        assertEquals(500L, totalSize)
    }

    // --- Group B: Safe Cache Clearing Tests ---

    @Test
    fun deleteDirectoryContents_safelyCleansChildrenWhileRetainingRoot() {
        val cacheRoot = tempFolder.newFolder("app_cache")
        val sub1 = File(cacheRoot, "sub1").apply { mkdirs() }
        File(sub1, "temp1.bin").writeBytes(ByteArray(100))
        File(cacheRoot, "temp2.bin").writeBytes(ByteArray(200))

        assertTrue(cacheRoot.exists())
        assertEquals(2, cacheRoot.listFiles()?.size)

        val result = StorageManager.deleteDirectoryContents(cacheRoot)
        assertTrue(result)
        // Root directory MUST remain intact
        assertTrue(cacheRoot.exists())
        assertTrue(cacheRoot.isDirectory)
        // Children must be completely gone
        assertEquals(0, cacheRoot.listFiles()?.size)
    }

    @Test
    fun deleteDirectoryContents_handlesNonExistentOrEmptyGracefully() {
        assertTrue(StorageManager.deleteDirectoryContents(null))

        val missing = File(tempFolder.root, "missing_cache")
        assertTrue(StorageManager.deleteDirectoryContents(missing))

        val emptyDir = tempFolder.newFolder("empty_cache")
        assertTrue(StorageManager.deleteDirectoryContents(emptyDir))
        assertTrue(emptyDir.exists())
    }

    @Test
    fun clearCache_deletesOnlyCacheFilesLeavingBooksAndDatabasesIntact() {
        val booksDir = tempFolder.newFolder("books")
        val bookFile = File(booksDir, "book.epub").apply { writeBytes(ByteArray(1024)) }

        val dbDir = tempFolder.newFolder("databases")
        val dbFile = File(dbDir, "foliohaven.sqlite3").apply { writeBytes(ByteArray(4096)) }

        val cacheDir = tempFolder.newFolder("cache")
        val cacheFile = File(cacheDir, "cover_cache.png").apply { writeBytes(ByteArray(512)) }

        val storageManager = StorageManager(
            booksDirOverride = booksDir,
            cacheDirOverride = cacheDir,
            databaseDirOverride = dbDir
        )

        val cleared = storageManager.clearCache()
        assertTrue(cleared)

        // Cache must be cleared but directory kept
        assertTrue(cacheDir.exists())
        assertEquals(0, cacheDir.listFiles()?.size)

        // Books and Database MUST NOT be touched
        assertTrue(bookFile.exists())
        assertEquals(1024L, bookFile.length())
        assertTrue(dbFile.exists())
        assertEquals(4096L, dbFile.length())
    }

    // --- Group C: Storage Aggregation & Domain Statistics ---

    @Test
    fun getLibraryStorageStats_aggregatesDirectoriesAndStatsCorrectly() {
        val booksDir = tempFolder.newFolder("books_stat")
        File(booksDir, "b1.epub").writeBytes(ByteArray(2000))
        File(booksDir, "b2.pdf").writeBytes(ByteArray(3000))

        val dbDir = tempFolder.newFolder("databases_stat")
        File(dbDir, "foliohaven.sqlite3").writeBytes(ByteArray(4000))
        File(dbDir, "foliohaven.sqlite3-wal").writeBytes(ByteArray(1000))

        val cacheDir = tempFolder.newFolder("cache_stat")
        File(cacheDir, "thumb.jpg").writeBytes(ByteArray(500))

        val dummyReadingStats = FolioHavenDbHelper.ReadingStats(
            totalBooks = 2,
            finishedBooks = 1,
            totalReadingMinutes = 150,
            readingStreakDays = 5
        )

        val storageManager = StorageManager(
            booksDirOverride = booksDir,
            cacheDirOverride = cacheDir,
            databaseDirOverride = dbDir,
            readingStatsProvider = { dummyReadingStats }
        )

        val stats = storageManager.getLibraryStorageStats()
        assertEquals(2, stats.totalBooks)
        assertEquals(150L, stats.readingMinutes)
        assertEquals(2.5, stats.readingHours, 0.001)
        assertEquals(2L, stats.readingHoursWhole)

        assertEquals(5000L, stats.booksStorageBytes)
        assertEquals(5000L, stats.databaseStorageBytes)
        assertEquals(500L, stats.cacheStorageBytes)
        assertEquals(10500L, stats.totalStorageBytes)
        assertEquals("10.3 KB", stats.formattedStorage)
        assertEquals("2g 30p", stats.formattedReadingTime)
    }

    @Test
    fun libraryStorageStats_formattedReadingTime_formatsHoursAndMinutes() {
        val s0 = LibraryStorageStats(totalBooks = 0, readingMinutes = 0, totalStorageBytes = 0)
        assertEquals("0 phút", s0.formattedReadingTime)

        val s1 = LibraryStorageStats(totalBooks = 1, readingMinutes = 45, totalStorageBytes = 0)
        assertEquals("45 phút", s1.formattedReadingTime)

        val s2 = LibraryStorageStats(totalBooks = 1, readingMinutes = 60, totalStorageBytes = 0)
        assertEquals("1 giờ", s2.formattedReadingTime)

        val s3 = LibraryStorageStats(totalBooks = 1, readingMinutes = 120, totalStorageBytes = 0)
        assertEquals("2 giờ", s3.formattedReadingTime)

        val s4 = LibraryStorageStats(totalBooks = 1, readingMinutes = 135, totalStorageBytes = 0)
        assertEquals("2g 15p", s4.formattedReadingTime)
    }

    @Test
    fun readingHistoryReset_resetsProgressWhileRetainingBooksCount() {
        val initialStats = FolioHavenDbHelper.ReadingStats(
            totalBooks = 5,
            finishedBooks = 3,
            totalReadingMinutes = 480,
            readingStreakDays = 12
        )
        // Resetting reading progress resets minutes, finished, and streak, while total books remains 5
        val postResetStats = FolioHavenDbHelper.ReadingStats(
            totalBooks = initialStats.totalBooks,
            finishedBooks = 0,
            totalReadingMinutes = 0,
            readingStreakDays = 0
        )
        assertEquals(5, postResetStats.totalBooks)
        assertEquals(0, postResetStats.finishedBooks)
        assertEquals(0, postResetStats.totalReadingMinutes)
        assertEquals(0, postResetStats.readingStreakDays)

        val storageStats = LibraryStorageStats(
            totalBooks = postResetStats.totalBooks,
            readingMinutes = postResetStats.totalReadingMinutes.toLong(),
            totalStorageBytes = 1000L
        )
        assertEquals(5, storageStats.totalBooks)
        assertEquals(0L, storageStats.readingMinutes)
        assertEquals(0.0, storageStats.readingHours, 0.001)
        assertEquals("0 phút", storageStats.formattedReadingTime)
    }
}
