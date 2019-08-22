package com.example.bakingapp;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.example.bakingapp.utils.RecipeDataUtils;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.allOf;

@RunWith(AndroidJUnit4.class)
public class RecipeListToStepVideoTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void TestClickOnRecipe_ClickOnStepToPlayVideo(){

        // Make sure we have at least one recipe in list
        Assert.assertTrue(RecipeDataUtils.getInstance().getNumRecipes() > 0);

        // Simulate click on a recipe from the recipe list in MainActivity
        onView(withId(R.id.recipe_list_rv))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Make sure there is at least 1 step in list
        Assert.assertTrue(RecipeDataUtils.getInstance().getNumSteps() > 0);

        // Simulate click on the ingredient card
        onView(withId(R.id.recipe_step_list_rv))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        // Check if the video view exists
        onView(allOf(withId(R.id.step_details_container))).check(matches(isDisplayed()));
    }
}
