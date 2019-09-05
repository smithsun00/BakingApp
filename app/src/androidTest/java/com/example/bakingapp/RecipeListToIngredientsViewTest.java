package com.example.bakingapp;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.example.bakingapp.data.RecipeData;
import com.example.bakingapp.utils.RecipeDataUtils;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

/*
This will test Moving from MainActivity to RecipeDetailsActivity
by clicking on a recipe from the list.
Then checking if Ingredient card is available
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class RecipeListToIngredientsViewTest {

    private static final String TEXT_INGREDIENTS = "Ingredients";

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
    public void TestClickOnRecipe_ClickOnIngredientCard(){

        // Wait until there is update with recipe data invovled
        onData(is(instanceOf(RecipeData.class)));

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

    @After
    public void unregisterIdlingResource() {
        if(mIdlingResource != null){
            Espresso.unregisterIdlingResources(mIdlingResource);
        }
    }
}