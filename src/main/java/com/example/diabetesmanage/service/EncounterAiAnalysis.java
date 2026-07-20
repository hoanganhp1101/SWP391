package com.example.diabetesmanage.service;

import com.example.diabetesmanage.config.GeminiConfig;
import com.example.diabetesmanage.dto.EncounterCreateDTO;
import com.example.diabetesmanage.model.Patient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EncounterAiAnalysis {

    private boolean used;         // true nếu kết quả đến từ Gemini
    private boolean configured;   // true nếu Gemini API key đã cấu hình
    private String error;         // thông báo lỗi (nếu có), không chặn workflow

    private String riskLevel;                 // low | medium | high | critical
    private int riskScore;                    // 0-100
    private String possibleDisease;           // bệnh khả năng
    private List<String> riskFactors = new ArrayList<>();
    private List<String> recommendedTests = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();
    private String shortExplanation;          // giải thích ngắn

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

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getPossibleDisease() {
        return possibleDisease;
    }

    public void setPossibleDisease(String possibleDisease) {
        this.possibleDisease = possibleDisease;
    }

    public List<String> getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(List<String> riskFactors) {
        this.riskFactors = riskFactors != null ? riskFactors : new ArrayList<>();
    }

    public List<String> getRecommendedTests() {
        return recommendedTests;
    }

    public void setRecommendedTests(List<String> recommendedTests) {
        this.recommendedTests = recommendedTests != null ? recommendedTests : new ArrayList<>();
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations != null ? recommendations : new ArrayList<>();
    }

    public String getShortExplanation() {
        return shortExplanation;
    }

    public void setShortExplanation(String shortExplanation) {
        this.shortExplanation = shortExplanation;
    }

    /** Tóm tắt dạng văn bản để hiển thị readonly ở Bước 2 (Treatment Plan). */
    public String buildSummaryText() {
        StringBuilder sb = new StringBuilder();
        if (riskLevel != null && !riskLevel.isBlank()) {
            sb.append("Mức độ rủi ro: ").append(riskLevel.toUpperCase());
            sb.append(" (").append(riskScore).append("/100)\n");
        }
        if (possibleDisease != null && !possibleDisease.isBlank()) {
            sb.append("Bệnh khả năng: ").append(possibleDisease).append("\n");
        }
        appendList(sb, "Yếu tố nguy cơ", riskFactors);
        appendList(sb, "Xét nghiệm đề xuất", recommendedTests);
        appendList(sb, "Khuyến nghị", recommendations);
        if (shortExplanation != null && !shortExplanation.isBlank()) {
            sb.append("Giải thích: ").append(shortExplanation);
        }
        return sb.toString().trim();
    }

    private void appendList(StringBuilder sb, String label, List<String> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        sb.append(label).append(": ");
        sb.append(String.join("; ", items));
        sb.append("\n");
    }
    private static final Logger LOG = Logger.getLogger(EncounterAiAnalysis.class.getName());

    private static final String GENERATE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private static final List<String> MODEL_FALLBACKS = Arrays.asList(
            "gemini-3.5-flash",
            "gemini-2.5-flash",
            "gemini-3.1-flash-lite",
            "gemini-3-flash",
            "gemini-flash-latest"
    );

    private final GeminiConfig geminiConfig = GeminiConfig.load();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public EncounterAiAnalysis analyze(EncounterCreateDTO form, Patient patient) {
        EncounterAiAnalysis analysis = new EncounterAiAnalysis();
        analysis.setConfigured(geminiConfig.isConfigured());

        if (!geminiConfig.isConfigured()) {
            applyRuleBasedFallback(analysis, form);
            analysis.setError("Chưa cấu hình Gemini API key — đang dùng phân tích theo quy tắc y khoa.");
            return analysis;
        }

        try {
            String prompt = buildPrompt(form, patient);
            String json = generateGeminiJsonResponse(prompt);
            parseResponse(json, analysis);
            analysis.setUsed(true);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.toString();
            LOG.log(Level.WARNING, "Gemini encounter analysis failed: " + message, e);
            applyRuleBasedFallback(analysis, form);
            analysis.setError("Gemini lỗi (" + message + ") — đang dùng phân tích theo quy tắc y khoa.");
        }
        return analysis;
    }

    private String buildPrompt(EncounterCreateDTO form, Patient patient) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là bác sĩ nội tiết chuyên tiểu đường. Phân tích dữ liệu lần khám và trả về ")
                .append("JSON THUẦN (không markdown). AI chỉ hỗ trợ, KHÔNG kê đơn, KHÔNG đưa quyết định cuối cùng.\n\n");
        sb.append("Schema JSON:\n");
        sb.append("{\n");
        sb.append("  \"riskLevel\": \"low|medium|high|critical\",\n");
        sb.append("  \"riskScore\": 0-100,\n");
        sb.append("  \"possibleDisease\": \"bệnh khả năng\",\n");
        sb.append("  \"riskFactors\": [\"yếu tố 1\", \"yếu tố 2\"],\n");
        sb.append("  \"recommendedTests\": [\"xét nghiệm 1\", \"xét nghiệm 2\"],\n");
        sb.append("  \"recommendations\": [\"khuyến nghị 1\", \"khuyến nghị 2\"],\n");
        sb.append("  \"shortExplanation\": \"giải thích ngắn 1-2 câu\"\n");
        sb.append("}\n\n");

        sb.append("Dữ liệu bệnh nhân:\n");
        if (patient != null) {
            sb.append("- Tên: ").append(nz(patient.getUser() != null ? patient.getUser().getHoTen() : null)).append("\n");
            sb.append("- Tuổi: ").append(nz(patient.getTuoi())).append("\n");
            sb.append("- Giới tính: ").append(nz(patient.getGioiTinh())).append("\n");
            sb.append("- Loại tiểu đường: ").append(nz(patient.getLoaiTieuDuong())).append("\n");
        }
        sb.append("- Loại hồ sơ: ").append(encounterTypeLabel(form.resolveEncounterType())).append("\n");
        sb.append("- Triệu chứng: ").append(nz(form.getTrieuChung())).append("\n");
        sb.append("- Tiền sử bệnh: ").append(nz(form.getTienSuBenh())).append("\n");
        sb.append("- Khám lâm sàng: ").append(nz(form.getKhamLamSang())).append("\n\n");

        sb.append("Chỉ số sức khỏe:\n");
        sb.append("- Đường huyết (mg/dL): ").append(nz(form.getDuongHuyetMgdl())).append("\n");
        sb.append("- HbA1c (%): ").append(nz(form.getHba1cPercent())).append("\n");
        sb.append("- BMI: ").append(nz(form.getBmi())).append("\n");
        sb.append("- Huyết áp: ").append(nz(form.getHuyetApTamThu())).append("/").append(nz(form.getHuyetApTamTruong())).append("\n");
        sb.append("- Nhịp tim: ").append(nz(form.getNhipTim())).append("\n");
        sb.append("- Cholesterol: ").append(nz(form.getCholesterolMmol())).append("\n");
        sb.append("- Triglyceride: ").append(nz(form.getTriglycerideMmol())).append("\n");

        return sb.toString();
    }

    private void parseResponse(String json, EncounterAiAnalysis analysis) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        if (root.has("riskLevel") && !root.get("riskLevel").isJsonNull()) {
            analysis.setRiskLevel(root.get("riskLevel").getAsString());
        }
        if (root.has("riskScore") && !root.get("riskScore").isJsonNull()) {
            try {
                analysis.setRiskScore(root.get("riskScore").getAsInt());
            } catch (NumberFormatException ignored) {
                // giữ 0 nếu AI trả về không phải số
            }
        }
        if (root.has("possibleDisease") && !root.get("possibleDisease").isJsonNull()) {
            analysis.setPossibleDisease(root.get("possibleDisease").getAsString());
        }
        analysis.setRiskFactors(toStringList(root, "riskFactors"));
        analysis.setRecommendedTests(toStringList(root, "recommendedTests"));
        analysis.setRecommendations(toStringList(root, "recommendations"));
        if (root.has("shortExplanation") && !root.get("shortExplanation").isJsonNull()) {
            analysis.setShortExplanation(root.get("shortExplanation").getAsString());
        }
    }

    private List<String> toStringList(JsonObject root, String key) {
        List<String> list = new ArrayList<>();
        if (!root.has(key) || root.get(key).isJsonNull() || !root.get(key).isJsonArray()) {
            return list;
        }
        JsonArray array = root.getAsJsonArray(key);
        for (JsonElement element : array) {
            if (element != null && !element.isJsonNull()) {
                list.add(element.getAsString());
            }
        }
        return list;
    }

    /**
     * Phân tích dự phòng theo ngưỡng y khoa khi Gemini không khả dụng.
     */
    private void applyRuleBasedFallback(EncounterAiAnalysis analysis, EncounterCreateDTO form) {
        List<String> factors = new ArrayList<>();
        List<String> tests = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        int score = 0;

        Double glucose = form.getDuongHuyetMgdl();
        if (glucose != null) {
            if (glucose >= 250) {
                factors.add("Đường huyết rất cao (" + fmt(glucose) + " mg/dL)");
                score += 45;
            } else if (glucose >= 180) {
                factors.add("Đường huyết cao (" + fmt(glucose) + " mg/dL)");
                score += 30;
            } else if (glucose < 70) {
                factors.add("Đường huyết thấp (" + fmt(glucose) + " mg/dL)");
                score += 40;
            }
        }

        Double hba1c = form.getHba1cPercent();
        if (hba1c != null) {
            if (hba1c >= 9.0) {
                factors.add("HbA1c rất cao (" + fmt(hba1c) + "%)");
                score += 35;
            } else if (hba1c >= 7.0) {
                factors.add("HbA1c cao (" + fmt(hba1c) + "%)");
                score += 20;
            }
        }

        Double bmi = form.getBmi();
        if (bmi != null && bmi >= 30) {
            factors.add("BMI cao (" + fmt(bmi) + ")");
            score += 15;
        }

        Integer sys = form.getHuyetApTamThu();
        Integer dia = form.getHuyetApTamTruong();
        if ((sys != null && sys >= 140) || (dia != null && dia >= 90)) {
            factors.add("Huyết áp cao (" + nz(sys) + "/" + nz(dia) + ")");
            score += 20;
        }

        tests.add("HbA1c định kỳ");
        tests.add("Đường huyết đói và sau ăn");
        tests.add("Lipid máu (cholesterol, triglyceride)");

        recommendations.add("Theo dõi đường huyết đều đặn.");
        recommendations.add("Điều chỉnh chế độ ăn và vận động phù hợp.");
        if (score >= 50) {
            recommendations.add("Cân nhắc đánh giá lại phác đồ điều trị.");
        }

        if (factors.isEmpty()) {
            factors.add("Chưa phát hiện bất thường rõ rệt từ chỉ số nhập vào.");
        }

        analysis.setRiskScore(Math.min(score, 100));
        analysis.setRiskLevel(score >= 70 ? "critical" : score >= 45 ? "high" : score >= 20 ? "medium" : "low");
        analysis.setPossibleDisease(encounterTypeLabel(form.resolveEncounterType()));
        analysis.setRiskFactors(new ArrayList<>(new LinkedHashSet<>(factors)));
        analysis.setRecommendedTests(tests);
        analysis.setRecommendations(recommendations);
        analysis.setShortExplanation(
                "Phân tích theo quy tắc y khoa dựa trên các chỉ số đã nhập. Bác sĩ cần đánh giá lâm sàng trước khi kê đơn.");
    }

    // ---- Gemini HTTP ----

    private String generateGeminiJsonResponse(String prompt) throws Exception {
        List<String> models = buildModelList();
        List<String> errors = new ArrayList<>();
        for (String model : models) {
            try {
                return callGeminiModel(model, prompt);
            } catch (Exception e) {
                String message = e.getMessage() != null ? e.getMessage() : e.toString();
                errors.add(model + ": " + message);
            }
        }
        throw new RuntimeException("Tất cả model Gemini đều thất bại. Chi tiết: " + String.join(" | ", errors));
    }

    private List<String> buildModelList() {
        LinkedHashSet<String> models = new LinkedHashSet<>();
        String preferred = geminiConfig.getModel();
        if (preferred != null && !preferred.isBlank()) {
            models.add(preferred.trim());
        }
        models.addAll(MODEL_FALLBACKS);
        return new ArrayList<>(models);
    }

    private String callGeminiModel(String model, String prompt) throws Exception {
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        parts.add(textPart);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("responseMimeType", "application/json");
        generationConfig.addProperty("temperature", 0.2);
        requestBody.add("generationConfig", generationConfig);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(GENERATE_URL, model)))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", geminiConfig.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API lỗi (" + response.statusCode() + ", model=" + model + ")");
        }
        return extractText(response.body());
    }

    private static String encounterTypeLabel(String typeCode) {
        if ("mau_tong_quat".equalsIgnoreCase(typeCode)) {
            return "Kết quả xét nghiệm máu tổng quát";
        }
        if ("sinh_hoa_mau".equalsIgnoreCase(typeCode)) {
            return "Kết quả sinh hóa máu";
        }
        return "Bệnh án tái khám Nội tiết";
    }

    private String extractText(String responseBody) {
        JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray candidates = root.getAsJsonArray("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("Gemini không trả về kết quả");
        }
        JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
        JsonArray parts = content.getAsJsonArray("parts");
        return parts.get(0).getAsJsonObject().get("text").getAsString();
    }

    private static String nz(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private static String fmt(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.1f", value);
    }
}
