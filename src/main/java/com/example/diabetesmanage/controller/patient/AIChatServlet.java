package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.service.HealthChatResponse;
import com.example.diabetesmanage.service.HealthChatService;
import com.example.diabetesmanage.service.PatientHealthContext;
import com.example.diabetesmanage.util.PatientPortalAuth;

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
 * Có kiểm soát phạm vi sức khỏe trước khi gọi Gemini.
 */
@WebServlet(name = "AIChatServlet", urlPatterns = {"/ai-chat"})
public class AIChatServlet extends HttpServlet {

    private static final int MAX_MESSAGE_LENGTH = 1000;

    private final HealthChatService healthChatService = new HealthChatService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }

        String userMessage = request.getParameter("message");
        JsonObject jsonResponse = new JsonObject();

        if (userMessage == null || userMessage.trim().isEmpty()) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("status", "blocked");
            jsonResponse.addProperty("reply", "Vui lòng nhập câu hỏi.");
            writeResponse(response, jsonResponse);
            return;
        }

        if (userMessage.length() > MAX_MESSAGE_LENGTH) {
            userMessage = userMessage.substring(0, MAX_MESSAGE_LENGTH);
        }

        try {
            PatientHealthContext context = buildMinimalContext(patientId);
            HealthChatResponse chatResult = healthChatService.process(userMessage.trim(), context);

            jsonResponse.addProperty("success", chatResult.isSuccess());
            jsonResponse.addProperty("status", chatResult.getStatusCode());
            jsonResponse.addProperty("reply", chatResult.getReply());

        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("reply", "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.");
        }

        writeResponse(response, jsonResponse);
    }

    /**
     * Chỉ lấy dữ liệu y khoa/lối sống tối thiểu từ SQL — không gửi tên, email, SĐT, địa chỉ cho AI.
     */
    private PatientHealthContext buildMinimalContext(String patientId) {
        PatientHealthContext ctx = new PatientHealthContext();
        try {
            PatientDAO patientDAO = new PatientDAO();
            Patient patient = patientDAO.getPatientById(patientId);
            if (patient != null) {
                ctx.setLoaiTieuDuong(patient.getLoaiTieuDuong());
                if (patient.getTienSuBenh() != null) {
                    ctx.setTienSuBenhTomTat(patient.getTienSuBenh());
                }
                if (patient.getDiUng() != null) {
                    ctx.setDiUngTomTat(patient.getDiUng());
                }
                if (patient.getChieuCaoCm() != null) {
                    ctx.setChieuCaoCm(patient.getChieuCaoCm());
                }
            }

            HealthRecordDAO recordDAO = new HealthRecordDAO();
            HealthRecord latest = recordDAO.getLatestHealthRecord(patientId);
            if (latest != null) {
                if (latest.getDuongHuyetMgdl() != null) {
                    ctx.setDuongHuyetMgdl(latest.getDuongHuyetMgdl());
                }
                if (latest.getCanNangKg() != null) {
                    ctx.setCanNangKg(latest.getCanNangKg());
                }
                if (latest.getBmi() != null) {
                    ctx.setBmi(latest.getBmi());
                }
                if (latest.getSoGioNgu() != null) {
                    ctx.setSoGioNgu(latest.getSoGioNgu());
                }
                if (latest.getSoBuocChan() != null) {
                    ctx.setSoBuocChan(latest.getSoBuocChan());
                }
                if (latest.getCarbsG() != null) {
                    ctx.setCarbsGGanNhat(latest.getCarbsG());
                }
            }

            HealthRecord comprehensive = recordDAO.getLatestComprehensiveRecord(patientId);
            if (comprehensive != null) {
                if (comprehensive.getHba1cPercent() != null) {
                    ctx.setHba1cPercent(comprehensive.getHba1cPercent());
                }
                if (ctx.getCanNangKg() == null && comprehensive.getCanNangKg() != null) {
                    ctx.setCanNangKg(comprehensive.getCanNangKg());
                }
                if (ctx.getBmi() == null && comprehensive.getBmi() != null) {
                    ctx.setBmi(comprehensive.getBmi());
                }
                if (ctx.getSoGioNgu() == null && comprehensive.getSoGioNgu() != null) {
                    ctx.setSoGioNgu(comprehensive.getSoGioNgu());
                }
                if (ctx.getSoBuocChan() == null && comprehensive.getSoBuocChan() != null) {
                    ctx.setSoBuocChan(comprehensive.getSoBuocChan());
                }
                if (ctx.getCarbsGGanNhat() == null && comprehensive.getCarbsG() != null) {
                    ctx.setCarbsGGanNhat(comprehensive.getCarbsG());
                }
            }

            HealthRecord bp = recordDAO.getLatestBloodPressureRecord(patientId);
            if (bp != null && bp.getHuyetApTamThu() != null) {
                ctx.setHuyetApTamThu(bp.getHuyetApTamThu());
                ctx.setHuyetApTamTruong(bp.getHuyetApTamTruong());
            }
        } catch (Exception e) {
            System.err.println("[AIChatServlet] Error building context: " + e.getMessage());
        }
        return ctx;
    }

    private void writeResponse(HttpServletResponse response, JsonObject json) throws IOException {
        PrintWriter out = response.getWriter();
        out.print(json.toString());
        out.flush();
    }
}
