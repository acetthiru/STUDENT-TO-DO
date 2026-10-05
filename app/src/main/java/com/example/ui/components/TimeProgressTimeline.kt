package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.data.model.UrgencyState
import com.example.data.model.getUrgencyState
import com.example.ui.theme.UrgencyApproaching
import com.example.ui.theme.UrgencyNormal
import com.example.ui.theme.UrgencyOverdue
import com.example.ui.theme.UrgencyUrgent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimeProgressTimeline(
    task: Task,
    currentTime: Long,
    modifier: Modifier = Modifier,
    isHighMotion: Boolean = true
) {
    val urgencyState = task.getUrgencyState(currentTime)
    val urgencyColor by animateColorAsState(
        targetValue = when (urgencyState) {
            UrgencyState.NORMAL -> UrgencyNormal
            UrgencyState.APPROACHING -> UrgencyApproaching
            UrgencyState.URGENT -> UrgencyUrgent
            UrgencyState.OVERDUE -> UrgencyOverdue
        },
        animationSpec = tween(500),
        label = "UrgencyColor"
    )

    // Pulsing alpha for URGENT & OVERDUE
    val infiniteTransition = rememberInfiniteTransition(label = "PulsingNowDot")
    val pulseAlpha by if (urgencyState == UrgencyState.URGENT || urgencyState == UrgencyState.OVERDUE) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "PulseAlpha"
        )
    } else {
        rememberUpdatedState(1f)
    }

    val totalDuration = (task.dueDate - task.startTime).coerceAtLeast(1)
    val elapsed = (currentTime - task.startTime).coerceIn(0, totalDuration)
    val rawRatio = elapsed.toFloat() / totalDuration.toFloat()

    val animatedRatio by animateFloatAsState(
        targetValue = rawRatio,
        animationSpec = if (isHighMotion) tween(600, easing = FastOutSlowInEasing) else tween(0),
        label = "TimelineRatio"
    )

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val startStr = timeFormat.format(Date(task.startTime))
    val dueStr = timeFormat.format(Date(task.dueDate))

    val remainingDiff = task.dueDate - currentTime
    val remainingText = when {
        task.isCompleted -> "Completed"
        remainingDiff < 0 -> {
            val overdueMins = (-remainingDiff / 60_000).toInt()
            "Overdue by ${overdueMins}m"
        }
        else -> {
            val mins = (remainingDiff / 60_000).toInt()
            val hrs = mins / 60
            if (hrs > 0) "${hrs}h ${mins % 60}m left" else "${mins}m left"
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
            .testTag("time_progress_timeline")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (urgencyState == UrgencyState.OVERDUE) Icons.Default.Warning else Icons.Default.Schedule,
                    contentDescription = "Urgency status",
                    tint = urgencyColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (urgencyState) {
                        UrgencyState.NORMAL -> "ON TIME"
                        UrgencyState.APPROACHING -> "APPROACHING"
                        UrgencyState.URGENT -> "URGENT DEADLINE"
                        UrgencyState.OVERDUE -> "OVERDUE WORK"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = urgencyColor
                )
            }

            Text(
                text = remainingText,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (urgencyState == UrgencyState.OVERDUE) UrgencyOverdue else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visual Timeline Slider Canvas
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = size.height / 2f

                // Track Line
                drawLine(
                    color = trackColor,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(widthPx, y),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Elapsed Line
                val activeX = widthPx * animatedRatio
                drawLine(
                    color = urgencyColor,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(activeX, y),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // NOW Indicator Dot
            val nowX = (constraints.maxWidth.dp * animatedRatio)
            Box(
                modifier = Modifier
                    .offset(x = (nowX - 8.dp).coerceIn(0.dp, constraints.maxWidth.dp - 16.dp))
                    .align(Alignment.CenterStart)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(urgencyColor.copy(alpha = pulseAlpha))
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "START: $startStr",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )

            Text(
                text = "NOW",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = urgencyColor,
                fontSize = 10.sp
            )

            Text(
                text = "DUE: $dueStr",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}
