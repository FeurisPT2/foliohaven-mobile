package com.example.foliohaven.ui.reader

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foliohaven.BookmarkRibbon
import com.example.foliohaven.data.model.AnnotationRecord
import com.example.foliohaven.data.model.BookRecord
import com.example.foliohaven.data.model.BookmarkRecord
import com.example.foliohaven.data.model.ReaderTheme
import com.example.foliohaven.data.reader.ChapterItem
import com.example.foliohaven.data.reader.DocumentEngineFactory
import com.example.foliohaven.data.reader.DocumentReader
import com.example.foliohaven.data.reader.PageContent
import com.example.foliohaven.data.reader.SearchResultItem
import com.example.foliohaven.data.repository.BookRepository
import com.example.foliohaven.ui.theme.BorderCharcoal
import com.example.foliohaven.ui.theme.CardSurface
import com.example.foliohaven.ui.theme.HighlightAmber
import com.example.foliohaven.ui.theme.HighlightAzure
import com.example.foliohaven.ui.theme.HighlightEmerald
import com.example.foliohaven.ui.theme.HighlightRose
import com.example.foliohaven.ui.theme.InputSurface
import com.example.foliohaven.ui.theme.PrimaryHover
import com.example.foliohaven.ui.theme.PrimaryPressed
import com.example.foliohaven.ui.theme.PrimarySubtle
import com.example.foliohaven.ui.theme.PrimaryTerracotta
import com.example.foliohaven.ui.theme.SurfaceEspresso
import com.example.foliohaven.ui.theme.TextDisabled
import com.example.foliohaven.ui.theme.TextLinen
import com.example.foliohaven.ui.theme.TextMuted
import com.example.foliohaven.ui.theme.TextParchment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

fun getHighlightColor(colorId: Int): Color {
    return when (colorId) {
        1 -> HighlightAmber
        2 -> HighlightEmerald
        3 -> HighlightAzure
        4 -> HighlightRose
        else -> HighlightAmber
    }
}

fun getHighlightName(colorId: Int): String {
    return when (colorId) {
        1 -> "Hổ phách"
        2 -> "Ngọc lục bảo"
        3 -> "Da trời"
        4 -> "Hồng hạc"
        else -> "Hổ phách"
    }
}

// ============================================================================
// Vector Icons
// ============================================================================

@Composable
fun SpeakerIcon(isPlaying: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(19.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.15f, size.height * 0.35f)
            lineTo(size.width * 0.38f, size.height * 0.35f)
            lineTo(size.width * 0.65f, size.height * 0.15f)
            lineTo(size.width * 0.65f, size.height * 0.85f)
            lineTo(size.width * 0.38f, size.height * 0.65f)
            lineTo(size.width * 0.15f, size.height * 0.65f)
            close()
        }
        drawPath(path, color = color)
        if (isPlaying) {
            val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = color,
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(size.width * 0.45f, size.height * 0.25f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.45f, size.height * 0.5f),
                style = stroke
            )
        }
    }
}

