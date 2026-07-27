package com.example.diabetesmanage.service;

import com.example.diabetesmanage.config.GeminiConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gọi Gemini để sinh khuyến nghị hỗ trợ bác sĩ từ dữ liệu sức khỏe thô.
 */
public class DoctorRecommendationGeminiClient {

    private static final Pattern JSON_OBJECT = Pattern.compile("\\{[\\s\\S]*\\}");
    private static final int MAX_OUTPUT_TOKENS = 1024;

    private final GeminiConfig config = GeminiConfig.load();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private volatile String lastError;

    public boolean isEnabled() {
        return config.isConfigured();
    }

    public String getModelName() {
        return config.getModel();
    }

    public String getLastError() {
        return lastError;
    }

    public GeminiRecommendation generate(GeminiRawInput raw) {
        lastError = null;
        if (!config.isConfigured()) {
            lastError = "Gemini chưa được cấu hình API key.";
            return null;
        }
        if (raw == null) {
            lastError = "Thiếu dữ liệu đầu vào.";
            return null;
        }

        String key = config.getApiKey() == null ? "" : config.getApiKey().trim();
        if (key.startsWith("PASTE_") || key.length() < 20) {
            lastError = "API key chưa hợp lệ. Lấy key tại https://aistudio.google.com/apikey.";
            return null;
        }

        String prompt = buildPrompt(raw);
        StringBuilder errors = new StringBuilder();
        for (String model : candidateModels(config.getModel())) {
            try {
                GeminiRecommendation result = callOnce(model, key, prompt);
                if (result != null) {
                    lastError = null;
                    return result;
                }
            } catch (AuthGeminiException e) {
                lastError = e.getMessage();
                System.err.println("[Gemini] Lỗi xác thực: " + lastError);
                return null;
            } catch (Exception e) {
                String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                errors.append('[').append(model).append("] ").append(message).append(" | ");
                System.err.println("[Gemini] " + model + " -> " + message);
            }
        }

        lastError = errors.length() == 0
                ? "Gemini không trả về JSON hợp lệ."
                : errors.toString();
        return null;
    }

    private List<String> candidateModels(String preferred) {
        Set<String> models = new LinkedHashSet<>();
        if (preferred != null && !preferred.isBlank()) {
            models.add(preferred.trim());
        }
        models.add("gemini-3.1-flash-preview");
        models.add("gemini-2.0-flash-lite");
        models.add("gemini-2.0-flash");
        models.add("gemini-flash-latest");
        return new ArrayList<>(models);
    }

    private GeminiRecommendation callOnce(String model, String apiKey, String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key="
                + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        return postGenerate(url, model, prompt);
    }

    private GeminiRecommendation postGenerate(String url, String model, String prompt) throws Exception {
        JsonObject body = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        body.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.5);
        generationConfig.addProperty("maxOutputTokens", MAX_OUTPUT_TOKENS);
        body.add("generationConfig", generationConfig);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = http.send(
                request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String responseBody = response.body() == null ? "" : response.body();
        int statusCode = response.statusCode();

        if (statusCode < 200 || statusCode >= 300) {
            String error = "HTTP " + statusCode + " model=" + model + " " + truncate(responseBody, 350);
            lastError = error;
            String lower = responseBody.toLowerCase(Locale.ROOT);
            if (statusCode == 401 || statusCode == 403
                    || lower.contains("api_key_invalid")
                    || lower.contains("api key not valid")
                    || lower.contains("permission denied")) {
                throw new AuthGeminiException(error);
            }
            throw new IllegalStateException(error);
        }

        GeminiRecommendation parsed = parseResponse(responseBody);
        if (parsed == null) {
            lastError = "Không đọc được phản hồi model=" + model + ": " + truncate(responseBody, 350);
            return null;
        }
        parsed.modelUsed = model;
        return parsed;
    }

