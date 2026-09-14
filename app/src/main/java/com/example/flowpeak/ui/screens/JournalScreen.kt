package com.example.flowpeak.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.flowpeak.state.AppState
import com.example.flowpeak.ui.components.*
import com.example.flowpeak.ui.theme.FlowPeakTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(appState: AppState) {
    var journalText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FlowPeakTheme.colors.background)
            .verticalScroll(scrollState)
    ) {
        ScreenHeader(
            tag = "Reflecție zilnică",
            title = "Jurnal & Trackere",
            subtitle = "Notează-ți gândurile și monitorizează-ți stilul de viață."
        )

        // ── Secțiunea Jurnal (Caseta de text) ──────────────────────
        Text(
            text = "INTRARE NOUĂ JURNAL",
            style = MaterialTheme.typography.labelSmall,
            color = FlowPeakTheme.colors.textMuted,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        FCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            OutlinedTextField(
                value = journalText,
                onValueChange = { journalText = it },
                placeholder = { Text("Cum a fost ziua ta? Ce realizări sau provocări ai întâmpinat?", color = FlowPeakTheme.colors.textMuted) },
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = FlowPeakTheme.colors.surface2,
                    unfocusedContainerColor = FlowPeakTheme.colors.surface2,
                    focusedTextColor = FlowPeakTheme.colors.textPrimary,
                    unfocusedTextColor = FlowPeakTheme.colors.textPrimary,
                    focusedIndicatorColor = FlowPeakTheme.colors.primary,
                    unfocusedIndicatorColor = FlowPeakTheme.colors.border
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (journalText.isNotBlank()) {
                        appState.addJournalEntry(journalText)
                        journalText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FlowPeakTheme.colors.primary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = if (FlowPeakTheme.colors.isDark) FlowPeakTheme.colors.background else FlowPeakTheme.colors.surface1
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Salvează",
                    color = if (FlowPeakTheme.colors.isDark) FlowPeakTheme.colors.background else FlowPeakTheme.colors.surface1,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Secțiunea Trackere Rapide ────────────────────────────
        Text(
            text = "MONITORIZARE ZILNICĂ",
            style = MaterialTheme.typography.labelSmall,
            color = FlowPeakTheme.colors.textMuted,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        FCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            // SOMN (Slider)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ore de somn",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = FlowPeakTheme.colors.textPrimary
                )
                Text(
                    text = "${appState.sleepHours.toInt()} ore",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = FlowPeakTheme.colors.primary
                )
            }
            Slider(
                value = appState.sleepHours,
                onValueChange = { appState.sleepHours = it },
                valueRange = 4f..12f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = FlowPeakTheme.colors.primary,
                    activeTrackColor = FlowPeakTheme.colors.primary,
                    inactiveTrackColor = FlowPeakTheme.colors.surface3
                )
            )

            Spacer(modifier = Modifier.height(14.dp))
            FDivider()
            Spacer(modifier = Modifier.height(14.dp))

            // ALIMENTAȚIE (Segmented Control)
            Text(
                text = "Calitatea alimentației",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = FlowPeakTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Healthy", "Normal", "Junk Food").forEach { food ->
                    val isSelected = appState.foodType == food
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) FlowPeakTheme.colors.primary.copy(alpha = 0.15f) else FlowPeakTheme.colors.surface2)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) FlowPeakTheme.colors.primary else FlowPeakTheme.colors.border,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { appState.foodType = food }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = food,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) FlowPeakTheme.colors.primary else FlowPeakTheme.colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            FDivider()
            Spacer(modifier = Modifier.height(14.dp))

            // HIDRATARE (Plus / Minus)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hidratare",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = FlowPeakTheme.colors.textPrimary
                    )
                    Text(
                        text = "Pahare de apă recomandate (8)",
                        style = MaterialTheme.typography.labelSmall,
                        color = FlowPeakTheme.colors.textMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FlowPeakTheme.colors.surface2)
                            .clickable { if (appState.hydrationCups > 0) appState.hydrationCups-- }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Remove,
                            contentDescription = "Minus",
                            tint = FlowPeakTheme.colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "${appState.hydrationCups}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FlowPeakTheme.colors.textPrimary
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FlowPeakTheme.colors.surface2)
                            .clickable { appState.hydrationCups++ }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Plus",
                            tint = FlowPeakTheme.colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // RECOMPENSĂ TRACKERE
            if (!appState.trackersCompletedToday) {
                Button(
                    onClick = { appState.completeTrackers() },
                    colors = ButtonDefaults.buttonColors(containerColor = FlowPeakTheme.colors.primary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(
                        text = "Finalizează și revendică +150 XP",
                        color = if (FlowPeakTheme.colors.isDark) FlowPeakTheme.colors.background else FlowPeakTheme.colors.surface1,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(FlowPeakTheme.colors.primary.copy(alpha = 0.08f))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Completat",
                        tint = FlowPeakTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recompensa de XP a fost colectată!",
                        color = FlowPeakTheme.colors.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Istoric Jurnal ───────────────────────────────────────
        Text(
            text = "ISTORIC INTRĂRI",
            style = MaterialTheme.typography.labelSmall,
            color = FlowPeakTheme.colors.textMuted,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        appState.journalEntries.forEach { entry ->
            FCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = entry.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = FlowPeakTheme.colors.textMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = entry.text,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = FlowPeakTheme.colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
