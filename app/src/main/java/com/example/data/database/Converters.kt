package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority

class Converters {
    @TypeConverter
    fun fromCategory(category: TaskCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): TaskCategory {
        return try {
            TaskCategory.valueOf(value)
        } catch (e: Exception) {
            TaskCategory.WORK
        }
    }

    @TypeConverter
    fun fromPriority(priority: TaskPriority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): TaskPriority {
        return try {
            TaskPriority.valueOf(value)
        } catch (e: Exception) {
            TaskPriority.MEDIUM
        }
    }
}
