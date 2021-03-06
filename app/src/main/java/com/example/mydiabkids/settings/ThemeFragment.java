package com.example.mydiabkids.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.mydiabkids.R;

public class ThemeFragment extends Fragment {

    RadioGroup group;
    Switch darkModeSwitch;
    SharedPreferences sharedPreferences, darkMode;

    public static final int PINK = R.id.pink;
    public static final int BLUE = R.id.blue;
    public static final int YELLOW = R.id.yellow;
    public static final int ORANGE = R.id.orange;
    public static final int GREEN = R.id.green;
    public static final int BROWN = R.id.brown;

    int current_theme;
    int isDarkMode;

    public ThemeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_theme, container, false);
        Context context = getContext();
        group = view.findViewById(R.id.button_group);
        sharedPreferences = getContext().getSharedPreferences(getString(R.string.theme_shared_pref), Context.MODE_PRIVATE);
        current_theme = sharedPreferences.getInt(getString(R.string.theme_shared_pref), PINK);
        group.check(current_theme);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(getString(R.string.theme_shared_pref), checkedId);
                editor.apply();
                getActivity().recreate();
                Toast.makeText(context, "Téma beállítása sikerült.", Toast.LENGTH_SHORT).show();
            }
        });
        darkMode = getContext().getSharedPreferences(getString(R.string.dark_shared_pref), Context.MODE_PRIVATE);
        darkModeSwitch = view.findViewById(R.id.dark_mode_switch);
        isDarkMode = AppCompatDelegate.getDefaultNightMode();
        darkModeSwitch.setChecked(isDarkMode == AppCompatDelegate.MODE_NIGHT_YES);
        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = darkMode.edit();
                editor.putBoolean(getString(R.string.dark_shared_pref), b);
                editor.apply();
                if(b) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(context, "Téma beállítása sikerült.", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}