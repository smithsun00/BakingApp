package com.example.bakingapp.model;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.bakingapp.SavedRecipeDatabase;

public class MainActivityViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final SavedRecipeDatabase mDb;

    public MainActivityViewModelFactory(SavedRecipeDatabase database) {
        mDb = database;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new MainActivityViewModel(mDb);
    }
}