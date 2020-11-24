package com.example.mydiabkids;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;
import static com.example.mydiabkids.ProfileSettingsFragment.BASIS;
import static com.example.mydiabkids.ProfileSettingsFragment.BIRTH_DATE;
import static com.example.mydiabkids.ProfileSettingsFragment.BOLUS;
import static com.example.mydiabkids.ProfileSettingsFragment.DEMAIL;
import static com.example.mydiabkids.ProfileSettingsFragment.DIAB_TYPE;
import static com.example.mydiabkids.ProfileSettingsFragment.DNAME;
import static com.example.mydiabkids.ProfileSettingsFragment.DTEL;
import static com.example.mydiabkids.ProfileSettingsFragment.FILE_NAME;
import static com.example.mydiabkids.ProfileSettingsFragment.HELPERS;
import static com.example.mydiabkids.ProfileSettingsFragment.NAME;
import static com.example.mydiabkids.ProfileSettingsFragment.PEMAIL;
import static com.example.mydiabkids.ProfileSettingsFragment.PNAME;
import static com.example.mydiabkids.ProfileSettingsFragment.PTEL;


public class ProfileFragment extends Fragment {

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CHOOSE_PIC_REQUEST_CODE = 3;
    public static final int TAKE_PIC_REQUEST_CODE = 4;
    public static final int MEDIA_TYPE_IMAGE = 2;

    private Uri mMediaUri;
    private ImageView profilePicture;
    private String currentPhotoPath;
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


        profilePicture = view.findViewById(R.id.profile_img);
        ImageButton modifyImg = view.findViewById(R.id.profile_img_modify_btn);
        modifyImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(context);
            }
        });

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

    //TODO: profile picture saving
    private void selectImage(Context context) {
        final CharSequence[] options = {"Kép készítése", "Választás a galériából", "Mégse"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Profilkép beállítása");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Kép készítése")) {
                    //Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    //startActivityForResult(takePicture, TAKE_PIC_REQUEST_CODE);
                } else if (options[item].equals("Választás a galériából")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, CHOOSE_PIC_REQUEST_CODE);
                } else if (options[item].equals("Mégse")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public ProfileFragment() {
        // Required empty public constructor
    }
}