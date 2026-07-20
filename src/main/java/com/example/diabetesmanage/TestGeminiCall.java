package com.example.diabetesmanage;

import com.example.diabetesmanage.model.*;
import com.example.diabetesmanage.dao.*;
import com.example.diabetesmanage.service.GeminiService;

import java.util.List;
import java.util.stream.Collectors;
import java.lang.reflect.Method;

public class TestGeminiCall {
    public static void main(String[] args) {
        try {
            PatientDAO patientDAO = new PatientDAO();
            String patientId = patientDAO.getDemoPatientId();
            
            if (patientId != null) {
                Patient patient = patientDAO.getPatientById(patientId);
                HealthRecordDAO hrDAO = new HealthRecordDAO();
                HealthRecord record = hrDAO.getLatestComprehensiveRecord(patientId);
                
                MasterFoodDAO foodDAO = new MasterFoodDAO();
                List<MasterFood> foods = foodDAO.getAllFoods().stream()
                        .filter(MasterFood::isTrangThai)
                        .collect(Collectors.toList());
                        
                System.out.println("Calling generateDailyDietPlan with " + foods.size() + " foods...");
                
                GeminiService geminiService = new GeminiService();
                String jsonResponse = geminiService.generateDailyDietPlan(patient, record, foods);
                System.out.println("\n--- FINAL JSON RESULT ---");
                System.out.println(jsonResponse);
                
                // Parse and save
                com.google.gson.Gson gson = new com.google.gson.Gson();
                java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<DietPlanDetail>>(){}.getType();
                List<DietPlanDetail> details = gson.fromJson(jsonResponse, listType);
                
                if (details != null && !details.isEmpty()) {
                    DietPlan plan = new DietPlan();
                    plan.setId(java.util.UUID.randomUUID().toString());
                    plan.setPatientId(patientId);
                    plan.setDoctorId("AI_SYSTEM");
                    plan.setGhiChu("Thực đơn được tạo tự động bởi AI (Fix)");
                    
                    List<DietPlanDetail> validDetails = new java.util.ArrayList<>();
                    for (DietPlanDetail d : details) {
                        d.setId(java.util.UUID.randomUUID().toString());
                        d.setDietPlanId(plan.getId());
                        validDetails.add(d);
                    }
                    plan.setChiTietThucPham(validDetails);
                    
                    DietPlanDAO planDAO = new DietPlanDAO();
                    
                    try (java.sql.Connection conn = com.example.diabetesmanage.util.DBContext.getConnection();
                         java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM diet_plans WHERE patient_id = ? AND DATE(ngay_tao) = CURDATE() AND doctor_id = 'AI_SYSTEM'")) {
                        ps.setString(1, patientId);
                        ps.executeUpdate();
                    } catch (Exception e) {}
                    
                    planDAO.saveDietPlan(plan);
                    System.out.println("===> SAVED NEW DIET PLAN TO DATABASE SUCCESSFULLY! <===");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
