package com.example.bakingapp.callbacks;

import android.view.MenuItem;

import com.example.bakingapp.data.RecipeData;

public interface IUpdateRecipeDataCallback {
    void RecipeUpdatedCallback(RecipeData recipe);
}
