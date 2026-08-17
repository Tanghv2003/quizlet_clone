package com.example.quizlet.data

import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class FolderRepository(private val store: JsonLibraryStore) {

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

    /** Thư mục ROOT không bao giờ được xóa, dù gọi từ đâu. */
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
        val lecture = LectureData(id = UUID.randomUUID().toString(), title = trimmed, createdAt = now, updatedAt = now)
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
        store.mutate { root ->
            updateFolder(root, folderId) { folder ->
                folder.copy(lectures = folder.lectures.filterNot { it.id == lectureId })
            }
        }
    }
    fun updateLectureContent(folderId: String, lectureId: String, newContent: String) {
        store.mutate { root ->
            updateFolder(root, folderId) { folder ->
                folder.copy(lectures = folder.lectures.map { lecture ->
                    if (lecture.id == lectureId) lecture.copy(content = newContent, updatedAt = System.currentTimeMillis())
                    else lecture
                })
            }
        }
    }


    // ----- Các hàm đệ quy thao tác trên cây bất biến (immutable tree) -----

    /** Tìm node có id = targetId trong cây, áp dụng transform lên đúng node đó, giữ nguyên phần còn lại. */
    private fun updateFolder(node: FolderData, targetId: String, transform: (FolderData) -> FolderData): FolderData {
        return if (node.id == targetId) {
            transform(node)
        } else {
            node.copy(folders = node.folders.map { updateFolder(it, targetId, transform) })
        }
    }

    /** Xóa node có id = targetId khỏi cây (tìm trong danh sách con ở mọi cấp), kéo theo toàn bộ cây con của nó. */
    private fun removeFolder(node: FolderData, targetId: String): FolderData {
        val remaining = node.folders.filterNot { it.id == targetId }
        return node.copy(folders = remaining.map { removeFolder(it, targetId) })
    }
}
