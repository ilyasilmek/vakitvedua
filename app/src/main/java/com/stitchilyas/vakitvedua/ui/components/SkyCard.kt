package com.stitchilyas.vakitvedua.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
    // Dynamic sky gradient depending on active prayer
    val skyColors = when (vakitInfo.activeIndex) {
        0 -> listOf(Color(0xFF2C3E50), Color(0xFF4CA1AF)) // İmsak / Seher
        1 -> listOf(Color(0xFFF3904F), Color(0xFF3B4371)) // Güneş doğuşu
        2 -> listOf(Color(0xFF2980B9), Color(0xFF6DD5FA), Color(0xFFFFFFFF)) // Öğle
        3 -> listOf(Color(0xFFE65C00), Color(0xFFF9D423)) // İkindi
        4 -> listOf(Color(0xFF834D9B), Color(0xFFD04ED6)) // Akşam
        else -> listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)) // Yatsı
    }

    val hours = vakitInfo.remainingMillis / 3600000L
    val minutes = (vakitInfo.remainingMillis % 3600000L) / 60000L
    val seconds = (vakitInfo.remainingMillis % 60000L) / 1000L
    val countdownText = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sky_countdown_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                            text = "ŞU AN: $activeVakitName".uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${vakitInfo.nextName} Vaktine",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Kerahat status badge
                    if (vakitInfo.isKerahat) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFD5503C).copy(alpha = 0.9f),
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
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.22f),
                            contentColor = Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Huzurlu Vakit",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFB8F0D0)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Namaz Vakti",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Big countdown numbers
                Text(
                    text = countdownText,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Kerahat detail message
                if (vakitInfo.isKerahat) {
                    val kMin = (vakitInfo.kerahatRemainingMillis / 60000L).coerceAtLeast(1)
                    Text(
                        text = "⚠ ${vakitInfo.kerahatName ?: "Kerahat"} · Çıkmasına $kMin dk kaldı. Bu sürede farz/nafile namaz kılınması mekruhtur.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.95f),
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
