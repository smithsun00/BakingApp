package com.example.bakingapp.model;

import android.view.MenuItem;

import androidx.lifecycle.ViewModel;

import com.example.bakingapp.AppExecutors;
import com.example.bakingapp.SavedRecipeDatabase;
import com.example.bakingapp.callbacks.IUnlockViewCallback;
import com.example.bakingapp.data.RecipeData;

public class RecipeDetailsListViewModel extends ViewModel {

    public RecipeDetailsListViewModel(){

    }

    public void AddToFavorite(final RecipeData recipeData, final SavedRecipeDatabase database, final IUnlockViewCallback iCallback, final MenuItem item, final boolean isChecked)
    {
        final AppExecutors executors = AppExecutors.getInstance();
        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                database.savedRecipesDao().insertRecipe(recipeData);

                executors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if(iCallback != null)
                            iCallback.UnlockItemCallback(item, isChecked);
                    }
                });
            }
        });
    }

    public void RemoveFromFavorite(final RecipeData recipeData, final SavedRecipeDatabase database, final IUnlockViewCallback iCallback, final MenuItem item, final boolean isChecked)
    {
        final AppExecutors executors = AppExecutors.getInstance();
        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                database.savedRecipesDao().deleteRecipe(recipeData);

                executors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if(iCallback != null)
                            iCallback.UnlockItemCallback(item, isChecked);
                    }
                });
            }
        });
    }
}