import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String DB_URL =
    "jdbc:mysql://localhost:3306/clicker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "myuser";
    private static final String DB_PASSWORD = "12345678";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null ||
            username.trim().isEmpty() || password.trim().isEmpty()) {
            out.print("LOGIN_FAIL");
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql = "SELECT password FROM users WHERE username = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String hashedPassword = PasswordUtil.hashPassword(password);

                if (hashedPassword.equals(storedPassword)) {
                    out.print("LOGIN_OK");
                } else if (password.equals(storedPassword)) {
                    updateStmt = conn.prepareStatement(
                            "UPDATE users SET password = ? WHERE username = ?");
                    updateStmt.setString(1, hashedPassword);
                    updateStmt.setString(2, username);
                    updateStmt.executeUpdate();
                    out.print("LOGIN_OK");
                } else {
                    out.print("LOGIN_FAIL");
                }
            } else {
                out.print("LOGIN_FAIL");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.print("LOGIN_FAIL");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
            try { if (updateStmt != null) updateStmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}
