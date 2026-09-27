package com.stitchilyas.vakitvedua.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.stitchilyas.vakitvedua.data.local.DataLoader
import com.stitchilyas.vakitvedua.data.model.DuaItem
import com.stitchilyas.vakitvedua.data.model.EsmaItem
import com.stitchilyas.vakitvedua.data.model.GuideItem
import com.stitchilyas.vakitvedua.data.model.NamazGuideData
import com.stitchilyas.vakitvedua.ui.theme.BrassGold
import com.stitchilyas.vakitvedua.ui.theme.TealPrimary

data class StepModalInfo(
    val categoryBadge: String,
    val stepNumber: String,
    val title: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DualarScreen(
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val tabTitles = listOf("Sure ve Dualar", "Esmâü'l-Hüsnâ", "İbadet Rehberi")

    val duas = remember {
        if (DataLoader.dualar.isEmpty()) DataLoader.loadAll(context)
        DataLoader.dualar
    }
    val esmaList = remember {
        if (DataLoader.esmaList.isEmpty()) DataLoader.loadAll(context)
        DataLoader.esmaList
    }
    val abdestGuide = remember {
        if (DataLoader.abdestGuide.isEmpty()) DataLoader.loadAll(context)
        DataLoader.abdestGuide
    }
    val namazGuide = remember {
        if (DataLoader.namazGuide.isEmpty()) DataLoader.loadAll(context)
        DataLoader.namazGuide
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Dualar ve Rehber",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Row
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

        when (selectedTab) {
            0 -> DuasTabContent(duas)
            1 -> EsmaTabContent(esmaList)
            2 -> RehberTabContent(abdestGuide, namazGuide)
        }
    }
}

@Composable
fun DuasTabContent(duas: List<DuaItem>) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(searchQuery, duas) {
        if (searchQuery.isBlank()) duas
        else duas.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.meal.contains(searchQuery, ignoreCase = true) ||
                    it.okunus.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Dua veya sure ara...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Temizle")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(filtered) { dua ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
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
                                text = dua.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            Row {
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Dua", "${dua.title}\n\n${dua.arabic}\n\n${dua.okunus}\n\n${dua.meal}"))
                                    Toast.makeText(context, "Kopyalandı", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "${dua.title}\n\n${dua.arabic}\n\n${dua.okunus}\n\n${dua.meal}\n\n(Vakit ve Dua)")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Paylaş"))
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Paylaş", modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        if (dua.arabic.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dua.arabic,
                                style = MaterialTheme.typography.titleLarge,
                                fontFamily = FontFamily.Serif,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth(),
                                lineHeight = 36.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (dua.okunus.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Okunuşu:",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrassGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = dua.okunus,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )
                        }

                        if (dua.meal.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Anlamı:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TealPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = dua.meal,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (dua.source.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = dua.source,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EsmaTabContent(esmaList: List<EsmaItem>) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(searchQuery, esmaList) {
        if (searchQuery.isBlank()) esmaList
        else esmaList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.meaning.contains(searchQuery, ignoreCase = true) ||
                    it.arabic.contains(searchQuery)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Esmâü'l-Hüsnâ'da ara...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Temizle")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(filtered) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TealPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = item.id.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.meaning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = item.arabic,
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = FontFamily.Serif,
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RehberTabContent(abdestGuide: List<GuideItem>, namazGuide: List<GuideItem>) {
    var subTab by remember { mutableIntStateOf(1) } // Default to Namazın Kılınışı
    var selectedPrayerIndex by remember { mutableIntStateOf(0) }
    val prayers = remember { NamazGuideData.PRAYERS }

    var selectedStepModal by remember { mutableStateOf<StepModalInfo?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Prominent Sub-Tab Buttons with Shadow & Elevation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ElevatedFilterButton(
                selected = subTab == 1,
                title = "Namaz Kılınışı",
                icon = Icons.Default.MenuBook,
                onClick = { subTab = 1 },
                modifier = Modifier.weight(1f)
            )

            ElevatedFilterButton(
                selected = subTab == 0,
                title = "Abdest Rehberi",
                icon = Icons.Default.CheckCircle,
                onClick = { subTab = 0 },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (subTab == 1) {
            // Shadowed, Prominent Prayer Selector Buttons
            Text(
                text = "KILINACAK VAKTİ SEÇİNİZ",
                style = MaterialTheme.typography.labelSmall,
                color = TealPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
            ) {
                items(prayers.indices.toList()) { idx ->
                    val p = prayers[idx]
                    val isSelected = selectedPrayerIndex == idx

                    Surface(
                        modifier = Modifier
                            .shadow(elevation = if (isSelected) 5.dp else 2.dp, shape = RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedPrayerIndex = idx },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = p.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val prayer = prayers[selectedPrayerIndex]

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // Prayer Header Summary Card with High Contrast
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, TealPrimary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = prayer.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary
                                    )
                                    Text(
                                        text = prayer.rekatSummary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BrassGold
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = TealPrimary,
                                    shadowElevation = 2.dp
                                ) {
                                    Text(
                                        text = prayer.totalRekat,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = prayer.description,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "💡 Detaylı kılınış için aşağıdaki adımlara dokununuz.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TealPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Prayer Sections (e.g. Sünnet, Farz)
                items(prayer.sections) { section ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Section Headline
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = TealPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = section.sectionTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = TealPrimary
                                    ) {
                                        Text(
                                            text = section.rekatCount,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Interactive Clickable Steps
                            section.steps.forEachIndexed { sIdx, (title, desc) ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            selectedStepModal = StepModalInfo(
                                                categoryBadge = "${prayer.name} · ${section.sectionTitle}",
                                                stepNumber = "${sIdx + 1}. Adım",
                                                title = title,
                                                description = desc
                                            )
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = TealPrimary,
                                            modifier = Modifier.size(28.dp),
                                            shadowElevation = 2.dp
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${sIdx + 1}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = title,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = desc,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Icon(
                                            Icons.Default.OpenInFull,
                                            contentDescription = "Detay",
                                            tint = TealPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Abdest Alınışı (Also fully clickable with animated popup!)
            Text(
                text = "ADIM ADIM ABDEST ALINIŞI",
                style = MaterialTheme.typography.labelSmall,
                color = TealPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(abdestGuide.indices.toList()) { idx ->
                    val item = abdestGuide[idx]
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                selectedStepModal = StepModalInfo(
                                    categoryBadge = "Abdest Rehberi",
                                    stepNumber = "${idx + 1}. Aşama",
                                    title = item.title,
                                    description = item.text
                                )
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = TealPrimary,
                                modifier = Modifier.size(36.dp),
                                shadowElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${idx + 1}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 18.sp,
                                    maxLines = 2,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                Icons.Default.OpenInFull,
                                contentDescription = "Detay",
                                tint = TealPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Animated Center Popup Modal Dialog
    selectedStepModal?.let { modal ->
        AnimatedStepDetailDialog(
            info = modal,
            onDismiss = { selectedStepModal = null }
        )
    }
}

@Composable
fun ElevatedFilterButton(
    selected: Boolean,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(if (selected) 6.dp else 2.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (selected) TealPrimary else MaterialTheme.colorScheme.surface,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else TealPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AnimatedStepDetailDialog(
    info: StepModalInfo,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TealPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = info.categoryBadge,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { onDismiss() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Kapat",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Step Number & Title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = TealPrimary,
                            modifier = Modifier.size(36.dp),
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = info.stepNumber.filter { it.isDigit() }.ifEmpty { "•" },
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = info.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Step Detailed Text in Readable Format
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = info.description,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 26.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Confirm Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .shadow(4.dp, RoundedCornerShape(14.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Anladım", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
