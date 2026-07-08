package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.service.medical.EncounterAiAnalysis;
import com.example.diabetesmanage.service.medical.EncounterAiAnalysisService;
import com.example.diabetesmanage.service.medical.EncounterCreateRequest;
import com.example.diabetesmanage.service.medical.MedicalEncounterCreateService;
import com.example.diabetesmanage.util.AuthContext;
import com.google.gson.Gson;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bước 1 (AJAX): "Phân tích AI". Validate dữ liệu, gọi Gemini và trả JSON.
 * KHÔNG ghi database. Kết quả AI được JSP hiển thị và đính vào form trước khi "Tiếp tục kê đơn".
 */
@WebServlet("/doctor/medical-encounter/analyze")
public class MedicalEncounterAnalyzeServlet extends HttpServlet {

    private final PatientDAO patientDAO = new PatientDAO();
    private final MedicalEncounterCreateService createService = new MedicalEncounterCreateService();
    private final EncounterAiAnalysisService aiService = new EncounterAiAnalysisService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        User doctor = AuthContext.getUser(request);
        if (!AuthContext.isDoctor(doctor)) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                    error(List.of("Chỉ bác sĩ mới được phép truy cập.")));
            return;
        }

        EncounterCreateRequest form;
        try {
            form = EncounterCreateRequest.fromRequest(request);
            createService.normalizeEndocrinePayload(request, form);
        } catch (NumberFormatException ex) {
            writeJson(response, HttpServletResponse.SC_OK,
                    error(List.of("Dữ liệu số không hợp lệ. Vui lòng kiểm tra lại các trường số.")));
            return;
        }

        List<String> errors = createService.validateStep1(form);
        if (!errors.isEmpty()) {
            writeJson(response, HttpServletResponse.SC_OK, error(errors));
            return;
        }

        Patient patient = null;
        String doctorId = doctor.getId() != null ? doctor.getId().toString() : null;
        if (doctorId != null) {
            patient = patientDAO.getPatientById(form.getPatientId(), doctorId);
        }
        if (patient == null) {
            writeJson(response, HttpServletResponse.SC_OK,
                    error(List.of("Không tìm thấy bệnh nhân hoặc bạn không có quyền truy cập.")));
            return;
        }

        EncounterAiAnalysis analysis = aiService.analyze(form, patient);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("used", analysis.isUsed());
        body.put("configured", analysis.isConfigured());
        body.put("error", analysis.getError());
        body.put("summaryText", analysis.buildSummaryText());

        Map<String, Object> ai = new LinkedHashMap<>();
        ai.put("riskLevel", analysis.getRiskLevel());
        ai.put("riskScore", analysis.getRiskScore());
        ai.put("possibleDisease", analysis.getPossibleDisease());
        ai.put("riskFactors", analysis.getRiskFactors());
        ai.put("recommendedTests", analysis.getRecommendedTests());
        ai.put("recommendations", analysis.getRecommendations());
        ai.put("shortExplanation", analysis.getShortExplanation());
        body.put("ai", ai);

        writeJson(response, HttpServletResponse.SC_OK, body);
    }

    private Map<String, Object> error(List<String> errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", false);
        body.put("errors", new ArrayList<>(errors));
        return body;
    }

    private void writeJson(HttpServletResponse response, int status, Map<String, Object> body)
            throws IOException {
        response.setStatus(status);
        try (PrintWriter out = response.getWriter()) {
            out.write(gson.toJson(body));
        }
    }
}
