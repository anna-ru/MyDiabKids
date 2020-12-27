package com.example.mydiabkids.glucosevalues.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GlValuesDao {

    @Query("SELECT * from values_table ORDER BY date ASC")
    LiveData<List<GlucoseValueEntity>> getAllValues();

    @Query("select * from values_table where date = :date")
    LiveData<GlucoseValueEntity> getValueWithDate(String date);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(GlucoseValueEntity glucoseValueEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReplace(GlucoseValueEntity glucoseValueEntity);

    @Update
    void updateValue(GlucoseValueEntity value);

    @Delete
    void deleteValue(GlucoseValueEntity value);

}
