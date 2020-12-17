package com.example.mydiabkids.glucosevalues.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.mydiabkids.glucosevalues.model.GlucoseValueEntity;

import java.util.List;

public class GlValuesRepository {

    private GlValuesDao glValuesDao;
    private LiveData<List<GlucoseValueEntity>> mAllValues;

    public GlValuesRepository(Application application) {
        GlValuesDatabase db = GlValuesDatabase.getDatabase(application);
        glValuesDao = db.glvaluesDao();
        mAllValues = glValuesDao.getAllValues();
    }

    public void insert(GlucoseValueEntity value) {
        GlValuesDatabase.databaseWriteExecutor.execute(() -> {
            glValuesDao.insert(value);
        });
    }

    public void insertReplace(GlucoseValueEntity value) {
        GlValuesDatabase.databaseWriteExecutor.execute(() -> {
            glValuesDao.insertReplace(value);
        });
    }

    public LiveData<List<GlucoseValueEntity>> getAllValuesWithDate() {
        return mAllValues;
    }

    public LiveData<GlucoseValueEntity> getValueWithDate(String date){
        return glValuesDao.getValueWithDate(date);
    }

    public void updateValue(GlucoseValueEntity value){
        GlValuesDatabase.databaseWriteExecutor.execute(() -> {
            glValuesDao.updateValue(value);
        });
    }

    public void deleteValue(GlucoseValueEntity value){
        GlValuesDatabase.databaseWriteExecutor.execute(() -> {
            glValuesDao.deleteValue(value);
        });
    }

}
