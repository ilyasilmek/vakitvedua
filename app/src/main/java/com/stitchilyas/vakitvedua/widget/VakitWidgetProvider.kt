package com.stitchilyas.vakitvedua.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.stitchilyas.vakitvedua.MainActivity
import com.stitchilyas.vakitvedua.R
import com.stitchilyas.vakitvedua.calculation.VakitCalc
import com.stitchilyas.vakitvedua.data.local.AppPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VakitWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = AppPreferences(context)
            val now = System.currentTimeMillis()
            val calToday = Calendar.getInstance()
            val calTomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }

            val timesToday = VakitCalc.times(calToday, prefs.latitude, prefs.longitude)
            val timesTomorrow = VakitCalc.times(calTomorrow, prefs.latitude, prefs.longitude)
            val info = VakitCalc.getCurrentInfo(timesToday, timesTomorrow, now)

            val locationText = if (prefs.districtName.isNotEmpty()) {
                "${prefs.cityName}, ${prefs.districtName}"
            } else {
                prefs.cityName
            }

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val nextTimeStr = timeFormat.format(info.nextTime)

            val remainingMinutes = info.remainingMillis / (1000 * 60)
            val hours = remainingMinutes / 60
            val mins = remainingMinutes % 60
            val remainingStr = if (hours > 0) "${hours} sa ${mins} dk" else "${mins} dk"

            // Create Intent to launch MainActivity when clicking widget
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
            val isSmall = widgetInfo?.initialLayout == R.layout.widget_vakit_small

            if (isSmall) {
                val views = RemoteViews(context.packageName, R.layout.widget_vakit_small)
                views.setTextViewText(R.id.tv_widget_location, locationText)
                views.setTextViewText(R.id.tv_widget_next_prayer_title, "Sıradaki: ${info.nextName}")
                views.setTextViewText(R.id.tv_widget_next_prayer_time, nextTimeStr)
                views.setTextViewText(R.id.tv_widget_remaining_time, "$remainingStr kaldı")
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } else {
                val views = RemoteViews(context.packageName, R.layout.widget_vakit_large)
                val dateFormat = SimpleDateFormat("d MMMM EEEE", Locale("tr", "TR"))
                val dateStr = dateFormat.format(calToday.time)

                views.setTextViewText(R.id.tv_large_location, locationText)
                views.setTextViewText(R.id.tv_large_date, dateStr)
                views.setTextViewText(R.id.tv_large_status, "${info.nextName} Vaktine $remainingStr kaldı")

                views.setTextViewText(R.id.tv_imsak, timeFormat.format(timesToday[0]))
                views.setTextViewText(R.id.tv_gunes, timeFormat.format(timesToday[1]))
                views.setTextViewText(R.id.tv_ogle, timeFormat.format(timesToday[2]))
                views.setTextViewText(R.id.tv_ikindi, timeFormat.format(timesToday[3]))
                views.setTextViewText(R.id.tv_aksam, timeFormat.format(timesToday[4]))
                views.setTextViewText(R.id.tv_yatsi, timeFormat.format(timesToday[5]))

                views.setOnClickPendingIntent(R.id.widget_large_root, pendingIntent)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
