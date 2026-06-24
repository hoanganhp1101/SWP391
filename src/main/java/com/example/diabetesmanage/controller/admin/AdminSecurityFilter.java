package com.example.diabetesmanage.controller.admin;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter(urlPatterns = {"/dashboard", "/patient-manager", "/admin/users"})
public class AdminSecurityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        boolean isLoggedInAdmin = (session != null && session.getAttribute("adminUser") != null);

        if (isLoggedInAdmin) {
            chain.doFilter(request, response);
        } else {
            httpRequest.setAttribute("errorMessage", "Vui lòng đăng nhập tài khoản Quản trị để truy cập khu vực này.");
            httpRequest.getRequestDispatcher("/WEB-INF/views/admin/admin-login.jsp").forward(httpRequest, httpResponse);
        }
    }
}