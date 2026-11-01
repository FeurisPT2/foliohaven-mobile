package com.example.foliohaven

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.foliohaven.data.db.FolioHavenDbHelper
import com.example.foliohaven.data.model.BookRecord
import com.example.foliohaven.data.repository.BookRepository
import com.example.foliohaven.ui.reader.getHighlightColor
import com.example.foliohaven.ui.theme.BadgeEpub
import com.example.foliohaven.ui.theme.BadgeMobi
import com.example.foliohaven.ui.theme.BadgePdf
import com.example.foliohaven.ui.theme.BadgeTxt
import com.example.foliohaven.ui.theme.BorderCharcoal
import com.example.foliohaven.ui.theme.BorderSubtle
import com.example.foliohaven.ui.theme.CardSurface
import com.example.foliohaven.ui.theme.CardSurfaceHover
import com.example.foliohaven.ui.theme.FolioHavenComposeTheme
import com.example.foliohaven.ui.theme.GoldFoilDeboss
import com.example.foliohaven.ui.theme.InputSurface
import com.example.foliohaven.ui.theme.PrimaryHover
import com.example.foliohaven.ui.theme.PrimaryPressed
import com.example.foliohaven.ui.theme.PrimarySubtle
import com.example.foliohaven.ui.theme.PrimaryTerracotta
import com.example.foliohaven.ui.theme.SuccessEmerald
import com.example.foliohaven.ui.theme.SurfaceEspresso
import com.example.foliohaven.ui.theme.TextLinen
import com.example.foliohaven.ui.theme.TextMuted
import com.example.foliohaven.ui.theme.TextParchment
import kotlinx.coroutines.launch

data class BookItem(
    val id: String,
    val title: String,
    val author: String,
    val format: String,
    val category: String = "Tất cả sách",
    val progress: Float,
    val coverGradient: List<Color>,
    val totalPages: Int,
    val currentPage: Int,
    val isFinished: Boolean = false
)

fun BookRecord.toBookItem(): BookItem = BookItem(
    id = id.toString(),
    title = title,
    author = author,
    format = format,
    category = category,
    progress = progressPercent,
    coverGradient = listOf(Color(coverGradientStart), Color(coverGradientEnd)),
    totalPages = totalPages,
    currentPage = currentPage,
    isFinished = isFinished
)

val categoryList = listOf("Tất cả sách", "EPUB", "PDF", "Tiểu thuyết", "Kỹ năng sống", "Kinh điển")

fun getFormatBadgeColor(format: String): Color {
    return when (format.uppercase()) {
        "EPUB" -> BadgeEpub
        "PDF" -> BadgePdf
        "MOBI" -> BadgeMobi
        else -> BadgeTxt
    }
}

// ============================================================================
// Vector Icons (Self-contained, 0 external icon dependencies)
// ============================================================================

@Composable
fun HomeIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.12f)
            lineTo(size.width * 0.88f, size.height * 0.45f)
            lineTo(size.width * 0.78f, size.height * 0.45f)
            lineTo(size.width * 0.78f, size.height * 0.88f)
            lineTo(size.width * 0.22f, size.height * 0.88f)
            lineTo(size.width * 0.22f, size.height * 0.45f)
            lineTo(size.width * 0.12f, size.height * 0.45f)
            close()
        }
        drawPath(path, color = color)
    }
}

@Composable
fun SearchIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(
            color = color,
            radius = size.width * 0.32f,
            center = Offset(size.width * 0.42f, size.height * 0.42f),
            style = stroke
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.65f, size.height * 0.65f),
            end = Offset(size.width * 0.90f, size.height * 0.90f),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun PlusIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        val stroke = 2.5.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.15f),
            end = Offset(size.width * 0.5f, size.height * 0.85f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.15f, size.height * 0.5f),
            end = Offset(size.width * 0.85f, size.height * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun PlayTriangle(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.15f)
            lineTo(size.width * 0.85f, size.height * 0.5f)
            lineTo(size.width * 0.25f, size.height * 0.85f)
            close()
        }
        drawPath(path, color = color)
    }
}

@Composable
fun BookShelfIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.15f, size.height * 0.22f)
            lineTo(size.width * 0.5f, size.height * 0.32f)
            lineTo(size.width * 0.85f, size.height * 0.22f)
            lineTo(size.width * 0.85f, size.height * 0.85f)
            lineTo(size.width * 0.5f, size.height * 0.75f)
            lineTo(size.width * 0.15f, size.height * 0.85f)
            close()
        }
        drawPath(path, color = color, style = stroke)
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.32f),
            end = Offset(size.width * 0.5f, size.height * 0.75f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun BookmarkRibbon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.12f)
            lineTo(size.width * 0.75f, size.height * 0.12f)
            lineTo(size.width * 0.75f, size.height * 0.88f)
            lineTo(size.width * 0.5f, size.height * 0.68f)
            lineTo(size.width * 0.25f, size.height * 0.88f)
            close()
        }
        drawPath(path, color = color)
    }
}

