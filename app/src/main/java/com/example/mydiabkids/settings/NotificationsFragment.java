package com.example.mydiabkids.settings;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mydiabkids.R;
import com.example.mydiabkids.glucosevalues.sensor.SensorFragment;

import static com.example.mydiabkids.MainActivity.CHANNEL_ID;

public class NotificationsFragment extends Fragment {

    SharedPreferences lowPref, highPref;
    CardView lowView, highView;
    TextView lowValue, highValue;
    Context context;

    public static final String lowPrefStr = "Low bg notification pref";
    public static final String highPrefStr = "High bg notification pref";

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        context = getContext();
        lowView = view.findViewById(R.id.low_view);
        highView = view.findViewById(R.id.high_view);

        lowValue = view.findViewById(R.id.low_bg);
        highValue = view.findViewById(R.id.high_bg);

        lowPref = context.getSharedPreferences(lowPrefStr, Context.MODE_PRIVATE);
        highPref = context.getSharedPreferences(highPrefStr, Context.MODE_PRIVATE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String low, high;
        low = lowPref.getString(lowPrefStr, "0.0");
        high = highPref.getString(highPrefStr, "0.0");

        lowValue.setText(low);
        highValue.setText(high);

        lowView.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View lowDialogView = inflater.inflate(R.layout.low_bg_setting_dialog, null);

            builder.setView(lowDialogView)
                    .setPositiveButton(R.string.kesz, (dialog, id) -> {
                        EditText editText = lowDialogView.findViewById(R.id.low_edit);
                        String result = editText.getText().toString();

                        SharedPreferences.Editor editor = lowPref.edit();
                        editor.putString(lowPrefStr, result);
                        editor.apply();
                        lowValue.setText(result);
                    })
                    .setNegativeButton(R.string.megsem, (dialog, id) -> dialog.cancel())
                    .show();
        });

        highView.setOnClickListener(view12 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View highDialogView = inflater.inflate(R.layout.high_bg_setting_dialog, null);

            builder.setView(highDialogView)
                    .setPositiveButton(R.string.kesz, (dialog, id) -> {
                        EditText editText = highDialogView.findViewById(R.id.high_edit);
                        String result = editText.getText().toString();

                        SharedPreferences.Editor editor = highPref.edit();
                        editor.putString(highPrefStr, result);
                        editor.apply();
                        highValue.setText(result);
                    })
                    .setNegativeButton(R.string.megsem, (dialog, id) -> dialog.cancel())
                    .show();
        });
    }
}