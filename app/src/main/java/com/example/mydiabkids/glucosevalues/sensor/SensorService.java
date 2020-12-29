package com.example.mydiabkids.glucosevalues.sensor;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.example.mydiabkids.MainActivity.CHANNEL_ID;
import static com.example.mydiabkids.settings.NotificationsFragment.highPrefStr;
import static com.example.mydiabkids.settings.NotificationsFragment.lowPrefStr;

public class SensorService extends Service {

    public static final String IP = "http://192.168.1.110";

    private final IBinder binder = new SensorBinder();
    public static AtomicBoolean isSensorRunning = new AtomicBoolean();
    public static final AtomicBoolean isInitialized = new AtomicBoolean();
    private InfluxDBClient client;

    private int currHour;
    private double currValue = 0.0;
    private double initValue = 0.0;
    private String currentDisplayedValue;
    private final ZoneId id = ZoneId.of("Europe/Budapest");
    private final DecimalFormat df = new DecimalFormat("#.#");
    private final MyHandlerThread myHandlerThread = new MyHandlerThread("SensorService");
    Context context;
    private final SensorInitialization initialization = SensorInitialization.getInstance();

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

    public boolean getIsRunning() {
        return isSensorRunning.get();
    }

    public class SensorBinder extends Binder{
        public SensorService getService(){
            return SensorService.this;
        }
    }

    public synchronized void startSensor(){
        Log.e("Service", "Sensor started");
        isInitialized.set(false);
        myHandlerThread.start();
        myHandlerThread.prepareHandler();
        myHandlerThread.postTask(sensor);
    }

    public void stopSensor(){
        isSensorRunning.set(false);
        myHandlerThread.removeCallback(sensor);
        myHandlerThread.quit();
    }

    private void buildNotification(int id){
        context = getApplicationContext();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Intent intent = new Intent(context, SensorFragment.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        switch (id){
            case 1:
                NotificationCompat.Builder lowBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.blood)
                        .setContentTitle("Alacsony vércukor: " + currentDisplayedValue + "!")
                        .setContentText("Ellenőrizd a vércukrod!")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true);
                notificationManager.notify(id, lowBuilder.build());
                break;
            case 2:
                NotificationCompat.Builder highBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.blood)
                        .setContentTitle("Magas vércukor: " + currentDisplayedValue + "!")
                        .setContentText("Ellenőrizd a vércukrod!")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true);

                notificationManager.notify(id, highBuilder.build());
                break;
        }
    }

    public void calibrate(double value){
        initValue = value;
        currValue = initValue;
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
            context = getApplicationContext();

            SharedPreferences lowPref, highPref;
            lowPref = context.getSharedPreferences(lowPrefStr, MODE_PRIVATE);
            highPref = context.getSharedPreferences(highPrefStr, MODE_PRIVATE);
            String lowValue = lowPref.getString(lowPrefStr, "0.0");
            String highValue = highPref.getString(highPrefStr, "0.0");
            double low, high;
            low = Double.parseDouble(lowValue);
            high = Double.parseDouble(highValue);

            isSensorRunning.set(true);
            client = InfluxDBClientFactory.create(IP + ":8086", token, org, bucket);
            try{
                writeApi = client.getWriteApi();
            } catch (Exception e){
                Log.e("InfluxDB", "WriteApi exception");
            }

            if(initValue < 1){
                String query = "from(bucket: \"GlucoseValues\") |> range(start: -10m) |> last()";
                try{
                    QueryApi queryApi = client.getQueryApi();
                    List<FluxTable> tables = queryApi.query(query);
                    for (FluxTable fluxTable : tables) {
                        List<FluxRecord> records = fluxTable.getRecords();
                        for (FluxRecord fluxRecord : records) {
                            initValue = (double) fluxRecord.getValueByKey("_value");
                        }
                    }
                    if(initValue >= 1) {
                        currValue = initValue;
                        setCurrentDisplayedValue(initValue);
                    }
                    initialization.setInitValue(initValue);
                    Log.e("Service", "initial value: " + initValue);
                }catch (Exception e){
                    Log.e("SensorService", e.getMessage());
                    isSensorRunning.set(false);
                    initialization.failedInitialization();
                }
                isInitialized.set(true);
            }

            initialization.waitForCalibration();

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
                    if(currValue <= low){
                        buildNotification(1);
                    } else if(currValue >= high){
                        buildNotification(2);
                    }

                }
                myHandlerThread.postDelayed(this, 10000);
            }
            client.close();
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


    public String getCurrentDisplayedValue(){
        return currentDisplayedValue;
    }

    private void setCurrentDisplayedValue(double value){
        currentDisplayedValue = df.format(value);
    }

    public void setCurrentValue(double value){
        currValue = value;
    }

    public double getCurrValue() {
        return currValue;
    }

    public double getInitValue() {
        return initValue;
    }

    public boolean getIsInitialized() {
        return isInitialized.get();
    }
}
