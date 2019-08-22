package com.example.bakingapp.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bakingapp.R;
import com.example.bakingapp.databinding.RecipeCardLayoutBinding;
import com.example.bakingapp.data.RecipeData;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipeListAdapter extends RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder> {

    // Keeps track of the context and list of images to display
    private List<RecipeData> mRecipes;

    final private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(RecipeData recipeItemClicked);
    }

    /**
     * Constructor method
     * @param recipes The list of recipes to display
     */
    public RecipeListAdapter(List<RecipeData> recipes, ListItemClickListener listener) {
        mRecipes = recipes;
        mOnClickListener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);

        RecipeCardLayoutBinding binder = RecipeCardLayoutBinding.inflate(inflater, viewGroup, false);
        return new RecipeViewHolder(binder);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder recipeViewHolder, int position) {

        RecipeData recipeData = mRecipes.get(position);
        recipeViewHolder.bind(recipeData);
    }

    @Override
    public int getItemCount() {
        return mRecipes != null ? mRecipes.size() : 0;
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private final RecipeCardLayoutBinding mBinder;

        // This was inspired by
        // https://medium.com/androiddevelopers/android-data-binding-recyclerview-db7c40d9f0e4
        public RecipeViewHolder(@NonNull RecipeCardLayoutBinding binder) {
            super(binder.getRoot());
            binder.getRoot().setOnClickListener(this);
            mBinder = binder;
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(mRecipes.get(clickedPosition));
        }

        void bind(RecipeData recipe)
        {
            // if has image load it, otherwise hide imageView:
            if(recipe.getImage() == null || recipe.getImage().equals("")){
                mBinder.recipeImageIv.setVisibility(View.GONE);
            }else{
                mBinder.recipeImageIv.setVisibility(View.VISIBLE);
                Picasso.get().load(recipe.getImage()).into(mBinder.recipeImageIv);
            }

            mBinder.recipeNameTv.setText(recipe.getName());
            mBinder.recipeServingsTv.setText(String.valueOf(recipe.getServings()));
            mBinder.executePendingBindings();
        }
    }
}
