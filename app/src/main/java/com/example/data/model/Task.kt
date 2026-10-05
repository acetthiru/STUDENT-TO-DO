package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority(val label: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    URGENT("Urgent", 4)
}

enum class TaskCategory(val label: String, val iconName: String) {
    WORK("Work", "Briefcase"),
    PERSONAL("Personal", "User"),
    STUDY("Study", "Book"),
    HEALTH("Health", "Heart"),
    CODING("Coding", "Code")
}

data class Subtask(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: TaskCategory = TaskCategory.WORK,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val estimatedMinutes: Int = 30,
    val startTime: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + 3600_000, // 1 hour default
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val subtasksJson: String = "[]",
    val orderIndex: Int = 0,
    val postponementCount: Int = 0
)

enum class UrgencyState {
    NORMAL,
    APPROACHING,
    URGENT,
    OVERDUE
}

fun Task.getUrgencyState(nowTime: Long = System.currentTimeMillis()): UrgencyState {
    if (isCompleted) return UrgencyState.NORMAL
    val diffMillis = dueDate - nowTime
    return when {
        diffMillis < 0 -> UrgencyState.OVERDUE
        diffMillis <= 15 * 60 * 1000 -> UrgencyState.URGENT // within 15 min
        diffMillis <= 60 * 60 * 1000 -> UrgencyState.APPROACHING // within 1 hour
        else -> UrgencyState.NORMAL
    }
}
