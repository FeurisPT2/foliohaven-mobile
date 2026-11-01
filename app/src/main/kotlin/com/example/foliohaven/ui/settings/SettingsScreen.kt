package com.example.foliohaven.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.foliohaven.R
import com.example.foliohaven.data.LibraryStorageStats
import com.example.foliohaven.data.StorageManager
import com.example.foliohaven.data.model.ReaderTheme
import com.example.foliohaven.data.repository.BookRepository
import com.example.foliohaven.data.repository.UserPreferencesRepository
import com.example.foliohaven.model.AppInfo
import com.example.foliohaven.model.CreditItem
import com.example.foliohaven.ui.theme.BorderCharcoal
import com.example.foliohaven.ui.theme.BorderSubtle
import com.example.foliohaven.ui.theme.CardSurface
import com.example.foliohaven.ui.theme.CardSurfaceHover
import com.example.foliohaven.ui.theme.DangerCrimson
import com.example.foliohaven.ui.theme.PrimaryHover
import com.example.foliohaven.ui.theme.PrimaryPressed
import com.example.foliohaven.ui.theme.PrimarySubtle
import com.example.foliohaven.ui.theme.PrimaryTerracotta
import com.example.foliohaven.ui.theme.SuccessEmerald
import com.example.foliohaven.ui.theme.SurfaceEspresso
import com.example.foliohaven.ui.theme.TextDisabled
import com.example.foliohaven.ui.theme.TextLinen
import com.example.foliohaven.ui.theme.TextMuted
import com.example.foliohaven.ui.theme.TextParchment
import com.example.foliohaven.ui.theme.WarningAmber
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Production-ready FolioHaven Settings Screen.
 * Strictly adheres to FolioHaven literary archival branding and provides 4 dedicated sections:
 * 1. Reading Preferences Defaults (font size, line spacing, reader theme, margins)
 * 2. Text-to-Speech (TTS) Defaults (speech rate, pitch, language preference)
 * 3. Storage & Library Management (storage/reading stats, cache/history cleanup, metadata export/import)
 * 4. About & App Info (version, build, MIT licensing, credits, offline commitment guarantee)
 */
