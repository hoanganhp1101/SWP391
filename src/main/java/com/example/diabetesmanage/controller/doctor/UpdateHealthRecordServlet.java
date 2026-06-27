package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.model.form.HealthRecordUpdateForm;
import com.example.diabetesmanage.util.AuthContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/doctor/health-record/update")
public class UpdateHealthRecordServlet extends HttpServlet {

    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        HealthRecordUpdateForm form;
        try {
            form = HealthRecordUpdateForm.fromRequest(request);
        } catch (NumberFormatException ex) {
            redirectWithEdit(request, response, request.getParameter("patientId"));
            return;
        }

        if (form.getRecordId() == null || form.getRecordId().isBlank()
                || form.getPatientId() == null || form.getPatientId().isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thieu thong tin ho so");
            return;
        }

        if (!AuthContext.ensureRecordAccess(user, patientDAO, healthRecordDAO, form.getRecordId(), response)) {
            return;
        }

        String recordPatientId = healthRecordDAO.getPatientIdByRecordId(form.getRecordId());
        if (!form.getPatientId().equals(recordPatientId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ho so khong thuoc benh nhan nay");
            return;
        }

        try {
            healthRecordDAO.update(form);
            response.sendRedirect(request.getContextPath()
                    + "/doctor/patient-detail?id=" + form.getPatientId() + "&hrUpdated=1");
        } catch (SQLException ex) {
            ex.printStackTrace();
            response.sendRedirect(request.getContextPath()
                    + "/doctor/patient-detail?id=" + form.getPatientId() + "&edit=1&hrError=1");
        }
    }

    private void redirectWithEdit(HttpServletRequest request, HttpServletResponse response, String patientId)
            throws IOException {
        if (patientId == null || patientId.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/doctor/patient-list");
            return;
        }
        response.sendRedirect(request.getContextPath()
                + "/doctor/patient-detail?id=" + patientId + "&edit=1&hrError=1");
    }
}
