package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.AIAnalysisDAO;
import com.example.diabetesmanage.dao.AlertDAO;
import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.AIAnalysis;
import com.example.diabetesmanage.model.Alert;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
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
import java.util.UUID;

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
    private final Gson gson = new GsonBuilder().create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/api/iot/history".equals(path)) {
            String patientId = PatientPortalAuth.requirePatientId(request, response);
            if (patientId == null) {
                return;
            }
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            boolean iotOnly = !"all".equalsIgnoreCase(request.getParameter("source"));
            writeJson(response, Map.of(
                    "status", "success",
                    "history", buildHistoryRows(patientId, iotOnly)
            ));
            return;
        }

        if ("/api/iot/measure".equals(path) || "/api/iot/save".equals(path)) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Dùng POST.");
            return;
        }

        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }

        Patient patientInfo = new PatientDAO().getPatientById(patientId);
        request.setAttribute("patientInfo", patientInfo);
        request.setAttribute("iotHistory", buildHistoryRows(patientId, true));
        request.getRequestDispatcher("/WEB-INF/views/patient/patient-iot-simulator.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }

        String path = request.getServletPath();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if ("/api/iot/measure".equals(path)) {
            handleMeasure(request, response);
            return;
        }
        if ("/api/iot/save".equals(path)) {
            handleSave(patientId, request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        writeJson(response, Map.of("status", "error", "message", "Endpoint không tồn tại"));
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
        JsonObject body = readJson(request);
        Map<String, Object> result = new HashMap<>();

        try {
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

            if (record.getDuongHuyetMgdl() == null
                    && record.getNhipTim() == null
                    && record.getHuyetApTamThu() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("status", "error");
                result.put("message", "Chưa có chỉ số để lưu. Hãy đo trước.");
                writeJson(response, result);
                return;
            }

            healthRecordDAO.insertHealthRecord(record);

            AIAnalysis analysis = null;
            try {
                Patient patient = new PatientDAO().getPatientById(patientId);
                analysis = new GeminiService().analyzeHealthData(record, patient);
                if (analysis != null) {
                    new AIAnalysisDAO().insertAnalysis(analysis);
                    String muc = analysis.getMucCanhBao();
                    if ("cao".equals(muc) || "nguy_hiem".equals(muc)) {
                        Alert alert = new Alert();
                        alert.setId(UUID.randomUUID().toString());
                        alert.setPatientId(patientId);
                        alert.setAiAnalysisId(analysis.getId());
                        alert.setLoaiCanhBao(resolveAlertType(record));
                        alert.setMucDo(muc);
                        alert.setTieuDe("AI phát hiện chỉ số bất thường từ IoT mô phỏng");
                        alert.setNoiDung(analysis.getPhanTichChiTiet());
                        new AlertDAO().insertAlert(alert);
                    }
                }
            } catch (Exception aiEx) {
                System.err.println("[IotSimulatorServlet] AI analysis skipped: " + aiEx.getMessage());
            }

            result.put("status", "success");
            result.put("message", "Đã lưu chỉ số IoT vào hồ sơ sức khỏe");
            result.put("history", buildHistoryRows(patientId, true));
            if (analysis != null) {
                result.put("analysis", analysis);
            }
            writeJson(response, result);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("status", "error");
            result.put("message", e.getMessage() != null ? e.getMessage() : "Lỗi lưu chỉ số");
            writeJson(response, result);
        }
    }

    private List<Map<String, Object>> buildHistoryRows(String patientId, boolean iotOnly) {
        List<HealthRecord> records = healthRecordDAO.getMeasurementHistory(patientId, 30, iotOnly);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (HealthRecord hr : records) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", hr.getId());
            row.put("measuredAt", hr.getThoiGianDo() != null ? hr.getThoiGianDo().format(HISTORY_TIME) : "--");
            row.put("duongHuyet", hr.getDuongHuyetMgdl());
            row.put("huyetApTamThu", hr.getHuyetApTamThu());
            row.put("huyetApTamTruong", hr.getHuyetApTamTruong());
            row.put("nhipTim", hr.getNhipTim());
            row.put("thoiDiemDoDuong", labelTiming(hr.getThoiDiemDoDuong()));
            row.put("source", isIotNote(hr.getGhiChu()) ? "IoT" : "Thủ công");
            row.put("ghiChu", hr.getGhiChu());
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

    private void writeJson(HttpServletResponse response, Map<String, Object> payload) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(payload));
            out.flush();
        }
    }

    private static String resolveAlertType(HealthRecord record) {
        if (record.getDuongHuyetMgdl() != null && record.getDuongHuyetMgdl() > 180) {
            return "duong_huyet_cao";
        }
        if (record.getHuyetApTamThu() != null && record.getHuyetApTamThu() >= 140) {
            return "xu_huong_tang";
        }
        return "xu_huong_tang";
    }
}
