package com.example.mydiabkids.glucosevalues.model;

import androidx.room.TypeConverter;

import com.example.mydiabkids.glucosevalues.model.Value;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

import kotlin.jvm.JvmStatic;

public class Converter {
    private Gson gson = new Gson();

    @TypeConverter
    @JvmStatic
    public List<Value> stringToSomeObjectList(String data){
        Type listType = new TypeToken<List<Value>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    @JvmStatic
    public String someObjectListToString(List<Value> list){
        return gson.toJson(list);
    }
}
