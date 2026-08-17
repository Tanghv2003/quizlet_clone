package com.example.quizlet.ui.study

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quizlet.data.FolderRepository
import com.example.quizlet.data.LectureContentHelper
import com.example.quizlet.data.FlashcardItem
import com.example.quizlet.data.LectureProgress
import com.example.quizlet.data.SpacedRepetitionData
import com.example.quizlet.data.StudyProgressStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

class LectureDetailViewModel(
    private val repository: FolderRepository,
    private val progressStore: StudyProgressStore,
    private val folderId: String,
    private val lectureId: String
) : ViewModel() {

    private val _items = MutableStateFlow<List<FlashcardItem>>(emptyList())
    val items: StateFlow<List<FlashcardItem>> = _items.asStateFlow()

    private val _lectureTitle = MutableStateFlow("")
    val lectureTitle: StateFlow<String> = _lectureTitle.asStateFlow()

    private var currentProgress: LectureProgress = LectureProgress(lectureId = lectureId)

    init {
        loadLecture()
    }

    private fun loadLecture() {
        val root = repository.rootFlow.value
        val folder = repository.findFolder(root, folderId)
        val lecture = folder?.lectures?.find { it.id == lectureId }
        if (lecture != null) {
            _lectureTitle.value = lecture.title
            val parsed = LectureContentHelper.parseItems(lecture.content)
            val initial = if (parsed.isEmpty()) {
                LectureContentHelper.parseItems(LectureContentHelper.sampleJson)
            } else {
                parsed
            }
            _items.value = initial
    fun addItem(foreign: String, native: String) {
        if (foreign.isBlank() || native.isBlank()) return
        val updated = _items.value + FlashcardItem(foreign.trim(), native.trim())
        saveItemsCompletely(updated)
    }

    fun editItem(index: Int, foreign: String, native: String) {
        val list = _items.value.toMutableList()
        if (index in list.indices) {
            list[index] = FlashcardItem(foreign.trim(), native.trim())
            saveItemsCompletely(list)
        }
    }

    fun deleteItem(index: Int) {
        val list = _items.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            saveItemsCompletely(list)
        }
    }

    fun importFromJson(jsonStr: String): Result<Unit> {
        val parsed = LectureContentHelper.parseItems(jsonStr)
        if (parsed.isEmpty()) {
            return Result.failure(IllegalArgumentException("JSON không hợp lệ hoặc không có từ vựng nào"))
        }
        saveItemsCompletely(parsed)
        return Result.success(Unit)
    }

    fun reviewCard(itemForeign: String, rating: Int) {
        val now = System.currentTimeMillis()
        val existing = currentProgress.cardProgressMap[itemForeign] ?: SpacedRepetitionData()

        var reps = existing.repetitions
        var interval = existing.intervalDays
        var ease = existing.easeFactor

        when (rating) {
            0 -> { reps = 0; interval = 1.0; ease = max(1.3, ease - 0.2) }
            1 -> { reps += 1; interval = if (reps == 1) 1.0 else interval * ease }
            2 -> { reps += 1; interval = if (reps == 1) 2.0 else interval * ease * 1.3 }
        }

        val nextTime = now + (interval * 24 * 60 * 60 * 1000).toLong()
        val newCardProgress = existing.copy(
            intervalDays = interval,
            repetitions = reps,
            easeFactor = ease,
            nextReviewTime = nextTime
        )

        val updatedMap = currentProgress.cardProgressMap.toMutableMap()
        updatedMap[itemForeign] = newCardProgress

        currentProgress = currentProgress.copy(
            cardProgressMap = updatedMap,
            lastStudiedAt = now
        )
        progressStore.saveProgress(currentProgress)
    }
}

class LectureDetailViewModelFactory(
    private val context: Context,
    private val repository: FolderRepository,
    private val folderId: String,
    private val lectureId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val store = StudyProgressStore(context)
        return LectureDetailViewModel(repository, store, folderId, lectureId) as T
    }
}

            if (parsed.isEmpty()) {
                saveItemsCompletely(initial)
            }
        }
        currentProgress = progressStore.loadProgress(lectureId)
    }

    fun saveItemsCompletely(newItems: List<FlashcardItem>) {
        _items.value = newItems
        val contentJson = LectureContentHelper.serializeItems(newItems)
        repository.updateLectureContent(folderId, lectureId, contentJson)
    }
