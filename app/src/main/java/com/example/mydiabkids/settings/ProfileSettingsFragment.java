package com.example.mydiabkids.settings;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mydiabkids.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileSettingsFragment extends Fragment {

    public static final String FILE_NAME = "profile_settings";
    public static final String NAME = "nev";
    public static final String BIRTH_DATE = "szuletesi_datum";
    public static final String DIAB_TYPE = "diabetesz_tipus";
    public static final String BOLUS = "bolus";
    public static final String BASIS = "bazis";
    public static final String HELPERS = "segedeszkozok";
    public static final String PNAME = "szulo_nev";
    public static final String PEMAIL = "szulo_email";
    public static final String PTEL = "szulo_tel";
    public static final String DNAME = "orvos_nev";
    public static final String DEMAIL = "orvos_email";
    public static final String DTEL = "orvos_tel";

    TextView birthTV;
    EditText nameEdit, bolusEdit, basisEdit, helpersEdit, pNameEdit, pEmailEdit, pTelEdit, dNameEdit, dEmailEdit, dTelEdit;
    Spinner diabTypeSpinner;
    Button save, cancel;

    Context context;
    HashMap<String, String> profileData;

    public ProfileSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_settings, container, false);

        context = getContext();

        birthTV = view.findViewById(R.id.birth_edit);
        nameEdit = view.findViewById(R.id.name_editText);
        bolusEdit = view.findViewById(R.id.bolus_edit);
        basisEdit = view.findViewById(R.id.basis_edit);
        helpersEdit = view.findViewById(R.id.tools_edit);
        pNameEdit = view.findViewById(R.id.pname_edit);
        pEmailEdit = view.findViewById(R.id.pemail_edit);
        pTelEdit = view.findViewById(R.id.ptel_edit);
        dNameEdit = view.findViewById(R.id.dname_edit);
        dEmailEdit = view.findViewById(R.id.demail_edit);
        dTelEdit = view.findViewById(R.id.dtel_edit);

        final Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -25);
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        birthTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                        (view1, year, month, day) ->
                                birthTV.setText(year + "." + (month+1) + "." + day), mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        diabTypeSpinner = view.findViewById(R.id.diab_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.diab_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        diabTypeSpinner.setAdapter(adapter);

        try {
            String jsonResponse = getJsonString();
            getDataFromJson(jsonResponse);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Valami hiba történt", Toast.LENGTH_SHORT).show();
        }

        save = view.findViewById(R.id.profile_save_btn);
        cancel = view.findViewById(R.id.profile_cancel_btn);

        cancel.setOnClickListener(Navigation.createNavigateOnClickListener(
                R.id.action_profileSettingsFragment_to_profileFragment));

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name, birthDate, diabType, bolus, basis, helpers, pName, pEmail, pTel, dName, dEmail, dTel;
                name = nameEdit.getText().toString();
                birthDate = birthTV.getText().toString();
                diabType = diabTypeSpinner.getSelectedItem().toString();
                bolus = bolusEdit.getText().toString();
                basis = basisEdit.getText().toString();
                helpers = helpersEdit.getText().toString();
                pName = pNameEdit.getText().toString();
                pEmail = pEmailEdit.getText().toString();
                pTel = pTelEdit.getText().toString();
                dName = dNameEdit.getText().toString();
                dEmail = dEmailEdit.getText().toString();
                dTel = dTelEdit.getText().toString();

                try {
                    JSONObject jsonObject = createJson(name, birthDate, diabType, bolus, basis, helpers, pName, pEmail,
                            pTel, dName, dEmail, dTel);
                    writeToJsonFile(jsonObject);
                    Toast.makeText(context, "Beállítások mentve", Toast.LENGTH_SHORT).show();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Valami hiba történt", Toast.LENGTH_SHORT).show();
                }
                Navigation.findNavController(view).navigate(R.id.action_profileSettingsFragment_to_profileFragment);
            }
        });

        return view;
    }

    private JSONObject createJson(String name, String birthDate, String diabType, String bolus, String basis, String helpers,
                                  String pName, String pEmail, String pTel, String dName, String dEmail, String dTel)
            throws JSONException {
        JSONObject jsonObject = new JSONObject();

        if(!name.isEmpty()) jsonObject.put(NAME, name);
        else jsonObject.put(NAME, " ");
        if(!birthDate.isEmpty()) jsonObject.put(BIRTH_DATE, birthDate);
        else jsonObject.put(BIRTH_DATE, " ");
        if(!diabType.isEmpty()) jsonObject.put(DIAB_TYPE, diabType);
        else jsonObject.put(DIAB_TYPE, " ");
        if(!bolus.isEmpty()) jsonObject.put(BOLUS, bolus);
        else jsonObject.put(BOLUS, " ");
        if(!basis.isEmpty()) jsonObject.put(BASIS, basis);
        else jsonObject.put(BASIS, " ");
        if(!helpers.isEmpty()) jsonObject.put(HELPERS, helpers);
        else jsonObject.put(HELPERS, " ");
        if(!pName.isEmpty()) jsonObject.put(PNAME, pName);
        else jsonObject.put(PNAME, " ");
        if(!pEmail.isEmpty()) jsonObject.put(PEMAIL, pEmail);
        else jsonObject.put(PEMAIL, " ");
        if(!pTel.isEmpty()) jsonObject.put(PTEL, pTel);
        else jsonObject.put(PTEL, " ");
        if(!dName.isEmpty()) jsonObject.put(DNAME, dName);
        else jsonObject.put(DNAME, " ");
        if(!dEmail.isEmpty()) jsonObject.put(DEMAIL, dEmail);
        else jsonObject.put(DEMAIL, " ");
        if(!dTel.isEmpty()) jsonObject.put(DTEL, dTel);
        else jsonObject.put(DTEL, " ");

        return jsonObject;
    }

    private void writeToJsonFile(JSONObject jsonObject) throws IOException {
        String userString = jsonObject.toString();

        File file = new File(context.getFilesDir(), FILE_NAME);
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
        while (line != null){
            stringBuilder.append(line).append("\n");
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }

    private void getDataFromJson(String response) throws JSONException {
        JSONObject jsonObject  = new JSONObject(response);
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

        if(!birthDate.isEmpty()) birthTV.setText(birthDate);
        if(!name.isEmpty()) nameEdit.setText(name);
        if(!diabType.isEmpty()) diabTypeSpinner.setPrompt(diabType);
        if(!bolus.isEmpty()) bolusEdit.setText(bolus);
        if(!basis.isEmpty()) basisEdit.setText(basis);
        if(!helpers.isEmpty()) helpersEdit.setText(helpers);
        if(!pName.isEmpty()) pNameEdit.setText(pName);
        if(!pEmail.isEmpty()) pEmailEdit.setText(pEmail);
        if(!pTel.isEmpty()) pTelEdit.setText(pTel);
        if(!dName.isEmpty()) dNameEdit.setText(dName);
        if(!dEmail.isEmpty()) dEmailEdit.setText(dEmail);
        if(!dTel.isEmpty()) dTelEdit.setText(dTel);
    }
}