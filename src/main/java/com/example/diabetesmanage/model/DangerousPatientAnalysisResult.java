package com.example.diabetesmanage.model;

import java.util.ArrayList;
import java.util.List;

public class DangerousPatientAnalysisResult {

    private List<UrgentPatientAlert> dangerousPatients = new ArrayList<>();
    private List<String> aiInsights = new ArrayList<>();
    private String aiSummary;
    private boolean geminiUsed;
    private int totalDangerousCount;
    private boolean geminiConfigured;
    private String geminiError;
    private String geminiConfigInfo;

    public List<UrgentPatientAlert> getDangerousPatients() {
        return dangerousPatients;
    }

    public void setDangerousPatients(List<UrgentPatientAlert> dangerousPatients) {
        this.dangerousPatients = dangerousPatients;
    }

    public List<String> getAiInsights() {
        return aiInsights;
    }

    public void setAiInsights(List<String> aiInsights) {
        this.aiInsights = aiInsights;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }

    public boolean isGeminiUsed() {
        return geminiUsed;
    }

    public void setGeminiUsed(boolean geminiUsed) {
        this.geminiUsed = geminiUsed;
    }

    public int getTotalDangerousCount() {
        return totalDangerousCount;
    }

    public void setTotalDangerousCount(int totalDangerousCount) {
        this.totalDangerousCount = totalDangerousCount;
    }

    public boolean isGeminiConfigured() {
        return geminiConfigured;
    }

    public void setGeminiConfigured(boolean geminiConfigured) {
        this.geminiConfigured = geminiConfigured;
    }

    public String getGeminiError() {
        return geminiError;
    }

    public void setGeminiError(String geminiError) {
        this.geminiError = geminiError;
    }

    public String getGeminiConfigInfo() {
        return geminiConfigInfo;
    }

    public void setGeminiConfigInfo(String geminiConfigInfo) {
        this.geminiConfigInfo = geminiConfigInfo;
    }
}
