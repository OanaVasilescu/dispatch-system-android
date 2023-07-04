package com.example.dispatchsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dispatchsystem.api.RetrofitClient;
import com.example.dispatchsystem.model.Credentials;
import com.example.dispatchsystem.model.Globals;
import com.example.dispatchsystem.model.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText passwordText, emailText;
    private TextView registerField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        registerField = (TextView)findViewById(R.id.registerField);

        findViewById(R.id.loginBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        findViewById(R.id.registerField).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

    }

    private void loginUser() {
        final String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        boolean errorFlag = false;

        if(email.isEmpty()) {
            emailText.setError("Email required");
            emailText.requestFocus();
            errorFlag = true;
        }

        if(password.isEmpty()) {
            passwordText.setError("Password required");
            passwordText.requestFocus();
            errorFlag = true;
        }

        if(!errorFlag) {
            Credentials user = new Credentials();
            user.setEmail(emailText.getText().toString()); // check
            user.setPassword(passwordText.getText().toString());
            //Log.e("Chestie", emailText.toString() );
            this.makeLoginCall(user);
        }

    }

    private void makeLoginCall(Credentials user) {
        Call<User> call = RetrofitClient
                .getInstance()
                .getAPI()
                .checkUser(user);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Boolean success;
                success = response.isSuccessful();

                int requestCode = response.code();

                if(success) {
                    User crtUser = response.body();
                    Globals.setCurrentUser(crtUser);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } else {
                    if(requestCode == 500) {
                        Toast.makeText(LoginActivity.this, "Server error", Toast.LENGTH_LONG).show();
                    }
                    else {
                        if (requestCode == 400) {
                            Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_LONG).show();
                        }
                        else
                            Toast.makeText(LoginActivity.this, "Authentication error. Email or password does not exist.", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();

            }
        });

    }
}