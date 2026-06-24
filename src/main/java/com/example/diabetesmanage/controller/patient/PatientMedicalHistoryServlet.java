package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.MedicalDocumentDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.MedicalDocument;
import com.example.diabetesmanage.model.Patient;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "PatientMedicalHistoryServlet", urlPatterns = {"/patient-medical-history"})
public class PatientMedicalHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PatientDAO patientDAO = new PatientDAO();
        String patientId = patientDAO.getDemoPatientId();

        if (patientId != null) {
            Patient patientInfo = patientDAO.getPatientById(patientId);
            MedicalDocumentDAO documentDAO = new MedicalDocumentDAO();
            List<MedicalDocument> medicalDocuments = documentDAO.getAllDocumentsByPatient(patientId);

            request.setAttribute("patientInfo", patientInfo);
            request.setAttribute("medicalDocuments", medicalDocuments);
        }

        request.getRequestDispatcher("/WEB-INF/views/patient/patient-medical-history.jsp").forward(request, response);
    }
}