@Composable
fun SettingsScreen(
    bookRepository: BookRepository?,
    preferencesRepository: UserPreferencesRepository,
    onShowGreeting: () -> Unit = {},
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ------------------------------------------------------------------------
    // Reactive State Collection from UserPreferencesRepository
    // ------------------------------------------------------------------------
    val fontSize by preferencesRepository.defaultFontSize.collectAsStateWithLifecycle()
    val lineSpacing by preferencesRepository.defaultLineSpacing.collectAsStateWithLifecycle()
    val readerTheme by preferencesRepository.defaultReaderTheme.collectAsStateWithLifecycle()
    val marginsDp by preferencesRepository.defaultMarginsDp.collectAsStateWithLifecycle()

    val ttsSpeechRate by preferencesRepository.ttsSpeechRate.collectAsStateWithLifecycle()
    val ttsPitch by preferencesRepository.ttsPitch.collectAsStateWithLifecycle()
    val ttsLanguage by preferencesRepository.ttsLanguage.collectAsStateWithLifecycle()

    // ------------------------------------------------------------------------
    // Storage & Library Statistics State
    // ------------------------------------------------------------------------
    val storageManager = remember(context) { StorageManager(context) }
    var storageStats by remember {
        mutableStateOf(
            LibraryStorageStats(
                totalBooks = 0,
                readingMinutes = 0L,
                totalStorageBytes = 0L
            )
        )
    }

    fun refreshStats() {
        coroutineScope.launch {
            val stats = bookRepository?.getLibraryStorageStats() ?: storageManager.getLibraryStorageStats()
            storageStats = stats
        }
    }

    LaunchedEffect(Unit) {
        refreshStats()
    }

    fun notifyUser(message: String) {
        coroutineScope.launch {
            snackbarHostState?.showSnackbar(message)
        }
    }

    // ------------------------------------------------------------------------
    // SAF File Launchers for Export & Import
    // ------------------------------------------------------------------------
    val exportMetadataLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { destUri ->
            coroutineScope.launch {
                try {
                    val json = bookRepository?.exportMetadataJson()
                        ?: bookRepository?.exportUserDataJson()
                        ?: ""
                    context.contentResolver.openOutputStream(destUri)?.use { outStream ->
                        outStream.write(json.toByteArray(Charsets.UTF_8))
                    }
                    notifyUser("Đã xuất bản sao lưu siêu dữ liệu thành công!")
                } catch (e: Exception) {
                    notifyUser("Lỗi khi xuất tệp: ${e.message}")
                }
            }
        }
    }

    val importMetadataLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { srcUri ->
            coroutineScope.launch {
                try {
                    val json = context.contentResolver.openInputStream(srcUri)?.use { inStream ->
                        inStream.bufferedReader().readText()
                    } ?: ""
                    val result = bookRepository?.importMetadataJson(json)
                    if (result?.isSuccess == true) {
                        val imported = result.getOrNull()
                        val bookmarksCount = imported?.bookmarksRestored ?: 0
                        val notesCount = imported?.annotationsRestored ?: 0
                        notifyUser("Khôi phục thành công: $bookmarksCount dấu trang, $notesCount ghi chú")
                        refreshStats()
                    } else {
                        notifyUser("Lỗi khi khôi phục: ${result?.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    notifyUser("Lỗi khi đọc tệp: ${e.message}")
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // Offline Text-to-Speech Engine Preview State
    // ------------------------------------------------------------------------
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        ttsEngine = tts
        onDispose {
            try {
                tts.stop()
                tts.shutdown()
            } catch (_: Exception) {
            }
        }
    }

    fun playTtsPreview() {
        val tts = ttsEngine ?: return
        try {
            tts.stop()
            tts.setSpeechRate(ttsSpeechRate)
            tts.setPitch(ttsPitch)
            val locale = when (ttsLanguage) {
                "en-US" -> Locale.US
                "vi-VN" -> Locale.forLanguageTag("vi-VN")
                else -> Locale.getDefault()
            }
            tts.language = locale
            val previewMessage = if (ttsLanguage == "en-US") {
                "Welcome to FolioHaven, your quiet book sanctuary."
            } else {
                "FolioHaven xin chào. Đây là giọng đọc văn học của bạn."
            }
            tts.speak(previewMessage, TextToSpeech.QUEUE_FLUSH, null, "foliohaven_tts_preview")
        } catch (e: Exception) {
            notifyUser("Không thể phát âm thanh TTS: ${e.message}")
        }
    }

    // ------------------------------------------------------------------------
    // Confirmation Dialogs
    // ------------------------------------------------------------------------
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            containerColor = CardSurface,
            tonalElevation = 0.dp,
            title = {
                Text(
                    text = "Đặt lại lịch sử đọc sách?",
                    color = TextParchment,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "Toàn bộ tiến trình đọc và thời gian đọc sẽ được đặt lại về 0. Tất cả tệp sách, danh sách dấu trang và ghi chú của bạn vẫn được bảo toàn nguyên vẹn trên thiết bị.",
                    color = TextLinen,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearHistoryDialog = false
                        coroutineScope.launch {
                            val success = bookRepository?.clearReadingHistory() ?: false
                            if (success) {
                                refreshStats()
                                notifyUser("Đã đặt lại lịch sử đọc sách về 0!")
                            } else {
                                notifyUser("Không thể đặt lại lịch sử đọc sách.")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Xác nhận đặt lại", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearHistoryDialog = false }
                ) {
                    Text("Hủy bỏ", color = TextLinen, fontSize = 12.sp)
                }
            }
        )
    }

    // ------------------------------------------------------------------------
    // Main Scrollable Content
    // ------------------------------------------------------------------------
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingsGearIcon(color = PrimaryHover, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Cài đặt & Tùy biến",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextParchment,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Không gian lưu trữ văn học & Tùy biến trải nghiệm đọc riêng tư",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ====================================================================
        // CARD 1: Reading Preferences Defaults
        // ====================================================================
        ReadingPreferencesCard(
            currentTheme = readerTheme,
            onThemeChange = { preferencesRepository.setDefaultReaderTheme(it) },
            fontSize = fontSize,
            onFontSizeChange = { preferencesRepository.setDefaultFontSize(it) },
            lineSpacing = lineSpacing,
            onLineSpacingChange = { preferencesRepository.setDefaultLineSpacing(it) },
            marginsDp = marginsDp,
            onMarginsChange = { preferencesRepository.setDefaultMarginsDp(it) }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // ====================================================================
        // CARD 2: Text-to-Speech (TTS) Defaults
        // ====================================================================
        TtsPreferencesCard(
            speechRate = ttsSpeechRate,
            onSpeechRateChange = { preferencesRepository.setTtsSpeechRate(it) },
            pitch = ttsPitch,
            onPitchChange = { preferencesRepository.setTtsPitch(it) },
            language = ttsLanguage,
            onLanguageChange = { preferencesRepository.setTtsLanguage(it) },
            onPlaySample = { playTtsPreview() }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // ====================================================================
        // CARD 3: Storage & Library Management
        // ====================================================================
        StorageManagementCard(
            stats = storageStats,
            onClearCache = {
                coroutineScope.launch {
                    val success = bookRepository?.clearCache() ?: storageManager.clearCache()
                    if (success) {
                        refreshStats()
                        notifyUser("Đã dọn dẹp bộ nhớ đệm thành công!")
                    } else {
                        notifyUser("Không thể dọn dẹp bộ nhớ đệm.")
                    }
                }
            },
            onClearHistoryClick = { showClearHistoryDialog = true },
            onExportMetadata = { exportMetadataLauncher.launch("foliohaven_metadata_backup.json") },
            onImportMetadata = { importMetadataLauncher.launch(arrayOf("application/json", "text/*", "*/*")) },
            onSeedSampleBooks = {
                coroutineScope.launch {
                    bookRepository?.seedDefaultBooksIfEmpty()
                    refreshStats()
                    notifyUser("Đã nạp 4 tác phẩm văn học kinh điển mẫu thành công!")
                }
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // ====================================================================
        // CARD 4: About & App Info
        // ====================================================================
        AboutAppInfoCard(
            appInfo = AppInfo.DEFAULT,
            onShowGreeting = onShowGreeting,
            onOpenUrl = { url ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                } catch (_: Exception) {
                    notifyUser("Không thể mở liên kết: $url")
                }
            }
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

// ============================================================================
// Sub-Composable: Card 1 - Reading Preferences Defaults
// ============================================================================
@Composable
private fun ReadingPreferencesCard(
    currentTheme: ReaderTheme,
    onThemeChange: (ReaderTheme) -> Unit,
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    lineSpacing: Float,
    onLineSpacingChange: (Float) -> Unit,
    marginsDp: Int,
    onMarginsChange: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, BorderCharcoal),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Card Title Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                SlidersTuneIcon(color = PrimaryHover, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Mặc định giao diện đọc",
                        fontWeight = FontWeight.Bold,
                        color = TextParchment,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "Thiết lập mặc định cho mọi tác phẩm khi mở trong phòng đọc",
                        color = TextLinen,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Reader Theme Selector
            Text(
                text = "Giao diện trang sách mặc định",
                color = TextParchment,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReaderTheme.values().forEach { theme ->
                    val isSelected = theme == currentTheme
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.backgroundColor)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) PrimaryTerracotta else BorderCharcoal,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onThemeChange(theme) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = theme.title,
                                color = theme.textColor,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryTerracotta)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Default Font Size (12sp - 28sp) with Stepper & Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cỡ chữ mặc định",
                    color = TextParchment,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onFontSizeChange(fontSize - 1f) },
                        enabled = fontSize > 12f,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(CardSurfaceHover)
                            .border(1.dp, BorderCharcoal, CircleShape)
                    ) {
                        Text(
                            text = "A-",
                            color = if (fontSize > 12f) TextParchment else TextDisabled,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${fontSize.toInt()} sp",
                        color = PrimaryHover,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onFontSizeChange(fontSize + 1f) },
                        enabled = fontSize < 28f,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(CardSurfaceHover)
                            .border(1.dp, BorderCharcoal, CircleShape)
                    ) {
                        Text(
                            text = "A+",
                            color = if (fontSize < 28f) TextParchment else TextDisabled,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Slider(
                value = fontSize,
                onValueChange = { onFontSizeChange(it) },
                valueRange = 12f..28f,
                steps = 15,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryTerracotta,
                    activeTrackColor = PrimaryHover,
                    inactiveTrackColor = BorderCharcoal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Live Typography Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(currentTheme.backgroundColor)
                    .border(1.dp, BorderCharcoal, RoundedCornerShape(10.dp))
                    .padding(horizontal = marginsDp.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        text = "Trích đoạn xem trước",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = (fontSize + 1).sp,
                        color = currentTheme.textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "“Một cuốn sách hay trên giá là một người bạn tĩnh lặng đợi chờ chúng ta tìm đến.”",
                        fontFamily = FontFamily.Serif,
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * lineSpacing).sp,
                        color = currentTheme.textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Default Line Spacing (1.2 - 2.2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Giãn dòng mặc định",
                    color = TextParchment,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = String.format(Locale.US, "%.1fx", lineSpacing),
                    color = PrimaryHover,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }
            Slider(
                value = lineSpacing,
                onValueChange = { onLineSpacingChange(it) },
                valueRange = 1.2f..2.2f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryTerracotta,
                    activeTrackColor = PrimaryHover,
                    inactiveTrackColor = BorderCharcoal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Line Spacing Quick Preset Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1.4f, 1.6f, 1.8f, 2.0f).forEach { preset ->
                    val isSelected = kotlin.math.abs(lineSpacing - preset) < 0.05f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimarySubtle else CardSurfaceHover)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) PrimaryTerracotta else BorderCharcoal,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onLineSpacingChange(preset) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${preset}x",
                            color = if (isSelected) PrimaryHover else TextLinen,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Default Margins (12dp - 36dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lề trang sách mặc định",
                    color = TextParchment,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$marginsDp dp",
                    color = PrimaryHover,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }
            Slider(
                value = marginsDp.toFloat(),
                onValueChange = { onMarginsChange(it.toInt()) },
                valueRange = 12f..36f,
                steps = 11,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryTerracotta,
                    activeTrackColor = PrimaryHover,
                    inactiveTrackColor = BorderCharcoal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Margins Preset Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(16 to "Gọn (16dp)", 24 to "Chuẩn (24dp)", 32 to "Rộng (32dp)").forEach { (dpVal, label) ->
                    val isSelected = marginsDp == dpVal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimarySubtle else CardSurfaceHover)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) PrimaryTerracotta else BorderCharcoal,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onMarginsChange(dpVal) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) PrimaryHover else TextLinen,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// Sub-Composable: Card 2 - Text-to-Speech (TTS) Defaults
// ============================================================================
@Composable
private fun TtsPreferencesCard(
    speechRate: Float,
    onSpeechRateChange: (Float) -> Unit,
    pitch: Float,
    onPitchChange: (Float) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit,
    onPlaySample: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, BorderCharcoal),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Card Title Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                TtsVoiceIcon(color = PrimaryHover, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Mặc định giọng đọc (TTS)",
                        fontWeight = FontWeight.Bold,
                        color = TextParchment,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "Tùy biến tốc độ đọc, cao độ và ngôn ngữ tổng hợp giọng đọc ngoại tuyến",
                        color = TextLinen,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Speech Rate Slider (0.5x - 2.0x, default 1.0x)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tốc độ đọc",
                    color = TextParchment,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = String.format(Locale.US, "%.2fx", speechRate),
                    color = PrimaryHover,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }
            Slider(
                value = speechRate,
                onValueChange = { onSpeechRateChange(it) },
                valueRange = 0.5f..2.0f,
                steps = 14,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryTerracotta,
                    activeTrackColor = PrimaryHover,
                    inactiveTrackColor = BorderCharcoal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Speech Rate Preset Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { rate ->
                    val isSelected = kotlin.math.abs(speechRate - rate) < 0.04f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimarySubtle else CardSurfaceHover)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) PrimaryTerracotta else BorderCharcoal,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSpeechRateChange(rate) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${rate}x",
                            color = if (isSelected) PrimaryHover else TextLinen,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Pitch Slider (0.5x - 2.0x, default 1.0x)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cao độ giọng",
                    color = TextParchment,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = String.format(Locale.US, "%.2fx", pitch),
                    color = PrimaryHover,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }
            Slider(
                value = pitch,
                onValueChange = { onPitchChange(it) },
                valueRange = 0.5f..2.0f,
                steps = 14,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryTerracotta,
                    activeTrackColor = PrimaryHover,
                    inactiveTrackColor = BorderCharcoal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Pitch Preset Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.8f to "Trầm (0.8x)", 1.0f to "Chuẩn (1.0x)", 1.2f to "Thanh (1.2x)").forEach { (pVal, label) ->
                    val isSelected = kotlin.math.abs(pitch - pVal) < 0.05f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimarySubtle else CardSurfaceHover)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) PrimaryTerracotta else BorderCharcoal,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onPitchChange(pVal) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) PrimaryHover else TextLinen,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Language Selector
            Text(
                text = "Ngôn ngữ giọng đọc",
                color = TextParchment,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "vi-VN" to "Tiếng Việt (vi-VN)",
                    "en-US" to "English (en-US)",
                    "system" to "Hệ thống"
                ).forEach { (langCode, label) ->
                    val isSelected = language == langCode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimarySubtle else CardSurfaceHover)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) PrimaryTerracotta else BorderCharcoal,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onLanguageChange(langCode) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) PrimaryHover else TextLinen,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Test Voice Audio Button
            OutlinedButton(
                onClick = onPlaySample,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, PrimaryPressed),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = PrimarySubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "▶ Nghe thử giọng mẫu",
                    color = PrimaryHover,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )
            }
        }
    }
}

