package com.example.bakingapp.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bakingapp.R;
import com.example.bakingapp.RecipeDetailsListFragment;
import com.example.bakingapp.databinding.IngredientsCardLayoutBinding;
import com.example.bakingapp.databinding.StepCardLayoutBinding;
import com.example.bakingapp.data.StepData;
import com.example.bakingapp.utils.RecipeDataUtils;

import java.util.ArrayList;

public class StepListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int ITEM_TYPE_INGREDIENTS_CARD = 0;
    private static final int ITEM_TYPE_STEP_CARD = 1;

    private ArrayList<StepData> mRecipeSteps;
    private Context mContext;

    final private RecipeDetailsListFragment.OnRecipeDetailsListItemClickListener mOnClickListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        mContext = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        if(viewType == ITEM_TYPE_INGREDIENTS_CARD){
            IngredientsCardLayoutBinding binder = IngredientsCardLayoutBinding.inflate(inflater, viewGroup, false);
            return new StepListAdapter.IngredientsViewHolder(binder);
        }else{
            StepCardLayoutBinding binder = StepCardLayoutBinding.inflate(inflater, viewGroup, false);
            return new StepListAdapter.StepViewHolder(binder);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return ITEM_TYPE_INGREDIENTS_CARD;
        }else{
            return ITEM_TYPE_STEP_CARD;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if(viewHolder instanceof StepViewHolder) {
            StepData stepData = mRecipeSteps.get(position - 1);
            ((StepViewHolder)viewHolder).bind(stepData, position + 1);
        }else if(viewHolder instanceof IngredientsViewHolder){
            ((IngredientsViewHolder)viewHolder).bind();
        }
    }

    @Override
    public int getItemCount() {
        return mRecipeSteps != null ? mRecipeSteps.size() : 0;
    }

    public StepListAdapter(ArrayList<StepData> recipeSteps, RecipeDetailsListFragment.OnRecipeDetailsListItemClickListener listener){
        mOnClickListener = listener;
        mRecipeSteps = recipeSteps;
    }

    public class StepViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private final StepCardLayoutBinding mBinder;

        // This was inspired by
        // https://medium.com/androiddevelopers/android-data-binding-recyclerview-db7c40d9f0e4
        public StepViewHolder(@NonNull StepCardLayoutBinding binder) {
            super(binder.getRoot());
            binder.getRoot().setOnClickListener(this);
            mBinder = binder;
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onItemSelected(clickedPosition);
        }

        void bind(StepData step, int numStep)
        {
            mBinder.stepCardShortDescriptionTv.setText(step.getShortDescription());
            String stepNumberText = mContext.getString(R.string.step_card_step_number_text) + " " + String.valueOf(numStep - 1);
            mBinder.stepCardNumber.setText(stepNumberText);
            mBinder.executePendingBindings();
        }
    }

    public class IngredientsViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private IngredientsCardLayoutBinding mBinder;

        public IngredientsViewHolder(@NonNull IngredientsCardLayoutBinding binder) {
            super(binder.getRoot());
            binder.getRoot().setOnClickListener(this);
            mBinder = binder;
        }

        // On click if details fragment exists -> update to show ingredients (Tablet-landscape mode)
        @Override
        public void onClick(View view) {
            mOnClickListener.onItemSelected(ITEM_TYPE_INGREDIENTS_CARD);
        }

        void bind(){
            String ingredientString = RecipeDataUtils.getInstance().getRecipeIngredientsAsString(mContext, null);

            mBinder.ingredientsCardTv.setText(ingredientString);
            mBinder.executePendingBindings();
        }
    }
}
