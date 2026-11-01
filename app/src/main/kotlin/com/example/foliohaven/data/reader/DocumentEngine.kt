package com.example.foliohaven.data.reader

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class ChapterItem(
    val title: String,
    val pageIndex: Int
)

data class SearchResultItem(
    val pageIndex: Int,
    val chapterTitle: String,
    val snippet: String,
    val query: String
)

sealed interface PageContent {
    data class Text(
        val chapterTitle: String,
        val content: String,
        val pageIndex: Int,
        val totalPages: Int
    ) : PageContent

    data class PdfPage(
        val bitmap: Bitmap,
        val pageIndex: Int,
        val totalPages: Int
    ) : PageContent

    data class Error(val message: String) : PageContent
}

interface DocumentReader : AutoCloseable {
    val totalPages: Int
    val tableOfContents: List<ChapterItem>
    fun getPage(index: Int, targetWidth: Int = 1080): PageContent
    fun search(query: String): List<SearchResultItem> = emptyList()
}

internal fun extractSnippet(content: String, query: String, snippetLength: Int = 80): String {
    val idx = content.indexOf(query, ignoreCase = true)
    if (idx < 0) return ""
    val start = (idx - 30).coerceAtLeast(0)
    val end = (idx + query.length + 50).coerceAtMost(content.length)
    val prefix = if (start > 0) "... " else ""
    val suffix = if (end < content.length) " ..." else ""
    return prefix + content.substring(start, end).replace('\n', ' ').trim() + suffix
}