// ============================================================================
// Sub-Composable: Card 3 - Storage & Library Management
// ============================================================================
@Composable
private fun StorageManagementCard(
    stats: LibraryStorageStats,
    onClearCache: () -> Unit,
    onClearHistoryClick: () -> Unit,
    onExportMetadata: () -> Unit,
    onImportMetadata: () -> Unit,
    onSeedSampleBooks: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, BorderCharcoal),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Card Title Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                StorageDbIcon(color = PrimaryHover, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Lưu trữ & Thư viện",
                        fontWeight = FontWeight.Bold,
                        color = TextParchment,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "Thống kê dung lượng cục bộ và các công cụ dọn dẹp, sao lưu dữ liệu",
                        color = TextLinen,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Storage & Reading Statistics Display (3 Metric Boxes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Total Books
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardSurfaceHover)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${stats.totalBooks}",
                            color = PrimaryHover,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tổng số sách",
                            color = TextMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Metric 2: Total Reading Hours
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardSurfaceHover)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stats.formattedReadingTime,
                            color = PrimaryHover,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Thời gian đọc",
                            color = TextMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Metric 3: Total Storage Used
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardSurfaceHover)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stats.formattedStorage,
                            color = PrimaryHover,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Dung lượng",
                            color = TextMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-breakdown details
            Text(
                text = "Tệp sách: ${StorageManager.formatStorageSize(stats.booksStorageBytes)}  •  CSDL: ${StorageManager.formatStorageSize(stats.databaseStorageBytes)}  •  Bộ nhớ đệm: ${StorageManager.formatStorageSize(stats.cacheStorageBytes)}",
                color = TextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Action Buttons: Clear Cache & Clear History
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Clear Cache Button
                Button(
                    onClick = onClearCache,
                    colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceHover),
                    border = BorderStroke(1.dp, BorderCharcoal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TrashIcon(color = WarningAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dọn bộ nhớ đệm",
                            color = TextParchment,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Clear Reading History Button
                Button(
                    onClick = onClearHistoryClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceHover),
                    border = BorderStroke(1.dp, DangerCrimson),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TrashIcon(color = DangerCrimson, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Đặt lại lịch sử",
                            color = DangerCrimson,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Metadata Backup & Restore Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onExportMetadata,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTerracotta),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Xuất sao lưu JSON",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Button(
                    onClick = onImportMetadata,
                    colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceHover),
                    border = BorderStroke(1.dp, BorderCharcoal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Nhập sao lưu JSON",
                        color = TextParchment,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Seed Default Books Option
            OutlinedButton(
                onClick = onSeedSampleBooks,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BorderCharcoal),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = CardSurfaceHover),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📖 Nạp 4 tác phẩm văn học kinh điển mẫu",
                    color = PrimaryHover,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ============================================================================
// Sub-Composable: Card 4 - About & App Info
// ============================================================================
@Composable
private fun AboutAppInfoCard(
    appInfo: AppInfo,
    onShowGreeting: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, BorderCharcoal),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // App Identity Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceEspresso)
                        .border(1.dp, BorderCharcoal, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_foliohaven_logo),
                        contentDescription = "FolioHaven Logo",
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = appInfo.appName,
                        fontWeight = FontWeight.Bold,
                        color = TextParchment,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "Phiên bản ${appInfo.versionName} (Bản dựng #${appInfo.buildNumber})",
                        color = PrimaryHover,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = appInfo.tagline,
                        color = TextMuted,
                        fontSize = 11.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. 100% Offline Commitment Guarantee Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceEspresso),
                border = BorderStroke(1.dp, PrimaryPressed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ShieldCheckIcon(color = SuccessEmerald, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CAM KẾT 100% NGOẠI TUYẾN & BẢO MẬT",
                            fontWeight = FontWeight.Bold,
                            color = SuccessEmerald,
                            fontSize = 12.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = appInfo.offlineGuarantee,
                        color = TextLinen,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Licensing & Repository Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Giấy phép bản quyền:",
                    color = TextLinen,
                    fontSize = 12.5.sp
                )
                Text(
                    text = appInfo.license,
                    color = TextParchment,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    modifier = Modifier.clickable { onOpenUrl(appInfo.licenseUrl) }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kho mã nguồn:",
                    color = TextLinen,
                    fontSize = 12.5.sp
                )
                Text(
                    text = "GitHub / FolioHaven",
                    color = PrimaryHover,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp,
                    modifier = Modifier.clickable { onOpenUrl(appInfo.repositoryUrl) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Categorized Architectural Credits
            Text(
                text = "Công nghệ & Đội ngũ Kiến trúc",
                fontWeight = FontWeight.Bold,
                color = TextParchment,
                fontSize = 13.5.sp,
                fontFamily = FontFamily.Serif
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                appInfo.credits.forEach { credit: CreditItem ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardSurfaceHover)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = credit.name,
                                    color = TextParchment,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                                Text(
                                    text = credit.category,
                                    color = PrimaryHover,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = credit.description,
                                color = TextMuted,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Ecosystem Links
            Text(
                text = "Hệ sinh thái FolioHaven",
                fontWeight = FontWeight.Bold,
                color = TextParchment,
                fontSize = 13.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            listOf(
                Triple("Web Portal", "https://github.com/FeurisPT2/foliohaven", "Vercel / HTML5"),
                Triple("PC Desktop Client", "https://github.com/FeurisPT2/Foliohaven-pc", "Flutter / Dart"),
                Triple("Mobile Android Client", "https://github.com/FeurisPT2/foliohaven-mobile", "Jetpack Compose / Kotlin")
            ).forEach { (name, url, tech) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardSurfaceHover)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .clickable { onOpenUrl(url) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = name, color = TextParchment, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(text = tech, color = TextMuted, fontSize = 11.sp)
                    }
                    Text(text = "↗", color = PrimaryHover, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Welcome Screen Revisit Button
            Button(
                onClick = onShowGreeting,
                colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceHover),
                border = BorderStroke(1.dp, BorderCharcoal),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✨ Xem lại màn hình chào mừng",
                    color = PrimaryHover,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
