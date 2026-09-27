package com.stitchilyas.vakitvedua.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitchilyas.vakitvedua.ads.AdManager
import com.stitchilyas.vakitvedua.ui.theme.BrassGold
import com.stitchilyas.vakitvedua.ui.theme.TealPrimary

fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

@Composable
fun DestekOlDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isAdLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.VolunteerActivism,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Geliştiriciye Destek Ol", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Vakit ve Dua tamamen ücretsizdir ve kullanıcı verilerini toplamaz. Uygulamanın gelişmesine aşağıdaki yollarla destek olabilirsiniz:",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Rewarded Ad Action
                DestekOptionRow(
                    icon = if (isAdLoading) Icons.Default.HourglassTop else Icons.Default.PlayCircle,
                    title = if (isAdLoading) "Reklam Yükleniyor..." else "Destek Ol & Reklam İzle",
                    subtitle = "Geliştiriciye katkı sağlamak için kısa bir video izleyin",
                    iconColor = TealPrimary,
                    onClick = {
                        val activity = context.findActivity()
                        if (activity != null) {
                            isAdLoading = true
                            Toast.makeText(context, "Reklam hazırlanıyor...", Toast.LENGTH_SHORT).show()
                            AdManager.showRewardedAd(
                                activity = activity,
                                onRewardEarned = {
                                    isAdLoading = false
                                    Toast.makeText(
                                        context,
                                        "Desteğiniz ve dualarınız için Allah razı olsun! Çok teşekkür ederiz.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onDismiss()
                                },
                                onAdClosed = {
                                    isAdLoading = false
                                },
                                onAdFailed = { errorMsg ->
                                    isAdLoading = false
                                    Toast.makeText(
                                        context,
                                        "Şu an görüntülenecek reklam bulunamadı. İlginiz için teşekkür ederiz!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Reklam başlatılamadı.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                DestekOptionRow(
                    icon = Icons.Default.Share,
                    title = "Uygulamayı Paylaş",
                    subtitle = "Yakınlarınıza tavsiye edin, hayra vesile olun",
                    iconColor = BrassGold,
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Namaz vakitleri, kıble, zikirmatik ve Kur'an-ı Kerim için 'Vakit ve Dua' uygulamasını tavsiye ederim: https://play.google.com/store/apps/details?id=com.stitchilyas.vakitvedua"
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Uygulamayı Paylaş"))
                        onDismiss()
                    }
                )

                DestekOptionRow(
                    icon = Icons.Default.Star,
                    title = "5 Yıldız ile Değerlendir",
                    subtitle = "Google Play'de olumlu yorum yaparak destekleyin",
                    iconColor = Color(0xFFF39C12),
                    onClick = {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=com.stitchilyas.vakitvedua")
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=com.stitchilyas.vakitvedua")
                            )
                            context.startActivity(intent)
                        }
                        onDismiss()
                    }
                )

                DestekOptionRow(
                    icon = Icons.Default.Mail,
                    title = "Geri Bildirim & İletişim",
                    subtitle = "Görüş, öneri ve taleplerinizi bize iletin",
                    iconColor = TealPrimary,
                    onClick = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:ilyasilmk@gmail.com?subject=Vakit%20ve%20Dua%20Geri%20Bildirim")
                        }
                        try {
                            context.startActivity(emailIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "E-posta uygulaması bulunamadı.", Toast.LENGTH_SHORT).show()
                        }
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

@Composable
fun DestekOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
