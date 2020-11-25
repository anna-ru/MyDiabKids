package com.example.mydiabkids;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        Context context = getContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(getString(R.string.diababa_shared_pref), MODE_PRIVATE);
        int currentImage = sharedPreferences.getInt(getString(R.string.diababa_shared_pref), R.drawable.blood_smiling);
        ImageView imageView = view.findViewById(R.id.diababa);
        imageView.setImageResource(currentImage);

        Button valuesBtn = view.findViewById(R.id.values_btn);
        valuesBtn.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_mainFragment_to_glucoseValuesFragment));

        Button profileBtn = view.findViewById(R.id.profile_btn);
        profileBtn.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_mainFragment_to_profileFragment));

        Button settingsBtn = view.findViewById(R.id.settings_btn);
        settingsBtn.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_mainFragment_to_settingsFragment));

        return view;
    }
}
