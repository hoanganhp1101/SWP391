package com.example.diabetesmanage.controller.patient;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.dao.AlertDAO;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Prescription;
import com.example.diabetesmanage.model.Alert;
import com.example.diabetesmanage.util.PatientPortalAuth;

@WebServlet(name = "PatientMedicalProfileServlet", urlPatterns = {"/patient-medical-profile"})
public class PatientMedicalProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }

        PatientDAO patientDAO = new PatientDAO();
        // 1. Thông tin Hành chính & Tiền sử
        Patient patientInfo = patientDAO.getPatientById(patientId);
        request.setAttribute("patientInfo", patientInfo);

        // 2 & 3. Chỉ số sinh tồn & Kết quả cận lâm sàng
        HealthRecordDAO recordDAO = new HealthRecordDAO();
        HealthRecord latestRecord = recordDAO.getLatestComprehensiveRecord(patientId);
        request.setAttribute("latestRecord", latestRecord);

        // 4. Kế hoạch điều trị & Đơn thuốc
        PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
        Prescription latestPrescription = prescriptionDAO.getLatestPrescription(patientId);
        request.setAttribute("latestPrescription", latestPrescription);

        // 5. Nhật ký y khoa & Tiến triển (Lấy Alerts)
        AlertDAO alertDAO = new AlertDAO();
        List<Alert> recentAlerts = alertDAO.getRecentAlerts(patientId);
        request.setAttribute("alerts", recentAlerts);

        request.getRequestDispatcher("/WEB-INF/views/patient/patient-medical-profile.jsp").forward(request, response);
    }
}
