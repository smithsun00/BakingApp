package com.example.bakingapp;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bakingapp.databinding.IngredientsCardLayoutBinding;

public class IngredientsCardFragment extends Fragment {

    private static final String INGREDIENT_TEXT_PARAM = "ingredient_text";

    private String mIngredientText;

    public IngredientsCardFragment() {
        // Required empty public constructor
    }

    public static IngredientsCardFragment newInstance(String ingredientText) {
        IngredientsCardFragment fragment = new IngredientsCardFragment();
        Bundle args = new Bundle();
        args.putString(INGREDIENT_TEXT_PARAM, ingredientText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIngredientText = getString(R.string.no_ingredients_found);
        if (getArguments() != null) {
            mIngredientText = getArguments().getString(INGREDIENT_TEXT_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        IngredientsCardLayoutBinding binder = IngredientsCardLayoutBinding.inflate(inflater, container, false);

        binder.ingredientsCardTv.setText(mIngredientText);

        return binder.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
