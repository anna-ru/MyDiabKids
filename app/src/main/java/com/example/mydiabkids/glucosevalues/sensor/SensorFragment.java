package com.example.mydiabkids.glucosevalues.sensor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.Context.MODE_PRIVATE;
import static com.example.mydiabkids.ThemeFragment.BLUE;
import static com.example.mydiabkids.ThemeFragment.BROWN;
import static com.example.mydiabkids.ThemeFragment.GREEN;
import static com.example.mydiabkids.ThemeFragment.ORANGE;
import static com.example.mydiabkids.ThemeFragment.PINK;
import static com.example.mydiabkids.ThemeFragment.YELLOW;

public class SensorFragment extends Fragment {

    ImageButton startBtn, stopBtn;
    Button valuesBtn;
    //WebView webView;
    TextView currentValue;
    Context context;
    MyHandlerThread myHandlerThread;
    private Handler mainHandler = new Handler();
    private Queue<Double> buffer = new LinkedList<>();
    private AtomicBoolean running = new AtomicBoolean();
    private InfluxDBClient client;

    private int currHour;
    private double currValue;
    private String currentDisplayedValue = "Nincs kapcsolat a szenzorral";
    ZoneId id = ZoneId.of("Europe/Budapest");
    DecimalFormat df = new DecimalFormat("#.#");

    public SensorFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor, container, false);
        context = getContext();
        startBtn = view.findViewById(R.id.start_sensor_btn);
        stopBtn = view.findViewById(R.id.stop_sensor_btn);
        setButtonColors();
        /*webView = view.findViewById(R.id.graph);
        webView.getSettings().setJavaScriptEnabled(true);
        String html = "<iframe src=\"http://192.168.1.110:3000/d-solo/Yp5ITa1Gz/mydiabkids?orgId=1&refresh=1m&from=1608009919328&to=1608031519328&panelId=2\" width=\"350\" height=\"200\" frameborder=\"0\"></iframe>";
        //webView.loadData(html, "text/html", "UTF-8");
        webView.loadUrl("https://snapshot.raintank.io/dashboard/snapshot/GtCazeQf7QPcnxtvp4fiPRK2umzU3iCF");
        //webView.setWebViewClient(new WebViewClient());*/
        valuesBtn = view.findViewById(R.id.sensor_values_btn);
        myHandlerThread = new MyHandlerThread("SensorDemo");
        currentValue = view.findViewById(R.id.current_value);

        currentValue.setText(currentDisplayedValue);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currHour = Instant.now().atZone(id).getHour();

        myHandlerThread.start();
        myHandlerThread.prepareHandler();

        startBtn.setOnClickListener(view1 -> {
            startBtn.setEnabled(false);
            myHandlerThread.postTask(sendToDB);
            Toast.makeText(context, "Szenzor start", Toast.LENGTH_SHORT).show();
        });

        stopBtn.setOnClickListener(view12 -> {
            running.set(false);
            startBtn.setEnabled(true);
            Toast.makeText(context, "Szenzor stop", Toast.LENGTH_SHORT).show();
        });

        valuesBtn.setOnClickListener(view13 -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://192.168.1.110:3000/goto/Kdx11E1Gk"));
            startActivity(browserIntent);
        });
    }

    private final Runnable sendToDB = new Runnable() {
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

            //Get the latest value from the database
            String query = "from(bucket: \"GlucoseValues\") |> range(start: -24h) |> last()";
            QueryApi queryApi = client.getQueryApi();
            List<FluxTable> tables = queryApi.query(query);
            for (FluxTable fluxTable : tables) {
                List<FluxRecord> records = fluxTable.getRecords();
                for (FluxRecord fluxRecord : records) {
                    currValue = (double) fluxRecord.getValueByKey("_value");
                    currentDisplayedValue = df.format(currValue);
                }
            }
            mainHandler.post(() -> {
                currentDisplayedValue = df.format(currValue);
                currentValue.setText(currentDisplayedValue);
            });

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
                        mainHandler.post(() -> {
                            currentDisplayedValue = df.format(sensorGlucose);
                            currentValue.setText(currentDisplayedValue);
                            //Toast.makeText(context, "Main thread update", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
                //Sending values every minute
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            client.close();
        }
    };

    //Generate values according to daily meals
    public void generateSensorData(){
        //night - stable glucose values
        if(currHour < 3){
            for(int i=0; i<60;++i){
                double d = 0.001 + new Random().nextDouble() * (0.01 - 0.001);
                currValue -= d;
                buffer.add(currValue);
                Log.e("InfluxDB", "Hour <3; write value: " + (currValue));
            }
        }
        //before breakfast - descending values
        else if(currHour >= 3 && currHour < 7){
            for(int i=0; i<60;++i){
                double d = 0.001 + new Random().nextDouble() * (0.03 - 0.001);
                currValue -= d;
                buffer.add(currValue);
                Log.e("InfluxDB", "3<=hour<7; write value: " + (currValue));
            }
        }
        //after breakfast or lunch or dinner - ascending values
        else if((currHour >= 7 && currHour < 9) || (currHour >= 13 && currHour < 15) || (currHour >= 19 && currHour < 21)) {
            for (int i = 0; i < 60; ++i) {
                double d = 0.01 + new Random().nextDouble() * (0.1 - 0.01);
                currValue += d;
                buffer.add(currValue);
                Log.e("InfluxDB", "after meals; write value: " + (currValue));
            }
        }
        //before snack or lunch or afternoon snack or dinner or go to bed - light desc values
        else if ((currHour >= 9 && currHour < 11) || currHour == 12 || currHour == 15 || currHour == 18 || currHour == 21) {
            for (int i = 0; i < 60; ++i) {
                double d = 0.01 + new Random().nextDouble() * (0.05 - 0.01);
                currValue -= d;
                buffer.add(currValue);
                Log.e("InfluxDB", "before snacks, or meals; write value: " + (currValue));
            }
        }
        //after snacks - asc values
        else if (currHour == 11 || (currHour >= 16 && currHour < 18) || currHour >= 22) {
            for (int i = 0; i < 60; ++i) {
                double d = 0.005 + new Random().nextDouble() * (0.1 - 0.005);
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