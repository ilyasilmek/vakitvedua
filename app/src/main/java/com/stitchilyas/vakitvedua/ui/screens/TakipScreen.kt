package com.stitchilyas.vakitvedua.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitchilyas.vakitvedua.data.local.AppPreferences
import com.stitchilyas.vakitvedua.ui.theme.BrassGold
import com.stitchilyas.vakitvedua.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TakipScreen(
    prefs: AppPreferences,
    modifier: Modifier = Modifier
) {
    var trackerRefresh by remember { mutableIntStateOf(0) }

    val kazaKeys = listOf(
        "sabah" to "Sabah Farzı",
        "ogle" to "Öğle Farzı",
        "ikindi" to "İkindi Farzı",
        "aksam" to "Akşam Farzı",
        "yatsi" to "Yatsı Farzı",
        "vitir" to "Vitir Namazı",
        "oruc" to "Kaza Orucu"
    )

    // Compute past 7 days (including today)
    val past7Days = remember(trackerRefresh) {
        val list = mutableListOf<Date>()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, -6)
        }
        for (i in 0 until 7) {
            list.add(cal.time)
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    val prayerNames = listOf("Sabah", "Öğle", "İkindi", "Akşam", "Yatsı")
    val dayFormat = SimpleDateFormat("EE", Locale("tr"))
    val dateFormat = SimpleDateFormat("d MMM", Locale("tr"))

    // Calculate weekly total
    var weeklyCompleted = 0
    past7Days.forEach { d ->
        prayerNames.forEach { p ->
            if (prefs.isPrayerChecked(d, p)) weeklyCompleted++
        }
    }
    val weeklyTotal = 35

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        item {
            Text(
                text = "Namaz Takibi ve Kaza",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Günlük vakitlerinizi işaretleyin, kaza namazlarınızı takip edin.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Weekly Attendance Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_tracker_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Son 7 Günlük İbadet Takibi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TealPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "$weeklyCompleted / $weeklyTotal kılındı",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = TealPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Column headers (Days)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vakit",
                            modifier = Modifier.width(52.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        past7Days.forEach { d ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = dayFormat.format(d),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = dateFormat.format(d),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Prayer Rows
                    prayerNames.forEach { prayer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = prayer,
                                modifier = Modifier.width(52.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )

                            past7Days.forEach { date ->
                                val isChecked = prefs.isPrayerChecked(date, prayer)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isChecked) TealPrimary else Color.Transparent,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.5.dp,
                                            if (isChecked) TealPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clickable {
                                                prefs.togglePrayerChecked(date, prayer)
                                                trackerRefresh++
                                            }
                                    ) {
                                        if (isChecked) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Kılındı",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Kaza Namazı Başlığı & Sihirbaz Butonu
        item {
            var showWizardDialog by remember { mutableStateOf(false) }
            var ageText by remember { mutableStateOf("25") }
            var pubertyText by remember { mutableStateOf("12") }
            var unperformedYearsText by remember { mutableStateOf("2") }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Kaza Namazı Sayacı",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manuel takip edebilir veya sihirbazla hesaplayabilirsiniz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showWizardDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrassGold)
                ) {
                    Text("Sihirbaz", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            if (showWizardDialog) {
                AlertDialog(
                    onDismissRequest = { showWizardDialog = false },
                    title = { Text("🧮 Kaza Borcu Hesaplama Sihirbazı", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sorumlu olduğunuz süre içerisindeki tahmini kılınmayan namaz ve oruç borcunuzu otomatik hesaplar.")

                            OutlinedTextField(
                                value = ageText,
                                onValueChange = { ageText = it },
                                label = { Text("Mevcut Yaşınız") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = pubertyText,
                                onValueChange = { pubertyText = it },
                                label = { Text("Ergenlik/Buluğ Yaşı (Varsayılan 12)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = unperformedYearsText,
                                onValueChange = { unperformedYearsText = it },
                                label = { Text("Tahmini Kılınmayan Yıl Sayısı") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val years = unperformedYearsText.toIntOrNull() ?: 1
                                val days = years * 365
                                prefs.setKazaCount("sabah", days)
                                prefs.setKazaCount("ogle", days)
                                prefs.setKazaCount("ikindi", days)
                                prefs.setKazaCount("aksam", days)
                                prefs.setKazaCount("yatsi", days)
                                prefs.setKazaCount("vitir", days)
                                prefs.setKazaCount("oruc", years * 30)
                                trackerRefresh++
                                showWizardDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Text("Hesapla ve Kaydet")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showWizardDialog = false }) {
                            Text("İptal")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        // Kaza items
        items(kazaKeys) { (key, label) ->
            var count by remember(key, trackerRefresh) {
                mutableIntStateOf(prefs.getKazaCount(key))
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("kaza_row_$key"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$count kaza borcu",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (count > 0) BrassGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // -1
                        FilledTonalIconButton(
                            onClick = {
                                if (count > 0) {
                                    count--
                                    prefs.setKazaCount(key, count)
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Eksilt", modifier = Modifier.size(18.dp))
                        }

                        // Count display
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.widthIn(min = 44.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        }

                        // +1
                        FilledIconButton(
                            onClick = {
                                count++
                                prefs.setKazaCount(key, count)
                            },
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = TealPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Arttır", modifier = Modifier.size(18.dp))
                        }

                        // +5
                        FilledTonalButton(
                            onClick = {
                                count += 5
                                prefs.setKazaCount(key, count)
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("+5", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
