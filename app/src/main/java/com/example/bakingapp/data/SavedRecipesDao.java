package com.example.bakingapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SavedRecipesDao {
    @Query("SELECT * FROM recipe")
    LiveData<List<RecipeData>> loadAllSavedRecipes();

    @Insert
    void insertRecipe(RecipeData recipe);

    @Delete
    void deleteRecipe(RecipeData recipe);
}
