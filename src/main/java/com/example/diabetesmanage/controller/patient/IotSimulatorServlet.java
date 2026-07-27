package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.AIAnalysis;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.service.ClinicalRiskService;
import com.example.diabetesmanage.service.ClinicalRiskService.PersistResult;
import com.example.diabetesmanage.service.GeminiService;
import com.example.diabetesmanage.service.IotSimulatorService;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Trang mô phỏng IoT đo chỉ số + API measure/save/history.
 */
@WebServlet(name = "IotSimulatorServlet", urlPatterns = {
        "/patient-iot",
        "/api/iot/measure",
        "/api/iot/save",
        "/api/iot/history"
})
public class IotSimulatorServlet extends HttpServlet {

    private static final DateTimeFormatter HISTORY_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IotSimulatorService simulatorService = new IotSimulatorService();
    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/api/iot/history".equals(path)) {
            handleHistoryGet(request, response);
            return;
        }

        if ("/api/iot/measure".equals(path) || "/api/iot/save".equals(path)) {
            writeJsonError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "Dùng POST cho endpoint này.");
            return;
        }

        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }

        Patient patientInfo = patientDAO.getPatientById(patientId);
        request.setAttribute("patientInfo", patientInfo);
        request.setAttribute("iotHistory", buildHistoryRows(patientId, true));
        request.getRequestDispatcher("/WEB-INF/views/patient/patient-iot-simulator.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        try {
            if ("/api/iot/measure".equals(path)) {
                handleMeasure(request, response);
                return;
            }

            if ("/api/iot/save".equals(path)) {
                String patientId = PatientPortalAuth.requirePatientId(request, response);
                if (patientId == null) {
                    return;
                }
                handleSave(patientId, request, response);
                return;
            }

            writeJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại");
        } catch (Throwable t) {
            System.err.println("[IotSimulatorServlet] POST " + path + " failed: " + t.getMessage());
            t.printStackTrace();
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    t.getMessage() != null ? t.getMessage() : "Lỗi xử lý IoT API");
        }
    }

    private void handleHistoryGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }
        boolean iotOnly = !"all".equalsIgnoreCase(request.getParameter("source"));
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "success");
        payload.put("history", buildHistoryRows(patientId, iotOnly));
        writeJson(response, payload);
    }

    private void handleMeasure(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject body = readJson(request);
        String scenarioRaw = body.has("scenario") ? body.get("scenario").getAsString() : "RANDOM";
        String timing = body.has("thoiDiemDoDuong") ? body.get("thoiDiemDoDuong").getAsString() : "luc_doi";

        Map<String, Object> reading = simulatorService.measure(
                simulatorService.parseScenario(scenarioRaw), timing);

        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "success");
        payload.put("reading", reading);
        writeJson(response, payload);
    }

    private void handleSave(String patientId, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Patient patient = patientDAO.getPatientById(patientId);
        if (patient == null) {
            writeJsonError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Không tìm thấy hồ sơ bệnh nhân. Hãy đăng xuất và đăng nhập lại.");
            return;
        }

        JsonObject body = readJson(request);
        HealthRecord record = buildRecordFromJson(patientId, body);

        if (record.getDuongHuyetMgdl() == null
                && record.getNhipTim() == null
                && record.getHuyetApTamThu() == null) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Chưa có chỉ số để lưu. Hãy đo trước.");
            return;
        }

        if (!healthRecordDAO.insertHealthRecord(record)) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không ghi được vào bảng health_records. Kiểm tra MySQL và chạy lại newdb.sql.");
            return;
        }

        // IoT: dùng rule-based (nhanh, không gọi Gemini — tránh timeout HTTP 500)
        PersistResult persistResult = null;
        AIAnalysis analysis = new GeminiService().analyzeHealthDataRuleBased(record, patient);
        if (analysis != null) {
            persistResult = ClinicalRiskService.applyRulesAndPersist(patientId, record, analysis);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", buildSaveMessage(persistResult));
        result.put("recordId", record.getId());
        if (analysis != null) {
            result.put("analysis", toAnalysisSummary(analysis, persistResult));
        }
        writeJson(response, result);
    }

    private HealthRecord buildRecordFromJson(String patientId, JsonObject body) {
        HealthRecord record = new HealthRecord();
        record.setPatientId(patientId);

        if (body.has("duongHuyet") && !body.get("duongHuyet").isJsonNull()) {
            record.setDuongHuyetMgdl(body.get("duongHuyet").getAsDouble());
        }
        if (body.has("nhipTim") && !body.get("nhipTim").isJsonNull()) {
            record.setNhipTim(body.get("nhipTim").getAsInt());
        }
        if (body.has("huyetApTamThu") && !body.get("huyetApTamThu").isJsonNull()) {
            record.setHuyetApTamThu(body.get("huyetApTamThu").getAsInt());
        }
        if (body.has("huyetApTamTruong") && !body.get("huyetApTamTruong").isJsonNull()) {
            record.setHuyetApTamTruong(body.get("huyetApTamTruong").getAsInt());
        }
        if (body.has("thoiDiemDoDuong") && !body.get("thoiDiemDoDuong").isJsonNull()) {
            record.setThoiDiemDoDuong(body.get("thoiDiemDoDuong").getAsString());
        }

        String deviceNote = body.has("deviceId") && !body.get("deviceId").isJsonNull()
                ? body.get("deviceId").getAsString()
                : "SIM-UNKNOWN";
        record.setGhiChu("Nguồn: mô phỏng IoT (" + deviceNote + ")");
        User nhapBoi = new User();
        nhapBoi.setId(patientId);
        record.setNhapBoi(nhapBoi);
        return record;
    }

    private List<Map<String, Object>> buildHistoryRows(String patientId, boolean iotOnly) {
        List<HealthRecord> records = healthRecordDAO.getMeasurementHistory(patientId, 30, iotOnly);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (HealthRecord hr : records) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", hr.getId() != null ? hr.getId() : "");
            row.put("measuredAt", hr.getThoiGianDo() != null ? hr.getThoiGianDo().format(HISTORY_TIME) : "--");
            row.put("duongHuyet", hr.getDuongHuyetMgdl());
            row.put("huyetApTamThu", hr.getHuyetApTamThu());
            row.put("huyetApTamTruong", hr.getHuyetApTamTruong());
            row.put("nhipTim", hr.getNhipTim());
            row.put("thoiDiemDoDuong", labelTiming(hr.getThoiDiemDoDuong()));
            row.put("source", isIotNote(hr.getGhiChu()) ? "IoT" : "Thủ công");
            row.put("ghiChu", hr.getGhiChu() != null ? hr.getGhiChu() : "");
            rows.add(row);
        }
        return rows;
    }

    private static boolean isIotNote(String note) {
        return note != null && note.contains("mô phỏng IoT");
    }

    private static String labelTiming(String timing) {
        if (timing == null || timing.isBlank()) {
            return "--";
        }
        if ("luc_doi".equals(timing)) {
            return "Lúc đói";
        }
        if ("sau_an_1h".equals(timing)) {
            return "Sau ăn 1h";
        }
        if ("sau_an_2h".equals(timing)) {
            return "Sau ăn 2h";
        }
        if ("truoc_ngu".equals(timing)) {
            return "Trước ngủ";
        }
        return timing;
    }

    private JsonObject readJson(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        if (sb.length() == 0) {
            return new JsonObject();
        }
        JsonObject parsed = gson.fromJson(sb.toString(), JsonObject.class);
        return parsed != null ? parsed : new JsonObject();
    }

    private static String buildSaveMessage(PersistResult persistResult) {
        if (persistResult == null) {
            return "Đã lưu chỉ số IoT.";
        }
        if (persistResult.isAlertCreated()) {
            return "Đã lưu chỉ số IoT và cập nhật cảnh báo trên Tổng quan.";
        }
        if (persistResult.isAnalysisSaved()) {
            return "Đã lưu chỉ số IoT và cập nhật phân tích AI.";
        }
        return "Đã lưu chỉ số IoT vào hồ sơ sức khỏe.";
    }

    private static Map<String, Object> toAnalysisSummary(AIAnalysis analysis, PersistResult persistResult) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("diemNguyCo", analysis.getDiemNguyCo());
        summary.put("mucCanhBao", analysis.getMucCanhBao());
        summary.put("modelVersion", analysis.getModelVersion());
        if (persistResult != null) {
            summary.put("alertCreated", persistResult.isAlertCreated());
            summary.put("alertTitle", persistResult.getAlertTitle());
        }
        return summary;
    }

    private void writeJson(HttpServletResponse response, Map<String, Object> payload) throws IOException {
        response.setCharacterEncoding("UTF-8");
        if (!response.isCommitted()) {
            response.setContentType("application/json;charset=UTF-8");
        }
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(payload));
            out.flush();
        }
    }

    private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        if (!response.isCommitted()) {
            response.resetBuffer();
            response.setStatus(status);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "error");
        payload.put("message", message);
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(payload));
            out.flush();
        }
    }
}
