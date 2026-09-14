package com.example.flowpeak.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

// ── Rutele aplicației ──────────────────────────────────────────
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Auth     : Screen("auth",     "Autentificare", Icons.Outlined.Lock)
    object Focus    : Screen("focus",    "Muncă",         Icons.Outlined.Timer)
    object Journal  : Screen("journal",  "Jurnal",        Icons.Outlined.Book)
    object Insights : Screen("insights", "Insights",      Icons.Outlined.BarChart)
    object Profile  : Screen("profile",  "Profil",        Icons.Outlined.Person)
    object Settings         : Screen("settings", "Setări",        Icons.Outlined.Settings)
    object MainProductivity : Screen("main_prod", "Productivitate", Icons.Outlined.Dashboard)
}

val bottomNavItems = listOf(
    Screen.Focus,
    Screen.Journal,
    Screen.Insights,
    Screen.Profile,
    Screen.Settings
)

