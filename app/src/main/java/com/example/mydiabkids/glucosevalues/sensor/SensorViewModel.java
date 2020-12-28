package com.example.mydiabkids.glucosevalues.sensor;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class SensorViewModel extends AndroidViewModel {

    SensorRepository repository;
    LiveData<SensorDisplayStatus> sensorDisplayStatus;

    public SensorViewModel(Application application){
        super(application);
        repository = new SensorRepository(application);
        //sensorDisplayStatus = repository.getSensorDisplayStatus();
    }

    public LiveData<SensorDisplayStatus> getSensorDisplayStatus(){
        return repository.getSensorDisplayStatus();
    }

    public void startSensor(){
        repository.startSensor();
    }

    public boolean getInitializedStatus(){
        return repository.getInitializedStatus();
    }

    public double getInitValue(){
        return repository.getInitValue();
    }

    public void setInitValue(double value) {
        repository.setInitValue(value);
    }

    public void calibrate(double value) {
        repository.calibrate(value);
    }

    public void setCurrentValue(double value) {
        repository.setCurrentValue(value);
    }

}
