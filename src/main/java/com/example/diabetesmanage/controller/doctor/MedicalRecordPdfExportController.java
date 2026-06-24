package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.model.medical.MedicalRecordDetailView;
import com.example.diabetesmanage.model.medical.PdfExportType;
import com.example.diabetesmanage.service.medical.MedicalRecordLoadService;
import com.example.diabetesmanage.service.medical.MedicalRecordPdfExportService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/doctor/record-export-pdf")
public class MedicalRecordPdfExportController extends HttpServlet {

    private final MedicalRecordLoadService loadService = new MedicalRecordLoadService();
    private final MedicalRecordPdfExportService pdfService = new MedicalRecordPdfExportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String recordId = request.getParameter("id");
        PdfExportType exportType = PdfExportType.fromParam(request.getParameter("type"));

        if (recordId == null || recordId.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing record id");
            return;
        }

        MedicalRecordDetailView view = loadService.loadDetailViewByRecordId(recordId);
        if (view == null || view.getRecordId() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Record not found");
            return;
        }

        try {
            byte[] pdfBytes = pdfService.generateMedicalRecordPdf(view, exportType);

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
