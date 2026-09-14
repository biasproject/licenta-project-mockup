package com.example.flowpeak.ui.screens

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import com.example.flowpeak.state.AppState
import com.example.flowpeak.state.TaskItem
import com.example.flowpeak.ui.components.*
import com.example.flowpeak.ui.theme.FlowPeakTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(appState: AppState) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var isAttentive by remember { mutableStateOf(true) }
    
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_ALARM, 85)
        } catch (e: Exception) {
            null
        }
    }

    // ── Timer Loop ─────────────────────────────────────────────
    LaunchedEffect(appState.timerIsRunning) {
        if (appState.timerIsRunning) {
            while (appState.timerIsRunning && appState.timerSecondsLeft > 0) {
                delay(1000L)
                appState.timerSecondsLeft--
                
            }
            if (appState.timerSecondsLeft <= 0) {
                appState.timerIsRunning = false
                appState.timerSecondsLeft = appState.maxTimerSeconds
            }
        }
    }

    // Evităm alertele false de la un singur cadru: utilizatorul trebuie să nu mai
    // privească ecranul timp de 3 secunde înainte de deschiderea popup-ului.
    LaunchedEffect(appState.timerIsRunning, isAttentive, appState.showFocusCheckDialog) {
        if (appState.timerIsRunning && !isAttentive && !appState.showFocusCheckDialog) {
            delay(3_000L)
            if (!isAttentive && appState.timerIsRunning) {
                appState.showFocusCheckDialog = true
                appState.focusCheckSecondsLeft = 60
            }
        }
    }

    FocusAttentionMonitor(
        enabled = appState.timerIsRunning && !appState.showFocusCheckDialog,
        onAttentionChanged = { attentive -> isAttentive = attentive }
    )

    // ── Focus Check Dialog Countdown Loop ──────────────────────
    LaunchedEffect(appState.showFocusCheckDialog) {
        if (appState.showFocusCheckDialog) {
            while (appState.showFocusCheckDialog && appState.focusCheckSecondsLeft > 0) {
                delay(1000L)
                appState.focusCheckSecondsLeft--
            }
            if (appState.focusCheckSecondsLeft <= 0 && appState.showFocusCheckDialog) {
                appState.triggerFocusAlert()
                // Continuous alarm and vibration until dismissed
                coroutineScope.launch {
                    while (appState.showFocusCheckDialog) {
                        // Vibrate
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator?.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator?.vibrate(1000)
                        }
                        // Sound alert
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000)
                        delay(2000L)
                    }
                }
            }
        }
    }

    // Time calculations
    val minutes = appState.timerSecondsLeft / 60
    val seconds = appState.timerSecondsLeft % 60
    val timerText = "%02d:%02d".format(minutes, seconds)
    
    // Progress for circular indicator
    val progressFraction = appState.timerSecondsLeft.toFloat() / appState.maxTimerSeconds.toFloat()
    
    // Animated XP progress
    val xpAnimatedProgress by animateFloatAsState(
        targetValue = appState.xpInCurrentLevel / 1000f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "xpProgress"
    )

    // Tracks details
    val musicTracks = listOf(
        "Focus Rock Beat" to "Rock",
        "Lofi Rain Beats" to "Lofi",
        "Deep White Noise" to "White Noise"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FlowPeakTheme.colors.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── XP Progress Bar (Top) ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FlowPeakTheme.colors.surface1)
                .border(width = 0.5.dp, color = FlowPeakTheme.colors.border)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nivel ${appState.level} · Focus",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = FlowPeakTheme.colors.textPrimary
                    )
                    Text(
                        text = "${appState.xpInCurrentLevel}/1000 XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = FlowPeakTheme.colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(FlowPeakTheme.colors.surface3)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(xpAnimatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(FlowPeakTheme.colors.primary)
                    )
                }
            }
        }

        ScreenHeader(
            tag = "Muncă activă",
            title = "Focalizare",
            subtitle = "Menține atenția și bifează sarcinile propuse."
        )

        // ── Timer Circular ─────────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            val primaryColor = FlowPeakTheme.colors.primary
            val borderClr = FlowPeakTheme.colors.border
            
            // Draw progress circle using Canvas
            Canvas(modifier = Modifier.size(220.dp)) {
                // Background circle
                drawCircle(
                    color = borderClr,
                    radius = size.minDimension / 2f - 4.dp.toPx(),
                    style = Stroke(width = 4.dp.toPx())
                )
                // Active progress arc
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progressFraction,
                    useCenter = false,
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                    size = Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx()),
                    style = Stroke(width = 6.dp.toPx())
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = timerText,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Light,
                    color = FlowPeakTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (!appState.timerIsRunning) {
                        IconButton(
                            onClick = { appState.adjustTimer(-5) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.RemoveCircleOutline,
                                contentDescription = "Reduce 5m",
                                tint = FlowPeakTheme.colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(
                        onClick = { appState.timerIsRunning = !appState.timerIsRunning },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(FlowPeakTheme.colors.surface2)
                            .size(46.dp)
                    ) {
                        Icon(
                            imageVector = if (appState.timerIsRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                            contentDescription = "Start/Pause",
                            tint = FlowPeakTheme.colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    if (!appState.timerIsRunning) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { appState.adjustTimer(5) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddCircleOutline,
                                contentDescription = "Adaugă 5m",
                                tint = FlowPeakTheme.colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── Durată Cronometru Personalizată ─────────────────────────
        Text(
            text = "DURATĂ FOCUS (MINUTE)",
            style = MaterialTheme.typography.labelSmall,
            color = FlowPeakTheme.colors.textMuted,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        var durationInputText by remember(appState.maxTimerSeconds) {
            mutableStateOf((appState.maxTimerSeconds / 60).toString())
        }

        FCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Setează minute focalizare",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = FlowPeakTheme.colors.textPrimary
                    )
                    Text(
                        text = if (appState.timerIsRunning) "Oprește cronometrul pentru a edita" else "Introduceți numărul de minute",
                        style = MaterialTheme.typography.labelSmall,
                        color = FlowPeakTheme.colors.textMuted
                    )
                }
                
                OutlinedTextField(
                    value = durationInputText,
                    onValueChange = { input ->
                        if (!appState.timerIsRunning) {
                            val filtered = input.filter { it.isDigit() }
                            durationInputText = filtered
                            val mins = filtered.toIntOrNull()
                            if (mins != null && mins > 0) {
                                appState.updateMaxTimerSeconds(mins)
                            }
                        }
                    },
                    enabled = !appState.timerIsRunning,
                    placeholder = { Text("ex: 25") },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = FlowPeakTheme.colors.surface2,
                        unfocusedContainerColor = FlowPeakTheme.colors.surface2,
                        focusedTextColor = FlowPeakTheme.colors.textPrimary,
                        unfocusedTextColor = FlowPeakTheme.colors.textPrimary,
                        focusedIndicatorColor = FlowPeakTheme.colors.primary,
                        unfocusedIndicatorColor = FlowPeakTheme.colors.border
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.width(100.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ── Player Audio ──────────────────────────────────────────
        Text(
            text = "MUZICĂ FUNDAL",
            style = MaterialTheme.typography.labelSmall,
            color = FlowPeakTheme.colors.textMuted,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        FCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause button
                IconButton(
                    onClick = { appState.isMusicPlaying = !appState.isMusicPlaying },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(FlowPeakTheme.colors.primary)
                ) {
                    Icon(
                        imageVector = if (appState.isMusicPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = "Audio Play/Pause",
                        tint = if (FlowPeakTheme.colors.isDark) FlowPeakTheme.colors.background else FlowPeakTheme.colors.surface1
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = musicTracks[appState.selectedTrackIndex].first,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = FlowPeakTheme.colors.textPrimary
                    )
                    Text(
                        text = "Stil: ${musicTracks[appState.selectedTrackIndex].second}",
                        style = MaterialTheme.typography.labelSmall,
                        color = FlowPeakTheme.colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selector vizual melode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                musicTracks.forEachIndexed { i, track ->
                    val isSelected = appState.selectedTrackIndex == i
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
                            .clickable {
                                appState.selectedTrackIndex = i
                                appState.isMusicPlaying = true
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = track.second,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) FlowPeakTheme.colors.primary else FlowPeakTheme.colors.textMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ── Lista Task-uri ────────────────────────────────────────
        Text(
            text = "SARCINI PROPUSE",
            style = MaterialTheme.typography.labelSmall,
            color = FlowPeakTheme.colors.textMuted,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        // Adaugare task nou
        var newTaskText by remember { mutableStateOf(TextFieldValue("")) }
        var newTaskCategory by remember { mutableStateOf(TextFieldValue("")) }
        FCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    placeholder = { Text("Nume sarcină / task...", color = FlowPeakTheme.colors.textMuted) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = FlowPeakTheme.colors.surface2,
                        unfocusedContainerColor = FlowPeakTheme.colors.surface2,
                        focusedTextColor = FlowPeakTheme.colors.textPrimary,
                        unfocusedTextColor = FlowPeakTheme.colors.textPrimary,
                        focusedIndicatorColor = FlowPeakTheme.colors.primary,
                        unfocusedIndicatorColor = FlowPeakTheme.colors.border
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTaskCategory,
                        onValueChange = { newTaskCategory = it },
                        placeholder = { Text("Categorie (ex: Studiu, Dev, Personal)", color = FlowPeakTheme.colors.textMuted) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = FlowPeakTheme.colors.surface2,
                            unfocusedContainerColor = FlowPeakTheme.colors.surface2,
                            focusedTextColor = FlowPeakTheme.colors.textPrimary,
                            unfocusedTextColor = FlowPeakTheme.colors.textPrimary,
                            focusedIndicatorColor = FlowPeakTheme.colors.primary,
                            unfocusedIndicatorColor = FlowPeakTheme.colors.border
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newTaskText.text.isNotBlank()) {
                                val catName = if (newTaskCategory.text.isBlank()) "General" else newTaskCategory.text
                                appState.addTask(newTaskText.text, catName)
                                newTaskText = TextFieldValue("")
                                newTaskCategory = TextFieldValue("")
                            }
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(FlowPeakTheme.colors.primary)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Adaugă",
                            tint = if (FlowPeakTheme.colors.isDark) FlowPeakTheme.colors.background else FlowPeakTheme.colors.surface1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Lista
        FCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (appState.tasks.isEmpty()) {
                    Text(
                        text = "Nu ai task-uri programate.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FlowPeakTheme.colors.textMuted,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    appState.tasks.forEachIndexed { i, task ->
                        // Scale anim for checkbox
                        val checkboxScale = remember { Animatable(1f) }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(FlowPeakTheme.colors.surface2)
                                .border(0.5.dp, FlowPeakTheme.colors.border, RoundedCornerShape(12.dp))
                                .clickable {
                                    coroutineScope.launch {
                                        checkboxScale.animateTo(1.3f, tween(100))
                                        checkboxScale.animateTo(1f, tween(100))
                                    }
                                    appState.toggleTask(task.id)
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .scale(checkboxScale.value)
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (task.isCompleted) FlowPeakTheme.colors.primary else FlowPeakTheme.colors.surface3)
                                    .border(
                                        width = 1.dp,
                                        color = if (task.isCompleted) FlowPeakTheme.colors.primary else FlowPeakTheme.colors.border,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                            ) {
                                if (task.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = if (FlowPeakTheme.colors.isDark) FlowPeakTheme.colors.background else FlowPeakTheme.colors.surface1,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                    ),
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (task.isCompleted) FlowPeakTheme.colors.textMuted else FlowPeakTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Categorie: ${task.category} · +100 XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FlowPeakTheme.colors.textMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    // ── Dialog de Verificare a Atenției ──────────────────────────
    if (appState.showFocusCheckDialog) {
        Dialog(onDismissRequest = { /* Don't dismiss by touching outside */ }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(FlowPeakTheme.colors.surface1)
                    .border(0.5.dp, FlowPeakTheme.colors.border, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Verificarea Atenției",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FlowPeakTheme.colors.accentPink
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Suspicious Mascot Avatar
                    MascotAvatar(
                        colorName = appState.avatarColorName,
                        expression = "Suspicious", // Force suspicious look
                        accessory = appState.avatarAccessory,
                        modifier = Modifier.size(150.dp),
                        isAnimated = false // Freeze breath animation to emphasize concern
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Mai ești focusat?",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = FlowPeakTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (appState.focusCheckSecondsLeft > 0) {
                            "Confirmă prezența în: ${appState.focusCheckSecondsLeft}s"
                        } else {
                            "ALERTA A FOST DECLANȘATĂ!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (appState.focusCheckSecondsLeft > 0) FlowPeakTheme.colors.textMuted else FlowPeakTheme.colors.accentPink,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            appState.showFocusCheckDialog = false
                            isAttentive = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FlowPeakTheme.colors.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text(
                            text = "Da, sunt aici",
                            color = if (FlowPeakTheme.colors.isDark) FlowPeakTheme.colors.background else FlowPeakTheme.colors.surface1,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Extension to scale modifier
fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(
        scaleX = scale,
        scaleY = scale
    )
)
