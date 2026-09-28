package com.stitchilyas.vakitvedua.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.stitchilyas.vakitvedua.calculation.HijriCalc
import com.stitchilyas.vakitvedua.calculation.VakitCalc
import com.stitchilyas.vakitvedua.data.local.AppPreferences
import com.stitchilyas.vakitvedua.ui.theme.BrassGold
import com.stitchilyas.vakitvedua.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakvimScreen(
    prefs: AppPreferences,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Dini Günler & Kandiller", "Aylık İmsakiye")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Takvim ve İmsakiye",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // Dini Günler
            val events = remember { HijriCalc.getUpcomingEvents(400) }
            val dateFormat = SimpleDateFormat("d MMMM yyyy, EEEE", Locale("tr"))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(events) { ev ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("event_item"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ev.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ev.daysLeft == 0) TealPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                if (ev.subtitle.isNotEmpty()) {
                                    Text(
                                        text = ev.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BrassGold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dateFormat.format(ev.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Days remaining pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (ev.daysLeft == 0) TealPrimary else TealPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = when (ev.daysLeft) {
                                        0 -> "BUGÜN"
                                        1 -> "Yarın"
                                        else -> "${ev.daysLeft} gün"
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ev.daysLeft == 0) Color.White else TealPrimary
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Aylık İmsakiye Table
            val lat = prefs.latitude
            val lng = prefs.longitude
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dayFormat = SimpleDateFormat("dd MMM", Locale("tr"))

            // Compute 30 days of prayer times
            val imsakiyeDays = remember(lat, lng) {
                val list = mutableListOf<Pair<Date, LongArray>>()
                val cal = Calendar.getInstance()
                for (i in 0 until 30) {
                    val times = VakitCalc.times(cal, lat, lng)
                    list.add(Pair(cal.time, times))
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                }
                list
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                item {
                    Text(
                        text = "${prefs.cityName} için 30 Günlük Namaz Vakitleri",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Table Header
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = TealPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tarih", modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("İmsak", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Güneş", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Öğle", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("İkindi", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Akşam", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Yatsı", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                items(imsakiyeDays) { (date, times) ->
                    val isToday = remember(date) {
                        val c1 = Calendar.getInstance().apply { time = date }
                        val c2 = Calendar.getInstance()
                        c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isToday) TealPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                        border = if (isToday) androidx.compose.foundation.BorderStroke(1.dp, TealPrimary) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dayFormat.format(date),
                                modifier = Modifier.weight(1.3f),
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (isToday) TealPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            for (t in times) {
                                Text(
                                    text = timeFormat.format(Date(t)),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 11.sp,
                                    fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
