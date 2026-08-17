package com.example.quizlet.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Lưu toàn bộ cây thư mục vào 1 file JSON nằm trong bộ nhớ riêng của app
 * (context.filesDir) -> không cần quyền, không cần mạng, chỉ app này đọc/ghi được.
 *
 * Cách hoạt động: toàn bộ cây được giữ trong bộ nhớ (MutableStateFlow<FolderData>)
 * để UI luôn đọc nhanh; mỗi lần thay đổi (thêm/sửa/xóa) sẽ ghi đè lại file JSON
 * trên background thread (Dispatchers.IO).
 */
class JsonLibraryStore private constructor(context: Context) {

    private val file: File = File(context.filesDir, "library.json")
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val writeMutex = Mutex()

    private val _root = MutableStateFlow(loadOrCreateRoot())
    val root: StateFlow<FolderData> = _root.asStateFlow()

    private fun loadOrCreateRoot(): FolderData {
        if (file.exists()) {
            val text = runCatching { file.readText() }.getOrNull()
            if (!text.isNullOrBlank()) {
                runCatching { json.decodeFromString<FolderData>(text) }.getOrNull()?.let { return it }
            }
        }
        // Chưa có file (lần đầu mở app) hoặc file lỗi -> tạo thư mục gốc mặc định
        val now = System.currentTimeMillis()
        val defaultRoot = FolderData(id = ROOT_FOLDER_ID, name = "Thư mục gốc", createdAt = now, updatedAt = now)
        writeToFile(defaultRoot)
        return defaultRoot
    }

    /**
     * Áp dụng 1 thay đổi lên cây (thêm/sửa/xóa thư mục hoặc bài giảng),
     * cập nhật state trong bộ nhớ ngay lập tức, rồi ghi file JSON ở nền.
     */
    fun mutate(transform: (FolderData) -> FolderData) {
        val updated = transform(_root.value)
        _root.value = updated
        ioScope.launch {
            writeMutex.withLock { writeToFile(updated) }
        }
    }

    private fun writeToFile(data: FolderData) {
        runCatching {
            file.writeText(json.encodeToString(data))
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: JsonLibraryStore? = null

        fun getInstance(context: Context): JsonLibraryStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: JsonLibraryStore(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
