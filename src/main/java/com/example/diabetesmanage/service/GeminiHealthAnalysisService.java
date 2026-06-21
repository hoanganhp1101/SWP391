package com.example.diabetesmanage.service;

import com.example.diabetesmanage.config.GeminiConfig;
import com.example.diabetesmanage.model.PatientHealthSnapshot;
import com.example.diabetesmanage.service.gemini.GeminiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeminiHealthAnalysisService {

    private final GeminiClient geminiClient;
    private final GeminiConfig config;

    public GeminiHealthAnalysisService() {
        this.geminiClient = new GeminiClient();
        this.config = GeminiConfig.load();
    }

    public GeminiAnalysis enrichWithGemini(List<PatientHealthSnapshot> candidates) {

        GeminiAnalysis result = new GeminiAnalysis();
        result.setConfigured(config.isConfigured());
        result.setConfigInfo(
                "Nguồn key: " + config.getConfigSource()
                        + ", file: " + (config.isPropertiesFileFound() ? "đã tìm thấy" : "không tìm thấy")
                        + ", key: " + config.getMaskedApiKey()
        );

        if (!config.isConfigured()) {
            if (!config.isPropertiesFileFound()) {
                result.setError(
                        "Không tìm thấy gemini.properties trong classpath. "
                                + "Tạo file tại src/main/resources/gemini.properties rồi Build/Rebuild project."
                );
            } else {
                result.setError("API key trống trong gemini.properties");
            }
            return result;
        }

        if (candidates.isEmpty()) {
            result.setError("Không có hồ sơ nguy hiểm để Gemini phân tích.");
            return result;
        }

        try {
            String prompt = buildPrompt(candidates);
            String jsonResponse = geminiClient.generateJsonResponse(prompt);
            parseResponse(jsonResponse, candidates, result);
            result.setUsed(true);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.toString();
            result.setError(message);
            System.err.println("Gemini phân tích thất bại: " + message);
            e.printStackTrace();
        }

        return result;
    }

    private String buildPrompt(List<PatientHealthSnapshot> candidates) {

        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là bác sĩ nội tiết chuyên về tiểu đường. ");
        sb.append("Phân tích các hồ sơ bệnh nhân nguy hiểm sau và trả về JSON thuần (không markdown).\n\n");
        sb.append("Tiêu chí nguy hiểm: đường huyết quá cao/thấp, HbA1c cao, huyết áp cao, BMI cao, ");
        sb.append("đường huyết tăng liên tục, insulin tăng nhưng đường huyết không cải thiện, ");
        sb.append("không theo dõi sức khỏe đều đặn.\n\n");
        sb.append("Schema JSON:\n");
        sb.append("{\n");
        sb.append("  \"overallSummary\": \"tóm tắt ngắn toàn hệ thống\",\n");
        sb.append("  \"insights\": [\"insight 1\", \"insight 2\"],\n");
        sb.append("  \"patients\": [\n");
        sb.append("    {\n");
        sb.append("      \"patientCode\": \"mã\",\n");
        sb.append("      \"riskLevel\": \"critical|high|medium\",\n");
        sb.append("      \"summary\": \"phân tích ngắn 1-2 câu bằng tiếng Việt\",\n");
        sb.append("      \"priorityScore\": 1-100\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n\n");
        sb.append("Dữ liệu bệnh nhân:\n");

        for (PatientHealthSnapshot snapshot : candidates) {
            sb.append("- Mã: ").append(snapshot.getPatientCode());
            sb.append(", Tên: ").append(snapshot.getPatientName());
            sb.append(", Loại tiểu đường: ").append(nullToDash(snapshot.getLoaiTieuDuong()));
            sb.append(", Điểm rủi ro: ").append(snapshot.getRiskScore());
            sb.append(", Lý do: ").append(String.join("; ", snapshot.getRiskReasons()));
            sb.append(", Số hồ sơ gần đây: ").append(snapshot.getRecentRecords().size());
            sb.append("\n");

            int limit = Math.min(5, snapshot.getRecentRecords().size());
            for (int i = 0; i < limit; i++) {
                var record = snapshot.getRecentRecords().get(i);
                sb.append("  + ")
                        .append(record.getThoiGianDo() != null ? record.getThoiGianDo().toLocalDate() : "?")
                        .append(": DH=").append(format(record.getDuongHuyetMgdl()))
                        .append(", HbA1c=").append(format(record.getHba1cPercent()))
                        .append(", HA=").append(formatBp(record))
                        .append(", BMI=").append(format(record.getBmi()))
                        .append(", Insulin=").append(formatInt(record.getLieuLuongInsulinUi()))
                        .append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private void parseResponse(
            String jsonResponse,
            List<PatientHealthSnapshot> candidates,
            GeminiAnalysis result) {

        JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

        if (root.has("overallSummary")) {
            result.setOverallSummary(root.get("overallSummary").getAsString());
        }

        if (root.has("insights")) {
            JsonArray insights = root.getAsJsonArray("insights");
            List<String> insightList = new ArrayList<>();
            for (JsonElement element : insights) {
                insightList.add(element.getAsString());
            }
            result.setInsights(insightList);
        }

        Map<String, PatientGeminiInsight> patientMap = new HashMap<>();
        if (root.has("patients")) {
            JsonArray patients = root.getAsJsonArray("patients");
            for (JsonElement element : patients) {
                JsonObject patient = element.getAsJsonObject();
                String code = patient.get("patientCode").getAsString();
                PatientGeminiInsight insight = new PatientGeminiInsight();
                insight.setSummary(patient.get("summary").getAsString());
                if (patient.has("riskLevel")) {
                    insight.setRiskLevel(patient.get("riskLevel").getAsString());
                }
                if (patient.has("priorityScore")) {
                    insight.setPriorityScore(patient.get("priorityScore").getAsInt());
                }
                patientMap.put(code, insight);
            }
        }

        result.setPatientInsights(patientMap);
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String format(Double value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String formatInt(Integer value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String formatBp(com.example.diabetesmanage.model.HealthRecord record) {
        if (record.getHuyetApTamThu() == null && record.getHuyetApTamTruong() == null) {
            return "-";
        }
        return (record.getHuyetApTamThu() != null ? record.getHuyetApTamThu() : "?")
                + "/"
                + (record.getHuyetApTamTruong() != null ? record.getHuyetApTamTruong() : "?");
    }

    public static class GeminiAnalysis {
        private boolean used;
        private boolean configured;
        private String error;
        private String configInfo;
        private String overallSummary;
        private List<String> insights = new ArrayList<>();
        private Map<String, PatientGeminiInsight> patientInsights = new HashMap<>();

        public boolean isUsed() {
            return used;
        }

        public void setUsed(boolean used) {
            this.used = used;
        }

        public boolean isConfigured() {
            return configured;
        }

        public void setConfigured(boolean configured) {
            this.configured = configured;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getConfigInfo() {
            return configInfo;
        }

        public void setConfigInfo(String configInfo) {
            this.configInfo = configInfo;
        }

        public String getOverallSummary() {
            return overallSummary;
        }

        public void setOverallSummary(String overallSummary) {
            this.overallSummary = overallSummary;
        }

        public List<String> getInsights() {
            return insights;
        }

        public void setInsights(List<String> insights) {
            this.insights = insights;
        }

        public Map<String, PatientGeminiInsight> getPatientInsights() {
            return patientInsights;
        }

        public void setPatientInsights(Map<String, PatientGeminiInsight> patientInsights) {
            this.patientInsights = patientInsights;
        }
    }

    public static class PatientGeminiInsight {
        private String summary;
        private String riskLevel;
        private int priorityScore;

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        public int getPriorityScore() {
            return priorityScore;
        }

        public void setPriorityScore(int priorityScore) {
            this.priorityScore = priorityScore;
        }
    }
}
