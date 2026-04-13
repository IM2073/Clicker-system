import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class SelectServlet extends HttpServlet {

    private static final String DB_URL      = "jdbc:mysql://localhost:3306/clicker";
    private static final String DB_USER     = "myuser";
    private static final String DB_PASSWORD = "12345678";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String choice = request.getParameter("choice");
        String questionIdParam = request.getParameter("questionId");
        String username = request.getParameter("username");

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        if (choice == null || choice.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("ERROR: missing choice parameter");
            return;
        }

        if (!choice.matches("[a-dA-D]")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("ERROR: choice must be one of A, B, C, or D");
            return;
        }

        int questionId;
        try {
            questionId = Integer.parseInt(questionIdParam);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("ERROR: invalid question id");
            return;
        }

        Connection conn = null;
        PreparedStatement checkQuestionStmt = null;
        PreparedStatement checkUserStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet questionRs = null;
        ResultSet userRs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            checkQuestionStmt = conn.prepareStatement(
                    "SELECT id FROM questions WHERE id = ? AND is_active = TRUE");
            checkQuestionStmt.setInt(1, questionId);
            questionRs = checkQuestionStmt.executeQuery();

            if (!questionRs.next()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("ERROR: question not found");
                return;
            }

            if (username != null && !username.trim().isEmpty()) {
                checkUserStmt = conn.prepareStatement("SELECT username FROM users WHERE username = ?");
                checkUserStmt.setString(1, username.trim());
                userRs = checkUserStmt.executeQuery();

                if (!userRs.next()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("ERROR: user not found");
                    return;
                }
            } else {
                username = null;
            }

            insertStmt = conn.prepareStatement(
                    "INSERT INTO responses (question_id, username, choice) VALUES (?, ?, ?)");
            insertStmt.setInt(1, questionId);
            insertStmt.setString(2, username);
            insertStmt.setString(3, choice.toLowerCase());
            insertStmt.executeUpdate();

            out.print("OK");

        } catch (ClassNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("ERROR: JDBC driver not found - " + e.getMessage());
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("ERROR: Database error - " + e.getMessage());
        } finally {
            try { if (questionRs != null) questionRs.close(); } catch (Exception e) {}
            try { if (userRs != null) userRs.close(); } catch (Exception e) {}
            try { if (checkQuestionStmt != null) checkQuestionStmt.close(); } catch (Exception e) {}
            try { if (checkUserStmt != null) checkUserStmt.close(); } catch (Exception e) {}
            try { if (insertStmt != null) insertStmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}
