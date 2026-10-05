package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.ExamRepository
import com.example.data.repository.GeminiAssistantRepository
import com.example.data.repository.GoogleDriveRepository
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FocusTimerUiState(
    val isRunning: Boolean = false,
    val selectedTask: Task? = null,
    val initialMinutes: Int = 25,
    val totalSeconds: Int = 25 * 60,
    val secondsRemaining: Int = 25 * 60,
    val startEpochMillis: Long = 0L,
    val isCompleted: Boolean = false
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val examRepository: ExamRepository
    private val driveRepository: GoogleDriveRepository
    private val geminiRepository = GeminiAssistantRepository()

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    private val _isHighMotion = MutableStateFlow(true)
    val isHighMotion: StateFlow<Boolean> = _isHighMotion.asStateFlow()

    private val _focusTimerState = MutableStateFlow(FocusTimerUiState())
    val focusTimerState: StateFlow<FocusTimerUiState> = _focusTimerState.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _showCelebration = MutableStateFlow(false)
    val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()

    private var focusTimerJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TaskRepository(db.taskDao(), db.focusDao(), db.taskLogDao())
        examRepository = ExamRepository(db.examDao())
        driveRepository = GoogleDriveRepository(application, db)

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            examRepository.seedInitialExamsIfEmpty()
        }
        startClockTimer()
    }

    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasks: StateFlow<List<Task>> = repository.pendingTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExams: StateFlow<List<ExamSubject>> = examRepository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scannedMaterials: StateFlow<List<ScannedMaterial>> = examRepository.allScannedMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val driveSyncStatus = driveRepository.syncStatus

    val allFocusSessions: StateFlow<List<FocusSession>> = repository.allFocusSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFocusMinutes: StateFlow<Int> = repository.totalFocusMinutes
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val taskLogs: StateFlow<List<TaskLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completionPercentage: StateFlow<Float> = tasks.map { list ->
        if (list.isEmpty()) 0f
        else {
            val completed = list.count { it.isCompleted }
            completed.toFloat() / list.size.toFloat()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    private fun startClockTimer() {
        viewModelScope.launch {
            while (true) {
                _currentTime.value = System.currentTimeMillis()
                delay(1000)
            }
        }
    }

    // Google Drive Actions
    fun backupToDrive() {
        viewModelScope.launch {
            driveRepository.backupDataToDrive()
        }
    }

    fun restoreFromDrive() {
        viewModelScope.launch {
            driveRepository.restoreDataFromDrive()
        }
    }

    // Exam Actions
    fun createExamSubject(subjectName: String, examDateMillis: Long, targetScore: String, totalTopics: Int) {
        viewModelScope.launch {
            examRepository.insertExam(
                ExamSubject(
                    subjectName = subjectName,
                    examDateMillis = examDateMillis,
                    targetScore = targetScore,
                    totalTopics = totalTopics,
                    masteredTopics = 0
                )
            )
        }
    }

    fun generateAiExamSchedule(
        subjectName: String,
        topics: List<String>,
        daysRemaining: Int,
        onResult: (List<Subtask>) -> Unit
    ) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val result = geminiRepository.generateExamStudySchedule(subjectName, topics, daysRemaining)
            _isAiLoading.value = false
            onResult(result)
        }
    }

    fun addScannedWork(title: String, imageUri: String?, rawText: String, subjectId: Long?) {
        viewModelScope.launch {
            examRepository.addScannedMaterial(title, imageUri, rawText, subjectId)
        }
    }

    fun getFlashcards(subjectId: Long) = examRepository.getFlashcards(subjectId)

    // Task Actions
    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
            val currentList = tasks.value
            val uncompletedCount = currentList.count { !it.isCompleted && it.id != task.id }
            if (!task.isCompleted && uncompletedCount == 0 && currentList.isNotEmpty()) {
                _showCelebration.value = true
            }
        }
    }

    fun dismissCelebration() {
        _showCelebration.value = false
    }

    fun postponeTask(task: Task, extraMinutes: Int) {
        viewModelScope.launch {
            repository.postponeTask(task, extraMinutes)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun createWorkTask(
        title: String,
        description: String,
        category: TaskCategory,
        priority: TaskPriority,
        estimatedMinutes: Int,
        dueDateMillis: Long,
        subtasks: List<Subtask> = emptyList()
    ) {
        viewModelScope.launch {
            val newTask = Task(
                title = title,
                description = description,
                category = category,
                priority = priority,
                estimatedMinutes = estimatedMinutes,
                startTime = System.currentTimeMillis(),
                dueDate = dueDateMillis,
                subtasksJson = TaskRepository.serializeSubtasks(subtasks)
            )
            repository.insertTask(newTask)
        }
    }

    fun generateSubtasksWithAi(title: String, description: String, onResult: (List<Subtask>) -> Unit) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val result = geminiRepository.breakDownTask(title, description)
            _isAiLoading.value = false
            onResult(result)
        }
    }

    fun toggleSubtask(task: Task, subtaskId: String) {
        viewModelScope.launch {
            val currentSubtasks = TaskRepository.deserializeSubtasks(task.subtasksJson)
            val updated = currentSubtasks.map {
                if (it.id == subtaskId) it.copy(isCompleted = !it.isCompleted) else it
            }
            val updatedTask = task.copy(subtasksJson = TaskRepository.serializeSubtasks(updated))
            repository.updateTask(updatedTask)
        }
    }

    // Focus Timer
    fun setFocusTask(task: Task?) {
        _focusTimerState.value = _focusTimerState.value.copy(selectedTask = task)
    }

    fun setFocusDuration(minutes: Int) {
        val totalSec = minutes * 60
        _focusTimerState.value = _focusTimerState.value.copy(
            initialMinutes = minutes,
            totalSeconds = totalSec,
            secondsRemaining = totalSec,
            isCompleted = false
        )
    }

    fun startFocusTimer() {
        if (_focusTimerState.value.isRunning) return
        val now = System.currentTimeMillis()
        val currentState = _focusTimerState.value
        _focusTimerState.value = currentState.copy(
            isRunning = true,
            startEpochMillis = now,
            isCompleted = false
        )

        focusTimerJob?.cancel()
        focusTimerJob = viewModelScope.launch {
            while (_focusTimerState.value.isRunning && _focusTimerState.value.secondsRemaining > 0) {
                delay(1000)
                val remaining = _focusTimerState.value.secondsRemaining - 1
                if (remaining <= 0) {
                    _focusTimerState.value = _focusTimerState.value.copy(
                        secondsRemaining = 0,
                        isRunning = false,
                        isCompleted = true
                    )
                    logFocusCompletion()
                    break
                } else {
                    _focusTimerState.value = _focusTimerState.value.copy(secondsRemaining = remaining)
                }
            }
        }
    }

    fun pauseFocusTimer() {
        focusTimerJob?.cancel()
        _focusTimerState.value = _focusTimerState.value.copy(isRunning = false)
    }

    fun resetFocusTimer() {
        pauseFocusTimer()
        val totalSec = _focusTimerState.value.initialMinutes * 60
        _focusTimerState.value = _focusTimerState.value.copy(
            secondsRemaining = totalSec,
            isCompleted = false
        )
    }

    private fun logFocusCompletion() {
        viewModelScope.launch {
            val state = _focusTimerState.value
            val session = FocusSession(
                taskId = state.selectedTask?.id,
                taskTitle = state.selectedTask?.title ?: "General Focus",
                durationMinutes = state.initialMinutes
            )
            repository.logFocusSession(session)
        }
    }

    fun toggleMotionPreference() {
        _isHighMotion.value = !_isHighMotion.value
    }
}
