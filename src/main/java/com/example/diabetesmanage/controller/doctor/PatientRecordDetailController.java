package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.model.medical.MedicalRecordDetailView;
import com.example.diabetesmanage.service.medical.MedicalRecordViewService;
import com.example.diabetesmanage.util.AuthContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/doctor/record-detail")
public class PatientRecordDetailController extends HttpServlet {

    private final MedicalRecordViewService viewService = new MedicalRecordViewService();
    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String recordId = request.getParameter("id");
        if (recordId == null || recordId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/doctor/patient-records");
            return;
        }

        if (!AuthContext.ensureRecordAccess(user, patientDAO, healthRecordDAO, recordId, response)) {
            return;
        }

        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        HealthRecord record = viewService.getRecordById(recordId, scopeDoctorId);
        if (record == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Record not found");
            return;
        }

        MedicalRecordDetailView detailView = viewService.loadDetailViewByRecordId(recordId, scopeDoctorId);

        request.setAttribute("record", record);
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
        HealthRecord record = viewService.getLatestRecordByPatientId(patientId, scopeDoctorId);
        if (record == null) {
            response.sendRedirect(request.getContextPath() + "/doctor/patient-records");
            return;
        }

        MedicalRecordDetailView detailView = viewService.loadDetailViewByPatientId(patientId, scopeDoctorId);

        request.setAttribute("record", record);
        request.setAttribute("detailView", detailView);
        request.getRequestDispatcher("/WEB-INF/views/doctor/medicalrecorddetail.jsp")
                .forward(request, response);
    }
}
