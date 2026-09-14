package com.example.flowpeak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.example.flowpeak.navigation.NavGraph
import com.example.flowpeak.state.AppState
import com.example.flowpeak.ui.theme.FlowPeakTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appState = remember { AppState(applicationContext) }

            LaunchedEffect(appState.isThemeAutomatic, appState.isDarkMode) {
                appState.updateThemeAutomatically()
            }

            FlowPeakTheme(isDarkMode = appState.isDarkMode) {
                NavGraph(appState)
            }
        }
    }
}

