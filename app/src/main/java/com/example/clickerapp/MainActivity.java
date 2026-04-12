package com.example.clickerapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:9999/clicker/select?choice=";

    private TextView tvStatus;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final String ADD_COMMENT_URL = "http://10.0.2.2:9999/clicker/AddCommentServlet";
    private static final String GET_COMMENTS_URL = "http://10.0.2.2:9999/clicker/GetCommentsServlet";

    private TextView tvComments;
    private android.widget.EditText etComment;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        username = getIntent().getStringExtra("username");

        findViewById(R.id.btnA).setOnClickListener(v -> sendChoice("a"));
        findViewById(R.id.btnB).setOnClickListener(v -> sendChoice("b"));
        findViewById(R.id.btnC).setOnClickListener(v -> sendChoice("c"));
        findViewById(R.id.btnD).setOnClickListener(v -> sendChoice("d"));

        tvComments = findViewById(R.id.tvComments);
        etComment = findViewById(R.id.etComment);

        Button btnSubmitComment = findViewById(R.id.btnSubmitComment);
        btnSubmitComment.setOnClickListener(v -> submitComment());
        loadComments();
    }

    private void sendChoice(String choice) {
        tvStatus.setText("Sending...");

        new Thread(() -> {
            String result;
            try {
                URL url = URI.create(BASE_URL + choice).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                result = reader.readLine();
                reader.close();
                conn.disconnect();
            } catch (Exception e) {
                result = "ERROR: " + e.getMessage();
            }

            String finalResult = result;
            handler.post(() -> tvStatus.setText("Choice " + choice.toUpperCase() + ": " + finalResult));
        }).start();
    }

    private void submitComment() {
        String comment = etComment.getText().toString().trim();

        if (comment.isEmpty()) {
            tvStatus.setText("Comment cannot be empty");
            return;
        }

        tvStatus.setText("Submitting comment...");

        new Thread(() -> {
            String result;
            try {


                String data = "username=" + username + "&comment=" + comment;

                URL url = URI.create(ADD_COMMENT_URL).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                conn.getOutputStream().write(data.getBytes());

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                result = reader.readLine();

                reader.close();
                conn.disconnect();

            } catch (Exception e) {
                result = "ERROR: " + e.getMessage();
            }

            String finalResult = result;
            handler.post(() -> {
                tvStatus.setText(finalResult);

                if ("SUCCESS".equals(finalResult)) {
                    etComment.setText("");
                    loadComments();
                }
            });

        }).start();
    }

    private void loadComments() {

        new Thread(() -> {
            StringBuilder result = new StringBuilder();

            try {
                URL url = URI.create(GET_COMMENTS_URL).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }

                reader.close();
                conn.disconnect();

            } catch (Exception e) {
                result.append("ERROR: ").append(e.getMessage());
            }

            handler.post(() -> {
                if (result.toString().trim().isEmpty()) {
                    tvComments.setText("No comments yet");
                } else {
                    tvComments.setText(result.toString());
                }
            });

        }).start();
    }
}
