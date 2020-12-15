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
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.example.mydiabkids.R;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SensorFragment extends Fragment {

    Button startBtn, stopBtn, valuesBtn;
    //WebView webView;
    Context context;
    MyHandlerThread myHandlerThread;
    private Handler mainHandler = new Handler();
    private Queue<SensorGlucose> buffer = new LinkedList<>();
    private AtomicBoolean running = new AtomicBoolean();

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
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Runnable sendToDB = new Runnable() {
            private final char[] token = "3NJ0Z0T1NSsJVx3CYNBXFSki7hKZqiSguAa63oHmUNEHvOGd6urEIV99mTptcMnWHXAdku4ZNFfajiUwUDxMPg==".toCharArray();
            private final String bucket = "GlucoseValues";
            private final String org = "MyDiabKids";
            private final String MEASUREMENT = "glucoseValue";
            private final String VALUE = "value";
            private final String TAG = "tag";

            private InfluxDBClient client;
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

                while (running.get()) {
                    demoSensor();
                    if(!buffer.isEmpty()){
                        SensorGlucose sensorGlucose = buffer.poll();
                        if(sensorGlucose != null){
                            Point point = Point.measurement(MEASUREMENT)
                                    .addField(VALUE, sensorGlucose.getValue())
                                    .addTag(TAG, sensorGlucose.getTag())
                                    .time(sensorGlucose.getTime(), WritePrecision.NS);

                            writeApi.writePoint(point);
                            Log.e("InfluxDB", "send value");

                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
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
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://snapshot.raintank.io/dashboard/snapshot/GtCazeQf7QPcnxtvp4fiPRK2umzU3iCF"));
            startActivity(browserIntent);
        });
    }

    public void sendGlValue(SensorGlucose sensorGlucose) {
        buffer.add(sensorGlucose);
        Log.e("InfluxDB", "write value " + buffer.size());
    }

    public void demoSensor(){
        //TODO: algorithm
        sendGlValue(new SensorGlucose("étkezés előtt", 6.7, Instant.now()));
    }

    @Override
    public void onDestroy() {
        myHandlerThread.quit();
        super.onDestroy();
    }

    public class MyHandlerThread extends HandlerThread {

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
}