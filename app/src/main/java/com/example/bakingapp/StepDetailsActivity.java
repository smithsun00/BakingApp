package com.example.bakingapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bakingapp.utils.RecipeDataUtils;


public class StepDetailsActivity extends AppCompatActivity implements
        StepDetailsFragment.OnNextOrPreviousStepClickListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_details);

        // If we got here then we are in stepDetailsActivity (Video page) in landscape mode on Tablet -> go back to two pane activity
        // We do not want video to take all screen on tablets
        if(findViewById(R.id.go_back_to_previous_activity_on_tablet) != null) {
                finish();
                return;
        }

        boolean isIngredientCardLayout;
        Intent intent = getIntent();
        isIngredientCardLayout = intent != null && intent.hasExtra(RecipeDetailsActivity.EXTRA_KEY_IS_INGREDIENT_CARD);

        // Load the correct fragment (IngredientCard or StepCard)
        FragmentManager fragmentManager = getSupportFragmentManager();

        if(RecipeDataUtils.getInstance().hasCurrentStep() && !isIngredientCardLayout){
            StepDetailsFragment stepDetailsFragment = new StepDetailsFragment(this);

            // Apparently we should be carefull when using .add method to add a fragment
            // this has caused multiple videos/sounds to be played for each time .add was called
            // e.g: when orientaton is changed
            fragmentManager.beginTransaction()
                    .replace(R.id.step_details_container, stepDetailsFragment)
                    .commit();
        }else{
            // No step selected -> show Ingredient fragment:
            String ingredientsText = RecipeDataUtils.getInstance().getRecipeIngredientsAsString(this, null);
            IngredientsCardFragment ingredientsCardFragment = IngredientsCardFragment.newInstance(ingredientsText);

            fragmentManager.beginTransaction()
                    .replace(R.id.step_details_container, ingredientsCardFragment)
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    // This will be called when a user has clicked either the previous or next step button
    @Override
    public void onClick(boolean nextStep) {
        int changedStepIndex = RecipeDataUtils.getInstance().getCurrentStepIndex() + (nextStep ? 1 : -1);
        RecipeDataUtils.getInstance().setCurrentStepIndex(changedStepIndex);

        FragmentManager fragmentManager = getSupportFragmentManager();
        StepDetailsFragment stepDetailsFragment = new StepDetailsFragment(this);

        fragmentManager.beginTransaction()
                .replace(R.id.step_details_container, stepDetailsFragment)
                .commit();
    }
}
