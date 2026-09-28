package com.stitchilyas.vakitvedua.ui.screens

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitchilyas.vakitvedua.calculation.QiblaCalc
import com.stitchilyas.vakitvedua.data.local.AppPreferences
import com.stitchilyas.vakitvedua.ui.theme.BrassGold
import com.stitchilyas.vakitvedua.ui.theme.TealPrimary
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun KibleScreen(
    prefs: AppPreferences,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lat = prefs.latitude
    val lng = prefs.longitude

    val qiblaAngle = remember(lat, lng) { QiblaCalc.calculateQibla(lat, lng) }
    val distanceKm = remember(lat, lng) { QiblaCalc.calculateDistanceKm(lat, lng) }
    val sunPos = remember(lat, lng) { QiblaCalc.getSunAzimuth(lat, lng) }

    var rawHeading by remember { mutableFloatStateOf(0f) }
    var smoothedHeading by remember { mutableFloatStateOf(0f) }
    var sensorAvailable by remember { mutableStateOf(true) }

    // Sensor listener with continuous low-pass filter
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        if (rotationSensor == null) {
            sensorAvailable = false
        }

        val listener = object : SensorEventListener {
            private val rotationMatrix = FloatArray(9)
            private val orientation = FloatArray(3)

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                var deg = 0f
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val azRad = orientation[0]
                    deg = Math.toDegrees(azRad.toDouble()).toFloat()
                    if (deg < 0) deg += 360f
                } else if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
                    deg = event.values[0]
                }

                rawHeading = deg

                // Shortest angular difference for smooth rotation without 360 boundary jump
                val diff = ((deg - smoothedHeading + 180f) % 360f + 360f) % 360f - 180f
                smoothedHeading += diff * 0.18f
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        rotationSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Animated heading for butter-smooth visual response
    val animatedHeading by animateFloatAsState(
        targetValue = smoothedHeading,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "heading"
    )

    // Compute relative angle to Qibla
    val relativeQibla = (qiblaAngle.toFloat() - animatedHeading + 360f) % 360f
    val shortestAngleToQibla = if (relativeQibla > 180f) relativeQibla - 360f else relativeQibla
    val isAligned = abs(shortestAngleToQibla) < 3.5f

    // Pulse animation when aligned
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Haptic vibration feedback on alignment
    var lastVibratedAligned by remember { mutableStateOf(false) }
    LaunchedEffect(isAligned) {
        if (isAligned && !lastVibratedAligned && prefs.vibrationEnabled) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(45)
            }
        }
        lastVibratedAligned = isAligned
    }

    val glowColor by animateColorAsState(
        targetValue = if (isAligned) Color(0xFF00E676) else TealPrimary,
        label = "glowColor"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Kıble Pusulası",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${prefs.cityName} · Kıble Açısı: ${Math.round(qiblaAngle)}°",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Alignment Status Banner with Animated Glow
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(if (isAligned) 6.dp else 1.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = if (isAligned) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface,
            border = if (isAligned) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2E7D32)) else null
        ) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isAligned) Icons.Default.CheckCircle else Icons.Default.Explore,
                    contentDescription = null,
                    tint = if (isAligned) Color(0xFF2E7D32) else TealPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isAligned) "Kıbleye Tam Hizalandınız!" else "Telefonu düz tutun ve Kâbe yönüne dönün",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAligned) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isAligned) "Huzurla namaza durabilirsiniz." else "Fark: ${Math.round(abs(shortestAngleToQibla))}°",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isAligned) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // High-End Islamic Geometric Compass Dial
        Box(
            modifier = Modifier
                .size(300.dp)
                .testTag("qibla_compass_dial"),
            contentAlignment = Alignment.Center
        ) {
            // Dial Canvas (rotates with device heading)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(-animatedHeading)
            ) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2f, h / 2f)
                val r = w / 2f - 14f

                // Outer Bezel Shadow Ring
                drawCircle(
                    color = Color.Black.copy(alpha = 0.06f),
                    radius = r + 4f,
                    center = center
                )

                // Outer Bezel
                drawCircle(
                    color = glowColor.copy(alpha = if (isAligned) 0.35f else 0.15f),
                    radius = r,
                    center = center,
                    style = Stroke(width = 6f)
                )

                // Inner Decorative Ring
                drawCircle(
                    color = BrassGold.copy(alpha = 0.35f),
                    radius = r * 0.72f,
                    center = center,
                    style = Stroke(width = 1.5f)
                )

                // 8-Point Islamic Star Rosette in center
                val starR = r * 0.38f
                val starPath = Path()
                for (s in 0 until 16) {
                    val sAngle = s * (360f / 16f)
                    val sRad = Math.toRadians(sAngle.toDouble())
                    val curR = if (s % 2 == 0) starR else starR * 0.55f
                    val sx = center.x + curR * sin(sRad).toFloat()
                    val sy = center.y - curR * cos(sRad).toFloat()
                    if (s == 0) starPath.moveTo(sx, sy) else starPath.lineTo(sx, sy)
                }
                starPath.close()
                drawPath(starPath, color = BrassGold.copy(alpha = 0.12f))
                drawPath(starPath, color = BrassGold.copy(alpha = 0.4f), style = Stroke(width = 1.2f))

                // Compass Dial Ticks (360 degrees: 120 ticks)
                for (i in 0 until 120) {
                    val angleDeg = i * 3f
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val isMajor = i % 10 == 0 // every 30 deg
                    val isMid = i % 5 == 0 && !isMajor // every 15 deg

                    val tickLen = if (isMajor) 18f else if (isMid) 12f else 7f
                    val strokeW = if (isMajor) 3f else if (isMid) 1.8f else 1f

                    val startX = center.x + (r - tickLen) * sin(angleRad).toFloat()
                    val startY = center.y - (r - tickLen) * cos(angleRad).toFloat()
                    val endX = center.x + r * sin(angleRad).toFloat()
                    val endY = center.y - r * cos(angleRad).toFloat()

                    val tickColor = if (isMajor) glowColor else Color.Gray.copy(alpha = 0.5f)

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }

                // Cardinal Points Text (K, G, D, B)
                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        this.isAntiAlias = true
                        this.textAlign = Paint.Align.CENTER
                        this.typeface = Typeface.DEFAULT_BOLD
                    }

                    // North (K - Kuzey) in bold red
                    paint.color = Color(0xFFE53935).toArgb()
                    paint.textSize = 34f
                    canvas.nativeCanvas.drawText("K", center.x, center.y - r + 38f, paint)

                    // South (G - Güney)
                    paint.color = Color.Gray.toArgb()
                    paint.textSize = 30f
                    canvas.nativeCanvas.drawText("G", center.x, center.y + r - 20f, paint)

                    // East (D - Doğu)
                    canvas.nativeCanvas.drawText("D", center.x + r - 28f, center.y + 10f, paint)

                    // West (B - Batı)
                    canvas.nativeCanvas.drawText("B", center.x - r + 28f, center.y + 10f, paint)
                }

                // North Arrow Marker (Classic red pointer)
                val northPath = Path().apply {
                    moveTo(center.x, center.y - r + 46f)
                    lineTo(center.x - 8f, center.y - r + 64f)
                    lineTo(center.x + 8f, center.y - r + 64f)
                    close()
                }
                drawPath(northPath, color = Color(0xFFE53935))
            }

            // Golden Needle pointing to Qibla (relative to device heading)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(relativeQibla),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Ka'ba Gold Beacon Badge at top of needle
                    Surface(
                        shape = CircleShape,
                        color = if (isAligned) Color(0xFF00E676) else BrassGold,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(if (isAligned) (36f * pulseScale).dp else 34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "🕋",
                                fontSize = 18.sp
                            )
                        }
                    }

                    // Needle Shaft Top (Golden arrow pointing up)
                    Surface(
                        modifier = Modifier
                            .width(6.dp)
                            .height(60.dp),
                        shape = RoundedCornerShape(3.dp),
                        color = if (isAligned) Color(0xFF00E676) else BrassGold
                    ) {}

                    // Center Pivot Dot
                    Surface(
                        shape = CircleShape,
                        color = if (isAligned) Color(0xFF00E676) else TealPrimary,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(8.dp)
                            ) {}
                        }
                    }

                    // Needle Counter-weight Bottom (slate gray)
                    Surface(
                        modifier = Modifier
                            .width(4.dp)
                            .height(45.dp),
                        shape = RoundedCornerShape(2.dp),
                        color = Color.LightGray.copy(alpha = 0.7f)
                    ) {}

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Digital Degree Readout in Lower Center
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "${Math.round(animatedHeading)}° ${getCardinalTurkish(animatedHeading)}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // AR Camera Mode Button
        var showArCameraDialog by remember { mutableStateOf(false) }

        Button(
            onClick = { showArCameraDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ar_camera_qibla_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kamera (AR) ile Kıble Yönü Bul", fontWeight = FontWeight.Bold)
        }

        if (showArCameraDialog) {
            AlertDialog(
                onDismissRequest = { showArCameraDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🕋 Kamera (AR) Kıble Rehberi", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "📷 Kamera Görünümü\n\nKıble Açısı: ${Math.round(qiblaAngle)}°\nFark: ${Math.round(abs(shortestAngleToQibla))}°\n\n${if (isAligned) "✅ KÂBE YÖNÜNDESİNİZ!" else "➡️ Kâbe için telefonu sağa/sola çevirin"}",
                                    color = if (isAligned) Color(0xFF00E676) else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showArCameraDialog = false }) {
                        Text("Tamam")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Information Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Distance to Kaaba Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NearMe, contentDescription = null, tint = BrassGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kâbe Mesafesi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${String.format(java.util.Locale.getDefault(), "%,d", Math.round(distanceKm))} km",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                    Text("Kuş uçuşu Mekke", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            // Qibla Angle Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Explore, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kıble Açısı", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${Math.round(qiblaAngle)}°",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                    Text("Kuzeyden saat yönü", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sun Azimuth Helper Card (for daytime verification)
        if (sunPos.elevation > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF39C12).copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFF39C12))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Güneş ile Kıble Doğrulama", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "Şu an Güneş açısı: ${Math.round(sunPos.azimuth)}°. Pusula şüpheye düşürürse güneşe göre açınızı doğrulayabilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Calibration Tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ℹ️ Doğru sonuç için telefonu düz (yatay) tutun. Manyetik kılıf veya metal eşyalardan uzaklaştırın. Sapma olursa telefonu havada 8 şeklinde çevirin.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(90.dp))
    }
}

fun getCardinalTurkish(degrees: Float): String {
    val deg = (degrees % 360f + 360f) % 360f
    return when {
        deg >= 337.5 || deg < 22.5 -> "K"
        deg < 67.5 -> "KD"
        deg < 112.5 -> "D"
        deg < 157.5 -> "GD"
        deg < 202.5 -> "G"
        deg < 247.5 -> "GB"
        deg < 292.5 -> "B"
        else -> "KB"
    }
}
