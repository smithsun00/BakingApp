package com.example.bakingapp;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import java.util.Random;


/**
 * Implementation of App Widget functionality.
 */
public class BakingAppWidget extends AppWidgetProvider {

    public static final String EXTRA_RECIPE_NAME = "recipe-name";

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String recipeName) {

        RemoteViews views = getIngredientArrayListView(context, recipeName, appWidgetId);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static RemoteViews getIngredientArrayListView(Context context, String recipeName, int appWidgetId){
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_array_list_view);

        Intent intent = new Intent(context, ArrayWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setEmptyView(R.id.ingredient_list_lv, R.id.empty_view);
        views.setRemoteAdapter(R.id.ingredient_list_lv, intent);

        views.setTextViewText(R.id.widget_recipe_name_tv, recipeName);

        Intent openBakingAppIntent = new Intent(context, MainActivity.class);
        openBakingAppIntent.putExtra(EXTRA_RECIPE_NAME, recipeName);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openBakingAppIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_array_list_container, pendingIntent);
        views.setPendingIntentTemplate(R.id.ingredient_list_lv, pendingIntent);

        return views;
    }

    @Override
    public void onEnabled(Context context) {
        BakingAppWidgetService.startActionUpdateBakingWidget(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static void updateBakingWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, String recipeName){
        for(int appWidgetId : appWidgetIds){
            updateAppWidget(context, appWidgetManager, appWidgetId, recipeName);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        BakingAppWidgetService.startActionUpdateBakingWidget(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }
}

