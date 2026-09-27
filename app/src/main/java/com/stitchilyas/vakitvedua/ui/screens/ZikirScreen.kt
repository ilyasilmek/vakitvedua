package com.stitchilyas.vakitvedua.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitchilyas.vakitvedua.data.local.AppPreferences
import com.stitchilyas.vakitvedua.ui.theme.BrassGold
import com.stitchilyas.vakitvedua.ui.theme.TealAccent
import com.stitchilyas.vakitvedua.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZikirScreen(
    prefs: AppPreferences,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val defaultPresets = listOf(
        "Sübhânallâh",
        "Elhamdülillâh",
        "Allâhu ekber",
        "Lâ ilâhe illallâh",
        "Estağfirullâh",
        "Salavat"
    )

    var customDhikrs by remember { mutableStateOf(prefs.getCustomDhikrs()) }
    val allDhikrs = defaultPresets + customDhikrs

    var selectedDhikr by remember { mutableStateOf(prefs.activeDhikrName) }
    var targetCount by remember { mutableIntStateOf(prefs.activeDhikrTarget) }
    var count by remember(selectedDhikr) { mutableIntStateOf(prefs.getDhikrCount(selectedDhikr)) }
    var vibrationEnabled by remember { mutableStateOf(prefs.vibrationEnabled) }

    var showAddDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var newDhikrText by remember { mutableStateOf("") }

    // Completed rounds
    val rounds = if (targetCount > 0) count / targetCount else 0
    val progressWithinTarget = if (targetCount > 0) {
        (count % targetCount).toFloat() / targetCount.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progressWithinTarget,
        label = "progress"
    )

    // Button press scale animation
    var isPressed by remember { mutableStateOf(false) }

    fun triggerVibration() {
        if (!vibrationEnabled) return
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(30)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Zikirmatik",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Dhikr selector chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allDhikrs) { dhikr ->
                val isSelected = dhikr == selectedDhikr
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedDhikr = dhikr
                        prefs.activeDhikrName = dhikr
                        count = prefs.getDhikrCount(dhikr)
                    },
                    label = { Text(dhikr) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TealPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
            item {
                ActionChip(
                    onClick = { showAddDialog = true },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Yeni Ekle")
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Target chips (33, 99, 100, 1000)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hedef: ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            listOf(33, 99, 100, 1000).forEach { tgt ->
                val isSelected = targetCount == tgt
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) BrassGold else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            targetCount = tgt
                            prefs.activeDhikrTarget = tgt
                        }
                ) {
                    Text(
                        text = "$tgt",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Tap Circle & Progress Ring
        Box(
            modifier = Modifier
                .size(240.dp)
                .testTag("dhikr_tap_area"),
            contentAlignment = Alignment.Center
        ) {
            // Circular Progress Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f - 14f

                // Track
                drawCircle(
                    color = TealPrimary.copy(alpha = 0.15f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 12f)
                )

                // Animated Progress Arc
                drawArc(
                    brush = Brush.sweepGradient(listOf(TealAccent, TealPrimary, BrassGold, TealAccent)),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 12f, cap = StrokeCap.Round)
                )
            }

            // Big Tactile Tap Button
            Surface(
                modifier = Modifier
                    .size(190.dp)
                    .scale(if (isPressed) 0.94f else 1.0f)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White)
                    ) {
                        count++
                        prefs.setDhikrCount(selectedDhikr, count)
                        triggerVibration()
                    },
                shape = CircleShape,
                color = TealPrimary,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(TealAccent, TealPrimary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = count.toString(),
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "DOKUN",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Rounds & info
        Text(
            text = "$rounds tur tamamlandı",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = BrassGold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            FilledTonalButton(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Sıfırla", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sıfırla")
            }

            // Vibration Toggle
            FilledTonalIconToggleButton(
                checked = vibrationEnabled,
                onCheckedChange = {
                    vibrationEnabled = it
                    prefs.vibrationEnabled = it
                }
            ) {
                Icon(
                    Icons.Default.Vibration,
                    contentDescription = "Titreşim",
                    tint = if (vibrationEnabled) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Delete Custom Dhikr if active
            if (customDhikrs.contains(selectedDhikr)) {
                FilledTonalIconButton(
                    onClick = {
                        prefs.removeCustomDhikr(selectedDhikr)
                        customDhikrs = prefs.getCustomDhikrs()
                        selectedDhikr = defaultPresets.first()
                        prefs.activeDhikrName = selectedDhikr
                        count = prefs.getDhikrCount(selectedDhikr)
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFD5503C))
                }
            }
        }
    }

    // Add Custom Dhikr Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Yeni Zikir Ekle") },
            text = {
                OutlinedTextField(
                    value = newDhikrText,
                    onValueChange = { newDhikrText = it },
                    label = { Text("Zikir adı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newDhikrText.isNotBlank()) {
                            prefs.addCustomDhikr(newDhikrText.trim())
                            customDhikrs = prefs.getCustomDhikrs()
                            selectedDhikr = newDhikrText.trim()
                            prefs.activeDhikrName = selectedDhikr
                            count = prefs.getDhikrCount(selectedDhikr)
                            newDhikrText = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Sayacı Sıfırla") },
            text = { Text("\"$selectedDhikr\" sayacı sıfırlansın mı?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        count = 0
                        prefs.setDhikrCount(selectedDhikr, 0)
                        showResetDialog = false
                    }
                ) {
                    Text("Sıfırla", color = Color(0xFFD5503C))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

@Composable
fun ActionChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            label()
        }
    }
}