@Composable
fun MoreVertIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val r = 2.2.dp.toPx()
        drawCircle(color = color, radius = r, center = Offset(size.width * 0.5f, size.height * 0.25f))
        drawCircle(color = color, radius = r, center = Offset(size.width * 0.5f, size.height * 0.5f))
        drawCircle(color = color, radius = r, center = Offset(size.width * 0.5f, size.height * 0.75f))
    }
}

// ============================================================================
// 3D Archival Book Cover (Realistic leather, spine crease & gold foil deboss)
// ============================================================================

@Composable
fun ArchivalBookCover(
    book: BookItem,
    modifier: Modifier = Modifier,
    isHero: Boolean = false
) {
    val badgeColor = getFormatBadgeColor(book.format)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(book.coverGradient))
            .border(1.dp, BorderCharcoal, RoundedCornerShape(8.dp))
            .shadow(if (isHero) 6.dp else 4.dp, RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0x22FFFFFF), Color(0x00FFFFFF)),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(if (isHero) 12.dp else 16.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x99000000),
                            Color(0x44000000),
                            Color(0x11000000),
                            Color(0x00000000)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isHero) 6.dp else 10.dp)
                .border(1.dp, GoldFoilDeboss.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                .padding(if (isHero) 6.dp else 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isHero) book.title.take(3).uppercase() else book.title,
                    color = TextParchment,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isHero) 16.sp else 13.sp,
                    maxLines = if (isHero) 1 else 3,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(if (isHero) 24.dp else 32.dp)
                        .height(1.5.dp)
                        .background(GoldFoilDeboss.copy(alpha = 0.6f))
                )
                if (!isHero) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.author,
                        color = GoldFoilDeboss.copy(alpha = 0.85f),
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 10.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(bottomStart = 6.dp),
            color = badgeColor.copy(alpha = 0.9f),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Text(
                text = book.format,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 22.dp)
                .width(9.dp)
                .height(18.dp)
                .background(PrimaryTerracotta)
        )

        if (book.progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(book.progress)
                        .fillMaxHeight()
                        .background(if (book.progress >= 1.0f) SuccessEmerald else PrimaryHover)
                )
            }
        }
    }
}

