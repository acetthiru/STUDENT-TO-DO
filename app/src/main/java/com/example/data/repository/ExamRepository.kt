package com.example.data.repository

import com.example.data.dao.ExamDao
import com.example.data.model.ExamSubject
import com.example.data.model.Flashcard
import com.example.data.model.ScannedMaterial
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import java.util.UUID

class ExamRepository(
    private val examDao: ExamDao
) {
    val allExams: Flow<List<ExamSubject>> = examDao.getAllExams()
    val allScannedMaterials: Flow<List<ScannedMaterial>> = examDao.getAllScannedMaterials()

    fun getFlashcards(subjectId: Long): Flow<List<Flashcard>> = examDao.getFlashcardsForSubject(subjectId)

    suspend fun insertExam(exam: ExamSubject): Long = examDao.insertExam(exam)

    suspend fun updateExam(exam: ExamSubject) = examDao.updateExam(exam)

    suspend fun deleteExam(exam: ExamSubject) = examDao.deleteExam(exam)

    suspend fun addScannedMaterial(
        title: String,
        imageUri: String?,
        rawText: String,
        subjectId: Long?
    ): Long {
        val extractedTopics = extractTopicsFromScannedText(rawText)
        val jsonArr = JSONArray().apply {
            extractedTopics.forEach { put(it) }
        }
        val material = ScannedMaterial(
            subjectId = subjectId,
            title = title,
            imageUri = imageUri,
            extractedText = rawText,
            generatedTopicsJson = jsonArr.toString()
        )
        val materialId = examDao.insertScannedMaterial(material)

        // Generate Flashcards automatically from scanned material
        if (subjectId != null) {
            extractedTopics.forEach { topic ->
                examDao.insertFlashcard(
                    Flashcard(
                        subjectId = subjectId,
                        question = "What is key concept: $topic?",
                        answer = "Essential study formula / definition extracted from scanned notes for '$topic'."
                    )
                )
            }
        }
        return materialId
    }

    private fun extractTopicsFromScannedText(text: String): List<String> {
        if (text.isBlank()) {
            return listOf("Key Chapter Summary", "Formula & Derivations", "Important Exam Questions")
        }
        val lines = text.split("\n", ".").filter { it.trim().length > 3 }
        return if (lines.isNotEmpty()) lines.take(5).map { it.trim() }
        else listOf("General Exam Topic 1", "General Exam Topic 2")
    }

    suspend fun seedInitialExamsIfEmpty() {
        if (examDao.getExamCount() == 0) {
            val now = System.currentTimeMillis()
            val mathExamId = examDao.insertExam(
                ExamSubject(
                    subjectName = "Calculus & Linear Algebra Final",
                    examDateMillis = now + (12 * 24 * 3600_000L), // 12 days from now
                    targetScore = "95%",
                    totalTopics = 12,
                    masteredTopics = 8,
                    notes = "Focus on Vector Calculus, Eigenvalues, and Differential Equations."
                )
            )

            val csExamId = examDao.insertExam(
                ExamSubject(
                    subjectName = "Data Structures & Algorithms Exam",
                    examDateMillis = now + (5 * 24 * 3600_000L), // 5 days from now
                    targetScore = "90%",
                    totalTopics = 10,
                    masteredTopics = 6,
                    notes = "Review Dynamic Programming, Graph Traversal, and Tree Balancing."
                )
            )

            // Seed sample scanned study notes
            examDao.insertScannedMaterial(
                ScannedMaterial(
                    subjectId = mathExamId,
                    title = "Chapter 4 Vector Calculus Formula Sheet",
                    extractedText = "Green's Theorem formula. Divergence and Curl in 3D. Line integrals along closed curves.",
                    generatedTopicsJson = "[\"Green's Theorem\", \"Divergence & Curl\", \"Line Integrals\"]"
                )
            )

            // Seed flashcards
            val flashcards = listOf(
                Flashcard(
                    subjectId = mathExamId,
                    question = "State Green's Theorem formula in 2D vector field.",
                    answer = "∮_C (P dx + Q dy) = ∬_R (∂Q/∂x - ∂P/∂y) dA",
                    masteryLevel = "MASTERED"
                ),
                Flashcard(
                    subjectId = mathExamId,
                    question = "How to calculate the gradient ∇f in 3D space?",
                    answer = "∇f = (∂f/∂x)i + (∂f/∂y)j + (∂f/∂z)k",
                    masteryLevel = "LEARNING"
                ),
                Flashcard(
                    subjectId = csExamId,
                    question = "What is the time complexity of Dijkstra's algorithm with Min-Heap?",
                    answer = "O((V + E) log V)",
                    masteryLevel = "MASTERED"
                )
            )

            flashcards.forEach { examDao.insertFlashcard(it) }
        }
    }
}
