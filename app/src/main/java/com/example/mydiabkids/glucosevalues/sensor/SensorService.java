package com.example.mydiabkids.glucosevalues.sensor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

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
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class SensorService extends Service {

    private final IBinder binder = new SensorBinder();
    public static AtomicBoolean isSensorRunning = new AtomicBoolean();
    private InfluxDBClient client;

    private int currHour;
    private double currValue = 0.0;
    private String currentDisplayedValue;
    private final ZoneId id = ZoneId.of("Europe/Budapest");
    private final DecimalFormat df = new DecimalFormat("#.#");
    private MyHandlerThread myHandlerThread = new MyHandlerThread("SensorService");

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Service", "Bind");
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Service", "Service started");
        return super.onStartCommand(intent, flags, startId);
    }

    public class SensorBinder extends Binder{
        public SensorService getService(){
            return SensorService.this;
        }
    }

    public synchronized void startSensor(){
        Log.e("Service", "Sensor started");
        myHandlerThread.start();
        myHandlerThread.prepareHandler();
        myHandlerThread.postTask(sensor);
    }

    public String getCurrentDisplayedValue(){
        return currentDisplayedValue;
    }

    private void setCurrentDisplayedValue(double value){
        currentDisplayedValue = df.format(value);
    }

    public void setCurrentValue(double value){
        currValue = value;
    }

    public void stopSensor(){
        isSensorRunning.set(false);
        client.close();
        myHandlerThread.removeCallback(sensor);
        myHandlerThread.quit();
    }

    private final Runnable sensor = new Runnable() {
         final char[] token = "3NJ0Z0T1NSsJVx3CYNBXFSki7hKZqiSguAa63oHmUNEHvOGd6urEIV99mTptcMnWHXAdku4ZNFfajiUwUDxMPg==".toCharArray();
         final String bucket = "GlucoseValues";
         final String org = "MyDiabKids";
         final String MEASUREMENT = "glucoseValue";
         final String VALUE = "value";
         final String TAG = "tag";

         WriteApi writeApi = null;

        @Override
        public void run() {
            isSensorRunning.set(true);
            client = InfluxDBClientFactory.create("http://192.168.1.137:8086", token, org, bucket);
            try{
                writeApi = client.getWriteApi();
            } catch (Exception e){
                Log.e("InfluxDB", "WriteApi exception");
            }

            if(currValue == 0.0){
                String query = "from(bucket: \"GlucoseValues\") |> range(start: -10m) |> last()";
                try{
                    QueryApi queryApi = client.getQueryApi();
                    List<FluxTable> tables = queryApi.query(query);
                    for (FluxTable fluxTable : tables) {
                        List<FluxRecord> records = fluxTable.getRecords();
                        for (FluxRecord fluxRecord : records) {
                            currValue = (double) fluxRecord.getValueByKey("_value");
                            Log.e("Service", "initial value: " + currValue);
                        }
                    }
                    setCurrentDisplayedValue(currValue);
                }catch (Exception e){
                    Log.e("SensorService", e.getMessage());
                    isSensorRunning.set(false);
                }

            }

            if(isSensorRunning.get()) {
                //Get current hour
                currHour = Instant.now().atZone(id).getHour();
                generateSensorData();
                if(currValue != 0){
                    Point point = Point.measurement(MEASUREMENT)
                            .addField(VALUE, currValue)
                            .addTag(TAG, "szenzor glükóz")
                            .time(Instant.now(), WritePrecision.NS);

                    writeApi.writePoint(point);
                    Log.e("InfluxDB", "send value: " + currValue);

                    //Get the latest value and write it to the UI
                    setCurrentDisplayedValue(currValue);

                }
                //Sending values every minute
                /*try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    client.close();
                    isSensorRunning.set(false);
                    Thread.currentThread().interrupt();
                    return;
                }*/
                myHandlerThread.postDelayed(this, 10000);
            }

        }
    };

    //Generate values according to daily meals
    public void generateSensorData(){
        //night - stable glucose values
        if(currHour < 3){
            double d = 0.001 + new Random().nextDouble() * (0.01 - 0.001);
            currValue -= d;
        }
        //before breakfast - descending values
        else if(currHour >= 3 && currHour < 7){
            double d = 0.001 + new Random().nextDouble() * (0.03 - 0.001);
            currValue -= d;
        }
        //after breakfast or lunch or dinner - ascending values
        else if((currHour >= 7 && currHour < 9) || (currHour >= 13 && currHour < 15) || (currHour >= 19 && currHour < 21)) {
            double d = 0.01 + new Random().nextDouble() * (0.1 - 0.01);
            currValue += d;
        }
        //before snack or lunch or afternoon snack or dinner or go to bed - light desc values
        else if ((currHour >= 9 && currHour < 11) || currHour == 12 || currHour == 15 || currHour == 18 || currHour == 21) {
            double d = 0.01 + new Random().nextDouble() * (0.05 - 0.01);
            currValue -= d;
        }
        //after snacks - asc values
        else if (currHour == 11 || (currHour >= 16 && currHour < 18) || currHour >= 22) {
            double d = 0.005 + new Random().nextDouble() * (0.1 - 0.005);
            currValue += d;
        }
        Log.e("Sensor data", "Generated value: " + currValue);
    }
}
