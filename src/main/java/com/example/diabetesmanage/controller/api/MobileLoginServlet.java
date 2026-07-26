package com.example.diabetesmanage.controller.api;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.UserDAO;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.util.Encode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "MobileLoginServlet", urlPatterns = {"/api/mobile/login"})
public class MobileLoginServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");

        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        Map<String, Object> responseData = new HashMap<>();

        try {
            // Đọc JSON từ body request
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            if (sb.toString().trim().isEmpty()) {
                responseData.put("status", "error");
                responseData.put("message", "Thiếu dữ liệu đầu vào");
                out.print(gson.toJson(responseData));
                return;
            }

            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);
            
            if (!jsonRequest.has("username") || !jsonRequest.has("password")) {
                responseData.put("status", "error");
                responseData.put("message", "Vui lòng cung cấp username và password");
                out.print(gson.toJson(responseData));
                return;
            }
            
            String username = jsonRequest.get("username").getAsString().trim();
            String password = jsonRequest.get("password").getAsString();

            if (username.isEmpty() || password.isEmpty()) {
                responseData.put("status", "error");
                responseData.put("message", "Tên đăng nhập và mật khẩu không được để trống");
                out.print(gson.toJson(responseData));
                return;
            }

            // Mã hóa mật khẩu
            Encode encoder = new Encode();
            String hashedPass = encoder.Encode(password);

            // Kiểm tra DB
            UserDAO userDAO = UserDAO.getInstance();
            User user = userDAO.checkLogin(username, hashedPass);

            if (user == null) {
                responseData.put("status", "error");
                responseData.put("message", "Email/Tên đăng nhập hoặc mật khẩu không chính xác");
                out.print(gson.toJson(responseData));
                return;
            }

            if (!user.isKichHoat()) {
                responseData.put("status", "error");
                responseData.put("message", "Tài khoản của bạn đã bị khóa");
                out.print(gson.toJson(responseData));
                return;
            }

            String role = normalizeRole(user.getVaiTro());
            if (!"benh_nhan".equalsIgnoreCase(role)) {
                responseData.put("status", "error");
                responseData.put("message", "Ứng dụng này chỉ dành cho bệnh nhân");
                out.print(gson.toJson(responseData));
                return;
            }

            // Tìm hồ sơ bệnh nhân
            PatientDAO patientDAO = new PatientDAO();
            String patientId = patientDAO.getPatientIdByUserId(user.getId());
            if (patientId == null || patientId.isBlank()) {
                patientId = patientDAO.ensurePatientProfileForUser(user.getId());
            }

            if (patientId == null || patientId.isBlank()) {
                responseData.put("status", "error");
                responseData.put("message", "Lỗi: Không tìm thấy hoặc không thể tạo hồ sơ bệnh nhân.");
                out.print(gson.toJson(responseData));
                return;
            }

            // Trả về kết quả thành công
            responseData.put("status", "success");
            responseData.put("patientId", patientId);
            
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("hoTen", user.getHoTen());
            userInfo.put("email", user.getEmail());
            responseData.put("userInfo", userInfo);
            
            out.print(gson.toJson(responseData));

        } catch (JsonSyntaxException e) {
            responseData.put("status", "error");
            responseData.put("message", "Dữ liệu JSON không hợp lệ");
            out.print(gson.toJson(responseData));
        } catch (Exception e) {
            responseData.put("status", "error");
            responseData.put("message", "Lỗi hệ thống: " + e.getMessage());
            out.print(gson.toJson(responseData));
        } finally {
            out.flush();
        }
    }

    private String normalizeRole(String dbRole) {
        if (dbRole == null) return "";
        return dbRole.trim().toLowerCase().replaceAll("\\s+", "_");
    }
}
