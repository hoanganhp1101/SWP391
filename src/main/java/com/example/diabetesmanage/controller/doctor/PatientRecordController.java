package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.model.HealthRecord;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/doctor/patient-records")
public class PatientRecordController extends HttpServlet {

    private final HealthRecordDAO healthRecordDAO =
            new HealthRecordDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String patientId =
                request.getParameter("patientId");

        if (patientId == null || patientId.trim().isEmpty()) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/doctor/patients"
            );
            return;
        }

        List<HealthRecord> records =
                healthRecordDAO.getByPatient(patientId);

        request.setAttribute(
                "records",
                records
        );

        request.setAttribute(
                "patientId",
                patientId
        );

        request.getRequestDispatcher(
                "/doctor/patient-records.jsp"
        ).forward(request, response);
    }
}
