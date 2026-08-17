package com.example.quizlet.data

import kotlinx.serialization.Serializable

const val ROOT_FOLDER_ID = "root"

@Serializable
data class LectureData(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)

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