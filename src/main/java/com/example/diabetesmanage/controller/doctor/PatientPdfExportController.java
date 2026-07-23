package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.service.PatientDetailPdfService;
import com.example.diabetesmanage.service.PatientDetailService;
import com.example.diabetesmanage.service.PatientDetailService.DetailBundle;
import com.example.diabetesmanage.util.AuthContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet("/doctor/export-patient-pdf")
public class PatientPdfExportController extends HttpServlet {

    private final PatientDAO patientDAO = new PatientDAO();
    private final PatientDetailService patientDetailService = new PatientDetailService();
    private final PatientDetailPdfService pdfService = new PatientDetailPdfService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String patientId = request.getParameter("id");
        if (patientId == null || patientId.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã bệnh nhân");
            return;
        }

        if (!AuthContext.ensurePatientAccess(user, patientDAO, patientId, response)) {
            return;
        }

        LocalDate fromDate = parseDateParam(request.getParameter("fromDate"));
        LocalDate toDate = parseDateParam(request.getParameter("toDate"));
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            fromDate = null;
            toDate = null;
        }

        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        DetailBundle bundle = patientDetailService.load(patientId.trim(), scopeDoctorId, fromDate, toDate);
        if (bundle.patient == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy bệnh nhân");
            return;
        }

        try {
            byte[] pdfBytes = pdfService.generate(
                    bundle.patient, bundle.healthRecord, bundle.history, fromDate, toDate);
            String code = bundle.patient.getPatientCode();
            String safeCode = code == null || code.isBlank()
                    ? "benh-nhan"
                    : code.replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = "chi-tiet-benh-nhan-" + safeCode + ".pdf";

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

    private LocalDate parseDateParam(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
