<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // Trang chủ = đăng nhập chung; role được xác định sau khi login
    response.sendRedirect(request.getContextPath() + "/Logincontroller");
%>
