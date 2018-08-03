package karsai.laszlo.bringcloser.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.activity.MainActivity;
import karsai.laszlo.bringcloser.activity.ReceivedDetailsActivity;
import timber.log.Timber;

/**
 * Implementation of App Widget functionality.
 */
public class ReceivedBringCloserItemsWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (action == null) {
            Timber.wtf("widget no action for update");
            return;
        }
        if (action.equals(ApplicationHelper.UPDATE_WIDGET_KEY)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv_widget);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.received_bring_closer_items_widget);
        Intent intent = new Intent(context, ReceivedBringCloserItemsWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.lv_widget, intent);
        views.setEmptyView(R.id.lv_widget, R.id.empty_view);

        Intent openMainActivityForUpNavigationIntent = new Intent(context, MainActivity.class);
        openMainActivityForUpNavigationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent openReceivedDetailsIntent = new Intent(context, ReceivedDetailsActivity.class);
        openReceivedDetailsIntent.setAction(Intent.ACTION_VIEW);
        PendingIntent openReceivedDetailsPendingIntent = PendingIntent.getActivities(
                context,
                0,
                new Intent[]{openMainActivityForUpNavigationIntent, openReceivedDetailsIntent},
                PendingIntent.FLAG_CANCEL_CURRENT
        );
        views.setPendingIntentTemplate(R.id.lv_widget, openReceivedDetailsPendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

