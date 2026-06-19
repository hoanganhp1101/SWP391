package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.AlertDAO;
import com.example.diabetesmanage.dao.AIAnalysisDAO;
import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.AIAnalysis;
import com.example.diabetesmanage.model.Alert;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.service.GeminiService;

import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "LogDataServlet", urlPatterns = {"/logData"})
public class LogDataServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String patientId = request.getParameter("patientId");
        if (patientId == null || patientId.trim().isEmpty()) {
            PatientDAO patientDAO = new PatientDAO();
            patientId = patientDAO.getDemoPatientId();
        }

        String glucoseStr = request.getParameter("duong_huyet");
        String donViStr = request.getParameter("don_vi_duong_huyet");
        String nhipTimStr = request.getParameter("nhip_tim");
        String haThuStr = request.getParameter("huyet_ap_thu");
        String haTruongStr = request.getParameter("huyet_ap_truong");
        String thoiDiem = request.getParameter("thoi_diem");
        String chestPain = request.getParameter("chest_pain");
        String dizziness = request.getParameter("dizziness");
        String fatigue = request.getParameter("fatigue");

        HealthRecord record = new HealthRecord();
        record.setPatientId(patientId);
        record.setThoiDiemDoDuong(thoiDiem);
        record.setChestPain(chestPain != null ? 1 : 0);
        record.setDizziness(dizziness != null ? 1 : 0);
        record.setFatigue(fatigue != null ? 1 : 0);
        
        try {
            if (glucoseStr != null && !glucoseStr.trim().isEmpty()) {
                double glucose = Double.parseDouble(glucoseStr);
                if ("mmol/L".equals(donViStr)) {
                    glucose = glucose * 18.0;
                }
                record.setDuongHuyetMgdl(glucose);
            }
            if (nhipTimStr != null && !nhipTimStr.trim().isEmpty()) {
                record.setNhipTim(Integer.parseInt(nhipTimStr));
            }
            if (haThuStr != null && !haThuStr.trim().isEmpty()) {
                record.setHuyetApTamThu(Integer.parseInt(haThuStr));
            }
            if (haTruongStr != null && !haTruongStr.trim().isEmpty()) {
                record.setHuyetApTamTruong(Integer.parseInt(haTruongStr));
            }
            
            // 1. Lưu bản ghi sức khỏe
            HealthRecordDAO dao = new HealthRecordDAO();
            dao.insertHealthRecord(record);
            
            // 2. Gọi AI phân tích chỉ số sức khỏe
            try {
                PatientDAO patientDAO = new PatientDAO();
                Patient patient = patientDAO.getPatientById(patientId);
                
                GeminiService geminiService = new GeminiService();
                AIAnalysis analysis = geminiService.analyzeHealthData(record, patient);
                
                if (analysis != null) {
                    // --- APPLY DYNAMIC RISK RULES ---
                    double dynamicRiskScore = 0.0;
                    boolean isRedFlag = false;
                    
                    // 1. Kiểm tra Đường Huyết (Chuyển mg/dL thành mmol/L để áp dụng rule y khoa)
                    if (record.getDuongHuyetMgdl() != null) {
                        double glucoseMmol = record.getDuongHuyetMgdl() / 18.0;
                        String td = record.getThoiDiemDoDuong() != null ? record.getThoiDiemDoDuong() : "";
                        
                        if (glucoseMmol < 3.9) {
                            isRedFlag = true;
                        } else if (glucoseMmol > 16.7) {
                            isRedFlag = true;
                        } else {
                            if ("luc_doi".equals(td)) {
                                if (glucoseMmol >= 7.3 && glucoseMmol <= 13.0) dynamicRiskScore += 15.0;
                                else if (glucoseMmol > 13.0 && glucoseMmol <= 16.7) dynamicRiskScore += 30.0;
                            } else if (td.startsWith("sau_an")) {
                                if (glucoseMmol >= 10.1 && glucoseMmol <= 15.0) dynamicRiskScore += 15.0;
                                else if (glucoseMmol > 15.0 && glucoseMmol <= 16.7) dynamicRiskScore += 30.0;
                            }
                        }
                    }
                    
                    // 2. Kiểm tra Huyết áp
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
                    
                    // 3. Tổng hợp Total Risk = Base Risk (AI) + Dynamic Risk (Rules)
                    double totalRisk = analysis.getDiemNguyCo() + dynamicRiskScore;
                    if (totalRisk > 100.0) totalRisk = 100.0;
                    analysis.setDiemNguyCo(totalRisk);
                    
                    // 4. Quyết định Mức Cảnh Báo Cuối Cùng
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

                    // Lưu kết quả phân tích AI vào database
                    AIAnalysisDAO aiDAO = new AIAnalysisDAO();
                    aiDAO.insertAnalysis(analysis);
                    
                    // 3. Nếu nguy cơ CAO hoặc NGUY HIỂM → Tạo Alert tự động
                    String mucCanhBao = analysis.getMucCanhBao();
                    if ("cao".equals(mucCanhBao) || "nguy_hiem".equals(mucCanhBao)) {
                        Alert alert = new Alert();
                        alert.setId(UUID.randomUUID().toString());
                        alert.setPatientId(patientId);
                        alert.setAiAnalysisId(analysis.getId());
                        
                        // Xác định loại cảnh báo dựa trên dữ liệu
                        String loaiCanhBao = determineAlertType(record);
                        alert.setLoaiCanhBao(loaiCanhBao);
                        alert.setMucDo(mucCanhBao);
                        
                        // Tiêu đề và nội dung
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
                // AI analysis thất bại không ảnh hưởng đến việc lưu dữ liệu
                System.err.println("[LogDataServlet] AI analysis failed: " + aiEx.getMessage());
                aiEx.printStackTrace();
            }
            
            response.sendRedirect(request.getContextPath() + "/patient-dashboard");
            
        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/patient-dashboard?error=InvalidInput");
        }
    }
    
    /**
     * Xác định loại cảnh báo dựa trên chỉ số bất thường nhất.
     */
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
