package com.example.bakingapp.data;

import androidx.room.TypeConverter;

import com.example.bakingapp.utils.NetworkUtils;

import java.util.ArrayList;

public class StepConverter {
    @TypeConverter
    public static ArrayList<StepData> toStepData(String stepString){
        return stepString == null ? null : NetworkUtils.ParseStepDataListFromJson(stepString);
    }

    @TypeConverter
    public static String toStepListString(ArrayList<StepData> steps){
        return steps == null ? null : NetworkUtils.StepDataListToJson(steps);
    }
}
