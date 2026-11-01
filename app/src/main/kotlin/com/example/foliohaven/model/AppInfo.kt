package com.example.foliohaven.model

/**
 * Domain model and application constants representing FolioHaven Mobile's
 * release metadata, architectural invariants, licensing, and offline commitment guarantee.
 *
 * Adheres strictly to PROJECT.md:
 * - Version name: "1.0"
 * - Build number: 1
 * - Licensing: "MIT License"
 * - Credits: list of contributors/technologies
 * - Offline commitment guarantee text
 */
data class AppInfo(
    val appName: String = APP_NAME,
    val versionName: String = VERSION_NAME,
    val buildNumber: Int = BUILD_NUMBER,
    val license: String = LICENSE,
    val licenseUrl: String = LICENSE_URL,
    val repositoryUrl: String = REPO_URL,
    val tagline: String = TAGLINE,
    val taglineEn: String = TAGLINE_EN,
    val offlineGuarantee: String = OFFLINE_GUARANTEE,
    val offlineGuaranteeEn: String = OFFLINE_GUARANTEE_EN,
    val credits: List<CreditItem> = DEFAULT_CREDITS
) {
    companion object {
        const val APP_NAME = "FolioHaven Mobile"
        const val VERSION_NAME = "1.0"
        const val BUILD_NUMBER = 1
        const val LICENSE = "MIT License"
        const val LICENSE_URL = "https://opensource.org/licenses/MIT"
        const val REPO_URL = "https://github.com/foliohaven/foliohaven-mobile"
        const val TAGLINE = "Nơi trú ẩn tĩnh lặng cho tâm hồn người đọc"
        const val TAGLINE_EN = "A calm, archival book sanctuary"

        const val OFFLINE_GUARANTEE =
            "FolioHaven cam kết 100% Offline & Bảo mật Riêng tư tuyệt đối: " +
            "Ứng dụng hoàn toàn không khai báo bất kỳ quyền truy cập Internet nào trong AndroidManifest.xml (0 network permissions), " +
            "không thu thập dữ liệu hành vi, không gửi telemetry, không hiển thị quảng cáo, và không yêu cầu đăng nhập đám mây. " +
            "Toàn bộ sách, ghi chú, dấu trang và lịch sử đọc lưu trữ duy nhất trên bộ nhớ cục bộ của thiết bị bạn."

        const val OFFLINE_GUARANTEE_EN =
            "FolioHaven is architected with a strict 100% Offline & Privacy-First Guarantee: " +
            "The app declares zero Internet permissions in AndroidManifest.xml, transmits zero telemetry, " +
            "contains zero trackers or advertisements, and requires no cloud authentication. " +
            "Your literary library, personal reading statistics, bookmarks, and annotations reside exclusively on your local device."

        val DEFAULT_CREDITS = listOf(
            CreditItem(
                category = "Kiến trúc Giao diện & Trải nghiệm",
                name = "Jetpack Compose & Material 3",
                description = "Nền tảng giao diện khai báo hiện đại với luồng dữ liệu một chiều (UDF)."
            ),
            CreditItem(
                category = "Động cơ Đọc Sách Đa Định dạng",
                name = "Readium & Android PdfRenderer",
                description = "Giải mã EPUB, PDF, MOBI và văn bản thuần túy với hiệu năng phần cứng tối ưu."
            ),
            CreditItem(
                category = "Bản sắc Thiết kế Văn học",
                name = "FolioHaven Literary Design System",
                description = "Bảng màu Terracotta & Espresso cùng hệ thống Canvas Vector Icons thuần túy không phụ thuộc thư viện ngoài."
            ),
            CreditItem(
                category = "Bảo quản Dữ liệu Cục bộ",
                name = "SQLite & SharedPreferences",
                description = "Cơ sở dữ liệu cục bộ bảo đảm lưu trữ an toàn, tức thì và độc lập hoàn toàn với mạng."
            ),
            CreditItem(
                category = "Giọng đọc Trợ năng",
                name = "Android Text-to-Speech (TTS)",
                description = "Động cơ chuyển đổi văn bản thành giọng đọc đa ngôn ngữ tự nhiên ngay trên thiết bị."
            )
        )

        /** Singleton default instance for direct consumption in Compose UI */
        val DEFAULT = AppInfo()
    }
}

/**
 * Entry representing an architectural component, technology, or contributor credit.
 */
data class CreditItem(
    val category: String,
    val name: String,
    val description: String
)
