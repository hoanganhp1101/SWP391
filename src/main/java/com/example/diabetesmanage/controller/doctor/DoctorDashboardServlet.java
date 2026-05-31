package com.example.diabetesmanage.controller.doctor;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "DoctorDashboardServlet", urlPatterns = {"/doctor-dashboard"})
public class DoctorDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Chuyển hướng tới giao diện JSP
        request.getRequestDispatcher("/WEB-INF/views/doctor/doctordashboard.jsp").forward(request, response);
    }
}
