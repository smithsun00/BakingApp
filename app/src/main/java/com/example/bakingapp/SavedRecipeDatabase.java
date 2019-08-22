package com.example.bakingapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.bakingapp.data.IngredientsConverter;
import com.example.bakingapp.data.RecipeData;
import com.example.bakingapp.data.SavedRecipesDao;
import com.example.bakingapp.data.StepConverter;

@Database(entities = {RecipeData.class}, version = 1, exportSchema = false)
@TypeConverters({StepConverter.class, IngredientsConverter.class})
public abstract class SavedRecipeDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "saved_recipes";
    private static SavedRecipeDatabase sInstance;

    public static SavedRecipeDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        SavedRecipeDatabase.class, SavedRecipeDatabase.DATABASE_NAME)
                        .build();
            }
        }
        return sInstance;
    }

    public abstract SavedRecipesDao savedRecipesDao();

}
