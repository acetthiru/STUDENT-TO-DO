package com.example.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NavScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    HOME("home", "HOME", Icons.Default.Home),
    EXAMS("exams", "EXAMS", Icons.Default.School),
    TASKS("tasks", "TASKS", Icons.Default.Checklist),
    FOCUS("focus", "FOCUS", Icons.Default.Timer),
    CALENDAR("calendar", "CALENDAR", Icons.Default.CalendarMonth),
    INSIGHTS("insights", "INSIGHTS", Icons.Default.BarChart),
    SETTINGS("settings", "SETTINGS", Icons.Default.Settings)
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (NavScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavScreen.values().forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 8.sp
                        )
                    )
                },
                alwaysShowLabel = false,
                modifier = Modifier.testTag("nav_tab_${screen.route}")
            )
        }
    }
}
