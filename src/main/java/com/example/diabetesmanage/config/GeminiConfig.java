package com.example.diabetesmanage.config;

import java.io.InputStream;
import java.util.Properties;

public class GeminiConfig {

    private static final String PROPERTIES_FILE = "gemini.properties";

    private final String apiKey;
    private final String model;
    private final boolean configured;
    private final boolean propertiesFileFound;
    private final String configSource;

    public GeminiConfig() {
        Properties props = loadProperties();
        propertiesFileFound = props.getProperty("_loaded", "false").equals("true");

        String envKey = firstNonBlank(
                System.getenv("GEMINI_API_KEY"),
                System.getenv("GOOGLE_API_KEY")
        );
        String fileKey = props.getProperty("gemini.api.key", "").trim();

        if (envKey != null) {
            apiKey = envKey;
            configSource = "environment";
        } else if (!fileKey.isBlank()) {
            apiKey = fileKey;
            configSource = "gemini.properties";
        } else {
            apiKey = "";
            configSource = "none";
        }

        model = props.getProperty("gemini.model", "gemini-3.5-flash").trim();
        configured = !apiKey.isBlank() && !apiKey.equals("YOUR_API_KEY_HERE");
    }

    public static GeminiConfig load() {
        return new GeminiConfig();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (in != null) {
                props.load(in);
                props.setProperty("_loaded", "true");
            }
        } catch (Exception e) {
            System.err.println("Không đọc được gemini.properties: " + e.getMessage());
        }
        return props;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public boolean isConfigured() {
        return configured;
    }

    public boolean isPropertiesFileFound() {
        return propertiesFileFound;
    }

    public String getConfigSource() {
        return configSource;
    }

    public String getMaskedApiKey() {
        if (apiKey.length() <= 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