@Composable
fun HighlightPenIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(19.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.78f)
            lineTo(size.width * 0.35f, size.height * 0.85f)
            lineTo(size.width * 0.82f, size.height * 0.38f)
            lineTo(size.width * 0.62f, size.height * 0.18f)
            lineTo(size.width * 0.15f, size.height * 0.65f)
            close()
        }
        drawPath(path, color = color, style = stroke)
        drawLine(
            color = color,
            start = Offset(size.width * 0.15f, size.height * 0.95f),
            end = Offset(size.width * 0.85f, size.height * 0.95f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun SearchIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(19.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(
            color = color,
            radius = size.width * 0.28f,
            center = Offset(size.width * 0.42f, size.height * 0.42f),
            style = stroke
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.65f, size.height * 0.65f),
            end = Offset(size.width * 0.88f, size.height * 0.88f),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun TocIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(19.dp)) {
        val y1 = size.height * 0.25f
        val y2 = size.height * 0.50f
        val y3 = size.height * 0.75f
        drawCircle(color, radius = 2.dp.toPx(), center = Offset(size.width * 0.15f, y1))
        drawLine(color, start = Offset(size.width * 0.32f, y1), end = Offset(size.width * 0.88f, y1), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color, radius = 2.dp.toPx(), center = Offset(size.width * 0.15f, y2))
        drawLine(color, start = Offset(size.width * 0.32f, y2), end = Offset(size.width * 0.88f, y2), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color, radius = 2.dp.toPx(), center = Offset(size.width * 0.15f, y3))
        drawLine(color, start = Offset(size.width * 0.32f, y3), end = Offset(size.width * 0.88f, y3), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: Long,
    bookRepository: BookRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialPage: Int = 1,
    initialTheme: ReaderTheme = ReaderTheme.SEPIA
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var book by remember { mutableStateOf<BookRecord?>(null) }
    var documentReader by remember { mutableStateOf<DocumentReader?>(null) }
    var currentPage by remember { mutableIntStateOf(initialPage) }
    var totalPages by remember { mutableIntStateOf(1) }
    var pageContent by remember { mutableStateOf<PageContent?>(null) }

    // Reading preferences
    var currentTheme by remember { mutableStateOf(initialTheme) }
    var fontSizeSp by remember { mutableFloatStateOf(16f) }
    var lineHeightMultiplier by remember { mutableFloatStateOf(1.6f) }
    var horizontalMarginDp by remember { mutableIntStateOf(24) }
    var isBookmarked by remember { mutableStateOf(false) }

    // Dialog & Sheet states
    var showDisplaySettingsSheet by remember { mutableStateOf(false) }
    var showTocSheet by remember { mutableStateOf(false) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var showAddAnnotationDialog by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }

    // In-Book Search state
    var searchQueryInput by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResultItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // Text-to-Speech (TTS)
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsPlaying by remember { mutableStateOf(false) }

    // Annotations & Notes
    var pageAnnotations by remember { mutableStateOf<List<AnnotationRecord>>(emptyList()) }
    var allBookAnnotations by remember { mutableStateOf<List<AnnotationRecord>>(emptyList()) }
    var allBookBookmarks by remember { mutableStateOf<List<BookmarkRecord>>(emptyList()) }

    var selectedColorId by remember { mutableIntStateOf(1) } // 1=Amber, 2=Emerald, 3=Azure, 4=Rose
    var noteQuoteInput by remember { mutableStateOf("") }
    var noteTextInput by remember { mutableStateOf("") }

    // Active reading timer tracker
    var sessionStartTime by remember { mutableStateOf(System.currentTimeMillis()) }

    fun flushReadingTime(targetPage: Int = currentPage) {
        val now = System.currentTimeMillis()
        val elapsedSeconds = ((now - sessionStartTime) / 1000).toInt().coerceAtLeast(0)
        sessionStartTime = now
        coroutineScope.launch {
            bookRepository.updateReadingProgress(bookId, targetPage, totalPages, elapsedSeconds)
        }
    }

    // Initialize TTS
    DisposableEffect(context) {
        var speechEngine: TextToSpeech? = null
        speechEngine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                speechEngine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isTtsPlaying = true
                    }
                    override fun onDone(utteranceId: String?) {
                        isTtsPlaying = false
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        isTtsPlaying = false
                    }
                })
            }
        }
        tts = speechEngine
        onDispose {
            speechEngine.stop()
            speechEngine.shutdown()
        }
    }

    // Refresh annotations & bookmarks for this book
    suspend fun refreshBookAnnotationsAndBookmarks() {
        val annotations = bookRepository.getAnnotations(bookId)
        val bookmarks = bookRepository.getBookmarks(bookId)
        allBookAnnotations = annotations
        allBookBookmarks = bookmarks
        pageAnnotations = annotations.filter { it.pageNumber == currentPage }
        isBookmarked = bookmarks.any { it.pageNumber == currentPage }
    }

    // Load page content
    fun loadPage(targetPage: Int) {
        val safePage = targetPage.coerceIn(1, totalPages)
        flushReadingTime(safePage)
        currentPage = safePage

        // Stop TTS when navigating pages
        if (isTtsPlaying) {
            tts?.stop()
            isTtsPlaying = false
        }

        coroutineScope.launch {
            scrollState.scrollTo(0)
            val reader = documentReader ?: return@launch
            val content = withContext(Dispatchers.IO) {
                reader.getPage(safePage - 1)
            }
            pageContent = content
            bookRepository.updateReadingProgress(bookId, safePage, totalPages)
            refreshBookAnnotationsAndBookmarks()
        }
    }

    // 1. Load book metadata and initialize document engine
    LaunchedEffect(bookId) {
        val loadedBook = bookRepository.getBookById(bookId)
        book = loadedBook
        if (loadedBook != null) {
            val startPage = if (initialPage > 1) initialPage else loadedBook.currentPage.coerceAtLeast(1)
            currentPage = startPage
            withContext(Dispatchers.IO) {
                try {
                    val reader = DocumentEngineFactory.openDocument(loadedBook.filePath, loadedBook.format)
                    documentReader = reader
                    totalPages = reader.totalPages.coerceAtLeast(1)
                    val content = reader.getPage(startPage - 1)
                    withContext(Dispatchers.Main) {
                        pageContent = content
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        pageContent = PageContent.Error("Không thể đọc tệp: ${e.message}")
                    }
                }
            }
            refreshBookAnnotationsAndBookmarks()
        }
    }

    // 2. Clean up reader on exit and record reading time
    DisposableEffect(bookId) {
        onDispose {
            val elapsed = ((System.currentTimeMillis() - sessionStartTime) / 1000).toInt().coerceAtLeast(0)
            if (elapsed > 1) {
                coroutineScope.launch(NonCancellable + Dispatchers.IO) {
                    bookRepository.updateReadingProgress(bookId, currentPage, totalPages, elapsed)
                }
            }
            documentReader?.close()
        }
    }

    fun toggleTts() {
        val engine = tts ?: return
        if (isTtsPlaying) {
            engine.stop()
            isTtsPlaying = false
        } else {
            val textToRead = when (val c = pageContent) {
                is PageContent.Text -> "${c.chapterTitle}. ${c.content}"
                else -> null
            }
            if (!textToRead.isNullOrBlank()) {
                val viLocale = Locale.forLanguageTag("vi-VN")
                if (engine.isLanguageAvailable(viLocale) >= TextToSpeech.LANG_AVAILABLE) {
                    engine.language = viLocale
                } else {
                    engine.language = Locale.getDefault()
                }
                engine.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "FolioHavenTTS_${System.currentTimeMillis()}")
                isTtsPlaying = true
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = currentTheme.backgroundColor,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceEspresso,
                    titleContentColor = TextParchment,
                    navigationIconContentColor = TextParchment,
                    actionIconContentColor = TextParchment
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 22.sp, color = TextParchment, fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = book?.title ?: "Đang tải sách...",
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            color = TextParchment,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = book?.author ?: "",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = TextLinen,
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    // 1. In-Book Search Button
                    IconButton(onClick = { showSearchSheet = true }) {
                        SearchIcon(color = TextParchment)
                    }

                    // 2. Table of Contents (TOC) Button
                    IconButton(onClick = { showTocSheet = true }) {
                        TocIcon(color = TextParchment)
                    }

                    // 3. TTS Read Aloud Button
                    IconButton(onClick = { toggleTts() }) {
                        SpeakerIcon(
                            isPlaying = isTtsPlaying,
                            color = if (isTtsPlaying) PrimaryHover else TextParchment
                        )
                    }

                    // 4. Highlight & Note Button
                    IconButton(onClick = {
                        if (noteQuoteInput.isBlank()) {
                            when (val c = pageContent) {
                                is PageContent.Text -> {
                                    noteQuoteInput = c.content.take(120).trim() + "..."
                                }
                                else -> {
                                    noteQuoteInput = "Ghi chú tại trang $currentPage"
                                }
                            }
                        }
                        showAddAnnotationDialog = true
                    }) {
                        HighlightPenIcon(color = TextParchment)
                    }

                    // 5. Bookmarks & Notes List Sheet Button
                    IconButton(onClick = { showBookmarksSheet = true }) {
                        Text("📑", fontSize = 15.sp)
                    }

                    // 6. Display Settings (Aa: Font, Spacing, Themes, Margins)
                    IconButton(onClick = { showDisplaySettingsSheet = true }) {
                        Text("Aa", fontSize = 15.sp, color = PrimaryHover, fontWeight = FontWeight.Bold)
                    }

                    // 7. Bookmark Ribbon Toggle
                    IconButton(onClick = {
                        coroutineScope.launch {
                            if (isBookmarked) {
                                val currentBm = allBookBookmarks.find { it.pageNumber == currentPage }
                                if (currentBm != null) {
                                    bookRepository.deleteBookmark(currentBm.id)
                                }
                            } else {
                                val snippetText = when (val c = pageContent) {
                                    is PageContent.Text -> c.content.take(90).trim() + "..."
                                    else -> "Đánh dấu tại trang $currentPage"
                                }
                                bookRepository.addBookmark(bookId, currentPage, "Trang $currentPage", snippetText)
                            }
                            refreshBookAnnotationsAndBookmarks()
                        }
                    }) {
                        BookmarkRibbon(
                            color = if (isBookmarked) PrimaryTerracotta else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceEspresso,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderCharcoal, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    val progressFloat = if (totalPages > 0) currentPage.toFloat() / totalPages.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progressFloat },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = PrimaryTerracotta,
                        trackColor = BorderCharcoal
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (currentPage > 1) loadPage(currentPage - 1) },
                            enabled = currentPage > 1
                        ) {
                            Text("◀", color = if (currentPage > 1) PrimaryHover else TextMuted, fontSize = 16.sp)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Trang $currentPage / $totalPages",
                                color = TextParchment,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${(progressFloat * 100).toInt()}% hoàn thành",
                                color = PrimaryHover,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = { if (currentPage < totalPages) loadPage(currentPage + 1) },
                            enabled = currentPage < totalPages
                        ) {
                            Text("▶", color = if (currentPage < totalPages) PrimaryHover else TextMuted, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(currentTheme.backgroundColor)
        ) {
            // Main Reading Canvas
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalMarginDp.dp, vertical = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                when (val content = pageContent) {
                    null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Đang tải trang sách...",
                                color = currentTheme.textColor.copy(alpha = 0.6f),
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }

                    is PageContent.PdfPage -> {
                        Image(
                            bitmap = content.bitmap.asImageBitmap(),
                            contentDescription = "Trang PDF ${content.pageIndex}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    is PageContent.Text -> {
                        Text(
                            text = content.chapterTitle,
                            fontFamily = FontFamily.Serif,
                            fontSize = (fontSizeSp + 4).sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.textColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = content.content,
                            fontFamily = FontFamily.Serif,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * lineHeightMultiplier).sp,
                            color = currentTheme.textColor,
                            textAlign = TextAlign.Justify
                        )
                    }

                    is PageContent.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = content.message,
                                color = Color(0xFFEF4444),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // In-Page Annotations Section (if any exists for this page)
                if (pageAnnotations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Ghi chú & Trích dẫn trang này (${pageAnnotations.size})",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PrimaryTerracotta
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    pageAnnotations.forEach { annotation ->
                        val swatchColor = getHighlightColor(annotation.colorId)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CardSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(swatchColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "\"${annotation.selectedText}\"",
                                        color = TextParchment,
                                        fontSize = 12.sp,
                                        fontStyle = FontStyle.Italic
                                    )
                                    if (!annotation.noteText.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "✍️ ${annotation.noteText}",
                                            color = PrimaryHover,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            bookRepository.deleteAnnotation(annotation.id)
                                            refreshBookAnnotationsAndBookmarks()
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Text("✕", color = TextMuted, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    // ========================================================================
    // 1. Table of Contents (TOC) Modal Dialog
    // ========================================================================
    if (showTocSheet) {
        val tocItems = documentReader?.tableOfContents ?: emptyList()
        AlertDialog(
            onDismissRequest = { showTocSheet = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TocIcon(color = PrimaryHover)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mục lục (${tocItems.size} phần)",
                        color = TextParchment,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (tocItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Chưa có danh sách mục lục.", color = TextMuted, fontSize = 13.sp)
                        }
                    } else {
                        tocItems.forEach { item ->
                            val isCurrent = currentPage == item.pageIndex
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrent) PrimarySubtle else CardSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isCurrent) 1.5.dp else 1.dp,
                                    color = if (isCurrent) PrimaryTerracotta else BorderCharcoal
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        loadPage(item.pageIndex)
                                        showTocSheet = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.title,
                                        color = if (isCurrent) PrimaryHover else TextParchment,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Trang ${item.pageIndex}",
                                        color = if (isCurrent) PrimaryHover else TextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTocSheet = false }) {
                    Text("Đóng", color = PrimaryHover, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceEspresso
        )
    }

    // ========================================================================
    // 2. In-Book Live Search Dialog
    // ========================================================================
    if (showSearchSheet) {
        AlertDialog(
            onDismissRequest = { showSearchSheet = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SearchIcon(color = PrimaryHover)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tìm kiếm trong sách",
                        color = TextParchment,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQueryInput,
                        onValueChange = {
                            searchQueryInput = it
                            if (it.length >= 2) {
                                coroutineScope.launch {
                                    isSearching = true
                                    val res = withContext(Dispatchers.Default) {
                                        documentReader?.search(it) ?: emptyList()
                                    }
                                    searchResults = res
                                    isSearching = false
                                }
                            } else {
                                searchResults = emptyList()
                            }
                        },
                        placeholder = { Text("Nhập từ khóa tìm kiếm...", color = TextDisabled, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputSurface,
                            unfocusedContainerColor = InputSurface,
                            focusedTextColor = TextParchment,
                            unfocusedTextColor = TextParchment,
                            focusedBorderColor = PrimaryTerracotta,
                            unfocusedBorderColor = BorderCharcoal
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isSearching) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryTerracotta, modifier = Modifier.size(28.dp))
                        }
                    } else if (searchQueryInput.length >= 2 && searchResults.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text("Không tìm thấy kết quả phù hợp.", color = TextMuted, fontSize = 12.5.sp)
                        }
                    } else if (searchResults.isNotEmpty()) {
                        Text(
                            text = "Tìm thấy ${searchResults.size} kết quả:",
                            color = PrimaryHover,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            searchResults.forEach { result ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CardSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable {
                                            loadPage(result.pageIndex)
                                            showSearchSheet = false
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = result.chapterTitle,
                                                color = PrimaryHover,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Trang ${result.pageIndex}",
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = result.snippet,
                                            color = TextLinen,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSearchSheet = false }) {
                    Text("Đóng", color = PrimaryHover, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceEspresso
        )
    }

    // ========================================================================
    // 3. Display Settings Modal Sheet (Themes, Typography, Line Spacing, Margins)
    // ========================================================================
    if (showDisplaySettingsSheet) {
        AlertDialog(
            onDismissRequest = { showDisplaySettingsSheet = false },
            title = {
                Text(
                    text = "Cài đặt giao diện & Đọc",
                    color = TextParchment,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. Reading Palette Picker
                    Text(text = "Bảng màu đọc sách:", color = TextLinen, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReaderTheme.entries.forEach { theme ->
                            val isSel = theme == currentTheme
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.backgroundColor,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) PrimaryTerracotta else BorderCharcoal
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clickable { currentTheme = theme }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = theme.title.take(6),
                                        color = theme.textColor,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Font Size Scaling
                    Text(text = "Cỡ chữ: ${fontSizeSp.toInt()} sp", color = TextLinen, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { if (fontSizeSp > 12f) fontSizeSp -= 2f },
                            colors = ButtonDefaults.buttonColors(containerColor = CardSurface),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("A-", color = TextParchment, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "Mẫu chữ đọc sách",
                            fontFamily = FontFamily.Serif,
                            fontSize = fontSizeSp.sp,
                            color = currentTheme.textColor,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Button(
                            onClick = { if (fontSizeSp < 32f) fontSizeSp += 2f },
                            colors = ButtonDefaults.buttonColors(containerColor = CardSurface),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("A+", color = TextParchment, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Line Spacing Multiplier
                    Text(text = "Khoảng cách dòng:", color = TextLinen, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1.4f to "1.4x", 1.6f to "1.6x", 1.8f to "1.8x", 2.0f to "2.0x").forEach { (spacing, label) ->
                            val isSel = lineHeightMultiplier == spacing
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) PrimarySubtle else CardSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSel) 1.5.dp else 1.dp,
                                    color = if (isSel) PrimaryTerracotta else BorderCharcoal
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { lineHeightMultiplier = spacing }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) PrimaryHover else TextParchment,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Horizontal Margins
                    Text(text = "Lề hai bên trang:", color = TextLinen, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(16 to "Gọn (16dp)", 24 to "Vừa (24dp)", 32 to "Rộng (32dp)").forEach { (margin, label) ->
                            val isSel = horizontalMarginDp == margin
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) PrimarySubtle else CardSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSel) 1.5.dp else 1.dp,
                                    color = if (isSel) PrimaryTerracotta else BorderCharcoal
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { horizontalMarginDp = margin }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) PrimaryHover else TextParchment,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDisplaySettingsSheet = false }) {
                    Text("Áp dụng", color = PrimaryHover, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceEspresso
        )
    }

    // ========================================================================
    // 4. Modal Dialog: Add Highlight & Note
    // ========================================================================
    if (showAddAnnotationDialog) {
        AlertDialog(
            onDismissRequest = { showAddAnnotationDialog = false },
            title = {
                Text(
                    text = "Đánh dấu & Thêm ghi chú",
                    color = TextParchment,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Chọn màu dạ quang:",
                        color = TextLinen,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (colorId in 1..4) {
                            val color = getHighlightColor(colorId)
                            val isSelected = selectedColorId == colorId
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) PrimaryHover else BorderCharcoal,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorId = colorId },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Text("✓", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Trích đoạn:",
                        color = TextLinen,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = noteQuoteInput,
                        onValueChange = { noteQuoteInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputSurface,
                            unfocusedContainerColor = InputSurface,
                            focusedTextColor = TextParchment,
                            unfocusedTextColor = TextParchment,
                            focusedBorderColor = PrimaryTerracotta,
                            unfocusedBorderColor = BorderCharcoal
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Ghi chú suy ngẫm (tùy chọn):",
                        color = TextLinen,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = noteTextInput,
                        onValueChange = { noteTextInput = it },
                        placeholder = { Text("Nhập suy ngẫm của bạn...", color = TextDisabled, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputSurface,
                            unfocusedContainerColor = InputSurface,
                            focusedTextColor = TextParchment,
                            unfocusedTextColor = TextParchment,
                            focusedBorderColor = PrimaryTerracotta,
                            unfocusedBorderColor = BorderCharcoal
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val quote = noteQuoteInput.ifBlank { "Trích dẫn trang $currentPage" }
                        val note = noteTextInput.ifBlank { null }
                        coroutineScope.launch {
                            bookRepository.addAnnotation(
                                bookId = bookId,
                                pageNumber = currentPage,
                                selectedText = quote,
                                noteText = note,
                                colorId = selectedColorId
                            )
                            refreshBookAnnotationsAndBookmarks()
                            showAddAnnotationDialog = false
                            noteTextInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTerracotta)
                ) {
                    Text("Lưu", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAnnotationDialog = false }) {
                    Text("Hủy", color = TextLinen)
                }
            },
            containerColor = SurfaceEspresso
        )
    }

    // ========================================================================
    // 5. Modal Dialog: Bookmarks & Notes List for this book
    // ========================================================================
    if (showBookmarksSheet) {
        AlertDialog(
            onDismissRequest = { showBookmarksSheet = false },
            title = {
                Text(
                    text = "Dấu trang & Ghi chú cuốn sách",
                    color = TextParchment,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (allBookBookmarks.isEmpty() && allBookAnnotations.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Chưa có dấu trang hoặc ghi chú nào.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        if (allBookBookmarks.isNotEmpty()) {
                            Text(
                                text = "Dấu trang (${allBookBookmarks.size})",
                                color = PrimaryHover,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            allBookBookmarks.forEach { bm ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CardSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable {
                                            loadPage(bm.pageNumber)
                                            showBookmarksSheet = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        BookmarkRibbon(color = PrimaryTerracotta)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Trang ${bm.pageNumber}",
                                                color = TextParchment,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            if (!bm.snippet.isNullOrBlank()) {
                                                Text(
                                                    text = bm.snippet,
                                                    color = TextLinen,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    bookRepository.deleteBookmark(bm.id)
                                                    refreshBookAnnotationsAndBookmarks()
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Text("✕", color = TextMuted, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        if (allBookAnnotations.isNotEmpty()) {
                            Text(
                                text = "Ghi chú & Dạ quang (${allBookAnnotations.size})",
                                color = PrimaryHover,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            allBookAnnotations.forEach { an ->
                                val swatch = getHighlightColor(an.colorId)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CardSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable {
                                            loadPage(an.pageNumber)
                                            showBookmarksSheet = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(swatch)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Trang ${an.pageNumber}: \"${an.selectedText.take(40)}...\"",
                                                color = TextParchment,
                                                fontSize = 12.sp,
                                                fontStyle = FontStyle.Italic
                                            )
                                            if (!an.noteText.isNullOrBlank()) {
                                                Text(
                                                    text = "✍️ ${an.noteText}",
                                                    color = PrimaryHover,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    bookRepository.deleteAnnotation(an.id)
                                                    refreshBookAnnotationsAndBookmarks()
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Text("✕", color = TextMuted, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBookmarksSheet = false }) {
                    Text("Đóng", color = PrimaryHover, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceEspresso
        )
    }
}
