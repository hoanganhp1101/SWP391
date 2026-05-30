package controller.login;
import dal.UserDAO;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import org.json.JSONObject;
/**
 * CẦU HÌNH:
 * - GOOGLE_CLIENT_ID : Client ID từ Google Cloud Console
 * - GOOGLE_CLIENT_SECRET : Client Secret từ Google Cloud Console
 * - REDIRECT_URI : Phải khớp với Authorized redirect URI trong Console
 */
@WebServlet(name = "GoogleLoginController", urlPatterns = { "/GoogleLogin" })
public class GoogleLoginController extends HttpServlet {
    // ===================== CONFIG =====================
    private static final String GOOGLE_CLIENT_ID = "377198838448-eo3s2hbdnjnf1dqd8785gd6vm3dg5cjo.apps.googleusercontent.com";
    private static final String GOOGLE_CLIENT_SECRET = "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";
    private static final String REDIRECT_URI = "http://localhost:8080/FashionWarehouse/GoogleLogin";
    // Token & userinfo endpoints
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    // ==================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String code = request.getParameter("code");
        String error = request.getParameter("error");
        // Người dùng từ chối cấp quyền
        if (error != null || code == null) {
            response.sendRedirect(request.getContextPath() + "/Logincontroller");
            return;
        }
        try {
            // 1. Đổi code lấy access_token
            String tokenResponse = exchangeCodeForToken(code);
            JSONObject tokenJson = new JSONObject(tokenResponse);
            String accessToken = tokenJson.getString("access_token");
            // 2. Lấy thông tin user từ Google
            String userInfoResponse = getUserInfo(accessToken);
            JSONObject userInfo = new JSONObject(userInfoResponse);
            String googleId = userInfo.getString("id");
            String email = userInfo.optString("email", "");
            String name = userInfo.optString("name", email);
            if (email.isEmpty()) {
                request.setAttribute("AccountError", "Không thể lấy email từ Google. Vui lòng thử lại.");
                request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
                return;
            }
            // 3. Tìm / tạo user trong DB
            UserDAO dao = UserDAO.getInstance();
            Users user = dao.getUserByGoogleId(googleId);
            if (user == null) {
                 user = dao.getUserByEmail(email);
            }
            if (user == null) {
                request.setAttribute("AccountError", 
        "Email " + email + " chưa được đăng ký trong hệ thống.<br>" +
        "Vui lòng liên hệ Admin để được cấp tài khoản!");
                request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
                return;
            }
            // 4. Set session và redirect
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            if ("Admin".equalsIgnoreCase(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/views/admin/dashboard.jsp");
            } else {
                response.sendRedirect(request.getContextPath() + "/views/inventory/homepage.jsp");
            }
        } catch (Exception e) {
            getServletContext().log("Google Login Error", e);
            request.setAttribute("AccountError", "Đăng nhập Google thất bại: " + e.getMessage());
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
        }
    }
    // ── Exchange authorization code for access token ──────────────────────────
    private String exchangeCodeForToken(String code) throws IOException {
        String params = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(GOOGLE_CLIENT_ID, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(GOOGLE_CLIENT_SECRET, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                + "&grant_type=authorization_code";
        URL url = new URL(TOKEN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes(StandardCharsets.UTF_8));
        }
        return readResponse(conn);
    }
    // ── Get user info from Google ─────────────────────────────────────────────
    private String getUserInfo(String accessToken) throws IOException {
        URL url = new URL(USERINFO_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        return readResponse(conn);
    }
    // ── Read HTTP response ───────────────────────────────────────────────────
    private String readResponse(HttpURLConnection conn) throws IOException {
        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line);
            return sb.toString();
        }
    }
}