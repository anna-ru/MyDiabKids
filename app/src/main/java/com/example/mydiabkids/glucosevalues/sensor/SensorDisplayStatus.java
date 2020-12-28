package com.example.mydiabkids.glucosevalues.sensor;

public class SensorDisplayStatus {
    String currValue;
    boolean btnStatus;

    public SensorDisplayStatus(String currValue, boolean btnStatus){
        this.currValue = currValue;
        this.btnStatus = btnStatus;
    }
}
