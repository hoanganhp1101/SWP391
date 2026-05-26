package com.example.diabetesmanage.controller.patient;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "PatientDashboardServlet", urlPatterns = {"/patient-dashboard"})
public class PatientDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Mock System Data cho giao diện Patient
        request.setAttribute("latestGlucose", 113);
        request.setAttribute("glucoseTime", "2 hours ago");
        request.setAttribute("hba1c", 6.8);
        request.setAttribute("dailyCarb", 180);
        request.setAttribute("nextAppointmentDate", "May 20");
        request.setAttribute("nextAppointmentDoctor", "Dr. Smith - Endocrinology");

        // Chuyển hướng tới giao diện JSP
        request.getRequestDispatcher("/WEB-INF/views/patient/patient-dashboard.jsp").forward(request, response);
    }
}
