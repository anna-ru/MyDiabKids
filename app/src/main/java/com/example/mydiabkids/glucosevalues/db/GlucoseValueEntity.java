package com.example.mydiabkids.glucosevalues.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.mydiabkids.glucosevalues.model.Converter;
import com.example.mydiabkids.glucosevalues.model.GlucoseValueDetails;

import java.util.List;


@Entity(tableName = "values_table")
@TypeConverters(Converter.class)
public class GlucoseValueEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "values")
    private List<GlucoseValueDetails> glucoseValues;

    public GlucoseValueEntity(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<GlucoseValueDetails> getGlucoseValues() {
        return glucoseValues;
    }

    public void setGlucoseValues(List<GlucoseValueDetails> glucoseValues) {
        this.glucoseValues = glucoseValues;
    }

}

