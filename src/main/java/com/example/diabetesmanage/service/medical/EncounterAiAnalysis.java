package com.example.diabetesmanage.service.medical;

import java.util.ArrayList;
import java.util.List;

/**
 * Kết quả phân tích AI (Gemini) cho một Medical Encounter ở Bước 1.
 *
 * <p>AI chỉ mang tính hỗ trợ: gợi ý mức độ rủi ro, bệnh khả năng, xét nghiệm và khuyến nghị.
 * AI KHÔNG kê đơn và KHÔNG đưa quyết định cuối cùng — bác sĩ chịu trách nhiệm ở Bước 2.
 * Object này không được lưu xuống database (project không có bảng ai_analysis) mà chỉ
 * trả về JSP qua AJAX và giữ tạm trong session giữa hai bước.
 */
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
}
