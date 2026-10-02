package com.stitchilyas.vakitvedua.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.stitchilyas.vakitvedua.data.local.AppPreferences
import java.util.Date

/**
 * AlarmManager'dan gelen vakit/kerahat alarmlarını ve bildirim aksiyonlarını karşılar.
 */
class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        when (action) {
            ACTION_MARK_PRAYER_DONE -> handleMarkDone(context, intent)
            ACTION_PRAYER_TIME -> {
                val prayerIndex = intent.getIntExtra(EXTRA_PRAYER_INDEX, -1)
                if (prayerIndex != -1) {
                    PrayerNotificationScheduler.showPrayerNotification(context, prayerIndex)
                }
            }
            ACTION_KERAHAT_WARNING -> {
                val name = intent.getStringExtra(EXTRA_KERAHAT_NAME)
                if (!name.isNullOrBlank()) {
                    PrayerNotificationScheduler.showKerahatNotification(context, name)
                }
            }
        }
    }

    private fun handleMarkDone(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        val prefs = AppPreferences(context)
        prefs.togglePrayerChecked(Date(), prayerName)

        if (notificationId != -1) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(notificationId)
        }

        Toast.makeText(
            context,
            "Allah kabul etsin! $prayerName namazı kılındı olarak işaretlendi.",
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        const val ACTION_MARK_PRAYER_DONE = "com.stitchilyas.vakitvedua.ACTION_MARK_PRAYER_DONE"
        const val ACTION_PRAYER_TIME = "com.stitchilyas.vakitvedua.ACTION_PRAYER_TIME"
        const val ACTION_KERAHAT_WARNING = "com.stitchilyas.vakitvedua.ACTION_KERAHAT_WARNING"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_PRAYER_INDEX = "extra_prayer_index"
        const val EXTRA_KERAHAT_NAME = "extra_kerahat_name"
    }
}
