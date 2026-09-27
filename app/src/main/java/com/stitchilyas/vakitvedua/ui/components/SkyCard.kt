package com.stitchilyas.vakitvedua.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitchilyas.vakitvedua.calculation.VakitCalc
import java.util.Locale

@Composable
fun SkyCard(
    vakitInfo: VakitCalc.CurrentVakitInfo,
    activeVakitName: String,
    modifier: Modifier = Modifier
) {
    // Natural dignified sky gradients according to traditional daily prayer times
    val skyColors = when (vakitInfo.activeIndex) {
        0 -> listOf(Color(0xFF1A2E2B), Color(0xFF2C4A43)) // İmsak / Seher - Deep emerald night
        1 -> listOf(Color(0xFF2E3842), Color(0xFFC5884B)) // Güneş doğuşu - Horizon dawn
        2 -> listOf(Color(0xFF1B4D3C), Color(0xFF2E735B)) // Öğle - Deep emerald zenith
        3 -> listOf(Color(0xFF8C532B), Color(0xFF3D2413)) // İkindi - Warm amber afternoon
        4 -> listOf(Color(0xFF38232F), Color(0xFF1C131A)) // Akşam - Twilight dusk
        else -> listOf(Color(0xFF0F1816), Color(0xFF1C2A26)) // Yatsı - Deep nocturnal obsidian
    }

    val hours = vakitInfo.remainingMillis / 3600000L
    val minutes = (vakitInfo.remainingMillis % 3600000L) / 60000L
    val seconds = (vakitInfo.remainingMillis % 60000L) / 1000L
    val countdownText = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sky_countdown_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(skyColors))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GÜNCEL VAKİT: $activeVakitName".uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD8C193),
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${vakitInfo.nextName} Vaktine Kalan",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Kerahat status badge
                    if (vakitInfo.isKerahat) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFA83A2A),
                            contentColor = Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Kerahat",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Kerahat Vakti",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            contentColor = Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Müsait Vakit",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFC5A059)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Eda Vakti",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Countdown display
                Text(
                    text = countdownText,
                    fontSize = 44.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Kerahat detail or peaceful message
                if (vakitInfo.isKerahat) {
                    val kMin = (vakitInfo.kerahatRemainingMillis / 60000L).coerceAtLeast(1)
                    Text(
                        text = "${vakitInfo.kerahatName ?: "Kerahat"} vakti içindesiniz. Çıkmasına $kMin dakika kaldı.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF2C3BB),
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "Vaktin ibadetini huzurla eda edebilirsiniz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
