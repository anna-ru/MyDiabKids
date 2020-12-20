package com.example.mydiabkids.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mydiabkids.R;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {

    TextView notifications, theme;
    ImageButton modify;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Context context = getContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(getString(R.string.diababa_shared_pref), MODE_PRIVATE);
        int currentImage = sharedPreferences.getInt(getString(R.string.diababa_shared_pref), R.drawable.blood_smiling);
        ImageView imageView = view.findViewById(R.id.diababa_img);
        imageView.setImageResource(currentImage);

        notifications = view.findViewById(R.id.notifications_txt);
        theme = view.findViewById(R.id.theme_change_txt);
        modify = view.findViewById(R.id.modify_diababa);

        notifications.setOnClickListener(Navigation.createNavigateOnClickListener(
                R.id.action_settingsFragment_to_notificationsFragment));
        theme.setOnClickListener(Navigation.createNavigateOnClickListener(
                R.id.action_settingsFragment_to_themeFragment));
        modify.setOnClickListener(Navigation.createNavigateOnClickListener(
                R.id.action_settingsFragment_to_diababaFragment));
        return view;
    }
}