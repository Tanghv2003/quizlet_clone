package com.example.quizlet

import android.app.Application
import com.example.quizlet.data.FolderRepository
import com.example.quizlet.data.JsonLibraryStore

class QuizletApplication : Application() {
    private val store: JsonLibraryStore by lazy { JsonLibraryStore.getInstance(this) }
    val folderRepository: FolderRepository by lazy { FolderRepository(store) }
}