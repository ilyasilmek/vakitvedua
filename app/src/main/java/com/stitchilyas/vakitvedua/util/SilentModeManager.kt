package com.stitchilyas.vakitvedua.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import com.stitchilyas.vakitvedua.notification.PrayerNotificationReceiver
import java.util.Calendar

/**
 * "Namazda Otomatik Sessiz Mod" ayarının işlevselliğini sağlar.
 *
 * Vakit bildirimi gösterildiğinde [enterSilent] çağrılır: mevcut ses modu
 * kaydedilir, cihaz titreşime alınır ve namaz süresi (varsayılan 10 dakika)
 * sonunda [restore] alarmı kurulur. Süre dolunca veya kullanıcı elle
 * değiştirirse orijinal mod geri yüklenir.
 */
object SilentModeManager {
    private const val TAG = "SilentModeManager"
    private const val SILENT_DURATION_MINUTES = 10
    private const val EXTRA_RESTORE_RINGER = "extra_restore_ringer"

    private fun audioManager(context: Context): AudioManager? =
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private fun restorePendingIntent(context: Context, ringerMode: Int): PendingIntent {
        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            action = PrayerNotificationReceiver.ACTION_RESTORE_RINGER
            putExtra(EXTRA_RESTORE_RINGER, ringerMode)
        }
        return PendingIntent.getBroadcast(
            context, 500, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Vaktin bildirimi gösterildiğinde sessiz moda geçer (ilgili ayar açıksa). */
    fun onPrayerTime(context: Context) {
        val prefs = com.stitchilyas.vakitvedua.data.local.AppPreferences(context)
        if (!prefs.autoSilentMode) return

        val am = audioManager(context) ?: return
        val current = try {
            am.ringerMode
        } catch (e: Exception) {
            Log.w(TAG, "Ringer modu okunamadı: ${e.message}")
            return
        }

        // Zaten sessizse veya DND aktifse dokunma
        if (current == AudioManager.RINGER_MODE_SILENT ||
            current == AudioManager.RINGER_MODE_VIBRATE
        ) return

        // Do Not Disturb erişimi yoksa yalnızca ringer değişir; izin varsa önce dener
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
        val dndAllowed = try {
            notificationManager?.isNotificationPolicyAccessGranted == true
        } catch (_: Exception) {
            false
        }
        if (!dndAllowed) {
            Log.i(TAG, "DND erişimi yok; yalnızca ringer titreşime alınıyor.")
        }

        try {
            am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            Log.d(TAG, "Ringer $current -> VIBRATE")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val restoreAt = Calendar.getInstance().apply {
                add(Calendar.MINUTE, SILENT_DURATION_MINUTES)
            }.timeInMillis
            val pi = restorePendingIntent(context, current)
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, restoreAt, pi)
            } catch (_: SecurityException) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, restoreAt, pi)
            }
        } catch (e: Exception) {
            // Bazı cihazlarda DND aktifken ringer değiştirilemez
            Log.w(TAG, "Sessiz moda geçilemedi: ${e.message}")
        }
    }

    /** AlarmManager'dan gelen geri yükleme isteğini işler. */
    fun restore(context: Context, savedRingerMode: Int) {
        val am = audioManager(context) ?: return
        val current = try {
            am.ringerMode
        } catch (_: Exception) {
            return
        }
        // Kullanıcı arada elle değiştirdiyse üzerine yazma
        if (current == AudioManager.RINGER_MODE_SILENT ||
            current == AudioManager.RINGER_MODE_VIBRATE
        ) {
            try {
                am.ringerMode = savedRingerMode
                Log.d(TAG, "Ringer geri yüklendi: $savedRingerMode")
            } catch (e: Exception) {
                Log.w(TAG, "Ringer geri yüklenemedi: ${e.message}")
            }
        }
    }

    /** Ayar kapatıldığında bekleyen geri yükleme alarmlarını iptal eder. */
    fun cancelPendingRestore(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = restorePendingIntent(context, AudioManager.RINGER_MODE_NORMAL)
        alarmManager.cancel(pi)
    }
}
