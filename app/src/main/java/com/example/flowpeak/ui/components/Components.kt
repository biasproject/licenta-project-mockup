package com.example.flowpeak.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.flowpeak.ui.theme.FlowPeakTheme

// ── Card de bază ───────────────────────────────────────────────
@Composable
fun FCard(
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val border = if (accentColor != null)
        BorderStroke(1.dp, accentColor.copy(alpha = 0.65f))
    else
        BorderStroke(0.5.dp, FlowPeakTheme.colors.border)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = FlowPeakTheme.colors.surface1),
        border   = border,
        content  = { Column(modifier = Modifier.padding(20.dp), content = content) }
    )
}

// ── Label mic deasupra valorii ─────────────────────────────────
@Composable
fun CardLabel(text: String) {
    Text(
        text  = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = FlowPeakTheme.colors.textMuted,
        letterSpacing = 1.5.sp
    )
}

// ── Valoare mare ───────────────────────────────────────────────
@Composable
fun CardValue(text: String, unit: String = "") {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(text, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        if (unit.isNotEmpty()) {
            Spacer(Modifier.width(4.dp))
            Text(unit, style = MaterialTheme.typography.bodyMedium, color = FlowPeakTheme.colors.textMuted,
                modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}

// ── Tag colorat ────────────────────────────────────────────────
@Composable
fun FTag(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

// ── Header ecran ───────────────────────────────────────────────
@Composable
fun ScreenHeader(tag: String, title: String, subtitle: String = "") {
    Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 20.dp)) {
        Text(tag.uppercase(), style = MaterialTheme.typography.labelSmall,
            color = FlowPeakTheme.colors.primary, letterSpacing = 2.sp)
        Spacer(Modifier.height(6.dp))
        Text(title, style = MaterialTheme.typography.displaySmall, color = FlowPeakTheme.colors.textPrimary)
        if (subtitle.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = FlowPeakTheme.colors.textMuted)
        }
    }
}

// ── Divider subtil ─────────────────────────────────────────────
@Composable
fun FDivider() {
    HorizontalDivider(color = FlowPeakTheme.colors.border, thickness = 0.5.dp)
}

// ── Pill/chip selectabil ───────────────────────────────────────
@Composable
fun FPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg  = if (selected) FlowPeakTheme.colors.primary else FlowPeakTheme.colors.surface2
    val txt = if (selected) {
        if (FlowPeakTheme.colors.isDark) FlowPeakTheme.colors.background else FlowPeakTheme.colors.surface1
    } else {
        FlowPeakTheme.colors.textMuted
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(0.5.dp, FlowPeakTheme.colors.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = txt)
    }
}
