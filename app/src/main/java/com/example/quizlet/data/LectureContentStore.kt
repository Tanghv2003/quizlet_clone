package com.example.quizlet.data

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File

class LectureContentStore(private val context: Context) {

    private val lecturesDir: File = File(context.filesDir, "lectures").apply {
        if (!exists()) mkdirs()
    }

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val cache = mutableMapOf<String, String>()

    fun loadContent(lectureId: String): String {
        return cache[lectureId] ?: run {
            val file = File(lecturesDir, "$lectureId.json")
            if (file.exists()) {
                val content = file.readText()
                cache[lectureId] = content
                content
            } else {
                val sample = LectureContentHelper.sampleJson
                saveContent(lectureId, sample)
                sample
            }
        }
    }

    fun saveContent(lectureId: String, content: String) {
        val file = File(lecturesDir, "$lectureId.json")
        file.writeText(content)
        cache[lectureId] = content
    }

    fun getItemCount(lectureId: String): Int {
        val content = loadContent(lectureId)
        return LectureContentHelper.parseItems(content).size
    }

    fun deleteContent(lectureId: String) {
        val file = File(lecturesDir, "$lectureId.json")
        file.delete()
        cache.remove(lectureId)
    }

    fun getLectureFilePath(lectureId: String): String {
        return File(lecturesDir, "$lectureId.json").absolutePath
    }

    fun exists(lectureId: String): Boolean {
        return File(lecturesDir, "$lectureId.json").exists()
    }
}

