package com.example.mydiabkids.glucosevalues.sensor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.mydiabkids.R;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class StatisticsFragment extends Fragment {

    TextView dAvg, wAvg, mAvg, dMin, wMin, mMin, dMax, wMax, mMax;
    Button start;
    MyHandlerThread myHandlerThread;
    Handler mainHandler;
    private final DecimalFormat df = new DecimalFormat("#.#");
    private final ZoneId id = ZoneId.of("Europe/Budapest");

    public StatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        dAvg = view.findViewById(R.id.d_avg);
        dMin = view.findViewById(R.id.d_min);
        dMax = view.findViewById(R.id.d_max);
        wAvg = view.findViewById(R.id.w_avg);
        wMin = view.findViewById(R.id.w_min);
        wMax = view.findViewById(R.id.w_max);
        mAvg = view.findViewById(R.id.m_avg);
        mMin = view.findViewById(R.id.m_min);
        mMax = view.findViewById(R.id.m_max);
        start = view.findViewById(R.id.view_statistics_btn);

        myHandlerThread = new MyHandlerThread("Sensor statistics");
        myHandlerThread.start();
        myHandlerThread.prepareHandler();

        mainHandler = new Handler();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        start.setOnClickListener(view1 -> myHandlerThread.postTask(loadStatistics));
    }

    Runnable loadStatistics = new Runnable() {
        double dAvg_ = 0, dMin_=0, dMax_=0, wAvg_=0, wMin_=0, wMax_=0, mAvg_=0, mMin_=0, mMax_=0;

        @Override
        public void run() {
            char[] token = "3NJ0Z0T1NSsJVx3CYNBXFSki7hKZqiSguAa63oHmUNEHvOGd6urEIV99mTptcMnWHXAdku4ZNFfajiUwUDxMPg==".toCharArray();
            String bucket = "GlucoseValues";
            String org = "MyDiabKids";
            InfluxDBClient client = InfluxDBClientFactory.create("http://192.168.1.137:8086", token, org, bucket);

            Instant stopTime = Instant.now();
            Instant startTimeDay, startTimeWeek, startTimeMonth;
            long minusWeekTime, minusMonthTime;

            long minusDayTime = stopTime.atZone(id).getHour();
            startTimeDay = stopTime.minus(minusDayTime, ChronoUnit.HOURS);

            int today = stopTime.atZone(id).getDayOfWeek().getValue(); //Get the number of the day of the week
            minusWeekTime = minusDayTime + (today-1)*24;
            startTimeWeek = stopTime.minus(minusWeekTime, ChronoUnit.HOURS);

            int dayOfMonth = stopTime.atZone(id).getDayOfMonth();
            minusMonthTime = minusDayTime + (dayOfMonth-1)*24;
            startTimeMonth = stopTime.minus(minusMonthTime, ChronoUnit.HOURS);

            String queryDAvg = "from(bucket: \"GlucoseValues\") |> range(start: " + startTimeDay + ", stop: " + stopTime
                    + ") |> movingAverage(n: 288)";
            String queryDMax = "from(bucket: \"GlucoseValues\") |> range(start: " + startTimeDay + ", stop: " + stopTime + ") |> max()";
            String queryDMin = "from(bucket: \"GlucoseValues\") |> range(start: " + startTimeDay + ", stop: " + stopTime + ") |> min()";
            String queryWAvg = "from(bucket: \"GlucoseValues\") |> range(start: " + startTimeWeek + ", stop: "
                    + stopTime +") |> movingAverage(n: 2016)";
            String queryWMax = "from(bucket: \"GlucoseValues\") |> range(start: " + startTimeWeek + ", stop: " + stopTime +") |> max()";
            String queryWMin = "from(bucket: \"GlucoseValues\") |> range(start: " + startTimeWeek + ", stop: "  + stopTime +") |> min()";
            String queryMAvg = "from(bucket: \"GlucoseValues\") |> range(start: " + startTimeMonth + ", stop: " + stopTime +
            ") |> movingAverage(n: 8640)";
            String queryMMax = "from(bucket: \"GlucoseValues\") |> range(start: " + startTimeMonth + ", stop: " + stopTime + ") |> max()";
            String queryMMin = "from(bucket: \"GlucoseValues\") |> range(start: " + startTimeMonth + ", stop: " + stopTime + ") |> min()";
            try{
                QueryApi queryApi = client.getQueryApi();
                //Daily average, max, min
                List<FluxTable> tables = queryApi.query(queryDAvg);
                for (FluxTable fluxTable : tables) {
                    List<FluxRecord> records = fluxTable.getRecords();
                    for (FluxRecord fluxRecord : records) {
                        dAvg_ = (double) fluxRecord.getValueByKey("_value");
                    }
                }
                tables = queryApi.query(queryDMax);
                for (FluxTable fluxTable : tables) {
                    List<FluxRecord> records = fluxTable.getRecords();
                    for (FluxRecord fluxRecord : records) {
                        dMax_ = (double) fluxRecord.getValueByKey("_value");
                    }
                }
                tables = queryApi.query(queryDMin);
                for (FluxTable fluxTable : tables) {
                    List<FluxRecord> records = fluxTable.getRecords();
                    for (FluxRecord fluxRecord : records) {
                        dMin_ = (double) fluxRecord.getValueByKey("_value");
                    }
                }
                //Weekly average, max, min
                tables = queryApi.query(queryWAvg);
                for (FluxTable fluxTable : tables) {
                    List<FluxRecord> records = fluxTable.getRecords();
                    for (FluxRecord fluxRecord : records) {
                        wAvg_ = (double) fluxRecord.getValueByKey("_value");
                    }
                }
                tables = queryApi.query(queryWMax);
                for (FluxTable fluxTable : tables) {
                    List<FluxRecord> records = fluxTable.getRecords();
                    for (FluxRecord fluxRecord : records) {
                        wMax_ = (double) fluxRecord.getValueByKey("_value");
                    }
                }
                tables = queryApi.query(queryWMin);
                for (FluxTable fluxTable : tables) {
                    List<FluxRecord> records = fluxTable.getRecords();
                    for (FluxRecord fluxRecord : records) {
                        wMin_ = (double) fluxRecord.getValueByKey("_value");
                    }
                }
                //Monthly average, max, min
                tables = queryApi.query(queryMAvg);
                for (FluxTable fluxTable : tables) {
                    List<FluxRecord> records = fluxTable.getRecords();
                    for (FluxRecord fluxRecord : records) {
                        mAvg_ = (double) fluxRecord.getValueByKey("_value");
                    }
                }
                tables = queryApi.query(queryMMax);
                for (FluxTable fluxTable : tables) {
                    List<FluxRecord> records = fluxTable.getRecords();
                    for (FluxRecord fluxRecord : records) {
                        mMax_ = (double) fluxRecord.getValueByKey("_value");
                    }
                }
                tables = queryApi.query(queryMMin);
                for (FluxTable fluxTable : tables) {
                    List<FluxRecord> records = fluxTable.getRecords();
                    for (FluxRecord fluxRecord : records) {
                        mMin_ = (double) fluxRecord.getValueByKey("_value");
                    }
                }
            }catch (Exception e){
                Log.e("Statistics", e.getMessage());
            }
            mainHandler.post(() -> {
                dAvg.setText(df.format(dAvg_));
                dMin.setText(df.format(dMin_));
                dMax.setText(df.format(dMax_));
                wAvg.setText(df.format(wAvg_));
                wMin.setText(df.format(wMin_));
                wMax.setText(df.format(wMax_));
                mAvg.setText(df.format(mAvg_));
                mMin.setText(df.format(mMin_));
                mMax.setText(df.format(mMax_));
            });
        }
    };
}