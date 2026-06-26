package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.util.AuthContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/doctor/patient-records")
public class PatientRecordController extends HttpServlet {

    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String keyword = request.getParameter("keyword");
        String patientId = request.getParameter("patientId");

        boolean hasDate = startDate != null && !startDate.isBlank()
                && endDate != null && !endDate.isBlank();
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        List<HealthRecord> records;
        if (hasDate || hasKeyword) {
            records = healthRecordDAO.searchHealthRecordRecords(
                    startDate, endDate, keyword, scopeDoctorId);
        } else {
            records = healthRecordDAO.getHealthRecords(scopeDoctorId);
        }

        if (patientId != null && !patientId.isBlank()) {
            List<HealthRecord> filtered = new ArrayList<>();
            for (HealthRecord record : records) {
                if (record.getPatient() != null && patientId.equals(record.getPatient().getId())) {
                    filtered.add(record);
                }
            }
            records = filtered;
        }

        request.setAttribute("records", records);
        request.setAttribute("patientId", patientId);
        request.getRequestDispatcher("/WEB-INF/views/doctor/medicalrecordmanagement.jsp")
                .forward(request, response);
    }
}
