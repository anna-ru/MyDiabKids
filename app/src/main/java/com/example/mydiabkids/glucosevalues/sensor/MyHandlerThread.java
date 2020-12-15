package com.example.mydiabkids.glucosevalues.sensor;

import android.os.Handler;
import android.os.HandlerThread;

class MyHandlerThread extends HandlerThread {

    private Handler handler;

    public MyHandlerThread(String name) {
        super(name);
    }

    public void postTask(Runnable task){
        handler.post(task);
    }

    public void prepareHandler(){
        handler = new Handler(getLooper());
    }

}
