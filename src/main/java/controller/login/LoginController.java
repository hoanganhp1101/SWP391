package controller.login;

import dal.UserDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.Encode;

/**
 * LoginController — xử lý đăng nhập với hashed password. URL: /Logincontroller
 */
@WebServlet(name = "Logincontroller", urlPatterns = {"/Logincontroller"})
public class LoginController extends HttpServlet {

    private static final String LOGIN_VIEW = "/WEB-INF/views/auth/login.jsp";
    private static final String ADMIN_VIEW = "/WEB-INF/views/admin/dashboard.jsp";
    private static final String USER_VIEW = "/WEB-INF/views/patient-dashboard.jsp";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(true);
        String service = request.getParameter("service");

        // Hiển thị trang login
        if (service == null) {
            request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
            return;
        }

        // Redirect sau khi login thành công (dùng bởi JS timeout)
        if (service.equals("admin")) {
            request.getRequestDispatcher(ADMIN_VIEW).forward(request, response);
            return;
        }
        if (service.equals("user")) {
            request.getRequestDispatcher(USER_VIEW).forward(request, response);
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

            // Đăng nhập thành công → set session
            session.setAttribute("user", user);

            // Redirect theo role tới CÁC CONTROLLER TƯƠNG ỨNG
            String role = user.getVaiTro();
            if ("quan_tri_vien".equalsIgnoreCase(role)) {
                session.setAttribute("status", 1);
                // Điều hướng về Servlet xử lý Dashboard của Admin
                response.sendRedirect(request.getContextPath() + "/admin-dashboard");

            } else if ("benh_nhan".equalsIgnoreCase(role)) {
                session.setAttribute("status", 2);
                // Bệnh nhân đăng nhập thành công -> Forward trực tiếp sang trang dashboard bệnh nhân
               response.sendRedirect(request.getContextPath() + "/patient-dashboard");

            } else if ("bac_si".equalsIgnoreCase(role)) {
                session.setAttribute("status", 3);
                // Nếu bạn có trang riêng cho bác sĩ, hãy khai báo VIEW hoặc điều hướng tới Servlet của bác sĩ ở đây
                // Tạm thời nếu chưa có, cho dùng chung giao diện hoặc chuyển hướng tùy ý:
                response.sendRedirect(request.getContextPath() + "/doctor-dashboard");

            }

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
