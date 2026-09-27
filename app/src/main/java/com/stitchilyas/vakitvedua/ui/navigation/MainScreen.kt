package com.stitchilyas.vakitvedua.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitchilyas.vakitvedua.data.local.AppPreferences
import com.stitchilyas.vakitvedua.ui.components.DestekOlDialog
import com.stitchilyas.vakitvedua.ui.screens.*
import com.stitchilyas.vakitvedua.ui.theme.BrassGold
import com.stitchilyas.vakitvedua.ui.theme.TealPrimary

enum class Screen(val title: String, val icon: ImageVector) {
    VAKITLER("Vakitler", Icons.Default.AccessTime),
    KIBLE("Kıble", Icons.Default.Explore),
    REHBER("Rehber", Icons.Default.AutoStories),
    KURAN("Kur'an", Icons.Default.MenuBook),
    MESAJLAR("Mesajlar", Icons.Default.Chat),
    ZIKIR("Zikir", Icons.Default.Adjust),
    DUALAR("Dualar", Icons.Default.AutoStories),
    TAKIP("Takip", Icons.Default.CheckCircle),
    TAKVIM("Takvim", Icons.Default.CalendarMonth),
    AYARLAR("Ayarlar", Icons.Default.Settings),
    DIGER("Menü", Icons.Default.MoreHoriz)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    prefs: AppPreferences,
    onThemeChanged: (Int) -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.VAKITLER) }
    var showDestekDialog by remember { mutableStateOf(false) }

    // System BackHandler: if on secondary screen, go back to Vakitler
    if (currentScreen != Screen.VAKITLER) {
        BackHandler {
            currentScreen = Screen.VAKITLER
        }
    }

    val bottomNavItems = listOf(
        Screen.VAKITLER,
        Screen.KIBLE,
        Screen.REHBER,
        Screen.KURAN,
        Screen.MESAJLAR,
        Screen.DIGER
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { currentScreen = Screen.VAKITLER }
                    ) {
                        Text(
                            text = "Vakit ve Dua",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }
                },
                actions = {
                    // Quick shortcut: Destek Ol
                    IconButton(
                        onClick = { showDestekDialog = true },
                        modifier = Modifier.testTag("topbar_destek_button")
                    ) {
                        Icon(
                            Icons.Default.VolunteerActivism,
                            contentDescription = "Destek Ol",
                            tint = TealPrimary
                        )
                    }

                    // Quick shortcut: Takip
                    IconButton(onClick = { currentScreen = Screen.TAKIP }) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Namaz Takibi",
                            tint = if (currentScreen == Screen.TAKIP) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Quick shortcut: Takvim
                    IconButton(onClick = { currentScreen = Screen.TAKVIM }) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Takvim ve İmsakiye",
                            tint = if (currentScreen == Screen.TAKVIM) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Quick shortcut: Ayarlar
                    IconButton(onClick = { currentScreen = Screen.AYARLAR }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Ayarlar",
                            tint = if (currentScreen == Screen.AYARLAR) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentScreen == screen || (screen == Screen.DIGER && (
                            currentScreen == Screen.TAKIP ||
                                    currentScreen == Screen.DUALAR ||
                                    currentScreen == Screen.TAKVIM ||
                                    currentScreen == Screen.AYARLAR ||
                                    currentScreen == Screen.ZIKIR
                            ))
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                screen.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealPrimary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.VAKITLER -> VakitlerScreen(
                    prefs = prefs,
                    onNavigateToTakip = { currentScreen = Screen.TAKIP }
                )
                Screen.KIBLE -> KibleScreen(prefs = prefs)
                Screen.REHBER -> DualarScreen(initialTab = 2) // Opens İbadet Rehberi (Namaz & Abdest) directly
                Screen.KURAN -> KuranScreen(prefs = prefs)
                Screen.MESAJLAR -> MesajlarScreen()
                Screen.ZIKIR -> ZikirScreen(prefs = prefs)
                Screen.DUALAR -> DualarScreen(initialTab = 0)
                Screen.TAKIP -> TakipScreen(prefs = prefs)
                Screen.TAKVIM -> TakvimScreen(prefs = prefs)
                Screen.AYARLAR -> AyarlarScreen(prefs = prefs, onThemeChanged = onThemeChanged)
                Screen.DIGER -> DigerMenuScreen(
                    onNavigate = { screen -> currentScreen = screen },
                    onOpenDestek = { showDestekDialog = true }
                )
            }
        }
    }

    if (showDestekDialog) {
        DestekOlDialog(onDismiss = { showDestekDialog = false })
    }
}

private data class MenuItemInfo(
    val screen: Screen,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun DigerMenuScreen(
    onNavigate: (Screen) -> Unit,
    onOpenDestek: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gridMenuItems = listOf(
        MenuItemInfo(Screen.ZIKIR, "Zikirmatik", "Zikir & tesbihat sayacı", Icons.Default.Adjust),
        MenuItemInfo(Screen.TAKIP, "Namaz & Kaza", "Kaza namazı takibi", Icons.Default.CheckCircle),
        MenuItemInfo(Screen.TAKVIM, "Takvim & İmsakiye", "Hicri vakitler & takvim", Icons.Default.CalendarMonth),
        MenuItemInfo(Screen.DUALAR, "Sure ve Dualar", "Dualar & ibadet rehberi", Icons.Default.MenuBook)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Tüm Özellikler ve Menü",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Zikirmatik, kaza takibi, takvim ve ayarlar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Destek Ol Special Banner Card in Menu
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable { onOpenDestek() }
                .testTag("menu_destek_banner"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = TealPrimary,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Destek Ol & Reklam İzle",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                        Text(
                            text = "Geliştiriciye katkıda bulunun",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TealPrimary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(gridMenuItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onNavigate(item.screen) }
                        .testTag("menu_card_${item.screen.name.lowercase()}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.15f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = TealPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        item.icon,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Prominent Full-Width Card for "Ayarlar & Bildirimler"
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onNavigate(Screen.AYARLAR) }
                        .testTag("menu_card_ayarlar"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = TealPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "Ayarlar & Bildirimler",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Ezan bildirimleri, sesler, konum ve tema",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
