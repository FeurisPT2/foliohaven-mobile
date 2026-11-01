package com.example.foliohaven.ui.greeting

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foliohaven.R
import com.example.foliohaven.data.model.ReaderTheme
import com.example.foliohaven.ui.theme.BorderCharcoal
import com.example.foliohaven.ui.theme.CardSurface
import com.example.foliohaven.ui.theme.FolioHavenComposeTheme
import com.example.foliohaven.ui.theme.PrimaryGlow
import com.example.foliohaven.ui.theme.PrimaryHover
import com.example.foliohaven.ui.theme.PrimaryPressed
import com.example.foliohaven.ui.theme.PrimarySubtle
import com.example.foliohaven.ui.theme.PrimaryTerracotta
import com.example.foliohaven.ui.theme.SurfaceEspresso
import com.example.foliohaven.ui.theme.TextLinen
import com.example.foliohaven.ui.theme.TextMuted
import com.example.foliohaven.ui.theme.TextParchment

/**
 * Step-by-step Onboarding Carousel for FolioHaven Mobile:
 * - Step 0: Brand Welcome & Real-Time Language Selection (Tiếng Việt 🇻🇳 / English 🇬🇧)
 * - Step 1: Core Principles (100% Offline, Literary Aesthetics, Reflection Tools)
 * - Step 2: Reading Sanctuary Setup (Theme Palette & Classic Books)
 */
