package com.example.diabetesmanage.controller.login;

import com.example.diabetesmanage.dao.UserDAO;
import java.io.IOException;
import java.security.SecureRandom;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.mail.MessagingException;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.util.Encode;
import com.example.diabetesmanage.util.EmailUtils;



@WebServlet(name = "ForgotPasswordController", urlPatterns = { "/ForgotPassword" })
public class ForgotPasswordController extends HttpServlet {

    private static final String FORGOT_VIEW = "/WEB-INF/views/auth/forgot-password.jsp";
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    // ── GET: hiển thị form ────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(FORGOT_VIEW).forward(request, response);
    }

    // ── POST: xử lý yêu cầu quên mật khẩu ────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String email = request.getParameter("email");
        request.setAttribute("email", email);

        // Validate email
        if (email == null || email.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập địa chỉ email");
            request.getRequestDispatcher(FORGOT_VIEW).forward(request, response);
            return;
        }

        // Tìm user theo email
        UserDAO dao = UserDAO.getInstance();
        User user = dao.getUserByEmail(email.trim());

        if (user == null) {
            request.setAttribute("error", "Email không tồn tại trong hệ thống");
            request.getRequestDispatcher(FORGOT_VIEW).forward(request, response);
            return;
        }

        // Tạo mật khẩu mới ngẫu nhiên 10 ký tự
        String newPassword = generateRandomPassword(10);

        // Hash mật khẩu mới
        Encode encoder = new Encode();
        String hashedPassword = encoder.Encode(newPassword);

        // Cập nhật mật khẩu mới vào DB
        boolean updated = dao.updatePassword(user.getId(), hashedPassword);
        if (!updated) {
            request.setAttribute("error", "Cập nhật mật khẩu thất bại. Vui lòng thử lại.");
            request.getRequestDispatcher(FORGOT_VIEW).forward(request, response);
            return;
        }

        // Gửi email chứa mật khẩu mới
        String subject = "[Diab] Mật khẩu mới của bạn";
        String body = buildEmailBody(user.getHoTen(), newPassword);

        try {
            EmailUtils.sendHtmlEmail(user.getEmail(), subject, body);
        } catch (MessagingException e) {
            getServletContext().log("Gửi email thất bại", e);
            // Vẫn thông báo user mật khẩu đã đổi, nhưng email lỗi
            request.setAttribute("error", "Mật khẩu đã được đặt lại, nhưng không thể gửi email. Liên hệ Admin.");
            request.getRequestDispatcher(FORGOT_VIEW).forward(request, response);
            return;
        }

        request.setAttribute("success", "Mật khẩu mới đã được gửi về email <strong>" + escapeHtml(email)
                + "</strong>. Vui lòng kiểm tra hộp thư (bao gồm thư mục Spam).");
        request.getRequestDispatcher(FORGOT_VIEW).forward(request, response);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private String buildEmailBody(String fullName, String newPassword) {
        return String.format(
                "<html>" +
                        "<body style=\"font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;\">" +
                        "  <div style=\"max-width: 500px; margin: 0 auto; background: #fff; border-radius: 8px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);\">" +
                        "    <h2 style=\"color: #4169e1;\">Fashion Warehouse</h2>" +
                        "    <p>Xin chào <strong>%s</strong>,</p>" +
                        "    <p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>" +
                        "    <p>Mật khẩu mới của bạn là:</p>" +
                        "    <div style=\"background-color: #f0f0f0; padding: 12px 20px; border-radius: 5px; font-size: 20px; letter-spacing: 2px; text-align: center; font-weight: bold; color: #333;\">" +
                        "      %s" +
                        "    </div>" +
                        "    <p style=\"margin-top: 20px;\">Vui lòng đăng nhập và <strong>đổi mật khẩu ngay</strong> sau khi nhận được email này.</p>" +
                        "    <p style=\"color: #999; font-size: 12px;\">Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng liên hệ Admin.</p>" +
                        "    <hr style=\"border: none; border-top: 1px solid #eee; margin: 20px 0;\">" +
                        "    <p style=\"color: #999; font-size: 11px; text-align: center;\">© 2026 Fashion Warehouse System</p>" +
                        "  </div>" +
                        "</body>" +
                        "</html>",
                escapeHtml(fullName),
                newPassword
        );
    }

    private String escapeHtml(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
