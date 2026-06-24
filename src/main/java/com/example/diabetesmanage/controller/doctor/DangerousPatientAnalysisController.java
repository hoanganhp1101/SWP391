package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.DoctorDashboardDAO;
import com.example.diabetesmanage.model.DangerousPatientDetail;
import com.example.diabetesmanage.service.DangerousPatientService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/doctor/dangerous-patient-analysis")
public class DangerousPatientAnalysisController extends HttpServlet {

    private final DangerousPatientService dangerousPatientService = new DangerousPatientService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String patientId = request.getParameter("id");

        if (patientId == null || patientId.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/doctor-dashboard");
            return;
        }

        DangerousPatientDetail detail = dangerousPatientService.getDangerousPatientDetail(
                DoctorDashboardDAO.DOCTOR_EMAIL,
                patientId
        );

        if (detail == null) {
            response.sendRedirect(request.getContextPath() + "/doctor-dashboard");
            return;
        }

        request.setAttribute("detail", detail);
        request.getRequestDispatcher("/WEB-INF/views/doctor/dangerouspatientanalysis.jsp")
                .forward(request, response);
    }
}
