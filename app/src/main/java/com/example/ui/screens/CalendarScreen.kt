package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.ui.components.TaskCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    tasks: List<Task>,
    currentTime: Long,
    isHighMotion: Boolean,
    onToggleComplete: (Task) -> Unit,
    onPostponeTask: (Task, Int) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onToggleSubtask: (Task, String) -> Unit
) {
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val dayNumFormat = SimpleDateFormat("d", Locale.getDefault())

    val daysInMonth = remember(selectedCalendar.get(Calendar.MONTH), selectedCalendar.get(Calendar.YEAR)) {
        val cal = selectedCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val list = mutableListOf<Date>()
        for (i in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, i)
            list.add(cal.time)
        }
        list
    }

    var selectedDate by remember { mutableStateOf(selectedCalendar.time) }

    val tasksForSelectedDate = remember(tasks, selectedDate) {
        val selCal = Calendar.getInstance().apply { time = selectedDate }
        tasks.filter { task ->
            val taskCal = Calendar.getInstance().apply { timeInMillis = task.dueDate }
            selCal.get(Calendar.YEAR) == taskCal.get(Calendar.YEAR) &&
                    selCal.get(Calendar.DAY_OF_YEAR) == taskCal.get(Calendar.DAY_OF_YEAR)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("calendar_screen")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "TIMELINE CALENDAR",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Month Selector Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newCal = selectedCalendar.clone() as Calendar
                            newCal.add(Calendar.MONTH, -1)
                            selectedCalendar = newCal
                        }
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                    }

                    Text(
                        text = monthYearFormat.format(selectedCalendar.time),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = {
                            val newCal = selectedCalendar.clone() as Calendar
                            newCal.add(Calendar.MONTH, 1)
                            selectedCalendar = newCal
                        }
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Days Grid Horizontal Scroll / Wrap
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    daysInMonth.take(7).forEach { date ->
                        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault()).format(date)
                        Text(
                            text = dayFormat.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Render week rows
                val chunks = daysInMonth.chunked(7)
                chunks.forEach { week ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        week.forEach { date ->
                            val isSelected = isSameDay(date, selectedDate)
                            val isToday = isSameDay(date, Date(currentTime))
                            val dayText = dayNumFormat.format(date)

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    )
                                    .clickable { selectedDate = date }
                            ) {
                                Text(
                                    text = dayText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val dateLabel = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(selectedDate)
        Text(
            text = "SCHEDULE FOR $dateLabel (${tasksForSelectedDate.size})",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (tasksForSelectedDate.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No work items scheduled for this date.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(tasksForSelectedDate, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        currentTime = currentTime,
                        onToggleComplete = { onToggleComplete(task) },
                        onPostponeMinutes = { mins -> onPostponeTask(task, mins) },
                        onDelete = { onDeleteTask(task) },
                        onToggleSubtask = { subId -> onToggleSubtask(task, subId) },
                        isHighMotion = isHighMotion
                    )
                }
            }
        }
    }
}

private fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
