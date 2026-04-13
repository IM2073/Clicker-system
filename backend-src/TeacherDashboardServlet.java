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

public class TeacherDashboardServlet extends HttpServlet {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/clicker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "myuser";
    private static final String DB_PASSWORD = "12345678";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String status = request.getParameter("status");

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Teacher Dashboard</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; background: #f4f7fb; color: #17324d; margin: 0; padding: 32px; }");
        out.println(".page { max-width: 1000px; margin: 0 auto; }");
        out.println(".grid { display: grid; grid-template-columns: minmax(320px, 420px) 1fr; gap: 20px; align-items: start; }");
        out.println(".card { background: #fffdfb; border: 1px solid #d7e3f4; border-radius: 20px; box-shadow: 0 10px 30px rgba(15, 23, 42, 0.05); padding: 24px; }");
        out.println("h1, h2 { margin-top: 0; }");
        out.println("label { display: block; font-weight: bold; margin-bottom: 8px; margin-top: 14px; }");
        out.println("input, textarea { width: 100%; box-sizing: border-box; border: 1px solid #cbd9ee; border-radius: 14px; padding: 12px 14px; font-size: 14px; }");
        out.println("textarea { min-height: 120px; resize: vertical; }");
        out.println("button { background: #1d4ed8; color: white; border: none; border-radius: 16px; padding: 14px 18px; font-size: 15px; font-weight: bold; cursor: pointer; margin-top: 18px; width: 100%; }");
        out.println(".status { margin-bottom: 16px; background: #e8f1ff; border: 1px solid #c9daf8; border-radius: 14px; padding: 12px 14px; }");
        out.println(".question-item { border: 1px solid #e0e7f0; border-radius: 16px; padding: 16px; margin-bottom: 14px; background: #ffffff; }");
        out.println(".meta { color: #60758b; font-size: 13px; margin-bottom: 8px; }");
        out.println("ol { margin: 0; padding-left: 18px; }");
        out.println("a { color: #1d4ed8; text-decoration: none; }");
        out.println("@media (max-width: 800px) { .grid { grid-template-columns: 1fr; } }");
        out.println("</style></head><body>");
        out.println("<div class='page'>");
        out.println("<h1>Teacher's Webpage: Question Creator</h1>");
        out.println("<p>Create new four-option questions for students. Comments and votes will stay attached to the specific question.</p>");

        if (status != null && !status.trim().isEmpty()) {
            out.println("<div class='status'>" + TextUtil.escapeHtml(status) + "</div>");
        }

        out.println("<div class='grid'>");
        out.println("<section class='card'>");
        out.println("<h2>Create Question</h2>");
        out.println("<form method='post' action='" + request.getContextPath() + "/teacher/create'>");
        out.println("<label for='teacherName'>Teacher Name</label>");
        out.println("<input id='teacherName' name='teacherName' placeholder='e.g. Ms Tan' required>");
        out.println("<label for='questionText'>Question</label>");
        out.println("<textarea id='questionText' name='questionText' placeholder='Write the question prompt here' required></textarea>");
        out.println("<label for='optionA'>Option A</label>");
        out.println("<input id='optionA' name='optionA' placeholder='First answer option' required>");
        out.println("<label for='optionB'>Option B</label>");
        out.println("<input id='optionB' name='optionB' placeholder='Second answer option' required>");
        out.println("<label for='optionC'>Option C</label>");
        out.println("<input id='optionC' name='optionC' placeholder='Third answer option' required>");
        out.println("<label for='optionD'>Option D</label>");
        out.println("<input id='optionD' name='optionD' placeholder='Fourth answer option' required>");
        out.println("<button type='submit'>Create Question</button>");
        out.println("</form>");
        out.println("</section>");

        out.println("<section class='card'>");
        out.println("<h2>Current Questions</h2>");
        out.println("<p><a href='" + request.getContextPath() + "/display'>Open results dashboard</a></p>");
        renderQuestions(out);
        out.println("</section>");
        out.println("</div></div></body></html>");
    }

    private void renderQuestions(PrintWriter out) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            stmt = conn.prepareStatement(
                    "SELECT id, question_text, option_a, option_b, option_c, option_d, created_by, created_at " +
                            "FROM questions ORDER BY created_at DESC, id DESC");
            rs = stmt.executeQuery();

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                out.println("<article class='question-item'>");
                out.println("<div class='meta'>Question #" + rs.getInt("id")
                        + " · Created by " + TextUtil.escapeHtml(rs.getString("created_by"))
                        + " · " + TextUtil.escapeHtml(rs.getString("created_at")) + "</div>");
                out.println("<h3>" + TextUtil.escapeHtml(rs.getString("question_text")) + "</h3>");
                out.println("<ol type='A'>");
                out.println("<li>" + TextUtil.escapeHtml(rs.getString("option_a")) + "</li>");
                out.println("<li>" + TextUtil.escapeHtml(rs.getString("option_b")) + "</li>");
                out.println("<li>" + TextUtil.escapeHtml(rs.getString("option_c")) + "</li>");
                out.println("<li>" + TextUtil.escapeHtml(rs.getString("option_d")) + "</li>");
                out.println("</ol>");
                out.println("</article>");
            }

            if (!hasRows) {
                out.println("<p>No questions yet. Create the first one from the form.</p>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<p>Failed to load questions: " + TextUtil.escapeHtml(e.getMessage()) + "</p>");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}
