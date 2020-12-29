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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.mydiabkids.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.Context.MODE_PRIVATE;
import static com.example.mydiabkids.glucosevalues.sensor.SensorService.IP;
import static com.example.mydiabkids.glucosevalues.sensor.SensorService.isInitialized;
import static com.example.mydiabkids.settings.ThemeFragment.BLUE;
import static com.example.mydiabkids.settings.ThemeFragment.BROWN;
import static com.example.mydiabkids.settings.ThemeFragment.GREEN;
import static com.example.mydiabkids.settings.ThemeFragment.ORANGE;
import static com.example.mydiabkids.settings.ThemeFragment.PINK;
import static com.example.mydiabkids.settings.ThemeFragment.YELLOW;
import static com.example.mydiabkids.glucosevalues.sensor.SensorService.isSensorRunning;

public class SensorFragment extends Fragment {

   // private static final String SERVICE_BINDER_KEY = "SensorServiceBinder";
    ImageButton startBtn, calibrationBtn;
    Button valuesBtn, statisticsBtn;
    TextView currentValue;
    Context context;
    MyHandlerThread myHandlerThread;
    private Handler mainHandler;
    private final SensorInitialization init = SensorInitialization.getInstance();
    SensorService.SensorBinder binder;
    //private Bundle savedState = null;

    private String currentDisplayedValue;

    //private Timer timer = new Timer();
    private Runnable task;

    private SensorService mService;
    private boolean isBound = false;
    Intent serviceIntent;

    //SensorViewModel sensorViewModel;

    public SensorFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sensorViewModel = new ViewModelProvider(this).get(SensorViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e("SensorFragment", "oncreateview");
        View view = inflater.inflate(R.layout.fragment_sensor, container, false);

        context = getContext();
        startBtn = view.findViewById(R.id.start_sensor_btn);
        calibrationBtn = view.findViewById(R.id.calibration_btn);
        setButtonColors();

        valuesBtn = view.findViewById(R.id.sensor_values_btn);
        statisticsBtn = view.findViewById(R.id.statistics_btn);
        currentValue = view.findViewById(R.id.current_value);

        //observeCurrentValue();
        currentDisplayedValue = "Indítsd el a szenzort";

        currentValue.setText(currentDisplayedValue);
        serviceIntent = new Intent(context, SensorService.class);

        mainHandler = new Handler();

        task = new Runnable() {
            @Override
            public void run() {
                if(isSensorRunning.get()){
                    currentDisplayedValue = mService.getCurrentDisplayedValue();
                    mainHandler.post(() -> {
                        if(currentDisplayedValue == null || currentDisplayedValue.equals("0"))
                            currentValue.setText("Betöltés...");
                        else{
                            currentValue.setText(currentDisplayedValue);
                            Log.e("SensorFragment", "Display: " + currentDisplayedValue);
                        }
                       //observeCurrentValue();
                    });
                } else {
                    mainHandler.post(() -> {
                       Toast.makeText(context, "Hiba történt a szenzor csatlakoztatása közben", Toast.LENGTH_SHORT).show();
                       currentValue.setText("Hiba. Próbáld újra később.");
                    });
                }
                myHandlerThread.postDelayed(this, 10000);
            }
        };

        if(isSensorRunning.get()){
            if(mService == null){
                getActivity().startService(serviceIntent);
                getActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
            } else startSensor();
        }

        startBtn.setOnClickListener(view1 -> {
            getActivity().startService(serviceIntent);
            getActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
            //startSensor();
        });

        valuesBtn.setOnClickListener(view13 -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(IP + ":3000/d/Yp5ITa1Gz/mydiabkids"));
            startActivity(browserIntent);
        });

        statisticsBtn.setOnClickListener(Navigation.createNavigateOnClickListener
                (R.id.action_sensorFragment_to_statisticsFragment));

        calibrationBtn.setOnClickListener(view14 -> {
            if(isSensorRunning.get()){
                extraCalibration();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Kalibrálás nem elérhető")
                        .setMessage("A szenzor nem küld adatokat. A kalibráláshoz előbb el kell indítanod a szenzort.")
                        .setPositiveButton("Oké", (dialog, id) ->{
                            dialog.cancel();
                        })
                        .show();
            }
        });

        return view;
    }
