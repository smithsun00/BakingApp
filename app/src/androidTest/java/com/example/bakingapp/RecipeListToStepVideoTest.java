package com.example.bakingapp;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.example.bakingapp.data.RecipeData;
import com.example.bakingapp.utils.RecipeDataUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RecipeListToStepVideoTest {

    private IdlingResource mIdlingResource;

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void registerIdlingResource() {
        mIdlingResource = mActivityTestRule.getActivity().getIdlingResource();
        Espresso.registerIdlingResources(mIdlingResource);
    }

    @Test
    public void TestClickOnRecipe_ClickOnStepToPlayVideo(){

        // Wait until there is update with recipe data invovled
        onData(instanceOf(RecipeData.class));

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

    @After
    public void unregisterIdlingResource() {
        if(mIdlingResource != null){
            Espresso.unregisterIdlingResources(mIdlingResource);
        }
    }
}
