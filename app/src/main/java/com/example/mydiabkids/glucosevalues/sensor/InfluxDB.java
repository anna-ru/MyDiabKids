package com.example.mydiabkids.glucosevalues.sensor;

import android.content.Context;
import android.widget.Toast;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.time.Instant;

public class InfluxDB {
    char[] token = "3NJ0Z0T1NSsJVx3CYNBXFSki7hKZqiSguAa63oHmUNEHvOGd6urEIV99mTptcMnWHXAdku4ZNFfajiUwUDxMPg==".toCharArray();
    String bucket = "GlucoseValues";
    String org = "MyDiabKids";
    InfluxDBClient client;

    public InfluxDB(){}

    public void startSensor(Context context){
        this.client = InfluxDBClientFactory.create("http://192.168.0.189:8086/", token, org, bucket);
        try (WriteApi writeApi = client.getWriteApi()) {
            Point point = Point.measurement("glucose value")
                    .addField("value", 5.6)
                    .time(Instant.now().toEpochMilli(), WritePrecision.MS);

            writeApi.writePoint(point);
            Toast.makeText(context, "Beírt érték", Toast.LENGTH_SHORT).show();
        }
        client.close();
    }

}
