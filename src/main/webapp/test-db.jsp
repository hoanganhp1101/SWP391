<%@ page import="java.sql.*" %>
<%@ page import="com.example.diabetesmanage.util.DBContext" %>
<%
    out.println("<h3>Patients</h3><ul>");
    try (Connection conn = DBContext.getConnection();
         Statement stmt = conn.createStatement()) {
        ResultSet rs = stmt.executeQuery("SELECT id, ho_ten FROM patients p JOIN users u ON p.user_id = u.id");
        while(rs.next()) {
            out.println("<li>" + rs.getString("id") + " - " + rs.getString("ho_ten") + "</li>");
        }
    } catch(Exception e) {
        out.println("Error: " + e.getMessage());
    }
    out.println("</ul>");
%>
