package com.stitchilyas.vakitvedua.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitchilyas.vakitvedua.data.local.AppPreferences
import com.stitchilyas.vakitvedua.data.local.DataLoader
import com.stitchilyas.vakitvedua.ui.theme.BrassGold
import com.stitchilyas.vakitvedua.ui.theme.TealPrimary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyarlarScreen(
    prefs: AppPreferences,
    onThemeChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCityPicker by remember { mutableStateOf(false) }
    val cities = remember { DataLoader.loadCities(context) }

    var selectedCityName by remember { mutableStateOf(prefs.cityName) }
    var selectedDistrictName by remember {
        mutableStateOf(
            if (prefs.districtName.isNotEmpty()) prefs.districtName
            else if (prefs.cityName == "İstanbul") "Maltepe"
            else ""
        )
    }

    var themeMode by remember { mutableIntStateOf(prefs.themeMode) }
    var ezanSound by remember { mutableStateOf(prefs.ezanSoundEnabled) }
    var reminderMin by remember { mutableIntStateOf(prefs.reminderMinutesBefore) }
    var kerahatWarn by remember { mutableStateOf(prefs.kerahatWarningEnabled) }
    var vibration by remember { mutableStateOf(prefs.vibrationEnabled) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        item {
            Text(
                text = "Ayarlar",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Konum Bölümü
        item {
            Text(
                text = "KONUM",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCityPicker = true }
                    .testTag("settings_location_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Seçili İl / İlçe",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        val displayLoc = if (selectedDistrictName.isNotEmpty()) {
                            "${selectedCityName.uppercase(Locale.getDefault())}-$selectedDistrictName"
                        } else {
                            selectedCityName.uppercase(Locale.getDefault())
                        }
                        Text(
                            text = displayLoc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Görünüm & Tema
        item {
            Text(
                text = "GÖRÜNÜM",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Uygulama Teması",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Sistem", "Açık", "Koyu").forEachIndexed { index, name ->
                            FilterChip(
                                selected = themeMode == index,
                                onClick = {
                                    themeMode = index
                                    prefs.themeMode = index
                                    onThemeChanged(index)
                                },
                                label = { Text(name) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Bildirimler
        item {
            Text(
                text = "BİLDİRİM VE SESLER",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Ezan Sesi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ezan Sesiyle Hatırlatma", fontWeight = FontWeight.SemiBold)
                            Text("Vakit girdiğinde ezan sesi çalar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = ezanSound,
                            onCheckedChange = {
                                ezanSound = it
                                prefs.ezanSoundEnabled = it
                            }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Kerahat Uyarısı
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Kerahat Vakti Bildirimi", fontWeight = FontWeight.SemiBold)
                            Text("Kerahat vakti girdiğinde uyar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = kerahatWarn,
                            onCheckedChange = {
                                kerahatWarn = it
                                prefs.kerahatWarningEnabled = it
                            }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Titreşim
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Titreşim Geri Bildirimi", fontWeight = FontWeight.SemiBold)
                            Text("Zikirmatik ve kıble hizalamada titreşim", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = vibration,
                            onCheckedChange = {
                                vibration = it
                                prefs.vibrationEnabled = it
                            }
                        )
                    }
                }
            }
        }

        // Hakkında & Gizlilik
        item {
            Text(
                text = "HAKKINDA",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vakit ve Dua: Namaz Vakitleri",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                    Text(
                        text = "Sürüm 1.54.01 (Native Android & Jetpack Compose)",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrassGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vakit ve Dua; namaz vakitlerini, kıbleyi, duaları ve günlük ibadet takibini sade bir arayüzde toplar. Hesap açmanızı istemez; verileriniz tamamen cihazınızda saklanır.\n\n" +
                                "• Namaz vakitleri Diyanet İşleri Başkanlığı yöntemiyle hesaplanır.\n" +
                                "• Kur'an-ı Kerim metinleri Tanzil Projesi'nden derlenmiştir.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Geliştirici: Stitchİlyas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showCityPicker) {
        CityPickerDialog(
            cities = cities,
            currentCity = selectedCityName,
            currentDistrict = selectedDistrictName,
            onDismiss = { showCityPicker = false },
            onSelect = { city, dist ->
                selectedCityName = city.name
                selectedDistrictName = dist?.name ?: ""
                prefs.cityName = selectedCityName
                prefs.districtName = selectedDistrictName
                prefs.latitude = dist?.lat ?: city.lat
                prefs.longitude = dist?.lng ?: city.lng
                showCityPicker = false
            }
        )
    }
}
