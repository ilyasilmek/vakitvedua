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

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Büyük ana ekran widget'ı: sıradaki vakit, geri sayım ve günün altı vakti. Küçüğü VakitWidgetSmall. */
public class VakitWidget extends AppWidgetProvider {
    public static final String ACTION_REFRESH = "com.stitchilyas.vakitvedua.WIDGET_REFRESH";
    private static final int[] TIME_IDS = {R.id.w_t0, R.id.w_t1, R.id.w_t2, R.id.w_t3, R.id.w_t4, R.id.w_t5};
    private static final int[] CELL_IDS = {R.id.w_c0, R.id.w_c1, R.id.w_c2, R.id.w_c3, R.id.w_c4, R.id.w_c5};

    protected boolean small() { return false; }

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) draw(ctx, mgr, id, small());
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
        int[] big = mgr.getAppWidgetIds(new ComponentName(ctx, VakitWidget.class));
        int[] sm = mgr.getAppWidgetIds(new ComponentName(ctx, VakitWidgetSmall.class));
        for (int id : big) draw(ctx, mgr, id, false);
        for (int id : sm) draw(ctx, mgr, id, true);
        if (big.length + sm.length > 0) scheduleNext(ctx);
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences("CapacitorStorage", Context.MODE_PRIVATE);
    }

    /** {lat, lng} ve yer adı; uygulama Preferences eklentisiyle "wloc" anahtarına yazar. */
    private static Object[] location(Context ctx) {
        double lat = 40.803, lng = 29.431; String place = "Gebze";
        try {
            String s = prefs(ctx).getString("wloc", null);
            if (s != null) {
                JSONObject o = new JSONObject(s);
                lat = o.getDouble("lat"); lng = o.getDouble("lng"); place = o.optString("place", place);
            }
        } catch (Exception ignored) { }
        return new Object[]{lat, lng, place};
    }

    /** Uygulamanın indirdiği Diyanet vakitleri ("wtimes", güne göre milisaniye); yoksa hesaplama. */
    static long[] times(Context ctx, Calendar day, double lat, double lng) {
        try {
            String s = prefs(ctx).getString("wtimes", null);
            if (s != null) {
                String k = String.format(Locale.US, "%04d-%02d-%02d", day.get(Calendar.YEAR),
                        day.get(Calendar.MONTH) + 1, day.get(Calendar.DAY_OF_MONTH));
                JSONArray a = new JSONObject(s).optJSONArray(k);
                if (a != null && a.length() == 6) {
                    long[] t = new long[6];
                    for (int i = 0; i < 6; i++) t[i] = a.getLong(i);
                    return t;
                }
            }
        } catch (Exception ignored) { }
        return VakitCalc.times(day, lat, lng);
    }

    /** Şu andan sonraki ilk vakit: {gün (0 bugün, 1 yarın), indeks, zaman} */
    private static long[] next(Context ctx, double lat, double lng, long[] today) {
        long now = System.currentTimeMillis();
        for (int i = 0; i < 6; i++) if (today[i] > now) return new long[]{0, i, today[i]};
        Calendar t = Calendar.getInstance(); t.add(Calendar.DAY_OF_MONTH, 1);
        long[] tm = times(ctx, t, lat, lng);
        return new long[]{1, 0, tm[0]};
    }

    private static void draw(Context ctx, AppWidgetManager mgr, int id, boolean small) {
        Object[] loc = location(ctx);
        double lat = (Double) loc[0], lng = (Double) loc[1];
        long[] t = times(ctx, Calendar.getInstance(), lat, lng);
        long[] nx = next(ctx, lat, lng, t);
        SimpleDateFormat hm = new SimpleDateFormat("HH:mm", Locale.getDefault());

        RemoteViews v = new RemoteViews(ctx.getPackageName(), small ? R.layout.widget_small : R.layout.widget_vakit);
        v.setTextViewText(R.id.w_place, (String) loc[2]);
        v.setTextViewText(R.id.w_next, VakitCalc.NAMES[(int) nx[1]] + " vaktine");
        long left = nx[2] - System.currentTimeMillis();
        v.setChronometer(R.id.w_cd, SystemClock.elapsedRealtime() + left, null, true);
        if (android.os.Build.VERSION.SDK_INT >= 24) v.setChronometerCountDown(R.id.w_cd, true);
        if (small) {
            v.setTextViewText(R.id.w_at, VakitCalc.NAMES[(int) nx[1]] + " " + hm.format(new Date(nx[2])));
        } else {
            int current = -1;
            for (int i = 0; i < 6; i++) if (t[i] <= System.currentTimeMillis()) current = i;
            for (int i = 0; i < 6; i++) {
                v.setTextViewText(TIME_IDS[i], hm.format(new Date(t[i])));
                v.setInt(CELL_IDS[i], "setBackgroundResource", i == current ? R.drawable.widget_cell_on : 0);
            }
        }
        Intent open = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        if (open != null) {
            v.setOnClickPendingIntent(R.id.w_root, PendingIntent.getActivity(ctx, 0, open,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        }
        mgr.updateAppWidget(id, v);
    }

    /** Bir sonraki vakit girdiğinde widget'ları yeniden çizmek için alarm kurar. */
    private static void scheduleNext(Context ctx) {
        Object[] loc = location(ctx);
        double lat = (Double) loc[0], lng = (Double) loc[1];
        long[] nx = next(ctx, lat, lng, times(ctx, Calendar.getInstance(), lat, lng));
        Intent i = new Intent(ctx, VakitWidget.class).setAction(ACTION_REFRESH);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 1, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.setAndAllowWhileIdle(AlarmManager.RTC, nx[2] + 1000, pi);
    }
}
