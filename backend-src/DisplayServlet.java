import java.io.*;
import java.sql.*;
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
        out.println("body { font-family: Arial, sans-serif; padding: 20px; }");
        out.println("table { border-collapse: collapse; width: 300px; }");
        out.println("th, td { border: 1px solid #ccc; padding: 8px 16px; text-align: center; }");
        out.println("th { background-color: #4CAF50; color: white; }");
        out.println("</style></head><body>");
        out.println("<h2>Clicker Results - Question 1</h2>");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql = "SELECT choice, COUNT(*) AS total " +
                         "FROM responses WHERE questionNo = 1 " +
                         "GROUP BY choice ORDER BY choice";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            out.println("<table>");
            out.println("<tr><th>Choice</th><th>Votes</th></tr>");

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                out.println("<tr>");
                out.println("<td>" + rs.getString("choice").toUpperCase() + "</td>");
                out.println("<td>" + rs.getInt("total") + "</td>");
                out.println("</tr>");
            }

            if (!hasRows) {
                out.println("<tr><td colspan='2'>No votes yet</td></tr>");
            }

            out.println("</table>");

            rs.close();
            stmt.close();
            conn.close();

        } catch (ClassNotFoundException e) {
            out.println("<p style='color:red'>ERROR: JDBC driver not found - " + e.getMessage() + "</p>");
        } catch (SQLException e) {
            out.println("<p style='color:red'>ERROR: Database error - " + e.getMessage() + "</p>");
        }

        out.println("</body></html>");
    }
}
