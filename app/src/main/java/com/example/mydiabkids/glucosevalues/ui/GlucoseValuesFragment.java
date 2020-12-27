package com.example.mydiabkids.glucosevalues.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.mydiabkids.R;
import com.example.mydiabkids.glucosevalues.ModifyGlValueActivity;
import com.example.mydiabkids.glucosevalues.NewGlValueActivity;
import com.example.mydiabkids.glucosevalues.model.GlValuesViewModel;
import com.example.mydiabkids.glucosevalues.model.GlucoseValue;
import com.example.mydiabkids.glucosevalues.db.GlucoseValueEntity;
import com.example.mydiabkids.glucosevalues.model.Value;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.thoughtbot.expandablecheckrecyclerview.listeners.OnCheckChildClickListener;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.example.mydiabkids.glucosevalues.ModifyGlValueActivity.MODIFY_DATE;
import static com.example.mydiabkids.glucosevalues.ModifyGlValueActivity.MODIFY_EATING;
import static com.example.mydiabkids.glucosevalues.ModifyGlValueActivity.MODIFY_INSULIN;
import static com.example.mydiabkids.glucosevalues.ModifyGlValueActivity.MODIFY_NOTE;
import static com.example.mydiabkids.glucosevalues.ModifyGlValueActivity.MODIFY_TIME;
import static com.example.mydiabkids.glucosevalues.ModifyGlValueActivity.MODIFY_TYPE;
import static com.example.mydiabkids.glucosevalues.ModifyGlValueActivity.MODIFY_VALUE;

public class GlucoseValuesFragment extends Fragment {

    //TODO: sending

    private GlValuesViewModel glValuesViewModel;
    public static final int NEW_GLUCOSE_VALUE_ACTIVITY_REQUEST_CODE = 0;
    public static final int MODIFY_GLUCOSE_VALUE_ACTIVITY_REQUEST_CODE = 1;
    private List<GlucoseValue> glucoseValues = new ArrayList<>();

