package com.example.foliohaven

import android.content.res.Configuration
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foliohaven.ui.theme.BadgeEpub
import com.example.foliohaven.ui.theme.BadgeMobi
import com.example.foliohaven.ui.theme.BadgePdf
import com.example.foliohaven.ui.theme.BadgeTxt
import com.example.foliohaven.ui.theme.BorderCharcoal
import com.example.foliohaven.ui.theme.BorderSubtle
import com.example.foliohaven.ui.theme.CardSurface
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

data class BookItem(
    val id: String,
    val title: String,
    val author: String,
    val format: String,
    val progress: Float,
    val coverGradient: List<Color>,
    val totalPages: Int,
    val currentPage: Int
)

// Archival Library Palette Grounded in replica/brand.md
val sampleBooks = listOf(
    BookItem(
        id = "1",
        title = "Sherlock Holmes: The Sign of Four",
        author = "Arthur Conan Doyle",
        format = "EPUB",
        progress = 0.68f,
        coverGradient = listOf(Color(0xFF1E293B), Color(0xFF0F172A)), // Archival Navy
        totalPages = 280,
        currentPage = 190
    ),
    BookItem(
        id = "2",
        title = "Chiến Tranh và Hòa Bình",
        author = "Leo Tolstoy",
        format = "PDF",
        progress = 0.32f,
        coverGradient = listOf(Color(0xFF451A20), Color(0xFF260D12)), // Archival Burgundy
        totalPages = 1225,
        currentPage = 392
    ),
    BookItem(
        id = "3",
        title = "Đắc Nhân Tâm",
        author = "Dale Carnegie",
        format = "EPUB",
        progress = 1.0f,
        coverGradient = listOf(Color(0xFF143022), Color(0xFF0B1F15)), // Archival Forest
        totalPages = 320,
        currentPage = 320
    ),
    BookItem(
        id = "4",
        title = "Nhà Giả Kim (The Alchemist)",
        author = "Paulo Coelho",
        format = "EPUB",
        progress = 0.15f,
        coverGradient = listOf(Color(0xFF6E280A), Color(0xFF451906)), // Archival Terracotta
        totalPages = 224,
        currentPage = 34
    )
)

val categoryList = listOf("Tất cả sách", "EPUB", "PDF", "Tiểu thuyết", "Kỹ năng sống", "Yêu thích")

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
        // 1. Subtle Diagonal Lighting Sheen
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0x22FFFFFF), Color(0x00FFFFFF)),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
            )
        }

        // 2. 3D Spine Crease Gradient on Left Edge
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

        // 3. Inner Gold Debossed Frame
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

        // 4. Format Tag
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

        // 5. Bookmark Ribbon at Top
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 22.dp)
                .width(9.dp)
                .height(18.dp)
                .background(PrimaryTerracotta)
        )

        // 6. Progress Bar on Bottom Edge
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
    onBookClick: (BookItem) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf("Tất cả sách") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            FolioHavenBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Thêm sách */ },
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
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Top Brand Header
            FolioHavenTopBar()

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Search Box
            SearchBarMock()

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Category Filter Chips
            CategoryFilterRow(
                categories = categoryList,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Hero Section: Đang đọc tiếp
            ContinueReadingHeroCard(
                book = sampleBooks[0],
                onClick = { onBookClick(sampleBooks[0]) }
            )

            Spacer(modifier = Modifier.height(26.dp))

            // 5. Section: Kệ sách của bạn
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
                TextButton(onClick = { /* Xem tất cả */ }) {
                    Text(
                        text = "Tất cả (${sampleBooks.size})",
                        color = PrimaryHover,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Horizontal Books Carousel with 3D Archival Covers
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sampleBooks) { book ->
                    BookCardItem(book = book, onClick = { onBookClick(book) })
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 7. Reading Insights & Stats
            ReadingStatsSection()

            // Bottom spacing for FAB
            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}

// ============================================================================
// Top Header Bar (Monogram 'F' logo, App Name & Offline Security Badge)
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
                    .background(
                        Brush.linearGradient(
                            listOf(PrimaryTerracotta, PrimaryHover)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "F",
                    color = Color.White,
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
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
fun SearchBarMock() {
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
            SearchIcon(color = TextMuted)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Tìm kiếm sách, tác giả, ghi chú...",
                color = TextMuted,
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp
            )
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
    onClick: () -> Unit
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

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // 3D Archival Cover Mini
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

@Composable
fun BookCardItem(
    book: BookItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(148.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // 3D Archival Book Cover
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

            Text(
                text = if (book.progress >= 1.0f) "Đã hoàn thành" else "${(book.progress * 100).toInt()}% đọc",
                style = MaterialTheme.typography.labelSmall,
                color = if (book.progress >= 1.0f) SuccessEmerald else PrimaryHover,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ReadingStatsSection() {
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
                value = "38 giờ",
                icon = "⏱️",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Đã hoàn thành",
                value = "12 cuốn",
                icon = "📚",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Chuỗi đọc",
                value = "7 ngày",
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
            label = { Text("Khác", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
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
