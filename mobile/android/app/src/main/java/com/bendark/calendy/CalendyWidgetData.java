package com.bendark.calendy;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Données du widget : l'appli (JavaScript) enregistre ici un résumé des 14 prochains jours.
 * Le widget filtre lui-même sur la date du jour, il reste donc juste après minuit même si l'appli n'a pas été ouverte.
 */
final class CalendyWidgetData {
    private static final String PREFS = "calendy_widget";
    private static final String KEY = "data";
    static final int DEFAULT_COLOR = 0xFF4F46E5;

    static final class Ev {
        String title = "";
        String kind = "event";
        long start, end;
        boolean allDay, important;
        int[] colors = new int[]{DEFAULT_COLOR};
    }

    private CalendyWidgetData() {}

    static void save(Context c, String json) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, json).apply();
    }

    static void clear(Context c) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply();
    }

    private static String raw(Context c) {
        SharedPreferences p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return p.getString(KEY, null);
    }

    static boolean hasData(Context c) {
        return raw(c) != null;
    }

    /** Fin de la période couverte par les données (ms), 0 si inconnue. */
    static long coveredUntil(Context c) {
        try {
            String r = raw(c);
            return r == null ? 0 : new JSONObject(r).optLong("to", 0);
        } catch (Exception e) {
            return 0;
        }
    }

    static long[] dayBounds(long nowMs) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(nowMs);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long a = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return new long[]{a, cal.getTimeInMillis()};
    }

    private static boolean wholeDay(Ev e, long a, long b) {
        return e.allDay || (e.start < a && e.end >= b);
    }

    /** Événements qui touchent le jour courant : journée entière d'abord, puis par heure. */
    static List<Ev> forToday(Context c, long nowMs) {
        final long[] day = dayBounds(nowMs);
        List<Ev> out = new ArrayList<>();
        String raw = raw(c);
        if (raw == null) return out;
        try {
            JSONArray arr = new JSONObject(raw).getJSONArray("ev");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Ev e = new Ev();
                e.start = o.getLong("s");
                e.end = o.getLong("e");
                if (e.start >= day[1] || (e.end <= day[0] && e.end != e.start)) continue;   // ne touche pas aujourd'hui
                e.title = o.optString("t", "");
                e.kind = o.optString("k", "event");
                e.allDay = o.optInt("a", 0) == 1;
                e.important = o.optInt("i", 0) == 1;
                JSONArray cs = o.optJSONArray("c");
                if (cs != null && cs.length() > 0) {
                    int n = Math.min(cs.length(), 3);
                    e.colors = new int[n];
                    for (int j = 0; j < n; j++) e.colors[j] = parseColor(cs.optString(j, ""));
                }
                out.add(e);
            }
        } catch (Exception ex) {
            return new ArrayList<>();
        }
        Collections.sort(out, new Comparator<Ev>() {
            @Override
            public int compare(Ev x, Ev y) {
                boolean wx = wholeDay(x, day[0], day[1]), wy = wholeDay(y, day[0], day[1]);
                if (wx != wy) return wx ? -1 : 1;
                int d = Long.compare(x.start, y.start);
                return d != 0 ? d : x.title.compareToIgnoreCase(y.title);
            }
        });
        return out;
    }

    private static int parseColor(String s) {
        try {
            return Color.parseColor(s);
        } catch (Exception e) {
            return DEFAULT_COLOR;
        }
    }

    static String timeLabel(Ev e, long nowMs) {
        long[] day = dayBounds(nowMs);
        if (wholeDay(e, day[0], day[1])) return "Journée";
        SimpleDateFormat f = new SimpleDateFormat("HH:mm", Locale.FRANCE);
        if (e.start < day[0]) return "→ " + f.format(new Date(e.end));
        return f.format(new Date(e.start));
    }

    static String dayLabel(long nowMs) {
        String s = new SimpleDateFormat("EEEE d MMMM", Locale.FRENCH).format(new Date(nowMs));
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static String countLabel(int n) {
        if (n == 0) return "Aucun événement";
        return n == 1 ? "1 événement" : n + " événements";
    }

    /** Icône du type d'événement (0 = aucune pour un événement simple). */
    static int iconFor(String kind) {
        if ("birthday".equals(kind)) return R.drawable.cw_ic_birthday;
        if ("medical".equals(kind)) return R.drawable.cw_ic_medical;
        if ("school_work".equals(kind)) return R.drawable.cw_ic_school_work;
        if ("outing".equals(kind)) return R.drawable.cw_ic_outing;
        if ("travel".equals(kind)) return R.drawable.cw_ic_travel;
        if ("task".equals(kind)) return R.drawable.cw_ic_task;
        return 0;
    }
}
