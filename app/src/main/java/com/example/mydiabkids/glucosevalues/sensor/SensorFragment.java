package com.example.mydiabkids.glucosevalues.sensor;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mydiabkids.R;

import java.text.BreakIterator;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;
import static com.example.mydiabkids.MainActivity.CHANNEL_ID;
import static com.example.mydiabkids.glucosevalues.sensor.SensorService.IP;
import static com.example.mydiabkids.settings.NotificationsFragment.highPrefStr;
import static com.example.mydiabkids.settings.NotificationsFragment.lowPrefStr;
import static com.example.mydiabkids.settings.ThemeFragment.BLUE;
import static com.example.mydiabkids.settings.ThemeFragment.BROWN;
import static com.example.mydiabkids.settings.ThemeFragment.GREEN;
import static com.example.mydiabkids.settings.ThemeFragment.ORANGE;
import static com.example.mydiabkids.settings.ThemeFragment.PINK;
import static com.example.mydiabkids.settings.ThemeFragment.YELLOW;
import static com.example.mydiabkids.glucosevalues.sensor.SensorService.isSensorRunning;

public class SensorFragment extends Fragment {

    ImageButton startBtn, stopBtn, calibrationBtn;
    Button valuesBtn, statisticsBtn;
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
        calibrationBtn = view.findViewById(R.id.calibration_btn);
        setButtonColors();

        valuesBtn = view.findViewById(R.id.sensor_values_btn);
        statisticsBtn = view.findViewById(R.id.statistics_btn);
        currentValue = view.findViewById(R.id.current_value);
        myHandlerThread = new MyHandlerThread("Current sensor value UI update");

        myHandlerThread.start();
        myHandlerThread.prepareHandler();
        currentDisplayedValue = "Indítsd el a szenzort";

        currentValue.setText(currentDisplayedValue);
        serviceIntent = new Intent(context, SensorService.class);

        if(isSensorRunning.get()){
            startBtn.setEnabled(false);
            getActivity().startService(serviceIntent);
            getActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
            if(isBound) {
                currentDisplayedValue = mService.getCurrentDisplayedValue();
                currentValue.setText(currentDisplayedValue);
                Log.e("SensorFragment", "sensor is running without start button");
            }

        }

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
                if(isSensorRunning.get()){
                    currentDisplayedValue = mService.getCurrentDisplayedValue();
                    mainHandler.post(() -> {
                        if(currentDisplayedValue == null || currentDisplayedValue.equals("0"))
                            currentValue.setText("Betöltés...");
                        else{
                            currentValue.setText(currentDisplayedValue);
                            Log.e("SensorFragment", "Display: " + currentDisplayedValue);
                        }
                    });
                } else {
                    mainHandler.post(() -> {
                       Toast.makeText(context, "Hiba történt a szenzor csatlakoztatása közben", Toast.LENGTH_SHORT).show();
                       currentValue.setText("Hiba. Próbáld újra később.");
                    });
                }

            }
        };

        startBtn.setOnClickListener(view1 -> {
            getActivity().startService(serviceIntent);
            getActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

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
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SensorService.SensorBinder binder = (SensorService.SensorBinder) service;
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
        startBtn.setEnabled(false);
        //stopBtn.setEnabled(true);
        if(!isSensorRunning.get()){
            mService.startSensor();
            Toast.makeText(context, "Szenzor start", Toast.LENGTH_SHORT).show();

            while(!mService.getIsInitialized()){
                //Wait for initialization
            }
        }
        if(mService.getIsInitialized() && mService.getInitValue() < 1){
            //Calibration needed
            needCalibration();
            timer.schedule(timerTask, 500, 10000);
        } else if (mService.getIsInitialized() && mService.getInitValue() >= 1){
            timer.schedule(timerTask, 500, 10000);
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
                    mService.calibrate(Double.parseDouble(result));
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
        //myHandlerThread.quit();
        //isSensorRunning.set(false);
        if(isBound){
            getActivity().unbindService(connection);
            getActivity().stopService(serviceIntent);
            isBound = false;
            Log.e("SensorFragment", "onDestroy");
        }
        //timer.cancel();
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