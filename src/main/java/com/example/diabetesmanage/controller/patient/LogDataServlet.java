package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.AIAnalysis;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.service.ClinicalRiskService;
import com.example.diabetesmanage.service.GeminiService;
import com.example.diabetesmanage.util.PatientPortalAuth;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "LogDataServlet", urlPatterns = { "/logData" })
public class LogDataServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String patientId = request.getParameter("patientId");
        if (patientId == null || patientId.trim().isEmpty()) {
            patientId = PatientPortalAuth.requirePatientId(request, response);
            if (patientId == null) {
                return;
            }
        }

        String glucoseStr = request.getParameter("duong_huyet");
        String donViStr = request.getParameter("don_vi_duong_huyet");
        String nhipTimStr = request.getParameter("nhip_tim");
        String haThuStr = request.getParameter("huyet_ap_thu");
        String haTruongStr = request.getParameter("huyet_ap_truong");
        String thoiDiem = request.getParameter("thoi_diem");
        String chestPain = request.getParameter("chest_pain");
        String dizziness = request.getParameter("dizziness");
        String fatigue = request.getParameter("fatigue");

        HealthRecord record = new HealthRecord();
        record.setPatientId(patientId);
        record.setThoiDiemDoDuong(thoiDiem);
        record.setChestPain(chestPain != null ? 1 : 0);
        record.setDizziness(dizziness != null ? 1 : 0);
        record.setFatigue(fatigue != null ? 1 : 0);

        try {
            if (glucoseStr != null && !glucoseStr.trim().isEmpty()) {
                double glucose = Double.parseDouble(glucoseStr);
                if ("mmol/L".equals(donViStr)) {
                    glucose = glucose * 18.0;
                }
                record.setDuongHuyetMgdl(glucose);
            }
            if (nhipTimStr != null && !nhipTimStr.trim().isEmpty()) {
                record.setNhipTim(Integer.parseInt(nhipTimStr));
            }
            if (haThuStr != null && !haThuStr.trim().isEmpty()) {
                record.setHuyetApTamThu(Integer.parseInt(haThuStr));
            }
            if (haTruongStr != null && !haTruongStr.trim().isEmpty()) {
                record.setHuyetApTamTruong(Integer.parseInt(haTruongStr));
            }

            // 1. Lưu bản ghi sức khỏe
            HealthRecordDAO dao = new HealthRecordDAO();
            dao.insertHealthRecord(record);

            // 2. Gọi AI phân tích chỉ số sức khỏe
            try {
                PatientDAO patientDAO = new PatientDAO();
                Patient patient = patientDAO.getPatientById(patientId);

                GeminiService geminiService = new GeminiService();
                AIAnalysis analysis = geminiService.analyzeHealthData(record, patient);

                if (analysis != null) {
                    // Pipeline chung: áp rule động, lưu ai_analysis, tạo alert nếu cần
                    ClinicalRiskService.applyRulesAndPersist(patientId, record, analysis);
                }
            } catch (Exception aiEx) {
                // AI analysis thất bại không ảnh hưởng đến việc lưu dữ liệu
                System.err.println("[LogDataServlet] AI analysis failed: " + aiEx.getMessage());
                aiEx.printStackTrace();
            }

            response.sendRedirect(request.getContextPath() + "/patient-dashboard");

        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/patient-dashboard?error=InvalidInput");
        }
    }

}
