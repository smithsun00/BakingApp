package com.example.bakingapp;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.example.bakingapp.data.RecipeData;

import java.util.ArrayList;

public class BakingAppWidgetService extends IntentService {

    private static final String ACTION_UPDATE_WIDGET = "update-widget";

    private SavedRecipeDatabase mDatabase;
    private Context mContext;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public BakingAppWidgetService() {
        super(BakingAppWidgetService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mContext = getApplicationContext();

        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_UPDATE_WIDGET.equals(action)) {
                updateWidget();
            }
        }
    }

    public static void startActionUpdateBakingWidget(final Context context){
        Intent intent = new Intent(context, BakingAppWidgetService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        context.startService(intent);
    }

    private void updateWidget(){
        final SavedRecipeDatabase database = SavedRecipeDatabase.getInstance(mContext);
        AppExecutors executors = AppExecutors.getInstance();

        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<RecipeData> markedRecipes = (ArrayList<RecipeData>)database.savedRecipesDao().loadMarkedInWidgetRecipes();

                // Get the first one which is marked with 'in_widget'
                // There supposed to be only one anyway, this is just a fail safe.
                if(markedRecipes != null && markedRecipes.size() > 0){
                    RecipeData recipe = markedRecipes.get(0);

                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(mContext, BakingAppWidget.class));

                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.ingredient_list_lv);

                    BakingAppWidget.updateBakingWidgets(mContext, appWidgetManager, appWidgetIds, recipe.getName());
                }
            }
        });
    }
}
