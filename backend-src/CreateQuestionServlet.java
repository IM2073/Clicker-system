import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CreateQuestionServlet extends HttpServlet {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/clicker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "myuser";
    private static final String DB_PASSWORD = "12345678";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String teacherName = trim(request.getParameter("teacherName"));
        String questionText = trim(request.getParameter("questionText"));
        String optionA = trim(request.getParameter("optionA"));
        String optionB = trim(request.getParameter("optionB"));
        String optionC = trim(request.getParameter("optionC"));
        String optionD = trim(request.getParameter("optionD"));

        if (teacherName.isEmpty() || questionText.isEmpty()
                || optionA.isEmpty() || optionB.isEmpty()
                || optionC.isEmpty() || optionD.isEmpty()) {
            redirectWithStatus(response, request.getContextPath(),
                    "Please fill in the teacher name, question, and all four options.");
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            stmt = conn.prepareStatement(
                    "INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, created_by, is_active) " +
                            "VALUES (?, ?, ?, ?, ?, ?, TRUE)");
            stmt.setString(1, questionText);
            stmt.setString(2, optionA);
            stmt.setString(3, optionB);
            stmt.setString(4, optionC);
            stmt.setString(5, optionD);
            stmt.setString(6, teacherName);
            stmt.executeUpdate();

            redirectWithStatus(response, request.getContextPath(), "Question created successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            redirectWithStatus(response, request.getContextPath(), "Failed to create question: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }

    private void redirectWithStatus(HttpServletResponse response, String contextPath, String status)
            throws IOException {
        response.sendRedirect(contextPath + "/teacher?status="
                + URLEncoder.encode(status, StandardCharsets.UTF_8));
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
