package com.example.bakingapp;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.bakingapp.adapters.StepListAdapter;
import com.example.bakingapp.databinding.FragmentRecipeDetailsListBinding;
import com.example.bakingapp.data.StepData;
import com.example.bakingapp.utils.RecipeDataUtils;

import java.util.ArrayList;

public class RecipeDetailsListFragment extends Fragment {

    FragmentRecipeDetailsListBinding mBinder;

    OnRecipeDetailsListItemClickListener mCallback;

    public interface OnRecipeDetailsListItemClickListener {
        void onItemSelected(int position);
    }

    public RecipeDetailsListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context context = getContext();

        ArrayList<StepData> steps = RecipeDataUtils.getInstance().getSteps();
        if(steps == null){
            // Show error toast: "recipe data not found"
            Toast.makeText(context, getString(R.string.no_ingredients_found), Toast.LENGTH_LONG).show();
            throw new NullPointerException();
        }

        mBinder = FragmentRecipeDetailsListBinding.inflate(inflater, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        mBinder.recipeStepListRv.setLayoutManager(layoutManager);
        mBinder.recipeStepListRv.setHasFixedSize(false);
        StepListAdapter adapter = new StepListAdapter(steps, mCallback);
        mBinder.recipeStepListRv.setAdapter(adapter);

        return mBinder.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnRecipeDetailsListItemClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnStepClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mCallback = null;
    }
}
