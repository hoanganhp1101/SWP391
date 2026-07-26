package com.example.diabetesmanage.controller.patient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.List;
import java.time.LocalDate;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.AppointmentDAO;
import com.example.diabetesmanage.dao.MedicalDocumentDAO;
import com.example.diabetesmanage.dao.AlertDAO;
import com.example.diabetesmanage.dao.AIAnalysisDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.Appointment;
import com.example.diabetesmanage.model.MedicalDocument;
import com.example.diabetesmanage.model.Alert;
import com.example.diabetesmanage.model.AIAnalysis;
import com.example.diabetesmanage.util.PatientPortalAuth;

@WebServlet(name = "PatientDashboardServlet", urlPatterns = { "/patient-dashboard" })
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
public class PatientDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }

        PatientDAO patientDAO = new PatientDAO();
        HealthRecordDAO recordDAO = new HealthRecordDAO();
        HealthRecord latestGlucoseRecord = recordDAO.getLatestHealthRecord(patientId);
        if (latestGlucoseRecord != null) {
            request.setAttribute("latestGlucose", latestGlucoseRecord.getDuongHuyetMgdl());
        }

        HealthRecord latestHeartRateRecord = recordDAO.getLatestHeartRateRecord(patientId);
        if (latestHeartRateRecord != null) {
            request.setAttribute("latestHeartRate", latestHeartRateRecord.getNhipTim());
        }

        HealthRecord latestBloodPressureRecord = recordDAO.getLatestBloodPressureRecord(patientId);
        if (latestBloodPressureRecord != null) {
            request.setAttribute("latestSystolic", latestBloodPressureRecord.getHuyetApTamThu());
            request.setAttribute("latestDiastolic", latestBloodPressureRecord.getHuyetApTamTruong());
        }

        // Lấy toàn bộ dữ liệu cho biểu đồ
        List<HealthRecord> allRecords = recordDAO.getAllRecordsForChart(patientId);
        StringBuilder jsonBuilder = new StringBuilder("[");
        for (int i = 0; i < allRecords.size(); i++) {
            HealthRecord r = allRecords.get(i);
            jsonBuilder.append("{")
                    .append("\"time\":\"").append(r.getThoiGianDo() != null ? r.getThoiGianDo().toString() : "")
                    .append("\",")
                    .append("\"glucose\":").append(r.getDuongHuyetMgdl() != null ? r.getDuongHuyetMgdl() : "null")
                    .append(",")
                    .append("\"hr\":").append(r.getNhipTim() != null ? r.getNhipTim() : "null").append(",")
                    .append("\"sys\":").append(r.getHuyetApTamThu() != null ? r.getHuyetApTamThu() : "null").append(",")
                    .append("\"dia\":").append(r.getHuyetApTamTruong() != null ? r.getHuyetApTamTruong() : "null")
                    .append("}");
            if (i < allRecords.size() - 1)
                jsonBuilder.append(",");
        }
        jsonBuilder.append("]");
        request.setAttribute("chartDataJson", jsonBuilder.toString());

        // Lấy thông tin bệnh nhân
        Patient patientInfo = patientDAO.getPatientById(patientId);
        request.setAttribute("patientInfo", patientInfo);

        // Lấy lịch hẹn
        AppointmentDAO appointmentDAO = new AppointmentDAO();
        List<Appointment> appointments = appointmentDAO.getUpcomingAppointments(patientId);
        request.setAttribute("appointments", appointments);

        // Lấy tài liệu y tế / lịch sử khám
        MedicalDocumentDAO medDocDAO = new MedicalDocumentDAO();
        List<MedicalDocument> medicalDocuments = medDocDAO.getRecentDocuments(patientId);
        request.setAttribute("medicalDocuments", medicalDocuments);

        // Lấy cảnh báo
        AlertDAO alertDAO = new AlertDAO();
        List<Alert> alerts = alertDAO.getRecentAlerts(patientId);
        request.setAttribute("alerts", alerts);

        // Lấy kết quả phân tích AI mới nhất
        AIAnalysisDAO aiDAO = new AIAnalysisDAO();
        AIAnalysis latestAI = aiDAO.getLatestAnalysis(patientId);
        if (latestAI != null) {
            request.setAttribute("aiAnalysis", latestAI);
        }

        // Chuyển hướng tới giao diện JSP
        request.getRequestDispatcher("/WEB-INF/views/patient/patient-dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (!"updateProfile".equals(action)) {
            response.sendRedirect("patient-dashboard");
            return;
        }
        String returnUrl = sanitizeReturnUrl(request.getParameter("returnUrl"));

        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }
        PatientDAO patientDAO = new PatientDAO();

        String hoTen = trimToNull(request.getParameter("hoTen"));
        String email = trimToNull(request.getParameter("email"));
        String soDienThoai = trimToNull(request.getParameter("soDienThoai"));
        String ngaySinhRaw = trimToNull(request.getParameter("ngaySinh"));
        String gioiTinh = trimToNull(request.getParameter("gioiTinh"));
        String diaChi = trimToNull(request.getParameter("diaChi"));
        String loaiTieuDuong = trimToNull(request.getParameter("loaiTieuDuong"));
        String tienSuBenh = trimToNull(request.getParameter("tienSuBenh"));
        String diUng = trimToNull(request.getParameter("diUng"));
        String anhDaiDien = trimToNull(request.getParameter("currentAnhDaiDien"));

        if (hoTen == null || email == null || soDienThoai == null || ngaySinhRaw == null) {
            redirectProfileError(response, returnUrl, "Vui lòng điền đầy đủ họ tên, email, số điện thoại, ngày sinh.");
            return;
        }

        // Validate Email
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!Pattern.matches(emailRegex, email)) {
            redirectProfileError(response, returnUrl, "Email không hợp lệ.");
            return;
        }

        // Validate Phone Number
        String phoneRegex = "^0[0-9]{9}$";
        if (!Pattern.matches(phoneRegex, soDienThoai)) {
            redirectProfileError(response, returnUrl, "Số điện thoại không hợp lệ (phải bắt đầu bằng 0 và gồm 10 chữ số).");
            return;
        }

        Date ngaySinh;
        try {
            ngaySinh = Date.valueOf(ngaySinhRaw);
            LocalDate birthDate = ngaySinh.toLocalDate();
            if (birthDate.isAfter(LocalDate.now())) {
                redirectProfileError(response, returnUrl, "Ngày sinh không được lớn hơn ngày hiện tại.");
                return;
            }
        } catch (IllegalArgumentException e) {
            redirectProfileError(response, returnUrl, "Ngày sinh không hợp lệ.");
            return;
        }

        try {
            String uploadedAvatar = saveUploadedAvatar(request);
            if (uploadedAvatar != null) {
                anhDaiDien = uploadedAvatar;
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Ảnh đại diện không được vượt quá 5MB.";
            redirectProfileError(response, returnUrl, errorMessage);
            return;
        }

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setHoTen(hoTen);
        patient.setEmail(email);
        patient.setSoDienThoai(soDienThoai);
        patient.setNgaySinh(ngaySinh);
        patient.setGioiTinh(gioiTinh);
        patient.setDiaChi(diaChi);
        patient.setLoaiTieuDuong(loaiTieuDuong);
        patient.setTienSuBenh(tienSuBenh);
        patient.setDiUng(diUng);
        patient.setAnhDaiDien(anhDaiDien);

        boolean updated = patientDAO.updatePatientProfile(patient);
        if (updated) {
            response.sendRedirect(returnUrl + "?profileUpdated=1");
        } else {
            redirectProfileError(response, returnUrl, "Không thể cập nhật hồ sơ. Vui lòng thử lại.");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void redirectProfileError(HttpServletResponse response, String returnUrl, String errorMessage)
            throws IOException {
        response.sendRedirect(returnUrl + "?profileUpdated=0&openProfileModal=1&error=" +
                URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
    }

    private String sanitizeReturnUrl(String returnUrl) {
        String cleanUrl = trimToNull(returnUrl);
        if ("patient-medical-profile".equals(cleanUrl) || "patient-prescriptions".equals(cleanUrl)
                || "patient-appointments".equals(cleanUrl) || "patient-medical-history".equals(cleanUrl)) {
            return cleanUrl;
        }
        return "patient-dashboard";
    }

    private String saveUploadedAvatar(HttpServletRequest request) throws IOException, ServletException {
        Part avatarPart = request.getPart("avatarFile");
        if (avatarPart == null || avatarPart.getSize() == 0) {
            return null;
        }
        if (avatarPart.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Ảnh đại diện không được vượt quá 5MB.");
        }

        String contentType = avatarPart.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException("Vui lòng chọn file ảnh hợp lệ.");
        }

        String extension = getFileExtension(avatarPart.getSubmittedFileName());
        if (extension == null) {
            throw new IllegalArgumentException("Ảnh đại diện chỉ hỗ trợ JPG, PNG, GIF hoặc WEBP.");
        }

        String uploadDirPath = request.getServletContext().getRealPath("/uploads/avatars");
        if (uploadDirPath == null) {
            throw new IllegalArgumentException("Không thể xác định thư mục lưu ảnh trên server.");
        }

        Files.createDirectories(Paths.get(uploadDirPath));
        String fileName = "avatar_" + System.currentTimeMillis() + extension;
        Path filePath = Paths.get(uploadDirPath, fileName);
        avatarPart.write(filePath.toString());

        return "uploads/avatars/" + fileName;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return ".jpg";
        }
        if (lowerName.endsWith(".png")) {
            return ".png";
        }
        if (lowerName.endsWith(".gif")) {
            return ".gif";
        }
        if (lowerName.endsWith(".webp")) {
            return ".webp";
        }
        return null;
    }
}
