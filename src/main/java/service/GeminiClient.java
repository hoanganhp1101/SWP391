package service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import config.GeminiConfig;
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
 * Gemini sinh toàn bộ khuyến nghị (điểm, mức, yếu tố, phân tích, khuyến nghị) từ số liệu thô.
 */
public class GeminiClient {

    private static final Pattern JSON_OBJECT = Pattern.compile("\\{[\\s\\S]*\\}");

    private final GeminiConfig config = GeminiConfig.get();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private volatile String lastError;

    public boolean isEnabled() {
        return config.isEnabled();
    }

    public String getModelName() {
        return config.getModel();
    }

    public String getLastError() {
        return lastError;
    }

    public GeminiRecommendation generate(GeminiRawInput raw) {
        lastError = null;
        if (!config.isEnabled()) {
            lastError = "Gemini tắt (enabled=false hoặc thiếu api key).";
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
        List<String> models = candidateModels(config.getModel());
        StringBuilder errors = new StringBuilder();

        for (String model : models) {
            try {
                GeminiRecommendation result = callOnce(model, key, prompt);
                if (result != null) {
                    lastError = null;
                    return result;
                }
            } catch (AuthGeminiException e) {
                lastError = e.getMessage();
                System.err.println("[Gemini] auth fail: " + lastError);
                return null;
            } catch (Exception e) {
                String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                errors.append("[").append(model).append("] ").append(msg).append(" | ");
                System.err.println("[Gemini] " + model + " → " + msg);
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
                + model
                + ":generateContent?key="
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

        JsonObject gen = new JsonObject();
        gen.addProperty("temperature", 0.5);
        gen.addProperty("maxOutputTokens", Math.max(256, Math.min(config.getMaxTokens(), 1024)));
        body.add("generationConfig", gen);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(Math.max(8000, Math.min(config.getTimeoutMs(), 30000))))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String respBody = response.body() == null ? "" : response.body();
        int code = response.statusCode();

        if (code < 200 || code >= 300) {
            String err = "HTTP " + code + " model=" + model + " " + truncate(respBody, 350);
            lastError = err;
            System.err.println("[Gemini] " + err);
            String lower = respBody.toLowerCase(Locale.ROOT);
            if (code == 401 || code == 403
                    || lower.contains("api_key_invalid")
                    || lower.contains("api key not valid")
                    || lower.contains("permission denied")) {
                throw new AuthGeminiException(err);
            }
            throw new IllegalStateException(err);
        }

        GeminiRecommendation parsed = parseResponse(respBody);
        if (parsed == null) {
            lastError = "Parse fail model=" + model + " body=" + truncate(respBody, 350);
            System.err.println("[Gemini] " + lastError);
            return null;
        }
        parsed.modelUsed = model;
        return parsed;
    }

    private String buildPrompt(GeminiRawInput r) {
        String hba1cVal = r.hasHba1c && r.hba1c != null ? String.format(Locale.ROOT, "%.1f", r.hba1c) : "null";

        return """
                Bạn là trợ lý hỗ trợ bác sĩ nội tiết (tiểu đường) tại Việt Nam.
                Dưới đây là số liệu thô từ hệ thống. KHÔNG bịa thêm số, không nêu tên bệnh nhân.
                Tự đánh giá rủi ro và viết khuyến nghị. Đây chỉ là gợi ý hỗ trợ, không thay quyết định lâm sàng.

                ```json
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
                ```

                Ghi chú: TIR = %% thời gian đường huyết trong [glucose_low, glucose_high].
                muc_canh_bao chỉ được một trong: nguy_hiem | cao | trung_binh.
                diem_nguy_co là số nguyên 0–100.

                Trả về đúng một JSON object, không markdown:
                {
                  "diem_nguy_co": 0,
                  "muc_canh_bao": "trung_binh",
                  "yeu_to_nguy_co": "yếu tố 1; yếu tố 2",
                  "phan_tich": "1-3 câu",
                  "khuyen_nghi": "1. ...\\n2. ...\\n3. ..."
                }
                """.formatted(
                r.scanDays,
                r.totalReadings,
                r.avgGlucose,
                r.tirPercent,
                r.hypoCount,
                r.hyperCount,
                r.dangerCount,
                hba1cVal,
                r.measuredRecently,
                r.glucoseLow,
                r.glucoseHigh,
                r.glucoseDanger,
                r.hba1cTarget,
                r.hba1cPoor,
                r.daysNoMeasure
        );
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
            JsonElement textEl = parts.get(0).getAsJsonObject().get("text");
            if (textEl == null || textEl.isJsonNull()) {
                return null;
            }
            String text = textEl.getAsString();
            if (text == null || text.isBlank()) {
                return null;
            }

            text = text.trim();
            if (text.startsWith("```")) {
                text = text.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
            }

            JsonObject json;
            try {
                json = JsonParser.parseString(text).getAsJsonObject();
            } catch (Exception ex) {
                Matcher m = JSON_OBJECT.matcher(text);
                if (!m.find()) {
                    return null;
                }
                json = JsonParser.parseString(m.group()).getAsJsonObject();
            }

            String phanTich = readString(json, "phan_tich");
            String khuyenNghi = readString(json, "khuyen_nghi");
            String yeuTo = readString(json, "yeu_to_nguy_co");
            String muc = readString(json, "muc_canh_bao");
            if (phanTich == null || phanTich.isBlank() || khuyenNghi == null || khuyenNghi.isBlank()) {
                return null;
            }

            double diem = 50;
            if (json.has("diem_nguy_co") && !json.get("diem_nguy_co").isJsonNull()) {
                diem = json.get("diem_nguy_co").getAsDouble();
            }
            diem = Math.max(0, Math.min(100, diem));

            muc = normalizeLevel(muc);

            Integer tokens = null;
            if (root.has("usageMetadata")) {
                JsonObject usage = root.getAsJsonObject("usageMetadata");
                if (usage.has("totalTokenCount")) {
                    tokens = usage.get("totalTokenCount").getAsInt();
                }
            }

            GeminiRecommendation out = new GeminiRecommendation();
            out.diemNguyCo = diem;
            out.mucCanhBao = muc;
            out.yeuToNguyCo = yeuTo == null || yeuTo.isBlank() ? "Theo đánh giá Gemini từ chỉ số gần đây" : yeuTo.trim();
            out.phanTich = phanTich.trim();
            out.khuyenNghi = khuyenNghi.trim();
            out.tokens = tokens;
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalizeLevel(String muc) {
        if (muc == null || muc.isBlank()) {
            return "trung_binh";
        }
        String m = muc.trim().toLowerCase(Locale.ROOT);
        if (m.contains("nguy") || m.equals("nguy_hiem")) {
            return "nguy_hiem";
        }
        if (m.contains("cao") || m.equals("high")) {
            return "cao";
        }
        return "trung_binh";
    }

    private static String readString(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            return null;
        }
        return json.get(key).getAsString();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
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
        AuthGeminiException(String message) {
            super(message);
        }
    }
}
