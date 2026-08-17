package com.example.quizlet.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.quizlet.data.FolderData
import com.example.quizlet.data.FolderRepository
import com.example.quizlet.data.LectureData
import com.example.quizlet.data.ROOT_FOLDER_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FolderViewModel(private val repository: FolderRepository) : ViewModel() {

    private val pathIds = MutableStateFlow(listOf(ROOT_FOLDER_ID))

    // Expose currentFolderId
    val currentFolderId: StateFlow<String> =
        pathIds.map { it.last() }.stateIn(viewModelScope, SharingStarted.Eagerly, ROOT_FOLDER_ID)

    val pathStack: StateFlow<List<FolderData>> = combine(repository.rootFlow, pathIds) { root, ids ->
        ids.mapNotNull { id -> repository.findFolder(root, id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val currentFolder: StateFlow<FolderData?> = combine(repository.rootFlow, currentFolderId) { root, id ->
        repository.findFolder(root, id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val subfolders: StateFlow<List<FolderData>> = currentFolder
        .map { it?.folders ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lectures: StateFlow<List<LectureData>> = currentFolder
        .map { it?.lectures ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun openFolder(folder: FolderData) {
        pathIds.value = pathIds.value + folder.id
    }

    fun goToBreadcrumb(index: Int) {
        val ids = pathIds.value
        if (index !in ids.indices) return
        pathIds.value = ids.take(index + 1)
    }

    fun goUp(): Boolean {
        if (pathIds.value.size <= 1) return false
        pathIds.value = pathIds.value.dropLast(1)
        return true
    }

    fun createFolder(name: String) = viewModelScope.launch {
        runCatching { repository.createFolder(name, currentFolderId.value) }
            .onFailure { _errorMessage.value = it.message }
    }

    fun renameFolder(folder: FolderData, newName: String) = viewModelScope.launch {
        runCatching { repository.renameFolder(folder.id, newName) }
            .onFailure { _errorMessage.value = it.message }
    }

    fun deleteFolder(folder: FolderData) = viewModelScope.launch {
        repository.deleteFolder(folder.id).onFailure { _errorMessage.value = it.message }
    }

    fun createLecture(title: String) = viewModelScope.launch {
        runCatching { repository.createLecture(title, currentFolderId.value) }
            .onFailure { _errorMessage.value = it.message }
    }

    fun renameLecture(lecture: LectureData, newTitle: String) = viewModelScope.launch {
        runCatching { repository.renameLecture(currentFolderId.value, lecture.id, newTitle) }
            .onFailure { _errorMessage.value = it.message }
    }

    fun deleteLecture(lecture: LectureData) = viewModelScope.launch {
        repository.deleteLecture(currentFolderId.value, lecture.id)
    }

    fun getItemCount(lectureId: String): Int {
        return repository.getLectureItemCount(lectureId)
    }

    fun getLecturePath(folderId: String, lectureId: String): String {
        return repository.getLecturePath(folderId, lectureId)
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

class FolderViewModelFactory(private val repository: FolderRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FolderViewModel(repository) as T
    }
}
