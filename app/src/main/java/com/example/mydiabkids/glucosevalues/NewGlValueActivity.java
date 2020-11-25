package com.example.mydiabkids.glucosevalues;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.mydiabkids.R;

import java.util.Calendar;

import static com.example.mydiabkids.ThemeFragment.BLUE;
import static com.example.mydiabkids.ThemeFragment.BROWN;
import static com.example.mydiabkids.ThemeFragment.GREEN;
import static com.example.mydiabkids.ThemeFragment.ORANGE;
import static com.example.mydiabkids.ThemeFragment.PINK;
import static com.example.mydiabkids.ThemeFragment.YELLOW;

public class NewGlValueActivity extends AppCompatActivity {

    public static final String EXTRA_DATE = "com.example.android.DATE";
    public static final String EXTRA_TIME = "com.example.android.TIME";
    public static final String EXTRA_EATING = "com.example.android.EATING";
    public static final String EXTRA_VALUE = "com.example.android.VALUE";
    public static final String EXTRA_INSULIN = "com.example.android.INSULIN";
    public static final String EXTRA_TYPE = "com.example.android.TYPE";
    public static final String EXTRA_NOTE = "com.example.android.NOTE";
    private int mYear, mMonth, mDay, mHour, mMinute;
    private PopupWindow mPopUpWindow;
    private Context mContext;

    Spinner eating_spinner, type_spinner;
    TextView date, time;
    EditText gl_value, insulin, note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_gl_value);
        mContext = getApplicationContext();

        eating_spinner = findViewById(R.id.eating_spinner);
        type_spinner = findViewById(R.id.type_spinner);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        gl_value = findViewById(R.id.values_edit_text);
        insulin = findViewById(R.id.insulin_edit_text);
        note = findViewById(R.id.note_edit_text);

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        date.setText(mYear + "." + (mMonth+1) + "." + mDay);
        time.setText(mHour + ":" + mMinute);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.eating_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eating_spinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type_spinner.setAdapter(adapter2);
    }

    public void showDatePicker(View view) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view1, year, month, day) ->
                        date.setText(year + "." + (month+1) + "." + day), mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void showTimePicker(View view) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view1, hourOfDay, minute) -> time.setText(hourOfDay + ":" + minute), mHour, mMinute, false);
        timePickerDialog.show();
    }


    public void save(View view) {
        Intent replyIntent = new Intent();
        if (TextUtils.isEmpty(gl_value.getText())) {
            View popupView = LayoutInflater.from(mContext).inflate(R.layout.popup_layout, null);
            mPopUpWindow = new PopupWindow(popupView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
            Button close_btn = popupView.findViewById(R.id.popup_btn);
            close_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPopUpWindow.dismiss();
                }
            });
            mPopUpWindow.showAtLocation(view, Gravity.CENTER,0,0);
        } else {

            String typeTxt = type_spinner.getSelectedItem().toString();
            if(!TextUtils.isEmpty(insulin.getText())) {
                double insulinTxt = Double.parseDouble(insulin.getText().toString());
                replyIntent.putExtra(EXTRA_INSULIN, insulinTxt);
                replyIntent.putExtra(EXTRA_TYPE, typeTxt);
            } else {
                replyIntent.putExtra(EXTRA_TYPE, "-");
            }

            if(!TextUtils.isEmpty(note.getText())) {
                String noteTxt = note.getText().toString();
                replyIntent.putExtra(EXTRA_NOTE, noteTxt);
            }

            String dateTxt = date.getText().toString();
            String timeTxt = time.getText().toString();
            String eatingTxt = eating_spinner.getSelectedItem().toString();

            double value = Double.parseDouble(gl_value.getText().toString());

            replyIntent.putExtra(EXTRA_DATE, dateTxt);
            replyIntent.putExtra(EXTRA_TIME, timeTxt);
            replyIntent.putExtra(EXTRA_EATING, eatingTxt);
            replyIntent.putExtra(EXTRA_VALUE, value);

            setResult(RESULT_OK, replyIntent);
            finish();
        }
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
