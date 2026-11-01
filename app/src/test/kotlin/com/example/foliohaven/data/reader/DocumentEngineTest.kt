package com.example.foliohaven.data.reader

import com.example.foliohaven.data.model.BookRecord
import com.example.foliohaven.data.model.ReaderTheme
import com.example.foliohaven.toBookItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DocumentEngineTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun plainTextDocumentReader_chunksIntoPagesAndDetectsChapters() {
        val file = tempFolder.newFile("sample_chapters.txt")
        val content = buildString {
            append("CHƯƠNG I: KHOA HỌC SUY LUẬN\n\n")
            for (i in 1..50) {
                append("Sherlock Holmes quan sát thấy các chi tiết tinh vi số $i. ")
            }
            append("\n\nCHƯƠNG II: VỤ ÁN DẤU BỘ TỨ\n\n")
            for (i in 1..50) {
                append("Bác sĩ Watson ghi chép lại lời khai của nhân chứng số $i. ")
            }
        }
        file.writeText(content)

        val reader = PlainTextDocumentReader(file, charsPerPage = 400)
        assertTrue("Pages should be greater than 1", reader.totalPages > 1)

        val toc = reader.tableOfContents
        assertTrue("TOC should contain detected chapters", toc.isNotEmpty())

        val firstPage = reader.getPage(0)
        assertTrue(firstPage is PageContent.Text)
        val textPage = firstPage as PageContent.Text
        assertTrue(textPage.content.contains("Sherlock Holmes"))

        // In-book search
        val searchResults = reader.search("Watson")
        assertFalse("Search should find Watson", searchResults.isEmpty())
        assertEquals("Watson", searchResults.first().query)
        assertTrue(searchResults.first().snippet.contains("Watson"))
        reader.close()
    }

    @Test
    fun epubDocumentReader_parsesZipHtmlAndToc() {
        val file = tempFolder.newFile("sample.epub")
        val zipOut = ZipOutputStream(FileOutputStream(file))

        // 1. Chapter 1 HTML
        zipOut.putNextEntry(ZipEntry("OEBPS/chapter1.xhtml"))
        val chap1Html = """
            <?xml version="1.0" encoding="utf-8"?>
            <html>
            <head><title>Chương 1: Khởi đầu</title></head>
            <body>
                <h1>Chương 1: Khởi đầu</h1>
                <p>Đây là nội dung chương đầu tiên của sách điện tử FolioHaven.</p>
            </body>
            </html>
        """.trimIndent()
        zipOut.write(chap1Html.toByteArray(Charsets.UTF_8))
        zipOut.closeEntry()

        // 2. Chapter 2 HTML
        zipOut.putNextEntry(ZipEntry("OEBPS/chapter2.xhtml"))
        val chap2Html = """
            <?xml version="1.0" encoding="utf-8"?>
            <html>
            <head><title>Chương 2: Thử thách</title></head>
            <body>
                <h1>Chương 2: Thử thách</h1>
                <p>Những thử thách vĩ đại luôn chờ đón người kiên định.</p>
            </body>
            </html>
        """.trimIndent()
        zipOut.write(chap2Html.toByteArray(Charsets.UTF_8))
        zipOut.closeEntry()

        // 3. toc.ncx
        zipOut.putNextEntry(ZipEntry("OEBPS/toc.ncx"))
        val tocNcx = """
            <?xml version="1.0" encoding="UTF-8"?>
            <ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
                <navMap>
                    <navPoint id="np1"><navLabel><text>Chương 1: Khởi đầu</text></navLabel><content src="chapter1.xhtml"/></navPoint>
                    <navPoint id="np2"><navLabel><text>Chương 2: Thử thách</text></navLabel><content src="chapter2.xhtml"/></navPoint>
                </navMap>
            </ncx>
        """.trimIndent()
        zipOut.write(tocNcx.toByteArray(Charsets.UTF_8))
        zipOut.closeEntry()

        zipOut.close()

        val reader = EpubDocumentReader(file)
        assertEquals(2, reader.totalPages)
        assertEquals(2, reader.tableOfContents.size)
        assertEquals("Chương 1: Khởi đầu", reader.tableOfContents[0].title)

        val page1 = reader.getPage(0) as PageContent.Text
        assertTrue(page1.content.contains("FolioHaven"))

        val searchRes = reader.search("FolioHaven")
        assertEquals(1, searchRes.size)
        assertEquals(1, searchRes[0].pageIndex)
        reader.close()
    }

    @Test
    fun mobiDocumentReader_decompressesPalmDocLz77() {
        val file = tempFolder.newFile("sample.mobi")
        val stream = ByteArrayOutputStream()

        // 1. PDB Header (78 bytes)
        val pdbHeader = ByteArray(78)
        val buf = ByteBuffer.wrap(pdbHeader).order(ByteOrder.BIG_ENDIAN)
        // Set book name
        val nameBytes = "SampleMobiBook".toByteArray(Charsets.US_ASCII)
        System.arraycopy(nameBytes, 0, pdbHeader, 0, nameBytes.size)
        // numRecords at offset 76: 2 records (record 0 = header, record 1 = compressed text)
        buf.putShort(76, 2.toShort())
        stream.write(pdbHeader)

        // 2. Record info headers: 2 records * 8 bytes = 16 bytes
        // Record 0 offset = 78 + 16 = 94
        val rec0Offset = 94
        val rec1Offset = rec0Offset + 16 // PalmDOC header is 16 bytes
        val rec0Buf = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN)
        rec0Buf.putInt(rec0Offset)
        rec0Buf.putInt(0) // attributes
        stream.write(rec0Buf.array())

        val rec1Buf = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN)
        rec1Buf.putInt(rec1Offset)
        rec1Buf.putInt(0)
        stream.write(rec1Buf.array())

        // 3. Record 0: PalmDOC Header (16 bytes)
        val docHeader = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN)
        docHeader.putShort(2.toShort()) // compression = 2 (PalmDOC LZ77)
        docHeader.putShort(0.toShort()) // unused
        docHeader.putInt(50) // text length
        docHeader.putShort(1.toShort()) // record count = 1
        docHeader.putShort(4096.toShort()) // record size
        docHeader.putInt(0) // encryption
        stream.write(docHeader.array())

        // 4. Record 1: PalmDOC LZ77 compressed text
        // Plaintext: "CHƯƠNG I: FolioHaven MOBI Reader. Tĩnh tâm đọc sách."
        // Using literal run for test bytes
        val textBytes = "CHƯƠNG I: FolioHaven MOBI Reader. Tĩnh tâm đọc sách.".toByteArray(Charsets.UTF_8)
        val lz77Stream = ByteArrayOutputStream()
        // Write as single byte literals (0x09..0x7F) or UTF-8 runs
        var p = 0
        while (p < textBytes.size) {
            val chunkLen = (textBytes.size - p).coerceAtMost(8)
            lz77Stream.write(chunkLen) // 0x01..0x08 indicates run of N bytes
            for (k in 0 until chunkLen) {
                lz77Stream.write(textBytes[p++].toInt() and 0xFF)
            }
        }
        stream.write(lz77Stream.toByteArray())

        file.writeBytes(stream.toByteArray())

        val reader = MobiDocumentReader(file)
        assertTrue("MOBI totalPages should be >= 1", reader.totalPages >= 1)

        val page1 = reader.getPage(0)
        assertTrue(page1 is PageContent.Text)
        val textPage = page1 as PageContent.Text
        assertTrue(textPage.content.contains("FolioHaven MOBI Reader"))

        val searchMatches = reader.search("MOBI")
        assertFalse("Search matches should not be empty", searchMatches.isEmpty())
        reader.close()
    }

    @Test
    fun extractSnippet_extractsContextCleanly() {
        val text = "FolioHaven is an offline distraction-free archival reader designed for calm literature consumption."
        val snippet = extractSnippet(text, "archival")
        assertTrue(snippet.contains("archival"))
        assertTrue(snippet.contains("reader"))
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
            progressPercent = 0.25f,
            isFinished = false
        )

        val item = record.toBookItem()
        assertEquals("42", item.id)
        assertEquals("The Sign of Four", item.title)
        assertEquals("Arthur Conan Doyle", item.author)
        assertEquals("EPUB", item.format)
        assertEquals("Tiểu thuyết", item.category)
        assertEquals(0.25f, item.progress)
        assertEquals(5, item.currentPage)
        assertEquals(20, item.totalPages)
        assertFalse(item.isFinished)
    }
}
