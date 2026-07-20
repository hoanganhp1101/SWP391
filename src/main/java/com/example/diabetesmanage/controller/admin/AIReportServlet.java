package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.Prescription;
import com.example.diabetesmanage.model.Medication;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.service.GeminiService;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet tạo báo cáo AI tổng hợp cho bác sĩ.
 * Gathers patient data → calls Gemini → forwards to report view.
 */
@WebServlet(name = "AIReportServlet", urlPatterns = {"/ai-report"})
public class AIReportServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Prefer explicit patientId; admin/doctor may fall back to demo only as last resort
        String patientId = request.getParameter("patientId");
        PatientDAO patientDAO = new PatientDAO();
        if (patientId == null || patientId.trim().isEmpty()) {
            HttpSession session = request.getSession(false);
            User user = null;
            if (session != null) {
                Object value = session.getAttribute("user");
                if (value instanceof User) {
                    user = (User) value;
                }
            }
            if (user != null && ("quan_tri_vien".equalsIgnoreCase(user.getVaiTro())
                    || "bac_si".equalsIgnoreCase(user.getVaiTro()))) {
                patientId = patientDAO.getDemoPatientId();
            }
        }

        if (patientId == null) {
            request.setAttribute("error", "Không tìm thấy bệnh nhân.");
            request.getRequestDispatcher("/WEB-INF/views/admin/ai-report.jsp").forward(request, response);
            return;
        }

        // Lấy thông tin bệnh nhân
        Patient patient = patientDAO.getPatientById(patientId);
        request.setAttribute("patient", patient);

        // Build health summary
        HealthRecordDAO recordDAO = new HealthRecordDAO();
        HealthRecord latestComprehensive = recordDAO.getLatestComprehensiveRecord(patientId);
        List<HealthRecord> recentRecords = recordDAO.getRecentDailyRecords(patientId);

        StringBuilder healthSummary = new StringBuilder();
        if (latestComprehensive != null) {
            if (latestComprehensive.getDuongHuyetMgdl() != null)
                healthSummary.append("- Đường huyết gần nhất: ").append(latestComprehensive.getDuongHuyetMgdl()).append(" mg/dL\n");
            if (latestComprehensive.getHba1cPercent() != null)
                healthSummary.append("- HbA1c: ").append(latestComprehensive.getHba1cPercent()).append("%\n");
            if (latestComprehensive.getHuyetApTamThu() != null)
                healthSummary.append("- Huyết áp: ").append(latestComprehensive.getHuyetApTamThu()).append("/").append(latestComprehensive.getHuyetApTamTruong()).append(" mmHg\n");
            if (latestComprehensive.getNhipTim() != null)
                healthSummary.append("- Nhịp tim: ").append(latestComprehensive.getNhipTim()).append(" BPM\n");
            if (latestComprehensive.getBmi() != null)
                healthSummary.append("- BMI: ").append(latestComprehensive.getBmi()).append("\n");
            if (latestComprehensive.getCholesterolMmol() != null)
                healthSummary.append("- Cholesterol: ").append(latestComprehensive.getCholesterolMmol()).append(" mmol/L\n");
        }

        if (!recentRecords.isEmpty()) {
            healthSummary.append("\n- Xu hướng đường huyết 7 ngày gần nhất:\n");
            for (HealthRecord r : recentRecords) {
                if (r.getDuongHuyetMgdl() != null) {
                    healthSummary.append("  + ").append(r.getDuongHuyetMgdl()).append(" mg/dL");
                    if (r.getLieuLuongInsulinUi() != null)
                        healthSummary.append(" (Insulin: ").append(r.getLieuLuongInsulinUi()).append(" UI)");
                    healthSummary.append("\n");
                }
            }
        }

        // Build prescription summary
        PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
        List<Prescription> prescriptions = prescriptionDAO.getPrescriptionsForPatient(patientId);
        StringBuilder prescriptionSummary = new StringBuilder();
        if (prescriptions != null && !prescriptions.isEmpty()) {
            Prescription latest = prescriptions.get(0);
            prescriptionSummary.append("- Chẩn đoán: ").append(latest.getChanDoan()).append("\n");
            if (latest.getHuongDieuTri() != null)
                prescriptionSummary.append("- Hướng điều trị: ").append(latest.getHuongDieuTri()).append("\n");
            if (latest.getMedications() != null) {
                prescriptionSummary.append("- Danh sách thuốc:\n");
                for (Medication med : latest.getMedications()) {
                    prescriptionSummary.append("  + ").append(med.getTenThuoc())
                            .append(" ").append(med.getLieuLuong()).append(" ").append(med.getDonVi())
                            .append(" - ").append(med.getTanSuat()).append("\n");
                }
            }
        }

        // Gọi AI tạo báo cáo
        try {
            GeminiService geminiService = new GeminiService();
            String report = geminiService.generateDoctorReport(patient, healthSummary.toString(), prescriptionSummary.toString());
            request.setAttribute("aiReport", report);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("aiReport", "Không thể tạo báo cáo AI. Lỗi: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/ai-report.jsp").forward(request, response);
    }
}
