package com.stitchilyas.vakitvedua;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.widget.RemoteViews;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Ana ekran widget'ı: sıradaki vakit, geri sayım ve günün altı vakti. */
public class VakitWidget extends AppWidgetProvider {
    public static final String ACTION_REFRESH = "com.stitchilyas.vakitvedua.WIDGET_REFRESH";
    private static final int[] TIME_IDS = {R.id.w_t0, R.id.w_t1, R.id.w_t2, R.id.w_t3, R.id.w_t4, R.id.w_t5};
    private static final int[] CELL_IDS = {R.id.w_c0, R.id.w_c1, R.id.w_c2, R.id.w_c3, R.id.w_c4, R.id.w_c5};

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) draw(ctx, mgr, id);
        scheduleNext(ctx);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        super.onReceive(ctx, intent);
        String a = intent.getAction();
        if (ACTION_REFRESH.equals(a) || Intent.ACTION_BOOT_COMPLETED.equals(a)
                || Intent.ACTION_TIME_CHANGED.equals(a) || Intent.ACTION_TIMEZONE_CHANGED.equals(a)) {
            updateAll(ctx);
        }
    }

    public static void updateAll(Context ctx) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, VakitWidget.class));
        for (int id : ids) draw(ctx, mgr, id);
        if (ids.length > 0) scheduleNext(ctx);
    }

    /** {lat, lng} ve yer adı; uygulama Preferences eklentisiyle "wloc" anahtarına yazar. */
    private static Object[] location(Context ctx) {
        double lat = 40.803, lng = 29.431; String place = "Gebze";
        try {
            SharedPreferences p = ctx.getSharedPreferences("CapacitorStorage", Context.MODE_PRIVATE);
            String s = p.getString("wloc", null);
            if (s != null) {
                JSONObject o = new JSONObject(s);
                lat = o.getDouble("lat"); lng = o.getDouble("lng"); place = o.optString("place", place);
            }
        } catch (Exception ignored) { }
        return new Object[]{lat, lng, place};
    }

    /** Şu andan sonraki ilk vakit: {gün (0 bugün, 1 yarın), indeks, zaman} */
    private static long[] next(double lat, double lng, long[] today) {
        long now = System.currentTimeMillis();
        for (int i = 0; i < 6; i++) if (today[i] > now) return new long[]{0, i, today[i]};
        Calendar t = Calendar.getInstance(); t.add(Calendar.DAY_OF_MONTH, 1);
        long[] tm = VakitCalc.times(t, lat, lng);
        return new long[]{1, 0, tm[0]};
    }

    private static void draw(Context ctx, AppWidgetManager mgr, int id) {
        Object[] loc = location(ctx);
        double lat = (Double) loc[0], lng = (Double) loc[1];
        long[] t = VakitCalc.times(Calendar.getInstance(), lat, lng);
        long[] nx = next(lat, lng, t);
        SimpleDateFormat hm = new SimpleDateFormat("HH:mm", Locale.getDefault());

        RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.widget_vakit);
        v.setTextViewText(R.id.w_place, (String) loc[2]);
        v.setTextViewText(R.id.w_next, VakitCalc.NAMES[(int) nx[1]] + " vaktine");
        long left = nx[2] - System.currentTimeMillis();
        v.setChronometer(R.id.w_cd, SystemClock.elapsedRealtime() + left, null, true);
        if (android.os.Build.VERSION.SDK_INT >= 24) v.setChronometerCountDown(R.id.w_cd, true);
        int current = -1;
        for (int i = 0; i < 6; i++) if (t[i] <= System.currentTimeMillis()) current = i;
        for (int i = 0; i < 6; i++) {
            v.setTextViewText(TIME_IDS[i], hm.format(new Date(t[i])));
            v.setInt(CELL_IDS[i], "setBackgroundResource", i == current ? R.drawable.widget_cell_on : 0);
        }
        Intent open = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        if (open != null) {
            v.setOnClickPendingIntent(R.id.w_root, PendingIntent.getActivity(ctx, 0, open,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        }
        mgr.updateAppWidget(id, v);
    }

    /** Bir sonraki vakit girdiğinde widget'ı yeniden çizmek için alarm kurar. */
    private static void scheduleNext(Context ctx) {
        Object[] loc = location(ctx);
        double lat = (Double) loc[0], lng = (Double) loc[1];
        long[] nx = next(lat, lng, VakitCalc.times(Calendar.getInstance(), lat, lng));
        Intent i = new Intent(ctx, VakitWidget.class).setAction(ACTION_REFRESH);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 1, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.setAndAllowWhileIdle(AlarmManager.RTC, nx[2] + 1000, pi);
    }
}
