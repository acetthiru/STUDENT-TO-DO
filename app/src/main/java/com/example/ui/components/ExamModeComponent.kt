package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subtask
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority

@Composable
fun ExamModeComponent(
    onGenerateSchedule: (subjectName: String, topicsList: List<String>, daysRemaining: Int, onResult: (List<Subtask>) -> Unit) -> Unit,
    onSaveExamWithTasks: (subjectName: String, examDateMillis: Long, targetScore: String, totalTopics: Int, scheduleTasks: List<Subtask>) -> Unit,
    modifier: Modifier = Modifier
) {
    var subjectName by remember { mutableStateOf("") }
    var targetScore by remember { mutableStateOf("90%") }
    var daysUntilExam by remember { mutableStateOf(7) }
    var rawTopicsInput by remember { mutableStateOf("") }

    var isCalculating by remember { mutableStateOf(false) }
    var generatedSchedule by remember { mutableStateOf<List<Subtask>>(emptyList()) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("exam_mode_component")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "⚡ AI EXAM MODE & AUTO-SCHEDULE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Input syllabus topics to generate an optimal daily study timeline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subject Name Input
            OutlinedTextField(
                value = subjectName,
                onValueChange = { subjectName = it },
                label = { Text("Exam / Subject Title") },
                placeholder = { Text("e.g. Advanced Physics Final") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exam_mode_subject_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Syllabus Topics Input
            OutlinedTextField(
                value = rawTopicsInput,
                onValueChange = { rawTopicsInput = it },
                label = { Text("Syllabus Chapters / Topics (Separated by commas or newlines)") },
                placeholder = { Text("e.g. Quantum Mechanics, Thermodynamics, Optics, Wave Motion") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .testTag("exam_mode_topics_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Target Score & Days Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DAYS UNTIL EXAM",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(3, 7, 14, 30).forEach { days ->
                            FilterChip(
                                selected = daysUntilExam == days,
                                onClick = { daysUntilExam = days },
                                label = { Text("${days}d") },
                                modifier = Modifier.testTag("exam_days_chip_$days")
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TARGET SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("85%", "90%", "95%").forEach { score ->
                            FilterChip(
                                selected = targetScore == score,
                                onClick = { targetScore = score },
                                label = { Text(score) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Auto-Calculate Schedule Button
            Button(
                onClick = {
                    if (subjectName.isNotBlank()) {
                        isCalculating = true
                        val topicsList = rawTopicsInput.split(",", "\n").map { it.trim() }.filter { it.isNotBlank() }
                        onGenerateSchedule(subjectName, topicsList, daysUntilExam) { schedule ->
                            generatedSchedule = schedule
                            isCalculating = false
                        }
                    }
                },
                enabled = subjectName.isNotBlank() && !isCalculating,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("generate_ai_exam_schedule_btn")
            ) {
                if (isCalculating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Calculating Daily Schedule...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("✨ AUTO-CALCULATE STUDY SCHEDULE", fontWeight = FontWeight.Bold)
                }
            }

            // Generated Schedule Preview List
            AnimatedVisibility(visible = generatedSchedule.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "GENERATED DAILY MILESTONES (${generatedSchedule.size}):",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    generatedSchedule.forEach { milestone ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = milestone.title,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save Exam & Populate Timeline
                    Button(
                        onClick = {
                            val now = System.currentTimeMillis()
                            val examDateMillis = now + (daysUntilExam * 24 * 3600_000L)
                            val topicsCount = rawTopicsInput.split(",", "\n").count { it.isNotBlank() }.coerceAtLeast(1)
                            onSaveExamWithTasks(
                                subjectName,
                                examDateMillis,
                                targetScore,
                                topicsCount,
                                generatedSchedule
                            )
                            // Reset input
                            subjectName = ""
                            rawTopicsInput = ""
                            generatedSchedule = emptyList()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_exam_and_schedule_btn")
                    ) {
                        Text("SAVE EXAM & ADD TO STUDY TIMELINE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
