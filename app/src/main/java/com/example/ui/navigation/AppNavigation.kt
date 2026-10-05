package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.ui.components.AddWorkBottomSheet
import com.example.ui.components.BackgroundParticlesCanvas
import com.example.ui.components.CelebrationDialog
import com.example.ui.screens.*
import com.example.ui.viewmodel.TaskViewModel

@Composable
fun AppNavigation(
    viewModel: TaskViewModel
) {
    val navController = rememberNavController()

    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val pendingTasks by viewModel.pendingTasks.collectAsStateWithLifecycle()
    val currentTime by viewModel.currentTime.collectAsStateWithLifecycle()
    val completionPercentage by viewModel.completionPercentage.collectAsStateWithLifecycle()
    val isHighMotion by viewModel.isHighMotion.collectAsStateWithLifecycle()
    val focusTimerState by viewModel.focusTimerState.collectAsStateWithLifecycle()
    val totalFocusMinutes by viewModel.totalFocusMinutes.collectAsStateWithLifecycle()
    val focusSessions by viewModel.allFocusSessions.collectAsStateWithLifecycle()
    val taskLogs by viewModel.taskLogs.collectAsStateWithLifecycle()
    val showCelebration by viewModel.showCelebration.collectAsStateWithLifecycle()

    val allExams by viewModel.allExams.collectAsStateWithLifecycle()
    val scannedMaterials by viewModel.scannedMaterials.collectAsStateWithLifecycle()
    val driveSyncStatus by viewModel.driveSyncStatus.collectAsStateWithLifecycle()

    var showAddWorkSheet by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavScreen.HOME.route

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { screen ->
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(NavScreen.HOME.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Ambient particle background
            BackgroundParticlesCanvas(isHighMotion = isHighMotion)

            NavHost(
                navController = navController,
                startDestination = NavScreen.HOME.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
            ) {
                composable(NavScreen.HOME.route) {
                    HomeScreen(
                        tasks = tasks,
                        currentTime = currentTime,
                        completionPercentage = completionPercentage,
                        isHighMotion = isHighMotion,
                        onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                        onPostponeTask = { task, mins -> viewModel.postponeTask(task, mins) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onToggleSubtask = { task, subId -> viewModel.toggleSubtask(task, subId) },
                        onStartFocusMode = { task ->
                            viewModel.setFocusTask(task)
                            navController.navigate(NavScreen.FOCUS.route)
                        },
                        onAddWorkClick = { showAddWorkSheet = true }
                    )
                }

                composable(NavScreen.EXAMS.route) {
                    ExamScreen(
                        exams = allExams,
                        scannedMaterials = scannedMaterials,
                        currentTime = currentTime,
                        driveSyncStatus = driveSyncStatus,
                        onBackupToDrive = { viewModel.backupToDrive() },
                        onRestoreFromDrive = { viewModel.restoreFromDrive() },
                        onAddExam = { name, dateMillis, score, topics ->
                            viewModel.createExamSubject(name, dateMillis, score, topics)
                        },
                        onGenerateSchedule = { subjectName, topics, days, onResult ->
                            viewModel.generateAiExamSchedule(subjectName, topics, days, onResult)
                        },
                        onSaveExamWithTasks = { subjectName, dateMillis, score, totalTopics, scheduleSubtasks ->
                            viewModel.createExamSubject(subjectName, dateMillis, score, totalTopics)
                            viewModel.createWorkTask(
                                title = "Exam Prep: $subjectName",
                                description = "AI Auto-Calculated Study Schedule",
                                category = TaskCategory.STUDY,
                                priority = TaskPriority.HIGH,
                                estimatedMinutes = 60,
                                dueDateMillis = dateMillis,
                                subtasks = scheduleSubtasks
                            )
                        },
                        onSaveScan = { title, uri, text, subjectId ->
                            viewModel.addScannedWork(title, uri, text, subjectId)
                        },
                        getFlashcardsForSubject = { subjectId -> viewModel.getFlashcards(subjectId) }
                    )
                }

                composable(NavScreen.TASKS.route) {
                    TasksScreen(
                        tasks = tasks,
                        currentTime = currentTime,
                        isHighMotion = isHighMotion,
                        onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                        onPostponeTask = { task, mins -> viewModel.postponeTask(task, mins) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onToggleSubtask = { task, subId -> viewModel.toggleSubtask(task, subId) },
                        onAddWorkClick = { showAddWorkSheet = true }
                    )
                }

                composable(NavScreen.FOCUS.route) {
                    FocusScreen(
                        timerState = focusTimerState,
                        pendingTasks = pendingTasks,
                        onSelectTask = { viewModel.setFocusTask(it) },
                        onSetDuration = { viewModel.setFocusDuration(it) },
                        onStart = { viewModel.startFocusTimer() },
                        onPause = { viewModel.pauseFocusTimer() },
                        onReset = { viewModel.resetFocusTimer() }
                    )
                }

                composable(NavScreen.CALENDAR.route) {
                    CalendarScreen(
                        tasks = tasks,
                        currentTime = currentTime,
                        isHighMotion = isHighMotion,
                        onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                        onPostponeTask = { task, mins -> viewModel.postponeTask(task, mins) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onToggleSubtask = { task, subId -> viewModel.toggleSubtask(task, subId) }
                    )
                }

                composable(NavScreen.INSIGHTS.route) {
                    InsightsScreen(
                        tasks = tasks,
                        totalFocusMinutes = totalFocusMinutes,
                        focusSessions = focusSessions,
                        taskLogs = taskLogs,
                        isHighMotion = isHighMotion
                    )
                }

                composable(NavScreen.SETTINGS.route) {
                    SettingsScreen(
                        isHighMotion = isHighMotion,
                        onToggleMotion = { viewModel.toggleMotionPreference() }
                    )
                }
            }
        }
    }

    if (showAddWorkSheet) {
        AddWorkBottomSheet(
            onDismiss = { showAddWorkSheet = false },
            onSaveTask = { title, desc, category, priority, estMins, dueMillis, subtasks ->
                viewModel.createWorkTask(title, desc, category, priority, estMins, dueMillis, subtasks)
            },
            onAiBreakdownRequest = { title, desc, onResult ->
                viewModel.generateSubtasksWithAi(title, desc, onResult)
            }
        )
    }

    if (showCelebration) {
        CelebrationDialog(
            totalCompleted = tasks.count { it.isCompleted },
            onDismiss = { viewModel.dismissCelebration() },
            onViewSummary = {
                viewModel.dismissCelebration()
                navController.navigate(NavScreen.INSIGHTS.route)
            }
        )
    }
}
