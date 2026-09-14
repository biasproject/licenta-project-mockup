package com.example.flowpeak.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flowpeak.state.AppState
import com.example.flowpeak.ui.components.*
import com.example.flowpeak.ui.theme.*

data class ShopItem(
    val id: String,
    val type: String, // "color", "expression", "accessory"
    val name: String,
    val cost: Int,
    val value: String // value passed to appState
)

// ── Mascot Composable drawn on Canvas ─────────────────────────
@Composable
fun MascotAvatar(
    colorName: String,
    expression: String,
    accessory: String,
    modifier: Modifier = Modifier,
    isAnimated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatarBreath")
    val scaleMultiplier by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    val baseColor = when (colorName) {
        "Orange" -> Color(0xFF2A7E82)
        "Lavender" -> Color(0xFF4A9C98)
        "Blue" -> Color(0xFF4C8E9B)
        "Pink" -> Color(0xFFB46670)
        else -> Color(0xFF789093) // Grey
    }

    val accentColor = FlowPeakTheme.colors.primary

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val baseRadius = (w.coerceAtMost(h) / 3.4f) * scaleMultiplier

        // Draw shadow under the mascot
        drawOval(
            color = Color.Black.copy(alpha = 0.12f),
            topLeft = Offset(cx - baseRadius * 1.1f, cy + baseRadius * 0.7f),
            size = Size(baseRadius * 2.2f, baseRadius * 0.25f)
        )

        // Draw ball (mascot body) with 3D radial gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(baseColor.copy(alpha = 0.75f), baseColor),
                center = Offset(cx - baseRadius * 0.35f, cy - baseRadius * 0.35f),
                radius = baseRadius * 1.4f
            ),
            radius = baseRadius,
            center = Offset(cx, cy)
        )

        // Face variables
        val eyeDistance = baseRadius * 0.34f
        val eyeY = cy - baseRadius * 0.18f
        val eyeRadius = baseRadius * 0.08f

        // Draw facial expressions
        when (expression) {
            "Happy" -> {
                // Curved eyes (smiling upward arcs)
                val pathLeft = Path().apply {
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(
                            cx - eyeDistance - eyeRadius,
                            eyeY - eyeRadius,
                            cx - eyeDistance + eyeRadius,
                            eyeY + eyeRadius
                        ),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                }
                val pathRight = Path().apply {
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(
                            cx + eyeDistance - eyeRadius,
                            eyeY - eyeRadius,
                            cx + eyeDistance + eyeRadius,
                            eyeY + eyeRadius
                        ),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                }
                drawPath(
                    pathLeft,
                    color = Color.White,
                    style = Stroke(width = baseRadius * 0.06f)
                )
                drawPath(
                    pathRight,
                    color = Color.White,
                    style = Stroke(width = baseRadius * 0.06f)
                )

                // Smiling mouth
                val mouthPath = Path().apply {
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(
                            cx - baseRadius * 0.22f,
                            cy + baseRadius * 0.05f,
                            cx + baseRadius * 0.22f,
                            cy + baseRadius * 0.35f
                        ),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                }
                drawPath(
                    mouthPath,
                    color = Color.White,
                    style = Stroke(width = baseRadius * 0.06f)
                )
            }
            "Focused" -> {
                // Concentrated intense small eyes
                drawCircle(color = Color.White, radius = eyeRadius * 0.6f, center = Offset(cx - eyeDistance, eyeY))
                drawCircle(color = Color.White, radius = eyeRadius * 0.6f, center = Offset(cx + eyeDistance, eyeY))

                // Small concentrated circular mouth
                drawCircle(
                    color = Color.White,
                    radius = baseRadius * 0.07f,
                    center = Offset(cx, cy + baseRadius * 0.2f),
                    style = Stroke(width = baseRadius * 0.05f)
                )
            }
            "Suspicious" -> {
                // Slanted suspicious eyes
                // Left eye: /
                drawLine(
                    color = Color.White,
                    start = Offset(cx - eyeDistance - eyeRadius * 1.3f, eyeY - eyeRadius * 0.4f),
                    end = Offset(cx - eyeDistance + eyeRadius * 1.3f, eyeY + eyeRadius * 0.4f),
                    strokeWidth = baseRadius * 0.08f
                )
                // Right eye: \
                drawLine(
                    color = Color.White,
                    start = Offset(cx + eyeDistance + eyeRadius * 1.3f, eyeY - eyeRadius * 0.4f),
                    end = Offset(cx + eyeDistance - eyeRadius * 1.3f, eyeY + eyeRadius * 0.4f),
                    strokeWidth = baseRadius * 0.08f
                )

                // Worried wavy mouth line
                drawLine(
                    color = Color.White,
                    start = Offset(cx - baseRadius * 0.16f, cy + baseRadius * 0.2f),
                    end = Offset(cx + baseRadius * 0.16f, cy + baseRadius * 0.16f),
                    strokeWidth = baseRadius * 0.06f
                )
            }
            else -> { // Neutral (Default)
                // Dot eyes
                drawCircle(color = Color.White, radius = eyeRadius, center = Offset(cx - eyeDistance, eyeY))
                drawCircle(color = Color.White, radius = eyeRadius, center = Offset(cx + eyeDistance, eyeY))

                // Straight mouth line
                drawLine(
                    color = Color.White,
                    start = Offset(cx - baseRadius * 0.18f, cy + baseRadius * 0.2f),
                    end = Offset(cx + baseRadius * 0.18f, cy + baseRadius * 0.2f),
                    strokeWidth = baseRadius * 0.06f
                )
            }
        }

        // Draw Accessories
        when (accessory) {
            "Hat" -> {
                // Top Hat
                val hatBaseY = cy - baseRadius * 0.95f
                val hatBaseW = baseRadius * 1.15f
                val hatBaseH = baseRadius * 0.1f

                // Draw brim
                drawRoundRect(
                    color = Color(0xFF1E1E1E),
                    topLeft = Offset(cx - hatBaseW / 2f, hatBaseY),
                    size = Size(hatBaseW, hatBaseH),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )

                // Draw crown
                val hatCrownW = baseRadius * 0.72f
                val hatCrownH = baseRadius * 0.52f
                val hatCrownY = hatBaseY - hatCrownH
                drawRoundRect(
                    color = Color(0xFF1E1E1E),
                    topLeft = Offset(cx - hatCrownW / 2f, hatCrownY),
                    size = Size(hatCrownW, hatCrownH),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Colored ribbon (uses dynamic theme accent color!)
                val ribbonH = baseRadius * 0.08f
                drawRect(
                    color = accentColor,
                    topLeft = Offset(cx - hatCrownW / 2f, hatBaseY - ribbonH),
                    size = Size(hatCrownW, ribbonH)
                )
            }
            "Glasses" -> {
                // Modern black frame glasses
                val frameRadius = eyeRadius * 1.8f
                val frameY = eyeY

                // Left frame
                drawCircle(
                    color = Color(0xFF161616),
                    radius = frameRadius,
                    center = Offset(cx - eyeDistance, frameY),
                    style = Stroke(width = baseRadius * 0.06f)
                )
                // Right frame
                drawCircle(
                    color = Color(0xFF161616),
                    radius = frameRadius,
                    center = Offset(cx + eyeDistance, frameY),
                    style = Stroke(width = baseRadius * 0.06f)
                )
                // Connector bridge
                drawLine(
                    color = Color(0xFF161616),
                    start = Offset(cx - eyeDistance + frameRadius, frameY),
                    end = Offset(cx + eyeDistance - frameRadius, frameY),
                    strokeWidth = baseRadius * 0.06f
                )
            }
            "Bowtie" -> {
                // Red Bowtie at base
                val bowY = cy + baseRadius * 0.95f
                val bowW = baseRadius * 0.42f
                val bowH = baseRadius * 0.22f

                val leftBow = Path().apply {
                    moveTo(cx, bowY)
                    lineTo(cx - bowW, bowY - bowH / 2f)
                    lineTo(cx - bowW, bowY + bowH / 2f)
                    close()
                }
                val rightBow = Path().apply {
                    moveTo(cx, bowY)
                    lineTo(cx + bowW, bowY - bowH / 2f)
                    lineTo(cx + bowW, bowY + bowH / 2f)
                    close()
                }

                drawPath(leftBow, color = Color(0xFFE63946))
                drawPath(rightBow, color = Color(0xFFE63946))
                drawCircle(color = Color.White, radius = baseRadius * 0.06f, center = Offset(cx, bowY))
            }
        }
    }
}

