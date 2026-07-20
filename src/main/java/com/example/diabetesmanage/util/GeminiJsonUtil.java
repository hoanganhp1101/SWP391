package com.example.diabetesmanage.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Trích xuất và parse JSON từ response Gemini (markdown, text thừa, nhiều object...).
 */
public final class GeminiJsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private GeminiJsonUtil() {
    }

    public static void logRawResponse(String rawResponse) {
        System.out.println("========== GEMINI RAW RESPONSE ==========");
        System.out.println(rawResponse != null ? rawResponse : "(null)");
        System.out.println("=========================================");
    }

    public static ParseResult parse(String rawResponse) {
        return parse(rawResponse, true);
    }

    public static ParseResult parse(String rawResponse, boolean logRaw) {
        if (logRaw) {
            logRawResponse(rawResponse);
        }

        if (rawResponse == null || rawResponse.isBlank()) {
            if (logRaw) {
                logParseError("Gemini trả response rỗng hoặc null", rawResponse);
            }
            return ParseResult.failure("Gemini trả response rỗng hoặc null", null, null);
        }

        String cleaned = cleanRawResponse(rawResponse);
        String extracted = extractJsonObject(cleaned);
        if (extracted == null || extracted.isBlank()) {
            if (logRaw) {
                logParseError("Không tìm thấy JSON object trong response", rawResponse);
            }
            return ParseResult.failure("Không tìm thấy JSON object trong response", cleaned, null);
        }

        try {
            JsonNode node = OBJECT_MAPPER.readTree(extracted);
            if (node == null || !node.isObject()) {
                if (logRaw) {
                    logParseError("JSON không phải object", rawResponse);
                }
                return ParseResult.failure("JSON không phải object", cleaned, extracted);
            }
            return ParseResult.success(node, extracted);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.toString();
            if (logRaw) {
                logParseError("Malformed JSON: " + message, rawResponse);
            }
            return ParseResult.failure("Malformed JSON: " + message, cleaned, extracted);
        }
    }

    public static JsonElement toGsonObject(JsonNode node) {
        if (node == null) {
            return null;
        }
        return JsonParser.parseString(node.toString());
    }

    private static void logParseError(String message, String rawResponse) {
        System.err.println("Gemini JSON parse error: " + message);
        logRawResponse(rawResponse);
    }

    /**
     * trim, bỏ markdown fence ```json ... ```
     */
    public static String cleanRawResponse(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();
        while (s.startsWith("```")) {
            s = s.replaceFirst("^```(?:json|JSON)?\\s*", "");
            int endFence = s.lastIndexOf("```");
            if (endFence >= 0) {
                s = s.substring(0, endFence);
            }
            s = s.trim();
        }
        return s.trim();
    }

    /**
     * Tìm object JSON đầu tiên (cân bằng ngoặc), fallback first '{' .. last '}'.
     */
    public static String extractJsonObject(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String balanced = extractFirstBalancedObject(raw.trim());
        if (balanced != null && !balanced.isBlank()) {
            return balanced;
        }
        String s = raw.trim();
        int first = s.indexOf('{');
        int last = s.lastIndexOf('}');
        if (first >= 0 && last > first) {
            return s.substring(first, last + 1);
        }
        return null;
    }

    private static String extractFirstBalancedObject(String s) {
        int start = s.indexOf('{');
        if (start < 0) {
            return null;
        }
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return s.substring(start, i + 1);
                }
                if (depth < 0) {
                    return null;
                }
            }
        }
        return null;
    }

    public static final class ParseResult {
        private final boolean success;
        private final JsonNode jsonNode;
        private final String cleanedText;
        private final String extractedJson;
        private final String error;

        private ParseResult(boolean success, JsonNode jsonNode, String cleanedText,
                            String extractedJson, String error) {
            this.success = success;
            this.jsonNode = jsonNode;
            this.cleanedText = cleanedText;
            this.extractedJson = extractedJson;
            this.error = error;
        }

        public static ParseResult success(JsonNode jsonNode, String extractedJson) {
            return new ParseResult(true, jsonNode, null, extractedJson, null);
        }

        public static ParseResult failure(String error, String cleanedText, String extractedJson) {
            return new ParseResult(false, null, cleanedText, extractedJson, error);
        }

        public boolean isSuccess() {
            return success;
        }

        public JsonNode getJsonNode() {
            return jsonNode;
        }

        public String getCleanedText() {
            return cleanedText;
        }

        public String getExtractedJson() {
            return extractedJson;
        }

        public String getError() {
            return error;
        }
    }
}
