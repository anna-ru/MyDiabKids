package com.example.mydiabkids.glucosevalues.sensor;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

interface ISensorRepository{
    LiveData<SensorDisplayStatus> getSensorDisplayStatus();
    void startSensor();
    boolean getInitializedStatus();
    double getInitValue();
    void calibrate(double value);
    void setCurrentValue(double value);
}

public class SensorRepository implements ISensorRepository{
    LiveData<SensorDisplayStatus> sensorDisplayStatus;
    private SensorService mService;
    private boolean isBound = false;
    SensorService.SensorBinder binder;
    private final SensorInitialization init = SensorInitialization.getInstance();

    public SensorRepository(Application application){
        Context context = application.getApplicationContext();
        Intent serviceIntent = new Intent(context, SensorService.class);
        context.startService(serviceIntent);
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        if(isBound){
            Log.e("SensorRepository", "Constructor");
            sensorDisplayStatus = getSensorDisplayStatus();
        }
    }

    @Override
    public LiveData<SensorDisplayStatus> getSensorDisplayStatus() {
        if (sensorDisplayStatus != null) {
            if(mService.getIsRunning()) {
                if (!sensorDisplayStatus.getValue().currValue.equals(mService.getCurrentDisplayedValue())) {
                    String value = mService.getCurrentDisplayedValue();
                    SensorDisplayStatus displayStatus = new SensorDisplayStatus(value, false);
                    ((MutableLiveData<SensorDisplayStatus>) sensorDisplayStatus).setValue(displayStatus);
                }
                return sensorDisplayStatus;
            }
        }

        String value = "Indítsd el a szenzort";
        boolean status = true;
        sensorDisplayStatus = new MutableLiveData<>();
        if (mService.getIsRunning()) {
            value = mService.getCurrentDisplayedValue();
            status = false;
        }
        SensorDisplayStatus displayStatus = new SensorDisplayStatus(value, status);
        ((MutableLiveData<SensorDisplayStatus>) sensorDisplayStatus).setValue(displayStatus);
        return sensorDisplayStatus;
    }

    @Override
    public void startSensor(){
        if(!mService.getIsRunning()){
            mService.startSensor();
            init.waitForInit();
        }
    }

    @Override
    public boolean getInitializedStatus() {
        return mService.getIsInitialized();
    }

    @Override
    public double getInitValue() {
        return mService.getInitValue();
    }

    public void setInitValue(double value) {
        init.setInitValue(value);
    }

    @Override
    public void calibrate(double value) {
        mService.calibrate(value);
    }

    @Override
    public void setCurrentValue(double value) {
        mService.setCurrentValue(value);
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            binder = (SensorService.SensorBinder) service;
            mService = binder.getService();
            isBound = true;

            Log.e("SensorRepository", "Bind service");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e("SensorRepository", "Unbind service");
            //isSensorRunning.set(false);
            isBound = false;
        }
    };
}