// ============================================================================
// Main Home Screen Composable
// ============================================================================

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    bookRepository: BookRepository? = null,
    onBookClick: (Long, Int) -> Unit = { _, _ -> },
    onShowGreeting: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf("Tất cả sách") }
    var searchQuery by remember { mutableStateOf("") }
    var stats by remember { mutableStateOf(FolioHavenDbHelper.ReadingStats(0, 0, 0, 0)) }

    // Bookmarks and Annotations state for Tab 2
    var allBookmarks by remember { mutableStateOf<List<FolioHavenDbHelper.BookmarkWithBook>>(emptyList()) }
    var allAnnotations by remember { mutableStateOf<List<FolioHavenDbHelper.AnnotationWithBook>>(emptyList()) }
    var bookmarksTabFilter by remember { mutableStateOf("Tất cả") } // "Tất cả", "Dấu trang", "Ghi chú"

    // Book Management state
    var selectedBookForAction by remember { mutableStateOf<BookItem?>(null) }
    var showBookActionMenu by remember { mutableStateOf(false) }
    var showEditBookDialog by remember { mutableStateOf(false) }
    var showDeleteBookDialog by remember { mutableStateOf(false) }
    var editTitleInput by remember { mutableStateOf("") }
    var editAuthorInput by remember { mutableStateOf("") }
    var editCategoryInput by remember { mutableStateOf("") }

    fun refreshBookmarksAndNotes() {
        coroutineScope.launch {
            allBookmarks = bookRepository?.getAllBookmarks() ?: emptyList()
            allAnnotations = bookRepository?.getAllAnnotations() ?: emptyList()
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 2) {
            refreshBookmarksAndNotes()
        }
    }

    // Collect repository state if available
    val repoBooks by bookRepository?.booksFlow?.collectAsStateWithLifecycle(emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    val displayBooks: List<BookItem> = repoBooks.map { it.toBookItem() }

    LaunchedEffect(selectedCategory, searchQuery) {
        bookRepository?.setCategoryFilter(selectedCategory)
        bookRepository?.setSearchQuery(searchQuery)
        val latestStats = bookRepository?.getReadingStats()
        if (latestStats != null) stats = latestStats
    }

    // SAF File Picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            coroutineScope.launch {
                val result = bookRepository?.importBookFromUri(selectedUri)
                if (result?.isSuccess == true) {
                    val imported = result.getOrNull()
                    snackbarHostState.showSnackbar("Đã thêm sách: ${imported?.title}")
                } else {
                    snackbarHostState.showSnackbar("Lỗi khi thêm sách: ${result?.exceptionOrNull()?.message}")
                }
            }
        }
    }

    // SAF Export JSON
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { destUri ->
            coroutineScope.launch {
                try {
                    val json = bookRepository?.exportUserDataJson() ?: ""
                    context.contentResolver.openOutputStream(destUri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                    snackbarHostState.showSnackbar("Đã xuất bản sao lưu JSON thành công!")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Lỗi khi xuất tệp: ${e.message}")
                }
            }
        }
    }

    // SAF Import JSON
    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { srcUri ->
            coroutineScope.launch {
                val repo = bookRepository ?: return@launch
                try {
                    val json = context.contentResolver.openInputStream(srcUri)?.use { inp ->
                        inp.bufferedReader().readText()
                    } ?: ""
                    val res = repo.importUserDataJson(json)
                    if (res.isSuccess) {
                        val (bmCount, anCount) = res.getOrThrow()
                        snackbarHostState.showSnackbar("Đã khôi phục $bmCount dấu trang và $anCount ghi chú thành công!")
                        val latestStats = repo.getReadingStats()
                        stats = latestStats
                        refreshBookmarksAndNotes()
                    } else {
                        snackbarHostState.showSnackbar("Lỗi khi nhập tệp: ${res.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Lỗi đọc tệp: ${e.message}")
                }
            }
        }
    }

    val windowSizeClass = rememberWindowWidthSizeClass()
    val isCompact = windowSizeClass == WindowWidthSizeClass.COMPACT

    Row(modifier = modifier.fillMaxSize()) {
        if (!isCompact) {
            FolioHavenNavigationRail(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }

        Scaffold(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (isCompact) {
                    FolioHavenBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }
            },
            floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    filePickerLauncher.launch(
                        arrayOf(
                            "application/epub+zip",
                            "application/pdf",
                            "text/plain",
                            "application/octet-stream",
                            "*/*"
                        )
                    )
                },
                containerColor = PrimaryTerracotta,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlusIcon(color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Thêm sách",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            FolioHavenTopBar()

            when (selectedTab) {
                0 -> {
                    // TAB 0: HOME SHELF
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(6.dp))
                        SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                        Spacer(modifier = Modifier.height(14.dp))
                        CategoryFilterRow(
                            categories = categoryList,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it }
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Shelf Content
                        if (displayBooks.isNotEmpty()) {
                            // Hero card
                            val heroBook = displayBooks.first()
                            ContinueReadingHeroCard(
                                book = heroBook,
                                onClick = { onBookClick(heroBook.id.toLongOrNull() ?: 1L, heroBook.currentPage) },
                                onMoreClick = {
                                    selectedBookForAction = heroBook
                                    showBookActionMenu = true
                                },
                                isCompact = isCompact
                            )

                            Spacer(modifier = Modifier.height(26.dp))

                            // Shelf Section
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Kệ sách của bạn",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextParchment,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { selectedTab = 1 }) {
                                    Text(
                                        text = "Tất cả (${displayBooks.size})",
                                        color = PrimaryHover,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(displayBooks) { book ->
                                    BookCardItem(
                                        book = book,
                                        onClick = { onBookClick(book.id.toLongOrNull() ?: 1L, book.currentPage) },
                                        onMoreClick = {
                                            selectedBookForAction = book
                                            showBookActionMenu = true
                                        },
                                        modifier = if (isCompact) Modifier.width(148.dp) else Modifier.width(168.dp)
                                    )
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal)
                            ) {
                                Column(
                                    modifier = Modifier.padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("📚", fontSize = 42.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Kệ sách của bạn đang trống",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextParchment,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Nhấn nút \"+ Thêm sách\" bên dưới để nhập sách EPUB, PDF hoặc TXT từ thiết bị của bạn.",
                                        color = TextMuted,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(26.dp))
                        ReadingStatsSection(stats = stats)
                        Spacer(modifier = Modifier.height(84.dp))
                    }
                }

                1 -> {
                    // TAB 1: LIBRARY GRID
                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                        Spacer(modifier = Modifier.height(10.dp))
                        CategoryFilterRow(
                            categories = categoryList,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Danh mục (${displayBooks.size} tác phẩm)",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextParchment,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )

                        if (displayBooks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🔍", fontSize = 36.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = if (searchQuery.isNotBlank()) "Không tìm thấy tác phẩm nào khớp với \"$searchQuery\"" else "Chưa có tác phẩm nào trong danh mục này",
                                        color = TextMuted,
                                        fontSize = 13.5.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            val libraryGridCells = when (windowSizeClass) {
                                WindowWidthSizeClass.EXPANDED -> GridCells.Adaptive(minSize = 175.dp)
                                WindowWidthSizeClass.MEDIUM -> GridCells.Adaptive(minSize = 160.dp)
                                WindowWidthSizeClass.COMPACT -> GridCells.Fixed(2)
                            }
                            LazyVerticalGrid(
                                columns = libraryGridCells,
                                contentPadding = PaddingValues(horizontal = if (isCompact) 16.dp else 24.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(displayBooks) { book ->
                                    BookCardItem(
                                        book = book,
                                        onClick = { onBookClick(book.id.toLongOrNull() ?: 1L, book.currentPage) },
                                        onMoreClick = {
                                            selectedBookForAction = book
                                            showBookActionMenu = true
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: BOOKMARKS & NOTES
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Dấu trang & Ghi chú của bạn",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextParchment,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tất cả các đánh dấu trang và trích đoạn yêu thích được lưu cục bộ 100% trên thiết bị.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Filter Chips: "Tất cả", "Dấu trang", "Ghi chú"
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Tất cả", "Dấu trang", "Ghi chú").forEach { filter ->
                                val isSelected = bookmarksTabFilter == filter
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) PrimaryTerracotta else CardSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) PrimaryTerracotta else BorderCharcoal
                                    ),
                                    modifier = Modifier.clickable { bookmarksTabFilter = filter }
                                ) {
                                    Text(
                                        text = filter,
                                        color = if (isSelected) Color.White else TextLinen,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.5.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val showBookmarks = bookmarksTabFilter == "Tất cả" || bookmarksTabFilter == "Dấu trang"
                        val showAnnotations = bookmarksTabFilter == "Tất cả" || bookmarksTabFilter == "Ghi chú"

                        if (allBookmarks.isEmpty() && allAnnotations.isEmpty()) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("📖", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Chưa có dấu trang hay ghi chú nào",
                                        fontWeight = FontWeight.Bold,
                                        color = TextParchment,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Khi đọc sách, bạn hãy bấm biểu tượng ruy băng để đánh dấu trang hoặc cây bút dạ quang để lưu trích dẫn và ghi chú nhé!",
                                        color = TextMuted,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            val bookmarkGridCells = if (isCompact) GridCells.Fixed(1) else GridCells.Adaptive(minSize = 340.dp)
                            LazyVerticalGrid(
                                columns = bookmarkGridCells,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                if (showBookmarks) {
                                    items(allBookmarks) { bm ->
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        BookmarkRibbon(color = PrimaryTerracotta)
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column {
                                                            Text(
                                                                text = bm.bookTitle,
                                                                fontWeight = FontWeight.Bold,
                                                                color = TextParchment,
                                                                fontSize = 13.5.sp,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            Text(
                                                                text = "Dấu trang tại Trang ${bm.pageNumber}",
                                                                color = PrimaryHover,
                                                                fontSize = 11.5.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            coroutineScope.launch {
                                                                bookRepository?.deleteBookmark(bm.id)
                                                                refreshBookmarksAndNotes()
                                                            }
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Text("✕", color = TextMuted, fontSize = 12.sp)
                                                    }
                                                }

                                                if (!bm.snippet.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "\"${bm.snippet}\"",
                                                        color = TextLinen,
                                                        fontSize = 12.sp,
                                                        fontStyle = FontStyle.Italic
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End
                                                ) {
                                                    Button(
                                                        onClick = { onBookClick(bm.bookId, bm.pageNumber) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = PrimarySubtle),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                                    ) {
                                                        Text("Đọc tiếp trang ${bm.pageNumber} ▶", color = PrimaryHover, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (showAnnotations) {
                                    items(allAnnotations) { an ->
                                        val swatchColor = getHighlightColor(an.colorId)
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(12.dp)
                                                                .clip(CircleShape)
                                                                .background(swatchColor)
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column {
                                                            Text(
                                                                text = an.bookTitle,
                                                                fontWeight = FontWeight.Bold,
                                                                color = TextParchment,
                                                                fontSize = 13.5.sp,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            Text(
                                                                text = "Ghi chú tại Trang ${an.pageNumber}",
                                                                color = swatchColor,
                                                                fontSize = 11.5.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            coroutineScope.launch {
                                                                bookRepository?.deleteAnnotation(an.id)
                                                                refreshBookmarksAndNotes()
                                                            }
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Text("✕", color = TextMuted, fontSize = 12.sp)
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "\"${an.selectedText}\"",
                                                    color = TextParchment,
                                                    fontSize = 12.sp,
                                                    fontStyle = FontStyle.Italic
                                                )

                                                if (!an.noteText.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = "✍️ ${an.noteText}",
                                                        color = PrimaryHover,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End
                                                ) {
                                                    Button(
                                                        onClick = { onBookClick(an.bookId, an.pageNumber) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = PrimarySubtle),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                                    ) {
                                                        Text("Đọc tiếp trang ${an.pageNumber} ▶", color = PrimaryHover, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: SETTINGS & ECOSYSTEM
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Cài đặt & Hệ sinh thái",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextParchment,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Offline Security Card
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPressed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "🔒 100% Offline & Riêng tư",
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryHover,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "FolioHaven không thu thập dữ liệu cá nhân, không chứa quảng cáo và không yêu cầu đăng nhập tài khoản đám mây.",
                                    color = TextLinen,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Backup & Restore Card (JSON Interop)
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "💾 Sao lưu & Trao đổi dữ liệu (.json)",
                                    fontWeight = FontWeight.Bold,
                                    color = TextParchment,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Xuất hoặc nhập toàn bộ tiến trình đọc sách, danh sách dấu trang và ghi chú dạng tệp JSON tiêu chuẩn tương thích với FolioHaven PC và Web.",
                                    color = TextLinen,
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { exportJsonLauncher.launch("foliohaven_backup.json") },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTerracotta),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Xuất JSON", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { importJsonLauncher.launch(arrayOf("application/json", "text/*", "*/*")) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceHover),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Nhập JSON", color = TextParchment, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Seed Sample Books Card (Optional on-demand)
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "📖 Thư viện tác phẩm mẫu",
                                    fontWeight = FontWeight.Bold,
                                    color = TextParchment,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Nạp 4 tác phẩm văn học kinh điển (Sherlock Holmes, Chiến Tranh & Hòa Bình, Đắc Nhân Tâm, Nhà Giả Kim) để trải nghiệm đọc ngay.",
                                    color = TextLinen,
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            bookRepository?.seedDefaultBooksIfEmpty()
                                            val latestStats = bookRepository?.getReadingStats()
                                            if (latestStats != null) stats = latestStats
                                            snackbarHostState.showSnackbar("Đã nạp 4 tác phẩm kinh điển mẫu thành công!")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceHover),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Nạp 4 sách kinh điển mẫu", color = PrimaryHover, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Welcome Screen Revisit Card
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "✨ Giới thiệu FolioHaven",
                                    fontWeight = FontWeight.Bold,
                                    color = TextParchment,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Xem lại màn hình chào mừng, triết lý đọc sách tĩnh tâm và các tính năng cốt lõi của ứng dụng.",
                                    color = TextLinen,
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onShowGreeting,
                                    colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceHover),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Xem lại màn hình chào", color = PrimaryHover, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Hệ sinh thái FolioHaven",
                            fontWeight = FontWeight.Bold,
                            color = TextParchment,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        EcosystemLinkItem("Web Portal", "https://github.com/FeurisPT2/foliohaven", "Vercel / HTML5")
                        Spacer(modifier = Modifier.height(8.dp))
                        EcosystemLinkItem("PC Desktop Client", "https://github.com/FeurisPT2/Foliohaven-pc", "Flutter / Dart")
                        Spacer(modifier = Modifier.height(8.dp))
                        EcosystemLinkItem("Mobile Android Client", "https://github.com/FeurisPT2/foliohaven-mobile", "Jetpack Compose / Kotlin")

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Phiên bản: 1.0.0 (Native Android Compose)",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // ====================================================================
        // Book Management Dialogs
        // ====================================================================

        // 1. Book Options Sheet / Dialog
        if (showBookActionMenu && selectedBookForAction != null) {
            val currentSelected = selectedBookForAction!!
            AlertDialog(
                modifier = Modifier.widthIn(max = 520.dp),
                onDismissRequest = { showBookActionMenu = false },
                title = {
                    Column {
                        Text(
                            text = currentSelected.title,
                            fontWeight = FontWeight.Bold,
                            color = TextParchment,
                            fontSize = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${currentSelected.author} • ${currentSelected.format} • ${currentSelected.category}",
                            color = TextLinen,
                            fontSize = 12.sp
                        )
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Action 1: Đọc tiếp
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CardSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showBookActionMenu = false
                                    onBookClick(currentSelected.id.toLongOrNull() ?: 1L, currentSelected.currentPage)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📖", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Đọc tiếp (Trang ${currentSelected.currentPage})",
                                    color = TextParchment,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Action 2: Sửa thông tin
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CardSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showBookActionMenu = false
                                    editTitleInput = currentSelected.title
                                    editAuthorInput = currentSelected.author
                                    editCategoryInput = currentSelected.category
                                    showEditBookDialog = true
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✏️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Sửa thông tin tác phẩm",
                                    color = TextParchment,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Action 3: Đánh dấu đã đọc / chưa đọc
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CardSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showBookActionMenu = false
                                    coroutineScope.launch {
                                        val newFinished = !currentSelected.isFinished
                                        bookRepository?.toggleBookFinished(currentSelected.id.toLongOrNull() ?: 1L, newFinished)
                                        snackbarHostState.showSnackbar(
                                            if (newFinished) "Đã đánh dấu là hoàn thành!" else "Đã chuyển về trạng thái đang đọc"
                                        )
                                        stats = bookRepository?.getReadingStats() ?: stats
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (currentSelected.isFinished) "↩️" else "✅", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (currentSelected.isFinished) "Đánh dấu chưa đọc xong" else "Đánh dấu đã đọc xong",
                                    color = TextParchment,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Action 4: Xóa sách
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CardSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showBookActionMenu = false
                                    showDeleteBookDialog = true
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🗑️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Xóa sách khỏi thư viện",
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBookActionMenu = false }) {
                        Text("Đóng", color = PrimaryHover, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = SurfaceEspresso
            )
        }

        // 2. Edit Book Dialog
        if (showEditBookDialog && selectedBookForAction != null) {
            val currentSelected = selectedBookForAction!!
            AlertDialog(
                modifier = Modifier.widthIn(max = 520.dp),
                onDismissRequest = { showEditBookDialog = false },
                title = {
                    Text(
                        text = "Sửa thông tin sách",
                        color = TextParchment,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                        Text("Tên sách:", color = TextLinen, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editTitleInput,
                            onValueChange = { editTitleInput = it },
                            modifier = Modifier.fillMaxWidth(),
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

                        Text("Tác giả:", color = TextLinen, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editAuthorInput,
                            onValueChange = { editAuthorInput = it },
                            modifier = Modifier.fillMaxWidth(),
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

                        Text("Thể loại / Danh mục:", color = TextLinen, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editCategoryInput,
                            onValueChange = { editCategoryInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = InputSurface,
                                unfocusedContainerColor = InputSurface,
                                focusedTextColor = TextParchment,
                                unfocusedTextColor = TextParchment,
                                focusedBorderColor = PrimaryTerracotta,
                                unfocusedBorderColor = BorderCharcoal
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Tiểu thuyết", "Kinh điển", "Kỹ năng sống", "Triết học", "Khoa học", "Lịch sử").forEach { chipCat ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CardSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                                    modifier = Modifier.clickable { editCategoryInput = chipCat }
                                ) {
                                    Text(
                                        text = chipCat,
                                        color = PrimaryHover,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val finalTitle = editTitleInput.ifBlank { currentSelected.title }
                            val finalAuthor = editAuthorInput.ifBlank { currentSelected.author }
                            val finalCategory = editCategoryInput.ifBlank { currentSelected.category }
                            coroutineScope.launch {
                                bookRepository?.updateBookMetadata(
                                    currentSelected.id.toLongOrNull() ?: 1L,
                                    finalTitle,
                                    finalAuthor,
                                    finalCategory
                                )
                                showEditBookDialog = false
                                snackbarHostState.showSnackbar("Đã cập nhật thông tin sách thành công!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTerracotta)
                    ) {
                        Text("Lưu", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditBookDialog = false }) {
                        Text("Hủy", color = TextLinen)
                    }
                },
                containerColor = SurfaceEspresso
            )
        }

        // 3. Delete Book Dialog
        if (showDeleteBookDialog && selectedBookForAction != null) {
            val currentSelected = selectedBookForAction!!
            AlertDialog(
                modifier = Modifier.widthIn(max = 520.dp),
                onDismissRequest = { showDeleteBookDialog = false },
                title = {
                    Text(
                        text = "Xóa cuốn sách này?",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Text(
                        text = "Bạn có chắc chắn muốn xóa tác phẩm \"${currentSelected.title}\" khỏi thư viện không? Toàn bộ dấu trang và ghi chú của cuốn sách cũng sẽ được dọn dẹp sạch khỏi thiết bị.",
                        color = TextLinen,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val bookId = currentSelected.id.toLongOrNull() ?: 1L
                                val ok = bookRepository?.deleteBook(bookId) ?: false
                                showDeleteBookDialog = false
                                if (ok) {
                                    snackbarHostState.showSnackbar("Đã xóa \"${currentSelected.title}\"")
                                    stats = bookRepository?.getReadingStats() ?: stats
                                    refreshBookmarksAndNotes()
                                } else {
                                    snackbarHostState.showSnackbar("Không thể xóa sách")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Xác nhận xóa", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteBookDialog = false }) {
                        Text("Giữ lại", color = TextParchment)
                    }
                },
                containerColor = SurfaceEspresso
            )
        }
    }
}
}

@Composable
fun EcosystemLinkItem(title: String, url: String, tech: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CardSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontWeight = FontWeight.SemiBold, color = TextParchment, fontSize = 13.sp)
                Text(text = tech, color = TextMuted, fontSize = 11.sp)
            }
            Text(text = "↗", color = PrimaryHover, fontSize = 16.sp)
        }
    }
}

// ============================================================================
// Top Header Bar
// ============================================================================

@Composable
fun FolioHavenTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, BorderCharcoal, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_foliohaven_logo),
                    contentDescription = "FolioHaven Logo",
                    modifier = Modifier.size(42.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "FolioHaven",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextParchment,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = "Thư viện đọc sách tĩnh tâm • Offline 100%",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        IconButton(
            onClick = { /* Menu */ },
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(CardSurface)
                .border(1.dp, BorderCharcoal, CircleShape)
        ) {
            MoreVertIcon(
                color = TextLinen,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = InputSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchIcon(color = if (query.isNotEmpty()) PrimaryHover else TextMuted)
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Tìm kiếm sách, tác giả, ghi chú...",
                        color = TextMuted,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = TextParchment,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.SansSerif
                    ),
                    cursorBrush = SolidColor(PrimaryHover),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Text(
                    text = "✕",
                    color = TextMuted,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { onQueryChange("") }
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { cat ->
            val isSelected = cat == selectedCategory
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PrimaryTerracotta else CardSurface)
                    .border(
                        1.dp,
                        if (isSelected) PrimaryHover else BorderCharcoal,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onCategorySelected(cat) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = cat,
                    color = if (isSelected) Color.White else TextLinen,
                    fontFamily = FontFamily.SansSerif,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ContinueReadingHeroCard(
    book: BookItem,
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    isCompact: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        if (!isCompact) {
            // Adaptive Wide Hero Layout for Tablet & Unfolded Foldables
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArchivalBookCover(
                    book = book,
                    modifier = Modifier.size(width = 86.dp, height = 126.dp),
                    isHero = true
                )

                Spacer(modifier = Modifier.width(20.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(SuccessEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ĐANG ĐỌC TIẾP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryHover,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimarySubtle,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryHover.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = book.format,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryHover
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = book.title,
                        fontFamily = FontFamily.Serif,
                        color = TextParchment,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${book.author} • Thể loại: ${book.category}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextLinen
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trang ${book.currentPage} / ${book.totalPages}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Text(
                            text = "${(book.progress * 100).toInt()}% hoàn thành",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryHover
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { book.progress },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PrimaryTerracotta,
                        trackColor = BorderCharcoal
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (onMoreClick != null) {
                        IconButton(
                            onClick = onMoreClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            MoreVertIcon(color = TextLinen, modifier = Modifier.size(18.dp))
                        }
                    }
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTerracotta),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        PlayTriangle(color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tiếp tục đọc ▶",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(SuccessEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ĐANG ĐỌC TIẾP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryHover,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimarySubtle,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryHover.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = book.format,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryHover
                            )
                        }
                        if (onMoreClick != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = onMoreClick,
                                modifier = Modifier.size(24.dp)
                            ) {
                                MoreVertIcon(color = TextLinen, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    ArchivalBookCover(
                        book = book,
                        modifier = Modifier.size(width = 72.dp, height = 104.dp),
                        isHero = true
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = book.title,
                                fontFamily = FontFamily.Serif,
                                color = TextParchment,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = book.author,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextLinen
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Trang ${book.currentPage} / ${book.totalPages}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            Text(
                                text = "${(book.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryHover
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { book.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(2.5.dp)),
                            color = PrimaryTerracotta,
                            trackColor = BorderCharcoal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTerracotta)
                ) {
                    PlayTriangle(color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tiếp tục đọc ngay",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BookCardItem(
    book: BookItem,
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier.width(148.dp)
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            ArchivalBookCover(
                book = book,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = book.title,
                fontFamily = FontFamily.Serif,
                color = TextParchment,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = book.author,
                style = MaterialTheme.typography.bodySmall,
                color = TextLinen,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (book.isFinished || book.progress >= 0.99f) "Đã hoàn thành" else "${(book.progress * 100).toInt()}% đọc",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (book.isFinished || book.progress >= 0.99f) SuccessEmerald else PrimaryHover,
                    fontWeight = FontWeight.SemiBold
                )

                if (onMoreClick != null) {
                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        MoreVertIcon(color = TextLinen, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ReadingStatsSection(stats: FolioHavenDbHelper.ReadingStats = FolioHavenDbHelper.ReadingStats(0, 0, 0, 0)) {
    val readingTimeText = if (stats.totalReadingMinutes < 60) {
        "${stats.totalReadingMinutes} phút"
    } else {
        "${stats.totalReadingMinutes / 60} giờ"
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Thống kê & Thói quen đọc",
            style = MaterialTheme.typography.titleLarge,
            color = TextParchment,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Thời gian đọc",
                value = readingTimeText,
                icon = "⏱️",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Đã hoàn thành",
                value = "${stats.finishedBooks} cuốn",
                icon = "📚",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Chuỗi đọc",
                value = "${stats.readingStreakDays} ngày",
                icon = "🔥",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = CardSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryHover
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun FolioHavenBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceEspresso,
        tonalElevation = 0.dp,
        modifier = Modifier.border(
            width = 1.dp,
            color = BorderCharcoal,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        )
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { HomeIcon(color = if (selectedTab == 0) PrimaryHover else TextMuted) },
            label = { Text("Trang chủ", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryHover,
                selectedTextColor = PrimaryHover,
                unselectedTextColor = TextMuted,
                indicatorColor = PrimarySubtle
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { BookShelfIcon(color = if (selectedTab == 1) PrimaryHover else TextMuted) },
            label = { Text("Thư viện", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryHover,
                selectedTextColor = PrimaryHover,
                unselectedTextColor = TextMuted,
                indicatorColor = PrimarySubtle
            )
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { BookmarkRibbon(color = if (selectedTab == 2) PrimaryHover else TextMuted) },
            label = { Text("Dấu trang", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryHover,
                selectedTextColor = PrimaryHover,
                unselectedTextColor = TextMuted,
                indicatorColor = PrimarySubtle
            )
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = { MoreVertIcon(color = if (selectedTab == 3) PrimaryHover else TextMuted) },
            label = { Text("Cài đặt", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryHover,
                selectedTextColor = PrimaryHover,
                unselectedTextColor = TextMuted,
                indicatorColor = PrimarySubtle
            )
        )
    }
}

enum class WindowWidthSizeClass {
    COMPACT, MEDIUM, EXPANDED
}

@Composable
fun rememberWindowWidthSizeClass(): WindowWidthSizeClass {
    val configuration = LocalConfiguration.current
    return when {
        configuration.screenWidthDp >= 840 -> WindowWidthSizeClass.EXPANDED
        configuration.screenWidthDp >= 600 -> WindowWidthSizeClass.MEDIUM
        else -> WindowWidthSizeClass.COMPACT
    }
}

@Composable
fun FolioHavenNavigationRail(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        containerColor = SurfaceEspresso,
        modifier = modifier
            .fillMaxHeight()
            .border(
                width = 1.dp,
                color = BorderCharcoal,
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ),
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderCharcoal, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_foliohaven_logo),
                        contentDescription = "FolioHaven Logo",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        NavigationRailItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { HomeIcon(color = if (selectedTab == 0) PrimaryHover else TextMuted) },
            label = { Text("Trang chủ", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = PrimaryHover,
                selectedTextColor = PrimaryHover,
                unselectedTextColor = TextMuted,
                indicatorColor = PrimarySubtle
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        NavigationRailItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { BookShelfIcon(color = if (selectedTab == 1) PrimaryHover else TextMuted) },
            label = { Text("Thư viện", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = PrimaryHover,
                selectedTextColor = PrimaryHover,
                unselectedTextColor = TextMuted,
                indicatorColor = PrimarySubtle
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        NavigationRailItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { BookmarkRibbon(color = if (selectedTab == 2) PrimaryHover else TextMuted) },
            label = { Text("Dấu trang", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = PrimaryHover,
                selectedTextColor = PrimaryHover,
                unselectedTextColor = TextMuted,
                indicatorColor = PrimarySubtle
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        NavigationRailItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = { MoreVertIcon(color = if (selectedTab == 3) PrimaryHover else TextMuted) },
            label = { Text("Cài đặt", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = PrimaryHover,
                selectedTextColor = PrimaryHover,
                unselectedTextColor = TextMuted,
                indicatorColor = PrimarySubtle
            )
        )
    }
}

@Preview(showBackground = true, name = "FolioHaven Mobile - Light Theme", widthDp = 390, heightDp = 844)
@Composable
fun HomeScreenPreview() {
    FolioHavenComposeTheme(darkTheme = false) {
        HomeScreen()
    }
}

@Preview(
    showBackground = true,
    name = "FolioHaven Mobile - Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun HomeScreenDarkPreview() {
    FolioHavenComposeTheme(darkTheme = true) {
        HomeScreen()
    }
}
