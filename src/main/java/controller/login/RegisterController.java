package controller.login;

import dal.UserDAO;
import java.io.IOException;
import java.sql.Timestamp;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.Encode;

/**
 * RegisterController — xử lý đăng ký tài khoản hệ thống Diabetes Support System.
 * URL: /RegisterController
 */
@WebServlet(name = "RegisterController", urlPatterns = { "/RegisterController" })
public class RegisterController extends HttpServlet {

    // Đường dẫn trỏ tới view register.jsp trong thư mục bảo mật WEB-INF
    private static final String REGISTER_VIEW = "/WEB-INF/views/auth/register.jsp";
    private static final String LOGIN_VIEW = "/WEB-INF/views/auth/login.jsp";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(true);
        String service = request.getParameter("service");

        // 1. Hiển thị trang đăng ký công khai (Khi truy cập qua đường dẫn /RegisterController trực tiếp)
        if (service == null) {
            request.getRequestDispatcher(REGISTER_VIEW).forward(request, response);
            return;
        }

        // 2. Xử lý đăng ký tài khoản hành động từ form (service = createaccount)
        if (service.equals("createaccount")) {
            // 1. Đọc thêm tham số hoTen và soDienThoai từ form gửi lên
            String inputHoTen = request.getParameter("hoTen");
            String inputSoDienThoai = request.getParameter("soDienThoai");
            String inputEmail = request.getParameter("Email");
            String inputPass = request.getParameter("password");
            String inputConfirmPass = request.getParameter("confirmPassword");

            // Giữ lại các giá trị đã nhập để hiển thị lại form nếu có lỗi
            request.setAttribute("hoTen", inputHoTen);
            request.setAttribute("soDienThoai", inputSoDienThoai);
            request.setAttribute("Email", inputEmail);

            boolean hasError = false;

            // ─── Validate Họ tên ──────────────────────────────────────────────
            if (inputHoTen == null || inputHoTen.isBlank()) {
                request.setAttribute("hoTenError", "Vui lòng nhập họ và tên của bạn");
                hasError = true;
            }

            // ─── Validate Số điện thoại (Định dạng Việt Nam chuẩn) ────────────
            if (inputSoDienThoai == null || inputSoDienThoai.isBlank()) {
                request.setAttribute("phoneError", "Vui lòng nhập số điện thoại");
                hasError = true;
            } else if (!inputSoDienThoai.matches("^0[0-9]{9}$")) {
                request.setAttribute("phoneError", "Số điện thoại không hợp lệ (phải gồm 10 số và bắt đầu bằng số 0)");
                hasError = true;
            }

            // ─── Validate dữ liệu Email ────────────────────────────────────────
            if (inputEmail == null || inputEmail.isBlank()) {
                request.setAttribute("emailError", "Vui lòng nhập địa chỉ email");
                hasError = true;
            } else if (!inputEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                request.setAttribute("emailError", "Định dạng email không hợp lệ");
                hasError = true;
            }

            // ─── Validate dữ liệu Mật khẩu ────────────────────────────────────
            if (inputPass == null || inputPass.isBlank()) {
                request.setAttribute("passError", "Vui lòng nhập mật khẩu");
                hasError = true;
            }
            if (!hasError && (inputConfirmPass == null || !inputConfirmPass.equals(inputPass))) {
                request.setAttribute("passError", "Mật khẩu xác nhận không trùng khớp");
                hasError = true;
            }

            if (hasError) {
                request.getRequestDispatcher(REGISTER_VIEW).forward(request, response);
                return;
            }

            UserDAO dao = UserDAO.getInstance();
            if (dao.isEmailExists(inputEmail.trim())) {
                request.setAttribute("RegisterError", "Địa chỉ email này đã được đăng ký trên hệ thống!");
                request.getRequestDispatcher(REGISTER_VIEW).forward(request, response);
                return;
            }

            Encode encoder = new Encode();
            String hashedPass = encoder.Encode(inputPass);

            // Gán toàn bộ thông tin sạch vào đối tượng User
            User newUser = new User();
            newUser.setHoTen(inputHoTen.trim());
            newUser.setSoDienThoai(inputSoDienThoai.trim());
            newUser.setEmail(inputEmail.trim());
            newUser.setMatKhauHash(hashedPass);
            newUser.setVaiTro("benh_nhan");
            newUser.setKichHoat(true);
            newUser.setNgayTao(new Timestamp(System.currentTimeMillis()));

            try {
                boolean isSuccess = dao.registerUser(newUser);
                if (isSuccess) {
                    request.setAttribute("UserName", inputEmail);
                    request.setAttribute("AccountError", "Đăng ký thành công! Bạn có thể đăng nhập ngay.");
                    request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
                } else {
                    request.setAttribute("RegisterError", "Đăng ký thất bại do hệ thống gặp lỗi sự cố.");
                    request.getRequestDispatcher(REGISTER_VIEW).forward(request, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("RegisterError", "Đăng ký thất bại: " + e.getMessage());
                request.getRequestDispatcher(REGISTER_VIEW).forward(request, response);
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
        return "Register Controller — Diabetes Support System";
    }
}