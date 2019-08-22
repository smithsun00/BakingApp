package com.example.bakingapp;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;

import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.bakingapp.adapters.StepListAdapter;
import com.example.bakingapp.callbacks.IUnlockViewCallback;
import com.example.bakingapp.data.RecipeData;
import com.example.bakingapp.data.StepData;
import com.example.bakingapp.model.RecipeDetailsListViewModel;
import com.example.bakingapp.utils.RecipeDataUtils;
import com.example.bakingapp.utils.UIUtils;

import java.util.ArrayList;

public class RecipeDetailsActivity extends AppCompatActivity implements RecipeDetailsListFragment.OnRecipeDetailsListItemClickListener,
        StepDetailsFragment.OnNextOrPreviousStepClickListener,
        IUnlockViewCallback {

    public static final String EXTRA_KEY_IS_INGREDIENT_CARD = "is_ingredient_card";

    private RecipeDetailsListViewModel mViewModel;
    private SavedRecipeDatabase mDatabase;

    private Toast mToast;

    private boolean mTwoPane;
    private boolean mIsRecipeInFavoriteList;
    private boolean mBlockFavoriteIconTouch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        ArrayList<StepData> steps = RecipeDataUtils.getInstance().getSteps();
        if(steps == null){
            // Show error toast: "recipe data not found"
            Toast.makeText(this, getString(R.string.no_ingredients_found), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mViewModel = ViewModelProviders.of(this).get(RecipeDetailsListViewModel.class);
        mDatabase = SavedRecipeDatabase.getInstance(this);

        // Add Actionbar back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Add space between items in steps list
        RecyclerView stepListRV = findViewById(R.id.recipe_step_list_rv);
        stepListRV.addItemDecoration(new UIUtils.SpacesItemDecoration(getResources().getInteger(R.integer.list_item_space)));

        // Check if recipe is in saved list
        mIsRecipeInFavoriteList = RecipeDataUtils.getInstance().isCurrentRecipeInFavorite();

        // Check if we have two pane view (Tablets)
        if(findViewById(R.id.recipe_details_activity) != null) {
            mTwoPane = true;
            FragmentManager fm = getSupportFragmentManager();

            // if we already have a step chosen -> display it in the right fragment
            if(RecipeDataUtils.getInstance().hasCurrentStep()){
                StepDetailsFragment stepDetailsFragment = new StepDetailsFragment(this);

                fm.beginTransaction()
                        .replace(R.id.step_details_fragment_landtablet, stepDetailsFragment)
                        .commit();
            }else{
                // No step selected -> show Ingredient fragment:
                String ingredientsText = RecipeDataUtils.getInstance().getRecipeIngredientsAsString(this, null);
                IngredientsCardFragment ingredientsCardFragment = IngredientsCardFragment.newInstance(ingredientsText);

                fm.beginTransaction()
                        .replace(R.id.step_details_fragment_landtablet, ingredientsCardFragment)
                        .commit();
            }
        }else{
            mTwoPane = false;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {

        MenuItem item = menu.findItem(R.id.add_to_favorite);

        if (mIsRecipeInFavoriteList){
            item.setIcon(R.drawable.star_gold);
        }
        else {
            item.setIcon(R.drawable.star_white);
        }
        item.setChecked(mIsRecipeInFavoriteList);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe_details_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if(itemId == R.id.menu_item_show_selected_recipe_in_widget) {
            RecipeData recipe = RecipeDataUtils.getInstance().getCurrentRecipe();

            if (recipe != null) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, BakingAppWidget.class));

                BakingAppWidget.updateBakingWidgets(this, appWidgetManager, appWidgetIds, recipe);
            } else {
                // No recipe was selected yet, show toast message to indicate it
                if (mToast != null) {
                    mToast.cancel();
                    mToast = null;
                }

                mToast = Toast.makeText(this, getString(R.string.no_recipe_selected), Toast.LENGTH_LONG);
                mToast.show();
            }

            return true;
        }else if(itemId == R.id.add_to_favorite){
            if(mBlockFavoriteIconTouch) return true;

            mBlockFavoriteIconTouch = true;

            RecipeData recipe = RecipeDataUtils.getInstance().getCurrentRecipe();

            if (item.isChecked()){
                // Update database: remove from database
                mViewModel.RemoveFromFavorite(recipe, mDatabase, this, item, false);
                return true;
            }
            else {
                // Update database: add to database
                mViewModel.AddToFavorite(recipe, mDatabase, this, item, true);
                return true;
            }
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemSelected(int clickedItemPosition) {
        boolean defaultIngredientCardSelected = clickedItemPosition == StepListAdapter.ITEM_TYPE_INGREDIENTS_CARD;
        // If item clicked is not ingredient card -> update current step index
        if(!defaultIngredientCardSelected) {
            int clickedItemIndex = clickedItemPosition - 1;
            RecipeDataUtils.getInstance().setCurrentStepIndex(clickedItemIndex);
        }

        // If multi-pane: reload right-side stepDetailsFragment
        if(mTwoPane){
            FragmentManager fm = getSupportFragmentManager();

            if(defaultIngredientCardSelected){
                // Load Ingredient fragent
                IngredientsCardFragment ingredientsCardFragment = new IngredientsCardFragment();

                fm.beginTransaction()
                        .replace(R.id.step_details_fragment_landtablet, ingredientsCardFragment)
                        .commit();
            }else{
                // Load relevant step details fragment
                StepDetailsFragment stepDetailsFragment = new StepDetailsFragment(this);

                fm.beginTransaction()
                        .replace(R.id.step_details_fragment_landtablet, stepDetailsFragment)
                        .commit();
            }
        }else{
            // Else: load StepDetailsActivity
            Intent stepActivityIntent = new Intent(this, StepDetailsActivity.class);

            // Flag with ingredientCard if it was clicked
            if(defaultIngredientCardSelected) {
                stepActivityIntent.putExtra(EXTRA_KEY_IS_INGREDIENT_CARD, true);
            }

            startActivity(stepActivityIntent);
        }
    }

    // If this activated, we got here view a two pane click on an arrow for next or previous step
    @Override
    public void onClick(boolean nextStep) {
        // We"ll simulate a click on the correct step from the list
        // so first we get the current step index + 1 (for position because ingredient card takes the first)
        // and add an addition +1 to get the next step position
        // or -1 if we want previous step
        int currentStepPosition = RecipeDataUtils.getInstance().getCurrentStepIndex() + 1;
        onItemSelected(currentStepPosition + (nextStep ? 1 : -1));
    }

    public void UnlockItemCallback(MenuItem item, boolean isChecked)
    {
        mBlockFavoriteIconTouch = false;

        item.setIcon(isChecked ? R.drawable.star_gold : R.drawable.star_white);
        item.setChecked(isChecked);

        if(mToast != null) {
            mToast.cancel();
            mToast = null;
        }

        int messageId = isChecked ? R.string.message_recipe_added_to_db : R.string.message_recipe_removed_from_db;
        mToast = Toast.makeText(this, getResources().getString(messageId), Toast.LENGTH_SHORT);
        mToast.show();
    }

}
