package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.UserDAO;
import com.example.diabetesmanage.model.User;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet(name = "AdminLoginServlet", urlPatterns = {"/admin/login"})
public class AdminLoginServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("adminUser") != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/admin-login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe"); // Lấy giá trị của checkbox

        try {
            User user = userDAO.authenticateAdmin(email, password); // Dùng hàm mới viết lúc nãy

            if (user != null) {
                HttpSession session = request.getSession();
                session.setAttribute("adminUser", user);

                if ("ON".equals(rememberMe)) {
                    Cookie cookieEmail = new Cookie("adminEmail", email);
                    Cookie cookiePass = new Cookie("adminPass", password);

                    cookieEmail.setMaxAge(60 * 60 * 24 * 30);
                    cookiePass.setMaxAge(60 * 60 * 24 * 30);

                    response.addCookie(cookieEmail);
                    response.addCookie(cookiePass);
                } else {
                    Cookie cookieEmail = new Cookie("adminEmail", "");
                    Cookie cookiePass = new Cookie("adminPass", "");

                    cookieEmail.setMaxAge(0);
                    cookiePass.setMaxAge(0);

                    response.addCookie(cookieEmail);
                    response.addCookie(cookiePass);
                }

                response.sendRedirect(request.getContextPath() + "/dashboard");

            } else {
                request.setAttribute("errorMessage", "Email hoặc mật khẩu không chính xác hoặc bạn không có quyền Admin.");
                request.getRequestDispatcher("/WEB-INF/views/admin/admin-login.jsp").forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Lỗi hệ thống. Vui lòng thử lại sau.");
            request.getRequestDispatcher("/WEB-INF/views/admin/admin-login.jsp").forward(request, response);
        }
    }
}