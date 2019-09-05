package com.example.bakingapp.model;

import android.view.MenuItem;

import androidx.lifecycle.ViewModel;

import com.example.bakingapp.AppExecutors;
import com.example.bakingapp.SavedRecipeDatabase;
import com.example.bakingapp.callbacks.IUnlockViewCallback;
import com.example.bakingapp.callbacks.IUpdateRecipeDataCallback;
import com.example.bakingapp.data.RecipeData;
import com.example.bakingapp.utils.RecipeDataUtils;

import java.util.List;

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
                            iCallback.UnlockItemCallback(item, isChecked, recipeData);
                    }
                });
            }
        });
    }

    public void UpdateRecipe(final RecipeData recipeData, final SavedRecipeDatabase database, final IUpdateRecipeDataCallback iCallback)
    {
        final AppExecutors executors = AppExecutors.getInstance();
        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                // First reset all marked inWidget recipes in database
                List<RecipeData> currentMarkedRecipe = RecipeDataUtils.getInstance().getFavoriteRecipeList();

                if(currentMarkedRecipe != null) {
                    for (int i = 0; i < currentMarkedRecipe.size(); i++) {
                        RecipeData recipe = currentMarkedRecipe.get(i);
                        recipe.setInWidget(false);
                        database.savedRecipesDao().updateRecipe(recipe);
                    }
                }

                // Now update selected recipe with inWidget true
                recipeData.setInWidget(true);
                database.savedRecipesDao().updateRecipe(recipeData);

                executors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if(iCallback != null)
                            iCallback.RecipeUpdatedCallback(recipeData);
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
                            iCallback.UnlockItemCallback(item, isChecked, recipeData);
                    }
                });
            }
        });
    }
}