package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.model.medical.MedicalRecordDetailView;
import com.example.diabetesmanage.service.medical.MedicalRecordViewService;
import com.example.diabetesmanage.service.medical.MedicalRecordViewService.PdfExportType;
import com.example.diabetesmanage.util.AuthContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/doctor/record-export-pdf")
public class MedicalRecordPdfExportController extends HttpServlet {

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
        PdfExportType exportType = PdfExportType.fromParam(request.getParameter("type"));

        if (recordId == null || recordId.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing record id");
            return;
        }

        if (!AuthContext.ensureRecordAccess(user, patientDAO, healthRecordDAO, recordId, response)) {
            return;
        }

        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        MedicalRecordDetailView view = viewService.loadDetailViewByRecordId(recordId, scopeDoctorId);
        if (view == null || view.getRecordId() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Record not found");
            return;
        }

        try {
            byte[] pdfBytes = viewService.generateMedicalRecordPdf(view, exportType);
            String fileName = "ho-so-" + safeFileName(view.getRecordCode()) + "-" + exportType.getParam() + ".pdf";

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setContentLength(pdfBytes.length);

            try (OutputStream out = response.getOutputStream()) {
                out.write(pdfBytes);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Khong the xuat PDF: " + e.getMessage());
        }
    }

    private String safeFileName(String value) {
        if (value == null || value.isBlank()) {
            return "medical-record";
        }
        return value.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
