package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.service.medical.MedicalRecordDeleteService;
import com.example.diabetesmanage.util.AuthContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/doctor/record-delete")
public class DeleteMedicalRecordServlet extends HttpServlet {

    private final MedicalRecordDeleteService deleteService = new MedicalRecordDeleteService();
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

        String recordId = request.getParameter("id");
        if (!AuthContext.ensureRecordAccess(user, patientDAO, healthRecordDAO, recordId, response)) {
            return;
        }

        try {
            deleteService.deleteByHealthRecordId(recordId, AuthContext.scopeDoctorId(user));
            response.sendRedirect(request.getContextPath() + "/doctor/patient-records?deleted=1");
        } catch (SQLException ex) {
            ex.printStackTrace();
            response.sendRedirect(request.getContextPath()
                    + "/doctor/record-detail?id=" + recordId + "&error=delete");
        }
    }
}
