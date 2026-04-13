package com.example.clickerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        EditText usernameEt = findViewById(R.id.username);
        EditText passwordEt = findViewById(R.id.password);
        EditText confirmPasswordEt = findViewById(R.id.confirmPassword);
        Button registerBtn = findViewById(R.id.registerBtn);
        Button backToLoginBtn = findViewById(R.id.backToLoginBtn);

        registerBtn.setOnClickListener(v -> {
            String username = usernameEt.getText().toString().trim();
            String password = passwordEt.getText().toString().trim();
            String confirmPassword = confirmPasswordEt.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "http://10.0.2.2:9999/clicker/register?username="
                    + URLEncoder.encode(username, StandardCharsets.UTF_8)
                    + "&password="
                    + URLEncoder.encode(password, StandardCharsets.UTF_8);

            new Thread(() -> {
                try {
                    URL u = new URL(url);
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(u.openStream())
                    );
                    String result = br.readLine();

                    runOnUiThread(() -> {
                        if ("REGISTER_OK".equals(result)) {
                            Toast.makeText(this, "Register Success", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(this, LoginActivity.class);
                            startActivity(intent);
                            finish();

                        } else if ("USERNAME_EXISTS".equals(result)) {
                            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Register Failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        });

        backToLoginBtn.setOnClickListener(v -> {
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
