package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.util.AuthContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/doctor/patient-detail")
public class PatientDetailController extends HttpServlet {

    private final PatientDAO patientDAO = new PatientDAO();
    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String patientId = request.getParameter("id");
        if (patientId == null || patientId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/doctor/patient-list");
            return;
        }

        if (!AuthContext.ensurePatientAccess(user, patientDAO, patientId, response)) {
            return;
        }

        Patient patient = patientDAO.getPatientById(patientId, AuthContext.scopeDoctorId(user));
        if (patient == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Patient not found");
            return;
        }

        List<MedicalEncounter> encounters = encounterDAO.findByPatientId(patientId);

        request.setAttribute("patient", patient);
        request.setAttribute("encounters", encounters);
        request.setAttribute("currentUser", user);
        request.getRequestDispatcher("/WEB-INF/views/doctor/patientdetail.jsp")
                .forward(request, response);
    }
}
