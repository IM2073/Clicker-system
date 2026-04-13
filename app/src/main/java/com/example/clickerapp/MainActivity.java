package com.example.clickerapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:9999/clicker/select";
    private static final String QUESTIONS_URL = "http://10.0.2.2:9999/clicker/questions";

    private TextView tvStatus;
    private TextView tvQuestionNumber;
    private TextView tvQuestion;
    private TextView tvCommentTitle;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final String ADD_COMMENT_URL = "http://10.0.2.2:9999/clicker/AddCommentServlet";
    private static final String GET_COMMENTS_URL = "http://10.0.2.2:9999/clicker/GetCommentsServlet";

    private TextView tvComments;
    private EditText etComment;
    private String username;
    private Button btnA;
    private Button btnB;
    private Button btnC;
    private Button btnD;
    private Button btnPrevious;
    private Button btnNext;
    private Button btnRefresh;
    private int currentQuestionIndex = 0;
    private final List<Question> questions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvCommentTitle = findViewById(R.id.tvCommentTitle);
        username = getIntent().getStringExtra("username");

        btnA = findViewById(R.id.btnA);
        btnB = findViewById(R.id.btnB);
        btnC = findViewById(R.id.btnC);
        btnD = findViewById(R.id.btnD);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnRefresh = findViewById(R.id.btnRefresh);

        btnA.setOnClickListener(v -> sendChoice("a"));
        btnB.setOnClickListener(v -> sendChoice("b"));
        btnC.setOnClickListener(v -> sendChoice("c"));
        btnD.setOnClickListener(v -> sendChoice("d"));
        btnPrevious.setOnClickListener(v -> showQuestion(currentQuestionIndex - 1));
        btnNext.setOnClickListener(v -> showQuestion(currentQuestionIndex + 1));
        btnRefresh.setOnClickListener(v -> loadQuestions());

        tvComments = findViewById(R.id.tvComments);
        etComment = findViewById(R.id.etComment);

        Button btnSubmitComment = findViewById(R.id.btnSubmitComment);
        btnSubmitComment.setOnClickListener(v -> submitComment());

        setInteractionEnabled(false);
        loadQuestions();
    }

    private void sendChoice(String choice) {
        if (questions.isEmpty()) {
            tvStatus.setText("No active questions available.");
            return;
        }

        tvStatus.setText("Sending...");

        new Thread(() -> {
            String result;
            Question currentQuestion = questions.get(currentQuestionIndex);
            int displayQuestionNumber = currentQuestionIndex + 1;
            try {
                String requestUrl = BASE_URL
                        + "?questionId=" + currentQuestion.id
                        + "&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                        + "&choice=" + URLEncoder.encode(choice, StandardCharsets.UTF_8);

                URL url = URI.create(requestUrl).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                result = readResponse(conn);
                conn.disconnect();
            } catch (Exception e) {
                result = "ERROR: " + e.getMessage();
            }

            String finalResult = result;
            handler.post(() -> {
                String status = "Question " + displayQuestionNumber
                        + " · Choice " + choice.toUpperCase(Locale.US)
                        + ": " + finalResult;
                tvStatus.setText(status);
            });
        }).start();
    }

    private void submitComment() {
        String comment = etComment.getText().toString().trim();

        if (comment.isEmpty()) {
            tvStatus.setText("Comment cannot be empty");
            return;
        }

        if (questions.isEmpty()) {
            tvStatus.setText("No question selected for discussion.");
            return;
        }

        tvStatus.setText("Submitting comment...");

        new Thread(() -> {
            String result;
            try {
                Question currentQuestion = questions.get(currentQuestionIndex);
                String data = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                        + "&questionId=" + currentQuestion.id
                        + "&comment=" + URLEncoder.encode(comment, StandardCharsets.UTF_8);

                URL url = URI.create(ADD_COMMENT_URL).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(data.getBytes(StandardCharsets.UTF_8));
                }

                result = readResponse(conn);
                conn.disconnect();

            } catch (Exception e) {
                result = "ERROR: " + e.getMessage();
            }

            String finalResult = result;
            handler.post(() -> {
                tvStatus.setText(finalResult);

                if ("SUCCESS".equals(finalResult)) {
                    etComment.setText("");
                    loadComments(questions.get(currentQuestionIndex).id);
                }
            });

        }).start();
    }

    private void loadQuestions() {
        tvStatus.setText("Loading questions...");
        setInteractionEnabled(false);

        new Thread(() -> {
            List<Question> loadedQuestions = new ArrayList<>();
            String errorMessage = null;

            try {
                URL url = URI.create(QUESTIONS_URL).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                String responseBody = readResponse(conn);
                conn.disconnect();

                JSONObject payload = new JSONObject(responseBody);
                JSONArray items = payload.getJSONArray("questions");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    loadedQuestions.add(new Question(
                            item.getInt("id"),
                            item.getString("prompt"),
                            new String[]{
                                    "A. " + item.getString("optionA"),
                                    "B. " + item.getString("optionB"),
                                    "C. " + item.getString("optionC"),
                                    "D. " + item.getString("optionD")
                            }
                    ));
                }

            } catch (Exception e) {
                errorMessage = e.getMessage();
            }

            String finalErrorMessage = errorMessage;
            handler.post(() -> {
                questions.clear();
                questions.addAll(loadedQuestions);

                if (finalErrorMessage != null) {
                    tvQuestionNumber.setText("Question list unavailable");
                    tvQuestion.setText("Unable to load teacher-created questions right now.");
                    tvCommentTitle.setText("Discussion");
                    tvComments.setText("Comments will appear here once questions load.");
                    tvStatus.setText("Unable to load questions: " + finalErrorMessage);
                    setInteractionEnabled(false);
                    return;
                }

                if (questions.isEmpty()) {
                    tvQuestionNumber.setText("No active questions");
                    tvQuestion.setText("Ask the teacher to create a question from the backend teacher page.");
                    tvCommentTitle.setText("Discussion");
                    tvComments.setText("No comments yet because there are no active questions.");
                    tvStatus.setText("No active questions available.");
                    setInteractionEnabled(false);
                    return;
                }

                if (currentQuestionIndex >= questions.size()) {
                    currentQuestionIndex = 0;
                }

                setInteractionEnabled(true);
                showQuestion(currentQuestionIndex);
                tvStatus.setText("Loaded " + questions.size() + " question(s).");
            });
        }).start();
    }

    private void loadComments(int questionId) {

        new Thread(() -> {
            StringBuilder result = new StringBuilder();
            int requestedQuestionId = questionId;

            try {
                URL url = URI.create(GET_COMMENTS_URL + "?questionId=" + questionId).toURL();
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
                if (questions.isEmpty()) {
                    return;
                }

                Question currentQuestion = questions.get(currentQuestionIndex);
                if (currentQuestion.id != requestedQuestionId) {
                    return;
                }

                if (result.toString().trim().isEmpty()) {
                    tvComments.setText("No comments yet");
                } else {
                    tvComments.setText(result.toString());
                }
            });

        }).start();
    }

    private void showQuestion(int newIndex) {
        if (newIndex < 0 || newIndex >= questions.size()) {
            return;
        }

        currentQuestionIndex = newIndex;
        Question question = questions.get(currentQuestionIndex);

        tvQuestionNumber.setText(String.format(
                Locale.US,
                "Question %d of %d",
                currentQuestionIndex + 1,
                questions.size()
        ));
        tvQuestion.setText(question.prompt);
        btnA.setText(question.options[0]);
        btnB.setText(question.options[1]);
        btnC.setText(question.options[2]);
        btnD.setText(question.options[3]);
        tvCommentTitle.setText(String.format(
                Locale.US,
                "Discussion · Question %d",
                currentQuestionIndex + 1
        ));
        btnPrevious.setEnabled(currentQuestionIndex > 0);
        btnNext.setEnabled(currentQuestionIndex < questions.size() - 1);
        tvComments.setText("Loading discussion...");
        loadComments(question.id);
    }

    private void setInteractionEnabled(boolean enabled) {
        btnA.setEnabled(enabled);
        btnB.setEnabled(enabled);
        btnC.setEnabled(enabled);
        btnD.setEnabled(enabled);
        btnPrevious.setEnabled(enabled && currentQuestionIndex > 0);
        btnNext.setEnabled(enabled && currentQuestionIndex < questions.size() - 1);
        btnRefresh.setEnabled(true);
        etComment.setEnabled(enabled);
        findViewById(R.id.btnSubmitComment).setEnabled(enabled);
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        int responseCode = conn.getResponseCode();
        BufferedReader reader;

        if (responseCode >= 400) {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        }

        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(line);
        }
        reader.close();

        if (responseCode >= 400) {
            throw new IllegalStateException(builder.toString().isEmpty()
                    ? "HTTP " + responseCode
                    : builder.toString());
        }

        return builder.toString();
    }

    private static class Question {
        final int id;
        final String prompt;
        final String[] options;

        Question(int id, String prompt, String[] options) {
            this.id = id;
            this.prompt = prompt;
            this.options = options;
        }
    }
}
