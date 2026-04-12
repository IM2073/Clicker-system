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

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        if (choice == null || choice.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("ERROR: missing choice parameter");
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql = "INSERT INTO responses (questionNo, choice) VALUES (1, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, choice.toLowerCase());
            stmt.executeUpdate();

            stmt.close();
            conn.close();

            out.print("OK");

        } catch (ClassNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("ERROR: JDBC driver not found - " + e.getMessage());
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("ERROR: Database error - " + e.getMessage());
        }
    }
}
