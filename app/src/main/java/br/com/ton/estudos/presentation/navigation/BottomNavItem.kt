package br.com.ton.estudos.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Dashboard.route,
        title = "Início",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = Screen.Schedule.route,
        title = "Cronograma",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    ),
    BottomNavItem(
        route = Screen.Timer.route,
        title = "Cronômetro",
        selectedIcon = Icons.Filled.Timer,
        unselectedIcon = Icons.Outlined.Timer
    ),
    BottomNavItem(
        route = Screen.Flashcards.route,
        title = "Flashcards",
        selectedIcon = Icons.Filled.Style,
        unselectedIcon = Icons.Outlined.Style
    ),
    BottomNavItem(
        route = Screen.Statistics.route,
        title = "Estatísticas",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    )
)
