package com.stitchilyas.vakitvedua.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitchilyas.vakitvedua.data.local.AppPreferences
import com.stitchilyas.vakitvedua.ui.components.DestekOlDialog
import com.stitchilyas.vakitvedua.ui.screens.*
import com.stitchilyas.vakitvedua.ui.theme.BrassGold
import com.stitchilyas.vakitvedua.ui.theme.TealPrimary
import com.stitchilyas.vakitvedua.util.L10n

enum class Screen(val key: String, val defaultTitle: String, val icon: ImageVector) {
    VAKITLER("nav_vakitler", "Vakitler", Icons.Default.AccessTime),
    KIBLE("nav_kible", "Kıble", Icons.Default.Explore),
    REHBER("nav_rehber", "Rehber", Icons.AutoMirrored.Filled.MenuBook),
    KURAN("nav_kuran", "Kur'an", Icons.AutoMirrored.Filled.MenuBook),
    MESAJLAR("nav_mesajlar", "Mesajlar", Icons.AutoMirrored.Filled.Chat),
    ZIKIR("nav_zikir", "Zikir", Icons.Default.Adjust),
    DUALAR("nav_dualar", "Dualar", Icons.AutoMirrored.Filled.MenuBook),
    TAKIP("nav_takip", "Takip", Icons.Default.CheckCircle),
    TAKVIM("nav_takvim", "Takvim", Icons.Default.CalendarMonth),
    AYARLAR("nav_ayarlar", "Ayarlar", Icons.Default.Settings),
    DIGER("nav_diger", "Menü", Icons.Default.MoreHoriz)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    prefs: AppPreferences,
    onThemeChanged: (Int) -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.VAKITLER) }
    var showDestekDialog by remember { mutableStateOf(false) }
    var appLang by remember { mutableStateOf(prefs.language) }

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
                            text = L10n.get("app_name", appLang),
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
                            contentDescription = L10n.get("nav_takip", appLang),
                            tint = if (currentScreen == Screen.TAKIP) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Quick shortcut: Takvim
                    IconButton(onClick = { currentScreen = Screen.TAKVIM }) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = L10n.get("nav_takvim", appLang),
                            tint = if (currentScreen == Screen.TAKVIM) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Quick shortcut: Ayarlar
                    IconButton(onClick = { currentScreen = Screen.AYARLAR }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = L10n.get("nav_ayarlar", appLang),
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
                    val title = L10n.get(screen.key, appLang)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = title
                            )
                        },
                        label = {
                            Text(
                                title,
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
                Screen.REHBER -> DualarScreen(initialTab = 2) // Opens İbadet Rehberi
                Screen.KURAN -> KuranScreen(prefs = prefs)
                Screen.MESAJLAR -> MesajlarScreen()
                Screen.ZIKIR -> ZikirScreen(prefs = prefs)
                Screen.DUALAR -> DualarScreen(initialTab = 0)
                Screen.TAKIP -> TakipScreen(prefs = prefs)
                Screen.TAKVIM -> TakvimScreen(prefs = prefs)
                Screen.AYARLAR -> AyarlarScreen(
                    prefs = prefs,
                    onThemeChanged = onThemeChanged,
                    onLanguageChanged = { newLang ->
                        appLang = newLang
                    }
                )
                Screen.DIGER -> DigerMenuScreen(
                    prefs = prefs,
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

private data class MenuItemData(
    val screen: Screen,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badgeColor: Color,
    val shape: Shape
)

@Composable
fun DigerMenuScreen(
    prefs: AppPreferences,
    onNavigate: (Screen) -> Unit,
    onOpenDestek: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = prefs.language

    val menuItems = remember(lang) {
        listOf(
            MenuItemData(
                screen = Screen.ZIKIR,
                title = L10n.get("nav_zikir", lang),
                subtitle = "Zikirmatik & Esmâlar",
                icon = Icons.Default.Adjust,
                badgeColor = Color(0xFFE91E63),
                shape = CutCornerShape(topStart = 20.dp, bottomEnd = 20.dp)
            ),
            MenuItemData(
                screen = Screen.TAKIP,
                title = L10n.get("nav_takip", lang),
                subtitle = "Namaz & Oruç Çetelesi",
                icon = Icons.Default.CheckCircle,
                badgeColor = Color(0xFF00897B),
                shape = RoundedCornerShape(topEnd = 20.dp, bottomStart = 20.dp)
            ),
            MenuItemData(
                screen = Screen.TAKVIM,
                title = L10n.get("nav_takvim", lang),
                subtitle = "Hicri Takvim & Kandiller",
                icon = Icons.Default.CalendarMonth,
                badgeColor = Color(0xFF3F51B5),
                shape = CutCornerShape(topEnd = 20.dp, bottomStart = 20.dp)
            ),
            MenuItemData(
                screen = Screen.DUALAR,
                title = L10n.get("nav_dualar", lang),
                subtitle = "Arapça Dualar & Cevşen",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                badgeColor = Color(0xFFFFA000),
                shape = RoundedCornerShape(20.dp)
            ),
            MenuItemData(
                screen = Screen.AYARLAR,
                title = L10n.get("nav_ayarlar", lang),
                subtitle = "Konum, Ses & Görünüm",
                icon = Icons.Default.Settings,
                badgeColor = Color(0xFF7B1FA2),
                shape = CutCornerShape(bottomEnd = 20.dp, topStart = 20.dp)
            ),
            MenuItemData(
                screen = Screen.KIBLE,
                title = L10n.get("nav_kible", lang),
                subtitle = "Kâbe Yönü & Pusula",
                icon = Icons.Default.Explore,
                badgeColor = Color(0xFF0288D1),
                shape = RoundedCornerShape(20.dp)
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Vibrant Menu Hero Banner
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
                                Color(0xFF00695C),
                                Color(0xFF3E2723)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🕌", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Vakit ve Dua Menüsü",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "İbadet Rehberi, Zikir, Takip & Tüm Araçlar",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrassGold
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = BrassGold.copy(alpha = 0.25f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Apps,
                                contentDescription = null,
                                tint = BrassGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Special Destek Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .clickable { onOpenDestek() }
                .testTag("menu_destek_banner"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF3E2723),
                                Color(0xFF5D4037)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BrassGold,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.VolunteerActivism,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Geliştiriciye Destek Ol",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrassGold
                            )
                            Text(
                                text = "Kısa reklam izleyerek hayır duası alın",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Button(
                        onClick = { onOpenDestek() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = BrassGold),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("✨ Destek", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Feature Cards Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(menuItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                        .shadow(3.dp, item.shape)
                        .clip(item.shape)
                        .clickable { onNavigate(item.screen) }
                        .testTag("menu_card_${item.screen.name.lowercase()}"),
                    shape = item.shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, item.badgeColor.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = item.badgeColor,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        item.icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = item.badgeColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
