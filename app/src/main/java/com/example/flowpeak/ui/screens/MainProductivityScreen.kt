package com.example.flowpeak.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flowpeak.state.AppState
import com.example.flowpeak.ui.components.FCard
import com.example.flowpeak.ui.components.FTag
import com.example.flowpeak.ui.components.ScreenHeader
import com.example.flowpeak.ui.theme.FlowPeakTheme

/**
 * Ecranul principal de productivitate (MainProductivityScreen).
 * 
 * Își adaptează dinamic culoarea de fundal prin animateColorAsState în funcție de scorul
 * smilingProbability furnizat de ML Kit, respectând următoarea logică psihologică:
 * 
 * Fundalul rămâne în identitatea vizuală petrol; starea este comunicată doar
 * printr-o tentă discretă, pentru a nu schimba brusc caracterul aplicației.
 */
@Composable
fun MainProductivityScreen(
    smilingProbability: Float,
    appState: AppState? = null
) {
    val isDarkMode = FlowPeakTheme.colors.isDark

    // 1. Calculăm culoarea de fundal țintă în funcție de scorul ML Kit și modul temei (Light / Dark)
    val targetBackgroundColor = remember(smilingProbability, isDarkMode) {
        when {
            // Sub 0.3: o tentă rece, concentrată
            smilingProbability < 0.3f -> {
                if (isDarkMode) Color(0xFF122326) else Color(0xFFF0F6F5)
            }
            // Între 0.3 și 0.7: neutru, fără distrageri
            smilingProbability in 0.3f..0.7f -> {
                if (isDarkMode) Color(0xFF101A1C) else Color(0xFFF5F7F6)
            }
            // Peste 0.7: o tentă ușor mai caldă, păstrând aceeași paletă
            else -> {
                if (isDarkMode) Color(0xFF162628) else Color(0xFFF3F7F4)
            }
        }
    }

    // 2. Animăm tranziția de culoare pentru a fi fluidă și fără flash-uri bruște pe ecran
    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "backgroundColorAnimation"
    )

    // Configurare informații stare psihologică curentă
    val (moodTitle, moodDescription, moodTag, moodColor, moodIcon) = remember(smilingProbability) {
        when {
            smilingProbability < 0.3f -> Tuple5(
                "Concentrare Profundă (Deep Work)",
                "Culoare calmantă ce reduce stresul, scade oboseala ochilor și sporește atenția.",
                "STARE SERIOASĂ / ÎNCORDATĂ",
                Color(0xFF3A8C96),
                Icons.Outlined.Psychology
            )
            smilingProbability in 0.3f..0.7f -> Tuple5(
                "Focus Echilibrat & Constant",
                "Spațiu de lucru curat, neutră și fără distrageri pentru sarcinile zilnice.",
                "STARE NEUTRĂ",
                Color(0xFF6F8588),
                Icons.Outlined.WorkOutline
            )
            else -> Tuple5(
                "Creativitate & Energie Crescută",
                "Nuanță caldă ce stimulează gândirea laterală, dinamismul și starea de bine.",
                "STARE ZÂMBITOARE / RELAXATĂ",
                Color(0xFF4E8E79),
                Icons.Outlined.Bolt
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBackgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        // Header ecran
        ScreenHeader(
            tag = "Productivitate Adaptivă",
            title = "Panoul Tău de Focus",
            subtitle = "Fundalul se adaptează automat stării tale psihologice"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Cardul principal de stare bazat pe ML Kit ─────────────────
        FCard(
            modifier = Modifier.padding(horizontal = 20.dp),
            accentColor = moodColor
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FTag(text = moodTag, color = moodColor)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = moodIcon,
                        contentDescription = null,
                        tint = moodColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(smilingProbability * 100).toInt()}% Zâmbet",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FlowPeakTheme.colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = moodTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FlowPeakTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = moodDescription,
                style = MaterialTheme.typography.bodySmall,
                color = FlowPeakTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Indicator vizual progres scor ML Kit
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Scor zâmbet (ML Kit)",
                        style = MaterialTheme.typography.labelSmall,
                        color = FlowPeakTheme.colors.textMuted
                    )
                    Text(
                        text = "%.2f".format(smilingProbability),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = moodColor
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(FlowPeakTheme.colors.surface3)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(smilingProbability.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(moodColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Widget Sesiune Curentă Focus ──────────────────────────
        FCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SESIUNE ACTIVĂ",
                        style = MaterialTheme.typography.labelSmall,
                        color = FlowPeakTheme.colors.textMuted,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (appState != null) "${appState.timerSecondsLeft / 60}m ramase" else "25m rămase",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = FlowPeakTheme.colors.textPrimary
                    )
                }

                IconButton(
                    onClick = { appState?.let { it.timerIsRunning = !it.timerIsRunning } },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = FlowPeakTheme.colors.primary)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = "Timer",
                        tint = if (FlowPeakTheme.colors.isDark) FlowPeakTheme.colors.background else FlowPeakTheme.colors.surface1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Sfaturi Psihologice de Productivitate ────────────────
        FCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "RECOMANDARE PSIHOLOGICĂ",
                style = MaterialTheme.typography.labelSmall,
                color = FlowPeakTheme.colors.textMuted,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            val recommendationText = when {
                smilingProbability < 0.3f -> "Ești într-o stare de concentrare intensă. Profită de această nuanță verde/albăstruie pentru a lucra la task-uri complexe de analiză sau cod fără întreruperi."
                smilingProbability in 0.3f..0.7f -> "Stare optimă pentru productivitate continuă. Nuanța neutră păstrează atenția pe task-uri fără oboseală vizuală."
                else -> "Stare excelentă și plină de energie! Nuanța caldă stimulează brain-storming-ul, rezolvarea creativă a problemelor și comunicarea."
            }
            Text(
                text = recommendationText,
                style = MaterialTheme.typography.bodyMedium,
                color = FlowPeakTheme.colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

// Data class utilitar pentru gruparea proprietăților de stare
private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
