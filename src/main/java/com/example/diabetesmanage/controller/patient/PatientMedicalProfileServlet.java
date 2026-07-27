package com.example.diabetesmanage.controller.patient;

import java.io.IOException;
import java.io.File;
import java.sql.Date;
import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.dao.AlertDAO;
import com.example.diabetesmanage.dao.MedicalDocumentDAO;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Prescription;
import com.example.diabetesmanage.model.Alert;
import com.example.diabetesmanage.model.MedicalDocument;
import com.example.diabetesmanage.util.PatientPortalAuth;

@WebServlet(name = "PatientMedicalProfileServlet", urlPatterns = { "/patient-medical-profile" })
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024 * 11)
public class PatientMedicalProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }

        PatientDAO patientDAO = new PatientDAO();
        Patient patientInfo = patientDAO.getPatientById(patientId);
        request.setAttribute("patientInfo", patientInfo);

        HealthRecordDAO recordDAO = new HealthRecordDAO();
        HealthRecord latestRecord = recordDAO.getLatestComprehensiveRecord(patientId);
        request.setAttribute("latestRecord", latestRecord);

        PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
        Prescription latestPrescription = prescriptionDAO.getLatestPrescription(patientId);
        request.setAttribute("latestPrescription", latestPrescription);

        AlertDAO alertDAO = new AlertDAO();
        List<Alert> recentAlerts = alertDAO.getRecentAlerts(patientId);
        request.setAttribute("alerts", recentAlerts);

        MedicalDocumentDAO docDAO = new MedicalDocumentDAO();
        List<MedicalDocument> documents = docDAO.getRecentDocuments(patientId);
        request.setAttribute("medicalDocuments", documents);

        request.getRequestDispatcher("/WEB-INF/views/patient/patient-medical-profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }

        PatientDAO patientDAO = new PatientDAO();
        Patient p = patientDAO.getPatientById(patientId);
        if (p != null) {
            p.setGioiTinh(request.getParameter("gioiTinh"));
            String chieuCaoStr = request.getParameter("chieuCaoCm");
            Double parsedHeight = parseSafeDouble(chieuCaoStr);
            if (parsedHeight != null && parsedHeight > 0 && parsedHeight < 300) {
                p.setChieuCaoCm(parsedHeight);
            }
            p.setDiaChi(request.getParameter("diaChi"));
            p.setBaoHiemYTe(request.getParameter("baoHiemYTe"));
            p.setTienSuBenh(request.getParameter("tienSuBenh"));
            p.setTienSuGiaDinh(request.getParameter("tienSuGiaDinh"));
            p.setDiUng(request.getParameter("diUng"));
            p.setNhomMau(request.getParameter("nhomMau"));

            String ngayChanDoanStr = request.getParameter("ngayChanDoanTieuDuong");
            if (ngayChanDoanStr != null && !ngayChanDoanStr.isEmpty()) {
                try {
                    p.setNgayChanDoanTieuDuong(Date.valueOf(ngayChanDoanStr));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid date format
                }
            }

            patientDAO.updatePatientMedicalProfile(p);

            String canNangStr = request.getParameter("canNangKg");
            String systoleStr = request.getParameter("huyetApTamThu");
            String diastoleStr = request.getParameter("huyetApTamTruong");
            String heartRateStr = request.getParameter("nhipTim");
            String glucoseStr = request.getParameter("duongHuyetMgdl");
            String hba1cStr = request.getParameter("hba1c");
            String cholesterolStr = request.getParameter("cholesterol");
            String triglycerideStr = request.getParameter("triglyceride");

            boolean hasVitals = (canNangStr != null && !canNangStr.isEmpty()) ||
                    (systoleStr != null && !systoleStr.isEmpty()) ||
                    (heartRateStr != null && !heartRateStr.isEmpty()) ||
                    (glucoseStr != null && !glucoseStr.isEmpty()) ||
                    (hba1cStr != null && !hba1cStr.isEmpty()) ||
                    (cholesterolStr != null && !cholesterolStr.isEmpty()) ||
                    (triglycerideStr != null && !triglycerideStr.isEmpty());

            if (hasVitals) {
                Double weight = parseSafeDouble(canNangStr);
                Integer systole = parseSafeInteger(systoleStr);
                Integer diastole = parseSafeInteger(diastoleStr);
                Integer heartRate = parseSafeInteger(heartRateStr);
                Double glucose = parseSafeDouble(glucoseStr);
                Double hba1c = parseSafeDouble(hba1cStr);
                Double cholesterol = parseSafeDouble(cholesterolStr);
                Double triglyceride = parseSafeDouble(triglycerideStr);

                // Basic range validation
                if (weight != null && (weight < 20 || weight > 300))
                    weight = null;
                if (systole != null && (systole < 50 || systole > 250))
                    systole = null;
                if (diastole != null && (diastole < 30 || diastole > 150))
                    diastole = null;
                if (heartRate != null && (heartRate < 30 || heartRate > 250))
                    heartRate = null;
                if (hba1c != null && (hba1c < 2 || hba1c > 30))
                    hba1c = null;

                Double bmi = null;
                if (weight != null && p.getChieuCaoCm() != null && p.getChieuCaoCm() > 0) {
                    double heightM = p.getChieuCaoCm() / 100.0;
                    bmi = weight / (heightM * heightM);
                    bmi = Math.round(bmi * 10.0) / 10.0;
                }
                HealthRecordDAO hrDAO = new HealthRecordDAO();
                hrDAO.insertExtractedHealthRecord(patientId, weight, bmi, systole, diastole, heartRate, glucose, hba1c,
                        cholesterol, triglyceride);
            }
        }

        Part filePart = request.getPart("pdfFile");
        if (filePart != null && filePart.getSize() > 0 && p != null) {
            if (filePart.getSize() > 10 * 1024 * 1024) {
                response.sendRedirect(request.getContextPath() + "/patient-medical-profile?error=" +
                        URLEncoder.encode("Tệp đính kèm không được vượt quá 10MB.", StandardCharsets.UTF_8));
                return;
            }
            String contentType = filePart.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                response.sendRedirect(request.getContextPath() + "/patient-medical-profile?error=" +
                        URLEncoder.encode("Chỉ hỗ trợ tải lên tệp PDF.", StandardCharsets.UTF_8));
                return;
            }

            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            if (fileName.toLowerCase().endsWith(".pdf")) {
                String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads" + File.separator
                        + "documents";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                String newFileName = System.currentTimeMillis() + "_" + fileName;
                filePart.write(uploadPath + File.separator + newFileName);

                MedicalDocument doc = new MedicalDocument();
                doc.setPatientId(patientId);
                doc.setBacSiId(p.getBacSiId());
                doc.setLoaiTaiLieu(request.getParameter("loaiTaiLieu") != null ? request.getParameter("loaiTaiLieu")
                        : "Bệnh án ngoài");
                doc.setTrangThai("hoan_thanh");
                doc.setFileUrl("uploads/documents/" + newFileName);
                doc.setNgayThucHien(new java.sql.Date(System.currentTimeMillis()));

                MedicalDocumentDAO docDAO = new MedicalDocumentDAO();
                docDAO.addDocument(doc);
            }
        }

        response.sendRedirect(request.getContextPath() + "/patient-medical-profile?success=true");
    }

    private Double parseSafeDouble(String value) {
        if (value == null || value.trim().isEmpty())
            return null;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseSafeInteger(String value) {
        if (value == null || value.trim().isEmpty())
            return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
