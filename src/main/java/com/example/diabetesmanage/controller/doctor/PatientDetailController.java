package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.util.AuthContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Alias cũ: /doctor/patient-detail → /doctor/patient-list?id=...
 * (chi tiết thật sự do PatientListController forward sang patientdetail.jsp)
 */
@WebServlet("/doctor/patient-detail")
public class PatientDetailController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        redirectToListDetail(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        redirectToListDetail(request, response);
    }

    private void redirectToListDetail(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String patientId = request.getParameter("id");
        if (patientId == null || patientId.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/doctor/patient-list?error=missing");
            return;
        }

        StringBuilder target = new StringBuilder(request.getContextPath())
                .append("/doctor/patient-list?id=")
                .append(URLEncoder.encode(patientId.trim(), StandardCharsets.UTF_8));

        appendIfPresent(target, "fromDate", request.getParameter("fromDate"));
        appendIfPresent(target, "toDate", request.getParameter("toDate"));

        response.sendRedirect(target.toString());
    }

    private static void appendIfPresent(StringBuilder target, String name, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        target.append('&').append(name).append('=')
                .append(URLEncoder.encode(value.trim(), StandardCharsets.UTF_8));
    }
}
