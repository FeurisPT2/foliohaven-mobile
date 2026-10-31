package com.example.foliohaven.data.model

import androidx.compose.ui.graphics.Color
import com.example.foliohaven.ui.theme.DarkSepiaBg
import com.example.foliohaven.ui.theme.DarkSepiaText
import com.example.foliohaven.ui.theme.DayBg
import com.example.foliohaven.ui.theme.DayText
import com.example.foliohaven.ui.theme.NightBg
import com.example.foliohaven.ui.theme.NightText
import com.example.foliohaven.ui.theme.SepiaBg
import com.example.foliohaven.ui.theme.SepiaText

enum class ReaderTheme(
    val title: String,
    val backgroundColor: Color,
    val textColor: Color
) {
    DAY("Ban ngày", DayBg, DayText),
    SEPIA("Parchment", SepiaBg, SepiaText),
    NIGHT("OLED Đêm", NightBg, NightText),
    DARK_SEPIA("Sepia Tối", DarkSepiaBg, DarkSepiaText)
}

data class BookRecord(
    val id: Long,
    val title: String,
    val author: String,
    val description: String? = null,
    val filePath: String,
    val fileSize: Long,
    val fileHash: String,
    val format: String,
    val coverPath: String? = null,
    val coverGradientStart: Long = 0xFF1E293BL,
    val coverGradientEnd: Long = 0xFF0F172AL,
    val category: String = "Tất cả sách",
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val progressPercent: Float = 0f,
    val lastOpenedAt: String = "",
    val isFinished: Boolean = false
)

data class BookmarkRecord(
    val id: Long = 0,
    val bookId: Long,
    val pageNumber: Int,
    val title: String,
    val snippet: String? = null,
    val createdAt: String = ""
)

data class AnnotationRecord(
    val id: Long = 0,
    val bookId: Long,
    val pageNumber: Int,
    val selectedText: String,
    val noteText: String? = null,
    val colorId: Int = 1, // 1=Amber, 2=Emerald, 3=Azure, 4=Rose
    val createdAt: String = ""
)
