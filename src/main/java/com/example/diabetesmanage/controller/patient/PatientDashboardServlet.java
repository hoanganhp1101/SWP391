package com.example.diabetesmanage.controller.patient;

import java.io.IOException;
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
}
