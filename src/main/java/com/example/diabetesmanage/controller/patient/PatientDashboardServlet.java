package com.example.diabetesmanage.controller.patient;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

@WebServlet(name = "PatientDashboardServlet", urlPatterns = {"/patient-dashboard"})
public class PatientDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Lấy ID thật từ CSDL cho bản demo
        PatientDAO patientDAO = new PatientDAO();
        String patientId = patientDAO.getDemoPatientId();

        if (patientId != null) {
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
                    .append("\"time\":\"").append(r.getThoiGianDo() != null ? r.getThoiGianDo().toString() : "").append("\",")
                    .append("\"glucose\":").append(r.getDuongHuyetMgdl() != null ? r.getDuongHuyetMgdl() : "null").append(",")
                    .append("\"hr\":").append(r.getNhipTim() != null ? r.getNhipTim() : "null").append(",")
                    .append("\"sys\":").append(r.getHuyetApTamThu() != null ? r.getHuyetApTamThu() : "null").append(",")
                    .append("\"dia\":").append(r.getHuyetApTamTruong() != null ? r.getHuyetApTamTruong() : "null")
                    .append("}");
                if (i < allRecords.size() - 1) jsonBuilder.append(",");
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

        PatientDAO patientDAO = new PatientDAO();
        String patientId = patientDAO.getDemoPatientId();
        if (patientId == null) {
            response.sendRedirect("patient-dashboard?profileUpdated=0&openProfileModal=1&error=" +
                    URLEncoder.encode("Không tìm thấy hồ sơ bệnh nhân.", StandardCharsets.UTF_8));
            return;
        }

        String hoTen = trimToNull(request.getParameter("hoTen"));
        String email = trimToNull(request.getParameter("email"));
        String soDienThoai = trimToNull(request.getParameter("soDienThoai"));
        String ngaySinhRaw = trimToNull(request.getParameter("ngaySinh"));
        String gioiTinh = trimToNull(request.getParameter("gioiTinh"));
        String diaChi = trimToNull(request.getParameter("diaChi"));
        String loaiTieuDuong = trimToNull(request.getParameter("loaiTieuDuong"));
        String tienSuBenh = trimToNull(request.getParameter("tienSuBenh"));
        String diUng = trimToNull(request.getParameter("diUng"));
        String anhDaiDien = trimToNull(request.getParameter("anhDaiDien"));

        if (hoTen == null || email == null || soDienThoai == null || ngaySinhRaw == null) {
            response.sendRedirect("patient-dashboard?profileUpdated=0&openProfileModal=1&error=" +
                    URLEncoder.encode("Vui lòng điền đầy đủ họ tên, email, số điện thoại, ngày sinh.", StandardCharsets.UTF_8));
            return;
        }

        Date ngaySinh;
        try {
            ngaySinh = Date.valueOf(ngaySinhRaw);
        } catch (IllegalArgumentException e) {
            response.sendRedirect("patient-dashboard?profileUpdated=0&openProfileModal=1&error=" +
                    URLEncoder.encode("Ngày sinh không hợp lệ.", StandardCharsets.UTF_8));
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
            response.sendRedirect("patient-dashboard?profileUpdated=1");
        } else {
            response.sendRedirect("patient-dashboard?profileUpdated=0&openProfileModal=1&error=" +
                    URLEncoder.encode("Không thể cập nhật hồ sơ. Vui lòng thử lại.", StandardCharsets.UTF_8));
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
