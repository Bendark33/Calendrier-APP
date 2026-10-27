package com.bendark.calendy;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

/** Fournit les lignes de la liste du widget. */
public class CalendyWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new Factory(getApplicationContext());
    }

    static class Factory implements RemoteViewsService.RemoteViewsFactory {
        private static final int[] BARS = {R.id.cw_bar1, R.id.cw_bar2, R.id.cw_bar3};
        private final Context ctx;
        private List<CalendyWidgetData.Ev> items = new ArrayList<>();
        private long now = System.currentTimeMillis();

        Factory(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        public void onCreate() {}

        @Override
        public void onDataSetChanged() {
            now = System.currentTimeMillis();
            items = CalendyWidgetData.forToday(ctx, now);
        }

        @Override
        public void onDestroy() {
            items.clear();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= items.size()) return null;
            CalendyWidgetData.Ev e = items.get(position);
            RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.cw_item);

            // Barre de couleur : une portion par personne concernée (3 au maximum)
            for (int i = 0; i < BARS.length; i++) {
                if (i < e.colors.length) {
                    v.setViewVisibility(BARS[i], View.VISIBLE);
                    v.setInt(BARS[i], "setBackgroundColor", e.colors[i]);
                } else {
                    v.setViewVisibility(BARS[i], View.GONE);
                }
            }
            v.setTextViewText(R.id.cw_time, CalendyWidgetData.timeLabel(e, now));
            v.setTextViewText(R.id.cw_title, e.title);

            int icon = CalendyWidgetData.iconFor(e.kind);
            if (icon != 0) {
                v.setImageViewResource(R.id.cw_kind, icon);
                v.setViewVisibility(R.id.cw_kind, View.VISIBLE);
            } else {
                v.setViewVisibility(R.id.cw_kind, View.GONE);
            }
            v.setViewVisibility(R.id.cw_star, e.important ? View.VISIBLE : View.GONE);
            v.setOnClickFillInIntent(R.id.cw_item, new Intent());
            return v;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
