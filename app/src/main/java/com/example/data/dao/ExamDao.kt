package com.example.data.dao

import androidx.room.*
import com.example.data.model.ExamSubject
import com.example.data.model.Flashcard
import com.example.data.model.ScannedMaterial
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exam_subjects ORDER BY examDateMillis ASC")
    fun getAllExams(): Flow<List<ExamSubject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamSubject): Long

    @Update
    suspend fun updateExam(exam: ExamSubject)

    @Delete
    suspend fun deleteExam(exam: ExamSubject)

    @Query("SELECT * FROM scanned_materials ORDER BY scannedAt DESC")
    fun getAllScannedMaterials(): Flow<List<ScannedMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScannedMaterial(material: ScannedMaterial): Long

    @Query("SELECT * FROM flashcards WHERE subjectId = :subjectId")
    fun getFlashcardsForSubject(subjectId: Long): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard): Long

    @Update
    suspend fun updateFlashcard(flashcard: Flashcard)

    @Query("SELECT COUNT(*) FROM exam_subjects")
    suspend fun getExamCount(): Int
}
