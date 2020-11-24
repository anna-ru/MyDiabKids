package com.example.mydiabkids.glucosevalues.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mydiabkids.glucosevalues.model.GlucoseValueEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {GlucoseValueEntity.class}, version = 1, exportSchema = false)
public abstract class GlValuesDatabase extends RoomDatabase {
    public abstract GlValuesDao glvaluesDao();

    private static volatile GlValuesDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static GlValuesDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (GlValuesDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            GlValuesDatabase.class, "glucose_values_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
