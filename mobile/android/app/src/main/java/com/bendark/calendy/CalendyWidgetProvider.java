package com.bendark.calendy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import java.util.Calendar;

/** Widget « Aujourd'hui » de l'écran d'accueil. */
public class CalendyWidgetProvider extends AppWidgetProvider {
    static final String ACTION_REFRESH = "com.bendark.calendy.WIDGET_REFRESH";

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        refresh(ctx, mgr, ids);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        super.onReceive(ctx, intent);
        if (intent != null && ACTION_REFRESH.equals(intent.getAction())) refreshAll(ctx);
    }

    @Override
    public void onEnabled(Context ctx) {
        scheduleMidnight(ctx);
    }

    @Override
    public void onDisabled(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(refreshIntent(ctx));
    }

    /** Appelée par l'appli (plugin) et par l'alarme de minuit. */
    static void refreshAll(Context ctx) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, CalendyWidgetProvider.class));
        if (ids == null || ids.length == 0) return;
        refresh(ctx, mgr, ids);
    }

    private static void refresh(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) mgr.updateAppWidget(id, build(ctx, id));
        mgr.notifyAppWidgetViewDataChanged(ids, R.id.cw_list);
        scheduleMidnight(ctx);
    }

    private static RemoteViews build(Context ctx, int widgetId) {
        long now = System.currentTimeMillis();
        RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.cw_widget);

        int count = CalendyWidgetData.forToday(ctx, now).size();
        v.setTextViewText(R.id.cw_date, CalendyWidgetData.dayLabel(now));
        v.setTextViewText(R.id.cw_count, CalendyWidgetData.countLabel(count));

        String empty;
        if (!CalendyWidgetData.hasData(ctx)) empty = ctx.getString(R.string.cw_empty_nodata);
        else if (CalendyWidgetData.coveredUntil(ctx) > 0 && now >= CalendyWidgetData.coveredUntil(ctx)) empty = ctx.getString(R.string.cw_empty_stale);
        else empty = ctx.getString(R.string.cw_empty_today);
        v.setTextViewText(R.id.cw_empty, empty);

        // Liste des événements
        Intent svc = new Intent(ctx, CalendyWidgetService.class);
        svc.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        svc.setData(Uri.parse(svc.toUri(Intent.URI_INTENT_SCHEME)));
        v.setRemoteAdapter(R.id.cw_list, svc);
        v.setEmptyView(R.id.cw_list, R.id.cw_empty);

        // Toucher l'en-tête, un événement ou le texte vide ouvre l'appli
        PendingIntent open = PendingIntent.getActivity(ctx, 0, launchIntent(ctx, null),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.cw_info, open);
        v.setOnClickPendingIntent(R.id.cw_empty, open);
        PendingIntent template = PendingIntent.getActivity(ctx, 2, launchIntent(ctx, null),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);   // modèle de clic de la liste : doit être modifiable
        v.setPendingIntentTemplate(R.id.cw_list, template);

        // Bouton « + » : ouvre l'appli sur la création d'un événement
        PendingIntent add = PendingIntent.getActivity(ctx, 1, launchIntent(ctx, Uri.parse("com.bendark.calendy://new")),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.cw_plus, add);
        return v;
    }

    private static Intent launchIntent(Context ctx, Uri data) {
        Intent i = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        if (i == null) i = new Intent();
        if (data != null) {
            i.setAction(Intent.ACTION_VIEW);
            i.setData(data);
        }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    private static PendingIntent refreshIntent(Context ctx) {
        Intent i = new Intent(ctx, CalendyWidgetProvider.class).setAction(ACTION_REFRESH);
        return PendingIntent.getBroadcast(ctx, 7, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /** Rafraîchit le widget peu après minuit (alarme approximative : aucune permission spéciale nécessaire). */
    private static void scheduleMidnight(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 1);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), refreshIntent(ctx));
    }
}
