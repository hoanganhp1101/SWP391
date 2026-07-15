package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.util.AuthContext;
import com.example.diabetesmanage.util.DoctorLayoutHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/doctor/patient-records")
public class PatientRecordController extends HttpServlet {

    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();

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

        List<MedicalEncounter> records;
        if (hasDate || hasKeyword) {
            records = encounterDAO.searchEncounters(
                    startDate, endDate, keyword, scopeDoctorId);
        } else {
            records = encounterDAO.getEncounters(scopeDoctorId);
        }

        if (patientId != null && !patientId.isBlank()) {
            List<MedicalEncounter> filtered = new ArrayList<>();
            for (MedicalEncounter record : records) {
                if (patientId.equals(record.getPatientId())) {
                    filtered.add(record);
                }
            }
            records = filtered;
        }

        DoctorLayoutHelper.prepare(request, user, "records");
        request.setAttribute("records", records);
        request.setAttribute("patientId", patientId);
        request.getRequestDispatcher("/WEB-INF/views/doctor/medicalrecordmanagement.jsp")
                .forward(request, response);
    }
}
