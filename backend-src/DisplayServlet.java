import java.io.*;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class DisplayServlet extends HttpServlet {

    private static final String DB_URL      = "jdbc:mysql://localhost:3306/clicker";
    private static final String DB_USER     = "myuser";
    private static final String DB_PASSWORD = "12345678";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Clicker Results</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; background: #f4f7fb; color: #1f2937; margin: 0; padding: 32px; }");
        out.println(".page { max-width: 960px; margin: 0 auto; }");
        out.println(".header { margin-bottom: 24px; }");
        out.println(".header h1 { margin-bottom: 8px; }");
        out.println(".question-card { background: #ffffff; border: 1px solid #dbe4f0; border-radius: 16px; padding: 20px; margin-bottom: 20px; box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06); }");
        out.println(".question-card h2 { margin-top: 0; font-size: 20px; }");
        out.println(".meta { color: #6b7280; font-size: 13px; margin-bottom: 10px; }");
        out.println("table { border-collapse: collapse; width: 100%; margin-top: 12px; }");
        out.println("th, td { border-bottom: 1px solid #e5e7eb; padding: 12px 16px; text-align: left; }");
        out.println("th { background-color: #0f4c81; color: white; }");
        out.println(".empty { color: #6b7280; }");
        out.println("</style></head><body>");
        out.println("<div class='page'>");
        out.println("<div class='header'>");
        out.println("<h1>Clicker Results Dashboard</h1>");
        out.println("<p>Summary of responses collected for all active teacher-created questions.</p>");
        out.println("</div>");

        Connection conn = null;
        PreparedStatement questionStmt = null;
        PreparedStatement resultStmt = null;
        ResultSet questionRs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            questionStmt = conn.prepareStatement(
                    "SELECT id, question_text, option_a, option_b, option_c, option_d, created_by, created_at " +
                            "FROM questions ORDER BY created_at DESC, id DESC");
            resultStmt = conn.prepareStatement(
                    "SELECT choice, COUNT(*) AS total FROM responses " +
                            "WHERE question_id = ? GROUP BY choice ORDER BY choice");
            questionRs = questionStmt.executeQuery();

            boolean hasQuestions = false;
            while (questionRs.next()) {
                hasQuestions = true;
                int questionId = questionRs.getInt("id");
                resultStmt.setInt(1, questionId);

                ResultSet rs = resultStmt.executeQuery();
                Map<String, Integer> totals = new LinkedHashMap<>();
                totals.put("A", 0);
                totals.put("B", 0);
                totals.put("C", 0);
                totals.put("D", 0);

                while (rs.next()) {
                    totals.put(rs.getString("choice").toUpperCase(), rs.getInt("total"));
                }

                out.println("<section class='question-card'>");
                out.println("<h2>Question #" + questionId + "</h2>");
                out.println("<div class='meta'>Created by " + TextUtil.escapeHtml(questionRs.getString("created_by"))
                        + " · " + TextUtil.escapeHtml(questionRs.getString("created_at")) + "</div>");
                out.println("<p>" + TextUtil.escapeHtml(questionRs.getString("question_text")) + "</p>");
                out.println("<table>");
                out.println("<tr><th>Option</th><th>Answer Text</th><th>Votes</th></tr>");
                out.println(renderRow("A", questionRs.getString("option_a"), totals.get("A")));
                out.println(renderRow("B", questionRs.getString("option_b"), totals.get("B")));
                out.println(renderRow("C", questionRs.getString("option_c"), totals.get("C")));
                out.println(renderRow("D", questionRs.getString("option_d"), totals.get("D")));

                out.println("</table>");
                out.println("</section>");
                rs.close();
            }

            if (!hasQuestions) {
                out.println("<section class='question-card'><p class='empty'>No questions have been created yet.</p></section>");
            }

        } catch (ClassNotFoundException e) {
            out.println("<p style='color:red'>ERROR: JDBC driver not found - " + e.getMessage() + "</p>");
        } catch (SQLException e) {
            out.println("<p style='color:red'>ERROR: Database error - " + e.getMessage() + "</p>");
        } finally {
            try { if (questionRs != null) questionRs.close(); } catch (Exception e) {}
            try { if (questionStmt != null) questionStmt.close(); } catch (Exception e) {}
            try { if (resultStmt != null) resultStmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }

        out.println("</div></body></html>");
    }

    private String renderRow(String label, String optionText, int votes) {
        return "<tr><td>" + label + "</td><td>" + TextUtil.escapeHtml(optionText)
                + "</td><td>" + votes + "</td></tr>";
    }
}
