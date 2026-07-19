package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.dto.MedicalEncounterDTO;
import com.example.diabetesmanage.service.MedicalRecordPdfService;
import com.example.diabetesmanage.service.MedicalRecordPdfService.PdfExportType;
import com.example.diabetesmanage.service.MedicalRecordViewService;
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
    private final MedicalRecordPdfService pdfService = new MedicalRecordPdfService();
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
        PdfExportType exportType = PdfExportType.fromParam(request.getParameter("type"));

        if (encounterId == null || encounterId.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã lần khám");
            return;
        }

        if (!AuthContext.ensureEncounterAccess(user, patientDAO, encounterDAO, encounterId, response)) {
            return;
        }

        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        MedicalEncounterDTO view = viewService.loadDetailViewByEncounterId(encounterId, scopeDoctorId);
        if (view == null || view.getRecordId() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy hồ sơ");
            return;
        }

        try {
            byte[] pdfBytes = pdfService.generateMedicalRecordPdf(view, exportType);
            String fileName = "ho-so-kham-" + safeFileName(view.getRecordCode()) + "-" + exportType.getParam() + ".pdf";

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
            return "medical-encounter";
        }
        return value.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
