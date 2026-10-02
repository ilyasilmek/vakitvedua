package com.stitchilyas.vakitvedua.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitchilyas.vakitvedua.data.local.DataLoader
import com.stitchilyas.vakitvedua.ui.theme.BrassGold
import com.stitchilyas.vakitvedua.ui.theme.TealPrimary
import com.stitchilyas.vakitvedua.util.CardGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MesajlarScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val categories = remember {
        if (DataLoader.messageCategories.isEmpty()) {
            DataLoader.loadAll(context)
        }
        DataLoader.messageCategories
    }

    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Card Creator state
    var showCardDialog by remember { mutableStateOf(false) }
    var cardTitleText by remember { mutableStateOf("Hayırlı Cuma'lar") }
    var cardMessageText by remember { mutableStateOf("Cumanız mübarek, dualarınız kabul olsun.") }
    var selectedStyle by remember { mutableStateOf(CardGenerator.CardStyle.EMERALD_GOLD) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hazır Mesajlar",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cuma, Kandil, Bayram ve tebrik mesajları",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    cardTitleText = "Hayırlı Cuma'lar"
                    cardMessageText = "Cumanız mübarek, dualarınız kabul olsun."
                    showCardDialog = true
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrassGold),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("create_card_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Style,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kart Yap", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        if (showCardDialog) {
            AlertDialog(
                onDismissRequest = { showCardDialog = false },
                title = {
                    Text(
                        "Tebrik Kartı",
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kart Teması:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth())

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(CardGenerator.CardStyle.values()) { style ->
                                FilterChip(
                                    selected = selectedStyle == style,
                                    onClick = { selectedStyle = style },
                                    label = { Text(style.styleName, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Live Card Preview Frame
                        val previewBg = Color(selectedStyle.bgColors[0])
                        val previewAccent = Color(selectedStyle.accentColor)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = previewBg,
                            border = androidx.compose.foundation.BorderStroke(2.dp, previewAccent)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🕌   🌙   🕋", color = previewAccent, fontSize = 14.sp)
                                Text("بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ", color = previewAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = cardTitleText,
                                    color = previewAccent,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = cardMessageText,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("VAKİT VE DUA · DUALARDA BULUŞMAK DİLEĞİYLE", color = previewAccent, fontSize = 9.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val bitmap = CardGenerator.createCardBitmap(
                                title = cardTitleText,
                                message = cardMessageText,
                                cardStyle = selectedStyle
                            )
                            // Caption is empty string -> Share IMAGE ONLY without extra text
                            CardGenerator.shareCardAsImage(
                                context = context,
                                bitmap = bitmap,
                                caption = ""
                            )
                            showCardDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text("Paylaş", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showCardDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("İptal")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Mesajlarda ara...") },
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

        // Category chips (when not actively searching)
        if (categories.isNotEmpty() && searchQuery.isBlank()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories.indices.toList()) { idx ->
                    val cat = categories[idx]
                    FilterChip(
                        selected = selectedCategoryIndex == idx,
                        onClick = { selectedCategoryIndex = idx },
                        label = {
                            Text(
                                text = cat.title,
                                fontWeight = if (selectedCategoryIndex == idx) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        val messagesToDisplay = remember(selectedCategoryIndex, searchQuery, categories) {
            if (searchQuery.isNotBlank()) {
                categories.flatMap { cat ->
                    cat.messages.filter { it.contains(searchQuery, ignoreCase = true) }
                }
            } else {
                categories.getOrNull(selectedCategoryIndex)?.messages ?: emptyList()
            }
        }

        if (messagesToDisplay.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "Aramanıza uygun mesaj bulunamadı." else "Henüz mesaj bulunmuyor.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(messagesToDisplay) { msg ->
                    val currentCategoryTitle = categories.getOrNull(selectedCategoryIndex)?.title ?: "Hayırlı Günler"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("message_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Social Media Action Icons Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        cardTitleText = currentCategoryTitle
                                        cardMessageText = msg
                                        showCardDialog = true
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrassGold),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kart Yap", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. WhatsApp Action
                                    SocialShareIcon(
                                        icon = Icons.Default.Forum,
                                        bgColor = Color(0xFF25D366),
                                        contentDesc = "WhatsApp",
                                        tooltip = "WhatsApp",
                                        onClick = {
                                            shareToPackage(
                                                context = context,
                                                packageName = "com.whatsapp",
                                                text = "$msg\n\n(Vakit ve Dua)"
                                            )
                                        }
                                    )

                                    // 2. Telegram Action
                                    SocialShareIcon(
                                        icon = Icons.AutoMirrored.Filled.Send,
                                        bgColor = Color(0xFF0088CC),
                                        contentDesc = "Telegram",
                                        tooltip = "Telegram",
                                        onClick = {
                                            shareToPackage(
                                                context = context,
                                                packageName = "org.telegram.messenger",
                                                text = "$msg\n\n(Vakit ve Dua)"
                                            )
                                        }
                                    )

                                    // 3. Kopyala Action
                                    SocialShareIcon(
                                        icon = Icons.Default.ContentCopy,
                                        bgColor = MaterialTheme.colorScheme.surfaceVariant,
                                        iconTint = MaterialTheme.colorScheme.onSurface,
                                        contentDesc = "Kopyala",
                                        tooltip = "Kopyala",
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Mesaj", msg))
                                            Toast.makeText(context, "Panoya kopyalandı", Toast.LENGTH_SHORT).show()
                                        }
                                    )

                                    // 4. Diğer / Sistem Paylaşımı
                                    SocialShareIcon(
                                        icon = Icons.Default.Share,
                                        bgColor = TealPrimary,
                                        contentDesc = "Diğer",
                                        tooltip = "Diğer Paylaş",
                                        onClick = {
                                            generalShare(context, msg)
                                        }
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

@Composable
fun SocialShareIcon(
    icon: ImageVector,
    bgColor: Color,
    iconTint: Color = Color.White,
    contentDesc: String,
    tooltip: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = bgColor,
        shadowElevation = 2.dp,
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun shareToPackage(context: Context, packageName: String, text: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage(packageName)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // App not installed -> Fallback to system chooser
        generalShare(context, text)
    }
}

private fun generalShare(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "$text\n\n(Vakit ve Dua)")
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Mesajı Paylaş"))
}
