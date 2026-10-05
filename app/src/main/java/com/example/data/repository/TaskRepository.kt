package com.example.data.repository

import com.example.data.dao.FocusDao
import com.example.data.dao.TaskDao
import com.example.data.dao.TaskLogDao
import com.example.data.model.FocusSession
import com.example.data.model.Subtask
import com.example.data.model.Task
import com.example.data.model.TaskCategory
import com.example.data.model.TaskLog
import com.example.data.model.TaskPriority
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class TaskRepository(
    private val taskDao: TaskDao,
    private val focusDao: FocusDao,
    private val taskLogDao: TaskLogDao
) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<Task>> = taskDao.getPendingTasks()
    val allFocusSessions: Flow<List<FocusSession>> = focusDao.getAllFocusSessions()
    val totalFocusMinutes: Flow<Int?> = focusDao.getTotalFocusMinutes()
    val allLogs: Flow<List<TaskLog>> = taskLogDao.getAllLogs()

    suspend fun insertTask(task: Task): Long {
        val id = taskDao.insertTask(task)
        taskLogDao.insertLog(TaskLog(taskId = id, taskTitle = task.title, action = "CREATED"))
        return id
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun completeTask(task: Task) {
        val updated = task.copy(
            isCompleted = true,
            completedAt = System.currentTimeMillis()
        )
        taskDao.updateTask(updated)
        taskLogDao.insertLog(
            TaskLog(
                taskId = task.id,
                taskTitle = task.title,
                action = "COMPLETED"
            )
        )
    }

    suspend fun toggleTaskCompletion(task: Task) {
        if (task.isCompleted) {
            val updated = task.copy(isCompleted = false, completedAt = null)
            taskDao.updateTask(updated)
        } else {
            completeTask(task)
        }
    }

    suspend fun postponeTask(task: Task, extraMinutes: Int) {
        val newDueDate = task.dueDate + (extraMinutes * 60 * 1000L)
        val updated = task.copy(
            dueDate = newDueDate,
            postponementCount = task.postponementCount + 1
        )
        taskDao.updateTask(updated)
        taskLogDao.insertLog(
            TaskLog(
                taskId = task.id,
                taskTitle = task.title,
                action = "POSTPONED"
            )
        )
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTaskById(task.id)
    }

    suspend fun logFocusSession(session: FocusSession) {
        focusDao.insertFocusSession(session)
    }

    suspend fun seedInitialDataIfEmpty() {
        if (taskDao.getTaskCount() == 0) {
            val now = System.currentTimeMillis()
            val seedTasks = listOf(
                Task(
                    title = "Design Motionistic Dynamic UI Component",
                    description = "Build smooth timeline NOW indicator and urgency progress ring in Jetpack Compose",
                    category = TaskCategory.CODING,
                    priority = TaskPriority.URGENT,
                    estimatedMinutes = 45,
                    startTime = now - 15 * 60 * 1000,
                    dueDate = now + 25 * 60 * 1000, // Due in 25 min (Approaching/Urgent)
                    isCompleted = false,
                    subtasksJson = serializeSubtasks(
                        listOf(
                            Subtask("1", "Setup Motion Animations", true),
                            Subtask("2", "Connect Room Database Flow", false),
                            Subtask("3", "Verify Touch Target Sizes", false)
                        )
                    )
                ),
                Task(
                    title = "Complete 25-Minute Deep Focus Sprint",
                    description = "Eliminate all distractions and tackle top priority task",
                    category = TaskCategory.WORK,
                    priority = TaskPriority.HIGH,
                    estimatedMinutes = 25,
                    startTime = now,
                    dueDate = now + 50 * 60 * 1000, // Due in 50 min
                    isCompleted = false
                ),
                Task(
                    title = "Review Weekly Productivity Insights",
                    description = "Check completion percentage trends and posture log",
                    category = TaskCategory.PERSONAL,
                    priority = TaskPriority.MEDIUM,
                    estimatedMinutes = 15,
                    startTime = now - 60 * 60 * 1000,
                    dueDate = now + 120 * 60 * 1000, // Due in 2 hours
                    isCompleted = false
                ),
                Task(
                    title = "Morning Work Breakdown & Standup",
                    description = "List high impact tasks for the day",
                    category = TaskCategory.WORK,
                    priority = TaskPriority.HIGH,
                    estimatedMinutes = 20,
                    startTime = now - 120 * 60 * 1000,
                    dueDate = now - 10 * 60 * 1000, // Overdue by 10 min to showcase overdue state!
                    isCompleted = true,
                    completedAt = now - 5 * 60 * 1000
                )
            )

            for (task in seedTasks) {
                taskDao.insertTask(task)
            }
        }
    }

    companion object {
        fun serializeSubtasks(list: List<Subtask>): String {
            val arr = JSONArray()
            list.forEach { item ->
                arr.put(JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("isCompleted", item.isCompleted)
                })
            }
            return arr.toString()
        }

        fun deserializeSubtasks(json: String): List<Subtask> {
            val result = mutableListOf<Subtask>()
            try {
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    result.add(
                        Subtask(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            title = obj.optString("title", ""),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }
    }
}
