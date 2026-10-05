package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String = "General Focus",
    val durationMinutes: Int,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "task_logs")
data class TaskLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val taskTitle: String,
    val action: String, // "COMPLETED", "POSTPONED", "CREATED"
    val timestamp: Long = System.currentTimeMillis()
)
