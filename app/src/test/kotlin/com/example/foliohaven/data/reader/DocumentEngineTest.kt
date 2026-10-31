package com.example.foliohaven.data.reader

import com.example.foliohaven.data.model.BookRecord
import com.example.foliohaven.data.model.ReaderTheme
import com.example.foliohaven.toBookItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DocumentEngineTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun plainTextDocumentReader_chunksIntoPages() {
        val file = tempFolder.newFile("sample.txt")
        val content = buildString {
            for (i in 1..200) {
                append("Paragraph $i of test document for FolioHaven e-reader. ")
            }
        }
        file.writeText(content)

        val reader = PlainTextDocumentReader(file, charsPerPage = 500)
        assertTrue("Pages should be greater than 1", reader.totalPages > 1)

        val firstPage = reader.getPage(0)
        assertTrue(firstPage is PageContent.Text)
        val textPage = firstPage as PageContent.Text
        assertEquals("Trang 1", textPage.chapterTitle)
        assertTrue(textPage.content.contains("Paragraph 1"))
        assertEquals(reader.totalPages, textPage.totalPages)
        reader.close()
    }

    @Test
    fun readerTheme_hasDefinedPalettes() {
        assertEquals("Ban ngày", ReaderTheme.DAY.title)
        assertEquals("Parchment", ReaderTheme.SEPIA.title)
        assertEquals("OLED Đêm", ReaderTheme.NIGHT.title)
        assertEquals("Sepia Tối", ReaderTheme.DARK_SEPIA.title)
    }

    @Test
    fun bookRecord_mapsCorrectlyToBookItem() {
        val record = BookRecord(
            id = 42L,
            title = "The Sign of Four",
            author = "Arthur Conan Doyle",
            description = "Detective story",
            filePath = "/path/to/book.epub",
            fileSize = 1024L,
            fileHash = "abc123hash",
            format = "EPUB",
            coverPath = null,
            coverGradientStart = 0xFF1E293BL,
            coverGradientEnd = 0xFF0F172AL,
            category = "Tiểu thuyết",
            currentPage = 5,
            totalPages = 20,
            progressPercent = 0.25f
        )

        val item = record.toBookItem()
        assertEquals("42", item.id)
        assertEquals("The Sign of Four", item.title)
        assertEquals("Arthur Conan Doyle", item.author)
        assertEquals("EPUB", item.format)
        assertEquals(0.25f, item.progress)
        assertEquals(5, item.currentPage)
        assertEquals(20, item.totalPages)
    }
}
