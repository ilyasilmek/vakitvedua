package com.stitchilyas.vakitvedua.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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

/**
 * Hâfız tercihini her ayetin MP3 dosyasının bulunduğu everyayah.com klasörüne eşler.
 * Kaynak: everyayah.com (ücretsiz, açık erişimli ayet-bazlı tilavet arşivi).
 */
private val RECITER_DIRS = mapOf(
    "mishari" to "Alafasy_128kbps",
    "abdulsamad" to "Abdul_Basit_Murattal_192kbps",
    "ghamadi" to "Ghamadi_40kbps",
    "husary" to "Husary_128kbps",
    "minshawi" to "Minshawy_Murattal_128kbps"
)

/**
 * Ayet ayet sesli okuma denetleyicisi. Her ayet için ayrı MP3 çalar, ayet bitince
 * otomatik olarak sıradaki ayete geçer; surenin sonunda durur. Okunan ayet
 * [currentSurah]/[currentAyahIndex] üzerinden takip edilir ve arayüzde vurgulanır.
 */
private class AyahAudioController(private val context: Context) {

    var mediaPlayer by mutableStateOf<MediaPlayer?>(null)
    var isPlaying by mutableStateOf(false)
    var isBuffering by mutableStateOf(false)
    var currentSurah by mutableStateOf<QuranSurah?>(null)
    var currentAyahIndex by mutableIntStateOf(-1)

    fun ayahAudioUrl(surah: QuranSurah, index: Int, reciterId: String): String {
        val dir = RECITER_DIRS[reciterId] ?: RECITER_DIRS.getValue("mishari")
        val ayahNo = surah.ayahs[index].number
        return "https://everyayah.com/data/$dir/" + String.format("%03d%03d.mp3", surah.id, ayahNo)
    }

    /** Verilen surede verilen ayetten (0 tabanlı indeks) itibaren okumayı başlatır. */
    fun playFrom(surah: QuranSurah, startIndex: Int, reciterId: String) {
        releasePlayer()
        currentSurah = surah
        currentAyahIndex = startIndex.coerceIn(0, surah.ayahs.size - 1)
        startCurrent(reciterId)
    }

    /** Başlat/duraklat: çalıyorsa duraklat, duraklatılmışsa devam ettir, boşsa sureyi baştan çal. */
    fun togglePlayback(surah: QuranSurah, reciterId: String) {
        val mp = mediaPlayer
        if (mp != null && currentSurah?.id == surah.id) {
            if (isPlaying) {
                if (mp.isPlaying) mp.pause()
                isPlaying = false
            } else {
                mp.start()
                isPlaying = true
            }
        } else {
            playFrom(surah, 0, reciterId)
        }
    }

