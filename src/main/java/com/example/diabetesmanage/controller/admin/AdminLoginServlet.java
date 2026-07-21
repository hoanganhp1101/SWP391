package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.UserDAO;
import com.example.diabetesmanage.model.User;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AdminLoginServlet", urlPatterns = {"/admin/login"})
public class AdminLoginServlet extends HttpServlet {

    private UserDAO userDAO;

    public AdminLoginServlet() {
    }

    AdminLoginServlet(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void init() throws ServletException {
        if (userDAO == null) {
            userDAO = new UserDAO();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("adminUser") != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        if ("1".equals(request.getParameter("required"))) {
            request.setAttribute("errorMessage", "Vui long dang nhap bang tai khoan quan tri de tiep tuc.");
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/admin-login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String email = normalize(request.getParameter("email"));
        String password = request.getParameter("password");
        boolean rememberMe = "on".equalsIgnoreCase(request.getParameter("rememberMe"));

        if (email.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("errorMessage", "Vui lòng nhập đầy đủ email và mật khẩu.");
            request.setAttribute("loginEmail", email);
            request.getRequestDispatcher("/WEB-INF/views/admin/admin-login.jsp").forward(request, response);
            return;
        }

        try {
            User user = userDAO.authenticateAdmin(email, password);

            if (user != null) {
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }

                HttpSession session = request.getSession(true);
                session.setAttribute("adminUser", user);
                session.setAttribute("loginUser", user);
                session.setMaxInactiveInterval(30 * 60);

                updateRememberCookies(request, response, email, rememberMe);
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }

            request.setAttribute("errorMessage", "Email hoặc mật khẩu không chính xác, hoặc tài khoản không có quyền quản trị.");
            request.setAttribute("loginEmail", email);
            request.getRequestDispatcher("/WEB-INF/views/admin/admin-login.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Lỗi hệ thống. Vui lòng thử lại sau.");
            request.setAttribute("loginEmail", email);
            request.getRequestDispatcher("/WEB-INF/views/admin/admin-login.jsp").forward(request, response);
        }
    }

    private void updateRememberCookies(HttpServletRequest request, HttpServletResponse response, String email, boolean rememberMe) {
        Cookie emailCookie = new Cookie("adminEmail", rememberMe ? email : "");
        emailCookie.setHttpOnly(true);
        emailCookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        emailCookie.setMaxAge(rememberMe ? 60 * 60 * 24 * 30 : 0);
        if (request.isSecure()) {
            emailCookie.setSecure(true);
        }
        response.addCookie(emailCookie);

        Cookie legacyPasswordCookie = new Cookie("adminPass", "");
        legacyPasswordCookie.setHttpOnly(true);
        legacyPasswordCookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        legacyPasswordCookie.setMaxAge(0);
        if (request.isSecure()) {
            legacyPasswordCookie.setSecure(true);
        }
        response.addCookie(legacyPasswordCookie);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
