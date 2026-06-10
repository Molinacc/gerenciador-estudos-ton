package br.com.ton.estudos.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.ton.estudos.presentation.dashboard.DashboardScreen
import br.com.ton.estudos.presentation.flashcards.FlashcardStudyScreen
import br.com.ton.estudos.presentation.flashcards.FlashcardsScreen
import br.com.ton.estudos.presentation.profile.ProfileScreen
import br.com.ton.estudos.presentation.schedule.ScheduleScreen
import br.com.ton.estudos.presentation.statistics.StatisticsScreen
import br.com.ton.estudos.presentation.timer.TimerScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.route == item.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn() + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut() + slideOutHorizontally { -it / 4 } },
            popEnterTransition = { fadeIn() + slideInHorizontally { -it / 4 } },
            popExitTransition = { fadeOut() + slideOutHorizontally { it / 4 } }
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(navController = navController) }
            composable(Screen.Schedule.route) { ScheduleScreen(navController = navController) }
            composable(Screen.Timer.route) { TimerScreen(navController = navController) }
            composable(Screen.Flashcards.route) { FlashcardsScreen(navController = navController) }
            composable(Screen.FlashcardStudy.route) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getString("deckId")?.toLongOrNull() ?: 0L
                FlashcardStudyScreen(deckId = deckId, navController = navController)
            }
            composable(Screen.Statistics.route) { StatisticsScreen(navController = navController) }
            composable(Screen.Profile.route) { ProfileScreen(navController = navController) }
        }
    }
}