@Composable
fun GreetingScreen(
    onGetStarted: (theme: ReaderTheme, seedSampleBooks: Boolean, language: String) -> Unit,
    modifier: Modifier = Modifier,
    initialLanguage: String = "vi"
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var selectedLanguage by remember { mutableStateOf(initialLanguage) }
    var selectedTheme by remember { mutableStateOf(ReaderTheme.SEPIA) }
    var seedSampleBooks by remember { mutableStateOf(true) }

    val isEn = selectedLanguage == "en"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 620.dp)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Step Progress Indicator & Step Count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentStep) {
                            0 -> if (isEn) "Step 1 of 3: Welcome" else "Bước 1/3: Chào mừng"
                            1 -> if (isEn) "Step 2 of 3: Philosophy" else "Bước 2/3: Triết lý"
                            else -> if (isEn) "Step 3 of 3: Reading Setup" else "Bước 3/3: Thiết lập"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryHover,
                        letterSpacing = 0.5.sp
                    )

                    // Stepper Indicator Bars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0..2) {
                            val isCurrent = i == currentStep
                            val isPassed = i < currentStep
                            Box(
                                modifier = Modifier
                                    .height(5.dp)
                                    .width(if (isCurrent) 26.dp else 9.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        when {
                                            isCurrent -> PrimaryTerracotta
                                            isPassed -> PrimaryHover.copy(alpha = 0.6f)
                                            else -> BorderCharcoal
                                        }
                                    )
                            )
                        }
                    }
                }

                // Main Page Container with Smooth Step Transitions
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width / 2 } + fadeIn())
                                    .togetherWith(slideOutHorizontally { width -> -width / 2 } + fadeOut())
                            } else {
                                (slideInHorizontally { width -> -width / 2 } + fadeIn())
                                    .togetherWith(slideOutHorizontally { width -> width / 2 } + fadeOut())
                            }
                        },
                        label = "GreetingStepTransition",
                        modifier = Modifier.fillMaxSize()
                    ) { step ->
                        when (step) {
                            0 -> StepWelcomeAndLanguage(
                                isEn = isEn,
                                selectedLanguage = selectedLanguage,
                                onLanguageChange = { selectedLanguage = it }
                            )

                            1 -> StepCorePrinciples(isEn = isEn)

                            2 -> StepReadingSetup(
                                isEn = isEn,
                                selectedTheme = selectedTheme,
                                onThemeChange = { selectedTheme = it },
                                seedSampleBooks = seedSampleBooks,
                                onSeedChange = { seedSampleBooks = it }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Navigation Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Button (Visible on Step 1 & 2)
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextLinen
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Text(
                                text = if (isEn) "← Back" else "← Quay lại",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Next or Finish Button
                    Button(
                        onClick = {
                            if (currentStep < 2) {
                                currentStep++
                            } else {
                                onGetStarted(selectedTheme, seedSampleBooks, selectedLanguage)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTerracotta),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(if (currentStep > 0) 2f else 1f)
                            .height(52.dp)
                            .shadow(8.dp, RoundedCornerShape(14.dp))
                    ) {
                        Text(
                            text = if (currentStep < 2) {
                                if (isEn) "Next →" else "Tiếp tục →"
                            } else {
                                if (isEn) "Begin Reading Journey ▶" else "Bắt đầu hành trình đọc ▶"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

/**
 * Backward compatibility overload
 */
@Composable
fun GreetingScreen(
    onGetStarted: (theme: ReaderTheme, seedSampleBooks: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    GreetingScreen(
        onGetStarted = { theme, seedSampleBooks, _ -> onGetStarted(theme, seedSampleBooks) },
        modifier = modifier,
        initialLanguage = "vi"
    )
}

// ============================================================================
// Step 0: Welcome & Language Selector
// ============================================================================

@Composable
private fun StepWelcomeAndLanguage(
    isEn: Boolean,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Brand Logo with Radiant Terracotta Glow
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(112.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PrimaryGlow, Color.Transparent)
                        )
                    )
            )

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SurfaceEspresso,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryPressed),
                shadowElevation = 8.dp,
                modifier = Modifier.size(76.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_foliohaven_logo),
                        contentDescription = "FolioHaven Logo",
                        modifier = Modifier.size(54.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "FolioHaven",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            letterSpacing = (-0.5).sp,
            color = TextParchment
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isEn) "Serene E-Reader Sanctuary • 100% Offline" else "Thư viện đọc sách tĩnh tâm • Ngoại tuyến 100%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryHover,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isEn)
                "A peaceful haven for book lovers. No advertisements, no tracking, zero distractions."
            else
                "Nơi trú ẩn tĩnh lặng cho những tâm hồn yêu sách. Không quảng cáo, không theo dõi, không gián đoạn.",
            style = MaterialTheme.typography.bodySmall,
            color = TextLinen,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Language Selector Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (isEn) "🌐 Choose Application Language" else "🌐 Chọn ngôn ngữ ứng dụng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextParchment,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isEn)
                        "Select your preferred language. You can change this later in Settings."
                    else
                        "Lựa chọn ngôn ngữ hiển thị. Bạn có thể thay đổi bất kỳ lúc nào trong Cài đặt.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val languages = listOf(
                        "vi" to ("Tiếng Việt" to "🇻🇳"),
                        "en" to ("English" to "🇬🇧")
                    )

                    languages.forEach { (code, data) ->
                        val (label, flag) = data
                        val isSelected = selectedLanguage == code

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimarySubtle else CardSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) PrimaryTerracotta else BorderCharcoal
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clickable { onLanguageChange(code) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = flag, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = label,
                                        color = if (isSelected) PrimaryHover else TextParchment,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = if (isEn) "Active" else "Đang chọn",
                                            color = PrimaryTerracotta,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
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

// ============================================================================
// Step 1: Core Principles (Pillars)
// ============================================================================

@Composable
private fun StepCorePrinciples(isEn: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isEn) "Honoring the Essence of Books" else "Tôn vinh Giá trị Nguyên bản",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextParchment
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isEn)
                "Every feature is crafted to bring serene focus and untroubled reading."
            else
                "Mọi tính năng được chăm chút để mang lại sự tĩnh tại và tập trung tối đa cho người đọc.",
            style = MaterialTheme.typography.bodySmall,
            color = TextLinen,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3 Core Pillars Cards
        GreetingPillarCard(
            icon = "🔒",
            title = if (isEn) "100% Offline & Private" else "100% Ngoại tuyến & Riêng tư",
            description = if (isEn)
                "All books, bookmarks, and notes are stored strictly locally on your device. Zero cloud accounts required, read freely even in airplane mode."
            else
                "Toàn bộ sách, dấu trang và ghi chú lưu trữ cục bộ trên thiết bị của bạn. Không cần tài khoản đám mây, an tâm đọc ngay cả ở chế độ máy bay."
        )

        Spacer(modifier = Modifier.height(12.dp))

        GreetingPillarCard(
            icon = "📖",
            title = if (isEn) "Classic Literary Aesthetics" else "Thẩm mỹ Sách Cổ điển",
            description = if (isEn)
                "Experience tactile 3D embossed leather covers, editorial Serif typography, and seamless support for EPUB, MOBI, PDF, and TXT formats."
            else
                "Trải nghiệm bìa 3D giả da dập nổi ánh nhũ vàng sang trọng, phông chữ Serif biên tập và hỗ trợ các định dạng EPUB, MOBI, PDF, TXT mượt mà."
        )

        Spacer(modifier = Modifier.height(12.dp))

        GreetingPillarCard(
            icon = "✍️",
            title = if (isEn) "Highlights, Notes & Voice" else "Dạ quang & Ghi chú Suy ngẫm",
            description = if (isEn)
                "Capture inspiring passages with 4 archival highlighter tones, write reflective notes, and enjoy hands-free natural Text-to-Speech listening."
            else
                "Lưu trích đoạn truyền cảm hứng với 4 tông màu dạ quang chuẩn mực và nghe đọc to (Text-to-Speech) rảnh tay bất cứ lúc nào."
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

// ============================================================================
// Step 2: Reading Setup & Classics
// ============================================================================

@Composable
private fun StepReadingSetup(
    isEn: Boolean,
    selectedTheme: ReaderTheme,
    onThemeChange: (ReaderTheme) -> Unit,
    seedSampleBooks: Boolean,
    onSeedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isEn) "Ready Your Reading Space" else "Khởi tạo Không gian Đọc",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextParchment
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isEn)
                "Choose your preferred reading palette and initial book collection."
            else
                "Chọn bảng màu yêu thích và bộ sưu tập khởi đầu của bạn.",
            style = MaterialTheme.typography.bodySmall,
            color = TextLinen,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Initial Reading Theme Selection Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isEn) "🎨 Preferred Reading Palette" else "🎨 Chọn gam màu đọc sách ưa thích",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextParchment,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isEn)
                        "You can easily change this anytime inside the reader."
                    else
                        "Bạn có thể dễ dàng thay đổi lại bất cứ lúc nào trong trình đọc.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReaderTheme.entries.forEach { theme ->
                        val isSelected = selectedTheme == theme
                        val themeTitle = when (theme) {
                            ReaderTheme.DAY -> if (isEn) "Day" else "Giấy sáng"
                            ReaderTheme.SEPIA -> if (isEn) "Sepia" else "Giấy ngà"
                            ReaderTheme.NIGHT -> if (isEn) "Night" else "Đêm đen"
                            ReaderTheme.DARK_SEPIA -> if (isEn) "Espresso" else "Cà phê"
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.backgroundColor,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) PrimaryHover else BorderCharcoal
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .clickable { onThemeChange(theme) }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = themeTitle,
                                    color = theme.textColor,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                                if (isSelected) {
                                    Text(
                                        text = "●",
                                        color = PrimaryHover,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seed Sample Classics Option
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = CardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSeedChange(!seedSampleBooks) }
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = seedSampleBooks,
                    onCheckedChange = { onSeedChange(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryTerracotta,
                        uncheckedColor = TextMuted,
                        checkmarkColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEn)
                            "Pre-load 4 classic literary masterpieces"
                        else
                            "Nạp sẵn 4 tác phẩm văn học kinh điển mẫu",
                        color = TextParchment,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isEn)
                            "Sherlock Holmes, War & Peace, How to Win Friends, The Alchemist"
                        else
                            "Sherlock Holmes, Chiến Tranh & Hòa Bình, Đắc Nhân Tâm, Nhà Giả Kim",
                        color = TextLinen,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

// ============================================================================
// Shared Greeting Pillar Card
// ============================================================================

@Composable
fun GreetingPillarCard(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = icon,
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextParchment,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLinen,
                    lineHeight = 17.sp,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Greeting Screen Preview", widthDp = 390, heightDp = 844)
@Composable
fun GreetingScreenPreview() {
    FolioHavenComposeTheme {
        GreetingScreen(onGetStarted = { _, _, _ -> })
    }
}
