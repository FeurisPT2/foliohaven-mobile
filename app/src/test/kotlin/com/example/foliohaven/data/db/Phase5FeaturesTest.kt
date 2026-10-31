package com.example.foliohaven.data.db

import com.example.foliohaven.data.model.AnnotationRecord
import com.example.foliohaven.data.model.BookmarkRecord
import com.example.foliohaven.ui.reader.getHighlightColor
import com.example.foliohaven.ui.reader.getHighlightName
import com.example.foliohaven.ui.theme.HighlightAmber
import com.example.foliohaven.ui.theme.HighlightAzure
import com.example.foliohaven.ui.theme.HighlightEmerald
import com.example.foliohaven.ui.theme.HighlightRose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase5FeaturesTest {

    @Test
    fun highlightColors_mapCorrectlyToTokens() {
        assertEquals(HighlightAmber, getHighlightColor(1))
        assertEquals(HighlightEmerald, getHighlightColor(2))
        assertEquals(HighlightAzure, getHighlightColor(3))
        assertEquals(HighlightRose, getHighlightColor(4))

        // Default fallback
        assertEquals(HighlightAmber, getHighlightColor(999))
    }

    @Test
    fun highlightNames_returnVietnameseLabels() {
        assertEquals("Hổ phách", getHighlightName(1))
        assertEquals("Ngọc lục bảo", getHighlightName(2))
        assertEquals("Da trời", getHighlightName(3))
        assertEquals("Hồng hạc", getHighlightName(4))
    }

    @Test
    fun annotationRecord_instantiatesCorrectly() {
        val annotation = AnnotationRecord(
            id = 10L,
            bookId = 1L,
            pageNumber = 14,
            selectedText = "Khoa học suy luận",
            noteText = "Phương pháp tư duy logic sắc sảo của Holmes",
            colorId = 2,
            createdAt = "2026-10-31T10:00:00Z"
        )

        assertEquals(10L, annotation.id)
        assertEquals(1L, annotation.bookId)
        assertEquals(14, annotation.pageNumber)
        assertEquals("Khoa học suy luận", annotation.selectedText)
        assertEquals("Phương pháp tư duy logic sắc sảo của Holmes", annotation.noteText)
        assertEquals(2, annotation.colorId)
    }

    @Test
    fun bookmarkRecord_instantiatesCorrectly() {
        val bookmark = BookmarkRecord(
            id = 5L,
            bookId = 2L,
            pageNumber = 392,
            title = "Trang 392",
            snippet = "Trận chiến Austerlitz và số phận nước Nga...",
            createdAt = "2026-10-31T10:30:00Z"
        )

        assertEquals(5L, bookmark.id)
        assertEquals(2L, bookmark.bookId)
        assertEquals(392, bookmark.pageNumber)
        assertEquals("Trang 392", bookmark.title)
        assertNotNull(bookmark.snippet)
        assertTrue(bookmark.snippet!!.contains("Austerlitz"))
    }

    @Test
    fun readingStats_computesHoursCorrectly() {
        val stats = FolioHavenDbHelper.ReadingStats(
            totalBooks = 4,
            finishedBooks = 2,
            totalReadingMinutes = 125,
            readingStreakDays = 7
        )

        assertEquals(4, stats.totalBooks)
        assertEquals(2, stats.finishedBooks)
        assertEquals(125, stats.totalReadingMinutes)
        assertEquals(2, stats.totalReadingMinutes / 60)
        assertEquals(7, stats.readingStreakDays)
    }
}
