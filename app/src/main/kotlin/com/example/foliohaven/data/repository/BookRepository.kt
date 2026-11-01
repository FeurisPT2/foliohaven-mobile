package com.example.foliohaven.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.foliohaven.data.ImportBackupResult
import com.example.foliohaven.data.LibraryStorageStats
import com.example.foliohaven.data.MetadataBackupManager
import com.example.foliohaven.data.StorageManager
import com.example.foliohaven.data.db.FolioHavenDbHelper
import com.example.foliohaven.data.model.AnnotationRecord
import com.example.foliohaven.data.model.BookRecord
import com.example.foliohaven.data.model.BookmarkRecord
import com.example.foliohaven.data.reader.DocumentEngineFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest

class BookRepository(
    private val context: Context,
    injectedDbHelper: FolioHavenDbHelper? = null,
    injectedStorageManager: StorageManager? = null,
    injectedPreferencesRepository: UserPreferencesRepository? = null
) {
    private val dbHelper = injectedDbHelper ?: FolioHavenDbHelper(context)
    private val storageManager = injectedStorageManager ?: StorageManager(context, dbHelper)
    private val preferencesRepository = injectedPreferencesRepository
    private val backupManager = MetadataBackupManager(dbHelper, preferencesRepository)

    private val _booksFlow = MutableStateFlow<List<BookRecord>>(emptyList())
    val booksFlow: StateFlow<List<BookRecord>> = _booksFlow.asStateFlow()

    private val _currentCategory = MutableStateFlow("Tất cả sách")
    val currentCategory: StateFlow<String> = _currentCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    suspend fun refreshBooks() = withContext(Dispatchers.IO) {
        val list = dbHelper.getAllBooks(_currentCategory.value, _searchQuery.value)
        _booksFlow.value = list
    }

    suspend fun setCategoryFilter(category: String) {
        _currentCategory.value = category
        refreshBooks()
    }

    suspend fun setSearchQuery(query: String) {
        _searchQuery.value = query
        refreshBooks()
    }

    suspend fun getBookById(id: Long): BookRecord? = withContext(Dispatchers.IO) {
        dbHelper.getBookById(id)
    }

    suspend fun updateBookMetadata(bookId: Long, title: String, author: String, category: String) =
        withContext(Dispatchers.IO) {
            val success = dbHelper.updateBookMetadata(bookId, title, author, category)
            refreshBooks()
            success
        }

    suspend fun deleteBook(bookId: Long) = withContext(Dispatchers.IO) {
        val book = dbHelper.getBookById(bookId)
        val success = dbHelper.deleteBook(bookId)
        if (success && book != null) {
            try {
                val file = File(book.filePath)
                if (file.exists() && file.parentFile?.absolutePath?.startsWith(context.filesDir.absolutePath) == true) {
                    file.delete()
                }
            } catch (_: Exception) {}
        }
        refreshBooks()
        success
    }

    suspend fun toggleBookFinished(bookId: Long, isFinished: Boolean) = withContext(Dispatchers.IO) {
        val success = dbHelper.toggleBookFinished(bookId, isFinished)
        refreshBooks()
        success
    }

    suspend fun updateReadingProgress(
        bookId: Long,
        currentPage: Int,
        totalPages: Int,
        elapsedSeconds: Int = 0
    ) = withContext(Dispatchers.IO) {
        dbHelper.updateReadingProgress(bookId, currentPage, totalPages, elapsedSeconds)
        refreshBooks()
    }

    suspend fun addBookmark(bookId: Long, pageNumber: Int, title: String, snippet: String? = null) =
        withContext(Dispatchers.IO) {
            dbHelper.addBookmark(bookId, pageNumber, title, snippet)
        }

    suspend fun getBookmarks(bookId: Long): List<BookmarkRecord> = withContext(Dispatchers.IO) {
        dbHelper.getBookmarks(bookId)
    }

    suspend fun getAllBookmarks(): List<FolioHavenDbHelper.BookmarkWithBook> = withContext(Dispatchers.IO) {
        dbHelper.getAllBookmarks()
    }

    suspend fun deleteBookmark(bookmarkId: Long) = withContext(Dispatchers.IO) {
        dbHelper.deleteBookmark(bookmarkId)
    }

    suspend fun addAnnotation(
        bookId: Long,
        pageNumber: Int,
        selectedText: String,
        noteText: String? = null,
        colorId: Int = 1
    ) = withContext(Dispatchers.IO) {
        dbHelper.addAnnotation(bookId, pageNumber, selectedText, noteText, colorId)
    }

    suspend fun getAnnotations(bookId: Long): List<AnnotationRecord> = withContext(Dispatchers.IO) {
        dbHelper.getAnnotations(bookId)
    }

    suspend fun getAllAnnotations(): List<FolioHavenDbHelper.AnnotationWithBook> = withContext(Dispatchers.IO) {
        dbHelper.getAllAnnotations()
    }

    suspend fun deleteAnnotation(annotationId: Long) = withContext(Dispatchers.IO) {
        dbHelper.deleteAnnotation(annotationId)
    }

    suspend fun getLibraryStorageStats(): LibraryStorageStats = withContext(Dispatchers.IO) {
        storageManager.getLibraryStorageStats()
    }

    suspend fun clearCache(): Boolean = withContext(Dispatchers.IO) {
        storageManager.clearCache()
    }

    suspend fun clearReadingHistory(): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.clearReadingHistory()
        if (success) {
            refreshBooks()
        }
        success
    }

    suspend fun exportMetadataJson(): String = withContext(Dispatchers.IO) {
        backupManager.exportMetadataJson()
    }

    suspend fun exportUserDataJson(): String = withContext(Dispatchers.IO) {
        backupManager.exportMetadataJson()
    }

    suspend fun importMetadataJson(jsonString: String): Result<ImportBackupResult> = withContext(Dispatchers.IO) {
        try {
            val result = backupManager.importMetadataJson(jsonString)
            refreshBooks()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importUserDataJson(jsonString: String): Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        try {
            val result = backupManager.importMetadataJson(jsonString)
            refreshBooks()
            Result.success(Pair(result.bookmarksRestored, result.annotationsRestored))
        } catch (e: Exception) {
            try {
                val legacyRes = dbHelper.importUserDataJson(jsonString)
                refreshBooks()
                Result.success(legacyRes)
            } catch (fallbackEx: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getReadingStats(): FolioHavenDbHelper.ReadingStats = withContext(Dispatchers.IO) {
        dbHelper.getReadingStats()
    }

    suspend fun importBookFromUri(uri: Uri): Result<BookRecord> = withContext(Dispatchers.IO) {
        try {
            var fileName = "imported_book"
            var fileSize = 0L

            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex != -1) fileName = it.getString(nameIndex) ?: fileName
                    if (sizeIndex != -1) fileSize = it.getLong(sizeIndex)
                }
            }

            val format = when {
                fileName.endsWith(".epub", ignoreCase = true) -> "EPUB"
                fileName.endsWith(".pdf", ignoreCase = true) -> "PDF"
                fileName.endsWith(".mobi", ignoreCase = true) -> "MOBI"
                else -> "TXT"
            }

            val booksDir = File(context.filesDir, "books").apply { if (!exists()) mkdirs() }
            val destFile = File(booksDir, "${System.currentTimeMillis()}_$fileName")

            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Could not open input stream for URI"))

            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var bytesRead: Int

            FileOutputStream(destFile).use { output ->
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                    output.write(buffer, 0, bytesRead)
                }
            }
            inputStream.close()

            val fileHash = digest.digest().joinToString("") { "%02x".format(it) }
            val finalSize = if (fileSize > 0) fileSize else destFile.length()

            // Calculate total pages / chapters
            val totalPages = try {
                val reader = DocumentEngineFactory.openDocument(destFile.absolutePath, format)
                val pages = reader.totalPages
                reader.close()
                pages
            } catch (_: Exception) {
                1
            }

            val cleanTitle = fileName.substringBeforeLast(".")
            val bookId = dbHelper.insertOrUpdateBook(
                title = cleanTitle,
                author = "Tác giả thư viện",
                description = "Sách đã nhập từ bộ nhớ thiết bị",
                filePath = destFile.absolutePath,
                fileSize = finalSize,
                fileHash = fileHash,
                format = format,
                coverPath = null,
                gradientStart = 0xFF1E293BL,
                gradientEnd = 0xFF0F172AL,
                category = format,
                initialTotalPages = totalPages
            )

            refreshBooks()
            val imported = dbHelper.getBookById(bookId)
                ?: return@withContext Result.failure(Exception("Failed to read inserted book"))
            Result.success(imported)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun seedDefaultBooksIfEmpty() = withContext(Dispatchers.IO) {
        val existing = dbHelper.getAllBooks()
        if (existing.isNotEmpty()) {
            _booksFlow.value = existing
            return@withContext
        }

        val booksDir = File(context.filesDir, "books").apply { if (!exists()) mkdirs() }

        // 1. Sherlock Holmes: The Sign of Four
        val file1 = File(booksDir, "sherlock_holmes.txt")
        if (!file1.exists()) {
            file1.writeText(
                """
                CHƯƠNG I: KHOA HỌC SUY LUẬN
                
                Sherlock Holmes lấy chiếc chai của mình từ góc lò sưởi và ống tiêm dưới da của mình từ chiếc hộp bằng da thuộc. Bằng những ngón tay dài, trắng, run rẩy, ông điều chỉnh chiếc kim tiêm mỏng manh và xắn tay áo sơ mi bên trái.
                
                Trong một lúc, đôi mắt ông trầm ngâm nhìn vào cẳng tay chằng chịt các vết chích. Sau đó, ông ấn mũi kim nhọn vào, nhấn pít-tông nhỏ xuống và ngả người vào chiếc ghế bành bọc nhung với một tiếng thở dài thỏa mãn.
                
                Ba lần một ngày trong suốt nhiều tháng, tôi đã chứng kiến cảnh tượng này, nhưng thói quen vẫn không làm cho tâm trí tôi quen với nó. Trái lại, ngày này qua ngày khác, tôi càng cảm thấy bực tức hơn trước cảnh tượng đó. Lương tâm tôi dằn vặt mỗi khi nghĩ rằng tôi thiếu dũng khí để can gián ông.
                
                "Hôm nay là gì?" Tôi hỏi, "Morphine hay cocaine?"
                
                Ông ngước mắt uể oải nhìn lên từ cuốn sách cổ mà ông vừa mở ra.
                
                "Đó là cocaine," ông nói, "dung dịch bảy phần trăm. Anh có muốn thử một chút không?"
                
                "Tuyệt đối không," tôi đáp cụt lủn. "Hệ thể chất của tôi chưa thể hồi phục sau chiến dịch Afghanistan. Tôi không thể chịu thêm những thử thách như vậy."
                
                Ông mỉm cười trước sự kịch liệt của tôi. "Có lẽ anh đúng, Watson," ông nói. "Tôi cho rằng tác dụng của nó về mặt thể chất là có hại. Tuy nhiên, nó kích thích não bộ một cách kỳ diệu, làm sáng tỏ mọi vấn đề."
                
                CHƯƠNG II: VỤ ÁN ĐƯỢC PHƠI BÀY
                
                Cô Mary Morstan bước vào căn phòng số 221B phố Baker với vẻ đĩnh đạc và bình tĩnh lạ thường. Cô là một thiếu nữ tóc vàng, ăn mặc trang nhã nhưng giản dị. Trong ánh mắt cô ánh lên nỗi lo âu sâu kín.
                
                "Cha tôi đã biến mất mười năm trước," cô bắt đầu kể. "Mỗi năm, vào đúng ngày này, tôi lại nhận được một viên ngọc trai quý giá gửi qua bưu điện không rõ người gửi. Và sáng nay, tôi nhận được lá thư này..."
                """.trimIndent()
            )
        }

        dbHelper.insertOrUpdateBook(
            title = "Sherlock Holmes: The Sign of Four",
            author = "Arthur Conan Doyle",
            description = "Tác phẩm trinh thám kinh điển với tài suy luận sắc bén của thám tử đại tài Sherlock Holmes.",
            filePath = file1.absolutePath,
            fileSize = file1.length(),
            fileHash = "hash_sherlock_holmes",
            format = "EPUB",
            coverPath = null,
            gradientStart = 0xFF1E293BL, // Archival Navy
            gradientEnd = 0xFF0F172AL,
            category = "Tiểu thuyết",
            initialTotalPages = 14
        )

        // 2. Chiến Tranh và Hòa Bình
        val file2 = File(booksDir, "chien_tranh_va_hoa_binh.txt")
        if (!file2.exists()) {
            file2.writeText(
                """
                CHIẾN TRANH VÀ HÒA BÌNH - LEO TOLSTOY
                
                PHẦN THỨ NHẤT
                
                "Eh bien, mon prince. Gênes et Lucques ne sont plus que des apanages, des поместья, de la famille Buonaparte..."
                
                Vào tháng 7 năm 1805, Anna Pavlovna Scherer, thị nữ danh dự và là người thân cận của Hoàng thái hậu Maria Feodorovna, đã mở đầu buổi dạ hội tiếp tân của mình bằng những lời lẽ đầy phẫn nộ hướng về phía Bonaparte.
                
                Những vị khách quý tộc Sankt-Peterburg lần lượt bước vào phòng khách xa hoa. Trong số họ có Pierre Bezukhov, chàng thanh niên vụng về nhưng có trái tim chân thành, vừa từ nước ngoài trở về, mang theo những tư tưởng cải cách tiến bộ.
                
                Cuộc sống thượng lưu Nga lúc bấy giờ tràn ngập các buổi khiêu vũ, những cuộc đàm đạo chính trị sâu sắc, đan xen cùng tiếng súng chiến trận đang ngày càng đến gần từ biên giới phía Tây...
                """.trimIndent()
            )
        }

        dbHelper.insertOrUpdateBook(
            title = "Chiến Tranh và Hòa Bình",
            author = "Leo Tolstoy",
            description = "Trường ca sử thi bất hủ về số phận con người và vận mệnh nước Nga trong bão lửa chiến tranh 1812.",
            filePath = file2.absolutePath,
            fileSize = file2.length(),
            fileHash = "hash_war_and_peace",
            format = "PDF",
            coverPath = null,
            gradientStart = 0xFF451A20L, // Archival Burgundy
            gradientEnd = 0xFF260D12L,
            category = "Kinh điển",
            initialTotalPages = 28
        )

        // 3. Đắc Nhân Tâm
        val file3 = File(booksDir, "dac_nhan_tam.txt")
        if (!file3.exists()) {
            file3.writeText(
                """
                ĐẮC NHÂN TÂM (HOW TO WIN FRIENDS & INFLUENCE PEOPLE) - DALE CARNEGIE
                
                NGUYÊN TẮC 1: KHÔNG CHỈ TRÍCH, OÁN TRÁCH HAY THAN PHIỀN
                
                Chỉ trích là vô bổ vì nó khiến đối phương phải đề phòng và thường thôi thúc họ tìm cách tự biện hộ. Chỉ trích là nguy hiểm vì nó làm tổn thương lòng tự trắc ẩn quý giá của con người, gây tổn hại đến ý thức về tầm quan trọng của họ và khơi dậy sự phẫn hận.
                
                B.F. Skinner, nhà tâm lý học nổi tiếng thế giới, đã chứng minh qua các thí nghiệm rằng một động vật được thưởng vì hành vi tốt sẽ học nhanh hơn và nhớ lâu hơn nhiều so với động vật bị trừng phạt vì hành vi xấu. Các nghiên cứu sau này cũng cho thấy điều tương tự áp dụng chính xác cho con người.
                
                NGUYÊN TẮC 2: THÀNH THẬT KHEN NGỢI VÀ BIẾT ƠN NGƯỜI KHÁC
                
                Chỉ có một cách duy nhất trên thế gian này để khiến bất kỳ ai làm bất cứ điều gì. Bạn đã bao giờ nghĩ về điều đó chưa? Đúng vậy, chỉ một cách duy nhất: Khiến người đó MUỐN làm điều đó.
                
                Mong muốn sâu sắc nhất trong bản chất con người là "khao khát được công nhận và cảm thấy mình quan trọng".
                """.trimIndent()
            )
        }

        dbHelper.insertOrUpdateBook(
            title = "Đắc Nhân Tâm",
            author = "Dale Carnegie",
            description = "Nghệ thuật đối nhân xử thế và thấu hiểu lòng người kinh điển của nhân loại.",
            filePath = file3.absolutePath,
            fileSize = file3.length(),
            fileHash = "hash_how_to_win_friends",
            format = "EPUB",
            coverPath = null,
            gradientStart = 0xFF143022L, // Archival Forest
            gradientEnd = 0xFF0B1F15L,
            category = "Kỹ năng sống",
            initialTotalPages = 16
        )

        // 4. Nhà Giả Kim
        val file4 = File(booksDir, "nha_gia_kim.txt")
        if (!file4.exists()) {
            file4.writeText(
                """
                NHÀ GIẢ KIM (THE ALCHEMIST) - PAULO COELHO
                
                Cậu chăn cừu Santiago thức dậy khi trời còn tối. Nhìn lên mái nhà thờ đổ nát, cậu thấy những vì sao lấp lánh qua những nhánh cây sung mọc nơi từng là phòng thánh.
                
                "Cừu của mình cần được dẫn ra đồng," cậu nghĩ thầm.
                
                Cậu đã chăn bầy cừu này suốt hai năm qua, đi khắp các vùng đồng cỏ xứ Andalusia. Cậu biết rõ tên từng con, con nào đi khập khiễng, con nào thích ăn cỏ non ở sườn đồi. Nhưng gần đây, có một giấc mơ kỳ lạ liên tục lặp lại...
                
                Một đứa trẻ dẫn cậu tới Kim Tự Tháp Ai Cập và bảo: "Nếu cậu đến đây, cậu sẽ tìm thấy một kho báu ẩn giấu."
                
                "Khi bạn thực sự khao khát một điều gì đó, toàn bộ vũ trụ sẽ hợp lực để giúp bạn đạt được nó."
                """.trimIndent()
            )
        }

        dbHelper.insertOrUpdateBook(
            title = "Nhà Giả Kim (The Alchemist)",
            author = "Paulo Coelho",
            description = "Hành trình theo đuổi Vận Mệnh Cá Nhân và lắng nghe tiếng nói của trái tim.",
            filePath = file4.absolutePath,
            fileSize = file4.length(),
            fileHash = "hash_the_alchemist",
            format = "EPUB",
            coverPath = null,
            gradientStart = 0xFF6E280AL, // Archival Terracotta
            gradientEnd = 0xFF451906L,
            category = "Tiểu thuyết",
            initialTotalPages = 12
        )

        _booksFlow.value = dbHelper.getAllBooks()
    }
}
