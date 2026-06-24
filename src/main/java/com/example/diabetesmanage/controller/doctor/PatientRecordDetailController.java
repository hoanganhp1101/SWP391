package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.medical.MedicalRecordDetailView;
import com.example.diabetesmanage.service.medical.MedicalRecordLoadService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/doctor/record-detail")
public class PatientRecordDetailController extends HttpServlet {

    private final MedicalRecordLoadService loadService = new MedicalRecordLoadService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String recordId = request.getParameter("id");

        if (recordId == null || recordId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/doctor/patient-records");
            return;
        }

        HealthRecord record = loadService.getRecordById(recordId);
        if (record == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Record not found");
            return;
        }

        MedicalRecordDetailView detailView = loadService.loadDetailViewByRecordId(recordId);

        request.setAttribute("record", record);
        request.setAttribute("detailView", detailView);

        request.getRequestDispatcher("/WEB-INF/views/doctor/medicalrecorddetail.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String patientId = request.getParameter("id");

        HealthRecord record = loadService.getLatestRecordByPatientId(patientId);
        if (record == null) {
            response.sendRedirect(request.getContextPath() + "/doctor/patient-records");
            return;
        }

        MedicalRecordDetailView detailView = loadService.loadDetailViewByPatientId(patientId);

        request.setAttribute("record", record);
        request.setAttribute("detailView", detailView);

        request.getRequestDispatcher("/WEB-INF/views/doctor/medicalrecorddetail.jsp")
                .forward(request, response);
    }
}
