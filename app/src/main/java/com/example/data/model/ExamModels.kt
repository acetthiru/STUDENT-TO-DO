package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_subjects")
data class ExamSubject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectName: String,
    val examDateMillis: Long,
    val targetScore: String = "90%",
    val totalTopics: Int = 10,
    val masteredTopics: Int = 0,
    val notes: String = ""
)

@Entity(tableName = "scanned_materials")
data class ScannedMaterial(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long? = null,
    val title: String,
    val imageUri: String? = null,
    val extractedText: String = "",
    val generatedTopicsJson: String = "[]",
    val scannedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val question: String,
    val answer: String,
    val masteryLevel: String = "NEW" // "NEW", "LEARNING", "MASTERED"
)
