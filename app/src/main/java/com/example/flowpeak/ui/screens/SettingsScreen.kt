package com.example.flowpeak.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.flowpeak.state.AppState
import com.example.flowpeak.ui.components.FCard
import com.example.flowpeak.ui.components.ScreenHeader
import com.example.flowpeak.ui.theme.FlowPeakTheme

@Composable
fun SettingsScreen(
    appState: AppState? = null
) {
    val context = LocalContext.current
    var isMoodAdaptationEnabled by remember { mutableStateOf(false) }

    // Launcher pentru cererea permisiunii de acces la cameră (android.permission.CAMERA)
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permisiunea a fost acceptată -> Switch devine ON
            isMoodAdaptationEnabled = true
            Toast.makeText(context, "Permisiune cameră acordată!", Toast.LENGTH_SHORT).show()
        } else {
            // Permisiunea a fost refuzată (fallback) -> Switch revine pe OFF, aplicația nu dă crash
            isMoodAdaptationEnabled = false
            Toast.makeText(
                context,
                "Permisiune refuzată. Adaptarea la starea de spirit necesită acces la cameră.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FlowPeakTheme.colors.background)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            tag = "Configurare",
            title = "Setări",
            subtitle = "Ajustează preferințele și funcționalitățile inteligente"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PERMISIUNI ȘI SENZORI",
            style = MaterialTheme.typography.labelSmall,
            color = FlowPeakTheme.colors.textMuted,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        FCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                ) {
                    Text(
                        text = "Adaptare inteligentă la starea de spirit",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = FlowPeakTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Analizează expresia facială prin cameră pentru a adapta tema și muzica la starea ta.",
                        style = MaterialTheme.typography.labelSmall,
                        color = FlowPeakTheme.colors.textMuted
                    )
                }

                Switch(
                    checked = isMoodAdaptationEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            // Verificăm dacă avem deja permisiunea de cameră acordată
                            val permissionStatus = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            )

                            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                                isMoodAdaptationEnabled = true
                            } else {
                                // Lansăm cererea de permisiune prin rememberLauncherForActivityResult
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        } else {
                            // Dacă utilizatorul oprește manual Switch-ul
                            isMoodAdaptationEnabled = false
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = FlowPeakTheme.colors.primary,
                        checkedTrackColor = FlowPeakTheme.colors.primary.copy(alpha = 0.5f),
                        uncheckedThumbColor = FlowPeakTheme.colors.textMuted,
                        uncheckedTrackColor = FlowPeakTheme.colors.surface3
                    )
                )
            }
        }
    }
}
