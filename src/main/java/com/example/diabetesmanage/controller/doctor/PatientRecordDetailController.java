package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.service.medical.EncounterDetail;
import com.example.diabetesmanage.service.medical.MedicalRecordViewService;
import com.example.diabetesmanage.util.AuthContext;
import com.example.diabetesmanage.util.DoctorLayoutHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/doctor/record-detail")
public class PatientRecordDetailController extends HttpServlet {

    private final MedicalRecordViewService viewService = new MedicalRecordViewService();
    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String encounterId = request.getParameter("id");
        if (encounterId == null || encounterId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/doctor/patient-records");
            return;
        }

        if (!AuthContext.ensureEncounterAccess(user, patientDAO, encounterDAO, encounterId, response)) {
            return;
        }

        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        MedicalEncounter encounter = encounterDAO.getEncounterById(encounterId, scopeDoctorId);
        if (encounter == null) {
            encounter = encounterDAO.getEncounterById(encounterId, null);
        }
        if (encounter == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Encounter not found");
            return;
        }

        EncounterDetail detailView = viewService.loadDetailViewByEncounterId(encounterId, scopeDoctorId);
        if (detailView == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Encounter not found");
            return;
        }

        DoctorLayoutHelper.prepare(request, user, "records");
        request.setAttribute("encounter", encounter);
        request.setAttribute("detailView", detailView);
        request.getRequestDispatcher("/WEB-INF/views/doctor/medicalrecorddetail.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String patientId = request.getParameter("id");
        if (!AuthContext.ensurePatientAccess(user, patientDAO, patientId, response)) {
            return;
        }

        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        MedicalEncounter encounter = viewService.getLatestEncounterByPatientId(patientId, scopeDoctorId);
        if (encounter == null) {
            response.sendRedirect(request.getContextPath() + "/doctor/patient-records");
            return;
        }

        EncounterDetail detailView = viewService.loadDetailViewByEncounterId(
                encounter.getId(), scopeDoctorId);

        DoctorLayoutHelper.prepare(request, user, "records");
        request.setAttribute("encounter", encounter);
        request.setAttribute("detailView", detailView);
        request.getRequestDispatcher("/WEB-INF/views/doctor/medicalrecorddetail.jsp")
                .forward(request, response);
    }
}
