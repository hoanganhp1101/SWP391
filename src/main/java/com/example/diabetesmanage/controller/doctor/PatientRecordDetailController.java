package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.model.HealthRecord;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/doctor/record-detail")
public class PatientRecordDetailController extends HttpServlet {

    private final HealthRecordDAO healthRecordDAO =
            new HealthRecordDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String recordId = request.getParameter("id");

        if (recordId == null || recordId.trim().isEmpty()) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/doctor/patients"
            );
            return;
        }

        HealthRecord record =
                healthRecordDAO.getRecordById(recordId);

        if (record == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Record not found"
            );
            return;
        }

        request.setAttribute(
                "record",
                record
        );

        request.getRequestDispatcher(
                "/doctor/record-detail.jsp"
        ).forward(request, response);
    }
}
