package com.stitchilyas.vakitvedua.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitchilyas.vakitvedua.calculation.HijriCalc
import com.stitchilyas.vakitvedua.calculation.VakitCalc
import com.stitchilyas.vakitvedua.data.local.AppPreferences
import com.stitchilyas.vakitvedua.data.local.DataLoader
import com.stitchilyas.vakitvedua.data.model.City
import com.stitchilyas.vakitvedua.data.model.District
import com.stitchilyas.vakitvedua.ui.components.DestekOlDialog
import com.stitchilyas.vakitvedua.ui.components.KerahatSlideCard
import com.stitchilyas.vakitvedua.ui.components.SkyCard
import com.stitchilyas.vakitvedua.ui.theme.BrassGold
import com.stitchilyas.vakitvedua.ui.theme.TealPrimary
import com.stitchilyas.vakitvedua.util.LocationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VakitlerScreen(
    prefs: AppPreferences,
    onNavigateToTakip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    // Tick every second for accurate countdown
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = System.currentTimeMillis()
        }
    }

    var selectedCityName by remember { mutableStateOf(prefs.cityName) }
    var selectedDistrictName by remember { mutableStateOf(prefs.districtName) }
    var lat by remember { mutableDoubleStateOf(prefs.latitude) }
    var lng by remember { mutableDoubleStateOf(prefs.longitude) }

    val cities = remember { DataLoader.loadCities(context) }
    var showCityDialog by remember { mutableStateOf(false) }

    // Prayer times calculation for today and tomorrow
    val todayCal = remember(now) { Calendar.getInstance().apply { timeInMillis = now } }
    val tomorrowCal = remember(now) {
        (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
    }

    val timesToday = remember(lat, lng, todayCal.get(Calendar.DAY_OF_YEAR)) {
        VakitCalc.times(todayCal, lat, lng)
    }
    val timesTomorrow = remember(lat, lng, tomorrowCal.get(Calendar.DAY_OF_YEAR)) {
        VakitCalc.times(tomorrowCal, lat, lng)
    }

    val vakitInfo = remember(timesToday, timesTomorrow, now) {
        VakitCalc.getCurrentInfo(timesToday, timesTomorrow, now)
    }

    val hijriDate = remember(todayCal) { HijriCalc.fromGregorian(todayCal) }
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // GPS Location Detection
    var isLocating by remember { mutableStateOf(false) }

    fun applyDetectedLocation(
        newCityName: String,
        newDistrictName: String,
        newLat: Double,
        newLng: Double
    ) {
        selectedCityName = newCityName
        selectedDistrictName = newDistrictName
        lat = newLat
        lng = newLng
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
            Toast.makeText(context, "Konumunuz aranıyor...", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(
                            context,
                            "Lütfen telefonunuzun GPS servisini açın.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is LocationHelper.LocationResult.PermissionDenied -> {
                        Toast.makeText(
                            context,
                            "Konum izni verilmedi.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is LocationHelper.LocationResult.Error -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            Toast.makeText(
                context,
                "Konumunuzu otomatik bulabilmek için lütfen konum iznini onaylayın.",
                Toast.LENGTH_LONG
            ).show()
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
            Toast.makeText(
                context,
                "Lütfen telefonunuzun Konum (GPS) servisini açın.",
                Toast.LENGTH_LONG
            ).show()
            try {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } catch (_: Exception) {}
            return
        }

        isLocating = true
        Toast.makeText(context, "Konumunuz aranıyor...", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(
                        context,
                        "Lütfen telefonunuzun GPS servisini açın.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is LocationHelper.LocationResult.PermissionDenied -> {
                    Toast.makeText(
                        context,
                        "Konum izni verilmedi.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is LocationHelper.LocationResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // State for tracked prayer checks today
    var trackingVersion by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    var showDestekDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        // Location & Hijri bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCityDialog = true }
                    .testTag("location_picker_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TealPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Konum",
                                    tint = TealPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (selectedDistrictName.isNotEmpty()) "$selectedCityName, $selectedDistrictName" else selectedCityName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Şehir Seç",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "$hijriDate",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrassGold,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // GPS Button
                    IconButton(
                        onClick = {
                            if (!isLocating) {
                                requestGpsLocation()
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("gps_locate_button")
                    ) {
                        if (isLocating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = TealPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = "Konumumu Bul",
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Swipeable Card Carousel (Slide 0: Sky Countdown, Slide 1: Kerahat Kadranı)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    if (page == 0) {
                        val activeName = VakitCalc.NAMES[vakitInfo.activeIndex]
                        SkyCard(
                            vakitInfo = vakitInfo,
                            activeVakitName = activeName
                        )
                    } else {
                        KerahatSlideCard(
                            timesToday = timesToday,
                            timesTomorrow = timesTomorrow,
                            vakitInfo = vakitInfo,
                            now = now
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Carousel Dots Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(2) { pageIndex ->
                        val isSelected = pagerState.currentPage == pageIndex
                        val dotWidth by animateDpAsState(
                            targetValue = if (isSelected) 22.dp else 8.dp,
                            label = "dotWidth"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(8.dp)
                                .width(dotWidth)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSelected) BrassGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pageIndex)
                                    }
                                }
                        )
                    }
                }
            }
        }

        // 6 Prayer Times Grid
        item {
            Text(
                text = "Günün Namaz Vakitleri",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Two rows of 3 vakits each
                val row1 = listOf(0, 1, 2)
                val row2 = listOf(3, 4, 5)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row1.forEach { idx ->
                        VakitCard(
                            index = idx,
                            name = VakitCalc.NAMES[idx],
                            timeMillis = timesToday[idx],
                            isActive = idx == vakitInfo.activeIndex,
                            isNext = idx == vakitInfo.nextIndex,
                            isChecked = prefs.isPrayerChecked(Date(now), VakitCalc.NAMES[idx]),
                            onToggleCheck = {
                                prefs.togglePrayerChecked(Date(now), VakitCalc.NAMES[idx])
                                trackingVersion++
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row2.forEach { idx ->
                        VakitCard(
                            index = idx,
                            name = VakitCalc.NAMES[idx],
                            timeMillis = timesToday[idx],
                            isActive = idx == vakitInfo.activeIndex,
                            isNext = idx == vakitInfo.nextIndex,
                            isChecked = prefs.isPrayerChecked(Date(now), VakitCalc.NAMES[idx]),
                            onToggleCheck = {
                                prefs.togglePrayerChecked(Date(now), VakitCalc.NAMES[idx])
                                trackingVersion++
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Destek Ol & Reklam İzle Butonu
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showDestekDialog = true }
                    .testTag("destek_ol_button"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TealPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.VolunteerActivism,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Destek Ol & Reklam İzle",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = TealPrimary
                            )
                            Text(
                                text = "Uygulamanın gelişimine katkıda bulunun",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { showDestekDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = TealPrimary,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Destek Ol", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Günün Hadisi / Duası Kartı
        item {
            val hadithList = remember { DataLoader.hadisList }
            val dailyHadith = remember(hadithList) {
                if (hadithList.isNotEmpty()) {
                    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                    hadithList[dayOfYear % hadithList.size]
                } else null
            }

            dailyHadith?.let { hadith ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Günün Hadis-i Şerifi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            IconButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "“${hadith.text}”\n(${hadith.source})"
                                        )
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Paylaş"))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Paylaş",
                                    tint = TealPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "“${hadith.text}”",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = hadith.source,
                            style = MaterialTheme.typography.labelSmall,
                            color = BrassGold,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // City Selection Dialog
    if (showCityDialog) {
        CityPickerDialog(
            cities = cities,
            currentCity = selectedCityName,
            currentDistrict = selectedDistrictName,
            onDismiss = { showCityDialog = false },
            onSelect = { city, district ->
                selectedCityName = city.name
                selectedDistrictName = district?.name ?: ""
                lat = district?.lat ?: city.lat
                lng = district?.lng ?: city.lng
                prefs.cityName = selectedCityName
                prefs.districtName = selectedDistrictName
                prefs.latitude = lat
                prefs.longitude = lng
                showCityDialog = false
            }
        )
    }

    if (showDestekDialog) {
        DestekOlDialog(
            onDismiss = { showDestekDialog = false }
        )
    }
}

@Composable
fun VakitCard(
    index: Int,
    name: String,
    timeMillis: Long,
    isActive: Boolean,
    isNext: Boolean,
    isChecked: Boolean,
    onToggleCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val containerColor = when {
        isActive -> TealPrimary.copy(alpha = 0.12f)
        isNext -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        isActive -> TealPrimary
        isNext -> TealPrimary.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                // Ignore Güneş for prayer tracking, 5 daily prayers can be checked
                if (index != 1) onToggleCheck()
            }
            .testTag("vakit_card_$index"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(if (isActive) 1.5.dp else 1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isActive) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isChecked && index != 1) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Kılındı",
                            tint = TealPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeFormat.format(Date(timeMillis)),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) TealPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityPickerDialog(
    cities: List<City>,
    currentCity: String,
    currentDistrict: String,
    onDismiss: () -> Unit,
    onSelect: (City, District?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf(cities.find { it.name == currentCity } ?: cities.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Şehir ve İlçe Seçimi", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Şehir ara...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                val filteredCities = remember(searchQuery, cities) {
                    if (searchQuery.isBlank()) cities
                    else cities.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }

                Text(
                    text = "81 İl",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    items(filteredCities) { city ->
                        val isSelected = city.name == selectedCity?.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedCity = city }
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = city.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = TealPrimary)
                            }
                        }
                    }
                }

                // Districts of chosen city
                selectedCity?.let { city ->
                    if (city.districts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${city.name} İlçeleri",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = currentDistrict.isEmpty() && currentCity == city.name,
                                    onClick = { onSelect(city, null) },
                                    label = { Text("Merkez") }
                                )
                            }
                            items(city.districts) { dist ->
                                FilterChip(
                                    selected = currentDistrict == dist.name && currentCity == city.name,
                                    onClick = { onSelect(city, dist) },
                                    label = { Text(dist.name) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                selectedCity?.let { onSelect(it, null) }
            }) {
                Text("Seç")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
