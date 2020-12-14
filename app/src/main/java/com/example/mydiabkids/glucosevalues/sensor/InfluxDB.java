package com.example.mydiabkids.glucosevalues.sensor;

import android.content.Context;
import android.widget.Toast;

import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.InfluxException;

import java.time.Instant;
import java.time.OffsetDateTime;


public class InfluxDB extends Thread{
    char[] token = "3NJ0Z0T1NSsJVx3CYNBXFSki7hKZqiSguAa63oHmUNEHvOGd6urEIV99mTptcMnWHXAdku4ZNFfajiUwUDxMPg==".toCharArray();
    String bucket = "GlucoseValues";
    String org = "MyDiabKids";
    InfluxDBClient client;
    //float value = (float) 5.6;
    Context context;

    public InfluxDB(Context context){this.context = context;}

    @Override
    public void run() {
        this.client = InfluxDBClientFactory.create("http://192.168.1.110:8086", token, org, bucket);
        try (WriteApi writeApi = client.getWriteApi()) {
            Point point = Point.measurement("glucoseValue")
                    .addField("value", 5.6)
                    .addTag("tag", "étkezés előtt")
                    .time(Instant.now(), WritePrecision.NS);

            writeApi.writePoint(point);

        } catch (InfluxException e){
            //Toast.makeText(context, "EXCEPTION", Toast.LENGTH_SHORT).show();
        }
        //Toast.makeText(context, "Beírt érték", Toast.LENGTH_SHORT).show();
        client.close();
    }

}
