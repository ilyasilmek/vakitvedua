package com.stitchilyas.vakitvedua.ui.screens

import android.Manifest
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitchilyas.vakitvedua.data.local.AppPreferences
import com.stitchilyas.vakitvedua.data.local.DataLoader
import com.stitchilyas.vakitvedua.ui.theme.BrassGold
import com.stitchilyas.vakitvedua.ui.theme.TealPrimary
import com.stitchilyas.vakitvedua.util.LocationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyarlarScreen(
    prefs: AppPreferences,
    onThemeChanged: (Int) -> Unit,
    onLanguageChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCityPicker by remember { mutableStateOf(false) }
    val cities = remember { DataLoader.loadCities(context) }

    var selectedCityName by remember { mutableStateOf(prefs.cityName) }
    var selectedDistrictName by remember { mutableStateOf(prefs.districtName) }
    var isLocating by remember { mutableStateOf(false) }

    fun applyDetectedLocation(
        newCityName: String,
        newDistrictName: String,
        newLat: Double,
        newLng: Double
    ) {
        selectedCityName = newCityName
        selectedDistrictName = newDistrictName
        prefs.cityName = newCityName
        prefs.districtName = newDistrictName
        prefs.latitude = newLat
        prefs.longitude = newLng

        val locationText = if (newDistrictName.isNotEmpty()) "$newCityName, $newDistrictName" else newCityName
        Toast.makeText(context, "Konumunuz güncellendi: $locationText", Toast.LENGTH_LONG).show()
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            isLocating = true
            Toast.makeText(context, "Konumunuz tespit ediliyor...", Toast.LENGTH_SHORT).show()
            LocationHelper.getCurrentLocation(context, cities) { result ->
                isLocating = false
                when (result) {
                    is LocationHelper.LocationResult.Success -> {
                        applyDetectedLocation(
                            result.cityName,
                            result.districtName,
                            result.latitude,
                            result.longitude
                        )
                    }
                    is LocationHelper.LocationResult.GpsDisabled -> {
                        Toast.makeText(context, "Lütfen GPS servisini açın.", Toast.LENGTH_LONG).show()
                    }
                    is LocationHelper.LocationResult.PermissionDenied -> {
                        Toast.makeText(context, "Konum izni verilmedi.", Toast.LENGTH_LONG).show()
                    }
                    is LocationHelper.LocationResult.Error -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            Toast.makeText(context, "Konum tespiti için lütfen konum iznini onaylayın.", Toast.LENGTH_LONG).show()
        }
    }

    fun requestGpsLocation() {
        if (!LocationHelper.hasPermission(context)) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        if (!LocationHelper.isLocationEnabled(context)) {
            Toast.makeText(context, "Lütfen telefonunuzun GPS servisini açın.", Toast.LENGTH_LONG).show()
            try {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } catch (_: Exception) {}
            return
        }

        isLocating = true
        Toast.makeText(context, "Konumunuz tespit ediliyor...", Toast.LENGTH_SHORT).show()
        LocationHelper.getCurrentLocation(context, cities) { result ->
            isLocating = false
            when (result) {
                is LocationHelper.LocationResult.Success -> {
                    applyDetectedLocation(
                        result.cityName,
                        result.districtName,
                        result.latitude,
                        result.longitude
                    )
                }
                is LocationHelper.LocationResult.GpsDisabled -> {
                    Toast.makeText(context, "Lütfen GPS servisini açın.", Toast.LENGTH_LONG).show()
                }
                is LocationHelper.LocationResult.PermissionDenied -> {
                    Toast.makeText(context, "Konum izni verilmedi.", Toast.LENGTH_LONG).show()
                }
                is LocationHelper.LocationResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var themeMode by remember { mutableIntStateOf(prefs.themeMode) }
    var ezanSound by remember { mutableStateOf(prefs.ezanSoundEnabled) }
    var kerahatWarn by remember { mutableStateOf(prefs.kerahatWarningEnabled) }
    var vibration by remember { mutableStateOf(prefs.vibrationEnabled) }

    val activeLocationText = if (selectedDistrictName.isNotEmpty()) "$selectedCityName, $selectedDistrictName" else selectedCityName

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 95.dp)
    ) {
        // Hero Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0F3832),
                                    Color(0xFF1B5E20),
                                    Color(0xFF0D47A1)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = BrassGold.copy(alpha = 0.2f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null,
                                            tint = BrassGold,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Ayarlar & Tercihler",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Uygulama Deneyiminizi Kişiselleştirin",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Active Location Badge inside Hero
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, BrassGold.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = BrassGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Aktif Konum: $activeLocationText",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. KONUM AYARLARI
        item {
            SettingsCategoryHeader(
                title = "KONUM VE ŞEHİR",
                icon = Icons.Default.MyLocation,
                badgeColor = Color(0xFF00897B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // GPS Otomatik Konum Card with Cut Corner Shape
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
                        .clickable { if (!isLocating) requestGpsLocation() }
                        .testTag("settings_gps_locate_card"),
                    shape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFF00897B).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF00897B).copy(alpha = 0.15f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isLocating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            strokeWidth = 2.5.dp,
                                            color = Color(0xFF00897B)
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.GpsFixed,
                                            contentDescription = null,
                                            tint = Color(0xFF00897B),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (isLocating) "Konum Aranıyor..." else "GPS ile Otomatik Konum Bul",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Bulunduğunuz il ve ilçeyi anında algılar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF00897B))
                    }
                }

                // Manuel Şehir Seçimi Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .clickable { showCityPicker = true }
                        .testTag("settings_location_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = TealPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Map,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Manuel Şehir & İlçe Seç",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = activeLocationText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TealPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 2. GÖRÜNÜM VE OKUYUCU TERCIHLERI
        item {
            SettingsCategoryHeader(
                title = "GÖRÜNÜM VE OKUYUCU",
                icon = Icons.Default.Palette,
                badgeColor = Color(0xFFFFA000)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Uygulama Teması",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val themes = listOf("Sistem", "Açık", "Koyu")
                        themes.forEachIndexed { index, name ->
                            val isSelected = themeMode == index
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        themeMode = index
                                        prefs.themeMode = index
                                        onThemeChanged(index)
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = name,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                    // Kur'an Tilavet Okuyucusu (Hafız)
                    var selectedReciter by remember { mutableStateOf(prefs.quranReciter) }
                    Column {
                        Text(
                            text = "Kur'an-ı Kerim Okuyucusu (Hafız)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val reciters = listOf(
                                "mishari" to "Mişari el-Afasi",
                                "abdulsamad" to "Abdulbasit Abdussamed",
                                "ghamdi" to "Saad el-Gamidi"
                            )
                            items(reciters) { (id, name) ->
                                val isSelected = selectedReciter == id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedReciter = id
                                        prefs.quranReciter = id
                                    },
                                    label = { Text(name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrassGold,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. BİLDİRİM VE SESLER
        item {
            SettingsCategoryHeader(
                title = "BİLDİRİM VE SESLER",
                icon = Icons.Default.NotificationsActive,
                badgeColor = Color(0xFFE91E63)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Ezan Sesi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ezan Sesiyle Hatırlatma", fontWeight = FontWeight.Bold)
                            Text("Vakit girdiğinde ezan sesi çalar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = ezanSound,
                            onCheckedChange = {
                                ezanSound = it
                                prefs.ezanSoundEnabled = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFE91E63))
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Ezan Makamı Seçimi
                    var selectedMakam by remember { mutableStateOf(prefs.ezanMakam) }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Ezan Makamı Seçimi", fontWeight = FontWeight.Bold)
                        Text("Sabah (Saba), Öğle (Uşşak), İkindi (Rast), Akşam (Segâh), Yatsı (Hicaz)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val makams = listOf("Genel", "Saba", "Uşşak", "Rast", "Segâh", "Hicaz")
                            items(makams) { makam ->
                                val isSelected = selectedMakam == makam
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedMakam = makam
                                        prefs.ezanMakam = makam
                                    },
                                    label = { Text(makam, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFE91E63),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Namazda Otomatik Sessiz Mod
                    var autoSilent by remember { mutableStateOf(prefs.autoSilentMode) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Namazda Otomatik Sessiz Mod", fontWeight = FontWeight.Bold)
                            Text("Vakit girdiğinde telefonu otomatik sessiz/titreşim moduna alır", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = autoSilent,
                            onCheckedChange = {
                                autoSilent = it
                                prefs.autoSilentMode = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFE91E63))
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
                            Text("Kerahat Vakti Bildirimi", fontWeight = FontWeight.Bold)
                            Text("Güneş doğarken, öğle ve akşam öncesi kerahat uyarısı", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = kerahatWarn,
                            onCheckedChange = {
                                kerahatWarn = it
                                prefs.kerahatWarningEnabled = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFE91E63))
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
                            Text("Titreşim Geri Bildirimi", fontWeight = FontWeight.Bold)
                            Text("Zikirmatik ve kıble hizalamada dokunsal titreşim", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = vibration,
                            onCheckedChange = {
                                vibration = it
                                prefs.vibrationEnabled = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFE91E63))
                        )
                    }
                }
            }
        }

        // 4. HAKKINDA & BİLGİ
        item {
            SettingsCategoryHeader(
                title = "HAKKINDA VE BİLGİ",
                icon = Icons.Default.Info,
                badgeColor = Color(0xFF3F51B5)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Vakit ve Dua",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            Text(
                                text = "Sürüm 1.54.02",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrassGold
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = BrassGold.copy(alpha = 0.2f),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "Stitchİlyas",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Unspecified,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Vakit ve Dua; namaz vakitlerini, kıble pusulasını, duaları ve günlük ibadet takibini sade ve modern bir arayüzde sunar. İnternetsiz çalışır ve verileriniz cihazınızda saklanır.\n\n" +
                                "• Namaz vakitleri Diyanet İşleri Başkanlığı yöntemiyle hesaplanır.\n" +
                                "• Kur'an-ı Kerim metinleri Tanzil Projesi'nden derlenmiştir.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp
                    )
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

@Composable
fun SettingsCategoryHeader(
    title: String,
    icon: ImageVector,
    badgeColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = badgeColor,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = badgeColor,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
