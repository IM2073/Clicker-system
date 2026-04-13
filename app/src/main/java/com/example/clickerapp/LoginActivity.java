package com.example.clickerapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        Button loginBtn = findViewById(R.id.loginBtn);
        Button registerBtn = findViewById(R.id.registerBtn);

        loginBtn.setOnClickListener(v -> {
            String username = ((EditText)findViewById(R.id.username)).getText().toString();
            String password = ((EditText)findViewById(R.id.password)).getText().toString();

            String url = "http://10.0.2.2:9999/clicker/login?username="
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
                        if ("LOGIN_OK".equals(result)) {
                            Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();

                            //jump to select page
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });

//to the register page
        registerBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