    fun stopAll() {
        releasePlayer()
        currentSurah = null
        currentAyahIndex = -1
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.stop()
        } catch (_: IllegalStateException) {
            // henüz hazırlanmamış oynatıcıyı durdurma denemesi — yoksay
        }
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        isBuffering = false
    }

    private fun startCurrent(reciterId: String) {
        val surah = currentSurah ?: return
        val idx = currentAyahIndex
        if (idx < 0 || idx >= surah.ayahs.size) return
        val url = ayahAudioUrl(surah, idx, reciterId)
        isBuffering = true
        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener {
                    this@AyahAudioController.isBuffering = false
                    this@AyahAudioController.isPlaying = true
                    start()
                }
                setOnCompletionListener { advance(reciterId) }
                setOnErrorListener { _, _, _ ->
                    onPlaybackError()
                    true
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (_: Exception) {
            onPlaybackError()
        }
    }

    private fun advance(reciterId: String) {
        val surah = currentSurah
        if (surah == null || currentAyahIndex >= surah.ayahs.size - 1) {
            // Sure bitti — oynatıcıyı bırak
            releasePlayer()
            return
        }
        releasePlayer()
        currentAyahIndex += 1
        startCurrent(reciterId)
    }

    private fun onPlaybackError() {
        releasePlayer()
        currentAyahIndex = -1
        Toast.makeText(
            context,
            "Ses yüklenemedi. İnternet bağlantınızı kontrol edin.",
            Toast.LENGTH_LONG
        ).show()
    }
}


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

    // Ayet bazlı sesli okuma denetleyicisi (durdur: geri tuşu veya X düğmesi)
    val audio = remember { AyahAudioController(context) }
    DisposableEffect(Unit) {
        onDispose { audio.stopAll() }
    }

    // If surah reading view is open, handle system back button
    if (selectedSurah != null) {
        BackHandler {
            audio.stopAll()
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
                    // Audio Play/Pause Button — sureyi ayet ayet okur
                    IconButton(
                        onClick = { audio.togglePlayback(surah, prefs.quranReciter) },
                        modifier = Modifier.testTag("surah_audio_button")
                    ) {
                        if (audio.isBuffering && audio.currentSurah?.id == surah.id) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = TealPrimary
                            )
                        } else {
                            Icon(
                                imageVector = if (audio.isPlaying && audio.currentSurah?.id == surah.id) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = if (audio.isPlaying && audio.currentSurah?.id == surah.id) "Duraklat" else "Dinle",
                                tint = TealPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

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

            // Audio Player Active Banner
            val surahActive = audio.currentSurah?.id == surah.id
            AnimatedVisibility(visible = surahActive && (audio.isPlaying || audio.isBuffering)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = TealPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            val currentAyahNo = if (audio.currentAyahIndex >= 0 && audio.currentAyahIndex < surah.ayahs.size) surah.ayahs[audio.currentAyahIndex].number else null
                            Text(
                                text = when {
                                    audio.isBuffering -> "Yükleniyor..."
                                    currentAyahNo != null -> "${surah.name} ${currentAyahNo}. Âyet Okunuyor"
                                    else -> "${surah.name} Suresi Okunuyor"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        }
                        IconButton(onClick = { audio.stopAll() }) {
                            Icon(Icons.Default.Close, contentDescription = "Durdur ve Kapat", tint = TealPrimary)
                        }
                    }
                }
            }

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

            // Ayahs list — oynatılan ayet vurgulanır ve görünüme kaydırılır
            val listState = rememberLazyListState()
            // Oynatılan ayet değiştikçe otomatik kaydır
            LaunchedEffect(audio.currentSurah?.id, audio.currentAyahIndex) {
                if (audio.currentSurah?.id == surah.id && audio.currentAyahIndex >= 0 && audio.isPlaying) {
                    try {
                        listState.animateScrollToItem(audio.currentAyahIndex)
                    } catch (_: Exception) {
                        // liste henüz hazır değilse yoksay
                    }
                }
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
            ) {
                itemsIndexed(surah.ayahs, key = { _, a -> "${surah.id}_${a.number}" }) { index, ayah ->
                    val isCurrentAyah = audio.currentSurah?.id == surah.id && audio.currentAyahIndex == index
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentAyah) TealPrimary.copy(alpha = 0.10f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isCurrentAyah) androidx.compose.foundation.BorderStroke(
                            1.dp, TealPrimary.copy(alpha = 0.45f)
                        ) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Ayah Number, Play and Share buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    // Bu ayetten itibaren okumayı başlat
                                    IconButton(
                                        onClick = {
                                            if (isCurrentAyah && audio.isPlaying) {
                                                audio.togglePlayback(surah, prefs.quranReciter)
                                            } else {
                                                audio.playFrom(surah, index, prefs.quranReciter)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp).testTag("ayah_audio_button_${surah.id}_${ayah.number}")
                                    ) {
                                        if (isCurrentAyah && audio.isBuffering) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = TealPrimary
                                            )
                                        } else {
                                            Icon(
                                                imageVector = if (isCurrentAyah && audio.isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                                contentDescription = if (isCurrentAyah && audio.isPlaying) "Duraklat" else "Bu Âyeti Dinle",
                                                tint = if (isCurrentAyah) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
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
