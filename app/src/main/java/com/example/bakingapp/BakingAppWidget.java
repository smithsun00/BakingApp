package com.example.bakingapp;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.bakingapp.data.RecipeData;
import com.example.bakingapp.utils.RecipeDataUtils;

/**
 * Implementation of App Widget functionality.
 */
public class BakingAppWidget extends AppWidgetProvider {

    public static final String EXTRA_RECIPE = "recipe_id";

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, RecipeData recipe) {
        RemoteViews views = getRemoteView(context, recipe);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        // This will make sure widget is clickable the first time it is created, even if no recipe data is populated in it.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, BakingAppWidget.class));

        BakingAppWidget.updateBakingWidgets(context, appWidgetManager, appWidgetIds, null);
    }

    private static RemoteViews getRemoteView(Context context , RecipeData recipe){
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.baking_app_widget);
        Log.i("____w","Setting click for widget");
        Intent openBakingAppIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openBakingAppIntent, 0);
        views.setOnClickPendingIntent(R.id.baking_widget_container, pendingIntent);

        if(recipe != null) {
            openBakingAppIntent.putExtra(EXTRA_RECIPE, recipe);

            views.setTextViewText(R.id.widget_recipe_name_tv, recipe.getName());
            String ingredientString = RecipeDataUtils.getInstance().getRecipeIngredientsAsString(context, recipe);
            views.setTextViewText(R.id.widget_ingredients_tv, ingredientString);
        }

        return views;
    }

    public static void updateBakingWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, RecipeData recipeData){
        for(int appWidgetId : appWidgetIds){
            updateAppWidget(context, appWidgetManager, appWidgetId, recipeData);
        }
    }
}

