package com.example.bakingapp.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.bakingapp.utils.NetworkUtils;

import java.util.ArrayList;

@Entity(tableName = "recipe")
public class RecipeData implements Parcelable {

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "ingredients")
    private ArrayList<IngredientData> ingredients;
    @ColumnInfo(name = "steps")
    private ArrayList<StepData> steps;
    @ColumnInfo(name = "servings")
    private int servings;
    @ColumnInfo(name = "image")
    private String image;
    @ColumnInfo(name = "in_widget")
    private boolean inWidget;

    public RecipeData(int id, String name, ArrayList<IngredientData> ingredients, ArrayList<StepData> steps, int servings, String image, boolean inWidget)
    {
        this.id = id;
        this.name = name;
        this.ingredients = ingredients;
        this.steps = steps;
        this.servings = servings;
        this.image = image;
        this.inWidget = inWidget;
    }

    @Ignore
    public RecipeData(Parcel parcel){
        id = parcel.readInt();
        name = parcel.readString();
        ingredients = NetworkUtils.ParseIngredientDataListFromJson(parcel.readString());
        steps = NetworkUtils.ParseStepDataListFromJson(parcel.readString());
        servings = parcel.readInt();
        image = parcel.readString();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<IngredientData> getIngredients() {
        return ingredients;
    }

    public ArrayList<StepData> getSteps() {
        return steps;
    }

    public int getServings() {
        return servings;
    }

    public String getImage() {
        return image;
    }

    public boolean getInWidget() {
        return inWidget;
    }

    public void setInWidget(boolean isInWidget) {
        inWidget = isInWidget;
    }

    @Ignore
    @Override
    public int describeContents() {
        return 0;
    }

    @Ignore
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(NetworkUtils.IngredientDataListToJson(ingredients));
        parcel.writeString(NetworkUtils.StepDataListToJson(steps));
        parcel.writeInt(servings);
        parcel.writeString(image);
    }

    @Ignore
    public static final Parcelable.Creator<RecipeData> CREATOR = new Parcelable.Creator<RecipeData>(){

        @Override
        public RecipeData createFromParcel(Parcel parcel) {
            return new RecipeData(parcel);
        }

        @Override
        public RecipeData[] newArray(int i) {
            return new RecipeData[i];
        }
    };
}
