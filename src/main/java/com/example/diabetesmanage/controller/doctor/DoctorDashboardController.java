package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.DoctorAlertDAO;
import com.example.diabetesmanage.dao.DoctorDashboardDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dto.HighRiskPatientDTO;
import com.example.diabetesmanage.dto.DashboardSummaryDTO;
import com.example.diabetesmanage.dto.CriticalPatientAlertDTO;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.service.DangerousPatientService;
import com.example.diabetesmanage.service.DangerousPatientService.AnalysisResult;
import com.example.diabetesmanage.util.AuthContext;
import com.example.diabetesmanage.util.DoctorLayoutHelper;
import com.example.diabetesmanage.util.HealthMetricAssessment;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "DoctorDashboardServlet", urlPatterns = {"/doctor-dashboard"})
public class DoctorDashboardController extends HttpServlet {

    private final DoctorDashboardDAO dashboardDAO = new DoctorDashboardDAO();
    private final DoctorAlertDAO doctorAlertDAO = new DoctorAlertDAO();
    private final DangerousPatientService dangerousPatientService = new DangerousPatientService();
    private final PatientDAO patientDAO = new PatientDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User doctor = AuthContext.requireDoctor(request, response);
        if (doctor == null) {
            return;
        }

        String doctorId = doctor.getId().toString();
        DashboardSummaryDTO stats = dashboardDAO.getDashboardStats(
                doctorId,
                request.getParameter("startDate"),
                request.getParameter("endDate"));
        if (stats == null) {
            stats = new DashboardSummaryDTO();
        }

        // KPI cảnh báo đọc từ bảng alerts (cùng nguồn với trang /doctor/alerts)
        // để dashboard và hộp thư cảnh báo luôn khớp số liệu.
        stats.setActiveAlerts(doctorAlertDAO.countAlerts(null, "unread", null, null, doctorId));
        stats.setPriorityLevel1Count(doctorAlertDAO.countAlerts("danger", "unread", null, null, doctorId));

        // Phân tích AI bệnh nhân nguy hiểm chỉ dùng để làm giàu danh sách khẩn cấp,
        // không ghi đè KPI cảnh báo.
        AnalysisResult analysisResult =
                dangerousPatientService.analyzeDangerousPatients(doctorId);
        List<CriticalPatientAlertDTO> dangerousPatients = analysisResult.getDangerousPatients();

        DoctorLayoutHelper.prepare(request, doctor, "dashboard");
        request.setAttribute("stats", stats);
        request.setAttribute("urgentPatients", dangerousPatients);
        request.setAttribute("analysisResult", analysisResult);

        request.getRequestDispatcher("/WEB-INF/views/doctor/doctordashboard.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User doctor = AuthContext.requireDoctor(request, response);
        if (doctor == null) {
            return;
        }

        String patientId = request.getParameter("id");
        if (patientId == null || patientId.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/doctor-dashboard");
            return;
        }

        if (!AuthContext.ensurePatientAccess(doctor, patientDAO, patientId, response)) {
            return;
        }

        HighRiskPatientDTO detail = dangerousPatientService.getDangerousPatientDetail(
                doctor.getId().toString(),
                patientId
        );

        if (detail == null) {
            response.sendRedirect(request.getContextPath() + "/doctor-dashboard");
            return;
        }

        DoctorLayoutHelper.prepare(request, doctor, "alerts");
        request.setAttribute("detail", detail);
        HealthMetricAssessment.populateMetricLevels(request, detail);
        request.getRequestDispatcher("/WEB-INF/views/doctor/dangerouspatientanalysis.jsp")
                .forward(request, response);
    }
}
