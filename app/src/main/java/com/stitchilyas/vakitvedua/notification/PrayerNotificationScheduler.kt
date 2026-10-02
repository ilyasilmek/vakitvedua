package com.stitchilyas.vakitvedua.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.stitchilyas.vakitvedua.MainActivity
import com.stitchilyas.vakitvedua.R
import com.stitchilyas.vakitvedua.calculation.VakitCalc
import com.stitchilyas.vakitvedua.data.local.AppPreferences
import java.util.Calendar

/**
 * Namaz vakti bildirimlerini planlar ve gösterir.
 * AlarmManager ile "sonraki vakte" tam zamanlı alarm kurar; vakit bildirimi
 * gösterildikten sonra otomatik olarak bir sonraki vakiye geçer.
 */
object PrayerNotificationScheduler {

    const val CHANNEL_EZAN = "ezan_channel"
    const val CHANNEL_KERAHAT = "kerahat_channel"

    private const val ACTION_PRAYER_TIME = "com.stitchilyas.vakitvedua.ACTION_PRAYER_TIME"
    private const val ACTION_KERAHAT_WARNING = "com.stitchilyas.vakitvedua.ACTION_KERAHAT_WARNING"
    private const val EXTRA_PRAYER_INDEX = "extra_prayer_index"
    private const val EXTRA_KERAHAT_NAME = "extra_kerahat_name"

    private fun alarmManager(context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun ensureChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val ezan = NotificationChannel(
            CHANNEL_EZAN,
            "Ezan ve Namaz Vakti",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Namaz vakti bildirimleri ve ezan sesi"
            enableVibration(true)
        }
        nm.createNotificationChannel(ezan)

        val kerahat = NotificationChannel(
            CHANNEL_KERAHAT,
            "Kerahat Uyarısı",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Kerahat vakti yaklaşınca uyarı"
        }
        nm.createNotificationChannel(kerahat)
    }

    fun hasNotificationPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Bugünün ve yarının vakitlerini hesaplayıp sıradaki vaki için alarm kurar.
     * Kerahat uyarısı açıksa bir sonraki kerahat dönemine de alarm kurar.
     */
    fun scheduleNext(context: Context) {
        val prefs = AppPreferences(context)

        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        val timesToday = VakitCalc.times(today, prefs.latitude, prefs.longitude)
        val timesTomorrow = VakitCalc.times(tomorrow, prefs.latitude, prefs.longitude)
        val info = VakitCalc.getCurrentInfo(timesToday, timesTomorrow)

        val now = System.currentTimeMillis()
        val nextIndex = info.nextIndex
        val nextTime = info.nextTime
        if (nextTime <= now) return

        setExactAlarm(context, prayerTimePendingIntent(context, nextIndex), nextTime)

        if (prefs.kerahatWarningEnabled) {
            val nextKerahat = VakitCalc.getKerahatPeriods(timesToday)
                .filter { it.start > now }
                .minByOrNull { it.start }
            if (nextKerahat != null) {
                setExactAlarm(
                    context,
                    kerahatPendingIntent(context, nextKerahat.name),
                    nextKerahat.start
                )
            }
        }
    }

    private fun setExactAlarm(context: Context, pi: PendingIntent, atMillis: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi)
        } catch (_: SecurityException) {
            // SCHEDULE_EXACT_ALARM izni yoksa tam olmayan alarm ile devam
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi)
        }
    }

    private fun prayerTimePendingIntent(context: Context, prayerIndex: Int): PendingIntent {
        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            action = ACTION_PRAYER_TIME
            putExtra(EXTRA_PRAYER_INDEX, prayerIndex)
        }
        return PendingIntent.getBroadcast(
            context, 100 + prayerIndex, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun kerahatPendingIntent(context: Context, name: String): PendingIntent {
        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            action = ACTION_KERAHAT_WARNING
            putExtra(EXTRA_KERAHAT_NAME, name)
        }
        return PendingIntent.getBroadcast(
            context, 200, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Vakti gelen namaz için bildirim gösterir, ezan sesi çalar ve sonraki vaki planlar. */
    fun showPrayerNotification(context: Context, prayerIndex: Int) {
        val name = VakitCalc.NAMES.getOrElse(prayerIndex) { "Vakit" }

        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = PendingIntent.getBroadcast(
            context, 300 + prayerIndex,
            Intent(context, PrayerNotificationReceiver::class.java).apply {
                action = PrayerNotificationReceiver.ACTION_MARK_PRAYER_DONE
                putExtra(PrayerNotificationReceiver.EXTRA_PRAYER_NAME, name)
                putExtra(PrayerNotificationReceiver.EXTRA_NOTIFICATION_ID, 1000 + prayerIndex)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_EZAN)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.prayer_time_title, name))
            .setContentText(context.getString(R.string.prayer_time_text, name))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .addAction(0, context.getString(R.string.prayer_done_action), doneIntent)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1000 + prayerIndex, notification)

        if (AppPreferences(context).ezanSoundEnabled) {
            playEzan(context)
        }

        scheduleNext(context)
    }

    private fun playEzan(context: Context) {
        try {
            val mp = MediaPlayer.create(context, R.raw.ezan)
            mp?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
                setOnCompletionListener { it.release() }
                start()
            }
        } catch (_: Exception) {
            // Ses çalınamadıysa bildirim yine gösterilmiş olur
        }
    }

    fun showKerahatNotification(context: Context, kerahatName: String) {
        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_KERAHAT)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.kerahat_title))
            .setContentText(context.getString(R.string.kerahat_text, kerahatName))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(2000, notification)
    }

    /** Bildirim izni verildikten sonra veya uygulama açıldığında çağrılır. */
    fun reschedule(context: Context) {
        if (!hasNotificationPermission(context)) return
        if (!AppPreferences(context).ezanSoundEnabled) return
        ensureChannels(context)
        scheduleNext(context)
    }
}
