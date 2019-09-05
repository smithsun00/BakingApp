package com.example.bakingapp.utils;

import android.content.Context;

import com.example.bakingapp.R;
import com.example.bakingapp.data.IngredientData;
import com.example.bakingapp.data.RecipeData;
import com.example.bakingapp.data.StepData;

import java.util.ArrayList;

public class RecipeDataUtils {

    public static final int INVALID_STEP_INDEX = -1;

    private static RecipeDataUtils instance = null;

    public static RecipeDataUtils getInstance(){
        if(instance == null){
            instance = new RecipeDataUtils();
        }

        return instance;
    }

    private boolean isSavedRecipesListVisible = false;
    private int mCurrentStepIndex;
    private RecipeData mCurrentRecipe;
    private RecipeData mCurrentWidgetRecipe;

    private ArrayList<RecipeData> mRecipeList;
    private ArrayList<RecipeData> mSavedRecipeList;

    public RecipeDataUtils(){
        mRecipeList = new ArrayList<>();
        mSavedRecipeList = new ArrayList<>();
        mCurrentStepIndex = INVALID_STEP_INDEX;
    }

    public RecipeData getCurrentWidgetRecipe(){
        return mCurrentWidgetRecipe;
    }

    public void setCurrentWidgetRecipe(RecipeData recipe){
        mCurrentWidgetRecipe = recipe;
    }

    public void setCurrentRecipe(RecipeData recipe)
    {
        mCurrentRecipe = recipe;
    }

    public RecipeData getCurrentRecipe(){
        return mCurrentRecipe;
    }

    public void resetCurrentValues(){
        mCurrentRecipe = null;
        mCurrentStepIndex = INVALID_STEP_INDEX;
    }

    public void setIsSavedRecipeListVisible(boolean isVisible){
        isSavedRecipesListVisible = isVisible;
    }

    public void AddRecipeToFavorite(RecipeData recipe){
        if(isRecipeInFavorite(recipe.getId())){
            return;
        }

        mSavedRecipeList.add(recipe);
    }

    public void RemoveRecipeToFavorite(RecipeData recipe){
        if(!isRecipeInFavorite(recipe.getId())){
            return;
        }

        mSavedRecipeList.remove(recipe);
    }

    public boolean isCurrentRecipeInFavorite(){
        RecipeData recipe = getCurrentRecipe();

        if(recipe != null && mSavedRecipeList != null && mSavedRecipeList.size() > 0){
            for(int i = 0; i < mSavedRecipeList.size(); i++){
                if(recipe.getId() == mSavedRecipeList.get(i).getId()){
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isRecipeInFavorite(int recipeId){
        if(mSavedRecipeList != null && mSavedRecipeList.size() > 0){
            for(int i = 0; i < mSavedRecipeList.size(); i++){
                if(recipeId == mSavedRecipeList.get(i).getId()){
                    return true;
                }
            }
        }

        return false;
    }

    public boolean hasSavedRecipes(){
        return mSavedRecipeList != null && mSavedRecipeList.size() > 0;
    }

    public boolean getIsSavedRecipeListVisible(){
        return isSavedRecipesListVisible;
    }

    public void setRecipeList(ArrayList<RecipeData> recipeList){
        mRecipeList = recipeList;
    }

    public void setmSavedRecipeList(ArrayList<RecipeData> recipeList){
        mSavedRecipeList = recipeList;

        // Get current recipe which is displayed in the widget:
        for(RecipeData recipe: mSavedRecipeList){
            if(recipe.getInWidget()){
                // Set as current widget recipe
                mCurrentWidgetRecipe = recipe;
            }
        }
    }

    public ArrayList<RecipeData> getRecipeList(){
        return isSavedRecipesListVisible ? mSavedRecipeList : mRecipeList;
    }

    public ArrayList<RecipeData> getFavoriteRecipeList(){
        return mSavedRecipeList;
    }

    public RecipeData getRecipeFromFavoriteByName(String recipeName){
        if(mSavedRecipeList != null) {
            for (RecipeData recipe : mSavedRecipeList) {
                if(recipe.getName().equals(recipeName)) return recipe;
            }
        }

        return null;
    }

    public ArrayList<StepData> getSteps(){
        RecipeData recipe = getCurrentRecipe();
        if(recipe == null){
            return null;
        }
        else{
            return recipe.getSteps();
        }
    }

    // Get Ingredients formatted string
    private String getIngredientDataAsString(IngredientData ingredientData){
        return ingredientData.getQuantity() + " " + ingredientData.getMeasure() + " - " + ingredientData.getIngredient();
    }

    public String getRecipeIngredientsAsString(Context context, RecipeData recipeData){
        RecipeData recipe = recipeData != null ? recipeData : getCurrentRecipe();
        String ingredientString = context.getResources().getString(R.string.no_ingredients_found);

        if(recipe.getIngredients() != null && recipe.getIngredients().size() > 0){
            ArrayList<IngredientData> ingredients = recipe.getIngredients();

            ingredientString = "";
            for (int i = 0; i < ingredients.size(); i++) {
                ingredientString += RecipeDataUtils.getInstance().getIngredientDataAsString(ingredients.get(i));
                ingredientString += i < ingredients.size() - 1 ? "\n" : "";
            }
        }

        return ingredientString;
    }

    public int getNumSteps(){
        RecipeData recipe = getCurrentRecipe();

        if(recipe != null && recipe.getSteps() != null){
            return recipe.getSteps().size();
        }

        return 0;
    }

    public boolean hasCurrentStep(){
        return mCurrentStepIndex != INVALID_STEP_INDEX;
    }

    public boolean hasNextStep(){
        RecipeData recipe = getCurrentRecipe();
        return recipe != null && hasCurrentStep() && mCurrentStepIndex < getNumSteps() - 1;
    }

    public boolean hasPreviousStep(){
        return hasCurrentStep() && mCurrentStepIndex > 0;
    }

    public void setCurrentStepIndex(int index){
        mCurrentStepIndex = index;
    }

    public int getCurrentStepIndex(){
        return mCurrentStepIndex;
    }

    public int getNumRecipes(){
        ArrayList<RecipeData> recipes = getRecipeList();
        return recipes != null ? recipes.size() : 0;
    }

    public StepData getCurrentStepData(){
        ArrayList<StepData> steps = getSteps();

        if(steps == null || mCurrentStepIndex >= steps.size() || mCurrentStepIndex < 0) return null;

        return steps.get(mCurrentStepIndex);
    }
}
