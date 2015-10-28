package org.dayup.inotes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class InotesSingleNotesWidgetProvider2x2 extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        final int N=appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            Intent intent = new Intent();
            intent.setClass(context, INotesDetailActivity.class);
            PendingIntent p = PendingIntent.getActivity(context, 0, intent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.inotes_single_note_widget_2x2);
            views.setOnClickPendingIntent(R.id.single_widget_body, p);
            appWidgetManager.updateAppWidget(appWidgetIds, views);
        }
    }

}
