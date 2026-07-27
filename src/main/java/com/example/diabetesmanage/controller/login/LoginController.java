package com.example.diabetesmanage.controller.login;

import com.example.diabetesmanage.dao.PatientDAO;
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
 * LoginController — đăng nhập bằng email + mật khẩu; portal theo {@code users.role} trong DB.
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

        if (service.equals("checkaccount")) {
            String inputUser = request.getParameter("UserName");
            String inputPass = request.getParameter("password");

            request.setAttribute("UserName", inputUser);

            boolean hasError = false;

            if (inputUser == null || inputUser.isBlank()) {
                request.setAttribute("emailError", "Vui lòng nhập email");
                hasError = true;
            } else if (inputUser.length() < 6 || inputUser.length() > 100) {
                request.setAttribute("emailError", "Email phải nằm trong khoảng từ 6-100 ký tự");
                hasError = true;
            }

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
            User user;
            try {
                user = dao.checkLogin(inputUser.trim(), hashedPass);
            } catch (IllegalStateException dbError) {
                request.setAttribute("AccountError",
                        "Không kết nối được MySQL. Kiểm tra MySQL đang chạy và mật khẩu trong DBContext.");
                request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
                return;
            }

            if (user == null) {
                request.setAttribute("AccountError", "Email hoặc mật khẩu không đúng.");
                request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
                return;
            }

            if (!user.isKichHoat()) {
                request.setAttribute("AccountError", "Tài khoản đã bị khóa.");
                request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
                return;
            }

            String role = normalizeRole(user.getVaiTro());
            if (!isSupportedRole(role)) {
                request.setAttribute("AccountError", "Tài khoản không có vai trò hợp lệ. Liên hệ quản trị.");
                request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
                return;
            }

            if ("benh_nhan".equalsIgnoreCase(role)) {
                String patientId = new PatientDAO().ensurePatientProfileForUser(user.getId());
                if (patientId == null) {
                    request.setAttribute("AccountError", "Không tạo được hồ sơ bệnh nhân. Vui lòng liên hệ quản trị.");
                    request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
                    return;
                }
            }
            if ("bac_si".equalsIgnoreCase(role)) {
                if (!UserDAO.getInstance().ensureDoctorProfile(user.getId())) {
                    request.setAttribute("AccountError", "Không tạo được hồ sơ bác sĩ. Vui lòng liên hệ quản trị.");
                    request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
                    return;
                }
            }

            session = rotateSession(request, session);
            session.setAttribute("user", user);
            session.setAttribute("loginUser", user);
            session.removeAttribute("adminUser");

            if ("quan_tri_vien".equalsIgnoreCase(role)) {
                session.setAttribute("adminUser", user);
                session.setAttribute("status", 1);
            } else if ("benh_nhan".equalsIgnoreCase(role)) {
                session.setAttribute("status", 2);
            } else if ("bac_si".equalsIgnoreCase(role)) {
                session.setAttribute("status", 3);
            }

            redirectByRole(response, request.getContextPath(), role);
        }
    }

    private static String normalizeRole(String role) {
        return role == null ? "" : role.trim();
    }

    private static boolean isSupportedRole(String role) {
        return "quan_tri_vien".equalsIgnoreCase(role)
                || "benh_nhan".equalsIgnoreCase(role)
                || "bac_si".equalsIgnoreCase(role);
    }

    private static HttpSession rotateSession(HttpServletRequest request, HttpSession oldSession) {
        if (oldSession != null) {
            try {
                oldSession.invalidate();
            } catch (IllegalStateException ignored) {
                // session đã hết hạn
            }
        }
        return request.getSession(true);
    }

    private void redirectByRole(HttpServletResponse response, String contextPath, String role)
            throws IOException {
        String normalized = normalizeRole(role);
        if ("quan_tri_vien".equalsIgnoreCase(normalized)) {
            response.sendRedirect(contextPath + "/admin-dashboard");
        } else if ("benh_nhan".equalsIgnoreCase(normalized)) {
            response.sendRedirect(contextPath + "/patient-dashboard");
        } else if ("bac_si".equalsIgnoreCase(normalized)) {
            response.sendRedirect(contextPath + "/doctor-dashboard");
        } else {
            response.sendRedirect(contextPath + "/Logincontroller");
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
