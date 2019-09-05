package com.example.bakingapp.callbacks;

import android.view.MenuItem;

import com.example.bakingapp.data.RecipeData;

public interface IUnlockViewCallback {
    void UnlockItemCallback(MenuItem item, boolean isChecked, RecipeData recipeData);
}
