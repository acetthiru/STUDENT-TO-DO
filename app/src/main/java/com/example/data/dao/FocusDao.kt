package com.example.data.dao

import androidx.room.*
import com.example.data.model.FocusSession
import com.example.data.model.TaskLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_sessions ORDER BY completedAt DESC")
    fun getAllFocusSessions(): Flow<List<FocusSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSession): Long

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions")
    fun getTotalFocusMinutes(): Flow<Int?>
}

@Dao
interface TaskLogDao {
    @Query("SELECT * FROM task_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<TaskLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TaskLog): Long
}
