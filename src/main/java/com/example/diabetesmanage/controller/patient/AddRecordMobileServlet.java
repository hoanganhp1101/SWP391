package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.AIAnalysisDAO;
import com.example.diabetesmanage.dao.AlertDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.AIAnalysis;
import com.example.diabetesmanage.model.Alert;
import com.example.diabetesmanage.service.GeminiService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.UUID;
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
                patientId = new PatientDAO().getDemoPatientId();
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
                    double dynamicRiskScore = 0.0;
                    boolean isRedFlag = false;
                    
                    if (record.getDuongHuyetMgdl() != null) {
                        double glucoseMmol = record.getDuongHuyetMgdl() / 18.0;
                        String td = record.getThoiDiemDoDuong() != null ? record.getThoiDiemDoDuong() : "";
                        
                        if (glucoseMmol < 3.9) {
                            isRedFlag = true;
                        } else if (glucoseMmol > 16.7) {
                            isRedFlag = true;
                        } else {
                            if ("luc_doi".equals(td) || "Lúc đói (Sáng sớm)".equals(td) || td.contains("đói")) {
                                if (glucoseMmol >= 7.3 && glucoseMmol <= 13.0) dynamicRiskScore += 15.0;
                                else if (glucoseMmol > 13.0 && glucoseMmol <= 16.7) dynamicRiskScore += 30.0;
                            } else {
                                if (glucoseMmol >= 10.1 && glucoseMmol <= 15.0) dynamicRiskScore += 15.0;
                                else if (glucoseMmol > 15.0 && glucoseMmol <= 16.7) dynamicRiskScore += 30.0;
                            }
                        }
                    }
                    
                    Integer sysBP = record.getHuyetApTamThu();
                    Integer diaBP = record.getHuyetApTamTruong();
                    if (sysBP != null || diaBP != null) {
                        int sys = (sysBP != null) ? sysBP : 0;
                        int dia = (diaBP != null) ? diaBP : 0;
                        
                        if (sys >= 160 || dia >= 100) {
                            isRedFlag = true;
                        } else if ((sys >= 140 && sys < 160) || (dia >= 90 && dia < 100)) {
                            dynamicRiskScore += 15.0;
                        }
                    }
                    
                    double totalRisk = analysis.getDiemNguyCo() + dynamicRiskScore;
                    if (totalRisk > 100.0) totalRisk = 100.0;
                    analysis.setDiemNguyCo(totalRisk);
                    
                    if (isRedFlag) {
                        analysis.setMucCanhBao("nguy_hiem");
                        analysis.setPhanTichChiTiet("🚨 [CẤP CỨU RED FLAG]: Chỉ số của bạn rơi vào mức NGUY HIỂM. Vui lòng liên hệ y tế ngay lập tức!\n\n" + analysis.getPhanTichChiTiet());
                    } else if (totalRisk >= 80.0) {
                        analysis.setMucCanhBao("nguy_hiem");
                    } else if (totalRisk >= 50.0) {
                        analysis.setMucCanhBao("cao");
                    } else if (totalRisk >= 20.0) {
                        analysis.setMucCanhBao("trung_binh");
                    } else {
                        analysis.setMucCanhBao("an_toan");
                    }

                    AIAnalysisDAO aiDAO = new AIAnalysisDAO();
                    aiDAO.insertAnalysis(analysis);
                    
                    String mucCanhBao = analysis.getMucCanhBao();
                    if ("cao".equals(mucCanhBao) || "nguy_hiem".equals(mucCanhBao)) {
                        Alert alert = new Alert();
                        alert.setId(UUID.randomUUID().toString());
                        alert.setPatientId(patientId);
                        alert.setAiAnalysisId(analysis.getId());
                        
                        String loaiCanhBao = determineAlertType(record);
                        alert.setLoaiCanhBao(loaiCanhBao);
                        alert.setMucDo(mucCanhBao);
                        
                        if (isRedFlag) {
                            alert.setTieuDe("🚨 [RED FLAG] CẢNH BÁO Y TẾ KHẨN CẤP");
                        } else {
                            alert.setTieuDe("⚠️ AI phát hiện chỉ số bất thường");
                        }
                        alert.setNoiDung(analysis.getPhanTichChiTiet());
                        
                        AlertDAO alertDAO = new AlertDAO();
                        alertDAO.insertAlert(alert);
                    }
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

    private String determineAlertType(HealthRecord record) {
        if (record.getDuongHuyetMgdl() != null && record.getDuongHuyetMgdl() > 180) {
            return "duong_huyet_cao";
        }
        if (record.getHuyetApTamThu() != null && record.getHuyetApTamThu() >= 140) {
            return "xu_huong_tang";
        }
        if (record.getDuongHuyetMgdl() != null) {
            return "duong_huyet_cao";
        }
        return "xu_huong_tang";
    }
}
