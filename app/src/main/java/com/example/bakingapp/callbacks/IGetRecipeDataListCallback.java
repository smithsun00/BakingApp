package com.example.bakingapp.callbacks;

import com.example.bakingapp.data.RecipeData;

import java.util.ArrayList;

public interface IGetRecipeDataListCallback {
    void onRecipeDataCallback(ArrayList<RecipeData> recipeData);
}
