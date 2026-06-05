package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.MedicationLogDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.model.MedicationLog;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.Prescription;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "PatientPrescriptionServlet", urlPatterns = {"/patient-prescriptions", "/patient-prescriptions/toggle"})
public class PatientPrescriptionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        PatientDAO patientDAO = new PatientDAO();
        String patientId = patientDAO.getDemoPatientId();
        
        if (patientId != null) {
            Patient patientInfo = patientDAO.getPatientById(patientId);
            request.setAttribute("patientInfo", patientInfo);
            
            PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
            Prescription latestPrescription = prescriptionDAO.getLatestPrescription(patientId);
            request.setAttribute("latestPrescription", latestPrescription);
            
            MedicationLogDAO logDAO = new MedicationLogDAO();
            List<MedicationLog> todayChecklist = logDAO.getTodayChecklist(patientId);
            request.setAttribute("todayChecklist", todayChecklist);
        }
        
        request.getRequestDispatcher("/WEB-INF/views/patient/patient-prescriptions.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getServletPath();
        if ("/patient-prescriptions/toggle".equals(action)) {
            PatientDAO patientDAO = new PatientDAO();
            String patientId = patientDAO.getDemoPatientId();
            String medicationId = request.getParameter("medicationId");
            
            if (patientId != null && medicationId != null && !medicationId.trim().isEmpty()) {
                MedicationLogDAO logDAO = new MedicationLogDAO();
                boolean success = logDAO.toggleMedicationStatus(patientId, medicationId);
                
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                if (success) {
                    response.getWriter().write("{\"status\":\"success\"}");
                } else {
                    response.getWriter().write("{\"status\":\"error\", \"message\":\"Lỗi cập nhật CSDL\"}");
                }
            } else {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"status\":\"error\", \"message\":\"Thiếu thông tin\"}");
            }
        }
    }
}
