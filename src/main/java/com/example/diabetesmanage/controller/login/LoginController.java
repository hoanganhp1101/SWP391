package com.example.diabetesmanage.controller.login;

import com.example.diabetesmanage.dao.UserDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.util.Encode;

/**
 * LoginController — xử lý đăng nhập với hashed password. URL: /Logincontroller
 */
@WebServlet(name = "Logincontroller", urlPatterns = {"/Logincontroller"})
public class LoginController extends HttpServlet {

    private static final String LOGIN_VIEW = "/WEB-INF/views/auth/login.jsp";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        String service = request.getParameter("service");

        // Hiển thị trang login (nếu đã đăng nhập thì vào đúng portal theo role)
        if (service == null) {
            if (session != null) {
                User current = (User) session.getAttribute("user");
                if (current != null) {
                    redirectByRole(response, request.getContextPath(), current.getVaiTro());
                    return;
                }
            }
            request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
            return;
        }

        if (service.equals("logout")) {
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        if (session == null) {
            session = request.getSession(true);
        }

        // Chỉ cho phép forward khi đã đăng nhập đúng role
        if (service.equals("admin")) {
            User current = (User) session.getAttribute("user");
            if (current == null || !"quan_tri_vien".equalsIgnoreCase(current.getVaiTro())) {
                response.sendRedirect(request.getContextPath() + "/Logincontroller");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/admin-dashboard");
            return;
        }
        if (service.equals("user")) {
            User current = (User) session.getAttribute("user");
            if (current == null || !"benh_nhan".equalsIgnoreCase(current.getVaiTro())) {
                response.sendRedirect(request.getContextPath() + "/Logincontroller");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/patient-dashboard");
            return;
        }

        // ── Xử lý đăng nhập ──────────────────────────────────────────────────
        if (service.equals("checkaccount")) {
            String inputUser = request.getParameter("UserName");
            String inputPass = request.getParameter("password");

            // Giữ lại giá trị đã nhập để hiển thị lại nếu lỗi
            request.setAttribute("UserName", inputUser);

            boolean hasError = false;

            // Validate username / email
            if (inputUser == null || inputUser.isBlank()) {
                request.setAttribute("emailError", "Vui lòng nhập email hoặc tên đăng nhập");
                hasError = true;

            } else if (inputUser.length() < 6 || inputUser.length() > 100) {
                request.setAttribute("emailError", "Email/username phải nằm trong khoảng từ 6-100 ký tự");
                hasError = true;
            }

            // Validate password
            if (inputPass == null || inputPass.isBlank()) {
                request.setAttribute("passError", "Vui lòng nhập mật khẩu");
                hasError = true;
            } else if (inputPass.length() > 50) {
                request.setAttribute("passError", "Mật khẩu không được vượt quá 50 ký tự");
                hasError = true;
            }
            
            if (hasError) {
                request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
                return;
            }
            
            Encode encoder = new Encode();
            String hashedPass = encoder.Encode(inputPass);

            UserDAO dao = UserDAO.getInstance();
            User user = dao.checkLogin(inputUser, hashedPass);

            if (user == null) {
                request.setAttribute("AccountError", "Email/tên đăng nhập hoặc mật khẩu không đúng");
                request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
                return;
            }

            if (!user.isKichHoat()) {
                request.setAttribute("AccountError", "Tài khoản đã bị khóa.");
                request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
                return;
            }

            // Đăng nhập thành công → set session (đồng bộ các key legacy)
            session.setAttribute("user", user);
            session.setAttribute("loginUser", user);

            String role = user.getVaiTro();
            if ("quan_tri_vien".equalsIgnoreCase(role)) {
                session.setAttribute("adminUser", user);
                session.setAttribute("status", 1);
                response.sendRedirect(request.getContextPath() + "/admin-dashboard");
            } else if ("benh_nhan".equalsIgnoreCase(role)) {
                session.setAttribute("status", 2);
                response.sendRedirect(request.getContextPath() + "/patient-dashboard");
            } else if ("bac_si".equalsIgnoreCase(role)) {
                session.setAttribute("status", 3);
                response.sendRedirect(request.getContextPath() + "/doctor-dashboard");
            } else {
                request.setAttribute("AccountError", "Vai trò không được hỗ trợ đăng nhập.");
                request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
            }

        }
    }

    private void redirectByRole(HttpServletResponse response, String contextPath, String role)
            throws IOException {
        if ("quan_tri_vien".equalsIgnoreCase(role)) {
            response.sendRedirect(contextPath + "/admin-dashboard");
        } else if ("benh_nhan".equalsIgnoreCase(role)) {
            response.sendRedirect(contextPath + "/patient-dashboard");
        } else if ("bac_si".equalsIgnoreCase(role)) {
            response.sendRedirect(contextPath + "/doctor-dashboard");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Login Controller — Diabetes Support System";
    }
}
