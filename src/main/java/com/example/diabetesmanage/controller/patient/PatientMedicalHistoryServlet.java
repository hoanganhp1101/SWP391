package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.MedicalDocumentDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.MedicalDocument;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.util.PatientPortalAuth;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "PatientMedicalHistoryServlet", urlPatterns = {"/patient-medical-history"})
public class PatientMedicalHistoryServlet extends HttpServlet {
    private static final int PAGE_SIZE = 5;// cho page tổng là 5

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }

        PatientDAO patientDAO = new PatientDAO();
        Patient patientInfo = patientDAO.getPatientById(patientId);
        MedicalDocumentDAO documentDAO = new MedicalDocumentDAO();
        int totalRecords = documentDAO.countDocumentsByPatient(patientId);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalRecords / PAGE_SIZE));
        int currentPage = parsePage(request.getParameter("page"), totalPages);
        int offset = (currentPage - 1) * PAGE_SIZE;
        List<MedicalDocument> medicalDocuments = documentDAO.getDocumentsByPatient(patientId, offset, PAGE_SIZE);

        request.setAttribute("patientInfo", patientInfo);
        request.setAttribute("medicalDocuments", medicalDocuments);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", totalRecords);

        request.getRequestDispatcher("/WEB-INF/views/patient/patient-medical-history.jsp").forward(request, response);
    }

    private int parsePage(String pageParam, int totalPages) {// page nếu không sang trang 2 thì trả về trang đầu 
        if (pageParam == null || pageParam.trim().isEmpty()) {
            return 1;
        }
        try {
            int page = Integer.parseInt(pageParam);
            if (page < 1) {
                return 1;
            }
            return Math.min(page, totalPages);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
