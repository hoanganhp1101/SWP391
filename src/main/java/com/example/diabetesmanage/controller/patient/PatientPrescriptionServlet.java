package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.MedicationLogDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.model.MedicationLog;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.Prescription;

import com.example.diabetesmanage.service.GeminiService;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;
import java.sql.Date;
import java.time.LocalDate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "PatientPrescriptionServlet", urlPatterns = {"/patient-prescriptions", "/patient-prescriptions/toggle", "/patient-prescriptions/ai-reminder"})
public class PatientPrescriptionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        String action = request.getServletPath();
        
        PatientDAO patientDAO = new PatientDAO();
        String patientId = patientDAO.getDemoPatientId();
        
        if ("/patient-prescriptions/ai-reminder".equals(action)) {
            handleAiReminder(request, response, patientDAO, patientId);
            return;
        }
        
        if (patientId != null) {
            Patient patientInfo = patientDAO.getPatientById(patientId);
            request.setAttribute("patientInfo", patientInfo);
            
            PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
            Prescription latestPrescription = prescriptionDAO.getLatestPrescription(patientId);
            request.setAttribute("latestPrescription", latestPrescription);
            
            MedicationLogDAO logDAO = new MedicationLogDAO();
            
            String rangeParam = request.getParameter("range");
            String dateParam = request.getParameter("date");
            
            if (rangeParam != null && (rangeParam.equals("7") || rangeParam.equals("30"))) {
                int days = Integer.parseInt(rangeParam);
                int adherenceRate = logDAO.getAdherenceRate(patientId, days);
                request.setAttribute("viewMode", "progress");
                request.setAttribute("range", days);
                request.setAttribute("adherenceRate", adherenceRate);
            } else {
                Date targetDate = Date.valueOf(LocalDate.now()); // Default today
                if (dateParam != null && !dateParam.trim().isEmpty()) {
                    try {
                        targetDate = Date.valueOf(dateParam);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                List<MedicationLog> checklist = logDAO.getChecklistByDate(patientId, targetDate);
                request.setAttribute("viewMode", "checklist");
                request.setAttribute("todayChecklist", checklist);
                request.setAttribute("selectedDate", targetDate.toString());
            }
        }
        
        request.getRequestDispatcher("/WEB-INF/views/patient/patient-prescriptions.jsp").forward(request, response);
    }
    
    private void handleAiReminder(HttpServletRequest request, HttpServletResponse response, PatientDAO patientDAO, String patientId) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (patientId == null) {
            response.getWriter().write("{\"reminder\": \"Không tìm thấy thông tin bệnh nhân.\"}");
            return;
        }
        
        Patient patientInfo = patientDAO.getPatientById(patientId);
        MedicationLogDAO logDAO = new MedicationLogDAO();
        Date targetDate = Date.valueOf(LocalDate.now());
        
        String dateParam = request.getParameter("date");
        if (dateParam != null && !dateParam.trim().isEmpty()) {
            try { targetDate = Date.valueOf(dateParam); } catch (Exception e) {}
        }
        
        List<MedicationLog> checklist = logDAO.getChecklistByDate(patientId, targetDate);
        
        GeminiService geminiService = new GeminiService();
        String reminder = geminiService.generateMedicationReminder(patientInfo.getHoTen(), checklist);
        
        JsonObject json = new JsonObject();
        json.addProperty("reminder", reminder);
        response.getWriter().write(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getServletPath();
        if ("/patient-prescriptions/toggle".equals(action)) {
            PatientDAO patientDAO = new PatientDAO();
            String patientId = patientDAO.getDemoPatientId();
            String medicationId = request.getParameter("medicationId");
            String dateStr = request.getParameter("date");
            
            if (patientId != null && medicationId != null && !medicationId.trim().isEmpty()) {
                MedicationLogDAO logDAO = new MedicationLogDAO();
                Date targetDate = Date.valueOf(LocalDate.now());
                if (dateStr != null && !dateStr.trim().isEmpty()) {
                    try {
                        targetDate = Date.valueOf(dateStr);
                    } catch (Exception e) {}
                }
                
                boolean success = logDAO.toggleMedicationStatus(patientId, medicationId, targetDate);
                
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
