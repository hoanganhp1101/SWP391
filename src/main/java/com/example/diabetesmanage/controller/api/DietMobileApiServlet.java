package com.example.diabetesmanage.controller.api;

import com.example.diabetesmanage.dao.DietPlanDAO;
import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.MasterFoodDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.DietPlan;
import com.example.diabetesmanage.model.DietPlanDetail;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.MasterFood;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.service.GeminiService;
import com.example.diabetesmanage.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet(name = "DietMobileApiServlet", urlPatterns = {"/api/mobile/diet", "/api/mobile/diet/generate"})
public class DietMobileApiServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        setupResponse(response);
        PrintWriter out = response.getWriter();
        Gson gson = GsonUtil.getGson();
        Map<String, Object> responseData = new HashMap<>();

        try {
            String patientId = request.getParameter("patientId");
            if (patientId == null || patientId.trim().isEmpty()) {
                responseData.put("status", "error");
                responseData.put("message", "Thiếu patientId");
                out.print(gson.toJson(responseData));
                return;
            }

            DietPlanDAO dietPlanDAO = new DietPlanDAO();
            DietPlan todayPlan = dietPlanDAO.getTodayDietPlan(patientId);

            if (todayPlan != null) {
                responseData.put("status", "success");
                responseData.put("dietPlan", mapDietPlanForMobile(todayPlan));
            } else {
                responseData.put("status", "error");
                responseData.put("message", "Chưa có thực đơn nào cho hôm nay");
            }
            out.print(gson.toJson(responseData));
        } catch (Exception e) {
            e.printStackTrace();
            responseData.put("status", "error");
            responseData.put("message", "Lỗi máy chủ: " + e.getMessage());
            out.print(gson.toJson(responseData));
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        setupResponse(response);
        PrintWriter out = response.getWriter();
        Gson gson = GsonUtil.getGson();
        Map<String, Object> responseData = new HashMap<>();

        try {
            // Read JSON body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);
            if (jsonRequest == null || !jsonRequest.has("patientId")) {
                responseData.put("status", "error");
                responseData.put("message", "Thiếu patientId");
                out.print(gson.toJson(responseData));
                return;
            }
            
            String patientId = jsonRequest.get("patientId").getAsString();
            PatientDAO patientDAO = new PatientDAO();
            Patient patient = patientDAO.getPatientById(patientId);
            
            if (patient == null) {
                responseData.put("status", "error");
                responseData.put("message", "Bệnh nhân không tồn tại");
                out.print(gson.toJson(responseData));
                return;
            }

            HealthRecordDAO hrDAO = new HealthRecordDAO();
            HealthRecord record = hrDAO.getLatestComprehensiveRecord(patientId);
            
            MasterFoodDAO foodDAO = new MasterFoodDAO();
            List<MasterFood> foods = foodDAO.getAllFoods().stream()
                    .filter(MasterFood::isTrangThai)
                    .collect(Collectors.toList());
                    
            GeminiService geminiService = new GeminiService();
            String jsonResponse = geminiService.generateDailyDietPlan(patient, record, foods);
            
            Type listType = new TypeToken<List<DietPlanDetail>>(){}.getType();
            List<DietPlanDetail> details = gson.fromJson(jsonResponse, listType);
            
            if (details != null && !details.isEmpty()) {
                DietPlan plan = new DietPlan();
                plan.setId(UUID.randomUUID().toString());
                plan.setPatientId(patientId);
                plan.setDoctorId("AI_SYSTEM"); 
                plan.setGhiChu("Thực đơn được tạo tự động bởi AI.");
                
                List<DietPlanDetail> validDetails = new ArrayList<>();
                for (DietPlanDetail d : details) {
                    d.setId(UUID.randomUUID().toString());
                    d.setDietPlanId(plan.getId());
                    validDetails.add(d);
                }
                plan.setChiTietThucPham(validDetails);
                
                DietPlanDAO planDAO = new DietPlanDAO();
                
                // Remove old AI diet plans today
                try (java.sql.Connection conn = com.example.diabetesmanage.context.DBContext.getConnection();
                     java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM diet_plans WHERE patient_id = ? AND DATE(ngay_tao) = CURDATE() AND doctor_id = 'AI_SYSTEM'")) {
                    ps.setString(1, patientId);
                    ps.executeUpdate();
                } catch (Exception e) {}
                
                planDAO.saveDietPlan(plan);
                
                DietPlan savedPlan = planDAO.getTodayDietPlan(patientId);
                
                responseData.put("status", "success");
                responseData.put("dietPlan", mapDietPlanForMobile(savedPlan != null ? savedPlan : plan));
            } else {
                responseData.put("status", "error");
                responseData.put("message", "AI không thể tạo thực đơn lúc này.");
            }
            out.print(gson.toJson(responseData));
        } catch (Exception e) {
            e.printStackTrace();
            responseData.put("status", "error");
            responseData.put("message", "Lỗi máy chủ: " + e.getMessage());
            out.print(gson.toJson(responseData));
        } finally {
            out.flush();
        }
    }

    private void setupResponse(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }

    private Map<String, Object> mapDietPlanForMobile(DietPlan plan) {
        Map<String, Object> dietData = new HashMap<>();
        dietData.put("id", plan.getId());
        dietData.put("luuYKhac", plan.getGhiChu());

        StringBuilder bs = new StringBuilder();
        StringBuilder bt = new StringBuilder();
        StringBuilder bto = new StringBuilder();
        StringBuilder bp = new StringBuilder();
        
        double totalCalo = 0;
        double totalCarbs = 0;

        if (plan.getChiTietThucPham() != null) {
            for (DietPlanDetail d : plan.getChiTietThucPham()) {
                MasterFood food = d.getThucPhamGoc();
                String mealStr = "";
                if (food != null) {
                    mealStr = "• " + food.getTenThucPham() + " (" + food.getDonViKhauPhan() + ") - " + d.getGhiChu();
                    if (food.getCaloKcal() != null) totalCalo += food.getCaloKcal();
                    totalCarbs += food.getCarbsG();
                } else {
                    mealStr = "• Món ăn - " + d.getGhiChu();
                }
                
                String buaAn = d.getBuaAn() != null ? d.getBuaAn().toLowerCase() : "";
                if (buaAn.contains("sáng") || buaAn.contains("sang")) { bs.append(mealStr).append("\n"); }
                else if (buaAn.contains("trưa") || buaAn.contains("trua")) { bt.append(mealStr).append("\n"); }
                else if (buaAn.contains("tối") || buaAn.contains("toi")) { bto.append(mealStr).append("\n"); }
                else if (buaAn.contains("phụ") || buaAn.contains("phu")) { bp.append(mealStr).append("\n"); }
                else { bs.append(mealStr).append("\n"); } // fallback
            }
        }

        dietData.put("buasAng", bs.toString().trim());
        dietData.put("buasTrua", bt.toString().trim());
        dietData.put("buasToi", bto.toString().trim());
        dietData.put("buasPhu", bp.toString().trim());
        
        dietData.put("mucTieuCalo", Math.round(totalCalo));
        dietData.put("tongCarbs", Math.round(totalCarbs));

        return dietData;
    }
}
