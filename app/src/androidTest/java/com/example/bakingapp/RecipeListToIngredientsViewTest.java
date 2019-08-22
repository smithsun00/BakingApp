package com.example.bakingapp;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.example.bakingapp.utils.RecipeDataUtils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/*
This will test Moving from MainActivity to RecipeDetailsActivity
by clicking on a recipe from the list.
Then checking if Ingredient card is available
 */
@RunWith(AndroidJUnit4.class)
public class RecipeListToIngredientsViewTest {

    private static final String TEXT_INGREDIENTS = "Ingredients";

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void TestClickOnRecipe_ClickOnIngredientCard(){

        // Make sure we have at least one recipe in list
        Assert.assertTrue(RecipeDataUtils.getInstance().getNumRecipes() > 0);

        // Simulate click on a recipe from the recipe list in MainActivity
        onView(withId(R.id.recipe_list_rv))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Simulate click on the ingredient card
        onView(withId(R.id.recipe_step_list_rv))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Check to see if a vew includes the text 'Ingredients'
        onView(withText(TEXT_INGREDIENTS)).check(matches(ViewMatchers.isDisplayed()));
    }
}