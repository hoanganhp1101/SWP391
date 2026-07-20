package com.example.diabetesmanage;

import com.example.diabetesmanage.model.*;
import com.example.diabetesmanage.dao.*;
import com.example.diabetesmanage.service.GeminiService;

import java.util.List;
import java.util.stream.Collectors;

public class TestGemini {
    public static void main(String[] args) {
        try {
            System.out.println("Starting test...");
            PatientDAO patientDAO = new PatientDAO();
            String patientId = patientDAO.getDemoPatientId();
            System.out.println("Patient ID: " + patientId);
            
            if (patientId != null) {
                Patient patient = patientDAO.getPatientById(patientId);
                HealthRecordDAO hrDAO = new HealthRecordDAO();
                HealthRecord record = hrDAO.getLatestComprehensiveRecord(patientId);
                
                MasterFoodDAO foodDAO = new MasterFoodDAO();
                List<MasterFood> foods = foodDAO.getAllFoods().stream()
                        .filter(MasterFood::isTrangThai)
                        .collect(Collectors.toList());
                        
                System.out.println("Found " + foods.size() + " active foods.");
                
                GeminiService geminiService = new GeminiService();
                System.out.println("Calling geminiService.generateDailyDietPlan...");
                String jsonResponse = geminiService.generateDailyDietPlan(patient, record, foods);
                System.out.println("Response:\n" + jsonResponse);
            } else {
                System.out.println("No demo patient found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
