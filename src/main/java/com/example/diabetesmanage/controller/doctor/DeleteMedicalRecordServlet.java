package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.MedicalEncounterDAO;
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
    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String encounterId = request.getParameter("id");
        if (!AuthContext.ensureEncounterAccess(user, patientDAO, encounterDAO, encounterId, response)) {
            return;
        }

        try {
            deleteService.deleteByEncounterId(encounterId, AuthContext.scopeDoctorId(user));
            response.sendRedirect(request.getContextPath() + "/doctor/patient-records?deleted=1");
        } catch (SQLException ex) {
            ex.printStackTrace();
            response.sendRedirect(request.getContextPath()
                    + "/doctor/record-detail?id=" + encounterId + "&error=delete");
        }
    }
}
