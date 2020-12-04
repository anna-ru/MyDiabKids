package com.example.mydiabkids.glucosevalues.sensor;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.mydiabkids.R;

public class SensorFragment extends Fragment {

    Button startBtn;
    InfluxDB influxDB;
    Context context;

    public SensorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor, container, false);
        context = getContext();
        startBtn = view.findViewById(R.id.start_sensor_btn);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //influxDB = new InfluxDB();
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //influxDB.startSensor(context);
                //Toast.makeText(context, "Szenzor start", Toast.LENGTH_SHORT).show();
            }
        });
    }
}