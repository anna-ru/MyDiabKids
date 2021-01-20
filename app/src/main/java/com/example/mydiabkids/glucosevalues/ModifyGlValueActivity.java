package com.example.mydiabkids.glucosevalues;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.mydiabkids.R;

import static com.example.mydiabkids.settings.ThemeFragment.BLUE;
import static com.example.mydiabkids.settings.ThemeFragment.BROWN;
import static com.example.mydiabkids.settings.ThemeFragment.GREEN;
import static com.example.mydiabkids.settings.ThemeFragment.ORANGE;
import static com.example.mydiabkids.settings.ThemeFragment.PINK;
import static com.example.mydiabkids.settings.ThemeFragment.YELLOW;

public class ModifyGlValueActivity extends AppCompatActivity {

    public static final String MODIFY_DATE = "com.example.android.MODIFY_DATE";
    public static final String MODIFY_TIME = "com.example.android.MODIFY_TIME";
    public static final String MODIFY_EATING = "com.example.android.MODIFY_EATING";
    public static final String MODIFY_VALUE = "com.example.android.MODIFY_VALUE";
    public static final String MODIFY_INSULIN = "com.example.android.MODIFY_INSULIN";
    public static final String MODIFY_TYPE = "com.example.android.MODIFY_TYPE";
    public static final String MODIFY_NOTE = "com.example.android.MODIFY_NOTE";
    public static final String CHILD_INDEX = "com.example.android.CHILD_INDEX";

    private TextView date, time;
    private Spinner eating_spinner, type_spinner;
    private EditText gl_value, insulin, note;
    private int mYear, mMonth, mDay, mHour, mMinute, childInd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_gl_value);

        date = findViewById(R.id.modify_date);
        time = findViewById(R.id.modify_time);
        eating_spinner = findViewById(R.id.modify_eating_spinner);
        type_spinner = findViewById(R.id.modify_type_spinner);
        gl_value = findViewById(R.id.modify_values);
        insulin = findViewById(R.id.modify_insulin);
        note = findViewById(R.id.modify_note);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.eating_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eating_spinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type_spinner.setAdapter(adapter2);

        Intent intent = getIntent();
        childInd = intent.getIntExtra(CHILD_INDEX, 0);
        String dateTxt, timeTxt;
        dateTxt = intent.getStringExtra(MODIFY_DATE);
        timeTxt = intent.getStringExtra(MODIFY_TIME);
        String[] dateArr, timeArr;
        if (dateTxt != null) {
            dateArr = dateTxt.split("\\.");
            mYear = Integer.parseInt(dateArr[0]);
            mMonth = Integer.parseInt(dateArr[1]);
            mDay = Integer.parseInt(dateArr[2]);
        }
        if (timeTxt != null) {
            timeArr = timeTxt.split(":");
            mHour = Integer.parseInt(timeArr[0]);
            mMinute = Integer.parseInt(timeArr[1]);
        }
        date.setText(dateTxt);
        time.setText(timeTxt);
        eating_spinner.setPrompt(intent.getStringExtra(MODIFY_EATING));
        type_spinner.setPrompt(intent.getStringExtra(MODIFY_TYPE));
        gl_value.setText(String.valueOf(intent.getDoubleExtra(MODIFY_VALUE, 0)));
        insulin.setText(String.valueOf(intent.getDoubleExtra(MODIFY_INSULIN, 0)));
        note.setText(intent.getStringExtra(MODIFY_NOTE));

        date.setOnClickListener(this::showDatePicker);

        time.setOnClickListener(this::showTimePicker);
    }

    public void showDatePicker(View view) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view1, year, month, day) ->
                        date.setText(year + "." + (month+1) + "." + day), mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void showTimePicker(View view) {
        TimePickerDialog timePickerDialog = /*new TimePickerDialog(this,
                (view1, hourOfDay, minute) -> time.setText(hourOfDay + ":" + minute), mHour, mMinute, false);*/
                new TimePickerDialog(this, (timePicker, hour, minute) -> {
                    String hourString, minString;
                    if (hour < 10) hourString = "0" + hour;
                    else hourString = String.valueOf(hour);
                    if(minute < 10) minString = "0" + minute;
                    else minString = String.valueOf(minute);
                    time.setText(hourString + ":" + minString);
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    public void save(View view) {
        Intent replyIntent = new Intent();

        double insulinTxt = Double.parseDouble(insulin.getText().toString());
        String noteTxt = note.getText().toString();
        String dateTxt = date.getText().toString();
        String timeTxt = time.getText().toString();
        String eatingTxt = eating_spinner.getSelectedItem().toString();
        String typeTxt = type_spinner.getSelectedItem().toString();
        double value = Double.parseDouble(gl_value.getText().toString());

        replyIntent.putExtra(MODIFY_INSULIN, insulinTxt);
        replyIntent.putExtra(MODIFY_NOTE, noteTxt);
        replyIntent.putExtra(MODIFY_DATE, dateTxt);
        replyIntent.putExtra(MODIFY_TIME, timeTxt);
        replyIntent.putExtra(MODIFY_EATING, eatingTxt);
        replyIntent.putExtra(MODIFY_TYPE, typeTxt);
        replyIntent.putExtra(MODIFY_VALUE, value);
        replyIntent.putExtra(CHILD_INDEX, childInd);

        setResult(RESULT_OK, replyIntent);
        finish();
    }

    public void cancel(View view) {
        Intent replyIntent = new Intent();
        setResult(RESULT_CANCELED, replyIntent);
        finish();
    }

    private void setTheme(){
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.theme_shared_pref), MODE_PRIVATE);
        int theme = sharedPreferences.getInt(getString(R.string.theme_shared_pref), PINK);
        switch (theme){
            case PINK:
                setTheme(R.style.PinkTheme);
                break;
            case BLUE:
                setTheme(R.style.BlueTheme);
                break;
            case GREEN:
                setTheme(R.style.GreenTheme);
                break;
            case YELLOW:
                setTheme(R.style.YellowTheme);
                break;
            case ORANGE:
                setTheme(R.style.OrangeTheme);
                break;
            case BROWN:
                setTheme(R.style.BrownTheme);
                break;
        }
    }
}
