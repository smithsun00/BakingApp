package com.example.bakingapp.utils;

import android.content.Context;

import com.example.bakingapp.AppExecutors;
import com.example.bakingapp.callbacks.IGetRecipeDataListCallback;
import com.example.bakingapp.callbacks.IInternetConnectionCallback;
import com.example.bakingapp.R;
import com.example.bakingapp.data.IngredientData;
import com.example.bakingapp.data.RecipeData;
import com.example.bakingapp.data.StepData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class NetworkUtils {

    private final static String RECIPE_DATA_REST_API_URL = "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json";

    private String recipeJsonData;
    private static boolean offlineMode = false;

    public static boolean getIsOffline(){
        return offlineMode;
    }

    public static void requestRecipeDataFromRestAPI(final Context context, final IGetRecipeDataListCallback iCallback)
    {
        final AppExecutors executors = AppExecutors.getInstance();

        executors.networkIO().execute(new Runnable() {
            @Override
            public void run() {

                try {

                    URL url = new URL(RECIPE_DATA_REST_API_URL);
                    String recipeDataListResponse = GetResponseFromHttpUrl(url);

                    final ArrayList<RecipeData> recipeDataList = ParseJsonDataIntoRecipeData(recipeDataListResponse);

                    // Check if ViewModel was not destroyed together with its Activity
                    executors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            if(iCallback != null)
                                iCallback.onRecipeDataCallback(recipeDataList);
                        }
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    if(iCallback != null)
                        iCallback.onRecipeDataCallback(null);
                }
            }
        });
    }

    public static ArrayList<StepData> ParseStepDataListFromJson(String jsonString){
        try{
            Gson gson = new Gson();

            return gson.fromJson(jsonString, new TypeToken<ArrayList<StepData>>(){}.getType());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String StepDataListToJson(ArrayList<StepData> stepList){
        Gson gson = new Gson();
        return gson.toJson(stepList);
    }

    public static ArrayList<IngredientData> ParseIngredientDataListFromJson(String jsonString){
        try{
            Gson gson = new Gson();

            return gson.fromJson(jsonString, new TypeToken<ArrayList<IngredientData>>(){}.getType());
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String IngredientDataListToJson(ArrayList<IngredientData> ingredientList){
        Gson gson = new Gson();
        return gson.toJson(ingredientList);
    }

    public static RecipeData ParseRecipeDataToJson(String json){
        try{
            Gson gson = new Gson();

            return gson.fromJson(json, new TypeToken<RecipeData>(){}.getType());
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String ParseRecipeDataToJson(RecipeData recipe){
        Gson gson = new Gson();
        return gson.toJson(recipe);
    }

    public static ArrayList<RecipeData> ParseJsonDataIntoRecipeData(String jsonString)
    {
        try
        {
            JSONArray recipeDataList = new JSONArray(jsonString);

            int numRecipesInDataList = recipeDataList.length();
            ArrayList<RecipeData> recipeList = new ArrayList<>();
            for (int i = 0; i < numRecipesInDataList; i++)
            {
                JSONObject recipeData = recipeDataList.getJSONObject(i);
                int id = recipeData.getInt("id");
                String name = recipeData.getString("name");
                int servings = recipeData.getInt("servings");
                String image = recipeData.getString("image");

                // ingredients
                JSONArray ingredientsDataList = recipeData.getJSONArray("ingredients");

                ArrayList<IngredientData> ingredientList = new ArrayList<>();
                for (int j = 0; j < numRecipesInDataList; j++)
                {
                    JSONObject ingredientsData = ingredientsDataList.getJSONObject(j);
                    int quantity = ingredientsData.getInt("quantity");
                    String measure = ingredientsData.getString("measure");
                    String ingredient = ingredientsData.getString("ingredient");

                    ingredientList.add(new IngredientData(quantity, measure, ingredient));
                }

                // steps
                JSONArray stepDataList = recipeData.getJSONArray("steps");

                int numStepsInRecipe = stepDataList.length();
                ArrayList<StepData> stepList = new ArrayList<>();
                for (int j = 0; j < numStepsInRecipe; j++)
                {
                    JSONObject stepsData = stepDataList.getJSONObject(j);
                    int stepId = stepsData.getInt("id");
                    String shortDescription = stepsData.getString("shortDescription");
                    String description = stepsData.getString("description");
                    String videoURL = stepsData.getString("videoURL");
                    String thumbnailURL = stepsData.getString("thumbnailURL");

                    stepList.add(new StepData(stepId, shortDescription, description, videoURL, thumbnailURL));
                }

                recipeList.add(new RecipeData(id, name, ingredientList, stepList, servings, image, false));
            }

            return recipeList;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static String GetResponseFromHttpUrl(URL url) throws IOException
    {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try
        {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();

            if(hasInput)
            {
                return scanner.next();
            }
            else
            {
                return null;
            }
        }
        finally
        {
            urlConnection.disconnect();
        }
    }

    // Check for internet connectivity:
    public static void hasConnection(final Context context, final IInternetConnectionCallback iCallback) {
        final AppExecutors executors = AppExecutors.getInstance();

        executors.networkIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket sock = new Socket();
                    int timeout = 1500;
                    sock.connect(new InetSocketAddress("8.8.8.8", 53), timeout);
                    sock.close();

                    executors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            if(iCallback != null)
                                iCallback.onConnectionCallback(context.getResources().getBoolean(R.bool.has_internet));
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();

                    offlineMode = true;

                    executors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            if(iCallback != null)
                                iCallback.onConnectionCallback(context.getResources().getBoolean(R.bool.no_internet));
                        }
                    });
                }
            }
        });
    }

}
