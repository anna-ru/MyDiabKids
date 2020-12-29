package com.example.mydiabkids.glucosevalues.db;

import androidx.room.TypeConverter;

import com.example.mydiabkids.glucosevalues.model.GlucoseValueDetails;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

import kotlin.jvm.JvmStatic;

public class Converter {
    private Gson gson = new Gson();

    @TypeConverter
    @JvmStatic
    public List<GlucoseValueDetails> stringToSomeObjectList(String data){
        Type listType = new TypeToken<List<GlucoseValueDetails>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    @JvmStatic
    public String someObjectListToString(List<GlucoseValueDetails> list){
        return gson.toJson(list);
    }
}
