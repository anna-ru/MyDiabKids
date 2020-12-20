package com.example.mydiabkids.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mydiabkids.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static com.example.mydiabkids.settings.ProfileSettingsFragment.BASIS;
import static com.example.mydiabkids.settings.ProfileSettingsFragment.BIRTH_DATE;
import static com.example.mydiabkids.settings.ProfileSettingsFragment.BOLUS;
import static com.example.mydiabkids.settings.ProfileSettingsFragment.DEMAIL;
import static com.example.mydiabkids.settings.ProfileSettingsFragment.DIAB_TYPE;
import static com.example.mydiabkids.settings.ProfileSettingsFragment.DNAME;
import static com.example.mydiabkids.settings.ProfileSettingsFragment.DTEL;
import static com.example.mydiabkids.settings.ProfileSettingsFragment.FILE_NAME;
import static com.example.mydiabkids.settings.ProfileSettingsFragment.HELPERS;
import static com.example.mydiabkids.settings.ProfileSettingsFragment.NAME;
import static com.example.mydiabkids.settings.ProfileSettingsFragment.PEMAIL;
import static com.example.mydiabkids.settings.ProfileSettingsFragment.PNAME;
import static com.example.mydiabkids.settings.ProfileSettingsFragment.PTEL;


public class ProfileFragment extends Fragment {

    Context context;
    TextView nameTV, birthTV, diabTV, bolusTV, basisTV, toolsTV, pNameTV, pEmailTV, pTelTV, dNameTV, dEmailTV, dTelTV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        context = this.getContext();
        ImageButton modify;
        modify = view.findViewById(R.id.profile_modify_btn);
        modify.setOnClickListener(Navigation.createNavigateOnClickListener(
                R.id.action_profileFragment_to_profileSettingsFragment));

        nameTV = view.findViewById(R.id.name_txt);
        birthTV = view.findViewById(R.id.birth_date_txt);
        diabTV = view.findViewById(R.id.diab_type_txt);
        bolusTV = view.findViewById(R.id.bolus_insulin_txt);
        basisTV = view.findViewById(R.id.basis_insulin_txt);
        toolsTV = view.findViewById(R.id.helpers_txt);
        pNameTV = view.findViewById(R.id.parent_name_edit);
        pEmailTV = view.findViewById(R.id.parent_email_edit);
        pTelTV = view.findViewById(R.id.parent_tel_edit);
        dNameTV = view.findViewById(R.id.doc_name_edit);
        dEmailTV = view.findViewById(R.id.doc_email_edit);
        dTelTV = view.findViewById(R.id.doc_tel_edit);

        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            try {
                JSONObject jsonObject = createJson(nameTV.getText().toString(), birthTV.getText().toString(),
                        diabTV.getText().toString(), bolusTV.getText().toString(), basisTV.getText().toString(),
                        toolsTV.getText().toString(), pNameTV.getText().toString(), pEmailTV.getText().toString(),
                        pTelTV.getText().toString(), dNameTV.getText().toString(), dEmailTV.getText().toString(),
                        dTelTV.getText().toString());
                writeToJsonFile(jsonObject, file);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Valami hiba történt", Toast.LENGTH_SHORT).show();
            }
        }

        try {
            String response = getJsonString();
            getDataFromJson(response);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Valami hiba történt", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private JSONObject createJson(String name, String birthDate, String diabType, String bolus, String basis, String helpers,
                                  String pName, String pEmail, String pTel, String dName, String dEmail, String dTel)
            throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(NAME, name);
        jsonObject.put(BIRTH_DATE, birthDate);
        jsonObject.put(DIAB_TYPE, diabType);
        jsonObject.put(BOLUS, bolus);
        jsonObject.put(BASIS, basis);
        jsonObject.put(HELPERS, helpers);
        jsonObject.put(PNAME, pName);
        jsonObject.put(PEMAIL, pEmail);
        jsonObject.put(PTEL, pTel);
        jsonObject.put(DNAME, dName);
        jsonObject.put(DEMAIL, dEmail);
        jsonObject.put(DTEL, dTel);

        return jsonObject;
    }

    private void writeToJsonFile(JSONObject jsonObject, File file) throws IOException {
        String userString = jsonObject.toString();

        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(userString);
        bufferedWriter.close();
    }

    private String getJsonString() throws IOException {
        File file = new File(context.getFilesDir(), FILE_NAME);
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder stringBuilder = new StringBuilder();
        String line = bufferedReader.readLine();
        while (line != null) {
            stringBuilder.append(line).append("\n");
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }

    private void getDataFromJson(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        String name, birthDate, diabType, bolus, basis, helpers, pName, pEmail, pTel, dName, dEmail, dTel;
        name = jsonObject.getString(NAME);
        birthDate = jsonObject.getString(BIRTH_DATE);
        diabType = jsonObject.getString(DIAB_TYPE);
        bolus = jsonObject.getString(BOLUS);
        basis = jsonObject.getString(BASIS);
        helpers = jsonObject.getString(HELPERS);
        pName = jsonObject.getString(PNAME);
        pEmail = jsonObject.getString(PEMAIL);
        pTel = jsonObject.getString(PTEL);
        dName = jsonObject.getString(DNAME);
        dEmail = jsonObject.getString(DEMAIL);
        dTel = jsonObject.getString(DTEL);

        if (!(birthDate).isEmpty()) birthTV.setText(birthDate);
        if (!(name).isEmpty()) nameTV.setText(name);
        if (!(diabType).isEmpty()) diabTV.setText(diabType);
        if (!(bolus).isEmpty()) bolusTV.setText(bolus);
        if (!(basis).isEmpty()) basisTV.setText(basis);
        if (!(helpers).isEmpty()) toolsTV.setText(helpers);
        if (!(pName).isEmpty()) pNameTV.setText(pName);
        if (!(pEmail).isEmpty()) pEmailTV.setText(pEmail);
        if (!(pTel).isEmpty()) pTelTV.setText(pTel);
        if (!dName.isEmpty()) dNameTV.setText(dName);
        if (!(dEmail).isEmpty()) dEmailTV.setText(dEmail);
        if (!(dTel).isEmpty()) dTelTV.setText(dTel);
    }

    public ProfileFragment() {
        // Required empty public constructor
    }
}