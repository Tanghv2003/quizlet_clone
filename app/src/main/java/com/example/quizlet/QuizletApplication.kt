package com.example.quizlet

import android.app.Application
import com.example.quizlet.data.FolderRepository
import com.example.quizlet.data.JsonLibraryStore
import com.example.quizlet.data.LectureContentStore

class QuizletApplication : Application() {
    private val store: JsonLibraryStore by lazy { JsonLibraryStore.getInstance(this) }
    private val contentStore: LectureContentStore by lazy { LectureContentStore(this) }
    val folderRepository: FolderRepository by lazy { FolderRepository(store, contentStore) }
}
