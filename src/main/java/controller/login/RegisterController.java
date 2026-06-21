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
 * RegisterController — xử lý đăng ký tài khoản hệ thống Diabetes Support
 * System. URL: /RegisterController
 */
@WebServlet(name = "RegisterController", urlPatterns = {"/RegisterController"})
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

            // 2. Định nghĩa các quy tắc (Regex)
            // Tên: Chứa chữ cái (bao gồm tiếng Việt) và khoảng trắng, từ 3-50 ký tự
            String nameRegex = "^[\\p{L}\\s]{1,50}$";
            // SĐT: Bắt đầu bằng số 0, theo sau là 9 chữ số (chuẩn VN)
            String phoneRegex = "^0\\d{9}$";
            // Email: Định dạng cơ bản chuẩn
            String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
            // ─── Validate Họ tên ──────────────────────────────────────────────
            if (inputHoTen == null || inputHoTen.trim().isEmpty()) {
                request.setAttribute("hoTenError", "Vui lòng nhập họ và tên của bạn");
                hasError = true;
            } else if (!inputHoTen.trim().matches(nameRegex)) {
                request.setAttribute("hoTenError", "Họ tên phải từ 3-50 ký tự và không chứa số hay ký tự đặc biệt.");
                hasError = true;
            }

            // ─── Validate Số điện thoại (Định dạng Việt Nam chuẩn) ────────────
            if (inputSoDienThoai == null || inputSoDienThoai.trim().isEmpty()) {
                request.setAttribute("phoneError", "Vui lòng nhập số điện thoại");
                hasError = true;
            } else if (!inputSoDienThoai.matches(phoneRegex)) {
                request.setAttribute("phoneError", "Số điện thoại không hợp lệ (phải gồm 10 số và bắt đầu bằng số 0)");
                hasError = true;
            }

            // ─── Validate dữ liệu Email ────────────────────────────────────────
            if (inputEmail == null || inputEmail.trim().isEmpty()) {
                request.setAttribute("emailError", "Vui lòng nhập địa chỉ email");
                hasError = true;
            } else if (!inputEmail.matches(emailRegex)) {
                request.setAttribute("emailError", "Định dạng email không hợp lệ");
                hasError = true;
            }

            // ─── Validate dữ liệu Mật khẩu ────────────────────────────────────
            if (inputPass == null || inputPass.isEmpty()) {
                request.setAttribute("passError", "Vui lòng nhập mật khẩu");
                hasError = true;
            } else if (inputPass.length() < 6) {
                request.setAttribute("passError", "Mật khẩu phải chứa ít nhất 6 ký tự.");
                hasError = true;
            }

            // Kiểm tra Xác nhận mật khẩu
            if (inputConfirmPass == null || !inputConfirmPass.equals(inputPass)) {
                request.setAttribute("passError", "Xác nhận mật khẩu không khớp.");
                hasError = true;
            }

            // 4. Nếu có bất kỳ lỗi nào, trả về lại form đăng ký
            if (hasError) {
                request.getRequestDispatcher(REGISTER_VIEW).forward(request, response);
                return; // Dừng luồng chạy tại đây, không cho lưu vào DB
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
