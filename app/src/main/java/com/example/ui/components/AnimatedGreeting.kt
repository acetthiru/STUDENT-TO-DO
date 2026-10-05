package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedGreeting(
    currentTimeMillis: Long,
    modifier: Modifier = Modifier
) {
    val calendar = remember(currentTimeMillis) {
        Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)

    val greetingText = when (hour) {
        in 5..11 -> "Good Morning,"
        in 12..16 -> "Good Afternoon,"
        in 17..21 -> "Good Evening,"
        else -> "Night Focus,"
    }

    val subtitleSlogan = "TIME → ACTION → PROGRESS → COMPLETION"

    val timeStr = remember(currentTimeMillis) {
        SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date(currentTimeMillis))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("animated_greeting")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greetingText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Master of Time",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            AnimatedContent(
                targetState = timeStr,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ClockTextAnimation"
            ) { targetTime ->
                Text(
                    text = targetTime,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("live_clock_display")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitleSlogan,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
