package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.MedicationLogDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "UpdateMedicationMobileServlet", urlPatterns = {"/api/mobile/medication/toggle"})
public class UpdateMedicationMobileServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        Map<String, Object> responseData = new HashMap<>();

        try {
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);
            String patientId = jsonRequest.has("patientId") ? jsonRequest.get("patientId").getAsString() : null;
            String medicationId = jsonRequest.has("medicationId") ? jsonRequest.get("medicationId").getAsString() : null;
            String dateStr = jsonRequest.has("date") ? jsonRequest.get("date").getAsString() : null;

            if (patientId == null || patientId.trim().isEmpty()) {
                PatientDAO patientDAO = new PatientDAO();
                patientId = patientDAO.getDemoPatientId();
            }

            if (patientId != null && medicationId != null && dateStr != null) {
                Date date = Date.valueOf(dateStr); // Expected format: yyyy-MM-dd
                MedicationLogDAO dao = new MedicationLogDAO();
                boolean success = dao.toggleMedicationStatus(patientId, medicationId, date);
                
                if (success) {
                    responseData.put("status", "success");
                    responseData.put("message", "Medication status toggled successfully.");
                } else {
                    responseData.put("status", "error");
                    responseData.put("message", "Failed to toggle medication status.");
                }
            } else {
                responseData.put("status", "error");
                responseData.put("message", "Missing required parameters.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseData.put("status", "error");
            responseData.put("message", e.getMessage());
        } finally {
            out.print(gson.toJson(responseData));
            out.flush();
        }
    }
}