@Composable
fun ProfileScreen(appState: AppState) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var previewColor by remember { mutableStateOf<String?>(null) }
    var previewExpression by remember { mutableStateOf<String?>(null) }
    var previewAccessory by remember { mutableStateOf<String?>(null) }

    // Definition of Shop Items
    val shopItems = listOf(
        // Colors
        ShopItem("c1", "color", "Gri", 0, "Grey"),
        ShopItem("c2", "color", "Portocaliu", 200, "Orange"),
        ShopItem("c3", "color", "Lavandă", 200, "Lavender"),
        ShopItem("c4", "color", "Albastru", 300, "Blue"),
        ShopItem("c5", "color", "Roz", 300, "Pink"),

        // Expressions
        ShopItem("e1", "expression", "Neutru", 0, "Neutral"),
        ShopItem("e2", "expression", "Fericit", 150, "Happy"),
        ShopItem("e3", "expression", "Focusat", 250, "Focused"),
        ShopItem("e4", "expression", "Bănuitor", 250, "Suspicious"),

        // Accessories
        ShopItem("a1", "accessory", "Fără", 0, "None"),
        ShopItem("a2", "accessory", "Pălărie", 400, "Hat"),
        ShopItem("a3", "accessory", "Ochelari", 500, "Glasses"),
        ShopItem("a4", "accessory", "Papion", 350, "Bowtie")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FlowPeakTheme.colors.background)
            .verticalScroll(scrollState)
    ) {
        // Screen Header
        ScreenHeader(
            tag = "Personalizare",
            title = appState.username.ifBlank { "Profilul tău" },
            subtitle = "Nivel ${appState.level} · Personalizează-ți mascota folosind XP."
        )

        // ── Avatar Area ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MascotAvatar(
                colorName = previewColor ?: appState.avatarColorName,
                expression = previewExpression ?: appState.avatarExpression,
                accessory = previewAccessory ?: appState.avatarAccessory,
                modifier = Modifier.size(200.dp)
            )
            
            val hasPreview = previewColor != null || previewExpression != null || previewAccessory != null
            if (hasPreview) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Resetează previzualizarea",
                    color = FlowPeakTheme.colors.accentPink,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(FlowPeakTheme.colors.accentPink.copy(alpha = 0.12f))
                        .border(width = 0.5.dp, color = FlowPeakTheme.colors.accentPink, shape = RoundedCornerShape(10.dp))
                        .clickable {
                            previewColor = null
                            previewExpression = null
                            previewAccessory = null
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        // XP Bar Info
        FCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Nivel ${appState.level}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FlowPeakTheme.colors.textPrimary
                )
                Text(
                    text = "${appState.xpInCurrentLevel}/1000 XP pentru Nivel ${appState.level + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = FlowPeakTheme.colors.primary
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(FlowPeakTheme.colors.surface3)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(appState.xpInCurrentLevel / 1000f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(FlowPeakTheme.colors.primary)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Total XP acumulat: ${appState.xp} XP",
                style = MaterialTheme.typography.labelSmall,
                color = FlowPeakTheme.colors.textMuted
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Magazin (Custom Shop) ──────────────────────────────────
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.ShoppingBag,
                contentDescription = null,
                tint = FlowPeakTheme.colors.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "CUSTOM",
                style = MaterialTheme.typography.labelSmall,
                color = FlowPeakTheme.colors.textMuted,
                letterSpacing = 1.5.sp
            )
        }

        var selectedCustomTab by remember { mutableIntStateOf(0) }
        val customCategories = listOf("Culoare" to "color", "Expresie" to "expression", "Accesorii" to "accessory")

        FCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            // Category pick tabs (Segmented Selector)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FlowPeakTheme.colors.surface2, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                customCategories.forEachIndexed { index, (label, _) ->
                    val isSelected = selectedCustomTab == index
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) FlowPeakTheme.colors.primary else Color.Transparent)
                            .clickable { selectedCustomTab = index }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) {
                                if (FlowPeakTheme.colors.isDark) FlowPeakTheme.colors.background else FlowPeakTheme.colors.surface1
                            } else {
                                FlowPeakTheme.colors.textMuted
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show items for active category tab
            val activeCategory = customCategories[selectedCustomTab].second
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                shopItems.filter { it.type == activeCategory }.forEach { item ->
                    val isUnlocked = when (item.type) {
                        "color" -> appState.unlockedColors.contains(item.value)
                        "expression" -> appState.unlockedExpressions.contains(item.value)
                        else -> appState.unlockedAccessories.contains(item.value)
                    }

                    val isEquipped = when (item.type) {
                        "color" -> appState.avatarColorName == item.value
                        "expression" -> appState.avatarExpression == item.value
                        else -> appState.avatarAccessory == item.value
                    }

                    val isPreviewing = when (item.type) {
                        "color" -> previewColor == item.value
                        "expression" -> previewExpression == item.value
                        else -> previewAccessory == item.value
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FlowPeakTheme.colors.surface2)
                            .border(
                                width = 0.5.dp,
                                color = if (isEquipped) {
                                    FlowPeakTheme.colors.primary
                                } else if (isPreviewing) {
                                    FlowPeakTheme.colors.primary.copy(alpha = 0.5f)
                                } else {
                                    FlowPeakTheme.colors.border
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = FlowPeakTheme.colors.textPrimary
                            )
                            Text(
                                text = if (item.cost == 0) "Gratuit" else "${item.cost} XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isUnlocked) FlowPeakTheme.colors.textMuted else FlowPeakTheme.colors.primary
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Button 1: Încercare (Preview)
                            OutlinedButton(
                                onClick = {
                                    when (item.type) {
                                        "color" -> previewColor = if (isPreviewing) null else item.value
                                        "expression" -> previewExpression = if (isPreviewing) null else item.value
                                        "accessory" -> previewAccessory = if (isPreviewing) null else item.value
                                    }
                                },
                                border = BorderStroke(1.dp, if (isPreviewing) FlowPeakTheme.colors.primary else FlowPeakTheme.colors.border),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isPreviewing) FlowPeakTheme.colors.primary.copy(alpha = 0.12f) else Color.Transparent,
                                    contentColor = if (isPreviewing) FlowPeakTheme.colors.primary else FlowPeakTheme.colors.textMuted
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp),
                                enabled = !isEquipped // Disable preview if it is already active permanently
                            ) {
                                Text(
                                    text = if (isPreviewing) "Încercat" else "Încearcă",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Button 2: Action (Cumpără / Echipează / Echipat)
                            Button(
                                onClick = {
                                    if (isUnlocked) {
                                        // Equip permanently
                                        when (item.type) {
                                            "color" -> {
                                                appState.avatarColorName = item.value
                                                previewColor = null // Clear preview
                                            }
                                            "expression" -> {
                                                appState.avatarExpression = item.value
                                                previewExpression = null
                                            }
                                            "accessory" -> {
                                                appState.avatarAccessory = item.value
                                                previewAccessory = null
                                            }
                                        }
                                    } else {
                                        // Purchase item
                                        if (appState.xp >= item.cost) {
                                            appState.xp -= item.cost
                                            when (item.type) {
                                                "color" -> {
                                                    appState.unlockedColors.add(item.value)
                                                    appState.avatarColorName = item.value
                                                    previewColor = null // Clear preview
                                                }
                                                "expression" -> {
                                                    appState.unlockedExpressions.add(item.value)
                                                    appState.avatarExpression = item.value
                                                    previewExpression = null
                                                }
                                                "accessory" -> {
                                                    appState.unlockedAccessories.add(item.value)
                                                    appState.avatarAccessory = item.value
                                                    previewAccessory = null
                                                }
                                            }
                                            Toast.makeText(context, "${item.name} cumpărat și echipat!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "XP insuficient pentru cumpărare!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    appState.saveUserSettings(appState.username)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isEquipped) {
                                        FlowPeakTheme.colors.surface3
                                    } else if (isUnlocked) {
                                        FlowPeakTheme.colors.primary.copy(alpha = 0.2f)
                                    } else {
                                        FlowPeakTheme.colors.primary
                                    }
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = if (isEquipped) {
                                        "Echipat"
                                    } else if (isUnlocked) {
                                        "Echipează"
                                    } else {
                                        "Cumpără"
                                    },
                                    color = if (isEquipped) {
                                        FlowPeakTheme.colors.textMuted
                                    } else if (isUnlocked) {
                                        FlowPeakTheme.colors.primary
                                    } else {
                                        if (FlowPeakTheme.colors.isDark) FlowPeakTheme.colors.background else FlowPeakTheme.colors.surface1
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Setări Generale ───────────────────────────────────────
        Text(
            text = "SETĂRI GENERALE",
            style = MaterialTheme.typography.labelSmall,
            color = FlowPeakTheme.colors.textMuted,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        FCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            // Tema Automată
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Schimbare automată temă",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = FlowPeakTheme.colors.textPrimary
                    )
                    Text(
                        text = "Aceeași identitate petrol, adaptată luminozității sistemului",
                        style = MaterialTheme.typography.labelSmall,
                        color = FlowPeakTheme.colors.textMuted
                    )
                }
                Switch(
                    checked = appState.isThemeAutomatic,
                    onCheckedChange = { appState.toggleThemeAutomatic(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = FlowPeakTheme.colors.primary,
                        checkedTrackColor = FlowPeakTheme.colors.primary.copy(alpha = 0.5f),
                        uncheckedThumbColor = FlowPeakTheme.colors.textMuted,
                        uncheckedTrackColor = FlowPeakTheme.colors.surface3
                    )
                )
            }

            if (!appState.isThemeAutomatic) {
                Spacer(modifier = Modifier.height(14.dp))
                FDivider()
                Spacer(modifier = Modifier.height(14.dp))

                // Selectare Manuală Temă
                Text(
                    text = "Alege tema manual",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = FlowPeakTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Light Mode Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!appState.isDarkMode) LightAccentOrange.copy(alpha = 0.15f) else FlowPeakTheme.colors.surface2)
                            .border(
                                width = 1.dp,
                                color = if (!appState.isDarkMode) LightAccentOrange else FlowPeakTheme.colors.border,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { appState.toggleDarkMode(false) }
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "Mod luminos",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (!appState.isDarkMode) LightAccentOrange else FlowPeakTheme.colors.textMuted
                        )
                    }

                    // Dark Mode Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (appState.isDarkMode) DarkAccentPurple.copy(alpha = 0.15f) else FlowPeakTheme.colors.surface2)
                            .border(
                                width = 1.dp,
                                color = if (appState.isDarkMode) DarkAccentPurple else FlowPeakTheme.colors.border,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { appState.toggleDarkMode(true) }
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "Mod întunecat",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (appState.isDarkMode) DarkAccentPurple else FlowPeakTheme.colors.textMuted
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Logout Button
            OutlinedButton(
                onClick = { appState.logoutUser() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FlowPeakTheme.colors.accentPink),
                border = BorderStroke(1.dp, FlowPeakTheme.colors.accentPink),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Deconectare cont", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