internal fun stripHtmlTags(html: String): String {
    return html
        .replace("(?i)<br\\s*/?>".toRegex(), "\n")
        .replace("(?i)</p>".toRegex(), "\n\n")
        .replace("(?i)</div>".toRegex(), "\n")
        .replace("(?i)</h1>|</h2>|</h3>".toRegex(), "\n\n")
        .replace("<[^>]*>".toRegex(), "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .trim()
}

// ----------------------------------------------------------------------------
// 1. Native Android PDF Engine
// ----------------------------------------------------------------------------
class AndroidPdfDocumentReader(private val file: File) : DocumentReader {
    private var pfd: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null

    override var totalPages: Int = 1
        private set

    override val tableOfContents: List<ChapterItem>
        get() {
            val list = mutableListOf<ChapterItem>()
            val step = if (totalPages > 50) 10 else if (totalPages > 20) 5 else 1
            for (p in 1..totalPages step step) {
                list.add(ChapterItem("Trang $p", p))
            }
            if (list.lastOrNull()?.pageIndex != totalPages && totalPages > 1) {
                list.add(ChapterItem("Trang $totalPages", totalPages))
            }
            return list
        }

    init {
        try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pfd?.let { descriptor ->
                renderer = PdfRenderer(descriptor)
                totalPages = renderer?.pageCount?.coerceAtLeast(1) ?: 1
            }
        } catch (_: Exception) {
            totalPages = 1
        }
    }

    override fun getPage(index: Int, targetWidth: Int): PageContent {
        val r = renderer ?: return PageContent.Error("Trình đọc PDF chưa khởi tạo")
        val pageIdx = index.coerceIn(0, (totalPages - 1).coerceAtLeast(0))

        return try {
            val page = r.openPage(pageIdx)
            val aspect = page.height.toFloat() / page.width.toFloat()
            val width = targetWidth.coerceIn(400, 2160)
            val height = (width * aspect).toInt().coerceIn(400, 3840)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(AndroidColor.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            PageContent.PdfPage(bitmap, pageIdx + 1, totalPages)
        } catch (e: Exception) {
            PageContent.Error("Không thể hiển thị trang PDF ${pageIdx + 1}: ${e.message}")
        }
    }

    override fun search(query: String): List<SearchResultItem> {
        // Simple page number jump support for PDF
        val pageNum = query.trim().toIntOrNull()
        if (pageNum != null && pageNum in 1..totalPages) {
            return listOf(
                SearchResultItem(
                    pageIndex = pageNum,
                    chapterTitle = "Trang $pageNum",
                    snippet = "Nhảy đến trang $pageNum của tài liệu PDF",
                    query = query
                )
            )
        }
        return emptyList()
    }

    override fun close() {
        try {
            renderer?.close()
            pfd?.close()
        } catch (_: Exception) {}
        renderer = null
        pfd = null
    }
}

// ----------------------------------------------------------------------------
// 2. Plain Text (.TXT) Reflow Engine with Chapter Detection & Search
// ----------------------------------------------------------------------------
class PlainTextDocumentReader(file: File, private val charsPerPage: Int = 1400) : DocumentReader {
    private val pages = mutableListOf<String>()
    private val pageChapterTitles = mutableListOf<String>()
    private val toc = mutableListOf<ChapterItem>()

    override val totalPages: Int
        get() = pages.size.coerceAtLeast(1)

    override val tableOfContents: List<ChapterItem>
        get() = toc.ifEmpty {
            listOf(ChapterItem("Bắt đầu đọc", 1))
        }

    init {
        try {
            val fullText = file.readText(Charsets.UTF_8)
            val words = fullText.split("\\s+".toRegex())
            val currentChunk = StringBuilder()
            val chapterRegex = Pattern.compile("(?i)^\\s*(CHƯƠNG|PHẦN|BÀI|MỤC|CHAPTER|PART|ACT|BOOK)\\s+([0-9IVXLCDM]+|[A-ZÀ-Ỹ\\s]+).*", Pattern.MULTILINE)

            var currentChapter = "Nội dung mở đầu"

            for (word in words) {
                if (currentChunk.length + word.length + 1 > charsPerPage && currentChunk.isNotEmpty()) {
                    val pageText = currentChunk.toString().trim()
                    pages.add(pageText)
                    pageChapterTitles.add(currentChapter)
                    currentChunk.clear()
                }
                if (currentChunk.isNotEmpty()) currentChunk.append(" ")
                currentChunk.append(word)

                // Detect chapter heading in accumulated text
                val matcher = chapterRegex.matcher(word)
                if (matcher.find()) {
                    val matchStr = matcher.group().trim()
                    if (matchStr.length > 3) {
                        currentChapter = matchStr.take(30)
                        val targetPageIndex = pages.size + 1
                        if (toc.none { it.title.equals(currentChapter, ignoreCase = true) }) {
                            toc.add(ChapterItem(currentChapter, targetPageIndex))
                        }
                    }
                }
            }
            if (currentChunk.isNotEmpty()) {
                pages.add(currentChunk.toString().trim())
                pageChapterTitles.add(currentChapter)
            }
        } catch (e: Exception) {
            pages.add("Lỗi khi tải nội dung văn bản: ${e.message}")
            pageChapterTitles.add("Lỗi")
        }
        if (pages.isEmpty()) {
            pages.add("Tài liệu trống")
            pageChapterTitles.add("Trang 1")
        }

        // If TOC is empty, create section bookmarks
        if (toc.isEmpty()) {
            val step = if (totalPages > 40) 10 else if (totalPages > 15) 5 else 1
            for (p in 1..totalPages step step) {
                toc.add(ChapterItem("Trang $p", p))
            }
        }
    }

    override fun getPage(index: Int, targetWidth: Int): PageContent {
        val safeIndex = index.coerceIn(0, pages.size - 1)
        val text = pages[safeIndex]
        val title = pageChapterTitles.getOrElse(safeIndex) { "Trang ${safeIndex + 1}" }
        return PageContent.Text(
            chapterTitle = title,
            content = text,
            pageIndex = safeIndex + 1,
            totalPages = totalPages
        )
    }

    override fun search(query: String): List<SearchResultItem> {
        if (query.isBlank()) return emptyList()
        val results = mutableListOf<SearchResultItem>()
        for (i in pages.indices) {
            val pageText = pages[i]
            if (pageText.contains(query, ignoreCase = true)) {
                val snippet = extractSnippet(pageText, query)
                val title = pageChapterTitles.getOrElse(i) { "Trang ${i + 1}" }
                results.add(
                    SearchResultItem(
                        pageIndex = i + 1,
                        chapterTitle = title,
                        snippet = snippet,
                        query = query
                    )
                )
            }
        }
        return results
    }

    override fun close() {}
}

// ----------------------------------------------------------------------------
// 3. Clean-Room EPUB Reflow Engine (With TOC and In-Book Search)
// ----------------------------------------------------------------------------
class EpubDocumentReader(private val file: File) : DocumentReader {
    private val chapters = mutableListOf<Pair<String, String>>() // (Title, Text)
    private val toc = mutableListOf<ChapterItem>()

    override val totalPages: Int
        get() = chapters.size.coerceAtLeast(1)

    override val tableOfContents: List<ChapterItem>
        get() = toc.ifEmpty {
            chapters.mapIndexed { idx, pair -> ChapterItem(pair.first, idx + 1) }
        }

    init {
        extractEpubChapters()
    }

    private fun extractEpubChapters() {
        val htmlEntries = mutableMapOf<String, String>()
        var tocNcxContent: String? = null
        var navXhtmlContent: String? = null

        try {
            val zip = ZipInputStream(FileInputStream(file))
            var entry: ZipEntry? = zip.nextEntry

            while (entry != null) {
                val name = entry.name
                if (!entry.isDirectory) {
                    val lower = name.lowercase()
                    if (lower.endsWith(".html") || lower.endsWith(".xhtml") || lower.endsWith(".htm")) {
                        val content = BufferedReader(InputStreamReader(zip, Charsets.UTF_8)).readText()
                        htmlEntries[name] = content
                        if (lower.contains("nav.") || lower.contains("toc.")) {
                            navXhtmlContent = content
                        }
                    } else if (lower.endsWith(".ncx")) {
                        tocNcxContent = BufferedReader(InputStreamReader(zip, Charsets.UTF_8)).readText()
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
            zip.close()

            // Sort entries naturally
            val sortedKeys = htmlEntries.keys.sorted()
            var chapterIndex = 1
            for (key in sortedKeys) {
                val rawHtml = htmlEntries[key] ?: continue
                val cleanText = stripHtmlTags(rawHtml)
                if (cleanText.isNotBlank()) {
                    // Try to detect chapter title from <h1>, <h2> or <title>
                    val titleMatch = Regex("(?i)<(h1|h2|title)[^>]*>(.*?)</\\1>").find(rawHtml)
                    val rawTitle = titleMatch?.groupValues?.get(2)?.let { stripHtmlTags(it).trim() }
                    val finalTitle = if (!rawTitle.isNullOrBlank() && rawTitle.length < 60) {
                        rawTitle
                    } else {
                        "Chương $chapterIndex"
                    }

                    chapters.add(finalTitle to cleanText)
                    toc.add(ChapterItem(finalTitle, chapterIndex))
                    chapterIndex++
                }
            }

            // If NCX TOC was found, parse navLabel texts for better chapter names
            if (tocNcxContent != null) {
                val ncxTitles = mutableListOf<String>()
                val navPointRegex = Regex("(?s)<text>\\s*([^<]+?)\\s*</text>")
                navPointRegex.findAll(tocNcxContent).forEach { match ->
                    val t = match.groupValues[1].trim()
                    if (t.isNotBlank() && t.length < 80) {
                        ncxTitles.add(t)
                    }
                }
                if (ncxTitles.isNotEmpty() && ncxTitles.size <= chapters.size) {
                    toc.clear()
                    ncxTitles.forEachIndexed { i, title ->
                        toc.add(ChapterItem(title, i + 1))
                    }
                }
            }
        } catch (e: Exception) {
            chapters.add("Lỗi" to "Không thể mở tệp EPUB: ${e.message}")
        }

        if (chapters.isEmpty()) {
            chapters.add("Thông báo" to "Tài liệu EPUB không chứa nội dung văn bản.")
            toc.add(ChapterItem("Trang 1", 1))
        }
    }

    override fun getPage(index: Int, targetWidth: Int): PageContent {
        val safeIndex = index.coerceIn(0, chapters.size - 1)
        val (title, content) = chapters[safeIndex]
        return PageContent.Text(
            chapterTitle = title,
            content = content,
            pageIndex = safeIndex + 1,
            totalPages = totalPages
        )
    }

    override fun search(query: String): List<SearchResultItem> {
        if (query.isBlank()) return emptyList()
        val results = mutableListOf<SearchResultItem>()
        for (i in chapters.indices) {
            val (title, content) = chapters[i]
            if (content.contains(query, ignoreCase = true)) {
                val snippet = extractSnippet(content, query)
                results.add(
                    SearchResultItem(
                        pageIndex = i + 1,
                        chapterTitle = title,
                        snippet = snippet,
                        query = query
                    )
                )
            }
        }
        return results
    }

    override fun close() {}
}

// ----------------------------------------------------------------------------
// 4. Clean-Room MOBI / AZW Reflow Engine (PalmDOC LZ77 Decompressor)
// ----------------------------------------------------------------------------
class MobiDocumentReader(private val file: File, private val charsPerPage: Int = 1400) : DocumentReader {
    private val pages = mutableListOf<String>()
    private val pageChapterTitles = mutableListOf<String>()
    private val toc = mutableListOf<ChapterItem>()

    override val totalPages: Int
        get() = pages.size.coerceAtLeast(1)

    override val tableOfContents: List<ChapterItem>
        get() = toc.ifEmpty {
            listOf(ChapterItem("Trang 1", 1))
        }

    init {
        extractMobiContent()
    }

    private fun extractMobiContent() {
        try {
            val bytes = file.readBytes()
            if (bytes.size < 78) {
                pages.add("Tệp MOBI không hợp lệ (kích thước quá nhỏ)")
                pageChapterTitles.add("Lỗi")
                return
            }

            // Palm Database (PDB) Header
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
            val numRecords = buffer.getShort(76).toInt() and 0xFFFF

            if (numRecords <= 1 || bytes.size < 78 + numRecords * 8) {
                // Fallback to text string extraction
                fallbackExtractText(bytes)
                return
            }

            // Read record offsets
            val recordOffsets = LongArray(numRecords)
            for (i in 0 until numRecords) {
                recordOffsets[i] = buffer.getInt(78 + i * 8).toLong() and 0xFFFFFFFFL
            }

            // Record 0 holds PalmDOC header
            val rec0Offset = recordOffsets[0].toInt()
            if (rec0Offset + 12 > bytes.size) {
                fallbackExtractText(bytes)
                return
            }

            val compression = buffer.getShort(rec0Offset).toInt() and 0xFFFF
            val textRecordCount = buffer.getShort(rec0Offset + 8).toInt() and 0xFFFF

            val decompressedStream = ByteArrayOutputStream()

            // Records 1 to textRecordCount contain the text chunks
            val maxTextRecords = textRecordCount.coerceAtMost(numRecords - 1)
            for (r in 1..maxTextRecords) {
                val start = recordOffsets[r].toInt()
                val end = if (r + 1 < numRecords) recordOffsets[r + 1].toInt() else bytes.size
                if (start in bytes.indices && end > start && end <= bytes.size) {
                    val chunk = bytes.copyOfRange(start, end)
                    when (compression) {
                        1 -> {
                            // Uncompressed
                            decompressedStream.write(chunk)
                        }
                        2 -> {
                            // PalmDOC LZ77 Decompression
                            decompressPalmDoc(chunk, decompressedStream)
                        }
                        else -> {
                            // Unsupported compression (e.g. Huff/CDIC) -> try raw text
                            decompressedStream.write(chunk)
                        }
                    }
                }
            }

            val fullRawText = try {
                decompressedStream.toString(Charsets.UTF_8.name())
            } catch (_: Exception) {
                decompressedStream.toString("ISO-8859-1")
            }

            val cleanText = stripHtmlTags(fullRawText)
            if (cleanText.isNotBlank()) {
                paginateText(cleanText)
            } else {
                fallbackExtractText(bytes)
            }
        } catch (e: Exception) {
            pages.add("Lỗi giải nén tệp MOBI: ${e.message}")
            pageChapterTitles.add("Lỗi")
        }

        if (pages.isEmpty()) {
            pages.add("Tài liệu MOBI không chứa nội dung văn bản.")
            pageChapterTitles.add("Trang 1")
            toc.add(ChapterItem("Trang 1", 1))
        }
    }

    /**
     * PalmDOC LZ77 decompressor:
     * 0x00: uncompressed literal 0x00
     * 0x01..0x08: literal run of N bytes
     * 0x09..0x7F: literal character (single byte)
     * 0x80..0xBF: 2-byte distance/length pair
     * 0xC0..0xFF: space + char (byte ^ 0x80)
     */
    private fun decompressPalmDoc(src: ByteArray, out: ByteArrayOutputStream) {
        var p = 0
        while (p < src.size) {
            val b = src[p++].toInt() and 0xFF
            when {
                b == 0x00 -> out.write(0x00)
                b in 0x01..0x08 -> {
                    for (k in 0 until b) {
                        if (p < src.size) out.write(src[p++].toInt() and 0xFF)
                    }
                }
                b in 0x09..0x7F -> out.write(b)
                b in 0x80..0xBF -> {
                    if (p < src.size) {
                        val b2 = src[p++].toInt() and 0xFF
                        val dist = ((b and 0x3F) shl 3) or (b2 shr 5)
                        val len = (b2 and 0x07) + 3
                        val currentBuffer = out.toByteArray()
                        val copyStart = currentBuffer.size - dist
                        for (k in 0 until len) {
                            val idx = copyStart + k
                            if (idx in currentBuffer.indices) {
                                out.write(currentBuffer[idx].toInt() and 0xFF)
                            }
                        }
                    }
                }
                b >= 0xC0 -> {
                    out.write(' '.code)
                    out.write(b xor 0x80)
                }
            }
        }
    }

    private fun fallbackExtractText(bytes: ByteArray) {
        val str = bytes.toString(Charsets.UTF_8).filter { it.isLetterOrDigit() || it.isWhitespace() || it in ".,;:!?'-–—\"\"''" }
        paginateText(str.ifBlank { "Không thể trích xuất văn bản từ tệp MOBI." })
    }

    private fun paginateText(text: String) {
        val words = text.split("\\s+".toRegex())
        val currentChunk = StringBuilder()
        var currentChapter = "Mở đầu MOBI"

        for (word in words) {
            if (currentChunk.length + word.length + 1 > charsPerPage && currentChunk.isNotEmpty()) {
                pages.add(currentChunk.toString().trim())
                pageChapterTitles.add(currentChapter)
                currentChunk.clear()
            }
            if (currentChunk.isNotEmpty()) currentChunk.append(" ")
            currentChunk.append(word)

            if (word.startsWith("CHƯƠNG", true) || word.startsWith("CHAPTER", true) || word.startsWith("PHẦN", true)) {
                currentChapter = word.take(24)
                val targetPageIndex = pages.size + 1
                if (toc.none { it.title.equals(currentChapter, ignoreCase = true) }) {
                    toc.add(ChapterItem(currentChapter, targetPageIndex))
                }
            }
        }
        if (currentChunk.isNotEmpty()) {
            pages.add(currentChunk.toString().trim())
            pageChapterTitles.add(currentChapter)
        }

        if (toc.isEmpty()) {
            val step = if (totalPages > 40) 10 else if (totalPages > 15) 5 else 1
            for (p in 1..totalPages step step) {
                toc.add(ChapterItem("Trang $p", p))
            }
        }
    }

    override fun getPage(index: Int, targetWidth: Int): PageContent {
        val safeIndex = index.coerceIn(0, pages.size - 1)
        val text = pages[safeIndex]
        val title = pageChapterTitles.getOrElse(safeIndex) { "Trang ${safeIndex + 1}" }
        return PageContent.Text(
            chapterTitle = title,
            content = text,
            pageIndex = safeIndex + 1,
            totalPages = totalPages
        )
    }

    override fun search(query: String): List<SearchResultItem> {
        if (query.isBlank()) return emptyList()
        val results = mutableListOf<SearchResultItem>()
        for (i in pages.indices) {
            val pageText = pages[i]
            if (pageText.contains(query, ignoreCase = true)) {
                val snippet = extractSnippet(pageText, query)
                val title = pageChapterTitles.getOrElse(i) { "Trang ${i + 1}" }
                results.add(
                    SearchResultItem(
                        pageIndex = i + 1,
                        chapterTitle = title,
                        snippet = snippet,
                        query = query
                    )
                )
            }
        }
        return results
    }

    override fun close() {}
}

// ----------------------------------------------------------------------------
// 5. Document Reader Factory
// ----------------------------------------------------------------------------
object DocumentEngineFactory {
    fun openDocument(filePath: String, format: String): DocumentReader {
        val file = File(filePath)
        return when (format.uppercase()) {
            "PDF" -> AndroidPdfDocumentReader(file)
            "EPUB" -> EpubDocumentReader(file)
            "MOBI", "AZW", "AZW3" -> MobiDocumentReader(file)
            else -> PlainTextDocumentReader(file)
        }
    }
}
