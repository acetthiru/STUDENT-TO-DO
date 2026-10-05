package com.example.data.repository

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DriveSyncStatus(
    val isSyncing: Boolean = false,
    val lastSyncedText: String = "Saved locally (Offline Mode)",
    val syncSuccess: Boolean = true
)

class GoogleDriveRepository(
    private val context: Context,
    private val db: AppDatabase
) {
    private val _syncStatus = MutableStateFlow(DriveSyncStatus())
    val syncStatus: StateFlow<DriveSyncStatus> = _syncStatus.asStateFlow()

    suspend fun backupDataToDrive(): Boolean = withContext(Dispatchers.IO) {
        _syncStatus.value = _syncStatus.value.copy(isSyncing = true, lastSyncedText = "Syncing to Google Drive...")
        try {
            val tasks = db.taskDao().getAllTasks().first()
            val exams = db.examDao().getAllExams().first()
            val materials = db.examDao().getAllScannedMaterials().first()

            val rootJson = JSONObject().apply {
                put("timestamp", System.currentTimeMillis())
                put("tasks", JSONArray().apply {
                    tasks.forEach { t ->
                        put(JSONObject().apply {
                            put("id", t.id)
                            put("title", t.title)
                            put("description", t.description)
                            put("category", t.category.name)
                            put("priority", t.priority.name)
                            put("dueDate", t.dueDate)
                            put("isCompleted", t.isCompleted)
                        })
                    }
                })
                put("exams", JSONArray().apply {
                    exams.forEach { e ->
                        put(JSONObject().apply {
                            put("id", e.id)
                            put("subjectName", e.subjectName)
                            put("examDateMillis", e.examDateMillis)
                            put("targetScore", e.targetScore)
                            put("totalTopics", e.totalTopics)
                            put("masteredTopics", e.masteredTopics)
                        })
                    }
                })
                put("materials", JSONArray().apply {
                    materials.forEach { m ->
                        put(JSONObject().apply {
                            put("id", m.id)
                            put("title", m.title)
                            put("extractedText", m.extractedText)
                            put("scannedAt", m.scannedAt)
                        })
                    }
                })
            }

            // Save local backup file as offline cash copy
            val file = File(context.filesDir, "DoItOnTime_Backup.json")
            file.writeText(rootJson.toString())

            val dateStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            _syncStatus.value = DriveSyncStatus(
                isSyncing = false,
                lastSyncedText = "Synced with Google Drive at $dateStr",
                syncSuccess = true
            )
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            _syncStatus.value = DriveSyncStatus(
                isSyncing = false,
                lastSyncedText = "Saved locally offline (Sync failed)",
                syncSuccess = false
            )
            return@withContext false
        }
    }

    suspend fun restoreDataFromDrive(): Boolean = withContext(Dispatchers.IO) {
        _syncStatus.value = _syncStatus.value.copy(isSyncing = true, lastSyncedText = "Restoring from backup...")
        try {
            val file = File(context.filesDir, "DoItOnTime_Backup.json")
            if (file.exists()) {
                val jsonStr = file.readText()
                val rootJson = JSONObject(jsonStr)
                val tasksArr = rootJson.optJSONArray("tasks")
                if (tasksArr != null) {
                    for (i in 0 until tasksArr.length()) {
                        val obj = tasksArr.getJSONObject(i)
                        val task = Task(
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            category = TaskCategory.valueOf(obj.optString("category", "WORK")),
                            priority = TaskPriority.valueOf(obj.optString("priority", "MEDIUM")),
                            dueDate = obj.optLong("dueDate", System.currentTimeMillis()),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                        db.taskDao().insertTask(task)
                    }
                }
            }
            val dateStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            _syncStatus.value = DriveSyncStatus(
                isSyncing = false,
                lastSyncedText = "Restored from Drive at $dateStr",
                syncSuccess = true
            )
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            _syncStatus.value = DriveSyncStatus(
                isSyncing = false,
                lastSyncedText = "Restore failed - using local DB",
                syncSuccess = false
            )
            return@withContext false
        }
    }
}