    public GlucoseValuesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_glucose_values, container, false);
        Button sensorBtn = view.findViewById(R.id.values_sensor_btn);
        sensorBtn.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_glucoseValuesFragment_to_sensorFragment));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        glValuesViewModel = new ViewModelProvider(this).get(GlValuesViewModel.class);
        glValuesViewModel.getAllValues().observe(getViewLifecycleOwner(), new Observer<List<GlucoseValueEntity>>() {
            @Override
            public void onChanged(List<GlucoseValueEntity> glucoseValueEntities) {
                glucoseValues.clear();
                for (GlucoseValueEntity value: glucoseValueEntities) {
                    Collections.sort(value.getGlucoseValues(), Collections.reverseOrder());
                    GlucoseValue glucoseValue = new GlucoseValue(value.getDate(), value.getGlucoseValues());
                    glucoseValues.add(glucoseValue);
                }
                GlucoseValueAdapter adapter = new GlucoseValueAdapter(glucoseValues);
                adapter.setChildClickListener(new ChildClickListener(adapter));
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);
            }
        });

        //Adding new glucose value
        FloatingActionButton fab = view.findViewById(R.id.new_value_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), NewGlValueActivity.class);
                startActivityForResult(intent, NEW_GLUCOSE_VALUE_ACTIVITY_REQUEST_CODE);
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String date, time, type, notes, eating;
        double gl_value, insulin;

        //Request adding a new value to the database
        if (requestCode == NEW_GLUCOSE_VALUE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            //Get the information from the intent data
            date = data.getStringExtra(NewGlValueActivity.EXTRA_DATE);
            time = data.getStringExtra(NewGlValueActivity.EXTRA_TIME);
            type = data.getStringExtra(NewGlValueActivity.EXTRA_TYPE);
            notes = data.getStringExtra(NewGlValueActivity.EXTRA_NOTE);
            eating = data.getStringExtra(NewGlValueActivity.EXTRA_EATING);
            gl_value = data.getDoubleExtra(NewGlValueActivity.EXTRA_VALUE, 0);
            insulin = data.getDoubleExtra(NewGlValueActivity.EXTRA_INSULIN, 0);

            Value value = new Value(time, type, notes, gl_value, insulin, eating);

            GlucoseValue glucoseValue = glucoseValueWithDateAlreadyExists(date);
            if(glucoseValue != null){
                List<Value> values = glucoseValue.getItems();
                values.add(value);
                Collections.sort(values, Collections.reverseOrder());
                insertToDate(values, date);
                Toast.makeText(
                        getContext(),
                        "Új érték ehhez a dátumhoz: " + date,
                        Toast.LENGTH_LONG).show();
            } else {
                insertNew(value, date);
                Toast.makeText(
                        getContext(),
                        "Új érték új dátummal (" + date + ") mentve",
                        Toast.LENGTH_LONG).show();
            }
        }
        //Request modify a value
        else if (requestCode == MODIFY_GLUCOSE_VALUE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            date = data.getStringExtra(MODIFY_DATE);
            time = data.getStringExtra(MODIFY_TIME);
            type = data.getStringExtra(MODIFY_TYPE);
            notes = data.getStringExtra(MODIFY_NOTE);
            eating = data.getStringExtra(MODIFY_EATING);
            gl_value = data.getDoubleExtra(MODIFY_VALUE, 0);
            insulin = data.getDoubleExtra(MODIFY_INSULIN, 0);

            Value value = new Value(time, type, notes, gl_value, insulin, eating);
            ArrayList<Value> values = new ArrayList<>();
            values.add(value);

            GlucoseValueEntity glValue = new GlucoseValueEntity(date);
            glValue.setGlucoseValues(values);
            glValuesViewModel.updateValue(glValue);
            Toast.makeText(
                    getContext(),
                    "Érték módosítva",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(
                    getContext(),
                    "Nem mentettünk el semmit",
                    Toast.LENGTH_LONG).show();
        }
    }

    public  GlucoseValue glucoseValueWithDateAlreadyExists(String date){
        for (GlucoseValue glucoseValue : glucoseValues) {
            if(glucoseValue.getTitle().equals(date)){
                return glucoseValue;
            }
        }
        return null;
    }

    //New values added to database
    public void insertNew(Value value, String date){
        ArrayList<Value> values = new ArrayList<>();
        values.add(value);

        GlucoseValueEntity glValue = new GlucoseValueEntity(date);
        glValue.setGlucoseValues(values);
        glValuesViewModel.insert(glValue);
    }

    public void insertToDate(List<Value> values, String date){
        GlucoseValueEntity glValue = new GlucoseValueEntity(date);
        glValue.setGlucoseValues(values);
        glValuesViewModel.insertReplace(glValue);
    }

    //Modify or delete a value
    private class ChildClickListener implements OnCheckChildClickListener {
        GlucoseValueAdapter adapter;

        ChildClickListener(GlucoseValueAdapter adapter){
            this.adapter = adapter;
        }

        @Override
        public void onCheckChildCLick(View v, boolean checked, CheckedExpandableGroup group, int childIndex) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Mit csináljunk?")
                    .setMessage("Válaszd ki, hogy mit szeretnél csinálni ezzel az értékkel!")
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            adapter.clearChoices();
                        }
                    })
                    .setNeutralButton("Semmit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton("Módosítás", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getContext(),
                            ModifyGlValueActivity.class);
                    GlucoseValue glucoseValue = (GlucoseValue) group;
                    Value value = (Value) ((GlucoseValue) group).getItems().get(childIndex);
                    intent.putExtra(MODIFY_DATE, glucoseValue.getTitle());
                    intent.putExtra(MODIFY_TIME, value.getTime());
                    intent.putExtra(MODIFY_VALUE, value.getGl_value());
                    intent.putExtra(MODIFY_INSULIN, value.getInsulin());
                    intent.putExtra(MODIFY_TYPE, value.getInsulin_type());
                    intent.putExtra(MODIFY_EATING, value.getBefore_eating());
                    intent.putExtra(MODIFY_NOTE, value.getNotes());
                    startActivityForResult(intent, MODIFY_GLUCOSE_VALUE_ACTIVITY_REQUEST_CODE);
                }
            }).setNegativeButton("Törlés", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    confirmDelete(group, childIndex);
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void deleteIfOnlyOneValueExistsWithThisDate(Value value, GlucoseValue glucoseValue){
        GlucoseValueEntity entity = new GlucoseValueEntity(glucoseValue.getTitle());
        ArrayList<Value> values = new ArrayList<>();
        values.add(value);
        entity.setGlucoseValues(values);
        glValuesViewModel.deleteValue(entity);
    }

    private void deleteIfMoreValuesExistWithThisDate(Value value, GlucoseValue glucoseValue){
        GlucoseValueEntity entity = new GlucoseValueEntity(glucoseValue.getTitle());
        List<Value> values = ((List<Value>) glucoseValue.getItems());
        values.remove(value);
        entity.setGlucoseValues(values);
        insertToDate(values, glucoseValue.getTitle());
    }

    private void confirmDelete(CheckedExpandableGroup group, int childIndex){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Biztos?")
                .setMessage("Biztosan szeretnéd törölni ezt az értéket?")
                .setPositiveButton("Igen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        GlucoseValue glucoseValue = (GlucoseValue) group;
                        Value value = (Value) ((GlucoseValue) group).getItems().get(childIndex);
                        int itemCount = ((GlucoseValue) group).getItems().size();
                        if(itemCount == 1){
                            deleteIfOnlyOneValueExistsWithThisDate(value, glucoseValue);
                        } else if(itemCount > 1){
                            deleteIfMoreValuesExistWithThisDate(value, glucoseValue);
                        }
                        Toast.makeText(
                                getContext(),
                                "Érték törölve",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Nem", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}