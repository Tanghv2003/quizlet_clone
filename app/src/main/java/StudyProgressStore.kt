package com.example.quizlet.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class SpacedRepetitionData(
    val intervalDays: Double = 0.0, // repetition interval in days (0 = new)
    val repetitions: Int = 0,
    val easeFactor: Double = 2.5,
    val nextReviewTime: Long = 0L // timestamp in ms
)

@Serializable
data class LectureProgress(
    val lectureId: String,
    // map from item foreign text (or index) to repetition data
    val cardProgressMap: Map<String, SpacedRepetitionData> = emptyMap(),
    val lastStudiedAt: Long = 0L
)

class StudyProgressStore(context: Context) {
    private val file = File(context.filesDir, "study_progress.json")
    private val json = Json { ignoreUnknownKeys = true }

    fun loadProgress(lectureId: String): LectureProgress {
        val all = loadAll()
        return all[lectureId] ?: LectureProgress(lectureId = lectureId)
    }

    fun saveProgress(progress: LectureProgress) {
        val all = loadAll().toMutableMap()
        all[progress.lectureId] = progress
        runCatching {
            file.writeText(json.encodeToString(all))
        }
    }

    private fun loadAll(): Map<String, LectureProgress> {
        if (!file.exists()) return emptyMap()
        val text = runCatching { file.readText() }.getOrNull() ?: return emptyMap()
        return runCatching {
            json.decodeFromString<Map<String, LectureProgress>>(text)
        }.getOrElse { emptyMap() }
    }
}
