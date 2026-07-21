package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.AdminReportDAO;
import com.example.diabetesmanage.dao.HighRiskPatientDAO;
import com.example.diabetesmanage.model.AdminReportStats;
import com.example.diabetesmanage.model.HighRiskPatient;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "AdminReportController", urlPatterns = {"/admin/reports"})
public class AdminReportController extends HttpServlet {

    private AdminReportDAO adminReportDAO;
    private HighRiskPatientDAO highRiskPatientDAO;

    public AdminReportController() {
    }

    AdminReportController(AdminReportDAO adminReportDAO, HighRiskPatientDAO highRiskPatientDAO) {
        this.adminReportDAO = adminReportDAO;
        this.highRiskPatientDAO = highRiskPatientDAO;
    }

    @Override
    public void init() throws ServletException {
        if (adminReportDAO == null) {
            adminReportDAO = new AdminReportDAO();
        }
        if (highRiskPatientDAO == null) {
            highRiskPatientDAO = new HighRiskPatientDAO();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int periodDays = parsePeriodDays(request.getParameter("periodDays"));
        AdminReportStats reportStats = adminReportDAO.getReportStats(periodDays);
        List<HighRiskPatient> priorityPatients = highRiskPatientDAO.getMonitoredPatients(null, null)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        request.setAttribute("reportStats", reportStats);
        request.setAttribute("priorityPatients", priorityPatients);
        request.setAttribute("selectedPeriodDays", periodDays);

        request.getRequestDispatcher("/WEB-INF/views/admin/reports.jsp").forward(request, response);
    }

    int parsePeriodDays(String value) {
        if ("7".equals(value)) {
            return 7;
        }
        if ("90".equals(value)) {
            return 90;
        }
        return 30;
    }
}
