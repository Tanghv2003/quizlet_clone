package com.example.quizlet.data

import kotlinx.serialization.Serializable

// id cố định của thư mục gốc -> dùng để nhận diện & chặn xóa root ở bất kỳ đâu
const val ROOT_FOLDER_ID = "root"

/**
 * 1 bài giảng, nằm bên trong 1 thư mục (không cần lưu id thư mục cha,
 * vì trong JSON nó đã nằm lồng bên trong field "lectures" của đúng thư mục chứa nó).
 */
@Serializable
data class LectureData(
    val id: String,
    val title: String,
    val content: String = "",
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * 1 thư mục. "folders" là danh sách thư mục con (đệ quy),
 * "lectures" là danh sách bài giảng trực tiếp trong thư mục này.
 * Đây là cây thư mục, ánh xạ 1-1 sang JSON dạng object lồng nhau:
 *
 * {
 *   "id": "root", "name": "Thư mục gốc",
 *   "folders": [
 *     { "id": "...", "name": "Toán", "folders": [...], "lectures": [...] }
 *   ],
 *   "lectures": []
 * }
 */
@Serializable
data class FolderData(
    val id: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val folders: List<FolderData> = emptyList(),
    val lectures: List<LectureData> = emptyList()
) {
    val isRoot: Boolean get() = id == ROOT_FOLDER_ID
}