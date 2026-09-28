package com.stitchilyas.vakitvedua.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.stitchilyas.vakitvedua.data.local.AppPreferences
import java.util.Date

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == ACTION_MARK_PRAYER_DONE) {
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
    }

    companion object {
        const val ACTION_MARK_PRAYER_DONE = "com.stitchilyas.vakitvedua.ACTION_MARK_PRAYER_DONE"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
