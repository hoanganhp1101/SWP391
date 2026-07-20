package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.AppointmentDAO;
import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.Appointment;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.util.PatientPortalAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "PatientMobileApiServlet", urlPatterns = {"/api/mobile/dashboard"})
public class PatientMobileApiServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Cấu hình response CORS để mobile app (đặc biệt là web app/emulator) có thể gọi được
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        
        PrintWriter out = response.getWriter();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        Map<String, Object> responseData = new HashMap<>();

        try {
            PatientDAO patientDAO = new PatientDAO();
            String patientId = request.getParameter("patientId");
            
            // Prefer client-supplied ID; otherwise resolve from session
            if (patientId == null || patientId.trim().isEmpty()) {
                patientId = PatientPortalAuth.requirePatientId(request, response);
                if (patientId == null) {
                    return;
                }
            }

            if (patientId != null) {
                // 1. Patient info
                Patient patientInfo = patientDAO.getPatientById(patientId);
                responseData.put("patient", patientInfo);

                HealthRecordDAO recordDAO = new HealthRecordDAO();
                
                // 2. Latest Vitals (Sử dụng getLatestComprehensiveRecord để lấy đầy đủ chỉ số mới nhất và Lab Results)
                HealthRecord comprehensiveRecord = recordDAO.getLatestComprehensiveRecord(patientId);
                
                Map<String, Object> vitals = new HashMap<>();
                if (comprehensiveRecord != null) {
                    vitals.put("glucose", comprehensiveRecord.getDuongHuyetMgdl());
                    vitals.put("hba1c", comprehensiveRecord.getHba1cPercent());
                    vitals.put("cholesterol", comprehensiveRecord.getCholesterolMmol());
                    vitals.put("triglyceride", comprehensiveRecord.getTriglycerideMmol());
                    vitals.put("heartRate", comprehensiveRecord.getNhipTim());
                    vitals.put("sys", comprehensiveRecord.getHuyetApTamThu());
                    vitals.put("dia", comprehensiveRecord.getHuyetApTamTruong());
                    vitals.put("bmi", comprehensiveRecord.getBmi());
                    vitals.put("weight", comprehensiveRecord.getCanNangKg());
                }
                responseData.put("latestVitals", vitals);

                // 3. Chart Data (all records)
                List<HealthRecord> allRecords = recordDAO.getAllRecordsForChart(patientId);
                responseData.put("chartData", allRecords);

                // 4. Appointments
                AppointmentDAO appointmentDAO = new AppointmentDAO();
                List<Appointment> appointments = appointmentDAO.getUpcomingAppointments(patientId);
                responseData.put("appointments", appointments);
                
                // 5. Treatment Plan (Phác đồ mới nhất)
                com.example.diabetesmanage.dao.PrescriptionDAO prescriptionDAO = new com.example.diabetesmanage.dao.PrescriptionDAO();
                com.example.diabetesmanage.model.Prescription latestPrescription = prescriptionDAO.getLatestPrescription(patientId);
                responseData.put("prescription", latestPrescription);

                // 6. Recent Alerts
                com.example.diabetesmanage.dao.AlertDAO alertDAO = new com.example.diabetesmanage.dao.AlertDAO();
                List<com.example.diabetesmanage.model.Alert> recentAlerts = alertDAO.getRecentAlerts(patientId);
                responseData.put("recentAlerts", recentAlerts);

                // 6.1. Latest AI Analysis
                com.example.diabetesmanage.dao.AIAnalysisDAO aiAnalysisDAO = new com.example.diabetesmanage.dao.AIAnalysisDAO();
                com.example.diabetesmanage.model.AIAnalysis latestAI = aiAnalysisDAO.getLatestAnalysis(patientId);
                
                if (latestAI == null && comprehensiveRecord != null && comprehensiveRecord.getThoiGianDo() != null) {
                    try {
                        com.example.diabetesmanage.service.GeminiService geminiService = new com.example.diabetesmanage.service.GeminiService();
                        latestAI = geminiService.analyzeHealthData(comprehensiveRecord, patientInfo);
                        if (latestAI != null && !latestAI.getModelVersion().startsWith("ERROR")) {
                            latestAI.setHealthRecordId(null); // aggregate comprehensive analysis
                            aiAnalysisDAO.insertAnalysis(latestAI);
                            
                            // If the warning level is high, also insert an Alert
                            String mucCanhBao = latestAI.getMucCanhBao();
                            if ("cao".equals(mucCanhBao) || "nguy_hiem".equals(mucCanhBao)) {
                                com.example.diabetesmanage.model.Alert alert = new com.example.diabetesmanage.model.Alert();
                                alert.setId(java.util.UUID.randomUUID().toString());
                                alert.setPatientId(patientId);
                                alert.setAiAnalysisId(latestAI.getId());
                                
                                String loaiCanhBao = "xu_huong_tang";
                                if (comprehensiveRecord.getDuongHuyetMgdl() != null && comprehensiveRecord.getDuongHuyetMgdl() > 180) {
                                    loaiCanhBao = "duong_huyet_cao";
                                }
                                alert.setLoaiCanhBao(loaiCanhBao);
                                alert.setMucDo(mucCanhBao);
                                
                                if ("nguy_hiem".equals(mucCanhBao)) {
                                    alert.setTieuDe("🚨 [RED FLAG] CẢNH BÁO Y TẾ KHẨN CẤP");
                                } else {
                                    alert.setTieuDe("⚠️ AI phát hiện chỉ số bất thường");
                                }
                                alert.setNoiDung(latestAI.getPhanTichChiTiet());
                                
                                alertDAO.insertAlert(alert);
                                
                                // Re-fetch recent alerts to include the new one
                                recentAlerts = alertDAO.getRecentAlerts(patientId);
                                responseData.put("recentAlerts", recentAlerts);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[PatientMobileApiServlet] Failed to run on-the-fly AI analysis: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                responseData.put("latestAIAnalysis", latestAI);

                // 7. Medication Checklist & AI Reminder
                com.example.diabetesmanage.dao.MedicationLogDAO medicationLogDAO = new com.example.diabetesmanage.dao.MedicationLogDAO();
                java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
                List<com.example.diabetesmanage.model.MedicationLog> medicationChecklist = medicationLogDAO.getChecklistByDate(patientId, today);
                responseData.put("medicationChecklist", medicationChecklist);

                com.example.diabetesmanage.service.GeminiService geminiService = new com.example.diabetesmanage.service.GeminiService();
                String medicationAiMessage = geminiService.generateMedicationReminder(patientInfo.getHoTen(), medicationChecklist);
                responseData.put("medicationAiMessage", medicationAiMessage != null ? medicationAiMessage : "");
                
                responseData.put("status", "success");
            } else {
                responseData.put("status", "error");
                responseData.put("message", "Patient ID not found.");
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
