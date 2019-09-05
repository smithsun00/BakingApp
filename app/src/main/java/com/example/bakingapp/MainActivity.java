package com.example.bakingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.bakingapp.IdlingResource.SimpleIdlingResource;
import com.example.bakingapp.adapters.RecipeListAdapter;
import com.example.bakingapp.callbacks.IGetRecipeDataListCallback;
import com.example.bakingapp.callbacks.IInternetConnectionCallback;
import com.example.bakingapp.data.RecipeData;
import com.example.bakingapp.databinding.ActivityMainBinding;
import com.example.bakingapp.model.MainActivityViewModel;
import com.example.bakingapp.model.MainActivityViewModelFactory;
import com.example.bakingapp.utils.NetworkUtils;
import com.example.bakingapp.utils.RecipeDataUtils;
import com.example.bakingapp.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;


// todo: fix back button from RecipeDetailsActivity -> for some reason when back from widget -> back reload same activity
public class MainActivity extends AppCompatActivity
implements RecipeListAdapter.ListItemClickListener,
        IInternetConnectionCallback,
        IGetRecipeDataListCallback {

    private final static String SAVED_STATE_KEY_CURRENT_SCROLL_POSITION = "scroll_y";

    private MainActivityViewModel mViewModel;

    private float mScrollListPosition;
    private MenuItem mSortByMenuItem;
    private ActivityMainBinding mBinder;
    private String mWidgetRecipeName;

    @Nullable
    private SimpleIdlingResource mIdlingResource;

    public SimpleIdlingResource getIdlingResource()
    {
        if(mIdlingResource == null)
        {
            mIdlingResource = new SimpleIdlingResource();
        }

        return mIdlingResource;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWidgetRecipeName = null;
        Intent widgetIntent = getIntent();
        if (widgetIntent != null && widgetIntent.hasExtra(BakingAppWidget.EXTRA_RECIPE_NAME)) {
            mWidgetRecipeName = widgetIntent.getStringExtra(BakingAppWidget.EXTRA_RECIPE_NAME);
        }

        // Get last scroll position if saved
        mScrollListPosition = 0f;
        if(savedInstanceState != null) {
            if (savedInstanceState.containsKey(SAVED_STATE_KEY_CURRENT_SCROLL_POSITION)) {
                mScrollListPosition = savedInstanceState.getFloat(SAVED_STATE_KEY_CURRENT_SCROLL_POSITION);
            }
        }

        LoadActivityContent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getIdlingResource().setIdleState(false);
    }

    private void LoadActivityContent(){
        int numColumns = getResources().getInteger(R.integer.recipe_list_num_column);
        GridLayoutManager layoutManager = new GridLayoutManager(this, numColumns);

        mBinder = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Set space between items in recipe list
        mBinder.recipeListRv.addItemDecoration(new UIUtils.SpacesItemDecoration(getResources().getInteger(R.integer.list_item_space)));
        mBinder.recipeListRv.setLayoutManager(layoutManager);

        // Initialize the View Model
        SavedRecipeDatabase database = SavedRecipeDatabase.getInstance(this);
        MainActivityViewModelFactory factory = new MainActivityViewModelFactory(database);
        mViewModel = ViewModelProviders.of(this, factory).get(MainActivityViewModel.class);

        // Initialize database
        initializeDatabaseAndListeners();

        // Reset recipeDataUtils current held data:
        RecipeDataUtils.getInstance().resetCurrentValues();

        // Check internet connection first:
        NetworkUtils.hasConnection(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent widgetIntent = getIntent();
        if (widgetIntent != null && widgetIntent.hasExtra(BakingAppWidget.EXTRA_RECIPE_NAME)) {
            mWidgetRecipeName = widgetIntent.getStringExtra(BakingAppWidget.EXTRA_RECIPE_NAME);
//            widgetIntent.removeExtra(BakingAppWidget.EXTRA_RECIPE_NAME);
        }
        else{
            mWidgetRecipeName = null;
        }
        // This load content incase we got here from RecipeDetailsActivity if app opened with Widget
        // and then we press back.
        // This prevents from skiping loading content for activity.
        if(RecipeDataUtils.getInstance().getCurrentWidgetRecipe() != null) {
            if(!TryLoadWidgetRecipe()){
                LoadActivityContent();
            }
        }
    }

    private boolean TryLoadWidgetRecipe(){

        // Get recipe from widget if set:
        RecipeData recipe = null;
        if (mWidgetRecipeName != null) {
            recipe = RecipeDataUtils.getInstance().getRecipeFromFavoriteByName(mWidgetRecipeName);
        }

        // If entered app using the widget -> go to details activity of the selected recipe
        if (recipe != null) {
            RecipeDataUtils.getInstance().setCurrentRecipe(recipe);

            RecipeDataUtils.getInstance().setCurrentWidgetRecipe(recipe);
            mWidgetRecipeName = null;
            Intent recipeDetailsActivityIntent = new Intent(this, RecipeDetailsActivity.class);
            startActivity(recipeDetailsActivityIntent);
            return true;
        }

        RecipeDataUtils.getInstance().setCurrentWidgetRecipe(null);

        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mBinder != null) {
            // Save current scroll position
            int verticalScrollOffset = mBinder.recipeListRv.computeVerticalScrollOffset();
            int verticalScrollRange = mBinder.recipeListRv.computeVerticalScrollRange();
            outState.putFloat(SAVED_STATE_KEY_CURRENT_SCROLL_POSITION, (float) verticalScrollOffset / verticalScrollRange);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if(mViewModel != null && mViewModel.GetSavedRecipesList().hasActiveObservers())
            mViewModel.GetSavedRecipesList().removeObservers(this);

        super.onDestroy();
    }

    @Override
    public void onListItemClick(RecipeData recipeItemClicked) {
        RecipeDataUtils.getInstance().setCurrentRecipe(recipeItemClicked);

        Intent recipeDetailsActivityIntent = new Intent(this, RecipeDetailsActivity.class);
        startActivity(recipeDetailsActivityIntent);
    }

    private void initializeDatabaseAndListeners()
    {
        mViewModel.GetSavedRecipesList().observe(this, new Observer<List<RecipeData>>() {
            @Override
            public void onChanged(List<RecipeData> recipeData) {
                RecipeDataUtils.getInstance().setmSavedRecipeList((ArrayList<RecipeData>) recipeData);

                // Get recipe from widget if set:
                if(TryLoadWidgetRecipe()){
                    return;
                }

                if(RecipeDataUtils.getInstance().getIsSavedRecipeListVisible()){
                    if(RecipeDataUtils.getInstance().hasSavedRecipes()) {
                        // if saved recipes list is currently visible -> update UI:
                        ArrayList<RecipeData> recipes = RecipeDataUtils.getInstance().getRecipeList();
                        updateUI(recipes);
                    }else{
                        loadDefaultRecipeList();
                    }
                }
            }
        });
    }

    private void showNoDataOfflineMessage(){
        mBinder.noInternetMessageTv.setVisibility(View.VISIBLE);
        mBinder.recipeListRv.setVisibility(View.INVISIBLE);
    }

    private void showDefaultView(){
        mBinder.noInternetMessageTv.setVisibility(View.INVISIBLE);
        mBinder.recipeListRv.setVisibility(View.VISIBLE);
    }

    private void loadSavedRecipesView(){
        // Mark current list as savedDataList
        RecipeDataUtils.getInstance().setIsSavedRecipeListVisible(true);
        // Check if saved recipes holds at least one recipe -> update UI with current savedRecipe list:
        ArrayList<RecipeData> recipes = RecipeDataUtils.getInstance().getRecipeList();
        if(recipes.size() > 0){
            showDefaultView();
            updateUI(recipes);
        }else{
            // Else -> Load no recipe data default view
            showNoDataOfflineMessage();
        }
    }

    private void loadDefaultRecipeList(){
        // Mark current list as default webservice based list
        RecipeDataUtils.getInstance().setIsSavedRecipeListVisible(false);
        ArrayList<RecipeData> recipes = RecipeDataUtils.getInstance().getRecipeList();
        if ((recipes == null || recipes.size() == 0) && !RecipeDataUtils.getInstance().getIsSavedRecipeListVisible()) {
            NetworkUtils.requestRecipeDataFromRestAPI(this, this);
        }else{
            onRecipeDataCallback(recipes);
        }
    }

    @Override
    public void onConnectionCallback(boolean hasInternet) {
        if(hasInternet && !RecipeDataUtils.getInstance().getIsSavedRecipeListVisible()) {
            // Initialize recipeList if not already initialized
            showDefaultView();
            loadDefaultRecipeList();
        }else{
            // No internet connection -> show saved recipes. if none show default view
            loadSavedRecipesView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mViewModel.GetSavedRecipesList().hasActiveObservers()) {
            mViewModel.GetSavedRecipesList().removeObservers(this);
        }
    }

    @Override
    public void onRecipeDataCallback(final ArrayList<RecipeData> recipeData) {
        // If no data exists -> show default no data view
        if(recipeData == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showNoDataOfflineMessage();
                }
            });
            return;
        }

        // Set recipe data in RecipeDataUtils
        RecipeDataUtils.getInstance().setRecipeList(recipeData);

        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              updateUI(recipeData);

                              getIdlingResource().setIdleState(true);
                          }
                      });
    }

    private void updateUI(final ArrayList<RecipeData> recipeData){

        RecipeListAdapter adapter = new RecipeListAdapter(recipeData, this);

        mBinder.recipeListRv.setHasFixedSize(false);
        mBinder.recipeListRv.setAdapter(adapter);

        // Apply scroll position if we have one saved
        if (mScrollListPosition != 0) {
            // Wait until RecycleView population is done.
            // Taken from here:
            // https://stackoverflow.com/questions/30397460/how-to-know-when-the-recyclerview-has-finished-laying-down-the-items
            mBinder.recipeListRv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mBinder.recipeListRv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int numRecipesInList = RecipeDataUtils.getInstance().getNumRecipes();
                    float scrollTo = numRecipesInList * mScrollListPosition;
                    mBinder.recipeListRv.scrollToPosition((int) scrollTo);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);

        // Menu Item Spinner - was taken from here:
        // https://stackoverflow.com/questions/37250397/how-to-add-a-spinner-next-to-a-menu-in-the-toolbar
        mSortByMenuItem = menu.findItem(R.id.sort_resipes_spinner);
        Spinner spinner = (Spinner) mSortByMenuItem.getActionView();

        int spinnerTitle = RecipeDataUtils.getInstance().getIsSavedRecipeListVisible() ? R.string.menu_item_spinner_option_saved :
                R.string.menu_item_spinner_option_default;
        mSortByMenuItem.setTitle(getString(R.string.sort_by_label) + " " + getString(spinnerTitle));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.action_get_recipe_by, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean isCurrentSavedRecipeList = RecipeDataUtils.getInstance().getIsSavedRecipeListVisible();

        switch (item.getItemId())
        {
            case R.id.sort_by_default:
                // If we are offline, we allow only already set 'favorite' list mode
                if(NetworkUtils.getIsOffline()) {
                    Toast.makeText(this, R.string.offline_mode_menu_items_blocked_message, Toast.LENGTH_LONG).show();
                    return true;
                }

                mSortByMenuItem.setTitle(getString(R.string.sort_by_label) + " " + getString(R.string.menu_item_spinner_option_default));

                if(!isCurrentSavedRecipeList) return true;

                loadDefaultRecipeList();
                return true;
            case R.id.sort_by_saved:
                // If we are offline, we allow only already set 'favorite' list mode
                if(NetworkUtils.getIsOffline()) {
                    Toast.makeText(this, R.string.offline_mode_menu_items_blocked_message, Toast.LENGTH_LONG).show();
                    return true;
                }else if(!RecipeDataUtils.getInstance().hasSavedRecipes()){
                    Toast.makeText(this, R.string.no_recipe_was_added_to_database_yet, Toast.LENGTH_LONG).show();
                    return true;
                }

                mSortByMenuItem.setTitle(getString(R.string.sort_by_label) + " " + getString(R.string.menu_item_spinner_option_saved));

                if(isCurrentSavedRecipeList) return true;

                loadSavedRecipesView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
