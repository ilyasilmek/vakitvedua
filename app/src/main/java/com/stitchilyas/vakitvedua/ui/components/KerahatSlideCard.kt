package com.stitchilyas.vakitvedua.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitchilyas.vakitvedua.calculation.VakitCalc
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun KerahatSlideCard(
    timesToday: LongArray,
    timesTomorrow: LongArray,
    vakitInfo: VakitCalc.CurrentVakitInfo,
    now: Long = System.currentTimeMillis(),
    modifier: Modifier = Modifier
) {
    val periods = VakitCalc.getKerahatPeriods(timesToday)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Calculate countdown for kerahat
    val nextKerahat = periods.find { it.start > now }
        ?: VakitCalc.getKerahatPeriods(timesTomorrow).first()

    val (cdMillis, cdLabel) = if (vakitInfo.isKerahat) {
        Pair(vakitInfo.kerahatRemainingMillis, "Kerahat bitimine")
    } else {
        val rem = max(0L, nextKerahat.start - now)
        Pair(rem, "Sonraki kerahat: ${timeFormat.format(Date(nextKerahat.start))}")
    }

    val hours = cdMillis / 3600000L
    val minutes = (cdMillis % 3600000L) / 60000L
    val seconds = (cdMillis % 60000L) / 1000L
    val countdownStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

    val bgColors = if (vakitInfo.isKerahat) {
        listOf(Color(0xFF3A1F1B), Color(0xFF6B2D24), Color(0xFF4A1F1B))
    } else {
        listOf(Color(0xFF1B2D30), Color(0xFF132325), Color(0xFF1E3033))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("kerahat_slide_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(bgColors))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KERAHAT KADRANI",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (vakitInfo.isKerahat) Color(0xFFFF9C8A) else Color(0xFF5CC2B3),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (vakitInfo.isKerahat) Color(0xFFD5503C).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (vakitInfo.isKerahat) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (vakitInfo.isKerahat) Color.White else Color(0xFFB8F0D0),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (vakitInfo.isKerahat) "Kerahat Vakti" else "Huzurlu Vakit",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Kerahat Countdown Numbers
                Text(
                    text = countdownStr,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = cdLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 24h Kerahat Circular Dial
                KerahatDial(
                    times = timesToday,
                    isKerahat = vakitInfo.isKerahat,
                    kerahatName = vakitInfo.kerahatName,
                    showCard = false,
                    showHeader = false
                )
            }
        }
    }
}
