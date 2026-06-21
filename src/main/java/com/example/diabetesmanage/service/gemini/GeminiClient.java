package com.example.diabetesmanage.service.gemini;

import com.example.diabetesmanage.config.GeminiConfig;
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

public class GeminiClient {

    private static final String GENERATE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private static final String LIST_MODELS_URL =
            "https://generativelanguage.googleapis.com/v1beta/models";

    private static final List<String> MODEL_FALLBACKS = Arrays.asList(
            "gemini-3.5-flash",
            "gemini-2.5-flash",
            "gemini-3.1-flash-lite",
            "gemini-3-flash",
            "gemini-flash-latest"
    );

    private final GeminiConfig config;
    private final HttpClient httpClient;

    public GeminiClient() {
        this.config = GeminiConfig.load();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public String generateJsonResponse(String prompt) throws Exception {

        if (!config.isConfigured()) {
            if (!config.isPropertiesFileFound()) {
                throw new IllegalStateException(
                        "Không tìm thấy file src/main/resources/gemini.properties. "
                                + "Hãy copy từ gemini.properties.example và rebuild project."
                );
            }
            throw new IllegalStateException("Gemini API key chưa được cấu hình trong gemini.properties");
        }

        List<String> modelsToTry = buildModelList();
        List<String> errors = new ArrayList<>();

        for (String model : modelsToTry) {
            try {
                return callModel(model, prompt);
            } catch (Exception e) {
                String message = e.getMessage() != null ? e.getMessage() : e.toString();
                errors.add(model + ": " + message);
                System.err.println("Gemini model " + model + " thất bại: " + message);
            }
        }

        String discoveredModel = discoverGenerateContentModel();
        if (discoveredModel != null && !modelsToTry.contains(discoveredModel)) {
            try {
                return callModel(discoveredModel, prompt);
            } catch (Exception e) {
                String message = e.getMessage() != null ? e.getMessage() : e.toString();
                errors.add(discoveredModel + ": " + message);
            }
        }

        throw new RuntimeException(
                "Tất cả model Gemini đều thất bại. "
                        + "Hãy đổi gemini.model trong gemini.properties (gợi ý: gemini-3.5-flash). "
                        + "Chi tiết: " + String.join(" | ", errors)
        );
    }

    private List<String> buildModelList() {
        String preferred = config.getModel();
        LinkedHashSet<String> models = new LinkedHashSet<>();
        if (preferred != null && !preferred.isBlank()) {
            models.add(preferred.trim());
        }
        models.addAll(MODEL_FALLBACKS);
        return new ArrayList<>(models);
    }

    private String discoverGenerateContentModel() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(LIST_MODELS_URL + "?pageSize=100"))
                    .timeout(Duration.ofSeconds(20))
                    .header("x-goog-api-key", config.getApiKey())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() != 200) {
                return null;
            }

            JsonArray models = JsonParser.parseString(response.body())
                    .getAsJsonObject()
                    .getAsJsonArray("models");

            if (models == null) {
                return null;
            }

            for (JsonElement element : models) {
                JsonObject model = element.getAsJsonObject();
                String name = model.get("name").getAsString();
                if (!name.contains("gemini")) {
                    continue;
                }

                JsonArray methods = model.getAsJsonArray("supportedGenerationMethods");
                if (methods == null) {
                    continue;
                }

                boolean supportsGenerate = false;
                for (JsonElement method : methods) {
                    if ("generateContent".equals(method.getAsString())) {
                        supportsGenerate = true;
                        break;
                    }
                }

                if (supportsGenerate) {
                    return name.replace("models/", "");
                }
            }
        } catch (Exception e) {
            System.err.println("Không lấy được danh sách model Gemini: " + e.getMessage());
        }

        return null;
    }

    private String callModel(String model, String prompt) throws Exception {

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

        String url = String.format(GENERATE_URL, model);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", config.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(
                        requestBody.toString(),
                        StandardCharsets.UTF_8
                ))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Gemini API lỗi (" + response.statusCode() + ", model=" + model + "): "
                            + shorten(response.body())
            );
        }

        return extractTextFromResponse(response.body());
    }

    private String extractTextFromResponse(String responseBody) {
        JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray candidates = root.getAsJsonArray("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("Gemini không trả về kết quả: " + shorten(responseBody));
        }

        JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
        JsonObject content = firstCandidate.getAsJsonObject("content");
        JsonArray parts = content.getAsJsonArray("parts");
        JsonObject firstPart = parts.get(0).getAsJsonObject();
        return firstPart.get("text").getAsString();
    }

    private String shorten(String text) {
        if (text == null) {
            return "";
        }
        return text.length() > 300 ? text.substring(0, 300) + "..." : text;
    }
}
