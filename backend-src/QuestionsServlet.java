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

public class QuestionsServlet extends HttpServlet {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/clicker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "myuser";
    private static final String DB_PASSWORD = "12345678";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            stmt = conn.prepareStatement(
                    "SELECT id, question_text, option_a, option_b, option_c, option_d " +
                            "FROM questions WHERE is_active = TRUE ORDER BY created_at DESC, id DESC");
            rs = stmt.executeQuery();

            StringBuilder json = new StringBuilder();
            json.append("{\"questions\":[");

            boolean first = true;
            while (rs.next()) {
                if (!first) {
                    json.append(",");
                }

                json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"prompt\":\"").append(TextUtil.escapeJson(rs.getString("question_text"))).append("\",")
                        .append("\"optionA\":\"").append(TextUtil.escapeJson(rs.getString("option_a"))).append("\",")
                        .append("\"optionB\":\"").append(TextUtil.escapeJson(rs.getString("option_b"))).append("\",")
                        .append("\"optionC\":\"").append(TextUtil.escapeJson(rs.getString("option_c"))).append("\",")
                        .append("\"optionD\":\"").append(TextUtil.escapeJson(rs.getString("option_d"))).append("\"")
                        .append("}");

                first = false;
            }

            json.append("]}");
            out.print(json);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"questions\":[],\"error\":\"" + TextUtil.escapeJson(e.getMessage()) + "\"}");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}
