package com.stitchilyas.vakitvedua;

import java.util.Calendar;
import java.util.TimeZone;

/** Diyanet yöntemiyle namaz vakti hesabı (web sürümündeki prayerTimes ile aynı algoritma). */
public final class VakitCalc {
    public static final String[] NAMES = {"İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı"};
    private static final int[] TEMKIN = {0, -7, 5, 4, 7, 0};

    private static double sin(double d) { return Math.sin(Math.toRadians(d)); }
    private static double cos(double d) { return Math.cos(Math.toRadians(d)); }
    private static double tan(double d) { return Math.tan(Math.toRadians(d)); }
    private static double asin(double x) { return Math.toDegrees(Math.asin(x)); }
    private static double acos(double x) { return Math.toDegrees(Math.acos(x)); }
    private static double atan2(double y, double x) { return Math.toDegrees(Math.atan2(y, x)); }
    private static double acot(double x) { return Math.toDegrees(Math.atan(1 / x)); }
    private static double fix(double a, double b) { a = a - b * Math.floor(a / b); return a < 0 ? a + b : a; }

    private static double julian(int y, int m, int d) {
        if (m <= 2) { y--; m += 12; }
        double A = Math.floor(y / 100.0), B = 2 - A + Math.floor(A / 4);
        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + d + B - 1524.5;
    }

    /** {decl, eqt} */
    private static double[] sunPos(double jd) {
        double D = jd - 2451545, g = fix(357.529 + .98560028 * D, 360), q = fix(280.459 + .98564736 * D, 360);
        double L = fix(q + 1.915 * sin(g) + .02 * sin(2 * g), 360), e = 23.439 - .00000036 * D;
        double RA = fix(atan2(cos(e) * sin(L), cos(L)) / 15, 24);
        return new double[]{asin(sin(e) * sin(L)), q / 15 - RA};
    }

    /** Verilen günün altı vaktini epoch milisaniye olarak döndürür. */
    public static long[] times(Calendar day, double lat, double lng) {
        Calendar mid = (Calendar) day.clone();
        mid.set(Calendar.HOUR_OF_DAY, 0); mid.set(Calendar.MINUTE, 0); mid.set(Calendar.SECOND, 0); mid.set(Calendar.MILLISECOND, 0);
        double tz = TimeZone.getDefault().getOffset(mid.getTimeInMillis() + 12 * 3600000L) / 3600000.0;
        final double jd0 = julian(mid.get(Calendar.YEAR), mid.get(Calendar.MONTH) + 1, mid.get(Calendar.DAY_OF_MONTH)) - lng / (15 * 24);
        double[] h = {5, 6, 12, 13, 18, 18};
        for (int it = 0; it < 2; it++) {
            double[] n = new double[6];
            n[0] = angT(jd0, lat, 18, h[0] / 24, true);
            n[1] = angT(jd0, lat, .833, h[1] / 24, true);
            n[2] = midDay(jd0, h[2] / 24);
            double dc = sunPos(jd0 + h[3] / 24)[0];
            n[3] = angT(jd0, lat, -acot(1 + tan(Math.abs(lat - dc))), h[3] / 24, false);
            n[4] = angT(jd0, lat, .833, h[4] / 24, false);
            n[5] = angT(jd0, lat, 17, h[5] / 24, false);
            h = n;
        }
        long[] out = new long[6];
        for (int i = 0; i < 6; i++) {
            double hr = h[i] + tz - lng / 15 + TEMKIN[i] / 60.0;
            out[i] = mid.getTimeInMillis() + Math.round(hr * 60) * 60000L;
        }
        return out;
    }

    private static double midDay(double jd0, double t) { return fix(12 - sunPos(jd0 + t)[1], 24); }

    private static double angT(double jd0, double lat, double ang, double t, boolean ccw) {
        double dc = sunPos(jd0 + t)[0], n = midDay(jd0, t);
        double T = acos((-sin(ang) - sin(dc) * sin(lat)) / (cos(dc) * cos(lat))) / 15;
        return n + (ccw ? -T : T);
    }
}
