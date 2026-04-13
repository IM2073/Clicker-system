import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AddCommentServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/clicker";
    private static final String DB_USER = "myuser";
    private static final String DB_PASSWORD = "12345678";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        String username = request.getParameter("username");
        String comment = request.getParameter("comment");
        String questionIdParam = request.getParameter("questionId");

        if (username == null || username.trim().isEmpty()) {
            out.print("ERROR: missing username");
            return;
        }

        if (comment == null || comment.trim().isEmpty()) {
            out.print("ERROR: empty comment");
            return;
        }

        int questionId;
        try {
            questionId = Integer.parseInt(questionIdParam);
        } catch (Exception e) {
            out.print("ERROR: invalid question id");
            return;
        }

        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement checkQuestionStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet rs = null;
        ResultSet questionRs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String checkUserSql = "SELECT * FROM users WHERE username = ?";
            checkStmt = conn.prepareStatement(checkUserSql);
            checkStmt.setString(1, username);
            rs = checkStmt.executeQuery();

            if (!rs.next()) {
                out.print("ERROR: user not registered");
                return;
            }

            String checkQuestionSql = "SELECT id FROM questions WHERE id = ? AND is_active = TRUE";
            checkQuestionStmt = conn.prepareStatement(checkQuestionSql);
            checkQuestionStmt.setInt(1, questionId);
            questionRs = checkQuestionStmt.executeQuery();

            if (!questionRs.next()) {
                out.print("ERROR: question not found");
                return;
            }

            String insertCommentSql =
                    "INSERT INTO comments (question_id, username, comment_text) VALUES (?, ?, ?)";
            insertStmt = conn.prepareStatement(insertCommentSql);
            insertStmt.setInt(1, questionId);
            insertStmt.setString(2, username);
            insertStmt.setString(3, comment);

            int rows = insertStmt.executeUpdate();

            if (rows > 0) {
                out.print("SUCCESS");
            } else {
                out.print("ERROR: failed to add comment");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.print("ERROR: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (questionRs != null) questionRs.close(); } catch (Exception e) {}
            try { if (checkStmt != null) checkStmt.close(); } catch (Exception e) {}
            try { if (checkQuestionStmt != null) checkQuestionStmt.close(); } catch (Exception e) {}
            try { if (insertStmt != null) insertStmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}
