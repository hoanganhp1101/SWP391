package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.HighRiskPatientDAO;
import com.example.diabetesmanage.model.HighRiskPatient;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "HighRiskPatientController", urlPatterns = {"/admin/high-risk-patients"})
public class HighRiskPatientController extends HttpServlet {

    private HighRiskPatientDAO highRiskPatientDAO;

    @Override
    public void init() throws ServletException {
        highRiskPatientDAO = new HighRiskPatientDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        String riskLevel = request.getParameter("riskLevel");

        List<HighRiskPatient> allPatients = highRiskPatientDAO.getMonitoredPatients(keyword, null);
        List<HighRiskPatient> filteredPatients = highRiskPatientDAO.getMonitoredPatients(keyword, riskLevel);

        request.setAttribute("patientList", filteredPatients);
        request.setAttribute("selectedRiskLevel", riskLevel);
        request.setAttribute("searchKeyword", keyword);
        request.setAttribute("totalMonitored", allPatients.size());
        request.setAttribute("criticalCount", countByRiskLevel(allPatients, "critical"));
        request.setAttribute("highCount", countByRiskLevel(allPatients, "high"));
        request.setAttribute("mediumCount", countByRiskLevel(allPatients, "medium"));

        request.getRequestDispatcher("/WEB-INF/views/admin/high-risk-patients.jsp").forward(request, response);
    }

    private long countByRiskLevel(List<HighRiskPatient> patients, String riskLevel) {
        return patients.stream()
                .filter(patient -> riskLevel.equals(patient.getRiskLevel()))
                .count();
    }
}
