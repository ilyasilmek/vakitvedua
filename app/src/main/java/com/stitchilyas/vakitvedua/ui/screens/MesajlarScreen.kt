package com.stitchilyas.vakitvedua.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
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
import com.stitchilyas.vakitvedua.ui.theme.TealPrimary

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
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
                                Text(
                                    text = "Paylaş:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                        icon = Icons.Default.Send,
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

                                    // 3. SMS Action
                                    SocialShareIcon(
                                        icon = Icons.Default.Sms,
                                        bgColor = Color(0xFF0288D1),
                                        contentDesc = "SMS",
                                        tooltip = "Mesaj",
                                        onClick = {
                                            val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                                                data = Uri.parse("sms:")
                                                putExtra("sms_body", "$msg\n\n(Vakit ve Dua)")
                                            }
                                            try {
                                                context.startActivity(smsIntent)
                                            } catch (e: Exception) {
                                                generalShare(context, msg)
                                            }
                                        }
                                    )

                                    // 4. Kopyala Action
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

                                    // 5. Diğer / Sistem Paylaşımı
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
            .size(36.dp)
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
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
