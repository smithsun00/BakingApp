package com.example.bakingapp;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.bakingapp.data.IngredientData;
import com.example.bakingapp.data.RecipeData;

import java.util.ArrayList;

public class ArrayWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ArrayRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ArrayRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    Context mContext;
    SavedRecipeDatabase mDatabase;
    ArrayList<IngredientData> mIngredients;

    public ArrayRemoteViewsFactory( Context applicationContext, Intent intent){
        mContext = applicationContext;
        if(mIngredients == null) {
            mIngredients = new ArrayList<>();
        }

        mDatabase = SavedRecipeDatabase.getInstance(mContext);
        AppExecutors executors = AppExecutors.getInstance();

        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<RecipeData> markedRecipes = (ArrayList<RecipeData>)mDatabase.savedRecipesDao().loadMarkedInWidgetRecipes();

                // Get the first one which is marked with 'in_widget'
                // There supposed to be only one anyway, this is just a fail safe.
                if(markedRecipes != null && markedRecipes.size() > 0){
                    RecipeData recipe = markedRecipes.get(0);
                    mIngredients = recipe.getIngredients();
                }
            }
        });
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        AppExecutors executors = AppExecutors.getInstance();

        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<RecipeData> markedRecipes = (ArrayList<RecipeData>)mDatabase.savedRecipesDao().loadMarkedInWidgetRecipes();

                // Get the first one which is marked with 'in_widget'
                // There supposed to be only one anyway, this is just a fail safe.
                if(markedRecipes != null && markedRecipes.size() > 0){
                    RecipeData recipe = markedRecipes.get(0);
                    mIngredients = recipe.getIngredients();
                }
            }
        });
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if(mIngredients == null) return 0;
        return mIngredients.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.ingredient_list_item);
        if(mIngredients != null){
            IngredientData ingredientData = mIngredients.get(position);
            views.setTextViewText(R.id.ingredient_quantity_tv, String.valueOf(ingredientData.getQuantity()));
            views.setTextViewText(R.id.ingredint_measure_unit_tv, String.valueOf(ingredientData.getMeasure()));
            views.setTextViewText(R.id.ingredint_name_tv, String.valueOf(ingredientData.getIngredient()));
        }

        return views;
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
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}