/*
    public void observeCurrentValue(){
        sensorViewModel.getSensorDisplayStatus().observe(getViewLifecycleOwner(), new Observer<SensorDisplayStatus>() {
            @Override
            public void onChanged(SensorDisplayStatus sensorDisplayStatus) {
                startBtn.setEnabled(sensorDisplayStatus.btnStatus);
                currentValue.setText(sensorDisplayStatus.currValue);
            }
        });
    }
*/
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            binder = (SensorService.SensorBinder) service;
            mService = binder.getService();
            isBound = true;

            startSensor();
            Log.e("SensorFragment", "Bind service");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            //Log.e("SensorFragment", "Unbind service");
            isSensorRunning.set(false);
            isBound = false;
        }
    };

    private void startSensor(){
        boolean firstStart = false;
        startBtn.setEnabled(false);
        if(!isSensorRunning.get()){
            mService.startSensor();
            init.waitForInit();
            firstStart = true;
        }
        //sensorViewModel.startSensor();
        if(!isSensorRunning.get() && isInitialized.get()) {
            Toast.makeText(context, "Hiba történt a szenzor csatlakoztatása közben", Toast.LENGTH_SHORT).show();
            currentValue.setText("Hiba. Próbáld újra később.");
        } else {
            myHandlerThread = new MyHandlerThread("Current sensor value UI update");
            myHandlerThread.start();
            myHandlerThread.prepareHandler();
            //if(sensorViewModel.getInitializedStatus() && sensorViewModel.getInitValue() < 1){
            if (isInitialized.get() && mService.getInitValue() < 1) {
                //Calibration needed
                needCalibration();
                myHandlerThread.postTask(task);
            }
            //else if(sensorViewModel.getInitializedStatus() && sensorViewModel.getInitValue() >= 1){
            else if (isInitialized.get() && mService.getInitValue() >= 1) {
                myHandlerThread.postTask(task);
            }
            if(firstStart) Toast.makeText(context, "Szenzor start", Toast.LENGTH_SHORT).show();
        }
    }

    private void extraCalibration(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View calibrationView = inflater.inflate(R.layout.calibration_dialog, null);
        EditText editText = calibrationView.findViewById(R.id.calib_edit);

        builder.setView(calibrationView)
                .setTitle("Kalibrálás")
                .setPositiveButton(R.string.kesz, (dialog, id) -> {
                    String result = editText.getText().toString();
                    mService.setCurrentValue(Double.parseDouble(result));
                    //sensorViewModel.setCurrentValue(Double.parseDouble(result));
                    Toast.makeText(context, "Sikeres kalibrálás", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.megsem, (dialog, id) -> {
                    dialog.cancel();
                });

        final AlertDialog dialog = builder.create();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                final Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                okButton.setEnabled(!editText.getText().toString().isEmpty());
            }
        });
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private void needCalibration(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View calibrationView = inflater.inflate(R.layout.calibration_dialog, null);
        EditText editText = calibrationView.findViewById(R.id.calib_edit);

        builder.setView(calibrationView)
                .setTitle("Kalibrálás szükséges")
                .setPositiveButton(R.string.kesz, (dialog, id) -> {
                    String result = editText.getText().toString();
                    Double r = Double.parseDouble(result);
                    init.setInitValue(r);
                    //sensorViewModel.setInitValue(r);
                    mService.calibrate(r);
                    //sensorViewModel.calibrate(r);
                })
                .setCancelable(false);

        final AlertDialog dialog = builder.create();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                final Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                okButton.setEnabled(!editText.getText().toString().isEmpty());
            }
        });
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    @Override
    public void onDestroy() {
        //if(isBound){
          //  getActivity().unbindService(connection);
          //  getActivity().stopService(serviceIntent);
          //  isBound = false;
            if(myHandlerThread != null) myHandlerThread.quit();
            Log.e("SensorFragment", "onDestroy");
        //}
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

}