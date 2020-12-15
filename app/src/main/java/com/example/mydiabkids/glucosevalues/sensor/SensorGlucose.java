package com.example.mydiabkids.glucosevalues.sensor;

import java.time.Instant;

public class SensorGlucose {
    private String tag;
    private double value;
    private Instant time;

    public SensorGlucose(String tag, double value, Instant time){
        this.tag = tag;
        this.value = value;
        this.time = time;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }
}
