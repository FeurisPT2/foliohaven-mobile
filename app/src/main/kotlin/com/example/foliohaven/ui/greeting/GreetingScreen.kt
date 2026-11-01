package com.example.foliohaven.ui.greeting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.foliohaven.ui.theme.PrimaryTerracotta
import com.example.foliohaven.ui.theme.SurfaceEspresso
import com.example.foliohaven.ui.theme.TextLinen
import com.example.foliohaven.ui.theme.TextMuted
import com.example.foliohaven.ui.theme.TextParchment

@Composable
fun GreetingScreen(
    onGetStarted: (theme: ReaderTheme, seedSampleBooks: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTheme by remember { mutableStateOf(ReaderTheme.SEPIA) }
    var seedSampleBooks by remember { mutableStateOf(true) }

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
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

            // Brand Logo with Radiant Terracotta Glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(112.dp)
            ) {
                // Ambient Glow Halo
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

                // Logo Container
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

            // App Name & Tagline
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
                text = "Thư viện đọc sách tĩnh tâm • Ngoại tuyến 100%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryHover,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Nơi trú ẩn tĩnh lặng cho những tâm hồn yêu sách. Không quảng cáo, không theo dõi, không gián đoạn.",
                style = MaterialTheme.typography.bodySmall,
                color = TextLinen,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 3 Core Pillars Cards
            GreetingPillarCard(
                icon = "🔒",
                title = "100% Ngoại tuyến & Riêng tư",
                description = "Toàn bộ sách, dấu trang và ghi chú lưu trữ cục bộ trên thiết bị của bạn. Không cần tài khoản đám mây, an tâm đọc ngay cả ở chế độ máy bay."
            )

            Spacer(modifier = Modifier.height(12.dp))

            GreetingPillarCard(
                icon = "📖",
                title = "Thẩm mỹ Sách Cổ điển",
                description = "Trải nghiệm bìa 3D giả da dập nổi ánh nhũ vàng sang trọng, phông chữ Serif biên tập và hỗ trợ các định dạng EPUB, PDF, TXT mượt mà."
            )

            Spacer(modifier = Modifier.height(12.dp))

            GreetingPillarCard(
                icon = "✍️",
                title = "Dạ quang & Ghi chú Suy ngẫm",
                description = "Lưu trích đoạn truyền cảm hứng với 4 tông màu dạ quang chuẩn mực và nghe đọc to (Text-to-Speech) rảnh tay bất cứ lúc nào."
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Initial Reading Theme Selection
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderCharcoal),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎨 Chọn gam màu đọc sách ưa thích",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextParchment,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bạn có thể dễ dàng thay đổi lại bất cứ lúc nào trong trình đọc.",
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
                                    .clickable { selectedTheme = theme }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = theme.title,
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
                    .clickable { seedSampleBooks = !seedSampleBooks }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = seedSampleBooks,
                        onCheckedChange = { seedSampleBooks = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PrimaryTerracotta,
                            uncheckedColor = TextMuted,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nạp sẵn 4 tác phẩm văn học kinh điển mẫu",
                            color = TextParchment,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sherlock Holmes, Chiến Tranh & Hòa Bình, Đắc Nhân Tâm, Nhà Giả Kim",
                            color = TextLinen,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Primary Call to Action Button
            Button(
                onClick = { onGetStarted(selectedTheme, seedSampleBooks) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTerracotta),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(12.dp, RoundedCornerShape(14.dp))
            ) {
                Text(
                    text = "Bắt đầu hành trình đọc ▶",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}

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
        GreetingScreen(onGetStarted = { _, _ -> })
    }
}
