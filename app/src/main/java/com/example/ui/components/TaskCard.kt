package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subtask
import com.example.data.model.Task
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.data.model.UrgencyState
import com.example.data.model.getUrgencyState
import com.example.data.repository.TaskRepository
import com.example.ui.theme.DarkSuccess
import com.example.ui.theme.UrgencyApproaching
import com.example.ui.theme.UrgencyNormal
import com.example.ui.theme.UrgencyOverdue
import com.example.ui.theme.UrgencyUrgent

@Composable
fun TaskCard(
    task: Task,
    currentTime: Long,
    onToggleComplete: () -> Unit,
    onPostponeMinutes: (Int) -> Unit,
    onDelete: () -> Unit,
    onToggleSubtask: (String) -> Unit,
    modifier: Modifier = Modifier,
    isHighMotion: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && isHighMotion) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "TaskCardPressScale"
    )

    val urgencyState = task.getUrgencyState(currentTime)
    val urgencyBorderColor = when {
        task.isCompleted -> DarkSuccess
        urgencyState == UrgencyState.OVERDUE -> UrgencyOverdue
        urgencyState == UrgencyState.URGENT -> UrgencyUrgent
        urgencyState == UrgencyState.APPROACHING -> UrgencyApproaching
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    val categoryIcon = when (task.category) {
        TaskCategory.WORK -> Icons.Default.Work
        TaskCategory.PERSONAL -> Icons.Default.Person
        TaskCategory.STUDY -> Icons.Default.School
        TaskCategory.HEALTH -> Icons.Default.Favorite
        TaskCategory.CODING -> Icons.Default.Code
    }

    val priorityColor = when (task.priority) {
        TaskPriority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
        TaskPriority.MEDIUM -> MaterialTheme.colorScheme.primary
        TaskPriority.HIGH -> UrgencyApproaching
        TaskPriority.URGENT -> UrgencyOverdue
    }

    val subtasks = remember(task.subtasksJson) {
        TaskRepository.deserializeSubtasks(task.subtasksJson)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(
                width = if (urgencyState == UrgencyState.URGENT || urgencyState == UrgencyState.OVERDUE) 1.5.dp else 1.dp,
                color = urgencyBorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated Checkmark Button
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("task_complete_btn_${task.id}")
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = "Toggle completion",
                        tint = if (task.isCompleted) DarkSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(interactionSource = interactionSource, indication = null) {
                            isExpanded = !isExpanded
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Category pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = categoryIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = task.category.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Priority pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = priorityColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = task.priority.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = priorityColor
                                ),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.testTag("task_expand_btn_${task.id}")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Live Time Timeline Indicator Component
            Spacer(modifier = Modifier.height(10.dp))
            TimeProgressTimeline(
                task = task,
                currentTime = currentTime,
                isHighMotion = isHighMotion
            )

            // Expanded Accordion Area
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Subtasks list if available
                    if (subtasks.isNotEmpty()) {
                        Text(
                            text = "SUBTASKS (${subtasks.count { it.isCompleted }}/${subtasks.size})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        subtasks.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleSubtask(sub.id) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (sub.isCompleted) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = if (sub.isCompleted) DarkSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sub.title,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (sub.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Action bar: Postpone chips & Delete button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SuggestionChip(
                                onClick = { onPostponeMinutes(15) },
                                label = { Text("+15m", fontSize = 11.sp) },
                                icon = {
                                    Icon(
                                        Icons.Default.MoreTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                modifier = Modifier.testTag("postpone_15m_${task.id}")
                            )

                            SuggestionChip(
                                onClick = { onPostponeMinutes(60) },
                                label = { Text("+1h", fontSize = 11.sp) },
                                icon = {
                                    Icon(
                                        Icons.Default.MoreTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                modifier = Modifier.testTag("postpone_1h_${task.id}")
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag("delete_task_btn_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete task",
                                tint = UrgencyOverdue
                            )
                        }
                    }
                }
            }
        }
    }
}
