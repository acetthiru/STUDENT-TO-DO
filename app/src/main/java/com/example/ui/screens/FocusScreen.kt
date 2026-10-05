package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.ui.viewmodel.FocusTimerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    timerState: FocusTimerUiState,
    pendingTasks: List<Task>,
    onSelectTask: (Task?) -> Unit,
    onSetDuration: (Int) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit
) {
    val totalSec = timerState.totalSeconds
    val remainingSec = timerState.secondsRemaining
    val progress = if (totalSec > 0) (remainingSec.toFloat() / totalSec.toFloat()) else 1f

    val mins = remainingSec / 60
    val secs = remainingSec % 60
    val timeFormatted = String.format("%02d:%02d", mins, secs)

    // Breathing aura animation
    val infiniteTransition = rememberInfiniteTransition(label = "FocusBreathingAura")
    val auraScale by if (timerState.isRunning) {
        infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "AuraScale"
        )
    } else {
        rememberUpdatedState(1f)
    }

    var showTaskMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("focus_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "FOCUS MODE",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Task Selector Dropdown Chip
            OutlinedCard(
                onClick = { showTaskMenu = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("focus_task_selector")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timerState.selectedTask?.title ?: "Select Focus Target",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            DropdownMenu(
                expanded = showTaskMenu,
                onDismissRequest = { showTaskMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("General Focus Sprint") },
                    onClick = {
                        onSelectTask(null)
                        showTaskMenu = false
                    }
                )
                pendingTasks.forEach { task ->
                    DropdownMenuItem(
                        text = { Text(task.title) },
                        onClick = {
                            onSelectTask(task)
                            showTaskMenu = false
                        }
                    )
                }
            }
        }

        // Circular Countdown Ring with Pulsing Aura
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(260.dp)
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val strokePx = 16.dp.toPx()

                // Track
                drawCircle(
                    color = trackColor,
                    style = Stroke(width = strokePx)
                )

                // Progress Arc
                val sweep = 360f * progress
                if (sweep > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(primaryColor, secondaryColor, primaryColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 48.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (timerState.isRunning) "FOCUS" else "READY",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = if (timerState.isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Timer Preset Selector Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            listOf(15, 25, 45, 60).forEach { minsPreset ->
                FilterChip(
                    selected = timerState.initialMinutes == minsPreset,
                    onClick = { onSetDuration(minsPreset) },
                    label = { Text("${minsPreset}m") },
                    modifier = Modifier.testTag("focus_duration_${minsPreset}m")
                )
            }
        }

        // Control Buttons (Start / Pause / Reset)
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("focus_reset_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FloatingActionButton(
                onClick = { if (timerState.isRunning) onPause() else onStart() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(72.dp)
                    .testTag("focus_start_pause_btn")
            ) {
                Icon(
                    imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (timerState.isRunning) "Pause" else "Start",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
