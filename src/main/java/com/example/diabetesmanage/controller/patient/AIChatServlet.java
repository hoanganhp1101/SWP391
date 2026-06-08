package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.service.GeminiService;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

/**
 * Servlet xử lý AJAX request từ chatbot AI trên dashboard.
 * Nhận tin nhắn → Build context bệnh nhân → Gọi Gemini → Trả JSON response.
 */
@WebServlet(name = "AIChatServlet", urlPatterns = {"/ai-chat"})
public class AIChatServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        String userMessage = request.getParameter("message");
        JsonObject jsonResponse = new JsonObject();

        if (userMessage == null || userMessage.trim().isEmpty()) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("reply", "Vui lòng nhập câu hỏi.");
            writeResponse(response, jsonResponse);
            return;
        }

        try {
            // Build context từ dữ liệu bệnh nhân hiện tại
            String patientContext = buildPatientContext();

            // Gọi AI
            GeminiService geminiService = new GeminiService();
            String aiReply = geminiService.chat(userMessage.trim(), patientContext);

            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("reply", aiReply);

        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("reply", "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.");
        }

        writeResponse(response, jsonResponse);
    }

    /**
     * Build context bệnh nhân để AI có thêm thông tin khi trả lời.
     */
    private String buildPatientContext() {
        StringBuilder ctx = new StringBuilder();
        try {
            PatientDAO patientDAO = new PatientDAO();
            String patientId = patientDAO.getDemoPatientId();
            if (patientId != null) {
                Patient patient = patientDAO.getPatientById(patientId);
                if (patient != null) {
                    ctx.append("Loại tiểu đường: ").append(patient.getLoaiTieuDuong() != null ? patient.getLoaiTieuDuong() : "Type 2").append("\n");
                    if (patient.getTienSuBenh() != null) {
                        ctx.append("Tiền sử bệnh: ").append(patient.getTienSuBenh()).append("\n");
                    }
                }

                HealthRecordDAO recordDAO = new HealthRecordDAO();
                HealthRecord latest = recordDAO.getLatestHealthRecord(patientId);
                if (latest != null && latest.getDuongHuyetMgdl() != null) {
                    ctx.append("Đường huyết gần nhất: ").append(latest.getDuongHuyetMgdl()).append(" mg/dL\n");
                }

                HealthRecord bp = recordDAO.getLatestBloodPressureRecord(patientId);
                if (bp != null && bp.getHuyetApTamThu() != null) {
                    ctx.append("Huyết áp gần nhất: ").append(bp.getHuyetApTamThu()).append("/").append(bp.getHuyetApTamTruong()).append(" mmHg\n");
                }
            }
        } catch (Exception e) {
            // Context building failed — continue without context
            System.err.println("[AIChatServlet] Error building context: " + e.getMessage());
        }
        return ctx.toString();
    }

    private void writeResponse(HttpServletResponse response, JsonObject json) throws IOException {
        PrintWriter out = response.getWriter();
        out.print(json.toString());
        out.flush();
    }
}
