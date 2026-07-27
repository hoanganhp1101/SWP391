package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.AIAnalysis;
import com.example.diabetesmanage.service.ClinicalRiskService;
import com.example.diabetesmanage.service.GeminiService;
import com.example.diabetesmanage.util.PatientPortalAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "AddRecordMobileServlet", urlPatterns = {"/api/mobile/add-record"})
public class AddRecordMobileServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        PrintWriter out = response.getWriter();
        Gson gson = new GsonBuilder().create();
        Map<String, Object> responseData = new HashMap<>();

        try {
            // Read JSON body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);
            
            String patientId = jsonObject.has("patientId") ? jsonObject.get("patientId").getAsString() : null;
            if (patientId == null || patientId.trim().isEmpty()) {
                patientId = PatientPortalAuth.requirePatientId(request, response);
                if (patientId == null) {
                    return;
                }
            }

            HealthRecord record = new HealthRecord();
            record.setPatientId(patientId);

            if (jsonObject.has("duongHuyet") && !jsonObject.get("duongHuyet").isJsonNull()) {
                record.setDuongHuyetMgdl(jsonObject.get("duongHuyet").getAsDouble());
            }
            if (jsonObject.has("nhipTim") && !jsonObject.get("nhipTim").isJsonNull()) {
                record.setNhipTim(jsonObject.get("nhipTim").getAsInt());
            }
            if (jsonObject.has("huyetApTamThu") && !jsonObject.get("huyetApTamThu").isJsonNull()) {
                record.setHuyetApTamThu(jsonObject.get("huyetApTamThu").getAsInt());
            }
            if (jsonObject.has("huyetApTamTruong") && !jsonObject.get("huyetApTamTruong").isJsonNull()) {
                record.setHuyetApTamTruong(jsonObject.get("huyetApTamTruong").getAsInt());
            }
            if (jsonObject.has("thoiDiemDoDuong") && !jsonObject.get("thoiDiemDoDuong").isJsonNull()) {
                record.setThoiDiemDoDuong(jsonObject.get("thoiDiemDoDuong").getAsString());
            }
            if (jsonObject.has("ghiChu") && !jsonObject.get("ghiChu").isJsonNull()) {
                record.setGhiChu(jsonObject.get("ghiChu").getAsString());
            }
            
            // Triệu chứng
            if (jsonObject.has("chestPain") && !jsonObject.get("chestPain").isJsonNull()) {
                record.setChestPain(jsonObject.get("chestPain").getAsInt());
            }
            if (jsonObject.has("dizziness") && !jsonObject.get("dizziness").isJsonNull()) {
                record.setDizziness(jsonObject.get("dizziness").getAsInt());
            }
            if (jsonObject.has("fatigue") && !jsonObject.get("fatigue").isJsonNull()) {
                record.setFatigue(jsonObject.get("fatigue").getAsInt());
            }

            HealthRecordDAO dao = new HealthRecordDAO();
            dao.insertHealthRecord(record);

            AIAnalysis analysis = null;
            try {
                PatientDAO patientDAO = new PatientDAO();
                Patient patient = patientDAO.getPatientById(patientId);
                
                GeminiService geminiService = new GeminiService();
                analysis = geminiService.analyzeHealthData(record, patient);
                
                if (analysis != null) {
                    // Pipeline chung: áp rule động, lưu ai_analysis, tạo alert nếu cần
                    ClinicalRiskService.applyRulesAndPersist(patientId, record, analysis);
                }
            } catch (Exception aiEx) {
                System.err.println("[AddRecordMobileServlet] AI analysis failed: " + aiEx.getMessage());
                aiEx.printStackTrace();
            }

            responseData.put("status", "success");
            responseData.put("message", "Thêm hồ sơ thành công");
            if (analysis != null) {
                responseData.put("analysis", analysis);
            }
            
            out.print(gson.toJson(responseData));
        } catch (Exception e) {
            e.printStackTrace();
            responseData.put("status", "error");
            responseData.put("message", e.getMessage());
            out.print(gson.toJson(responseData));
        } finally {
            out.flush();
        }
    }

}
