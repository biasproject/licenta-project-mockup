package com.example.flowpeak.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.flowpeak.state.AppState
import com.example.flowpeak.ui.screens.*
import com.example.flowpeak.ui.theme.FlowPeakTheme

@Composable
fun NavGraph(appState: AppState) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    // Verificăm starea de login și redirecționăm corespunzător
    LaunchedEffect(appState.isLoggedIn, currentRoute) {
        if (!appState.isLoggedIn && currentRoute != Screen.Auth.route) {
            navController.navigate(Screen.Auth.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        } else if (appState.isLoggedIn && (currentRoute == Screen.Auth.route || currentRoute == null)) {
            navController.navigate(Screen.Focus.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        containerColor = FlowPeakTheme.colors.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = FlowPeakTheme.colors.surface1,
                    tonalElevation  = 0.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick  = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = if (FlowPeakTheme.colors.isDark) FlowPeakTheme.colors.background else FlowPeakTheme.colors.surface1,
                                selectedTextColor   = FlowPeakTheme.colors.primary,
                                unselectedIconColor = FlowPeakTheme.colors.textMuted,
                                unselectedTextColor = FlowPeakTheme.colors.textMuted,
                                indicatorColor      = FlowPeakTheme.colors.primary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Auth.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Auth.route)     { AuthScreen(appState) }
            composable(Screen.Focus.route)    { FocusScreen(appState) }
            composable(Screen.Journal.route)  { JournalScreen(appState) }
            composable(Screen.Insights.route) { InsightsScreen(appState) }
            composable(Screen.Profile.route)  { ProfileScreen(appState) }
            composable(Screen.Settings.route)         { SettingsScreen(appState) }
            composable(Screen.MainProductivity.route) { MainProductivityScreen(smilingProbability = 0.5f, appState = appState) }
        }
    }
}
