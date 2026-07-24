package config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Đọc cấu hình Gemini từ classpath gemini.properties.
 * Ưu tiên biến môi trường GEMINI_API_KEY nếu có.
 */
public final class GeminiConfig {

    private static final GeminiConfig INSTANCE = new GeminiConfig();

    private final boolean enabled;
    private final String apiKey;
    private final String model;
    private final int maxTokens;
    private final int timeoutMs;

    private GeminiConfig() {
        Properties p = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("gemini.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String envKey = System.getenv("GEMINI_API_KEY");
        String fileKey = p.getProperty("gemini.api.key", "").trim();
        this.apiKey = (envKey != null && !envKey.isBlank()) ? envKey.trim() : fileKey;

        this.enabled = Boolean.parseBoolean(p.getProperty("gemini.enabled", "false"))
                && this.apiKey != null && !this.apiKey.isBlank();
        this.model = p.getProperty("gemini.model", "gemini-2.0-flash").trim();
        this.maxTokens = parseInt(p.getProperty("gemini.max.tokens"), 2048);
        this.timeoutMs = parseInt(p.getProperty("gemini.timeout.ms"), 20000);
    }

    public static GeminiConfig get() {
        return INSTANCE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    private static int parseInt(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