    private String buildPrompt(GeminiRawInput r) {
        String hba1cValue = r.hasHba1c && r.hba1c != null
                ? String.format(Locale.ROOT, "%.1f", r.hba1c)
                : "null";

        return """
                Bạn là trợ lý hỗ trợ bác sĩ nội tiết tại Việt Nam.
                Dữ liệu dưới đây lấy trực tiếp từ hệ thống. Không bịa thêm số liệu và không nêu tên bệnh nhân.
                Hãy đánh giá rủi ro và viết khuyến nghị hỗ trợ; kết quả không thay thế quyết định lâm sàng.

                {
                  "cua_so_ngay": %d,
                  "so_lan_do": %d,
                  "duong_huyet_tb_mgdl": %.0f,
                  "tir_percent": %.1f,
                  "so_lan_hypo": %d,
                  "so_lan_hyper": %d,
                  "so_lan_danger": %d,
                  "hba1c_percent": %s,
                  "co_do_gan_day": %s,
                  "nguong": {
                    "glucose_low": %d,
                    "glucose_high": %d,
                    "glucose_danger": %d,
                    "hba1c_target": %.1f,
                    "hba1c_poor": %.1f,
                    "days_no_measure": %d
                  }
                }

                TIR là phần trăm số lần đo nằm trong [glucose_low, glucose_high].
                muc_canh_bao chỉ được là nguy_hiem, cao hoặc trung_binh.
                diem_nguy_co là số nguyên từ 0 đến 100.

                Chỉ trả về một JSON object, không dùng markdown:
                {
                  "diem_nguy_co": 0,
                  "muc_canh_bao": "trung_binh",
                  "yeu_to_nguy_co": "yếu tố 1; yếu tố 2",
                  "phan_tich": "1-3 câu",
                  "khuyen_nghi": "1. ...\\n2. ...\\n3. ..."
                }
                """.formatted(
                r.scanDays, r.totalReadings, r.avgGlucose, r.tirPercent,
                r.hypoCount, r.hyperCount, r.dangerCount, hba1cValue,
                r.measuredRecently, r.glucoseLow, r.glucoseHigh, r.glucoseDanger,
                r.hba1cTarget, r.hba1cPoor, r.daysNoMeasure);
    }

    private GeminiRecommendation parseResponse(String body) {
        try {
            JsonObject root = JsonParser.parseString(body).getAsJsonObject();
            JsonArray candidates = root.getAsJsonArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return null;
            }
            JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
            if (content == null) {
                return null;
            }
            JsonArray parts = content.getAsJsonArray("parts");
            if (parts == null || parts.isEmpty()) {
                return null;
            }
            JsonElement textElement = parts.get(0).getAsJsonObject().get("text");
            if (textElement == null || textElement.isJsonNull() || textElement.getAsString().isBlank()) {
                return null;
            }

            String text = textElement.getAsString().trim();
            if (text.startsWith("```")) {
                text = text.replaceFirst("^```(?:json)?\\s*", "")
                        .replaceFirst("\\s*```$", "").trim();
            }

            JsonObject json;
            try {
                json = JsonParser.parseString(text).getAsJsonObject();
            } catch (Exception ignored) {
                Matcher matcher = JSON_OBJECT.matcher(text);
                if (!matcher.find()) {
                    return null;
                }
                json = JsonParser.parseString(matcher.group()).getAsJsonObject();
            }

            String analysis = readString(json, "phan_tich");
            String recommendation = readString(json, "khuyen_nghi");
            if (analysis == null || analysis.isBlank()
                    || recommendation == null || recommendation.isBlank()) {
                return null;
            }

            double score = json.has("diem_nguy_co") && !json.get("diem_nguy_co").isJsonNull()
                    ? json.get("diem_nguy_co").getAsDouble() : 50;

            GeminiRecommendation result = new GeminiRecommendation();
            result.diemNguyCo = Math.max(0, Math.min(100, score));
            result.mucCanhBao = normalizeLevel(readString(json, "muc_canh_bao"));
            String riskFactors = readString(json, "yeu_to_nguy_co");
            result.yeuToNguyCo = riskFactors == null || riskFactors.isBlank()
                    ? "Theo đánh giá của Gemini từ chỉ số gần đây" : riskFactors.trim();
            result.phanTich = analysis.trim();
            result.khuyenNghi = recommendation.trim();

            if (root.has("usageMetadata")) {
                JsonObject usage = root.getAsJsonObject("usageMetadata");
                if (usage.has("totalTokenCount")) {
                    result.tokens = usage.get("totalTokenCount").getAsInt();
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalizeLevel(String value) {
        if (value == null || value.isBlank()) {
            return "trung_binh";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("nguy") || normalized.equals("danger")) {
            return "nguy_hiem";
        }
        if (normalized.contains("cao") || normalized.equals("high")) {
            return "cao";
        }
        return "trung_binh";
    }

    private static String readString(JsonObject json, String key) {
        return !json.has(key) || json.get(key).isJsonNull() ? null : json.get(key).getAsString();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }

    public static final class GeminiRecommendation {
        public double diemNguyCo;
        public String mucCanhBao;
        public String yeuToNguyCo;
        public String phanTich;
        public String khuyenNghi;
        public Integer tokens;
        public String modelUsed;
    }

    private static final class AuthGeminiException extends Exception {
        private AuthGeminiException(String message) {
            super(message);
        }
    }
}
