package com.stitchilyas.vakitvedua.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitchilyas.vakitvedua.data.local.AppPreferences
import com.stitchilyas.vakitvedua.data.local.DataLoader
import com.stitchilyas.vakitvedua.data.model.QuranSurah
import com.stitchilyas.vakitvedua.ui.theme.BrassGold
import com.stitchilyas.vakitvedua.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuranScreen(
    prefs: AppPreferences,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val surahs = remember { DataLoader.loadQuran(context) }

    var selectedSurah by remember { mutableStateOf<QuranSurah?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var arabicFontSize by remember { mutableFloatStateOf(24f) }

    // If surah reading view is open, handle system back button
    if (selectedSurah != null) {
        BackHandler {
            selectedSurah = null
        }
    }

    if (selectedSurah == null) {
        // Surah List View
        val filteredSurahs = remember(searchQuery, surahs) {
            if (searchQuery.isBlank()) surahs
            else surahs.filter {
                it.name.contains(searchQuery, ignoreCase = true) || it.id.toString() == searchQuery
            }
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
        ) {
            item {
                Text(
                    text = "Kur'an-ı Kerim",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sureler ve Âyetler (Arapça Metin)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Sure ara...") },
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
            }

            // Quick Bookmark Card
            item {
                val bookmarked = surahs.find { it.id == prefs.bookmarkSurah }
                if (bookmarked != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSurah = bookmarked },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.1f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = BrassGold)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Son Okunan Sure",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrassGold,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${bookmarked.name} Suresi",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TealPrimary)
                        }
                    }
                }
            }

            // Surahs list with guaranteed unique keys
            items(filteredSurahs, key = { "${it.id}_${it.name}" }) { surah ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedSurah = surah }
                        .testTag("surah_item_${surah.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TealPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = surah.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    } else {
        // Surah Reader View
        val surah = selectedSurah!!
        val isBookmarked = prefs.bookmarkSurah == surah.id

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selectedSurah = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = surah.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Font size down
                    IconButton(
                        onClick = { if (arabicFontSize > 18f) arabicFontSize -= 2f },
                        enabled = arabicFontSize > 18f
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Yazıyı Küçült")
                    }
                    // Font size up
                    IconButton(
                        onClick = { if (arabicFontSize < 40f) arabicFontSize += 2f },
                        enabled = arabicFontSize < 40f
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Yazıyı Büyüt")
                    }
                    // Bookmark
                    IconButton(onClick = {
                        if (isBookmarked) {
                            prefs.bookmarkSurah = -1
                            Toast.makeText(context, "Yer işareti kaldırıldı", Toast.LENGTH_SHORT).show()
                        } else {
                            prefs.bookmarkSurah = surah.id
                            Toast.makeText(context, "Yer işareti eklendi", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Yer İşareti",
                            tint = if (isBookmarked) BrassGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider()

            // Besmele Header (for all surahs except Tawbah #9, if not already starting with Besmele)
            if (surah.id != 9 && surah.id != 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        color = TealPrimary,
                        fontSize = (arabicFontSize * 0.9f).sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Ayahs list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
            ) {
                items(surah.ayahs, key = { "${surah.id}_${it.number}" }) { ayah ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Ayah Number Pill and Share
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = TealPrimary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = ayah.number.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TealPrimary
                                        )
                                    }
                                }

                                Row {
                                    // Copy
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Ayet", "${ayah.text} (${surah.name}, ${ayah.number})"))
                                            Toast.makeText(context, "Ayet kopyalandı", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", modifier = Modifier.size(16.dp))
                                    }

                                    // Share
                                    IconButton(
                                        onClick = {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, "${ayah.text}\n\n(${surah.name} Suresi, ${ayah.number}. Âyet)")
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Âyeti Paylaş"))
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Paylaş", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Arabic Ayah Text
                            Text(
                                text = ayah.text,
                                fontSize = arabicFontSize.sp,
                                fontFamily = FontFamily.Serif,
                                lineHeight = (arabicFontSize * 1.7f).sp,
                                textAlign = TextAlign.Right,
                                style = LocalTextStyle.current.copy(
                                    textDirection = TextDirection.Rtl
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
