package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
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

        List<HealthRecord> records;

        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        if (startDate != null && endDate != null
                && !startDate.isEmpty()
                && !endDate.isEmpty()) {

            records = healthRecordDAO.getHealthRecordsByDateRange(
                    startDate,
                    endDate
            );

        } else {

            records = healthRecordDAO.getHealthRecord();
        }

        request.setAttribute(
                "records",
                records
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/doctor/medicalrecordmanagement.jsp"
        ).forward(request, response);
    }
}
