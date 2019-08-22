package com.example.bakingapp.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.bakingapp.SavedRecipeDatabase;
import com.example.bakingapp.data.RecipeData;

import java.util.List;

public class MainActivityViewModel extends ViewModel {

    private LiveData<List<RecipeData>> mSavedRecipes;

    public MainActivityViewModel(SavedRecipeDatabase database){
        mSavedRecipes = database.savedRecipesDao().loadAllSavedRecipes();
    }

    public LiveData<List<RecipeData>> GetSavedRecipesList()
    {
        return mSavedRecipes;
    }
}
