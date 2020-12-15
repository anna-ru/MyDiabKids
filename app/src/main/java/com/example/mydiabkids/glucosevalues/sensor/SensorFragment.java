package com.example.mydiabkids.glucosevalues.sensor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mydiabkids.R;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class SensorFragment extends Fragment {

    Button startBtn, stopBtn, valuesBtn;
    //WebView webView;
    TextView currentValue;
    Context context;
    MyHandlerThread myHandlerThread;
    private Handler mainHandler = new Handler();
    private Queue<Double> buffer = new LinkedList<>();
    private AtomicBoolean running = new AtomicBoolean();
    private InfluxDBClient client;

    private int currHour;
    private double currValue = 5.2;
    private double currentDisplayedValue = 0;

    public SensorFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor, container, false);
        context = getContext();
        startBtn = view.findViewById(R.id.start_sensor_btn);
        stopBtn = view.findViewById(R.id.stop_sensor_btn);
        /*webView = view.findViewById(R.id.graph);
        webView.getSettings().setJavaScriptEnabled(true);
        String html = "<iframe src=\"http://192.168.1.110:3000/d-solo/Yp5ITa1Gz/mydiabkids?orgId=1&refresh=1m&from=1608009919328&to=1608031519328&panelId=2\" width=\"350\" height=\"200\" frameborder=\"0\"></iframe>";
        //webView.loadData(html, "text/html", "UTF-8");
        webView.loadUrl("https://snapshot.raintank.io/dashboard/snapshot/GtCazeQf7QPcnxtvp4fiPRK2umzU3iCF");
        //webView.setWebViewClient(new WebViewClient());*/
        valuesBtn = view.findViewById(R.id.sensor_values_btn);
        myHandlerThread = new MyHandlerThread("SensorDemo");
        currentValue = view.findViewById(R.id.current_value);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ZoneId id = ZoneId.of("Europe/Budapest");
        currHour = Instant.now().atZone(id).getHour();
        Runnable sendToDB = new Runnable() {
            private final char[] token = "3NJ0Z0T1NSsJVx3CYNBXFSki7hKZqiSguAa63oHmUNEHvOGd6urEIV99mTptcMnWHXAdku4ZNFfajiUwUDxMPg==".toCharArray();
            private final String bucket = "GlucoseValues";
            private final String org = "MyDiabKids";
            private final String MEASUREMENT = "glucoseValue";
            private final String VALUE = "value";
            private final String TAG = "tag";

            private WriteApi writeApi;

            @Override
            public void run() {
                running.set(true);
                client = InfluxDBClientFactory.create("http://192.168.1.110:8086", token, org, bucket);
                try{
                    writeApi = client.getWriteApi();
                } catch (Exception e){
                    Log.e("InfluxDB", "WriteApi exception");
                }

                /*
                String query = "from(bucket: \"GlucoseValues\") |> range(start: -1m) |> first()";
                QueryApi queryApi = client.getQueryApi();
                List<FluxTable> tables = queryApi.query(query);
                for (FluxTable fluxTable : tables) {
                    List<FluxRecord> records = fluxTable.getRecords();
                    for (FluxRecord fluxRecord : records) {
                        currentDisplayedValue = (double) fluxRecord.getValueByKey("_value");
                    }
                }*/

                while (running.get()) {
                    //Get current hour
                    if(currHour != Instant.now().atZone(id).getHour() || buffer.isEmpty()){
                        currHour = Instant.now().atZone(id).getHour();
                        generateSensorData();
                    }
                    if(!buffer.isEmpty()){
                        //Write data to InfluxDB
                        double sensorGlucose = buffer.poll();
                        if(sensorGlucose != 0){
                            Point point = Point.measurement(MEASUREMENT)
                                    .addField(VALUE, sensorGlucose)
                                    .addTag(TAG, "szenzor glükóz")
                                    .time(Instant.now(), WritePrecision.NS);

                            writeApi.writePoint(point);
                            Log.e("InfluxDB", "send value");

                            //Get the latest value and write it to the UI
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    currentDisplayedValue = Math.round(sensorGlucose * 10) / 10.0;
                                    currentValue.setText(String.valueOf(currentDisplayedValue));
                                    Toast.makeText(context, "Main thread update", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    //Sending values every minute
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                client.close();
            }
        };

        myHandlerThread.start();
        myHandlerThread.prepareHandler();

        startBtn.setOnClickListener(view1 -> {
            myHandlerThread.postTask(sendToDB);
            Toast.makeText(context, "Szenzor start", Toast.LENGTH_SHORT).show();
        });

        stopBtn.setOnClickListener(view12 -> {
            running.set(false);
            Toast.makeText(context, "Szenzor stop", Toast.LENGTH_SHORT).show();
        });

        valuesBtn.setOnClickListener(view13 -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://192.168.1.110:3000/goto/Kdx11E1Gk"));
            startActivity(browserIntent);
        });
    }

    //Generate values according to daily meals
    //TODO: get more realistic data
    public void generateSensorData(){
        //night - stable glucose values
        if(currHour < 3){
            for(int i=0; i<60;++i){
                double d = 0.1 + new Random().nextDouble() * (0.2 - 0.1);
                currValue -= d;
                buffer.add(currValue);
                Log.e("InfluxDB", "Hour <3; write value: " + (currValue));
            }
        }
        //before breakfast - descending values
        else if(currHour >= 3 && currHour < 7){
            for(int i=0; i<60;++i){
                double d = 0.1 + new Random().nextDouble() * (0.5 - 0.1);
                currValue -= d;
                buffer.add(currValue);
                Log.e("InfluxDB", "3<=hour<7; write value: " + (currValue));
            }
        }
        //after breakfast or lunch or dinner - ascending values
        else if((currHour >= 7 && currHour < 9) || (currHour >= 13 && currHour < 15) || (currHour >= 19 && currHour < 21)) {
            for (int i = 0; i < 60; ++i) {
                double d = 0.1 + new Random().nextDouble() * (0.5 - 0.1);
                currValue += d;
                buffer.add(currValue);
                Log.e("InfluxDB", "after meals; write value: " + (currValue));
            }
        }
        //before snack or lunch or afternoon snack or dinner or go to bed - light desc values
        else if ((currHour >= 9 && currHour < 11) || currHour == 12 || currHour == 15 || currHour == 18 || currHour == 21) {
            for (int i = 0; i < 60; ++i) {
                double d = 0.1 + new Random().nextDouble() * (0.3 - 0.1);
                currValue -= d;
                buffer.add(currValue);
                Log.e("InfluxDB", "before snacks, or meals; write value: " + (currValue));
            }
        }
        //after snacks - asc values
        else if (currHour == 11 || (currHour >= 16 && currHour < 18) || currHour >= 22) {
            for (int i = 0; i < 60; ++i) {
                double d = 0.1 + new Random().nextDouble() * (0.3 - 0.1);
                currValue += d;
                buffer.add(currValue);
                Log.e("InfluxDB", "after snacks; write value: " + (currValue));
            }
        }
    }

    @Override
    public void onDestroy() {
        myHandlerThread.quit();
        super.onDestroy();
    }
}