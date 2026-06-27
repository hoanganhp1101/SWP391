package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.model.form.HealthRecordUpdateForm;
import com.example.diabetesmanage.util.AuthContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/doctor/patient-detail")
public class PatientDetailController extends HttpServlet {

    private final PatientDAO patientDAO = new PatientDAO();
    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();

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

        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        Patient patient = patientDAO.getPatientById(patientId, scopeDoctorId);
        if (patient == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Patient not found");
            return;
        }

        HealthRecord latestHealthRecord = healthRecordDAO.findLatestByPatientId(patientId, scopeDoctorId);
        boolean editMode = "1".equals(request.getParameter("edit"));

        request.setAttribute("patient", patient);
        request.setAttribute("latestHealthRecord", latestHealthRecord);
        request.setAttribute("editMode", editMode);
        if (latestHealthRecord != null) {
            request.setAttribute("hrForm",
                    HealthRecordUpdateForm.fromHealthRecord(latestHealthRecord, patientId));
        }
        request.setAttribute("currentUser", user);
        request.getRequestDispatcher("/WEB-INF/views/doctor/patientdetail.jsp")
                .forward(request, response);
    }
}
