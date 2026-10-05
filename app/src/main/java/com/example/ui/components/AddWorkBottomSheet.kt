package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkBottomSheet(
    onDismiss: () -> Unit,
    onSaveTask: (
        title: String,
        description: String,
        category: TaskCategory,
        priority: TaskPriority,
        estimatedMinutes: Int,
        dueDateMillis: Long,
        subtasks: List<Subtask>
    ) -> Unit,
    onAiBreakdownRequest: (String, String, (List<Subtask>) -> Unit) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.WORK) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var estimatedMinutes by remember { mutableStateOf(30) }
    var dueTimeMinutesFromNow by remember { mutableStateOf(60) } // Default 1 hr from now
    var subtasks by remember { mutableStateOf<List<Subtask>>(emptyList()) }
    var showAiDialog by remember { mutableStateOf(false) }
    var isAiLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(scrollState)
                .testTag("add_work_bottom_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+ ADD WORK",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close Sheet")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Task Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Work / Task Title") },
                placeholder = { Text("e.g. Prepare Presentation Slides") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description & Notes (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .testTag("task_desc_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // AI Breakdown Assistant Button
            OutlinedButton(
                onClick = {
                    if (title.isNotBlank()) {
                        isAiLoading = true
                        showAiDialog = true
                        onAiBreakdownRequest(title, description) { result ->
                            subtasks = result
                            isAiLoading = false
                        }
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_breakdown_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "✨ AI Work Breakdown",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${subtasks.size} AI Subtasks Attached",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Chips
            Text(
                text = "CATEGORY",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TaskCategory.values()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Priority Chips
            Text(
                text = "PRIORITY & URGENCY",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskPriority.values().forEach { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = { selectedPriority = priority },
                        label = { Text(priority.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Due Date / Time Preset Selector
            Text(
                text = "DEADLINE / DUE TIME",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    15 to "+15m",
                    30 to "+30m",
                    60 to "+1h",
                    120 to "+2h",
                    240 to "+4h"
                ).forEach { (mins, label) ->
                    FilterChip(
                        selected = dueTimeMinutesFromNow == mins,
                        onClick = { dueTimeMinutesFromNow = mins },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val now = System.currentTimeMillis()
                        val dueMillis = now + (dueTimeMinutesFromNow * 60_000L)
                        onSaveTask(
                            title,
                            description,
                            selectedCategory,
                            selectedPriority,
                            estimatedMinutes,
                            dueMillis,
                            subtasks
                        )
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_work_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "SAVE & START TIMELINE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }

    if (showAiDialog) {
        AiBreakdownDialog(
            isLoading = isAiLoading,
            generatedSubtasks = subtasks,
            onAccept = { result ->
                subtasks = result
                showAiDialog = false
            },
            onDismiss = { showAiDialog = false }
        )
    }
}
