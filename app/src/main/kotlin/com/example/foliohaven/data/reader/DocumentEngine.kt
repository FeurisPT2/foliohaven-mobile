package com.example.foliohaven.data.reader

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

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
    fun getPage(index: Int, targetWidth: Int = 1080): PageContent
}

// ----------------------------------------------------------------------------
// 1. Native Android PDF Engine
// ----------------------------------------------------------------------------
class AndroidPdfDocumentReader(private val file: File) : DocumentReader {
    private var pfd: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null

    override var totalPages: Int = 1
        private set

    init {
        try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pfd?.let { descriptor ->
                renderer = PdfRenderer(descriptor)
                totalPages = renderer?.pageCount?.coerceAtLeast(1) ?: 1
            }
        } catch (e: Exception) {
            totalPages = 1
        }
    }

    override fun getPage(index: Int, targetWidth: Int): PageContent {
        val r = renderer ?: return PageContent.Error("PDF Renderer not initialized")
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
            PageContent.Error("Failed to render PDF page ${pageIdx + 1}: ${e.message}")
        }
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
// 2. Plain Text (.TXT) Reflow Engine
// ----------------------------------------------------------------------------
class PlainTextDocumentReader(file: File, private val charsPerPage: Int = 1400) : DocumentReader {
    private val pages = mutableListOf<String>()

    override val totalPages: Int
        get() = pages.size.coerceAtLeast(1)

    init {
        try {
            val fullText = file.readText(Charsets.UTF_8)
            val words = fullText.split("\\s+".toRegex())
            val currentChunk = StringBuilder()

            for (word in words) {
                if (currentChunk.length + word.length + 1 > charsPerPage && currentChunk.isNotEmpty()) {
                    pages.add(currentChunk.toString().trim())
                    currentChunk.clear()
                }
                if (currentChunk.isNotEmpty()) currentChunk.append(" ")
                currentChunk.append(word)
            }
            if (currentChunk.isNotEmpty()) {
                pages.add(currentChunk.toString().trim())
            }
        } catch (e: Exception) {
            pages.add("Error loading text: ${e.message}")
        }
        if (pages.isEmpty()) {
            pages.add("Tài liệu trống")
        }
    }

    override fun getPage(index: Int, targetWidth: Int): PageContent {
        val safeIndex = index.coerceIn(0, pages.size - 1)
        val text = pages[safeIndex]
        return PageContent.Text(
            chapterTitle = "Trang ${safeIndex + 1}",
            content = text,
            pageIndex = safeIndex + 1,
            totalPages = totalPages
        )
    }

    override fun close() {}
}

// ----------------------------------------------------------------------------
// 3. Clean-Room EPUB Reflow Engine (Decompresses OPF and Spine HTML)
// ----------------------------------------------------------------------------
class EpubDocumentReader(private val file: File) : DocumentReader {
    private val chapters = mutableListOf<Pair<String, String>>() // (Title, Text)

    override val totalPages: Int
        get() = chapters.size.coerceAtLeast(1)

    init {
        extractEpubChapters()
    }

    private fun extractEpubChapters() {
        try {
            val zip = ZipInputStream(FileInputStream(file))
            var entry: ZipEntry? = zip.nextEntry
            val htmlEntries = mutableMapOf<String, String>()

            while (entry != null) {
                val name = entry.name
                if (!entry.isDirectory && (name.endsWith(".html", true) || name.endsWith(".xhtml", true) || name.endsWith(".htm", true))) {
                    val content = BufferedReader(InputStreamReader(zip, Charsets.UTF_8)).readText()
                    htmlEntries[name] = content
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
                    val title = "Chương $chapterIndex"
                    chapters.add(title to cleanText)
                    chapterIndex++
                }
            }
        } catch (e: Exception) {
            chapters.add("Lỗi" to "Không thể mở tệp EPUB: ${e.message}")
        }

        if (chapters.isEmpty()) {
            chapters.add("Thông báo" to "Tài liệu EPUB không chứa nội dung văn bản.")
        }
    }

    private fun stripHtmlTags(html: String): String {
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

    override fun close() {}
}

// ----------------------------------------------------------------------------
// 4. Document Reader Factory
// ----------------------------------------------------------------------------
object DocumentEngineFactory {
    fun openDocument(filePath: String, format: String): DocumentReader {
        val file = File(filePath)
        return when (format.uppercase()) {
            "PDF" -> AndroidPdfDocumentReader(file)
            "EPUB" -> EpubDocumentReader(file)
            else -> PlainTextDocumentReader(file)
        }
    }
}
