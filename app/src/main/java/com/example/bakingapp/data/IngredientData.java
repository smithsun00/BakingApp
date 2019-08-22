package com.example.bakingapp.data;

import androidx.room.ColumnInfo;

public class IngredientData {

    @ColumnInfo(name = "quantity")
    private int quantity;
    @ColumnInfo(name = "measure")
    private String measure;
    @ColumnInfo(name = "ingredient")
    private String ingredient;

    public IngredientData(int quantity, String measure, String ingredient){
        this.quantity = quantity;
        this.measure = measure;
        this.ingredient = ingredient;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getMeasure() {
        return measure;
    }

    public String getIngredient() {
        return ingredient;
    }

}
