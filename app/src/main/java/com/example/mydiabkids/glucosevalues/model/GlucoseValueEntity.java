package com.example.mydiabkids.glucosevalues.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;


@Entity(tableName = "values_table")
@TypeConverters(Converter.class)
public class GlucoseValueEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "values")
    private List<Value> glucoseValues;

    public GlucoseValueEntity(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Value> getGlucoseValues() {
        return glucoseValues;
    }

    public void setGlucoseValues(List<Value> glucoseValues) {
        this.glucoseValues = glucoseValues;
    }

}

