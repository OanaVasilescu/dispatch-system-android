package com.example.dispatchsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import com.example.dispatchsystem.api.RetrofitClient;
import com.example.dispatchsystem.controller.InputValidator;
import com.example.dispatchsystem.model.User;
import com.google.android.material.button.MaterialButton;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegisterActivity extends android.app.Activity {
    private EditText firstName,lastName, email, password, retypePassword, cnp, age;
    private TextView sexString;
    private ImageButton female, male;
    private MaterialButton register;
    private Spinner bloodGroupInput;
    private InputValidator inputValidator = new InputValidator();
    String Colector = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firstName = findViewById(R.id.registerFirstNameInput);
        lastName = findViewById(R.id.registerLastNameInput);
        email = findViewById(R.id.registerEmailInput);
        password = findViewById(R.id.registerPasswordInput);
        retypePassword = findViewById(R.id.registerPasswordRetype);
        cnp = findViewById(R.id.cnpInput);
        age = findViewById(R.id.ageInput);
        female = findViewById(R.id.Female);
        male = findViewById(R.id.Male);
        register = findViewById(R.id.registerBtn);
        bloodGroupInput = (Spinner) findViewById(R.id.bloodGroupInput);
        sexString = (TextView) findViewById(R.id.sexString);

        female.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String display = "female";
                sexString.setText(display);
            }
        });

        male.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String display = "male";
                sexString.setText(display);
            }
        });

        List<String> bloodGroups = new ArrayList<>();
        bloodGroups.add("Select blood group");
        bloodGroups.add("A+");
        bloodGroups.add("A-");
        bloodGroups.add("B+");
        bloodGroups.add("B-");
        bloodGroups.add("AB+");
        bloodGroups.add("AB-");
        bloodGroups.add("0+");
        bloodGroups.add("0-");
        bloodGroups.add("UNKNOWN");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bloodGroups);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodGroupInput.setAdapter(arrayAdapter);
        bloodGroupInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (adapterView.getItemAtPosition(position).equals("Select blood group")) {
                    Colector = "UNKNOWN";
                } else {
                    String item = adapterView.getItemAtPosition(position).toString();
                    //Colector = adapterView.getItemAtPosition(position).toString();
                    switch (item) {
                        case "A+":
                            Colector = "Aplus";
                            break;
                        case "A-":
                            Colector = "Aminus";
                            break;
                        case "B+":
                            Colector = "Bplus";
                            break;
                        case "B-":
                            Colector = "Bminus";
                            break;
                        case "AB+":
                            Colector = "ABplus";
                            break;
                        case "AB-":
                            Colector = "ABminus";
                            break;
                        case "0+":
                            Colector = "Oplus";
                            break;
                        case "0-":
                            Colector = "Ominus";
                            break;
                        default:
                            Colector = "UNKNOWN";
                            break;
                    }
                }
            }



            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Colector = "UNKNOWN";
            }
        });

        findViewById(R.id.registerBtn).setOnClickListener(view -> registerUser());
        System.out.println("register");

    }


    private void registerUser() {
        HashMap<EditText, String> inputs = new HashMap<>();
        boolean isError = false;

        String firstNameString = firstName.getText().toString().trim();
        String lastNameString = lastName.getText().toString().trim();
        String emailString = email.getText().toString().trim();
        String passwordString = password.getText().toString().trim();
        String retypePasswordString = retypePassword.getText().toString().trim();
        String cnpString = cnp.getText().toString().trim();
        String ageString = age.getText().toString().trim();
        String bloodGroupString = bloodGroupInput.getSelectedItem().toString();

        inputs.put(firstName, firstNameString);
        inputs.put(lastName, lastNameString);
        inputs.put(email, emailString);
        inputs.put(password, passwordString);
        inputs.put(retypePassword, retypePasswordString);
        inputs.put(cnp, cnpString);
        inputs.put(age, ageString);

        for (EditText field : inputs.keySet()) {
            if (inputs.get(field).isEmpty()) {
                inputValidator.setFieldError(field, "This field cannot be empty");
                isError = true;
            }
        }


        if (sexString.getText().toString().equals("") || sexString.getText().toString().equals("Please select your sex.")) {
            sexString.setText("Please select your sex.");
            isError = true;
        }

        if (!inputs.get(retypePassword).isEmpty()) {
            if (!inputValidator.doStringsMatch(passwordString, retypePasswordString)) {
                inputValidator.setFieldError(retypePassword, "Passwords do not match");
                isError = true;
            } else {
                isError = false;
            }
        } else {
            isError = true;
        }
        if (!isError)

            this.makeCall(new User(firstNameString, lastNameString, emailString, passwordString, cnpString, ageString, sexString.getText().toString(), Colector));

    }

    private void makeCall (User newUser){
        Call<ResponseBody> call = RetrofitClient
                .getInstance()
                .getAPI()
                .createUser(newUser);

        call.enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Boolean success;
                success = response.isSuccessful();
                int requestCode = response.code();
                if (success) {
                    Toast.makeText(RegisterActivity.this, "Registered successfully. Please log in", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                } else {
                    if (requestCode == 400)
                        Toast.makeText(RegisterActivity.this, "Username or email is taken!", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(RegisterActivity.this, "Server Error", Toast.LENGTH_LONG).show();
                }
            }
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                System.out.println("failure: " + t.getMessage());
            }
        });
    }
}
