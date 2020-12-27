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
import com.example.mydiabkids.glucosevalues.model.GlucoseValueDetails;
import com.example.mydiabkids.glucosevalues.model.GlucoseValueHeader;
import com.example.mydiabkids.glucosevalues.db.GlucoseValueEntity;
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
    private List<GlucoseValueHeader> glucoseValueHeaders = new ArrayList<>();

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
                glucoseValueHeaders.clear();
                for (GlucoseValueEntity value: glucoseValueEntities) {
                    Collections.sort(value.getGlucoseValues(), Collections.reverseOrder());
                    GlucoseValueHeader glucoseValueHeader = new GlucoseValueHeader(value.getDate(), value.getGlucoseValues());
                    glucoseValueHeaders.add(glucoseValueHeader);
                }
                GlucoseValueAdapter adapter = new GlucoseValueAdapter(glucoseValueHeaders);
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

            GlucoseValueDetails glucoseValueDetails = new GlucoseValueDetails(time, type, notes, gl_value, insulin, eating);

            GlucoseValueHeader glucoseValueHeader = glucoseValueWithDateAlreadyExists(date);
            if(glucoseValueHeader != null){
                List<GlucoseValueDetails> values = glucoseValueHeader.getItems();
                values.add(glucoseValueDetails);
                Collections.sort(values, Collections.reverseOrder());
                insertToDate(values, date);
                Toast.makeText(
                        getContext(),
                        "Új érték ehhez a dátumhoz: " + date,
                        Toast.LENGTH_LONG).show();
            } else {
                insertNew(glucoseValueDetails, date);
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

            GlucoseValueDetails glucoseValueDetails = new GlucoseValueDetails(time, type, notes, gl_value, insulin, eating);
            ArrayList<GlucoseValueDetails> values = new ArrayList<>();
            values.add(glucoseValueDetails);

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

    public GlucoseValueHeader glucoseValueWithDateAlreadyExists(String date){
        for (GlucoseValueHeader glucoseValueHeader : glucoseValueHeaders) {
            if(glucoseValueHeader.getTitle().equals(date)){
                return glucoseValueHeader;
            }
        }
        return null;
    }

    //New values added to database
    public void insertNew(GlucoseValueDetails glucoseValueDetails, String date){
        ArrayList<GlucoseValueDetails> values = new ArrayList<>();
        values.add(glucoseValueDetails);

        GlucoseValueEntity glValue = new GlucoseValueEntity(date);
        glValue.setGlucoseValues(values);
        glValuesViewModel.insert(glValue);
    }

    public void insertToDate(List<GlucoseValueDetails> glucoseValueDetails, String date){
        GlucoseValueEntity glValue = new GlucoseValueEntity(date);
        glValue.setGlucoseValues(glucoseValueDetails);
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
                    GlucoseValueHeader glucoseValueHeader = (GlucoseValueHeader) group;
                    GlucoseValueDetails glucoseValueDetails = (GlucoseValueDetails) ((GlucoseValueHeader) group).getItems().get(childIndex);
                    intent.putExtra(MODIFY_DATE, glucoseValueHeader.getTitle());
                    intent.putExtra(MODIFY_TIME, glucoseValueDetails.getTime());
                    intent.putExtra(MODIFY_VALUE, glucoseValueDetails.getGl_value());
                    intent.putExtra(MODIFY_INSULIN, glucoseValueDetails.getInsulin());
                    intent.putExtra(MODIFY_TYPE, glucoseValueDetails.getInsulin_type());
                    intent.putExtra(MODIFY_EATING, glucoseValueDetails.getBefore_eating());
                    intent.putExtra(MODIFY_NOTE, glucoseValueDetails.getNotes());
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

    private void deleteIfOnlyOneValueExistsWithThisDate(GlucoseValueDetails glucoseValueDetails, GlucoseValueHeader glucoseValueHeader){
        GlucoseValueEntity entity = new GlucoseValueEntity(glucoseValueHeader.getTitle());
        ArrayList<GlucoseValueDetails> values = new ArrayList<>();
        values.add(glucoseValueDetails);
        entity.setGlucoseValues(values);
        glValuesViewModel.deleteValue(entity);
    }

    private void deleteIfMoreValuesExistWithThisDate(GlucoseValueDetails glucoseValueDetails, GlucoseValueHeader glucoseValueHeader){
        GlucoseValueEntity entity = new GlucoseValueEntity(glucoseValueHeader.getTitle());
        List<GlucoseValueDetails> values = ((List<GlucoseValueDetails>) glucoseValueHeader.getItems());
        values.remove(glucoseValueDetails);
        entity.setGlucoseValues(values);
        insertToDate(values, glucoseValueHeader.getTitle());
    }

    private void confirmDelete(CheckedExpandableGroup group, int childIndex){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Biztos?")
                .setMessage("Biztosan szeretnéd törölni ezt az értéket?")
                .setPositiveButton("Igen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        GlucoseValueHeader glucoseValueHeader = (GlucoseValueHeader) group;
                        GlucoseValueDetails glucoseValueDetails = (GlucoseValueDetails) ((GlucoseValueHeader) group).getItems().get(childIndex);
                        int itemCount = ((GlucoseValueHeader) group).getItems().size();
                        if(itemCount == 1){
                            deleteIfOnlyOneValueExistsWithThisDate(glucoseValueDetails, glucoseValueHeader);
                        } else if(itemCount > 1){
                            deleteIfMoreValuesExistWithThisDate(glucoseValueDetails, glucoseValueHeader);
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