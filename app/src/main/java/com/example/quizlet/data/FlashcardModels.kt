package com.example.quizlet.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class FlashcardItem(
    val foreign: String,
    val native: String
)

/**
 * Parses and serializes flashcard items from/to lecture content JSON string.
 * Format in LectureData.content: JSON array of FlashcardItem.
 */
object LectureContentHelper {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    fun parseItems(content: String): List<FlashcardItem> {
        if (content.isBlank()) return emptyList()
        return runCatching {
            json.decodeFromString<List<FlashcardItem>>(content)
        }.getOrElse { emptyList() }
    }

    fun serializeItems(items: List<FlashcardItem>): String {
        return runCatching {
            json.encodeToString(items)
        }.getOrElse { "[]" }
    }

    val sampleJson: String = """[
  {
    "foreign": "1",
    "native": "oneeeabc"
  },
  {
    "foreign": "2",
    "native": "two"
  },
  {
    "foreign": "3",
    "native": "three"
  },
  {
    "foreign": "4",
    "native": "four"
  },
  {
    "foreign": "5",
    "native": "five"
  },
  {
    "foreign": "6",
    "native": "six"
  },
  {
    "foreign": "7",
    "native": "seven"
  },
  {
    "foreign": "8",
    "native": "eight"
  },
  {
    "foreign": "9",
    "native": "nine"
  },
  {
    "foreign": "10",
    "native": "ten"
  }
]""".trimIndent()
}
