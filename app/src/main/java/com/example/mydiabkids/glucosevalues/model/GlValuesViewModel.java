package com.example.mydiabkids.glucosevalues.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.mydiabkids.glucosevalues.db.GlucoseValueEntity;

import java.util.List;

public class GlValuesViewModel extends AndroidViewModel {

    private GlValuesRepository mRepository;
    private LiveData<List<GlucoseValueEntity>> mAllValues;

    public GlValuesViewModel(@NonNull Application application) {
        super(application);
        mRepository = new GlValuesRepository(application);
        mAllValues = mRepository.getAllValuesWithDate();
    }

    public LiveData<List<GlucoseValueEntity>> getAllValues() {
        return  mAllValues;
    }

    public LiveData <GlucoseValueEntity> getValueWithDate(String date){
        return  mRepository.getValueWithDate(date);
    }

    public void insert(GlucoseValueEntity glucoseValueEntity) {
        mRepository.insert(glucoseValueEntity);
    }

    public void insertReplace(GlucoseValueEntity glucoseValueEntity) {
        mRepository.insertReplace(glucoseValueEntity);
    }

    public void updateValue(GlucoseValueEntity value){
        mRepository.updateValue(value);
    }

    public void deleteValue(GlucoseValueEntity value){
        mRepository.deleteValue(value);
    }

}
