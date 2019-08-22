package com.example.bakingapp.data;

import androidx.room.TypeConverter;

import com.example.bakingapp.utils.NetworkUtils;

import java.util.ArrayList;

public class IngredientsConverter {
    @TypeConverter
    public static ArrayList<IngredientData> toStepData(String ingredientString){
        return ingredientString == null ? null : NetworkUtils.ParseIngredientDataListFromJson(ingredientString);
    }

    @TypeConverter
    public static String toStepListString(ArrayList<IngredientData> ingredients){
        return ingredients == null ? null : NetworkUtils.IngredientDataListToJson(ingredients);
    }
}
