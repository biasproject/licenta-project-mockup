package com.example.flowpeak.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flowpeak.state.AppState
import com.example.flowpeak.ui.components.*
import com.example.flowpeak.ui.theme.FlowPeakTheme

@Composable
fun InsightsScreen(appState: AppState) {
    val scrollState = rememberScrollState()
    var selectedChartTab by remember { mutableIntStateOf(0) } // 0 = Timp Muncit, 1 = Task-uri, 2 = Alerte Focus

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FlowPeakTheme.colors.background)
            .verticalScroll(scrollState)
    ) {
        ScreenHeader(
            tag = "Tablou de bord",
            title = "Insights",
            subtitle = "Analiza performanței și recomandări personalizate."
        )

        // ── Selector Date ─────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Timp Lucrat", "Task-uri", "Alerte Focus").forEachIndexed { index, label ->
                val isSelected = selectedChartTab == index
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
                        .clickable { selectedChartTab = index }
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) FlowPeakTheme.colors.primary else FlowPeakTheme.colors.textMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Grafic Canvas ─────────────────────────────────────────
        FCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = when (selectedChartTab) {
                    0 -> "TIMP DE LUCRU (MINUTE / ZI)"
                    1 -> "TASK-URI BIFATE (ZILNIC)"
                    else -> "ALERTE DE ATENȚIE DECLANȘATE"
                },
                style = MaterialTheme.typography.labelSmall,
                color = FlowPeakTheme.colors.textMuted,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Animație bară
            val animationProgress = remember { Animatable(0f) }
            LaunchedEffect(selectedChartTab) {
                animationProgress.snapTo(0f)
                animationProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                )
            }

            val dataPoints = appState.weeklyStats.map {
                when (selectedChartTab) {
                    0 -> it.workedMinutes
                    1 -> it.completedTasks
                    else -> it.focusAlerts
                }
            }
            val maxValue = dataPoints.maxOrNull()?.coerceAtLeast(1f) ?: 1f
            val labels = appState.weeklyStats.map { it.dayName }

            val accentColor = FlowPeakTheme.colors.primary
            val gridColor = FlowPeakTheme.colors.border
            val textMutedColor = FlowPeakTheme.colors.textMuted

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val paddingLeft = 40f
                    val paddingBottom = 60f
                    val chartWidth = canvasWidth - paddingLeft
                    val chartHeight = canvasHeight - paddingBottom

                    // Draw grid lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = chartHeight - (chartHeight / gridLines) * i
                        drawLine(
                            color = gridColor,
                            start = Offset(paddingLeft, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1f
                        )
                    }

                    // Draw bars
                    val barCount = dataPoints.size
                    val spacing = 20f
                    val totalSpacing = spacing * (barCount - 1)
                    val barWidth = (chartWidth - totalSpacing) / barCount

                    dataPoints.forEachIndexed { index, value ->
                        val barHeight = (value / maxValue) * chartHeight * animationProgress.value
                        val x = paddingLeft + index * (barWidth + spacing)
                        val y = chartHeight - barHeight

                        if (barHeight > 0f) {
                            drawRoundRect(
                                color = accentColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }

                        // Draw Day Names
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.argb(
                                    (textMutedColor.alpha * 255).toInt(),
                                    (textMutedColor.red * 255).toInt(),
                                    (textMutedColor.green * 255).toInt(),
                                    (textMutedColor.blue * 255).toInt()
                                )
                                textSize = 11.dp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            drawText(
                                labels[index],
                                x + barWidth / 2,
                                chartHeight + 35f,
                                paint
                            )

                            // Value on top of bar
                            if (value > 0) {
                                val valuePaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.argb(
                                        (accentColor.alpha * 255).toInt(),
                                        (accentColor.red * 255).toInt(),
                                        (accentColor.green * 255).toInt(),
                                        (accentColor.blue * 255).toInt()
                                    )
                                    textSize = 10.dp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                                    isAntiAlias = true
                                }
                                val displayVal = if (selectedChartTab == 0) "${value.toInt()}m" else "${value.toInt()}"
                                drawText(
                                    displayVal,
                                    x + barWidth / 2,
                                    y - 10f,
                                    valuePaint
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Secțiunea Recomandări AI ──────────────────────────────
        Text(
            text = "RECOMANDĂRI AI DE PRODUCTIVITATE",
            style = MaterialTheme.typography.labelSmall,
            color = FlowPeakTheme.colors.textMuted,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        // Card 1
        FCard(
            modifier = Modifier.padding(horizontal = 20.dp),
            accentColor = FlowPeakTheme.colors.primary
        ) {
            Text(
                text = "Ritmul tău circadian",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = FlowPeakTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Datele arată că ești cu 34% mai productiv în prima parte a zilei, mai ales după o sesiune de somn de minim 7 ore. Planifică sarcinile ce cer analiză profundă între orele 09:00 și 12:00.",
                style = MaterialTheme.typography.bodyMedium,
                color = FlowPeakTheme.colors.textPrimary.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card 2
        FCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Managementul pauzelor",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = FlowPeakTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Rata ta de distragere și alertele de focus cresc după 45 de minute de muncă neîntreruptă. Încearcă să reduci timpul sesiunii timer la 25 de minute pentru a menține un flow optim.",
                style = MaterialTheme.typography.bodyMedium,
                color = FlowPeakTheme.colors.textPrimary.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card 3
        FCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Hidratare & Focus",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = FlowPeakTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "În zilele în care tracker-ul de apă a înregistrat mai puțin de 5 pahare, scorul tău de focus a scăzut ușor în a doua parte a zilei. Menține nivelul de hidratare pentru a evita starea de oboseală mentală.",
                style = MaterialTheme.typography.bodyMedium,
                color = FlowPeakTheme.colors.textPrimary.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
