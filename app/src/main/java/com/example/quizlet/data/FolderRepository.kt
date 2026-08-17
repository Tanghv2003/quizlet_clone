package com.example.quizlet.data



import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class FolderRepository(
    private val store: JsonLibraryStore,
    private val contentStore: LectureContentStore
) {

    val rootFlow: StateFlow<FolderData> = store.root

    fun findFolder(root: FolderData, targetId: String): FolderData? {
        if (root.id == targetId) return root
        for (child in root.folders) {
            findFolder(child, targetId)?.let { return it }
        }
        return null
    }

    fun createFolder(name: String, parentId: String) {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Tên thư mục không được để trống" }
        val now = System.currentTimeMillis()
        val newFolder = FolderData(id = UUID.randomUUID().toString(), name = trimmed, createdAt = now, updatedAt = now)
        store.mutate { root -> updateFolder(root, parentId) { it.copy(folders = it.folders + newFolder) } }
    }

    fun renameFolder(folderId: String, newName: String) {
        val trimmed = newName.trim()
        require(trimmed.isNotEmpty()) { "Tên thư mục không được để trống" }
        store.mutate { root ->
            updateFolder(root, folderId) { it.copy(name = trimmed, updatedAt = System.currentTimeMillis()) }
        }
    }

    fun deleteFolder(folderId: String): Result<Unit> {
        if (folderId == ROOT_FOLDER_ID) {
            return Result.failure(IllegalStateException("Không thể xóa thư mục gốc"))
        }
        store.mutate { root -> removeFolder(root, folderId) }
        return Result.success(Unit)
    }

    fun createLecture(title: String, folderId: String) {
        val trimmed = title.trim()
        require(trimmed.isNotEmpty()) { "Tên bài giảng không được để trống" }
        val now = System.currentTimeMillis()
        val lecture = LectureData(
            id = UUID.randomUUID().toString(),
            title = trimmed,
            createdAt = now,
            updatedAt = now
        )
        contentStore.saveContent(lecture.id, "[]")
        store.mutate { root -> updateFolder(root, folderId) { it.copy(lectures = it.lectures + lecture) } }
    }

    fun renameLecture(folderId: String, lectureId: String, newTitle: String) {
        val trimmed = newTitle.trim()
        require(trimmed.isNotEmpty()) { "Tên bài giảng không được để trống" }
        store.mutate { root ->
            updateFolder(root, folderId) { folder ->
                folder.copy(lectures = folder.lectures.map { lecture ->
                    if (lecture.id == lectureId) lecture.copy(title = trimmed, updatedAt = System.currentTimeMillis())
                    else lecture
                })
            }
        }
    }

    fun deleteLecture(folderId: String, lectureId: String) {
        contentStore.deleteContent(lectureId)
        store.mutate { root ->
            updateFolder(root, folderId) { folder ->
                folder.copy(lectures = folder.lectures.filterNot { it.id == lectureId })
            }
        }
    }

    fun updateLectureContent(lectureId: String, newContent: String) {
        contentStore.saveContent(lectureId, newContent)
        // Cập nhật updatedAt trong metadata (tùy chọn)
        store.mutate { root ->
            updateLectureMetadata(root, lectureId) { it.copy(updatedAt = System.currentTimeMillis()) }
        }
    }

    fun getLectureContent(lectureId: String): String {
        return contentStore.loadContent(lectureId)
    }

    fun getLectureItemCount(lectureId: String): Int {
        return contentStore.getItemCount(lectureId)
    }

    fun getLecturePath(folderId: String, lectureId: String): String {
        fun buildPath(node: FolderData, targetFolderId: String, path: MutableList<String>): Boolean {
            if (node.id == targetFolderId) {
                path.add(node.name)
                return true
            }
            for (child in node.folders) {
                if (buildPath(child, targetFolderId, path)) {
                    path.add(0, node.name)
                    return true
                }
            }
            return false
        }
        val path = mutableListOf<String>()
        val root = rootFlow.value
        if (buildPath(root, folderId, path)) {
            val folder = findFolder(root, folderId)
            val lecture = folder?.lectures?.find { it.id == lectureId }
            return path.joinToString(" > ") + " > " + (lecture?.title ?: "Unknown")
        }
        return "Không xác định"
    }

    private fun updateLectureMetadata(node: FolderData, lectureId: String, transform: (LectureData) -> LectureData): FolderData {
        return node.copy(
            lectures = node.lectures.map { lecture ->
                if (lecture.id == lectureId) transform(lecture) else lecture
            },
            folders = node.folders.map { updateLectureMetadata(it, lectureId, transform) }
        )
    }

    // Các hàm đệ quy
    private fun updateFolder(node: FolderData, targetId: String, transform: (FolderData) -> FolderData): FolderData {
        return if (node.id == targetId) {
            transform(node)
        } else {
            node.copy(folders = node.folders.map { updateFolder(it, targetId, transform) })
        }
    }

    private fun removeFolder(node: FolderData, targetId: String): FolderData {
        val remaining = node.folders.filterNot { it.id == targetId }
        return node.copy(folders = remaining.map { removeFolder(it, targetId) })
    }
}