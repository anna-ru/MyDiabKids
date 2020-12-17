package com.example.mydiabkids.glucosevalues.sensor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mydiabkids.R;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import java.text.DecimalFormat;
import java.time.ZoneId;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;
import static com.example.mydiabkids.ThemeFragment.BLUE;
import static com.example.mydiabkids.ThemeFragment.BROWN;
import static com.example.mydiabkids.ThemeFragment.GREEN;
import static com.example.mydiabkids.ThemeFragment.ORANGE;
import static com.example.mydiabkids.ThemeFragment.PINK;
import static com.example.mydiabkids.ThemeFragment.YELLOW;
import static com.example.mydiabkids.glucosevalues.sensor.SensorService.isSensorRunning;

public class SensorFragment extends Fragment {

    ImageButton startBtn, stopBtn;
    Button valuesBtn;
    TextView currentValue;
    Context context;
    MyHandlerThread myHandlerThread;
    private Handler mainHandler;

    //private double currValue;
    private String currentDisplayedValue;

    private Timer timer = new Timer();
    private TimerTask timerTask;

    private SensorService mService;
    private boolean isBound = false;
    Intent serviceIntent;

    public SensorFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor, container, false);
        context = getContext();
        startBtn = view.findViewById(R.id.start_sensor_btn);
        //stopBtn = view.findViewById(R.id.stop_sensor_btn);
        setButtonColors();

        valuesBtn = view.findViewById(R.id.sensor_values_btn);
        currentValue = view.findViewById(R.id.current_value);
        myHandlerThread = new MyHandlerThread("Current sensor value UI update");

        myHandlerThread.start();
        myHandlerThread.prepareHandler();
        currentDisplayedValue = "Indítsd el a szenzort";

        currentValue.setText(currentDisplayedValue);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //stopBtn.setEnabled(false);
        mainHandler = new Handler();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                currentDisplayedValue = mService.getCurrentDisplayedValue();
                mainHandler.post(() -> {
                    currentValue.setText(currentDisplayedValue);
                    Log.e("SensorFragment", "Display: " + currentDisplayedValue);
                });
            }
        };

        startBtn.setOnClickListener(view1 -> {
            serviceIntent = new Intent(context, SensorService.class);
            getActivity().startService(serviceIntent);
            getActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
            Toast.makeText(context, "Szenzor start", Toast.LENGTH_SHORT).show();
        });

        /*
        stopBtn.setOnClickListener(view12 -> {
            isSensorRunning.set(false);
            mService.stopSensor();
            getActivity().stopService(serviceIntent);
            getActivity().unbindService(connection);
            isBound = false;

            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
            currentValue.setText("Indítsd el a szenzort");
            timer.cancel();
            Toast.makeText(context, "Szenzor stop", Toast.LENGTH_SHORT).show();
        });
*/
        valuesBtn.setOnClickListener(view13 -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://192.168.1.137:3000/goto/lY5w3X1Mz"));
            startActivity(browserIntent);
        });
    }

    @Override
    public void onStart() {
        if(isSensorRunning.get()){
            serviceIntent = new Intent(context, SensorService.class);
            getActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
            Log.e("SensorFragment", "sensor is running without start button");
        }
        super.onStart();
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SensorService.SensorBinder binder = (SensorService.SensorBinder) service;
            mService = binder.getService();
            isBound = true;

            startSensor();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isSensorRunning.set(false);
            isBound = false;
        }
    };

    private void startSensor(){
        startBtn.setEnabled(false);
        //stopBtn.setEnabled(true);
        if(!isSensorRunning.get()){
            mService.startSensor();
        }
        timer.schedule(timerTask, 500, 10000);
    }

    @Override
    public void onDestroy() {
        //myHandlerThread.quit();
        //isSensorRunning.set(false);
        if(isBound){
            getActivity().unbindService(connection);
            isBound = false;
        }
        timer.cancel();
        Log.e("SensorFragment", "onDestroy");
        super.onDestroy();
    }

    public void setButtonColors(){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("com.example.mydiabkids.THEME", MODE_PRIVATE);
        int theme = sharedPreferences.getInt("com.example.mydiabkids.THEME", PINK);
        switch (theme){
            case PINK:
                startBtn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.pink_card_view_colors));
                break;
            case BLUE:
                startBtn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.blue_card_view_colors));
                break;
            case GREEN:
                startBtn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.green_card_view_colors));
                break;
            case YELLOW:
                startBtn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.yellow_card_view_colors));
                break;
            case ORANGE:
                startBtn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.orange_card_view_colors));
                break;
            case BROWN:
                startBtn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.brown_card_view_colors));
                break;
        }
    }

    //TODO: ha újra indul az activity, akkor nem jelenik meg a szenzor érték
}