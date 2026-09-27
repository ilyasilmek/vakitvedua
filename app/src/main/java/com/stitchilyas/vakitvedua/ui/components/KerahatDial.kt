package com.stitchilyas.vakitvedua.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitchilyas.vakitvedua.calculation.VakitCalc
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun KerahatDial(
    times: LongArray,
    isKerahat: Boolean,
    kerahatName: String?,
    modifier: Modifier = Modifier,
    showCard: Boolean = true,
    showHeader: Boolean = true
) {
    val periods = VakitCalc.getKerahatPeriods(times)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val now = System.currentTimeMillis()
    val nowCal = Calendar.getInstance().apply { timeInMillis = now }
    val nowHours = nowCal.get(Calendar.HOUR_OF_DAY) +
            nowCal.get(Calendar.MINUTE) / 60f +
            nowCal.get(Calendar.SECOND) / 3600f

    fun toHours(millis: Long): Float {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
    }

    // Prayer boundaries: [0, imsak, gunes, ogle, ikindi, aksam, yatsi, 24]
    val bounds = listOf(
        0f,
        toHours(times[0]), // İmsak
        toHours(times[1]), // Güneş
        toHours(times[2]), // Öğle
        toHours(times[3]), // İkindi
        toHours(times[4]), // Akşam
        toHours(times[5]), // Yatsı
        24f
    )

    val labels = listOf("Yatsı", "Sabah", "", "Öğle", "İkindi", "Akşam", "Yatsı")
    val wedgeColors = listOf(
        Color(0xFF1E3A40), // Gece
        Color(0xFF2A9D8F), // Sabah
        Color(0xFFE9C46A), // Kuşluk
        Color(0xFFF4A261), // Öğle
        Color(0xFFE76F51), // İkindi
        Color(0xFF2A7B88), // Akşam
        Color(0xFF1E3A40)  // Yatsı
    )

    val textMuteArgb = if (showCard) {
        MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    } else {
        Color.White.copy(alpha = 0.8f).toArgb()
    }

    val dialContent = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (showCard) 16.dp else 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showHeader) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "24 Saatlik Kerahat Kadranı",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (showCard) MaterialTheme.colorScheme.onSurface else Color.White
                    )
                    if (isKerahat) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD5503C).copy(alpha = if (showCard) 0.15f else 0.85f)
                        ) {
                            Text(
                                text = "Şu an Kerahat",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (showCard) Color(0xFFD5503C) else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Canvas Dial Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val center = Offset(w / 2f, h - 14f)
                    val r1 = (w * 0.42f).coerceAtMost(h * 0.90f)
                    val r0 = r1 * 0.54f
                    val rMid = (r0 + r1) / 2f

                    fun hourToAngle(hour: Float): Float {
                        return 180f + (hour / 24f) * 180f
                    }

                    fun buildWedge(h1: Float, h2: Float): Path {
                        val a1 = hourToAngle(h1)
                        val a2 = hourToAngle(h2)
                        val sweep = (a2 - a1).coerceAtLeast(0.5f)

                        return Path().apply {
                            arcTo(
                                rect = Rect(center.x - r1, center.y - r1, center.x + r1, center.y + r1),
                                startAngleDegrees = a1,
                                sweepAngleDegrees = sweep,
                                forceMoveTo = true
                            )
                            arcTo(
                                rect = Rect(center.x - r0, center.y - r0, center.x + r0, center.y + r0),
                                startAngleDegrees = a1 + sweep,
                                sweepAngleDegrees = -sweep,
                                forceMoveTo = false
                            )
                            close()
                        }
                    }

                    // 1. Draw Prayer Wedges
                    for (i in 0 until bounds.size - 1) {
                        val hStart = bounds[i]
                        val hEnd = bounds[i + 1]
                        if (hEnd <= hStart) continue

                        val path = buildWedge(hStart, hEnd)
                        val color = wedgeColors.getOrElse(i) { Color.LightGray }

                        drawPath(path, color = color.copy(alpha = if (showCard) 0.55f else 0.70f))
                        drawPath(path, color = Color.White.copy(alpha = 0.35f), style = Stroke(width = 1.2f))

                        // Draw Label inside wedge
                        val lbl = labels.getOrElse(i) { "" }
                        if (lbl.isNotEmpty()) {
                            val midHour = (hStart + hEnd) / 2f
                            val midAngle = hourToAngle(midHour)
                            val rad = Math.toRadians(midAngle.toDouble())
                            val lx = center.x + rMid * cos(rad).toFloat()
                            val ly = center.y + rMid * sin(rad).toFloat()

                            drawIntoCanvas { canvas ->
                                val paint = Paint().apply {
                                    this.color = Color.White.toArgb()
                                    this.textSize = if (hEnd - hStart > 2f) 28f else 22f
                                    this.isFakeBoldText = true
                                    this.textAlign = Paint.Align.CENTER
                                    this.isAntiAlias = true
                                }
                                canvas.nativeCanvas.drawText(lbl, lx, ly + 8f, paint)
                            }
                        }
                    }

                    // 2. Draw Kerahat Overlays (Coral Wedges)
                    periods.forEach { period ->
                        val kStartH = toHours(period.start)
                        val kEndH = toHours(period.end)
                        val active = now in period.start until period.end

                        val kPath = buildWedge(kStartH, kEndH)
                        val kColor = if (active) Color(0xFFE74C3C) else Color(0xFFF08A7E)

                        drawPath(kPath, color = kColor.copy(alpha = 0.95f))
                        drawPath(kPath, color = Color.White, style = Stroke(width = if (active) 2.5f else 1.2f))
                    }

                    // 3. Draw Outer Hour Ticks & Numbers
                    for (hour in 0..24) {
                        val angle = hourToAngle(hour.toFloat())
                        val rad = Math.toRadians(angle.toDouble())
                        val isMajor = hour % 3 == 0

                        val tickLen = if (isMajor) 14f else 8f
                        val startR = r1 + 3f
                        val endR = startR + tickLen

                        val x1 = center.x + startR * cos(rad).toFloat()
                        val y1 = center.y + startR * sin(rad).toFloat()
                        val x2 = center.x + endR * cos(rad).toFloat()
                        val y2 = center.y + endR * sin(rad).toFloat()

                        drawLine(
                            color = if (isMajor) Color.Gray else Color.Gray.copy(alpha = 0.5f),
                            start = Offset(x1, y1),
                            end = Offset(x2, y2),
                            strokeWidth = if (isMajor) 2.5f else 1.2f
                        )

                        // Draw Hour numbers (00, 03, 06, 09, 12, 15, 18, 21, 24)
                        if (isMajor) {
                            val textR = endR + 14f
                            val tx = center.x + textR * cos(rad).toFloat()
                            val ty = center.y + textR * sin(rad).toFloat()

                            drawIntoCanvas { canvas ->
                                val paint = Paint().apply {
                                    this.color = textMuteArgb
                                    this.textSize = 24f
                                    this.textAlign = Paint.Align.CENTER
                                    this.isAntiAlias = true
                                    this.typeface = Typeface.DEFAULT_BOLD
                                }
                                val txt = String.format(Locale.US, "%02d", hour)
                                canvas.nativeCanvas.drawText(txt, tx, ty + 8f, paint)
                            }
                        }
                    }

                    // 4. Current Time Needle
                    val nowAngle = hourToAngle(nowHours)
                    val nowRad = Math.toRadians(nowAngle.toDouble())
                    val handLen = r1 + 6f
                    val nx = center.x + handLen * cos(nowRad).toFloat()
                    val ny = center.y + handLen * sin(nowRad).toFloat()

                    // Needle line
                    drawLine(
                        color = Color(0xFF2A9D8F),
                        start = center,
                        end = Offset(nx, ny),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                    // Needle Arrow / Tip Circle
                    drawCircle(
                        color = Color(0xFFE76F51),
                        radius = 6f,
                        center = Offset(nx, ny)
                    )
                    // Center Pivot Dot
                    drawCircle(
                        color = Color(0xFF2A9D8F),
                        radius = 8f,
                        center = center
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.5f,
                        center = center
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Kerahat Chips Below
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                periods.forEach { period ->
                    val active = now in period.start until period.end
                    val cardBg = if (active) {
                        Color(0xFFD5503C).copy(alpha = if (showCard) 0.15f else 0.85f)
                    } else {
                        if (showCard) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else Color.White.copy(alpha = 0.12f)
                    }
                    val border = if (active) Color(0xFFF5A99C) else Color.Transparent

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = cardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, border)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = period.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (active) (if (showCard) Color(0xFFB8412F) else Color.White) else (if (showCard) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.8f)),
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = "${timeFormat.format(Date(period.start))}–${timeFormat.format(Date(period.end))}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (active) (if (showCard) Color(0xFFB8412F) else Color.White) else (if (showCard) MaterialTheme.colorScheme.onSurface else Color.White)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCard) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            dialContent()
        }
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            dialContent()
        }
    }
}
