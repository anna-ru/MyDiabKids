package com.example.mydiabkids.glucosevalues.sensor;

import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.example.mydiabkids.glucosevalues.sensor.SensorService.isInitialized;

public class SensorInitialization {

    private static SensorInitialization INSTANCE;
    private double initValue = 0.0;

    private SensorInitialization(){}

    public synchronized static SensorInitialization getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SensorInitialization();
        }
        return INSTANCE;
    }

    public synchronized void waitForInit(){
        while (!isInitialized.get()) {
            try {
                wait();
                Log.e("SensorInit", "waitForInit");
            } catch (InterruptedException e)  {
                Thread.currentThread().interrupt();
                Log.e("Thread interrupted", e.getMessage());
            }
        }

        notifyAll();
    }

    public synchronized void waitForCalibration() {
        while (initValue < 1) {
            try {
                wait();
            } catch (InterruptedException e)  {
                Thread.currentThread().interrupt();
                Log.e("Thread interrupted", e.getMessage());
            }
        }

        notifyAll();
    }

    public synchronized void setInitValue(double initValue) {
        this.initValue = initValue;
        notifyAll();
    }

    public synchronized void failedInitialization() {
        Log.e("Service", "Failed to init");
        notifyAll();
    }
}
