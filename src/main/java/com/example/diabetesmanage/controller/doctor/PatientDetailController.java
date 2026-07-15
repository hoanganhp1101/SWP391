package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.service.medical.HealthRecordService;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.util.AuthContext;
import com.example.diabetesmanage.util.DoctorLayoutHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/doctor/patient-detail")
public class PatientDetailController extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(PatientDetailController.class.getName());

    private final PatientDAO patientDAO = new PatientDAO();
    private final HealthRecordService healthRecordService = new HealthRecordService();

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

        String normalizedPatientId = patientId.trim();
        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        Patient patient = patientDAO.getPatientById(normalizedPatientId, scopeDoctorId);
        if (patient == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Patient not found");
            return;
        }

        HealthRecord healthRecord = healthRecordService.getByPatientId(normalizedPatientId, null);
        LOG.log(Level.INFO, "patient-detail patientId={0} healthRecordPresent={1} healthRecordId={2}",
                new Object[]{
                        normalizedPatientId,
                        healthRecord != null,
                        healthRecord != null ? healthRecord.getId() : null
                });

        DoctorLayoutHelper.prepare(request, user, "patients");
        request.setAttribute("patient", patient);
        request.setAttribute("healthRecord", healthRecord);
        request.setAttribute("hasHealthRecord", healthRecord != null);
        request.setAttribute("currentUser", user);
        request.getRequestDispatcher("/WEB-INF/views/doctor/patientdetail.jsp")
                .forward(request, response);
    }
}
